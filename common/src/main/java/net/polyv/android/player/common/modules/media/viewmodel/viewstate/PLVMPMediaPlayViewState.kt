package net.polyv.android.player.common.modules.media.viewmodel.viewstate

import net.polyv.android.player.business.scene.vod.model.vo.PLVVodSubtitleText
import net.polyv.android.player.core.api.listener.state.PLVMediaPlayerState

/**
 * @author Hoshiiro
 */
data class PLVMPMediaPlayViewState(
    val currentProgress: Long = 0,
    val duration: Long = 0,
    val isPlaying: Boolean = false,
    val playerState: PLVMediaPlayerState = PLVMediaPlayerState.STATE_IDLE,
    val isBuffering: Boolean = false,
    // bytes per second
    val bufferingSpeed: Long = 0,
    val speed: Float = 1F,
    val subtitleTexts: List<PLVVodSubtitleText> = emptyList()
)