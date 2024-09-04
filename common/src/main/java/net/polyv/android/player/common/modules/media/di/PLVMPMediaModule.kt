package net.polyv.android.player.common.modules.media.di

import net.polyv.android.player.common.modules.media.mediator.PLVMPMediaMediator
import net.polyv.android.player.common.modules.media.model.PLVMPMediaRepo
import net.polyv.android.player.common.modules.media.viewmodel.PLVMPMediaViewModel
import net.polyv.android.player.common.modules.media.viewmodel.usecase.ObserveNetworkPoorUseCase
import net.polyv.android.player.common.modules.media.viewmodel.usecase.PLVMPMediaUseCases
import net.polyv.android.player.common.modules.media.viewmodel.usecase.UpdateBufferingSpeedUseCase
import net.polyv.android.player.common.modules.media.viewmodel.usecase.UpdateMediaStateUseCase
import net.polyv.android.player.sdk.foundation.di.dependModule
import net.polyv.android.player.sdk.foundation.di.get

/**
 * @author Hoshiiro
 */
internal val mediaModule = dependModule {
    provide { PLVMPMediaMediator() }

    provide { PLVMPMediaRepo(get()) }

    provide { ObserveNetworkPoorUseCase(get()) }
    provide { UpdateBufferingSpeedUseCase(get()) }
    provide { UpdateMediaStateUseCase(get()) }
    provide { PLVMPMediaUseCases(get(), get(), get()) }

    provide { PLVMPMediaViewModel(get(), get(), get()) }

    afterCreate<PLVMPMediaMediator> { runCatching { get<PLVMPMediaRepo>() } }
}