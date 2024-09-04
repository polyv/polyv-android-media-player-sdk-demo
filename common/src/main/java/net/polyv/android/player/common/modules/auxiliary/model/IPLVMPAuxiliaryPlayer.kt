package net.polyv.android.player.common.modules.auxiliary.model

import net.polyv.android.player.core.api.render.IPLVMediaPlayerRenderView

/**
 * @author Hoshiiro
 */
interface IPLVMPAuxiliaryPlayer {

    fun setRenderView(renderView: IPLVMediaPlayerRenderView)

    fun bind()

    fun unbind()

}