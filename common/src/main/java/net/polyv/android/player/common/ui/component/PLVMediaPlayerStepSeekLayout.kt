package net.polyv.android.player.common.ui.component

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import androidx.appcompat.widget.AppCompatImageView
import android.util.AttributeSet
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import net.polyv.android.player.common.R
import net.polyv.android.player.common.di.PLVMediaPlayerLocalProvider
import net.polyv.android.player.common.modules.auxiliary.viewmodel.PLVMPAuxiliaryViewModel
import net.polyv.android.player.common.modules.media.viewmodel.PLVMPMediaViewModel
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.PLVMPMediaControllerViewModel
import net.polyv.android.player.core.api.listener.state.PLVMediaPlayerState
import net.polyv.android.player.sdk.foundation.lang.watchStates
import kotlin.math.sqrt

/** 公共皮肤组件：与控制栏一起显隐，只占用中央按钮区域，不拦截其余画面。 */
class PLVMediaPlayerStepSeekLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    private val backward = createButton()
    private val play = createButton()
    private val forward = createButton()
    private val backwardIcon = StepIcon(false)
    private val forwardIcon = StepIcon(true)

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER
        layoutDirection = LAYOUT_DIRECTION_LTR
        visibility = GONE
        backward.setImageDrawable(backwardIcon)
        forward.setImageDrawable(forwardIcon)
        addView(backward)
        addView(play, LayoutParams(dp(56), dp(56)).apply {
            marginStart = dp(20)
            marginEnd = dp(20)
        })
        addView(forward)
    }

    private fun createButton() = AppCompatImageView(context).apply {
        layoutParams = LayoutParams(dp(56), dp(56))
        setPadding(dp(8), dp(8), dp(8), dp(8))
        scaleType = ImageView.ScaleType.FIT_CENTER
        isFocusable = true
        isClickable = true
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (isInEditMode) return
        val scope = PLVMediaPlayerLocalProvider.localDependScope.on(this).current()!!
        val media = scope.get<PLVMPMediaViewModel>()
        val controller = scope.get<PLVMPMediaControllerViewModel>()
        val auxiliary = scope.get<PLVMPAuxiliaryViewModel>()
        backward.setOnClickListener { controller.seekByStep(false) }
        forward.setOnClickListener { controller.seekByStep(true) }
        play.setOnClickListener {
            if (media.mediaPlayViewState.value?.isPlaying == true) media.pause() else media.start()
        }
        watchStates {
            val state = controller.mediaControllerViewState.value ?: return@watchStates
            val playback = media.mediaPlayViewState.value ?: return@watchStates
            val advert = auxiliary.auxiliaryInfoViewState.value
            val ready = playback.playerState in listOf(
                PLVMediaPlayerState.STATE_PREPARED,
                PLVMediaPlayerState.STATE_PLAYING,
                PLVMediaPlayerState.STATE_PAUSED
            )
            val visible = state.seekButtonsEnabled && state.controllerVisible && !state.controllerLocking
                    && !state.isMediaStopOverlayVisible && !state.isFloatActionLayoutVisible
                    && !state.progressSeekBarDragging && !state.longPressSpeeding
                    && playback.duration > 0 && ready
                    && (advert == null || !advert.stage.isAuxiliaryStage())
            visibility = if (visible) VISIBLE else GONE
            backwardIcon.step = state.seekStepSeconds
            forwardIcon.step = state.seekStepSeconds
            backward.contentDescription = context.getString(R.string.plv_media_player_step_backward, state.seekStepSeconds)
            forward.contentDescription = context.getString(R.string.plv_media_player_step_forward, state.seekStepSeconds)
            play.setImageResource(if (playback.isPlaying) R.drawable.plv_media_player_play_button_icon_to_pause_landscape
                else R.drawable.plv_media_player_play_button_icon_to_play_landscape)
            play.contentDescription = context.getString(if (playback.isPlaying) R.string.plv_media_player_pause else R.string.plv_media_player_play)
        }.disposeOnDetached(this)
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density + 0.5F).toInt()

    private class StepIcon(private val forward: Boolean) : Drawable() {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
        var step = 15
            set(value) {
                if (field != value) {
                    field = value
                    invalidateSelf()
                }
            }

        override fun draw(canvas: Canvas) {
            val saved = canvas.save()
            canvas.translate(bounds.left.toFloat(), bounds.top.toFloat())
            canvas.scale(bounds.width() / 32F, bounds.height() / 32F)
            val arrow = canvas.save()
            // 缺口朝向跳转方向，文字保持正向；箭头两翼沿切线对称。
            canvas.rotate(if (forward) 90F else -90F, 16F, 16F)
            if (!forward) canvas.scale(-1F, 1F, 16F, 16F)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 2.2F
            paint.strokeCap = Paint.Cap.ROUND
            paint.strokeJoin = Paint.Join.ROUND
            canvas.drawArc(RectF(5F, 5F, 27F, 27F), -45F, 270F, false, paint)
            val diagonal = 11F / sqrt(2F)
            val tipX = 16F - diagonal
            val tipY = 16F - diagonal
            val tangent = 1F / sqrt(2F)
            val baseX = tipX - 4F * tangent
            val baseY = tipY + 4F * tangent
            canvas.drawPath(Path().apply {
                moveTo(baseX - 4F * tangent, baseY - 4F * tangent)
                lineTo(tipX, tipY)
                lineTo(baseX + 4F * tangent, baseY + 4F * tangent)
            }, paint)
            canvas.restoreToCount(arrow)
            paint.style = Paint.Style.FILL
            paint.typeface = Typeface.DEFAULT_BOLD
            paint.textAlign = Paint.Align.CENTER
            paint.textSize = 11F
            canvas.drawText(step.toString(), 16F, 16F - (paint.ascent() + paint.descent()) / 2F, paint)
            canvas.restoreToCount(saved)
        }

        override fun setAlpha(alpha: Int) { paint.alpha = alpha; invalidateSelf() }
        override fun setColorFilter(colorFilter: ColorFilter?) { paint.colorFilter = colorFilter; invalidateSelf() }
        override fun getOpacity() = PixelFormat.TRANSLUCENT
    }
}
