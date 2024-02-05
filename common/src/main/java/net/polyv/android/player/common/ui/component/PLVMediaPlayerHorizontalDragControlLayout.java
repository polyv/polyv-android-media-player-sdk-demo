package net.polyv.android.player.common.ui.component;

import static com.plv.foundationsdk.component.livedata.PLVLiveDataExt.observeUntilViewDetached;
import static com.plv.foundationsdk.utils.PLVSugarUtil.clamp;
import static com.plv.foundationsdk.utils.PLVSugarUtil.requireNotNull;
import static com.plv.foundationsdk.utils.PLVTimeUnit.minutes;

import androidx.lifecycle.Observer;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;

import net.polyv.android.player.business.scene.common.player.IPLVMediaPlayer;
import net.polyv.android.player.common.ui.localprovider.PLVMediaPlayerLocalProvider;
import net.polyv.android.player.common.ui.viewmodel.PLVMediaPlayerControlViewModel;
import net.polyv.android.player.common.ui.viewmodel.action.PLVMediaPlayerControlAction;
import net.polyv.android.player.common.ui.viewmodel.viewstate.PLVMediaPlayerControlViewState;
import net.polyv.android.player.common.utils.extensions.PLVMediaPlayerExtensions;

/**
 * @author Hoshiiro
 */
public class PLVMediaPlayerHorizontalDragControlLayout extends FrameLayout implements GestureDetector.OnGestureListener {

    private final GestureDetector gestureDetector = new GestureDetector(getContext(), this);

    private PLVMediaPlayerControlViewState currentViewState = null;

    private boolean isScrolling = false;
    private boolean isScrollingHorizontal = false;

    private long position = -1;
    private long duration = -1;

    public PLVMediaPlayerHorizontalDragControlLayout(@NonNull Context context) {
        super(context);
    }

    public PLVMediaPlayerHorizontalDragControlLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PLVMediaPlayerHorizontalDragControlLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public boolean handleOnTouchEvent(MotionEvent event) {
        if (!isAllowControl()) {
            return false;
        }
        boolean consume = isScrollingHorizontal;
        gestureDetector.onTouchEvent(event);
        if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
            onActionUp();
        }
        return consume;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (isInEditMode()) return;

        observeUntilViewDetached(
                requireNotNull(PLVMediaPlayerLocalProvider.localControlViewModel.on(this).current())
                        .getControlViewStateLiveData(),
                this,
                new Observer<PLVMediaPlayerControlViewState>() {
                    @Override
                    public void onChanged(@Nullable @org.jetbrains.annotations.Nullable PLVMediaPlayerControlViewState viewState) {
                        currentViewState = viewState;
                    }
                }
        );
    }

    @Override
    public boolean onDown(@NonNull MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(@NonNull MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(@NonNull MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(@NonNull MotionEvent e1, @NonNull MotionEvent e2, float distanceX, float distanceY) {
        if (!isScrolling) {
            isScrolling = true;
            isScrollingHorizontal = Math.abs(distanceX) > Math.abs(distanceY);
            saveCurrentProgress();
        }
        if (isScrollingHorizontal) {
            handleScrolling(e1, e2);
        }
        return isScrollingHorizontal;
    }

    protected void onActionUp() {
        if (isScrollingHorizontal) {
            handleSeek();
        }
        isScrolling = false;
        isScrollingHorizontal = false;
        position = -1;
        duration = -1;
        hintDragging(0, false);
    }

    @Override
    public void onLongPress(@NonNull MotionEvent e) {

    }

    @Override
    public boolean onFling(@NonNull MotionEvent e1, @NonNull MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    private void saveCurrentProgress() {
        IPLVMediaPlayer mediaPlayer = PLVMediaPlayerLocalProvider.localMediaPlayer.on(this).current();
        if (mediaPlayer == null) {
            return;
        }
        Long position = mediaPlayer.getStateListenerRegistry().getProgressState().getValue();
        Long duration = mediaPlayer.getStateListenerRegistry().getDurationState().getValue();
        this.position = position == null ? -1 : position;
        this.duration = duration == null ? -1 : duration;
    }

    private void handleScrolling(@NonNull MotionEvent start, @NonNull MotionEvent current) {
        if (position < 0 || duration < 0) {
            return;
        }
        float dx = current.getX() - start.getX();
        float percent = dx / ScreenUtils.getScreenOrientatedWidth();
        float dprogress = percent * minutes(3).toMillis();
        float targetProgress = clamp(position + dprogress, 0, duration);
        hintDragging((long) targetProgress, true);
    }

    private void handleSeek() {
        IPLVMediaPlayer mediaPlayer = PLVMediaPlayerLocalProvider.localMediaPlayer.on(this).current();
        if (mediaPlayer == null || currentViewState == null || currentViewState.progressSeekBarDragPosition < 0) {
            return;
        }
        PLVMediaPlayerExtensions.seekTo(mediaPlayer, currentViewState.progressSeekBarDragPosition);
    }

    private void hintDragging(long targetProgress, boolean isDragging) {
        PLVMediaPlayerControlViewModel controlViewModel = PLVMediaPlayerLocalProvider.localControlViewModel.on(this).current();
        if (controlViewModel != null) {
            controlViewModel.requestControl(PLVMediaPlayerControlAction.progressSeekBarDrag(targetProgress, isDragging));
        }
    }

    protected boolean isAllowControl() {
        return currentViewState != null && !currentViewState.controllerLocking;
    }

}
