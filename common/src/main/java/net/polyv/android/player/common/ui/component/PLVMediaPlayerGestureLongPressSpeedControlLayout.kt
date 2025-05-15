package net.polyv.android.player.common.ui.component

import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.FrameLayout
import net.polyv.android.player.common.di.PLVMediaPlayerLocalProvider
import net.polyv.android.player.common.modules.media.viewmodel.PLVMPMediaViewModel
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.LongPressSpeedingAction
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.PLVMPMediaControllerViewModel
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.viewstate.PLVMPMediaControllerViewState

/**
 * @author Hoshiiro
 */
class PLVMediaPlayerGestureLongPressSpeedControlLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle), GestureDetector.OnGestureListener {
    private val gestureDetector = GestureDetector(context, this)

    private var controllerViewState: PLVMPMediaControllerViewState? = null
    private var isPlaying: Boolean = false

    private var isLongPressing = false

    fun handleOnTouchEvent(event: MotionEvent): Boolean {
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

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (isInEditMode) return

        PLVMediaPlayerLocalProvider.localDependScope.on(this).current()!!.get<PLVMPMediaViewModel>()
            .mediaPlayViewState
            .observeUntilViewDetached(this) { viewState ->
                isPlaying = viewState.isPlaying
            }

        PLVMediaPlayerLocalProvider.localDependScope.on(this).current()!!.get<PLVMPMediaControllerViewModel>()
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
        PLVMediaPlayerLocalProvider.localDependScope.on(this).current()!!.get<PLVMPMediaControllerViewModel>()
            .handleLongPressSpeeding(if (isLongPressing) LongPressSpeedingAction.START else LongPressSpeedingAction.FINISH)
    }

}
