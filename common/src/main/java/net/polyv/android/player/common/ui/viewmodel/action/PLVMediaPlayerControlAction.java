package net.polyv.android.player.common.ui.viewmodel.action;

import net.polyv.android.player.business.scene.common.model.vo.PLVMediaBitRate;

/**
 * @author Hoshiiro
 */
public class PLVMediaPlayerControlAction {

    public static PLVMediaPlayerControlAction showMediaController() {
        return new ShowMediaController();
    }

    public static PLVMediaPlayerControlAction hideMediaController() {
        return new HideMediaController();
    }

    public static PLVMediaPlayerControlAction lockMediaController(boolean toLock) {
        LockMediaController lockMediaController = new LockMediaController();
        lockMediaController.toLock = toLock;
        return lockMediaController;
    }

    public static PLVMediaPlayerControlAction closeFloatMenuLayout() {
        return new CloseFloatMenuLayout();
    }

    public static PLVMediaPlayerControlAction showBitRateSelectLayout() {
        return new ShowBitRateSelectLayout();
    }

    public static PLVMediaPlayerControlAction showSpeedSelectLayout() {
        return new ShowSpeedSelectLayout();
    }

    public static PLVMediaPlayerControlAction showMoreActionLayout() {
        return new ShowMoreActionLayout();
    }

    public static PLVMediaPlayerControlAction hintBrightnessChanged(int brightness) {
        HintBrightnessChanged hintBrightnessChanged = new HintBrightnessChanged();
        hintBrightnessChanged.brightness = brightness;
        return hintBrightnessChanged;
    }

    public static PLVMediaPlayerControlAction hintVolumeChanged(int volume) {
        HintVolumeChanged hintVolumeChanged = new HintVolumeChanged();
        hintVolumeChanged.volume = volume;
        return hintVolumeChanged;
    }

    public static PLVMediaPlayerControlAction hintBitRateChanged(PLVMediaBitRate bitRate) {
        HintBitRateChanged hintBitRateChanged = new HintBitRateChanged();
        hintBitRateChanged.bitRate = bitRate;
        return hintBitRateChanged;
    }

    public static PLVMediaPlayerControlAction progressSeekBarDrag(long position, boolean isDragging) {
        ProgressSeekBarDragAction action = new ProgressSeekBarDragAction();
        action.position = position;
        action.isDragging = isDragging;
        return action;
    }

    public static PLVMediaPlayerControlAction hintLongPressControl(float speed, boolean isLongPressing) {
        HintLongPressSpeedControl action = new HintLongPressSpeedControl();
        action.speed = speed;
        action.isLongPressing = isLongPressing;
        return action;
    }

    public static PLVMediaPlayerControlAction hintErrorOverlayLayoutVisible(boolean visible) {
        HintErrorOverlayLayoutVisible action = new HintErrorOverlayLayoutVisible();
        action.visible = visible;
        return action;
    }

    public static PLVMediaPlayerControlAction hintCompleteOverlayLayoutVisible(boolean visible) {
        HintCompleteOverlayLayoutVisible action = new HintCompleteOverlayLayoutVisible();
        action.visible = visible;
        return action;
    }

    public static PLVMediaPlayerControlAction hintNetworkPoorIndicateVisible(boolean visible) {
        HintNetworkPoorIndicateVisible action = new HintNetworkPoorIndicateVisible();
        action.visible = visible;
        return action;
    }

    public static PLVMediaPlayerControlAction launchFloatWindow(int reason) {
        LaunchFloatWindow action = new LaunchFloatWindow();
        action.reason = reason;
        return action;
    }

    public static PLVMediaPlayerControlAction hintManualPauseVideo(boolean isPause) {
        HintManualPauseVideo action = new HintManualPauseVideo();
        action.isPause = isPause;
        return action;
    }

    public static class ShowMediaController extends PLVMediaPlayerControlAction {}

    public static class HideMediaController extends PLVMediaPlayerControlAction {}

    public static class LockMediaController extends PLVMediaPlayerControlAction {
        public boolean toLock;
    }

    public static class CloseFloatMenuLayout extends PLVMediaPlayerControlAction {}

    public static class ShowBitRateSelectLayout extends PLVMediaPlayerControlAction {}

    public static class ShowSpeedSelectLayout extends PLVMediaPlayerControlAction {}

    public static class ShowMoreActionLayout extends PLVMediaPlayerControlAction {}

    public static class HintBrightnessChanged extends PLVMediaPlayerControlAction {
        public int brightness;
    }

    public static class HintVolumeChanged extends PLVMediaPlayerControlAction {
        public int volume;
    }

    public static class HintBitRateChanged extends PLVMediaPlayerControlAction {
        public PLVMediaBitRate bitRate;
    }

    public static class ProgressSeekBarDragAction extends PLVMediaPlayerControlAction {
        public long position;
        public boolean isDragging;
    }

    public static class HintLongPressSpeedControl extends PLVMediaPlayerControlAction {
        public float speed;
        public boolean isLongPressing;
    }

    public static class LaunchFloatWindow extends PLVMediaPlayerControlAction {
        public int reason;
    }

    public static class HintErrorOverlayLayoutVisible extends PLVMediaPlayerControlAction {
        public boolean visible;
    }

    public static class HintCompleteOverlayLayoutVisible extends PLVMediaPlayerControlAction {
        public boolean visible;
    }

    public static class HintNetworkPoorIndicateVisible extends PLVMediaPlayerControlAction {
        public boolean visible;
    }

    public static class HintManualPauseVideo extends PLVMediaPlayerControlAction {
        public boolean isPause;
    }

}
