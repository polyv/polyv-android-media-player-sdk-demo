package net.polyv.android.player.common.modules.auxiliary.model

import net.polyv.android.player.business.scene.auxiliary.player.PLVAuxiliaryMediaPlayer
import net.polyv.android.player.common.modules.auxiliary.mediator.PLVMPAuxiliaryMediator
import net.polyv.android.player.common.modules.media.mediator.PLVMPMediaMediator
import net.polyv.android.player.core.api.render.IPLVMediaPlayerRenderView
import net.polyv.android.player.sdk.foundation.di.LifecycleAwareDependComponent

/**
 * @author Hoshiiro
 */
internal class PLVMPAuxiliaryRepo(
    val mediaMediator: PLVMPMediaMediator,
    val mediator: PLVMPAuxiliaryMediator
) : IPLVMPAuxiliaryPlayer, LifecycleAwareDependComponent {

    val auxiliaryPlayer = PLVAuxiliaryMediaPlayer()

    override fun setRenderView(renderView: IPLVMediaPlayerRenderView) {
        auxiliaryPlayer.setRenderView(renderView)
    }

    override fun bind() {
        mediaMediator.bindAuxiliaryPlayer?.invoke(auxiliaryPlayer)
    }

    override fun unbind() {
        auxiliaryPlayer.unbind()
    }

    override fun onDestroy() {
        unbind()
        auxiliaryPlayer.destroy()
    }

}