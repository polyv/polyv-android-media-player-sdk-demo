package net.polyv.android.player.common.ui.component.danmu

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.os.Bundle
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.Guideline
import androidx.appcompat.app.AppCompatActivity
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.polyv.android.common.libs.lang.state.derivedStateOf
import net.polyv.android.common.libs.lang.state.mutableStateOf
import net.polyv.android.common.libs.lang.state.watchStates
import net.polyv.android.common.libs.lang.ui.children
import net.polyv.android.player.business.scene.common.coroutine.PLVMediaPlayerGlobalCoroutineScope
import net.polyv.android.player.common.R
import net.polyv.android.player.common.modules.danmu.viewmodel.PLVMPDanmuViewModel
import net.polyv.android.player.common.modules.danmu.viewmodel.viewstate.PLVMPDanmuSize
import net.polyv.android.player.sdk.PLVDeviceManager.hideNavigationBar
import net.polyv.android.player.sdk.PLVDeviceManager.hideStatusBar
import net.polyv.android.player.sdk.addon.business.danmu.vo.PLVMediaPlayerDanmuMode
import net.polyv.android.player.sdk.foundation.graphics.dp
import net.polyv.android.player.sdk.foundation.graphics.parseColor
import kotlin.math.abs
import kotlin.math.min

/**
 * @author Hoshiiro
 */
class PLVMediaPlayerDanmuInputActivity : AppCompatActivity() {

    companion object {
        private var launchPayload: Payload? = null

        fun launch(from: Context, payload: Payload) {
            val intent = Intent(from, PLVMediaPlayerDanmuInputActivity::class.java)
            from.startActivity(intent)
            launchPayload = payload
        }
    }

    private val contentView by lazy { findViewById<ViewGroup>(android.R.id.content) }
    private val danmuInputKeyboardHeightGuideline by lazy { findViewById<Guideline>(R.id.plv_media_player_danmu_input_keyboard_height_guideline) }
    private val danmuInputContainer by lazy { findViewById<ConstraintLayout>(R.id.plv_media_player_danmu_input_container) }
    private val danmuStyleSettingIv by lazy { findViewById<ImageView>(R.id.plv_media_player_danmu_style_setting_iv) }
    private val danmuContentEt by lazy { findViewById<EditText>(R.id.plv_media_player_danmu_content_et) }
    private val danmuSendTv by lazy { findViewById<TextView>(R.id.plv_media_player_danmu_send_tv) }
    private val danmuStyleLayout by lazy { findViewById<ConstraintLayout>(R.id.plv_media_player_danmu_style_layout) }
    private val danmuStyleColorLayout by lazy { findViewById<ConstraintLayout>(R.id.plv_media_player_danmu_style_color_layout) }
    private val danmuStyleColorTv by lazy { findViewById<TextView>(R.id.plv_media_player_danmu_style_color_tv) }
    private val danmuStyleColorViewContainer by lazy { findViewById<LinearLayout>(R.id.plv_media_player_danmu_style_color_view_container) }
    private val danmuStyleModeLayout by lazy { findViewById<ConstraintLayout>(R.id.plv_media_player_danmu_style_mode_layout) }
    private val danmuStyleModeTv by lazy { findViewById<TextView>(R.id.plv_media_player_danmu_style_mode_tv) }
    private val danmuStyleModeRoll by lazy { findViewById<TextView>(R.id.plv_media_player_danmu_style_mode_roll) }
    private val danmuStyleModeTop by lazy { findViewById<TextView>(R.id.plv_media_player_danmu_style_mode_top) }
    private val danmuStyleModeBottom by lazy { findViewById<TextView>(R.id.plv_media_player_danmu_style_mode_bottom) }
    private val danmuStyleSizeLayout by lazy { findViewById<ConstraintLayout>(R.id.plv_media_player_danmu_style_size_layout) }
    private val danmuStyleSizeTv by lazy { findViewById<TextView>(R.id.plv_media_player_danmu_style_size_tv) }
    private val danmuStyleSizeSmall by lazy { findViewById<TextView>(R.id.plv_media_player_danmu_style_size_small) }
    private val danmuStyleSizeMedium by lazy { findViewById<TextView>(R.id.plv_media_player_danmu_style_size_medium) }
    private val danmuStyleSizeLarge by lazy { findViewById<TextView>(R.id.plv_media_player_danmu_style_size_large) }
    private val danmuInputHitAreaBackPress by lazy { findViewById<View>(R.id.plv_media_player_danmu_input_hit_area_back_press) }

    private val keyboardHeight = mutableStateOf(0)
    private val keyboardVisible = derivedStateOf { (keyboardHeight.value ?: 0) > 0 }
    private val styleSettingVisible = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideStatusBar(this)
        hideNavigationBar(this)
        setContentView(R.layout.plv_media_player_danmu_input_layout)

