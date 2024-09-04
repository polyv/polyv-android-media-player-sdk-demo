package net.polyv.android.player.common.modules.mediacontroller.model

import net.polyv.android.player.common.modules.media.mediator.PLVMPMediaMediator
import net.polyv.android.player.common.modules.mediacontroller.mediator.PLVMPMediaControllerMediator

/**
 * @author Hoshiiro
 */
internal class PLVMPMediaControllerRepo(
    val mediaMediator: PLVMPMediaMediator,
    val mediator: PLVMPMediaControllerMediator
)