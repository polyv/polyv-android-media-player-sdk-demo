package net.polyv.android.player.common.modules.mediacontroller.mediator

import net.polyv.android.player.business.scene.common.player.error.PLVMediaPlayerBusinessError
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.viewstate.PLVMPMediaControllerViewState
import net.polyv.android.player.common.utils.floatwindow.enums.PLVFloatWindowLaunchReason
import net.polyv.android.player.sdk.foundation.lang.MutableEvent
import net.polyv.android.player.sdk.foundation.lang.MutableState

/**
 * @author Hoshiiro
 */
class PLVMPMediaControllerMediator {

    val mediaControllerViewState = MutableState(PLVMPMediaControllerViewState())

    // 亮度更新事件，范围 0 ~ 100
    val brightnessUpdateEvent = MutableEvent<Int>()

    // 音量更新事件，范围 0 ~ 100
    val volumeUpdateEvent = MutableEvent<Int>()
    val businessErrorState = MutableState<PLVMediaPlayerBusinessError?>(null)
    val playCompleteState = MutableState(false)
    val launchFloatWindowEvent = MutableEvent<PLVFloatWindowLaunchReason>()

}