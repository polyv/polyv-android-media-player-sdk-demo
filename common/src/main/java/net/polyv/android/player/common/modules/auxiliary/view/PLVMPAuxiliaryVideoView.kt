package net.polyv.android.player.common.modules.auxiliary.view

import android.content.Context
import net.polyv.android.player.common.modules.auxiliary.model.PLVMPAuxiliaryRepo
import net.polyv.android.player.sdk.PLVAuxiliaryVideoView
import net.polyv.android.player.sdk.foundation.di.DependScope

/**
 * @author Hoshiiro
 */
class PLVMPAuxiliaryVideoView(
    context: Context,
    dependScope: DependScope
) : PLVAuxiliaryVideoView(
    context = context,
    player = dependScope.get<PLVMPAuxiliaryRepo>().auxiliaryPlayer
)