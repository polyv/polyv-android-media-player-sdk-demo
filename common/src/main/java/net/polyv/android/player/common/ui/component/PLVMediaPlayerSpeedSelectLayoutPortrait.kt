package net.polyv.android.player.common.ui.component

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.TextView
import net.polyv.android.player.common.di.PLVMediaPlayerLocalProvider
import net.polyv.android.player.common.modules.media.viewmodel.PLVMPMediaViewModel
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.PLVMPMediaControllerViewModel
import net.polyv.android.player.sdk.foundation.graphics.dp
import net.polyv.android.player.sdk.foundation.graphics.parseColor
import net.polyv.android.player.sdk.foundation.ui.children

/**
 * @author Hoshiiro
 */
private val SUPPORT_SPEED_LIST = listOf("0.5", "0.75", "1", "1.25", "1.5", "2", "3")
private val TEXT_COLOR_SELECTED = parseColor("#3F76FC")
private val TEXT_COLOR_NORMAL = parseColor("#333333")

class PLVMediaPlayerSpeedSelectLayoutPortrait @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : LinearLayout(context, attrs, defStyle) {

    init {
        orientation = HORIZONTAL
        initSpeedTextView()
    }

    private fun initSpeedTextView() {
        SUPPORT_SPEED_LIST
            .forEach { speed: String ->
                val textView = TextView(context).apply {
                    text = "${speed}x"
                    setTextColor(TEXT_COLOR_NORMAL)
                    textSize = 12F
                    tag = speed
                    setOnClickListener { onSelectSpeed(speed.toFloat()) }
                }
                val layoutParams = MarginLayoutParams(40.dp().px(), 17.dp().px())
                layoutParams.setMarginEnd(28.dp().px())
                addView(textView, layoutParams)
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
                this.children()
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
    }

    private fun onSelectSpeed(speed: Float) {
        PLVMediaPlayerLocalProvider.localDependScope.on(this).current()!!
            .get<PLVMPMediaViewModel>()
            .setSpeed(speed)
        PLVMediaPlayerLocalProvider.localDependScope.on(this).current()!!
            .get<PLVMPMediaControllerViewModel>()
            .popFloatActionLayout()
    }

}
