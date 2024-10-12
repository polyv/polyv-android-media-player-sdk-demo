package net.polyv.android.player.common.modules.download.single.model

import net.polyv.android.player.common.modules.download.single.mediator.PLVMPDownloadItemMediator
import net.polyv.android.player.common.modules.media.mediator.PLVMPMediaMediator

/**
 * @author Hoshiiro
 */
internal class PLVMPDownloadItemRepo(
    val mediator: PLVMPDownloadItemMediator,
    val mediaMediator: PLVMPMediaMediator
)