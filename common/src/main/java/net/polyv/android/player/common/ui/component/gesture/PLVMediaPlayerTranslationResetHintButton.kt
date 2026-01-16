package net.polyv.android.player.common.ui.component.gesture

import android.content.Context
import androidx.appcompat.widget.AppCompatTextView
import android.util.AttributeSet
import android.view.View
import net.polyv.android.player.common.di.PLVMediaPlayerLocalProvider
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.PLVMPMediaControllerViewModel
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.viewstate.PLVMPMediaViewTranslation
import net.polyv.android.player.sdk.foundation.graphics.isLandscape

/**
 * @author Hoshiiro
 */
class PLVMediaPlayerTranslationResetHintButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : AppCompatTextView(context, attrs, defStyle) {

    init {
        setOnClickListener {
            PLVMediaPlayerLocalProvider.localDependScope.on(this).current()
                ?.get<PLVMPMediaControllerViewModel>()
                ?.setMediaViewTranslation(PLVMPMediaViewTranslation.IDENTITY)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (isInEditMode) return

        val dependScope = PLVMediaPlayerLocalProvider.localDependScope.on(this).current()!!
        dependScope.get<PLVMPMediaControllerViewModel>()
            .mediaControllerViewState
            .observe { viewState ->
                val visible = viewState.mediaViewTranslation != PLVMPMediaViewTranslation.IDENTITY
                        && viewState.controllerVisible
                        && !viewState.isMediaStopOverlayVisible
                        && !viewState.controllerLocking
                        && !(viewState.isFloatActionLayoutVisible && isLandscape())
                visibility = if (visible) View.VISIBLE else View.GONE
            }
            .disposeOnDetached(this)
    }

}