package net.polyv.android.player.common.ui.component

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import net.polyv.android.common.libs.lang.ui.removeFromParent
import net.polyv.android.player.common.di.PLVMediaPlayerLocalProvider
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.PLVMPMediaControllerViewModel

/**
 * @author Hoshiiro
 */
class PLVMediaPlayerVideoViewContainer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (isInEditMode) return

        val dependScope = PLVMediaPlayerLocalProvider.localDependScope.on(this).current()!!
        dependScope.get<PLVMPMediaControllerViewModel>()
            .mediaControllerViewState
            .observe { viewState ->
                val translation = viewState.mediaViewTranslation
                translationX = translation.offsetX
                translationY = translation.offsetY
                scaleX = translation.scale
                scaleY = translation.scale
                rotation = translation.rotateAngle
            }
            .disposeOnDetached(this)
    }

    fun setVideoView(view: View?) {
        removeAllViews()
        view?.removeFromParent()
        if (view != null) {
            addView(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        }
    }

}