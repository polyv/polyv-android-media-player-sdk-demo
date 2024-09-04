package net.polyv.android.player.common.modules.auxiliary.viewmodel

import net.polyv.android.player.common.modules.auxiliary.model.IPLVMPAuxiliaryPlayer
import net.polyv.android.player.common.modules.auxiliary.model.PLVMPAuxiliaryRepo
import net.polyv.android.player.common.modules.auxiliary.viewmodel.usecase.PLVMPAuxiliaryUseCases

/**
 * @author Hoshiiro
 */
class PLVMPAuxiliaryViewModel internal constructor(
    private val repo: PLVMPAuxiliaryRepo,
    private val useCases: PLVMPAuxiliaryUseCases
) : IPLVMPAuxiliaryPlayer by repo {

    val auxiliaryInfoViewState = repo.mediator.auxiliaryInfoViewState
    val auxiliaryPlayViewState = repo.mediator.auxiliaryPlayViewState

    fun setEnterFromFloatWindow(isEnterFromFloatWindow: Boolean) {
        this.useCases.beforePlayListener.setEnterFromFloatWindow(isEnterFromFloatWindow)
    }

}