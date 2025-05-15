package net.polyv.android.player.common.ui.component

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.TextView
import net.polyv.android.player.common.R
import net.polyv.android.player.common.di.PLVMediaPlayerLocalProvider
import net.polyv.android.player.common.modules.media.viewmodel.PLVMPMediaViewModel
import net.polyv.android.player.core.api.listener.state.PLVMediaPlayerState
import net.polyv.android.player.sdk.foundation.lang.PLVRememberState.Companion.rememberStateOf
import java.util.Locale

/**
 * @author Hoshiiro
 */
class PLVMediaPlayerBufferingSpeedLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {

    private val bufferingSpeedTv by lazy { findViewById<TextView>(R.id.plv_media_player_buffering_speed_tv) }

    private var currentIsPreparing: Boolean = false
    private var currentIsBuffering: Boolean = false
    private var trafficBytesPerSecond: Long = 0

    init {
        LayoutInflater.from(context).inflate(R.layout.plv_media_player_ui_component_buffering_speed_layout, this)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (isInEditMode) return

        PLVMediaPlayerLocalProvider.localDependScope.on(this).current()!!
            .get(PLVMPMediaViewModel::class.java)
            .mediaPlayViewState
            .observeUntilViewDetached(this) { viewState ->
                currentIsBuffering = viewState.isBuffering
                currentIsPreparing = viewState.playerState == PLVMediaPlayerState.STATE_PREPARING
                trafficBytesPerSecond = viewState.bufferingSpeed
                onViewStateChanged()
            }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        currentIsPreparing = false
        currentIsBuffering = false
        onViewStateChanged()
    }

    private fun onViewStateChanged() {
        rememberStateOf("onTrafficSpeedChanged")
            .compareLastAndSet(currentIsBuffering, currentIsPreparing, trafficBytesPerSecond)
            .ifNotEquals {
                onTrafficSpeedChanged()
            }
    }

    private fun onTrafficSpeedChanged() {
        val isLoading = currentIsBuffering || currentIsPreparing
        val showBufferingSpeed = isLoading && trafficBytesPerSecond >= 0
        if (!showBufferingSpeed) {
            visibility = GONE
            return
        }
        visibility = VISIBLE
        bufferingSpeedTv.text = speedText
    }

    private val speedText: String
        get() {
            val bytesPerSecond = trafficBytesPerSecond.toDouble()
            return if (bytesPerSecond < 1 shl 10) {
                String.format(Locale.getDefault(), "%.0fB/S", bytesPerSecond)
            } else if (bytesPerSecond < 1 shl 20) {
                String.format(Locale.getDefault(), "%.1fKB/S", bytesPerSecond / (1 shl 10))
            } else {
                String.format(Locale.getDefault(), "%.1fMB/S", bytesPerSecond / (1 shl 20))
            }
        }
}
