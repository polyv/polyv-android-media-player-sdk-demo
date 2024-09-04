package net.polyv.android.player.common.modules.auxiliary.viewmodel.usecase

import net.polyv.android.player.common.modules.auxiliary.model.PLVMPAuxiliaryRepo
import net.polyv.android.player.common.modules.auxiliary.viewmodel.viewstate.PLVMPAuxiliaryInfoViewState
import net.polyv.android.player.common.modules.auxiliary.viewmodel.viewstate.PLVMPAuxiliaryPlayViewState
import net.polyv.android.player.sdk.foundation.di.LifecycleAwareDependComponent
import net.polyv.android.player.sdk.foundation.lang.MutableObserver
import net.polyv.android.player.sdk.foundation.lang.MutableObserver.Companion.disposeAll

/**
 * @author Hoshiiro
 */
internal class AuxiliaryUpdateMediaStateUseCase(
    private val repo: PLVMPAuxiliaryRepo
) : LifecycleAwareDependComponent {

    private var infoViewState: PLVMPAuxiliaryInfoViewState?
        get() = repo.mediator.auxiliaryInfoViewState.value
        set(value) = repo.mediator.auxiliaryInfoViewState.setValue(value)

    private var playViewState: PLVMPAuxiliaryPlayViewState
        get() = repo.mediator.auxiliaryPlayViewState.value ?: PLVMPAuxiliaryPlayViewState()
        set(value) = repo.mediator.auxiliaryPlayViewState.setValue(value)

    private val observers = mutableListOf<MutableObserver<*>>()

    init {
        observeState()
    }

    private fun observeState() {
        repo.auxiliaryPlayer.getAuxiliaryListenerRegistry().onShowAdvertEvent.observe {
            infoViewState = PLVMPAuxiliaryInfoViewState(
                url = it.dataSource.url,
                isImage = it.dataSource.isImage,
                clickNavigationUrl = it.dataSource.clickNavigationUrl,
                showDuration = it.dataSource.duration,
                canSkip = it.dataSource.canSkip,
                beforeSkipDuration = it.dataSource.beforeSkipDuration,
                stage = it.stage
            )
        }.addTo(this.observers)

        repo.auxiliaryPlayer.getAuxiliaryListenerRegistry().onFinishAdvertEvent.observe {
            infoViewState = null
        }.addTo(this.observers)

        repo.auxiliaryPlayer.getAuxiliaryListenerRegistry().onTimeLeftCountDownEvent.observe {
            playViewState = playViewState.copy(
                timeLeftInSeconds = it.timeLeftInSeconds
            )
        }.addTo(this.observers)
    }

    override fun onDestroy() {
        this.observers.disposeAll()
        this.observers.clear()
    }

}