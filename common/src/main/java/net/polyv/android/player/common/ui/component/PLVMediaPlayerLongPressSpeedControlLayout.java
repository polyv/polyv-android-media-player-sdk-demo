package net.polyv.android.player.common.ui.component;

import static com.plv.foundationsdk.component.livedata.PLVLiveDataExt.observeUntilViewDetached;
import static com.plv.foundationsdk.utils.PLVSugarUtil.requireNotNull;

import android.arch.lifecycle.Observer;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import com.plv.foundationsdk.component.exts.Nullables;
import com.plv.foundationsdk.utils.PLVSugarUtil;

import net.polyv.android.player.business.scene.common.player.IPLVMediaPlayer;
import net.polyv.android.player.common.ui.localprovider.PLVMediaPlayerLocalProvider;
import net.polyv.android.player.common.ui.viewmodel.PLVMediaPlayerControlViewModel;
import net.polyv.android.player.common.ui.viewmodel.action.PLVMediaPlayerControlAction;
import net.polyv.android.player.common.ui.viewmodel.viewstate.PLVMediaPlayerControlViewState;
import net.polyv.android.player.core.api.listener.state.PLVMediaPlayerPlayingState;

/**
 * @author Hoshiiro
 */
public class PLVMediaPlayerLongPressSpeedControlLayout extends FrameLayout implements GestureDetector.OnGestureListener {

    private static final float SPEED_ON_LONG_PRESS = 2F;

    private final GestureDetector gestureDetector = new GestureDetector(getContext(), this);

    protected PLVMediaPlayerPlayingState currentPlayingState = null;
    protected PLVMediaPlayerControlViewState currentViewState = null;

    private boolean isLongPressing = false;
    private float speedBeforeLongPressControl = 1F;

    public PLVMediaPlayerLongPressSpeedControlLayout(@NonNull Context context) {
        super(context);
    }

    public PLVMediaPlayerLongPressSpeedControlLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PLVMediaPlayerLongPressSpeedControlLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public boolean handleOnTouchEvent(MotionEvent event) {
        if (!isAllowControl()) {
            return false;
        }
        boolean isLongPressingBeforeEvent = isLongPressing;
        gestureDetector.onTouchEvent(event);
        if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
            if (isLongPressing) {
                isLongPressing = false;
                handleOnLongPress(false);
            }
        }
        return isLongPressingBeforeEvent;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (isInEditMode()) return;

        observeUntilViewDetached(
                requireNotNull(PLVMediaPlayerLocalProvider.localMediaPlayer.on(this).current())
                        .getStateListenerRegistry()
                        .getPlayingState(),
                this,
                new Observer<PLVMediaPlayerPlayingState>() {
                    @Override
                    public void onChanged(@Nullable @org.jetbrains.annotations.Nullable PLVMediaPlayerPlayingState playingState) {
                        currentPlayingState = playingState;
                    }
                }
        );

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
        return false;
    }

    @Override
    public void onLongPress(@NonNull MotionEvent e) {
        isLongPressing = true;
        handleOnLongPress(true);
    }

    @Override
    public boolean onFling(@NonNull MotionEvent e1, @NonNull MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    private void handleOnLongPress(boolean isLongPressing) {
        IPLVMediaPlayer mediaPlayer = PLVMediaPlayerLocalProvider.localMediaPlayer.on(this).current();
        if (mediaPlayer == null) {
            return;
        }
        if (isLongPressing) {
            Float speed = mediaPlayer.getStateListenerRegistry().getSpeed().getValue();
            if (speed != null) {
                speedBeforeLongPressControl = speed;
            }
            mediaPlayer.setSpeed(SPEED_ON_LONG_PRESS);
            hintLongPress(SPEED_ON_LONG_PRESS, true);
        } else {
            mediaPlayer.setSpeed(speedBeforeLongPressControl);
            hintLongPress(speedBeforeLongPressControl, false);
        }
    }

    private void hintLongPress(float speed, boolean isLongPressing) {
        final PLVMediaPlayerControlViewModel controlViewModel = PLVMediaPlayerLocalProvider.localControlViewModel.on(this).current();
        boolean locking = Nullables.of(new PLVSugarUtil.Supplier<Boolean>() {
            @Override
            public Boolean get() {
                return controlViewModel.getControlViewStateLiveData().getValue().controllerLocking;
            }
        }).getOrDefault(false);
        if (!locking && controlViewModel != null) {
            controlViewModel.requestControl(PLVMediaPlayerControlAction.hintLongPressControl(speed, isLongPressing));
        }
    }

    protected boolean isAllowControl() {
        return currentViewState != null
                && !currentViewState.controllerLocking
                && currentPlayingState == PLVMediaPlayerPlayingState.PLAYING;
    }

}
