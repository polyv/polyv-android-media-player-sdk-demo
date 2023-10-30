package net.polyv.android.player.demo.scene.feed.pager;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;

import net.polyv.android.player.demo.utils.VerticalViewPager;

/**
 * @author Hoshiiro
 */
public class PLVMediaPlayerFeedViewPager extends VerticalViewPager {

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
        if (ScreenUtils.isLandscape() || isRequestDisallowInterceptTouchEvent) {
            return false;
        }
        return super.onInterceptTouchEvent(ev);
    }

}
