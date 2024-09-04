package net.polyv.android.player.common.modules.auxiliary.viewmodel.usecase

/**
 * @author Hoshiiro
 */
internal class PLVMPAuxiliaryUseCases(
    val beforePlayListener: AuxiliaryBeforePlayListener,
    val updateMediaStateUseCase: AuxiliaryUpdateMediaStateUseCase
)