package net.polyv.android.player.common.ui.viewmodel;

import static com.plv.foundationsdk.component.livedata.PLVLiveDataExt.mutableLiveData;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import com.plv.foundationsdk.component.event.PLVEvent;
import com.plv.foundationsdk.component.event.PLVMutableEvent;

import net.polyv.android.player.common.ui.viewmodel.action.PLVMediaPlayerControlAction;
import net.polyv.android.player.common.ui.viewmodel.viewstate.PLVMediaPlayerControlViewState;

/**
 * @author Hoshiiro
 */
public class PLVMediaPlayerControlViewModel {

    private final PLVMediaPlayerControlViewState currentControlViewState = new PLVMediaPlayerControlViewState();
    private final MutableLiveData<PLVMediaPlayerControlViewState> controlViewStateLiveData = mutableLiveData(currentControlViewState);
    private final PLVMutableEvent<PLVMediaPlayerControlAction> controlActionEvent = new PLVMutableEvent<>();

    public LiveData<PLVMediaPlayerControlViewState> getControlViewStateLiveData() {
        return controlViewStateLiveData;
    }

    public PLVEvent<PLVMediaPlayerControlAction> getControlActionEvent() {
        return controlActionEvent;
    }

    public void requestControl(PLVMediaPlayerControlAction action) {
        if (action instanceof PLVMediaPlayerControlAction.ShowMediaController) {
            currentControlViewState.controllerVisible = true;
            updateControlViewState();
        } else if (action instanceof PLVMediaPlayerControlAction.HideMediaController) {
            currentControlViewState.controllerVisible = false;
            updateControlViewState();
        } else if (action instanceof PLVMediaPlayerControlAction.LockMediaController) {
            currentControlViewState.controllerLocking = ((PLVMediaPlayerControlAction.LockMediaController) action).toLock;
            updateControlViewState();
        } else if (action instanceof PLVMediaPlayerControlAction.CloseFloatMenuLayout) {
            currentControlViewState.bitRateSelectLayoutVisible = false;
            currentControlViewState.speedSelectLayoutVisible = false;
            currentControlViewState.moreActionLayoutVisible = false;
            updateControlViewState();
        } else if (action instanceof PLVMediaPlayerControlAction.ShowBitRateSelectLayout) {
            currentControlViewState.bitRateSelectLayoutVisible = true;
            updateControlViewState();
        } else if (action instanceof PLVMediaPlayerControlAction.ShowSpeedSelectLayout) {
            currentControlViewState.speedSelectLayoutVisible = true;
            updateControlViewState();
        } else if (action instanceof PLVMediaPlayerControlAction.ShowMoreActionLayout) {
            currentControlViewState.moreActionLayoutVisible = true;
            updateControlViewState();
        } else if (action instanceof PLVMediaPlayerControlAction.HintBrightnessChanged) {
            controlActionEvent.set(action);
        } else if (action instanceof PLVMediaPlayerControlAction.HintVolumeChanged) {
            controlActionEvent.set(action);
        } else if (action instanceof PLVMediaPlayerControlAction.ProgressSeekBarDragAction) {
            currentControlViewState.progressSeekBarDragging = ((PLVMediaPlayerControlAction.ProgressSeekBarDragAction) action).isDragging;
            currentControlViewState.progressSeekBarDragPosition = ((PLVMediaPlayerControlAction.ProgressSeekBarDragAction) action).position;
            updateControlViewState();
        } else if (action instanceof PLVMediaPlayerControlAction.HintLongPressSpeedControl) {
            controlActionEvent.set(action);
        } else if (action instanceof PLVMediaPlayerControlAction.LaunchFloatWindow) {
            controlActionEvent.set(action);
        } else if (action instanceof PLVMediaPlayerControlAction.HintErrorOverlayLayoutVisible) {
            currentControlViewState.errorOverlayLayoutVisible = ((PLVMediaPlayerControlAction.HintErrorOverlayLayoutVisible) action).visible;
            updateControlViewState();
        } else if (action instanceof PLVMediaPlayerControlAction.HintCompleteOverlayLayoutVisible) {
            currentControlViewState.completeOverlayLayoutVisible = ((PLVMediaPlayerControlAction.HintCompleteOverlayLayoutVisible) action).visible;
            updateControlViewState();
        } else if (action instanceof PLVMediaPlayerControlAction.HintNetworkPoorIndicateVisible) {
            currentControlViewState.networkPoorIndicateLayoutVisible = ((PLVMediaPlayerControlAction.HintNetworkPoorIndicateVisible) action).visible;
            updateControlViewState();
        } else if (action instanceof PLVMediaPlayerControlAction.HintBitRateChanged) {
            controlActionEvent.set(action);
        } else if (action instanceof PLVMediaPlayerControlAction.HintManualPauseVideo) {
            currentControlViewState.isManualPauseVideo = ((PLVMediaPlayerControlAction.HintManualPauseVideo) action).isPause;
            updateControlViewState();
        }
    }

    private void updateControlViewState() {
        controlViewStateLiveData.postValue(currentControlViewState.clone());
    }

}
