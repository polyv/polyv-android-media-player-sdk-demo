package net.polyv.android.player.common.modules.media.viewmodel

import net.polyv.android.player.common.modules.media.mediator.PLVMPMediaMediator
import net.polyv.android.player.common.modules.media.model.IPLVMPMediaPlayer
import net.polyv.android.player.common.modules.media.model.PLVMPMediaRepo
import net.polyv.android.player.common.modules.media.viewmodel.usecase.PLVMPMediaUseCases

/**
 * @author Hoshiiro
 */
class PLVMPMediaViewModel internal constructor(
    private val repo: PLVMPMediaRepo,
    private val mediator: PLVMPMediaMediator,
    private val useCases: PLVMPMediaUseCases
) : IPLVMPMediaPlayer by repo {

    val mediaPlayViewState = this.mediator.mediaPlayViewState
    val mediaInfoViewState = this.mediator.mediaInfoViewState
    val networkPoorEvent = this.mediator.networkPoorEvent
    val onChangeBitRateEvent = this.mediator.onChangeBitRateEvent
    val onPreparedEvent = this.mediator.onPreparedEvent
    val onAutoContinueEvent = this.mediator.onAutoContinueEvent
    val onInfoEvent = this.mediator.onInfoEvent
    val onCompleteEvent = this.mediator.onCompleteEvent
    val playerState = this.mediator.playerState

}