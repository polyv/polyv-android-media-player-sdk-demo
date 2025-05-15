package net.polyv.android.player.common.ui.component

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.FrameLayout
import net.polyv.android.player.common.di.PLVMediaPlayerLocalProvider
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.ChangeDirection
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.PLVMPMediaControllerViewModel
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.viewstate.PLVMPMediaControllerViewState
import net.polyv.android.player.sdk.foundation.graphics.getScreenHeight
import kotlin.math.abs

/**
 * @author Hoshiiro
 */
class PLVMediaPlayerGestureBrightnessVolumeControlLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle), GestureDetector.OnGestureListener {
    private val gestureDetector = GestureDetector(context, this)

    private var controlViewState: PLVMPMediaControllerViewState? = null

    private var isScrolling = false
    private var isScrollingVertical = false

    /**
     * true - 左侧滑动，false - 右侧滑动
     */
    private var isScrollOnLeftSide = true
    private var accumulateAdjustDiff = 0.0

    fun handleOnTouchEvent(event: MotionEvent): Boolean {
        if (!isAllowControl) {
            return false
        }
        val isScrollingVerticalBeforeEvent = isScrollingVertical
        gestureDetector.onTouchEvent(event)
        if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL) {
            isScrolling = false
            isScrollingVertical = false
            accumulateAdjustDiff = 0.0
        }
        return isScrollingVerticalBeforeEvent
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (isInEditMode) return

        PLVMediaPlayerLocalProvider.localDependScope.on(this).current()!!
            .get(PLVMPMediaControllerViewModel::class.java)
            .mediaControllerViewState
            .observeUntilViewDetached(this) { viewState ->
                controlViewState = viewState
            }
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
            isScrollOnLeftSide = e1.x < width.toFloat() / 2
        }
        if (isScrollingVertical) {
            handleOnScrolling(isScrollOnLeftSide, distanceY)
        }
        return isScrollingVertical
    }

    override fun onLongPress(e: MotionEvent) {
    }

    override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
        return false
    }

    private fun handleOnScrolling(left: Boolean, distanceY: Float) {
        val diff = accumulateAdjustDiff + (distanceY / (0.4 * getScreenHeight().px()) * 100)
        if (abs(diff) < 8) {
            accumulateAdjustDiff = diff
            return
        }

        val controllerViewModel = PLVMediaPlayerLocalProvider.localDependScope.on(this).current()!!
            .get(PLVMPMediaControllerViewModel::class.java)
        val direction = if (diff > 0) ChangeDirection.UP else ChangeDirection.DOWN
        if (left) {
            controllerViewModel.changeBrightness(direction, (context as Activity))
        } else {
            controllerViewModel.changeVolume(direction, context)
        }
        accumulateAdjustDiff = 0.0
    }

    private val isAllowControl: Boolean
        get() = controlViewState != null && !controlViewState!!.controllerLocking
}
