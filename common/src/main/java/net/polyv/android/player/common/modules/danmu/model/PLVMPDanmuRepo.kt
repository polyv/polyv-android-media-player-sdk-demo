package net.polyv.android.player.common.modules.danmu.model

import net.polyv.android.player.common.modules.media.mediator.PLVMPMediaMediator

/**
 * @author Hoshiiro
 */
class PLVMPDanmuRepo(
    val mediaMediator: PLVMPMediaMediator
) {

    val danmuManager by lazy { mediaMediator.addonBusinessManager!!().danmu }

}