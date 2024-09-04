package net.polyv.android.player.common.ui.component;

import static net.polyv.android.player.sdk.foundation.graphics.DisplaysKt.getScreenHeight;
import static net.polyv.android.player.sdk.foundation.lang.PreconditionsKt.requireNotNull;

import android.app.Activity;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import net.polyv.android.player.common.di.PLVMediaPlayerLocalProvider;
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.ChangeDirection;
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.PLVMPMediaControllerViewModel;
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.viewstate.PLVMPMediaControllerViewState;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

/**
 * @author Hoshiiro
 */
public class PLVMediaPlayerBrightnessVolumeControlLayout extends FrameLayout implements GestureDetector.OnGestureListener {

    private final GestureDetector gestureDetector = new GestureDetector(getContext(), this);

    protected PLVMPMediaControllerViewState controlViewState = null;

    private boolean isScrolling = false;
    private boolean isScrollingVertical = false;
    /**
     * true - 左侧滑动，false - 右侧滑动
     */
    private boolean isScrollOnLeftSide = true;
    private int accumulateAdjustDiff = 0;

    public PLVMediaPlayerBrightnessVolumeControlLayout(@NonNull Context context) {
        super(context);
    }

    public PLVMediaPlayerBrightnessVolumeControlLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PLVMediaPlayerBrightnessVolumeControlLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public boolean handleOnTouchEvent(MotionEvent event) {
        if (!isAllowControl()) {
            return false;
        }
        boolean isScrollingVerticalBeforeEvent = isScrollingVertical;
        gestureDetector.onTouchEvent(event);
        if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
            isScrolling = false;
            isScrollingVertical = false;
            accumulateAdjustDiff = 0;
        }
        return isScrollingVerticalBeforeEvent;
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
                        controlViewState = viewState;
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
            isScrollingVertical = Math.abs(distanceY) > Math.abs(distanceX);
            isScrollOnLeftSide = e1.getX() < (float) getWidth() / 2;
        }
        if (isScrollingVertical) {
            handleOnScrolling(isScrollOnLeftSide, distanceY);
        }
        return isScrollingVertical;
    }

    @Override
    public void onLongPress(@NonNull MotionEvent e) {

    }

    @Override
    public boolean onFling(@NonNull MotionEvent e1, @NonNull MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    private void handleOnScrolling(boolean left, float distanceY) {
        final int diff = accumulateAdjustDiff + (int) (distanceY / (0.4F * getScreenHeight().px()) * 100);
        if (Math.abs(diff) < 8) {
            accumulateAdjustDiff = diff;
            return;
        }
        PLVMPMediaControllerViewModel controllerViewModel = requireNotNull(PLVMediaPlayerLocalProvider.localDependScope.on(this).current())
                .get(PLVMPMediaControllerViewModel.class);
        ChangeDirection direction = diff > 0 ? ChangeDirection.UP : ChangeDirection.DOWN;
        if (left) {
            controllerViewModel.changeBrightness(direction, (Activity) getContext());
        } else {
            controllerViewModel.changeVolume(direction, getContext());
        }
        accumulateAdjustDiff = 0;
    }

    protected boolean isAllowControl() {
        return controlViewState != null && !controlViewState.getControllerLocking();
    }

}
