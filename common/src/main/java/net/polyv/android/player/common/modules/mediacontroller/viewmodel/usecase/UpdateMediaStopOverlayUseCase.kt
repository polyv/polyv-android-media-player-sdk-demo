package net.polyv.android.player.common.modules.mediacontroller.viewmodel.usecase

import net.polyv.android.player.common.modules.mediacontroller.model.PLVMPMediaControllerRepo
import net.polyv.android.player.core.api.listener.state.PLVMediaPlayerPlayingState
import net.polyv.android.player.core.api.listener.state.PLVMediaPlayerState
import net.polyv.android.player.sdk.foundation.di.LifecycleAwareDependComponent
import net.polyv.android.player.sdk.foundation.lang.DerivedState

/**
 * @author Hoshiiro
 */
internal class UpdateMediaStopOverlayUseCase(
    private val repo: PLVMPMediaControllerRepo
) : LifecycleAwareDependComponent {

    private val businessErrorState = DerivedState {
        val error = repo.mediaMediator.businessErrorState.value
        val isPlaying = repo.mediaMediator.playingState.value == PLVMediaPlayerPlayingState.PLAYING
        error.takeIf { isPlaying }
    }.also { it.relayTo(repo.mediator.businessErrorState) }

    private val playCompleteState = DerivedState {
        repo.mediaMediator.playerState.value == PLVMediaPlayerState.STATE_COMPLETED
    }.also { it.relayTo(repo.mediator.playCompleteState) }

    override fun onDestroy() {
        businessErrorState.destroy()
        playCompleteState.destroy()
    }

}