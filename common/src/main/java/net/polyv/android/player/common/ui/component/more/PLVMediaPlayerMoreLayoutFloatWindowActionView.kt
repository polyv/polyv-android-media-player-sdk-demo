package net.polyv.android.player.common.ui.component.more

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import net.polyv.android.player.business.scene.common.model.vo.PLVMediaOutputMode
import net.polyv.android.player.common.R
import net.polyv.android.player.common.di.PLVMediaPlayerLocalProvider
import net.polyv.android.player.common.modules.media.viewmodel.PLVMPMediaViewModel
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.PLVMPMediaControllerViewModel
import net.polyv.android.player.common.ui.component.floatwindow.PLVMediaPlayerFloatWindowManager
import net.polyv.android.player.common.utils.floatwindow.enums.PLVFloatWindowLaunchReason
import net.polyv.android.player.sdk.foundation.graphics.parseColor

/**
 * @author Hoshiiro
 */
class PLVMediaPlayerMoreLayoutFloatWindowActionView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), View.OnClickListener {
    private val floatWindowActionIv by lazy { findViewById<ImageView>(R.id.plv_media_player_float_window_action_iv) }
    private val floatWindowActionTv by lazy { findViewById<TextView>(R.id.plv_media_player_float_window_action_tv) }

    private var tintColorIconNormal = Color.WHITE
    private var tintColorIconSelected = parseColor("#3F76FC")
    private var textColorNormal = parseColor("#CCFFFFFF")
    private var textColorSelected = parseColor("#CC3F76FC")

    init {
        LayoutInflater.from(context)
            .inflate(R.layout.plv_media_player_ui_component_more_float_window_action_layout, this)
        parseAttrs(attrs)
        setOnClickListener(this)
    }

    private fun parseAttrs(attrs: AttributeSet?) {
        val typedArray = context.obtainStyledAttributes(
            attrs,
            R.styleable.PLVMediaPlayerMoreLayoutFloatWindowActionView
        )
        tintColorIconNormal = typedArray.getColor(
            R.styleable.PLVMediaPlayerMoreLayoutFloatWindowActionView_plv_icon_tint_normal,
            tintColorIconNormal
        )
        tintColorIconSelected = typedArray.getColor(
            R.styleable.PLVMediaPlayerMoreLayoutFloatWindowActionView_plv_icon_tint_selected,
            tintColorIconSelected
        )
        textColorNormal = typedArray.getColor(
            R.styleable.PLVMediaPlayerMoreLayoutFloatWindowActionView_plv_text_color_normal,
            textColorNormal
        )
        textColorSelected = typedArray.getColor(
            R.styleable.PLVMediaPlayerMoreLayoutFloatWindowActionView_plv_text_color_selected,
            textColorSelected
        )
        typedArray.recycle()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (isInEditMode) return

        PLVMediaPlayerFloatWindowManager.getInstance()
            .floatingViewShowState
            .observe { showing: Boolean? ->
                val currentFloatWindowShowing = showing ?: false
                if (currentFloatWindowShowing) {
                    floatWindowActionIv.setImageTintList(ColorStateList.valueOf(tintColorIconSelected))
                    floatWindowActionTv.setTextColor(textColorSelected)
                } else {
                    floatWindowActionIv.setImageTintList(ColorStateList.valueOf(tintColorIconNormal))
                    floatWindowActionTv.setTextColor(textColorNormal)
                }
            }
            .disposeOnDetached(this)

        PLVMediaPlayerLocalProvider.localDependScope.on(this).current()!!
            .get<PLVMPMediaViewModel>()
            .mediaInfoViewState
            .observe { viewState ->
                val currentMediaOutputMode = viewState.outputMode
                val show = currentMediaOutputMode == PLVMediaOutputMode.AUDIO_VIDEO
                visibility = if (show) VISIBLE else GONE
            }
            .disposeOnDetached(this)
    }


    override fun onClick(v: View?) {
        val dependScope = PLVMediaPlayerLocalProvider.localDependScope.on(this).current()
        dependScope?.get<PLVMPMediaControllerViewModel>()
            ?.launchFloatWindow(PLVFloatWindowLaunchReason.MANUAL)
        dependScope?.get<PLVMPMediaControllerViewModel>()
            ?.popFloatActionLayout()
    }
}
