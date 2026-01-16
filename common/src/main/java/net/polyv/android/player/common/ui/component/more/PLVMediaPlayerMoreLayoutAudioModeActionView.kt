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
import net.polyv.android.player.sdk.foundation.graphics.parseColor

/**
 * @author Hoshiiro
 */
class PLVMediaPlayerMoreLayoutAudioModeActionView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), View.OnClickListener {
    private val audioModeActionIv by lazy { findViewById<ImageView>(R.id.plv_media_player_audio_mode_action_iv) }
    private val audioModeActionTv by lazy { findViewById<TextView>(R.id.plv_media_player_audio_mode_action_tv) }

    private var tintColorIconNormal = Color.WHITE
    private var tintColorIconSelected = parseColor("#3F76FC")
    private var textColorNormal = parseColor("#CCFFFFFF")
    private var textColorSelected = parseColor("#CC3F76FC")

    init {
        LayoutInflater.from(context).inflate(R.layout.plv_media_player_ui_component_more_audio_mode_action_layout, this)
        parseAttrs(attrs)
        setOnClickListener(this)
    }

    private fun parseAttrs(attrs: AttributeSet?) {
        val typedArray = context.obtainStyledAttributes(
            attrs,
            R.styleable.PLVMediaPlayerMoreLayoutAudioModeActionView
        )
        tintColorIconNormal = typedArray.getColor(
            R.styleable.PLVMediaPlayerMoreLayoutAudioModeActionView_plv_icon_tint_normal,
            tintColorIconNormal
        )
        tintColorIconSelected = typedArray.getColor(
            R.styleable.PLVMediaPlayerMoreLayoutAudioModeActionView_plv_icon_tint_selected,
            tintColorIconSelected
        )
        textColorNormal = typedArray.getColor(
            R.styleable.PLVMediaPlayerMoreLayoutAudioModeActionView_plv_text_color_normal,
            textColorNormal
        )
        textColorSelected = typedArray.getColor(
            R.styleable.PLVMediaPlayerMoreLayoutAudioModeActionView_plv_text_color_selected,
            textColorSelected
        )
        typedArray.recycle()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (isInEditMode) return

        PLVMediaPlayerLocalProvider.localDependScope.on(this).current()!!
            .get<PLVMPMediaViewModel>()
            .mediaInfoViewState
            .observe { viewState ->
                val currentMediaOutputMode = viewState.outputMode
                if (currentMediaOutputMode == PLVMediaOutputMode.AUDIO_ONLY) {
                    audioModeActionIv.setImageTintList(ColorStateList.valueOf(tintColorIconSelected))
                    audioModeActionTv.setTextColor(textColorSelected)
                } else {
                    audioModeActionIv.setImageTintList(ColorStateList.valueOf(tintColorIconNormal))
                    audioModeActionTv.setTextColor(textColorNormal)
                }
            }
            .disposeOnDetached(this)
    }

    override fun onClick(v: View?) {
        val dependScope = PLVMediaPlayerLocalProvider.localDependScope.on(this).current()
        val currentMediaOutputMode = dependScope?.get<PLVMPMediaViewModel>()?.mediaInfoViewState?.value?.outputMode
        val currentIsAudioMode = currentMediaOutputMode == PLVMediaOutputMode.AUDIO_ONLY
        dependScope?.get<PLVMPMediaViewModel>()
            ?.changeMediaOutputMode(if (currentIsAudioMode) PLVMediaOutputMode.AUDIO_VIDEO else PLVMediaOutputMode.AUDIO_ONLY)
        dependScope?.get<PLVMPMediaControllerViewModel>()
            ?.popFloatActionLayout()
    }
}
