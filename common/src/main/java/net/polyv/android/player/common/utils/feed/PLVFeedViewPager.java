package net.polyv.android.player.common.utils.feed;

import static net.polyv.android.player.sdk.foundation.graphics.DisplaysKt.getScreenHeight;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;

import net.polyv.android.player.common.utils.ui.viewpager.VerticalViewPager;

/**
 * @author Hoshiiro
 */
public class PLVFeedViewPager extends VerticalViewPager {

    @Nullable
    private OnOverScrollListener onOverScrollListener = null;
    @Nullable
    private OnLoadFeedResourceListener onLoadFeedResourceListener = null;

    private float accumulateTopOverScroll = 0;
    private float accumulateBottomOverScroll = 0;

    public PLVFeedViewPager(Context context) {
        super(context);
        init();
    }

    public PLVFeedViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setOverScrollMode(OVER_SCROLL_NEVER);
        setOnHandleScrollListener(onHandleScrollListener);
        setOnPageChangeListener(onPageChangeListener);
    }

    public PLVFeedViewPager setOnOverScrollListener(@Nullable OnOverScrollListener onOverScrollListener) {
        this.onOverScrollListener = onOverScrollListener;
        return this;
    }

    public PLVFeedViewPager setOnLoadFeedResourceListener(@Nullable OnLoadFeedResourceListener onLoadFeedResourceListener) {
        this.onLoadFeedResourceListener = onLoadFeedResourceListener;
        return this;
    }

    private final OnHandleScrollListener onHandleScrollListener = new OnHandleScrollListener() {
        @Override
        public float onScroll(float oldScrollY, float scrollY, float deltaY, float topBound, float bottomBound) {
            if (oldScrollY + accumulateBottomOverScroll + deltaY >= bottomBound) {
                scrollY = bottomBound;
                accumulateBottomOverScroll += deltaY - (bottomBound - oldScrollY);
            } else if (oldScrollY + accumulateTopOverScroll + deltaY >= topBound) {
                scrollY = oldScrollY + accumulateTopOverScroll + deltaY;
                accumulateTopOverScroll = 0;
                accumulateBottomOverScroll = 0;
            } else {
                scrollY = topBound;
                accumulateTopOverScroll += deltaY - (oldScrollY - topBound);
            }

            if (onOverScrollListener != null) {
                onOverScrollListener.onOverScroll(accumulateTopOverScroll, accumulateBottomOverScroll);
            }

            return scrollY;
        }

        @Override
        public void onFinishScroll() {
            if (shouldRefreshFeedResource(accumulateTopOverScroll)) {
                if (onLoadFeedResourceListener != null) {
                    onLoadFeedResourceListener.onRequestLoadFeedResource(0);
                }
            }
            if (shouldLoadMoreFeedResource(accumulateBottomOverScroll, getCurrentItem(), getAdapter().getCount())) {
                if (onLoadFeedResourceListener != null) {
                    onLoadFeedResourceListener.onRequestLoadFeedResource(getAdapter().getCount());
                }
            }

            if (onOverScrollListener != null) {
                onOverScrollListener.onFinishOverScroll(accumulateTopOverScroll, accumulateBottomOverScroll);
            }
            accumulateTopOverScroll = 0;
            accumulateBottomOverScroll = 0;
        }
    };

    private final ViewPager.OnPageChangeListener onPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            int count = getAdapter().getCount();
            if (shouldLoadMoreFeedResource(0, position, count)) {
                if (onLoadFeedResourceListener != null) {
                    onLoadFeedResourceListener.onRequestLoadFeedResource(count);
                }
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    protected boolean shouldRefreshFeedResource(float topOverScroll) {
        return topOverScroll < -getScreenHeight().px() * 0.2F;
    }

    protected boolean shouldLoadMoreFeedResource(float bottomOverScroll, int currentPage, int count) {
        return bottomOverScroll > 0 || currentPage > count - 5;
    }

    public interface OnOverScrollListener {

        void onOverScroll(float topOverScroll, float bottomOverScroll);

        void onFinishOverScroll(float topOverScroll, float bottomOverScroll);

    }

    public interface OnLoadFeedResourceListener {

        /**
         * 请求加载 Feed 资源
         *
         * @param fromIndex 起始索引，刷新列表时为 0，加载更多时为 adapter.count
         */
        void onRequestLoadFeedResource(int fromIndex);

    }

}
