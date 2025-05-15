package net.polyv.android.player.common.ui.component

import android.content.Context
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import net.polyv.android.player.business.scene.common.model.vo.PLVMediaBitRate
import net.polyv.android.player.business.scene.common.model.vo.PLVMediaOutputMode
import net.polyv.android.player.common.R
import net.polyv.android.player.common.di.PLVMediaPlayerLocalProvider
import net.polyv.android.player.common.modules.media.viewmodel.PLVMPMediaViewModel
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.PLVMPMediaControllerViewModel
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.viewstate.PLVMPMediaControllerFloatAction
import net.polyv.android.player.sdk.foundation.graphics.isLandscape
import net.polyv.android.player.sdk.foundation.graphics.isPortrait
import net.polyv.android.player.sdk.foundation.graphics.parseColor
import net.polyv.android.player.sdk.foundation.lang.PLVRememberState.Companion.rememberStateOf
import net.polyv.android.player.sdk.foundation.lang.format
import net.polyv.android.player.sdk.foundation.lang.requireNotNull

/**
 * @author Hoshiiro
 */
class PLVMediaPlayerNetworkPoorIndicateLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), View.OnClickListener {
    // <editor-fold defaultstate="collapsed" desc="变量">
    private val networkPoorIndicateTv by lazy { findViewById<TextView>(R.id.plv_media_player_network_poor_indicate_tv) }
    private val networkPoorIndicateCloseIv by lazy { findViewById<ImageView>(R.id.plv_media_player_network_poor_indicate_close_iv) }

    private var isVisible: Boolean = false
    private var alternativeBitRate: PLVMediaBitRate? = null
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化方法">
    init {
        LayoutInflater.from(context).inflate(R.layout.plv_media_player_ui_component_network_poor_indicate_layout, this)

        networkPoorIndicateCloseIv.setOnClickListener(this)
        onViewStateChanged()
    }

    // </editor-fold>
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (isInEditMode) return

        val mediaViewModel = PLVMediaPlayerLocalProvider.localDependScope.on(this).current()!!
            .get<PLVMPMediaViewModel>()
        val controllerViewModel = PLVMediaPlayerLocalProvider.localDependScope.on(this).current()!!
            .get<PLVMPMediaControllerViewModel>()

        mediaViewModel.networkPoorEvent
            .observeUntilViewDetached(this) {
                val supportBitRates = mediaViewModel.mediaInfoViewState.value!!.supportBitRates
                val currentBitRate = mediaViewModel.mediaInfoViewState.value!!.bitRate
                val outputMode = mediaViewModel.mediaInfoViewState.value!!.outputMode
                if (supportBitRates.isEmpty() || currentBitRate == null || outputMode == PLVMediaOutputMode.AUDIO_ONLY) {
                    return@observeUntilViewDetached
                }
                val nextDowngradeBitRate = supportBitRates
                    .filter { mediaBitRate -> mediaBitRate.index < currentBitRate.index }
                    .maxByOrNull { mediaBitRate -> mediaBitRate.index }
                if (nextDowngradeBitRate == null) {
                    return@observeUntilViewDetached
                }
                alternativeBitRate = nextDowngradeBitRate
                isVisible = true
                onViewStateChanged()
            }

        mediaViewModel.onChangeBitRateEvent
            .observeUntilViewDetached(this) {
                isVisible = false
                onViewStateChanged()
            }

        mediaViewModel.onPreparedEvent
            .observeUntilViewDetached(this) {
                isVisible = false
                onViewStateChanged()
            }

        controllerViewModel.mediaControllerViewState
            .observeUntilViewDetached(this) { viewState ->
                val lastFloatActionLayout = viewState.floatActionLayouts.lastOrNull()
                if ((isPortrait() && lastFloatActionLayout == PLVMPMediaControllerFloatAction.MORE)
                    || (isLandscape() && lastFloatActionLayout == PLVMPMediaControllerFloatAction.BITRATE)
                ) {
                    isVisible = false
                    onViewStateChanged()
                }
            }
    }

    private fun onViewStateChanged() {
        rememberStateOf("onNetworkPoorIndicate")
            .compareLastAndSet(isVisible, alternativeBitRate)
            .ifNotEquals {
                onNetworkPoorIndicate()
            }
    }

    private fun onNetworkPoorIndicate() {
        visibility = if (isVisible) VISIBLE else GONE
        setIndicateText()
    }

    private fun setIndicateText() {
        if (alternativeBitRate == null) {
            return
        }
        val sb = SpannableStringBuilder()
        sb.append(context.getString(R.string.plv_media_player_ui_component_network_poor_hint_text))
        sb.append(
            context.getString(R.string.plv_media_player_ui_component_network_poor_switch_bitrate_action_text)
                .format(alternativeBitRate!!.name),
            object : ClickableSpan() {
                override fun onClick(widget: View) {
                    onClickDowngradeBitRate()
                }

                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.color = parseColor("#3F76FC")
                    ds.isUnderlineText = false
                }
            },
            SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        networkPoorIndicateTv.text = sb
        networkPoorIndicateTv.movementMethod = LinkMovementMethod.getInstance()
    }

    // <editor-fold defaultstate="collapsed" desc="点击事件">
    override fun onClick(v: View) {
        when (v.id) {
            networkPoorIndicateCloseIv.id -> {
                isVisible = false
                onViewStateChanged()
            }

            else -> {}
        }
    }

    private fun onClickDowngradeBitRate() {
        requireNotNull(PLVMediaPlayerLocalProvider.localDependScope.on(this).current())
            .get(PLVMPMediaViewModel::class.java)
            .changeBitRate(alternativeBitRate!!)
    }

    // </editor-fold>
}
