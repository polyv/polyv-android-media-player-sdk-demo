package net.polyv.android.player.common.ui.component

import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.FrameLayout
import net.polyv.android.player.common.di.PLVMediaPlayerLocalProvider
import net.polyv.android.player.common.modules.media.viewmodel.PLVMPMediaViewModel
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.DragSeekBarAction
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.PLVMPMediaControllerViewModel
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.viewstate.PLVMPMediaControllerViewState
import net.polyv.android.player.sdk.foundation.graphics.getScreenWidth
import net.polyv.android.player.sdk.foundation.lang.Duration.Companion.minutes
import net.polyv.android.player.sdk.foundation.lang.clamp
import net.polyv.android.player.sdk.foundation.lang.requireNotNull
import kotlin.math.abs

/**
 * @author Hoshiiro
 */
class PLVMediaPlayerGestureHorizontalDragControlLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle), GestureDetector.OnGestureListener {
    private val gestureDetector = GestureDetector(context, this)

    private var controllerViewState: PLVMPMediaControllerViewState? = null

    private var isScrolling = false
    private var isScrollingHorizontal = false

    private var position: Long = -1
    private var duration: Long = -1

    fun handleOnTouchEvent(event: MotionEvent): Boolean {
        if (!isAllowControl) {
            return false
        }
        val consume = isScrollingHorizontal
        gestureDetector.onTouchEvent(event)
        if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL) {
            onActionUp()
        }
        return consume
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (isInEditMode) return

        PLVMediaPlayerLocalProvider.localDependScope.on(this).current()!!
            .get(PLVMPMediaControllerViewModel::class.java)
            .mediaControllerViewState
            .observeUntilViewDetached(this) { viewState ->
                controllerViewState = viewState
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
            PLVMediaPlayerLocalProvider.localDependScope.on(this).current()!!
                .get(PLVMPMediaControllerViewModel::class.java)
                .handleDragSeekBar(DragSeekBarAction.FINISH)
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
        val viewState = PLVMediaPlayerLocalProvider.localDependScope.on(this).current()!!
            .get(PLVMPMediaViewModel::class.java)
            .mediaPlayViewState
            .value
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
        requireNotNull(PLVMediaPlayerLocalProvider.localDependScope.on(this).current())
            .get(PLVMPMediaControllerViewModel::class.java)
            .handleDragSeekBar(DragSeekBarAction.DRAG, targetProgress.toLong())
    }

    private val isAllowControl: Boolean
        get() = controllerViewState != null && !controllerViewState!!.controllerLocking
}
