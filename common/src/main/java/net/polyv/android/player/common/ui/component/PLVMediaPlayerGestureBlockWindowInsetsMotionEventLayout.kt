package net.polyv.android.player.common.ui.component

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout
import net.polyv.android.player.sdk.PLVDeviceManager.getNavigationBarHeight
import net.polyv.android.player.sdk.PLVDeviceManager.getStatusBarHeight
import net.polyv.android.player.sdk.foundation.graphics.getScreenHeight
import net.polyv.android.player.sdk.foundation.graphics.getScreenWidth
import net.polyv.android.player.sdk.foundation.graphics.isPortrait

/**
 * @author Hoshiiro
 */
class PLVMediaPlayerGestureBlockWindowInsetsMotionEventLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var blockTouchEvent = false

    fun handleOnTouchEvent(event: MotionEvent): Boolean {
        var result = blockTouchEvent
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                blockTouchEvent = windowInsets.any { it.contains(event.rawX.toInt(), event.rawY.toInt()) }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                blockTouchEvent = false
            }
        }
        result = result or blockTouchEvent
        return result
    }

    private val windowInsets: List<Rect>
        get() {
            val statusBar = Rect(0, 0, getScreenWidth().px(), getStatusBarHeight().px())
            val navigationBar = if (isPortrait()) {
                Rect(
                    0,
                    getScreenHeight().px() - getNavigationBarHeight().px(),
                    getScreenWidth().px(),
                    getScreenHeight().px()
                )
            } else {
                Rect(
                    getScreenWidth().px() - getNavigationBarHeight().px(),
                    0,
                    getScreenWidth().px(),
                    getScreenHeight().px()
                )
            }
            return listOf(statusBar, navigationBar)
        }

}