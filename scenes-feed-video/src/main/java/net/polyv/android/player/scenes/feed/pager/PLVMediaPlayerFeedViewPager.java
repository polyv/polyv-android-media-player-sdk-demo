package net.polyv.android.player.scenes.feed.pager;

import static net.polyv.android.player.sdk.foundation.graphics.DisplaysKt.isLandscape;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import net.polyv.android.player.common.utils.feed.PLVFeedViewPager;

/**
 * @author Hoshiiro
 */
public class PLVMediaPlayerFeedViewPager extends PLVFeedViewPager {

    private boolean isRequestDisallowInterceptTouchEvent = false;

    public PLVMediaPlayerFeedViewPager(Context context) {
        super(context);
    }

    public PLVMediaPlayerFeedViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        super.requestDisallowInterceptTouchEvent(disallowIntercept);
        this.isRequestDisallowInterceptTouchEvent = disallowIntercept;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (isLandscape() || isRequestDisallowInterceptTouchEvent) {
            return false;
        }
        return super.onInterceptTouchEvent(ev);
    }

}
