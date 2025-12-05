package net.polyv.android.player.common.ui.component

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import net.polyv.android.player.common.R
import net.polyv.android.player.common.di.PLVMediaPlayerLocalProvider
import net.polyv.android.player.common.modules.media.viewmodel.PLVMPMediaViewModel
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.PLVMPMediaControllerViewModel
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.viewstate.PLVMPMediaControllerFloatAction
import net.polyv.android.player.sdk.foundation.graphics.dp
import net.polyv.android.player.sdk.foundation.graphics.parseColor
import net.polyv.android.player.sdk.foundation.ui.children

/**
 * @author Hoshiiro
 */
private val SUPPORT_SPEED_LIST = listOf("0.5", "0.75", "1", "1.25", "1.5", "2", "3")
private val TEXT_COLOR_SELECTED = parseColor("#3F76FC")
private val TEXT_COLOR_NORMAL = Color.WHITE

class PLVMediaPlayerSpeedSelectLayoutLandscape @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle), View.OnClickListener {
    private val speedSelectCloseIv: ImageView
    private val speedSelectContainer: LinearLayout

    init {
        LayoutInflater.from(getContext()).inflate(R.layout.plv_media_player_ui_component_speed_select_layout, this)
        speedSelectCloseIv = findViewById<ImageView>(R.id.plv_media_player_speed_select_close_iv)
        speedSelectContainer = findViewById<LinearLayout>(R.id.plv_media_player_speed_select_container)

        initSpeedSelectLayout()

        setOnClickListener(this)
        speedSelectCloseIv.setOnClickListener(this)
    }

    private fun initSpeedSelectLayout() {
        SUPPORT_SPEED_LIST
            .forEach { speed: String ->
                val textView = TextView(context).apply {
                    text = "${speed}x"
                    setTextColor(TEXT_COLOR_NORMAL)
                    textSize = 12F
                    tag = speed
                    setOnClickListener { onSelectSpeed(speed.toFloat()) }
                }
                val lp = MarginLayoutParams(80.dp().px(), LayoutParams.WRAP_CONTENT)
                lp.topMargin = 24.dp().px()
                lp.bottomMargin = 24.dp().px()
                speedSelectContainer.addView(textView, lp)
            }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (isInEditMode) return

        PLVMediaPlayerLocalProvider.localDependScope.on(this).current()!!
            .get<PLVMPMediaViewModel>()
            .mediaPlayViewState
            .observe { viewState ->
                val currentSpeed = viewState.speed
                speedSelectContainer.children()
                    .filter { it is TextView && it.tag is String }
                    .forEach {
                        val textView = it as TextView
                        if ((textView.tag as String).toFloatOrNull() == currentSpeed) {
                            textView.setTextColor(TEXT_COLOR_SELECTED)
                        } else {
                            textView.setTextColor(TEXT_COLOR_NORMAL)
                        }
                    }
            }
            .disposeOnDetached(this)

        PLVMediaPlayerLocalProvider.localDependScope.on(this).current()!!
            .get<PLVMPMediaControllerViewModel>()
            .mediaControllerViewState
            .observe { viewState ->
                val isVisible = viewState.lastFloatActionLayout == PLVMPMediaControllerFloatAction.SPEED
                        && !viewState.isMediaStopOverlayVisible
                visibility = if (isVisible) View.VISIBLE else View.GONE
            }
            .disposeOnDetached(this)
    }

    override fun onClick(v: View) {
        val id = v.id
        if (id == speedSelectCloseIv.id) {
            closeLayout()
        } else if (id == this.id) {
            closeLayout()
        }
    }

    private fun onSelectSpeed(speed: Float) {
        PLVMediaPlayerLocalProvider.localDependScope.on(this).current()!!
            .get<PLVMPMediaViewModel>()
            .setSpeed(speed)
        closeLayout()
    }

    private fun closeLayout() {
        PLVMediaPlayerLocalProvider.localDependScope.on(this).current()!!
            .get<PLVMPMediaControllerViewModel>()
            .popFloatActionLayout()
    }

}
