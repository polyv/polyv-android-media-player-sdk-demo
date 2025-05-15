package net.polyv.android.player.scenes.feed.pager

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import net.polyv.android.player.common.utils.feed.PLVFeedViewPager
import net.polyv.android.player.sdk.foundation.graphics.isLandscape

/**
 * @author Hoshiiro
 */
class PLVMediaPlayerFeedViewPager @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null
) : PLVFeedViewPager(context, attrs) {

    private var isRequestDisallowInterceptTouchEvent = false

    override fun requestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
        super.requestDisallowInterceptTouchEvent(disallowIntercept)
        this.isRequestDisallowInterceptTouchEvent = disallowIntercept
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (isLandscape() || isRequestDisallowInterceptTouchEvent) {
            return false
        }
        return super.onInterceptTouchEvent(ev)
    }

}
