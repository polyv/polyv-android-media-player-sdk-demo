package net.polyv.android.player.common.ui.component.gesture

import android.app.Activity
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.FrameLayout
import net.polyv.android.player.common.R
import net.polyv.android.player.common.di.PLVMediaPlayerLocalProvider
import net.polyv.android.player.common.modules.media.viewmodel.PLVMPMediaViewModel
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.ChangeDirection
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.DragSeekBarAction
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.LongPressSpeedingAction
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.PLVMPMediaControllerViewModel
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.viewstate.PLVMPMediaViewTranslation
import net.polyv.android.player.sdk.foundation.collections.listOf
import net.polyv.android.player.sdk.foundation.graphics.getScreenHeight
import net.polyv.android.player.sdk.foundation.graphics.getScreenWidth
import net.polyv.android.player.sdk.foundation.lang.Duration.Companion.minutes
import net.polyv.android.player.sdk.foundation.lang.clamp
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * @author Hoshiiro
 */
class PLVMediaPlayerGestureHandleLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {

    private val brightnessGestureHandler = BrightnessGestureHandler(this)
    private val volumeGestureHandler = VolumeGestureHandler(this)
    private val horizonDragGestureHandler = HorizonDragGestureHandler(this)
    private val longPressGestureHandler = LongPressGestureHandler(this)
    private val mediaTranslationGestureHandler = MediaTranslationGestureHandler(this)
    private val gestureHandlers = listOf<GestureHandler>(
        brightnessGestureHandler,
        volumeGestureHandler,
        horizonDragGestureHandler,
        longPressGestureHandler,
        mediaTranslationGestureHandler,
    )
    private var isAllowControl: Boolean = false
    private var lastGestureHandler: GestureHandler? = null

    init {
        val typedArray = context.obtainStyledAttributes(
            attrs,
            R.styleable.PLVMediaPlayerGestureHandleLayout
        )
        brightnessGestureHandler.enable = typedArray.getBoolean(
            R.styleable.PLVMediaPlayerGestureHandleLayout_plv_brightness_gesture_enable,
            false
        )
        volumeGestureHandler.enable = typedArray.getBoolean(
            R.styleable.PLVMediaPlayerGestureHandleLayout_plv_volume_gesture_enable,
            false
        )
        horizonDragGestureHandler.enable = typedArray.getBoolean(
            R.styleable.PLVMediaPlayerGestureHandleLayout_plv_horizon_drag_gesture_enable,
            false
        )
        longPressGestureHandler.enable = typedArray.getBoolean(
            R.styleable.PLVMediaPlayerGestureHandleLayout_plv_long_press_gesture_enable,
            false
        )
        mediaTranslationGestureHandler.enable = typedArray.getBoolean(
            R.styleable.PLVMediaPlayerGestureHandleLayout_plv_translation_gesture_enable,
            false
        )
        typedArray.recycle()
    }

    fun handleOnTouchEvent(event: MotionEvent): Boolean {
        if (!isAllowControl) return false

        return when (event.action) {
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                lastGestureHandler = null
                gestureHandlers.map { it.enable && it.handle(event) }.any { it }
            }

            else -> {
                if (lastGestureHandler != null) {
                    lastGestureHandler!!.handle(event)
                } else {
                    lastGestureHandler = gestureHandlers.firstOrNull { it.enable && it.handle(event) }
                    lastGestureHandler != null
                }
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (isInEditMode) return

        PLVMediaPlayerLocalProvider.localDependScope.on(this).current()!!
            .get<PLVMPMediaControllerViewModel>()
            .mediaControllerViewState
            .observe {
                isAllowControl = !it.controllerLocking
            }
            .disposeOnDetached(this)
    }

}

private sealed class GestureHandler {
    var enable: Boolean = false
    abstract fun handle(event: MotionEvent): Boolean
}

private class BrightnessGestureHandler(
    private val parent: ViewGroup
) : GestureHandler(), GestureDetector.OnGestureListener {
    private val gestureDetector = GestureDetector(parent.context, this)

    private var isScrolling = false
    private var isScrollingVertical = false
    private var isScrollingTargetSide = false
    private var accumulateAdjustDiff = 0.0

    override fun handle(event: MotionEvent): Boolean {
        val consume = isScrollingVertical && isScrollingTargetSide
        gestureDetector.onTouchEvent(event)
        if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL) {
            isScrolling = false
            isScrollingVertical = false
            isScrollingTargetSide = false
            accumulateAdjustDiff = 0.0
        }
        return consume
    }

    override fun onDown(e: MotionEvent): Boolean {
        return false
    }

