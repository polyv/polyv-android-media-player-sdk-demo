package net.polyv.android.player.common.ui.component.more

import android.content.Context
import androidx.appcompat.widget.AppCompatImageView
import android.util.AttributeSet
import android.view.View
import net.polyv.android.player.common.di.PLVMediaPlayerLocalProvider
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.PLVMPMediaControllerViewModel
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.viewstate.PLVMPMediaControllerFloatAction
import net.polyv.android.player.sdk.foundation.graphics.isLandscape

/**
 * @author Hoshiiro
 */
class PLVMediaPlayerMoreActionImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr), View.OnClickListener {

    init {
        setOnClickListener(this)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (isInEditMode) return

        PLVMediaPlayerLocalProvider.localDependScope.on(this).current()!!
            .get<PLVMPMediaControllerViewModel>()
            .mediaControllerViewState
            .observe { viewState ->
                var isVisible = viewState.controllerVisible
                        && !viewState.isMediaStopOverlayVisible && !viewState.progressSeekBarDragging && !viewState.controllerLocking && !(viewState.isFloatActionLayoutVisible && isLandscape())
                isVisible = isVisible || viewState.isMediaStopOverlayVisible
                visibility = if (isVisible) VISIBLE else GONE
            }
            .disposeOnDetached(this)
    }

    override fun onClick(v: View?) {
        PLVMediaPlayerLocalProvider.localDependScope.on(this).current()
            ?.get<PLVMPMediaControllerViewModel>()
            ?.pushFloatActionLayout(PLVMPMediaControllerFloatAction.MORE)
    }

}
