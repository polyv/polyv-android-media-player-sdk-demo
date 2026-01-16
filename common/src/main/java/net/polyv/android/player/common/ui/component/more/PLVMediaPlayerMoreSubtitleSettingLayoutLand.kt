package net.polyv.android.player.common.ui.component.more

import android.content.Context
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.Guideline
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.CompoundButton
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import net.polyv.android.player.business.scene.common.model.vo.PLVMediaSubtitle
import net.polyv.android.player.common.R
import net.polyv.android.player.common.di.PLVMediaPlayerLocalProvider
import net.polyv.android.player.common.modules.media.viewmodel.PLVMPMediaViewModel
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.PLVMPMediaControllerViewModel
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.viewstate.PLVMPMediaControllerFloatAction
import net.polyv.android.player.sdk.foundation.graphics.parseColor
import net.polyv.android.player.sdk.foundation.lang.PLVRememberState.Companion.rememberStateOf
import net.polyv.android.player.sdk.foundation.ui.children

/**
 * @author Hoshiiro
 */
class PLVMediaPlayerMoreSubtitleSettingLayoutLand @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    private val moreActionCloseIv by lazy { findViewById<ImageView>(R.id.plv_media_player_more_action_close_iv) }
    private val moreActionContainer by lazy { findViewById<ConstraintLayout>(R.id.plv_media_player_more_action_container) }
    private val moreActionStartGuideLine by lazy { findViewById<Guideline>(R.id.more_action_start_guide_line) }
    private val moreSubtitleSettingShowSwitchLabel by lazy { findViewById<TextView>(R.id.plv_media_player_more_subtitle_setting_show_switch_label) }
    private val moreSubtitleSettingShowSwitch by lazy { findViewById<Switch>(R.id.plv_media_player_more_subtitle_setting_show_switch) }
    private val moreSubtitleContainer by lazy { findViewById<LinearLayout>(R.id.plv_media_player_more_subtitle_container) }

    private var lastSelectedSubtitle: List<PLVMediaSubtitle>? = null

    init {
        LayoutInflater.from(context)
            .inflate(R.layout.plv_media_player_ui_component_more_subtitle_setting_layout_land, this)
        setOnClickListener(this)
        moreActionCloseIv.setOnClickListener(this)
        moreSubtitleSettingShowSwitch.setOnCheckedChangeListener(this)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (isInEditMode) return

        val dependScope = PLVMediaPlayerLocalProvider.localDependScope.on(this).current()!!

        dependScope
            .get<PLVMPMediaViewModel>()
            .mediaInfoViewState
            .observe { viewState ->
                val isSubtitleEnable = viewState.currentSubtitle != null && !viewState.currentSubtitle.isEmpty()
                val selectedSubtitle = viewState.currentSubtitle
                if (selectedSubtitle != null && !selectedSubtitle.isEmpty()) {
                    lastSelectedSubtitle = selectedSubtitle
                }

                moreSubtitleContainer.visibility = if (isSubtitleEnable) VISIBLE else GONE
                moreSubtitleSettingShowSwitch.setChecked(isSubtitleEnable)

                moreSubtitleContainer.children()
                    .filterIsInstance<SubtitleItemLayout>()
                    .forEach {
                        it.updateSelectState(selectedSubtitle)
                    }
            }
            .disposeOnDetached(this)

        dependScope
            .get<PLVMPMediaViewModel>()
            .mediaInfoViewState
            .observe { viewState ->
                val supportSubtitleList = viewState.supportSubtitles
                rememberStateOf("updateSupportSubtitleList")
                    .compareLastAndSet(supportSubtitleList)
                    .ifNotEquals {
                        moreSubtitleContainer.removeAllViews()
                        supportSubtitleList
                            .map { subtitle -> SubtitleItemLayout(context, subtitle) }
                            .forEach { subtitleItemLayout ->
                                moreSubtitleContainer.addView(subtitleItemLayout)
                            }
                    }
            }
            .disposeOnDetached(this)

        dependScope
            .get<PLVMPMediaControllerViewModel>()
            .mediaControllerViewState
            .observe { viewState ->
                val isVisible = viewState.lastFloatActionLayout == PLVMPMediaControllerFloatAction.SUBTITLE
                        && !viewState.isMediaStopOverlayVisible
                visibility = if (isVisible) VISIBLE else GONE
                parent?.requestDisallowInterceptTouchEvent(!isVisible)
                parent?.requestDisallowInterceptTouchEvent(isVisible)
            }
            .disposeOnDetached(this)
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        when (buttonView.id) {
            moreSubtitleSettingShowSwitch.id -> {
                val dependScope = PLVMediaPlayerLocalProvider.localDependScope.on(this).current()
                if (!isChecked) {
                    dependScope?.get<PLVMPMediaViewModel>()?.setShowSubtitles(emptyList())
                } else {
                    lastSelectedSubtitle?.let {
                        dependScope?.get<PLVMPMediaViewModel>()?.setShowSubtitles(it)
                    }
                }
            }
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            moreActionCloseIv.id -> closeLayout()
            this.id -> closeLayout()
        }
    }

    private fun closeLayout() {
        PLVMediaPlayerLocalProvider.localDependScope.on(this).current()
            ?.get<PLVMPMediaControllerViewModel>()
            ?.popFloatActionLayout()
    }

    private class SubtitleItemLayout(
        context: Context,
        private val subtitle: List<PLVMediaSubtitle>
    ) : FrameLayout(context), OnClickListener {
        private val moreSubtitleItemText by lazy { findViewById<TextView>(R.id.plv_media_player_more_subtitle_item_text) }

        init {
            LayoutInflater.from(context).inflate(R.layout.plv_media_player_ui_component_more_subtitle_item_land, this)
            moreSubtitleItemText.text = if (subtitle.isEmpty()) {
                ""
            } else if (subtitle.size == 1) {
                subtitle[0].name
            } else {
                context.getString(R.string.plv_media_player_ui_component_subtitle_setting_double_subtitle_prefix) +
                        subtitle.joinToString("/") { it.name }
            }
            setOnClickListener(this)
        }

        fun updateSelectState(selectedSubtitle: List<PLVMediaSubtitle>?) {
            if (this.subtitle == selectedSubtitle) {
                moreSubtitleItemText.setTextColor(parseColor("#3F76FC"))
            } else {
                moreSubtitleItemText.setTextColor(parseColor("#FFFFFF"))
            }
        }

        override fun onClick(v: View?) {
            PLVMediaPlayerLocalProvider.localDependScope.on(this).current()
                ?.get<PLVMPMediaViewModel>()
                ?.setShowSubtitles(subtitle)
        }
    }
}