    override fun onShowPress(e: MotionEvent) {
    }

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        return false
    }

    override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
        if (e1 == null) return false

        if (!isScrolling) {
            isScrolling = true
            isScrollingVertical = abs(distanceY.toDouble()) > abs(distanceX.toDouble())
            isScrollingTargetSide = e1.x < parent.width.toFloat() / 2
        }
        val handle = isScrollingVertical && isScrollingTargetSide
        if (handle) {
            handleOnScrolling(distanceY)
        }
        return handle
    }

    override fun onLongPress(e: MotionEvent) {
    }

    override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
        return false
    }

    private fun handleOnScrolling(distanceY: Float) {
        val diff = accumulateAdjustDiff + (distanceY / (0.4 * getScreenHeight().px()) * 100)
        if (abs(diff) < 8) {
            accumulateAdjustDiff = diff
            return
        }

        val direction = if (diff > 0) ChangeDirection.UP else ChangeDirection.DOWN
        PLVMediaPlayerLocalProvider.localDependScope.on(parent).current()
            ?.get(PLVMPMediaControllerViewModel::class.java)
            ?.changeBrightness(direction, (parent.context as Activity))
        accumulateAdjustDiff = 0.0
    }

}

private class VolumeGestureHandler(
    private val parent: ViewGroup
) : GestureHandler(), GestureDetector.OnGestureListener {
    private val gestureDetector = GestureDetector(parent.context, this)

    private var isScrolling = false
    private var isScrollingVertical = false
    private var isScrollingTargetSide = false
    private var accumulateAdjustDiff = 0.0

    override fun handle(event: MotionEvent): Boolean {
        val consume = isScrollingVertical && isScrollingTargetSide
        gestureDetector.onTouchEvent(event)
        if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL) {
            isScrolling = false
            isScrollingVertical = false
            isScrollingTargetSide = false
            accumulateAdjustDiff = 0.0
        }
        return consume
    }

    override fun onDown(e: MotionEvent): Boolean {
        return false
    }

    override fun onShowPress(e: MotionEvent) {
    }

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        return false
    }

    override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
        if (e1 == null) return false

        if (!isScrolling) {
            isScrolling = true
            isScrollingVertical = abs(distanceY.toDouble()) > abs(distanceX.toDouble())
            isScrollingTargetSide = e1.x > parent.width.toFloat() / 2
        }
        val handle = isScrollingVertical && isScrollingTargetSide
        if (handle) {
            handleOnScrolling(distanceY)
        }
        return handle
    }

    override fun onLongPress(e: MotionEvent) {
    }

    override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
        return false
    }

    private fun handleOnScrolling(distanceY: Float) {
        val diff = accumulateAdjustDiff + (distanceY / (0.4 * getScreenHeight().px()) * 100)
        if (abs(diff) < 8) {
            accumulateAdjustDiff = diff
            return
        }

        val direction = if (diff > 0) ChangeDirection.UP else ChangeDirection.DOWN
        PLVMediaPlayerLocalProvider.localDependScope.on(parent).current()
            ?.get(PLVMPMediaControllerViewModel::class.java)
            ?.changeVolume(direction, parent.context)
        accumulateAdjustDiff = 0.0
    }

}

private class HorizonDragGestureHandler(
    private val parent: ViewGroup
) : GestureHandler(), GestureDetector.OnGestureListener {

    private val gestureDetector = GestureDetector(parent.context, this)

    private var isScrolling = false
    private var isScrollingHorizontal = false

    private var position: Long = -1
    private var duration: Long = -1

    override fun handle(event: MotionEvent): Boolean {
        val consume = isScrollingHorizontal
        gestureDetector.onTouchEvent(event)
        if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL) {
            onActionUp()
        }
        return consume
    }

    override fun onDown(e: MotionEvent): Boolean {
        return false
    }

    override fun onShowPress(e: MotionEvent) {
    }

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        return false
    }

    override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
        if (e1 == null) return false

        if (!isScrolling) {
            isScrolling = true
            isScrollingHorizontal = abs(distanceX.toDouble()) > abs(distanceY.toDouble())
            saveCurrentProgress()
        }
        if (isScrollingHorizontal) {
            handleScrolling(e1, e2)
        }
        return isScrollingHorizontal
    }

    private fun onActionUp() {
        if (isScrollingHorizontal) {
            PLVMediaPlayerLocalProvider.localDependScope.on(parent).current()
                ?.get(PLVMPMediaControllerViewModel::class.java)
                ?.handleDragSeekBar(DragSeekBarAction.FINISH)
        }
        isScrolling = false
        isScrollingHorizontal = false
        position = -1
        duration = -1
    }

    override fun onLongPress(e: MotionEvent) {
    }

    override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
        return false
    }

    private fun saveCurrentProgress() {
        val viewState = PLVMediaPlayerLocalProvider.localDependScope.on(parent).current()
            ?.get(PLVMPMediaViewModel::class.java)
            ?.mediaPlayViewState
            ?.value
        if (viewState == null) {
            return
        }
        this.position = viewState.currentProgress
        this.duration = viewState.duration
    }

    private fun handleScrolling(start: MotionEvent, current: MotionEvent) {
        if (position < 0 || duration < 0) {
            return
        }
        val dx = current.x - start.x
        val percent = dx / getScreenWidth().px()
        val dprogress = percent * 3.minutes().toMillis()
        val targetProgress = (position + dprogress).clamp(0f, duration.toFloat())
        PLVMediaPlayerLocalProvider.localDependScope.on(parent).current()
            ?.get(PLVMPMediaControllerViewModel::class.java)
            ?.handleDragSeekBar(DragSeekBarAction.DRAG, targetProgress.toLong())
    }
}

