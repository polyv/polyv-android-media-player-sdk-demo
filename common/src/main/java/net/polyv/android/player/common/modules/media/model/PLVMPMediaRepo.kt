package net.polyv.android.player.common.modules.media.model

import net.polyv.android.player.business.scene.common.model.vo.PLVMediaBitRate
import net.polyv.android.player.business.scene.common.model.vo.PLVMediaOutputMode
import net.polyv.android.player.business.scene.common.model.vo.PLVMediaResource
import net.polyv.android.player.business.scene.common.model.vo.PLVMediaSubtitle
import net.polyv.android.player.common.modules.media.mediator.PLVMPMediaMediator
import net.polyv.android.player.core.api.listener.state.PLVMediaPlayerPlayingState
import net.polyv.android.player.core.api.option.PLVMediaPlayerOption
import net.polyv.android.player.core.api.render.IPLVMediaPlayerRenderView
import net.polyv.android.player.sdk.PLVMediaPlayer
import net.polyv.android.player.sdk.foundation.di.LifecycleAwareDependComponent
import net.polyv.android.player.sdk.foundation.lang.MutableObserver
import net.polyv.android.player.sdk.foundation.lang.MutableObserver.Companion.disposeAll

/**
 * @author Hoshiiro
 */
internal class PLVMPMediaRepo(
    val mediator: PLVMPMediaMediator
) : IPLVMPMediaPlayer, LifecycleAwareDependComponent {

    val player: PLVMediaPlayer = PLVMediaPlayer()
    private val observers = mutableListOf<MutableObserver<*>>()

    init {
        this.mediator.seekTo = { this.seekTo(it) }
        this.mediator.getSpeed = { this.player.getStateListenerRegistry().speed.value ?: 1F }
        this.mediator.setSpeed = { this.player.setSpeed(it) }
        this.mediator.isPlaying = { this.player.getStateListenerRegistry().playingState.value == PLVMediaPlayerPlayingState.PLAYING }
        this.mediator.getVolume = { this.player.getStateListenerRegistry().volume.value ?: 100 }
        this.mediator.setVolume = { this.player.setVolume(it) }
        this.mediator.bindAuxiliaryPlayer = { this.player.bindAuxiliaryPlayer(it) }
        this.player.getBusinessListenerRegistry().onAutoContinueEvent.relayTo(this.mediator.onAutoContinueEvent)
            .addTo(this.observers)
        this.player.getBusinessListenerRegistry().businessErrorState.relayTo(this.mediator.businessErrorState)
            .addTo(this.observers)
        this.player.getStateListenerRegistry().playingState.relayTo(this.mediator.playingState).addTo(this.observers)
        this.player.getStateListenerRegistry().playerState.relayTo(this.mediator.playerState).addTo(this.observers)
        this.player.getEventListenerRegistry().onInfo.relayTo(this.mediator.onInfoEvent).addTo(this.observers)
        this.player.getEventListenerRegistry().onPrepared.relayTo(this.mediator.onPreparedEvent).addTo(this.observers)
        this.player.getEventListenerRegistry().onCompleted.relayTo(this.mediator.onCompleteEvent).addTo(this.observers)
    }

    override fun setMediaResource(mediaResource: PLVMediaResource) {
        this.player.setMediaResource(mediaResource)
    }

    override fun setRenderView(renderView: IPLVMediaPlayerRenderView) {
        this.player.setRenderView(renderView)
    }

    override fun setAutoContinue(autoContinue: Boolean) {
        this.player.setAutoContinue(autoContinue)
    }

    override fun setPlayerOption(options: List<PLVMediaPlayerOption>) {
        this.player.setPlayerOption(options)
    }

    override fun start() {
        this.player.start()
    }

    override fun pause() {
        this.player.pause()
    }

    override fun seekTo(position: Long) {
        val duration = player.getStateListenerRegistry().durationState.value ?: 0L
        val nextPosition = position.coerceIn(0, duration)
        player.seek(nextPosition)
        if (nextPosition < duration) {
            player.start()
        } else {
            player.pause()
        }
    }

    override fun restart() {
        this.player.restart()
    }

    override fun setSpeed(speed: Float) {
        this.player.setSpeed(speed)
    }

    override fun setVolume(volume: Int) {
        this.player.setVolume(volume)
    }

    override fun changeBitRate(bitRate: PLVMediaBitRate) {
        this.player.changeBitRate(bitRate)
        this.mediator.onChangeBitRateEvent.setValue(bitRate)
    }

    override fun changeMediaOutputMode(outputMode: PLVMediaOutputMode) {
        this.player.changeMediaOutputMode(outputMode)
    }

    override fun setShowSubtitles(subtitles: List<PLVMediaSubtitle>) {
        this.player.setShowSubtitles(subtitles)
    }

    override fun onDestroy() {
        this.player.destroy()
        this.observers.disposeAll()
        this.observers.clear()
    }

}