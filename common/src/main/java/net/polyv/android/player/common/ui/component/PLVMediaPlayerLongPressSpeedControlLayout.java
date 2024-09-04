package net.polyv.android.player.common.ui.component;

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
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.LongPressSpeedingAction;
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.PLVMPMediaControllerViewModel;
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.viewstate.PLVMPMediaControllerViewState;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

/**
 * @author Hoshiiro
 */
public class PLVMediaPlayerLongPressSpeedControlLayout extends FrameLayout implements GestureDetector.OnGestureListener {

    private final GestureDetector gestureDetector = new GestureDetector(getContext(), this);

    protected PLVMPMediaControllerViewState controllerViewState = null;
    protected boolean isPlaying = false;

    private boolean isLongPressing = false;

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

        requireNotNull(PLVMediaPlayerLocalProvider.localDependScope.on(this).current())
                .get(PLVMPMediaViewModel.class)
                .getMediaPlayViewState()
                .observeUntilViewDetached(this, new Function1<PLVMPMediaPlayViewState, Unit>() {
                    @Override
                    public Unit invoke(PLVMPMediaPlayViewState playViewState) {
                        isPlaying = playViewState.isPlaying();
                        return null;
                    }
                });

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
        requireNotNull(PLVMediaPlayerLocalProvider.localDependScope.on(this).current())
                .get(PLVMPMediaControllerViewModel.class)
                .handleLongPressSpeeding(isLongPressing ? LongPressSpeedingAction.START : LongPressSpeedingAction.FINISH);
    }

    protected boolean isAllowControl() {
        return controllerViewState != null
                && !controllerViewState.getControllerLocking()
                && isPlaying;
    }

}