private class LongPressGestureHandler(
    private val parent: ViewGroup
) : GestureHandler(), GestureDetector.OnGestureListener {
    private val gestureDetector = GestureDetector(parent.context, this)
    private var isLongPressing = false

    override fun handle(event: MotionEvent): Boolean {
        val isLongPressingBeforeEvent = isLongPressing
        gestureDetector.onTouchEvent(event)
        if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL) {
            if (isLongPressing) {
                isLongPressing = false
                handleOnLongPress(false)
            }
        }
        return isLongPressingBeforeEvent
    }

    override fun onDown(e: MotionEvent): Boolean {
        return false
    }

    override fun onShowPress(e: MotionEvent) {
    }

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        return false
    }

    override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
        return false
    }

    override fun onLongPress(e: MotionEvent) {
        isLongPressing = true
        handleOnLongPress(true)
    }

    override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
        return false
    }

    private fun handleOnLongPress(isLongPressing: Boolean) {
        PLVMediaPlayerLocalProvider.localDependScope.on(parent).current()
            ?.get<PLVMPMediaControllerViewModel>()
            ?.handleLongPressSpeeding(if (isLongPressing) LongPressSpeedingAction.START else LongPressSpeedingAction.FINISH)
    }

}

private class MediaTranslationGestureHandler(
    private val parent: ViewGroup
) : GestureHandler() {
    private var isHandling = false
    private var lastPointX1: Float = 0F
    private var lastPointX2: Float = 0F
    private var lastPointY1: Float = 0F
    private var lastPointY2: Float = 0F

    @RequiresApi(29)
    override fun handle(event: MotionEvent): Boolean {
        if (Build.VERSION.SDK_INT < 29) return false
        val controllerViewModel = PLVMediaPlayerLocalProvider.localDependScope.on(parent).current()
            ?.get<PLVMPMediaControllerViewModel>()
        if (controllerViewModel == null) return false

        if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL) {
            val consume = isHandling
            if (isHandling) {
                controllerViewModel.onMediaViewTranslationGestureFinish()
            }
            reset()
            return consume
        }

        if (!isHandling) {
            if (event.pointerCount >= 2) {
                isHandling = true
                updateLastPoint(event)
                return true
            } else {
                return false
            }
        }

        if (event.pointerCount >= 2) {
            var translation = controllerViewModel.mediaControllerViewState.value?.mediaViewTranslation
                ?: PLVMPMediaViewTranslation()
            translation = translation.applyMove(event)
            translation = translation.applyScale(event)
            controllerViewModel.setMediaViewTranslation(translation)
        }
        updateLastPoint(event)
        return true
    }

    @RequiresApi(29)
    private fun PLVMPMediaViewTranslation.applyMove(event: MotionEvent): PLVMPMediaViewTranslation {
        val lastCenterX = (lastPointX1 + lastPointX2) / 2F
        val lastCenterY = (lastPointY1 + lastPointY2) / 2F
        val centerX = (event.getRawX(0) + event.getRawX(1)) / 2F
        val centerY = (event.getRawY(0) + event.getRawY(1)) / 2F
        return copy(
            offsetX = this.offsetX + (centerX - lastCenterX),
            offsetY = this.offsetY + (centerY - lastCenterY),
        )
    }

    @RequiresApi(29)
    private fun PLVMPMediaViewTranslation.applyScale(event: MotionEvent): PLVMPMediaViewTranslation {
        val lastPointerDistance = sqrt((lastPointX1 - lastPointX2).pow(2) + (lastPointY1 - lastPointY2).pow(2))
        val pointerDistance = sqrt(
            (event.getRawX(0) - event.getRawX(1)).pow(2) + (event.getRawY(0) - event.getRawY(1)).pow(
                2
            )
        )
        val factor = pointerDistance / lastPointerDistance
        return copy(
            scale = (this.scale * factor).coerceIn(0.25F, 4F)
        )
    }

    @RequiresApi(29)
    private fun updateLastPoint(event: MotionEvent) {
        if (event.pointerCount >= 2) {
            lastPointX1 = event.getRawX(0)
            lastPointX2 = event.getRawX(1)
            lastPointY1 = event.getRawY(0)
            lastPointY2 = event.getRawY(1)
        }
    }

    private fun reset() {
        isHandling = false
        lastPointX1 = 0F
        lastPointX2 = 0F
        lastPointY1 = 0F
        lastPointY2 = 0F
    }

}