        initStyleSetting()
        observeKeyboardHeight()
        danmuInputHitAreaBackPress.setOnClickListener { onBackPressed() }
        danmuSendTv.setOnClickListener { onSendDanmu() }
        danmuContentEt.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                onSendDanmu()
                true
            } else false
        }
        danmuContentEt.requestFocus()

        launchPayload?.onCreate?.invoke()
    }

    private fun initStyleSetting() {
        watchStates {
            val visible = styleSettingVisible.value ?: false
            danmuStyleLayout.visibility = if (visible) View.VISIBLE else View.GONE
        }
        danmuStyleSettingIv.setOnClickListener {
            val toVisible = !(styleSettingVisible.value ?: false)
            if (toVisible) {
                hideKeyboard()
            }
            styleSettingVisible.setValue(toVisible)
        }

        fun bindDanmuColor(colorView: PLVMediaPlayerDanmuColorView) {
            watchStates {
                val currentColor = launchPayload?.danmuViewModel?.danmuStyle?.value?.color
                colorView.isSelected = colorView.color == currentColor
            }
            colorView.setOnClickListener { launchPayload?.danmuViewModel?.setDanmuColor(colorView.color) }
        }

        val colorViews = danmuStyleColorViewContainer.children().filterIsInstance<PLVMediaPlayerDanmuColorView>()
        colorViews.forEach { bindDanmuColor(it) }

        fun bindDanmuMode(textView: TextView, mode: PLVMediaPlayerDanmuMode) {
            watchStates {
                val currentMode = launchPayload?.danmuViewModel?.danmuStyle?.value?.mode
                textView.setTextColor(if (mode == currentMode) parseColor("#31adfe") else Color.WHITE)
            }
            textView.setOnClickListener { launchPayload?.danmuViewModel?.setDanmuMode(mode) }
        }
        bindDanmuMode(danmuStyleModeRoll, PLVMediaPlayerDanmuMode.ROLL_RIGHT_TO_LEFT)
        bindDanmuMode(danmuStyleModeTop, PLVMediaPlayerDanmuMode.FIX_TOP)
        bindDanmuMode(danmuStyleModeBottom, PLVMediaPlayerDanmuMode.FIX_BOTTOM)

        fun bindDanmuSize(textView: TextView, size: PLVMPDanmuSize) {
            watchStates {
                val currentSize = launchPayload?.danmuViewModel?.danmuStyle?.value?.size
                textView.setTextColor(if (size == currentSize) parseColor("#31adfe") else Color.WHITE)
            }
            textView.setOnClickListener { launchPayload?.danmuViewModel?.setDanmuSize(size) }
        }
        bindDanmuSize(danmuStyleSizeSmall, PLVMPDanmuSize.SMALL)
        bindDanmuSize(danmuStyleSizeMedium, PLVMPDanmuSize.MEDIUM)
        bindDanmuSize(danmuStyleSizeLarge, PLVMPDanmuSize.LARGE)
    }

    private fun observeKeyboardHeight() {
        contentView.viewTreeObserver.addOnGlobalLayoutListener {
            val decorView = window.decorView
            val outRect = Rect().apply { decorView.getWindowVisibleDisplayFrame(this) }
            keyboardHeight.setValue(abs(decorView.bottom - outRect.bottom))
        }
        watchStates {
            val keyboardHeight = keyboardHeight.value ?: 0
            danmuInputKeyboardHeightGuideline.setGuidelineEnd(keyboardHeight)
        }
        watchStates {
            val keyboardVisible = keyboardVisible.value ?: false
            if (keyboardVisible) {
                styleSettingVisible.setValue(false)
            }
        }
    }

    override fun onBackPressed() {
        if (handleBackPressed()) return
        super.onBackPressed()
    }

    private fun handleBackPressed(): Boolean {
        if (styleSettingVisible.value ?: false) {
            styleSettingVisible.setValue(false)
            return true
        }
        if (keyboardVisible.value ?: false) {
            hideKeyboard()
            return true
        }
        return false
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager?
        imm?.hideSoftInputFromWindow(danmuContentEt.windowToken, 0)
    }

    private fun onSendDanmu() {
        val danmuViewModel = launchPayload?.danmuViewModel ?: return
        val content = danmuContentEt.text.toString()
        if (content.isBlank()) return
        PLVMediaPlayerGlobalCoroutineScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO) {
                danmuViewModel.sendDanmu(content)
            }
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        launchPayload?.onDestory?.invoke()
        launchPayload = null
    }

    class Payload(
        val danmuViewModel: PLVMPDanmuViewModel,
        val onCreate: () -> Unit = {},
        val onDestory: () -> Unit = {},
    )

}

class PLVMediaPlayerDanmuColorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    val color: Int
    private val paintSolid: Paint
    private val paintStroke: Paint

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.PLVMediaPlayerDanmuColorView)
        color = typedArray.getColor(R.styleable.PLVMediaPlayerDanmuColorView_plv_color, Color.WHITE)
        typedArray.recycle()
        paintSolid = Paint().apply {
            color = this@PLVMediaPlayerDanmuColorView.color
            style = Paint.Style.FILL
        }
        paintStroke = Paint().apply {
            color = this@PLVMediaPlayerDanmuColorView.color
            style = Paint.Style.STROKE
            strokeWidth = 2.dp().px().toFloat()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cx = width.toFloat() / 2
        val cy = height.toFloat() / 2
        val radius = min(cx, cy)
        if (isSelected) {
            canvas.drawCircle(cx, cy, radius - paintStroke.strokeWidth / 2, paintStroke)
        }
        canvas.drawCircle(cx, cy, radius - paintStroke.strokeWidth - 2.dp().px(), paintSolid)
    }

}