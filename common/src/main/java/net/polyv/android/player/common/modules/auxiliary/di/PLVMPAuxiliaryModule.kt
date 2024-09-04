package net.polyv.android.player.common.modules.auxiliary.di

import net.polyv.android.player.common.modules.auxiliary.mediator.PLVMPAuxiliaryMediator
import net.polyv.android.player.common.modules.auxiliary.model.PLVMPAuxiliaryRepo
import net.polyv.android.player.common.modules.auxiliary.viewmodel.PLVMPAuxiliaryViewModel
import net.polyv.android.player.common.modules.auxiliary.viewmodel.usecase.AuxiliaryBeforePlayListener
import net.polyv.android.player.common.modules.auxiliary.viewmodel.usecase.AuxiliaryUpdateMediaStateUseCase
import net.polyv.android.player.common.modules.auxiliary.viewmodel.usecase.PLVMPAuxiliaryUseCases
import net.polyv.android.player.sdk.foundation.di.dependModule
import net.polyv.android.player.sdk.foundation.di.get

/**
 * @author Hoshiiro
 */
internal val auxiliaryModule = dependModule {
    provide { PLVMPAuxiliaryMediator() }

    provide { PLVMPAuxiliaryRepo(get(), get()) }

    provide { AuxiliaryBeforePlayListener(get()) }
    provide { AuxiliaryUpdateMediaStateUseCase(get()) }
    provide { PLVMPAuxiliaryUseCases(get(), get()) }

    provide { PLVMPAuxiliaryViewModel(get(), get()) }
}