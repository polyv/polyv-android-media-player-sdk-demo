package net.polyv.android.player.common.ui.component.auxiliary

import android.content.Context
import androidx.appcompat.widget.AppCompatTextView
import android.util.AttributeSet
import net.polyv.android.player.business.scene.common.model.vo.PLVMediaPlayStage
import net.polyv.android.player.common.R
import net.polyv.android.player.common.di.PLVMediaPlayerLocalProvider
import net.polyv.android.player.common.modules.auxiliary.viewmodel.PLVMPAuxiliaryViewModel
import net.polyv.android.player.sdk.foundation.lang.format
import net.polyv.android.player.sdk.foundation.lang.watchStates

/**
 * @author Hoshiiro
 */
class PLVMediaPlayerAuxiliaryCountDownTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (isInEditMode) return

        val dependScope = PLVMediaPlayerLocalProvider.localDependScope.on(this).current()!!

        watchStates {
            val infoState = dependScope.get<PLVMPAuxiliaryViewModel>().auxiliaryInfoViewState.value
            val isAdvertShowing = infoState != null && infoState.stage.isAuxiliaryStage()
            val currentPlayStage = infoState?.stage
            val isAdvertStage = currentPlayStage == PLVMediaPlayStage.HEAD_ADVERT || currentPlayStage == PLVMediaPlayStage.TAIL_ADVERT
            val showCountDown = isAdvertShowing && isAdvertStage
            visibility = if (showCountDown) VISIBLE else GONE
        }.disposeOnDetached(this)

        watchStates {
            val playState = dependScope.get<PLVMPAuxiliaryViewModel>().auxiliaryPlayViewState.value
            val countDownTimeLeft = playState?.timeLeftInSeconds ?: 0
            text = context.getString(R.string.plv_media_player_ui_component_auxiliary_time_left_text)
                .format(countDownTimeLeft)
        }.disposeOnDetached(this)
    }

}
