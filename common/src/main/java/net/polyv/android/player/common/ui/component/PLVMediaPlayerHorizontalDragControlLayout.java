package net.polyv.android.player.common.ui.component;

import static net.polyv.android.player.sdk.foundation.graphics.DisplaysKt.getScreenWidth;
import static net.polyv.android.player.sdk.foundation.lang.Duration.minutes;
import static net.polyv.android.player.sdk.foundation.lang.NumbersKt.clamp;
import static net.polyv.android.player.sdk.foundation.lang.PreconditionsKt.requireNotNull;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import net.polyv.android.player.common.di.PLVMediaPlayerLocalProvider;
import net.polyv.android.player.common.modules.media.viewmodel.PLVMPMediaViewModel;
import net.polyv.android.player.common.modules.media.viewmodel.viewstate.PLVMPMediaPlayViewState;
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.DragSeekBarAction;
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.PLVMPMediaControllerViewModel;
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.viewstate.PLVMPMediaControllerViewState;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

/**
 * @author Hoshiiro
 */
public class PLVMediaPlayerHorizontalDragControlLayout extends FrameLayout implements GestureDetector.OnGestureListener {

    private final GestureDetector gestureDetector = new GestureDetector(getContext(), this);

    private PLVMPMediaControllerViewState controllerViewState = null;

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

        requireNotNull(PLVMediaPlayerLocalProvider.localDependScope.on(this).current())
                .get(PLVMPMediaControllerViewModel.class)
                .getMediaControllerViewState()
                .observeUntilViewDetached(this, new Function1<PLVMPMediaControllerViewState, Unit>() {
                    @Override
                    public Unit invoke(PLVMPMediaControllerViewState viewState) {
                        controllerViewState = viewState;
                        return null;
                    }
                });
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
            requireNotNull(PLVMediaPlayerLocalProvider.localDependScope.on(this).current())
                    .get(PLVMPMediaControllerViewModel.class)
                    .handleDragSeekBar(DragSeekBarAction.FINISH);
        }
        isScrolling = false;
        isScrollingHorizontal = false;
        position = -1;
        duration = -1;
    }

    @Override
    public void onLongPress(@NonNull MotionEvent e) {

    }

    @Override
    public boolean onFling(@NonNull MotionEvent e1, @NonNull MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    private void saveCurrentProgress() {
        PLVMPMediaPlayViewState viewState = requireNotNull(PLVMediaPlayerLocalProvider.localDependScope.on(this).current())
                .get(PLVMPMediaViewModel.class)
                .getMediaPlayViewState()
                .getValue();
        if (viewState == null) {
            return;
        }
        this.position = viewState.getCurrentProgress();
        this.duration = viewState.getDuration();
    }

    private void handleScrolling(@NonNull MotionEvent start, @NonNull MotionEvent current) {
        if (position < 0 || duration < 0) {
            return;
        }
        float dx = current.getX() - start.getX();
        float percent = dx / getScreenWidth().px();
        float dprogress = percent * minutes(3).toMillis();
        float targetProgress = clamp(position + dprogress, 0, duration);
        requireNotNull(PLVMediaPlayerLocalProvider.localDependScope.on(this).current())
                .get(PLVMPMediaControllerViewModel.class)
                .handleDragSeekBar(DragSeekBarAction.DRAG, (long) targetProgress);
    }

    protected boolean isAllowControl() {
        return controllerViewState != null && !controllerViewState.getControllerLocking();
    }

}
