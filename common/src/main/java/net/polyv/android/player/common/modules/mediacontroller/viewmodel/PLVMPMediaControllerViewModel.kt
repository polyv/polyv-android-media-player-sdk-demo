package net.polyv.android.player.common.modules.mediacontroller.viewmodel

import android.app.Activity
import android.content.Context
import net.polyv.android.player.common.modules.mediacontroller.model.PLVMPMediaControllerRepo
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.usecase.PLVMPMediaControllerUseCases
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.viewstate.PLVMPMediaControllerFloatAction
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.viewstate.PLVMPMediaControllerViewState
import net.polyv.android.player.common.utils.floatwindow.enums.PLVFloatWindowLaunchReason
import net.polyv.android.player.core.api.listener.event.PLVMediaPlayerOnInfoEvent
import net.polyv.android.player.sdk.PLVDeviceManager
import net.polyv.android.player.sdk.foundation.di.LifecycleAwareDependComponent
import net.polyv.android.player.sdk.foundation.lang.MutableObserver
import net.polyv.android.player.sdk.foundation.lang.MutableObserver.Companion.disposeAll

/**
 * @author Hoshiiro
 */
class PLVMPMediaControllerViewModel internal constructor(
    private val repo: PLVMPMediaControllerRepo,
    private val useCases: PLVMPMediaControllerUseCases
) : LifecycleAwareDependComponent {

    val mediaControllerViewState = repo.mediator.mediaControllerViewState
    val brightnessUpdateEvent = repo.mediator.brightnessUpdateEvent
    val volumeUpdateEvent = repo.mediator.volumeUpdateEvent
    val launchFloatWindowEvent = repo.mediator.launchFloatWindowEvent

    private var viewState: PLVMPMediaControllerViewState
        get() = repo.mediator.mediaControllerViewState.value ?: PLVMPMediaControllerViewState()
        set(value) = repo.mediator.mediaControllerViewState.setValue(value)

    private val observers = mutableListOf<MutableObserver<*>>()

    init {
        observeSeekFinishEvent()
        observeMediaStopState()
    }

    @JvmOverloads
    fun handleDragSeekBar(action: DragSeekBarAction, progress: Long = 0) {
        when (action) {
            DragSeekBarAction.DRAG -> {
                viewState = viewState.copy(
                    progressSeekBarDragging = true,
                    progressSeekBarWaitSeekFinish = true,
                    progressSeekBarDragPosition = progress
                )
            }

            DragSeekBarAction.FINISH -> {
                if (viewState.progressSeekBarDragging) {
                    repo.mediaMediator.seekTo?.invoke(viewState.progressSeekBarDragPosition)
                }
                viewState = viewState.copy(
                    progressSeekBarDragging = false,
                    progressSeekBarDragPosition = 0
                )
            }
        }
    }

    @JvmOverloads
    fun changeControllerVisible(toVisible: Boolean = !viewState.controllerVisible) {
        viewState = viewState.copy(
            controllerVisible = toVisible
        )
    }

    fun handleLongPressSpeeding(action: LongPressSpeedingAction) {
        when (action) {
            LongPressSpeedingAction.START -> {
                if (repo.mediaMediator.isPlaying?.invoke() != true) {
                    return
                }
                viewState = viewState.copy(
                    longPressSpeeding = true,
                    speedBeforeLongPress = repo.mediaMediator.getSpeed?.invoke() ?: 1F
                )
                repo.mediaMediator.setSpeed?.invoke(2F)
            }

            LongPressSpeedingAction.FINISH -> {
                if (!viewState.longPressSpeeding) {
                    return
                }
                repo.mediaMediator.setSpeed?.invoke(viewState.speedBeforeLongPress)
                viewState = viewState.copy(
                    longPressSpeeding = false,
                    speedBeforeLongPress = 1F
                )
            }
        }
    }

    fun changeBrightness(direction: ChangeDirection, activity: Activity) {
        val currentBrightness = PLVDeviceManager.getBrightness(activity)
        val nextBrightnessCandidate = if (direction == ChangeDirection.UP) currentBrightness + 10 else currentBrightness - 10
        val nextBrightness = nextBrightnessCandidate.coerceIn(0, 100)
        PLVDeviceManager.setBrightness(activity, nextBrightness)
        repo.mediator.brightnessUpdateEvent.setValue(nextBrightness)
    }

    fun changeVolume(direction: ChangeDirection, context: Context) {
        val currentVolume = PLVDeviceManager.getVolume(context)
        val nextVolumeCandidate = if (direction == ChangeDirection.UP) currentVolume + 10 else currentVolume - 10
        val nextVolume = nextVolumeCandidate.coerceIn(0, 100)
        PLVDeviceManager.setVolume(context, nextVolume)
        repo.mediator.volumeUpdateEvent.setValue(nextVolume)
    }

    fun lockMediaController(action: LockMediaControllerAction) {
        viewState = viewState.copy(
            controllerLocking = action == LockMediaControllerAction.LOCK
        )
    }

    fun pushFloatActionLayout(layout: PLVMPMediaControllerFloatAction) {
        val layouts = viewState.floatActionLayouts
        viewState = viewState.copy(
            floatActionLayouts = layouts.toMutableList().apply { add(layout) }
        )
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun popFloatActionLayout() {
        val layouts = viewState.floatActionLayouts
        if (layouts.isEmpty()) {
            return
        }
        viewState = viewState.copy(
            floatActionLayouts = layouts.toMutableList().apply { removeLastOrNull() }
        )
    }

    fun launchFloatWindow(reason: PLVFloatWindowLaunchReason) {
        repo.mediator.launchFloatWindowEvent.setValue(reason)
    }

    private fun observeSeekFinishEvent() {
        repo.mediaMediator.onInfoEvent.observe {
            if (!viewState.progressSeekBarWaitSeekFinish) {
                return@observe
            }
            if (it.what in listOf(
                    PLVMediaPlayerOnInfoEvent.MEDIA_INFO_AUDIO_SEEK_RENDERING_START,
                    PLVMediaPlayerOnInfoEvent.MEDIA_INFO_VIDEO_SEEK_RENDERING_START,
                    PLVMediaPlayerOnInfoEvent.MEDIA_INFO_AUDIO_RENDERING_START,
                    PLVMediaPlayerOnInfoEvent.MEDIA_INFO_VIDEO_RENDERING_START
                )
            ) {
                viewState = viewState.copy(
                    progressSeekBarWaitSeekFinish = false
                )
            }
        }.addTo(this.observers)
    }

    private fun observeMediaStopState() {
        repo.mediator.businessErrorState.observe {
            viewState = viewState.copy(
                errorOverlayLayoutVisible = it != null
            )
        }.addTo(this.observers)
        repo.mediator.playCompleteState.observe {
            viewState = viewState.copy(
                completeOverlayLayoutVisible = it
            )
        }.addTo(this.observers)
    }

    override fun onDestroy() {
        this.observers.disposeAll()
        this.observers.clear()
    }

}

enum class DragSeekBarAction {
    DRAG,
    FINISH
}

enum class LongPressSpeedingAction {
    START,
    FINISH
}

enum class ChangeDirection {
    UP,
    DOWN
}

enum class LockMediaControllerAction {
    LOCK,
    UNLOCK
}