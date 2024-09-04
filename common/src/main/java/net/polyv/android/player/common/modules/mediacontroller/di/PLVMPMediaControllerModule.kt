package net.polyv.android.player.common.modules.mediacontroller.di

import net.polyv.android.player.common.modules.mediacontroller.mediator.PLVMPMediaControllerMediator
import net.polyv.android.player.common.modules.mediacontroller.model.PLVMPMediaControllerRepo
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.PLVMPMediaControllerViewModel
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.usecase.PLVMPMediaControllerUseCases
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.usecase.UpdateMediaStopOverlayUseCase
import net.polyv.android.player.sdk.foundation.di.dependModule
import net.polyv.android.player.sdk.foundation.di.get

/**
 * @author Hoshiiro
 */
internal val mediaControllerModule = dependModule {
    provide { PLVMPMediaControllerMediator() }

    provide { PLVMPMediaControllerRepo(get(), get()) }

    provide { UpdateMediaStopOverlayUseCase(get()) }
    provide { PLVMPMediaControllerUseCases(get()) }

    provide { PLVMPMediaControllerViewModel(get(), get()) }
}