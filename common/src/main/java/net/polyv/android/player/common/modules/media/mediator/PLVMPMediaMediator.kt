package net.polyv.android.player.common.modules.media.mediator

import net.polyv.android.player.business.scene.auxiliary.player.IPLVAuxiliaryMediaPlayer
import net.polyv.android.player.business.scene.common.model.vo.PLVMediaBitRate
import net.polyv.android.player.business.scene.common.player.error.PLVMediaPlayerBusinessError
import net.polyv.android.player.business.scene.common.player.listener.event.PLVMediaPlayerAutoContinueEvent
import net.polyv.android.player.common.modules.media.viewmodel.viewstate.PLVMPMediaInfoViewState
import net.polyv.android.player.common.modules.media.viewmodel.viewstate.PLVMPMediaPlayViewState
import net.polyv.android.player.core.api.listener.event.PLVMediaPlayerOnCompletedEvent
import net.polyv.android.player.core.api.listener.event.PLVMediaPlayerOnInfoEvent
import net.polyv.android.player.core.api.listener.event.PLVMediaPlayerOnPreparedEvent
import net.polyv.android.player.core.api.listener.state.PLVMediaPlayerPlayingState
import net.polyv.android.player.core.api.listener.state.PLVMediaPlayerState
import net.polyv.android.player.sdk.foundation.lang.MutableEvent
import net.polyv.android.player.sdk.foundation.lang.MutableState

/**
 * @author Hoshiiro
 */
class PLVMPMediaMediator {

    val mediaPlayViewState = MutableState(PLVMPMediaPlayViewState())
    val mediaInfoViewState = MutableState(PLVMPMediaInfoViewState())
    val networkPoorEvent = MutableEvent<Long>()
    val onChangeBitRateEvent = MutableEvent<PLVMediaBitRate>()
    val onPreparedEvent = MutableEvent<PLVMediaPlayerOnPreparedEvent>()
    val onAutoContinueEvent = MutableEvent<PLVMediaPlayerAutoContinueEvent>()
    val onInfoEvent = MutableEvent<PLVMediaPlayerOnInfoEvent>()
    val onCompleteEvent = MutableEvent<PLVMediaPlayerOnCompletedEvent>()
    val playingState = MutableState(PLVMediaPlayerPlayingState.PAUSING)
    val playerState = MutableState(PLVMediaPlayerState.STATE_IDLE)
    val bufferingSpeed = MutableState(0.0)
    val businessErrorState = MutableState<PLVMediaPlayerBusinessError?>(null)

    var seekTo: ((Long) -> Unit)? = null
    var getSpeed: (() -> Float)? = null
    var setSpeed: ((Float) -> Unit)? = null
    var isPlaying: (() -> Boolean)? = null
    var getVolume: (() -> Int)? = null
    var setVolume: ((Int) -> Unit)? = null
    var bindAuxiliaryPlayer: ((IPLVAuxiliaryMediaPlayer) -> Unit)? = null

}