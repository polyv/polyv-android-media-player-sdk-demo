package net.polyv.android.player.common.ui.component;

import static com.plv.foundationsdk.component.livedata.PLVLiveDataExt.observeUntilViewDetached;
import static com.plv.foundationsdk.utils.PLVSugarUtil.clamp;
import static com.plv.foundationsdk.utils.PLVSugarUtil.requireNotNull;

import android.app.Activity;
import androidx.lifecycle.Observer;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import com.plv.foundationsdk.component.exts.Nullables;
import com.plv.foundationsdk.utils.PLVSugarUtil;
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;

import net.polyv.android.player.common.ui.localprovider.PLVMediaPlayerLocalProvider;
import net.polyv.android.player.common.ui.viewmodel.PLVMediaPlayerControlViewModel;
import net.polyv.android.player.common.ui.viewmodel.action.PLVMediaPlayerControlAction;
import net.polyv.android.player.common.ui.viewmodel.viewstate.PLVMediaPlayerControlViewState;
import net.polyv.android.player.sdk.PLVDeviceManager;

/**
 * @author Hoshiiro
 */
public class PLVMediaPlayerBrightnessVolumeControlLayout extends FrameLayout implements GestureDetector.OnGestureListener {

    private final GestureDetector gestureDetector = new GestureDetector(getContext(), this);

    protected PLVMediaPlayerControlViewState currentViewState = null;

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
        final int diff = accumulateAdjustDiff + (int) (distanceY / (0.4F * ScreenUtils.getScreenOrientatedHeight()) * 100);
        if (Math.abs(diff) < 8) {
            accumulateAdjustDiff = diff;
            return;
        }
        if (left) {
            int brightness = PLVDeviceManager.getBrightness((Activity) getContext());
            brightness = clamp(brightness + diff, 0, 100);
            PLVDeviceManager.setBrightness((Activity) getContext(), brightness);
            hintBrightnessChanged(brightness);
        } else {
            int volume = PLVDeviceManager.getVolume(getContext());
            volume = clamp(volume + diff, 0, 100);
            PLVDeviceManager.setVolume(getContext(), volume);
            hintVolumeChanged(volume);
        }
        accumulateAdjustDiff = 0;
    }

    private void hintBrightnessChanged(int brightness) {
        final PLVMediaPlayerControlViewModel controlViewModel = PLVMediaPlayerLocalProvider.localControlViewModel.on(this).current();
        boolean locking = Nullables.of(new PLVSugarUtil.Supplier<Boolean>() {
            @Override
            public Boolean get() {
                return controlViewModel.getControlViewStateLiveData().getValue().controllerLocking;
            }
        }).getOrDefault(false);
        if (!locking && controlViewModel != null) {
            controlViewModel.requestControl(PLVMediaPlayerControlAction.hintBrightnessChanged(brightness));
        }
    }

    private void hintVolumeChanged(int volume) {
        final PLVMediaPlayerControlViewModel controlViewModel = PLVMediaPlayerLocalProvider.localControlViewModel.on(this).current();
        boolean locking = Nullables.of(new PLVSugarUtil.Supplier<Boolean>() {
            @Override
            public Boolean get() {
                return controlViewModel.getControlViewStateLiveData().getValue().controllerLocking;
            }
        }).getOrDefault(false);
        if (!locking && controlViewModel != null) {
            controlViewModel.requestControl(PLVMediaPlayerControlAction.hintVolumeChanged(volume));
        }
    }

    protected boolean isAllowControl() {
        return currentViewState != null && !currentViewState.controllerLocking;
    }

}
