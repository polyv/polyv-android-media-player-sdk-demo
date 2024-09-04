package net.polyv.android.player.common.modules.media.viewmodel.usecase

import android.graphics.Rect
import net.polyv.android.player.business.scene.common.model.api.vo.progressImageInterval
import net.polyv.android.player.business.scene.common.model.vo.PLVMediaOutputMode
import net.polyv.android.player.business.scene.common.model.vo.PLVMediaSubtitle
import net.polyv.android.player.common.modules.media.model.PLVMPMediaRepo
import net.polyv.android.player.common.modules.media.viewmodel.viewstate.PLVMPMediaInfoViewState
import net.polyv.android.player.common.modules.media.viewmodel.viewstate.PLVMPMediaPlayViewState
import net.polyv.android.player.common.modules.media.viewmodel.viewstate.PLVMPSubtitleTextStyle
import net.polyv.android.player.core.api.listener.state.PLVMediaPlayerPlayingState
import net.polyv.android.player.core.api.listener.state.PLVMediaPlayerState
import net.polyv.android.player.sdk.foundation.di.LifecycleAwareDependComponent
import net.polyv.android.player.sdk.foundation.graphics.parseColor
import net.polyv.android.player.sdk.foundation.lang.DerivedState
import net.polyv.android.player.sdk.foundation.lang.isLiteralTrue

/**
 * @author Hoshiiro
 */
class UpdateMediaStateUseCase internal constructor(
    private val repo: PLVMPMediaRepo
) : LifecycleAwareDependComponent {

    private val mediaPlayViewState = DerivedState {
        PLVMPMediaPlayViewState(
            currentProgress = repo.player.getStateListenerRegistry().progressState.value ?: 0,
            duration = repo.player.getStateListenerRegistry().durationState.value ?: 0,
            isPlaying = repo.player.getStateListenerRegistry().playingState.value == PLVMediaPlayerPlayingState.PLAYING,
            playerState = repo.player.getStateListenerRegistry().playerState.value ?: PLVMediaPlayerState.STATE_IDLE,
            isBuffering = repo.player.getStateListenerRegistry().isBuffering.value ?: false,
            bufferingSpeed = repo.mediator.bufferingSpeed.value?.toLong() ?: 0L,
            speed = repo.player.getStateListenerRegistry().speed.value ?: 1F,
            subtitleTexts = repo.player.getBusinessListenerRegistry().vodCurrentSubTitleTexts.value ?: emptyList()
        )
    }.also { it.relayTo(repo.mediator.mediaPlayViewState) }

    private val mediaInfoViewState = DerivedState {
        PLVMPMediaInfoViewState(
            title = repo.player.getBusinessListenerRegistry().vodVideoJson.value?.title ?: "",
            videoSize = repo.player.getStateListenerRegistry().videoSize.value ?: Rect(),
            bitRate = repo.player.getBusinessListenerRegistry().currentMediaBitRate.value,
            supportBitRates = repo.player.getBusinessListenerRegistry().supportMediaBitRates.value ?: emptyList(),
            outputMode = repo.player.getBusinessListenerRegistry().currentMediaOutputMode.value
                ?: PLVMediaOutputMode.AUDIO_VIDEO,
            supportOutputModes = repo.player.getBusinessListenerRegistry().supportMediaOutputModes.value ?: emptyList(),
            currentSubtitle = repo.player.getBusinessListenerRegistry().currentShowSubTitles.value,
            supportSubtitles = getSupportSubtitles(),
            progressPreviewImage = repo.player.getBusinessListenerRegistry().vodVideoJson.value?.progressImage,
            progressPreviewImageInterval = repo.player.getBusinessListenerRegistry().vodVideoJson.value?.progressImageInterval,
            audioModeCoverImage = repo.player.getBusinessListenerRegistry().vodVideoJson.value?.first_image,
            topSubtitleTextStyle = getSubtitleTextStyle("top"),
            bottomSubtitleTextStyle = getSubtitleTextStyle("bottom")
        )
    }.also { it.relayTo(repo.mediator.mediaInfoViewState) }

    @OptIn(ExperimentalStdlibApi::class)
    private fun getSupportSubtitles(): List<List<PLVMediaSubtitle>> {
        val subtitleSetting = repo.player.getBusinessListenerRegistry().supportSubtitleSetting.value
            ?: return emptyList()
        if (!subtitleSetting.available) {
            return emptyList()
        }
        return buildList {
            subtitleSetting.defaultDoubleSubtitles?.let { add(it) }
            addAll(subtitleSetting.availableSubtitles.map { listOf(it) })
        }.distinct()
    }

    private fun getSubtitleTextStyle(position: String): PLVMPSubtitleTextStyle {
        val isDoubleSubtitle = (repo.player.getBusinessListenerRegistry().currentShowSubTitles.value?.size ?: 0) >= 2
        val targetSubtitleStyle = repo.player.getBusinessListenerRegistry().vodVideoJson.value?.player?.subtitles
            ?.find { subtitle ->
                return@find if (!isDoubleSubtitle) {
                    subtitle?.style == "single"
                } else {
                    subtitle?.style == "double" && subtitle.position == position
                }
            }
        if (targetSubtitleStyle == null) {
            return PLVMPSubtitleTextStyle()
        }
        return PLVMPSubtitleTextStyle(
            fontColor = parseColor(targetSubtitleStyle.fontColor ?: "#FFFFFF"),
            isBold = targetSubtitleStyle.fontBold?.isLiteralTrue() == true,
            isItalic = targetSubtitleStyle.fontItalics?.isLiteralTrue() == true,
            backgroundColor = parseColor(targetSubtitleStyle.backgroundColor ?: "#000000")
        )
    }

    override fun onDestroy() {
        mediaPlayViewState.destroy()
        mediaInfoViewState.destroy()
    }

}