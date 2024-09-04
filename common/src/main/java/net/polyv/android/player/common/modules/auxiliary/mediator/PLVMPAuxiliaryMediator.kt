package net.polyv.android.player.common.modules.auxiliary.mediator

import net.polyv.android.player.common.modules.auxiliary.viewmodel.viewstate.PLVMPAuxiliaryInfoViewState
import net.polyv.android.player.common.modules.auxiliary.viewmodel.viewstate.PLVMPAuxiliaryPlayViewState
import net.polyv.android.player.sdk.foundation.lang.MutableState

/**
 * @author Hoshiiro
 */
class PLVMPAuxiliaryMediator {

    val auxiliaryInfoViewState = MutableState<PLVMPAuxiliaryInfoViewState?>()
    val auxiliaryPlayViewState = MutableState(PLVMPAuxiliaryPlayViewState())

}