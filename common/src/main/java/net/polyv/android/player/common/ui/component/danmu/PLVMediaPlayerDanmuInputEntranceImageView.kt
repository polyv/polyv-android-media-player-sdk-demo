package net.polyv.android.player.common.ui.component.danmu

import android.content.Context
import androidx.appcompat.widget.AppCompatImageView
import android.util.AttributeSet
import android.view.View
import net.polyv.android.player.common.di.PLVMediaPlayerLocalProvider
import net.polyv.android.player.common.modules.danmu.viewmodel.PLVMPDanmuViewModel
import net.polyv.android.player.common.modules.media.viewmodel.PLVMPMediaViewModel
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.PLVMPMediaControllerViewModel
import net.polyv.android.player.sdk.foundation.graphics.isLandscape

/**
 * @author Hoshiiro
 */
class PLVMediaPlayerDanmuInputEntranceImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private var pausePlayerOnShowDanmuInput = false

    init {
        setOnClickListener {
            val dependScope = PLVMediaPlayerLocalProvider.localDependScope.on(this).current()
                ?: return@setOnClickListener
            val danmuViewModel = dependScope.get<PLVMPDanmuViewModel>()
            val mediaViewModel = dependScope.get<PLVMPMediaViewModel>()
            val controllerViewModel = dependScope.get<PLVMPMediaControllerViewModel>()
            controllerViewModel.changeControllerVisible(false)
            PLVMediaPlayerDanmuInputActivity.launch(
                context,
                PLVMediaPlayerDanmuInputActivity.Payload(
                    danmuViewModel,
                    onCreate = {
                        if (mediaViewModel.mediaPlayViewState.value?.isPlaying ?: false) {
                            mediaViewModel.pause()
                            pausePlayerOnShowDanmuInput = true
                        }
                    },
                    onDestory = {
                        if (pausePlayerOnShowDanmuInput) {
                            mediaViewModel.start()
                        }
                        pausePlayerOnShowDanmuInput = false
                    }
                ))
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (isInEditMode) return

        PLVMediaPlayerLocalProvider.localDependScope.on(this).current()!!
            .get<PLVMPMediaControllerViewModel>()
            .mediaControllerViewState
            .observe { viewState ->
                val isVisible = viewState.controllerVisible
                        && !viewState.isMediaStopOverlayVisible
                        && !viewState.controllerLocking
                        && !(viewState.isFloatActionLayoutVisible && isLandscape())
                visibility = if (isVisible) View.VISIBLE else View.GONE
            }
            .disposeOnDetached(this)
    }

}