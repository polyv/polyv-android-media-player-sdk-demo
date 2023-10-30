package net.polyv.android.player.demo.scene.feed;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.plv.foundationsdk.utils.PLVSugarUtil;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;

import net.polyv.android.player.business.scene.common.model.vo.PLVMediaResource;
import net.polyv.android.player.common.utils.data.PLVStatefulData;
import net.polyv.android.player.common.utils.orientation.PLVActivityOrientationManager;
import net.polyv.android.player.demo.R;
import net.polyv.android.player.demo.scene.feed.pager.PLVMediaPlayerFeedViewPager;
import net.polyv.android.player.demo.scene.feed.pager.PLVMediaPlayerFeedViewPagerAdapter;
import net.polyv.android.player.demo.scene.feed.viewmodel.IPLVMediaPlayerFeedVideoDataViewModel;
import net.polyv.android.player.demo.utils.VerticalViewPager;

import java.util.List;

/**
 * @author Hoshiiro
 */
public class PLVMediaPlayerFeedVideoLayout extends FrameLayout {

    // <editor-fold defaultstate="collapsed" desc="Layout-属性">

    private IPLVMediaPlayerFeedVideoDataViewModel feedViewModel;

    private PLVMediaPlayerFeedViewPager feedViewPager;

    private final PLVMediaPlayerFeedViewPagerAdapter feedViewPagerAdapter
            = new PLVMediaPlayerFeedViewPagerAdapter(((FragmentActivity) getContext()).getSupportFragmentManager());

    private boolean isRequestingMediaResource = false;
    private boolean isRequestingRefreshMediaResource = false;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Layout-构造方法">
    public PLVMediaPlayerFeedVideoLayout(@NonNull Context context) {
        super(context);
    }

    public PLVMediaPlayerFeedVideoLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PLVMediaPlayerFeedVideoLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Layout-初始化">
    public void init(IPLVMediaPlayerFeedVideoDataViewModel feedViewModel) {
        this.feedViewModel = feedViewModel;

        initLayout();  // 初始化layout布局
        initViewModel();  // 设置 feedViewModel 从HTTP接口获取到数据后的处理逻辑
        initViewPager();  // 设置 ViewPager 的手势操作逻辑，包括 滑动、翻页 等
    }

    private void initLayout() {
        setKeepScreenOn(true);
        LayoutInflater.from(getContext()).inflate(R.layout.plv_media_player_feed_video_layout, this);
        feedViewPager = findViewById(R.id.plv_media_player_feed_video_view_pager);
    }

    // 设置 feedViewModel 从HTTP接口获取到数据后的处理逻辑
    private void initViewModel() {
        feedViewModel.getOnReceiveMediaResourceEvent().observe(new PLVSugarUtil.Consumer<PLVStatefulData<List<PLVMediaResource>>>() {
            @Override
            public void accept(PLVStatefulData<List<PLVMediaResource>> result) {
                result
                        .ifSuccess(new PLVStatefulData.SuccessHandler<List<PLVMediaResource>>() {
                            @Override
                            public void success(List<PLVMediaResource> data) {
                                if (isRequestingRefreshMediaResource) {
                                    feedViewPagerAdapter.clearMediaResource();
                                }
                                feedViewPagerAdapter.acceptMediaResource(data);
                            }
                        })
                        .ifError(new PLVStatefulData.ErrorHandler() {
                            @Override
                            public void error(String errorMsg, Throwable throwable) {
                                Toast.makeText(getContext(), R.string.plv_media_player_load_new_media_resource_fail, Toast.LENGTH_SHORT).show();
                            }
                        });
                isRequestingMediaResource = false;
                isRequestingRefreshMediaResource = false;
            }
        });
    }

    // 设置 ViewPager 的手势操作逻辑，包括 滑动、翻页 等
    private void initViewPager() {
        feedViewPager.setOverScrollMode(View.OVER_SCROLL_NEVER);
        feedViewPager.setAdapter(feedViewPagerAdapter);
        feedViewPager.setOnHandleScrollListener(new VerticalViewPager.OnHandleScrollListener() {
            private float accumulateTopOverScroll = 0;
            private float accumulateBottomOverScroll = 0;

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
                updateViewPagerPosition();
                return scrollY;
            }

            @Override
            public void onFinishScroll() {
                boolean isHandleTopOverScroll = accumulateTopOverScroll < -ScreenUtils.getScreenOrientatedHeight() * 0.2F;
                if (isHandleTopOverScroll && !isRequestingMediaResource) {
                    feedViewModel.requireMoreMediaResource(0);
                    isRequestingMediaResource = true;
                    isRequestingRefreshMediaResource = true;
                }
                if (accumulateBottomOverScroll > 0 && !isRequestingMediaResource) {
                    feedViewModel.requireMoreMediaResource(feedViewPagerAdapter.getCount());
                    isRequestingMediaResource = true;
                }
                accumulateTopOverScroll = 0;
                accumulateBottomOverScroll = 0;
                updateViewPagerPosition();
            }

            private void updateViewPagerPosition() {
                MarginLayoutParams lp = (MarginLayoutParams) feedViewPager.getLayoutParams();
                lp.topMargin = Math.min((int) (-accumulateTopOverScroll * 0.75F), ConvertUtils.dp2px(100));
                lp.bottomMargin = Math.min((int) (accumulateBottomOverScroll * 0.75F), ConvertUtils.dp2px(100));
                feedViewPager.setLayoutParams(lp);
            }
        });
        feedViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if (position > feedViewPagerAdapter.getCount() - 5 && !isRequestingMediaResource) {
                    feedViewModel.requireMoreMediaResource(feedViewPagerAdapter.getCount());
                    isRequestingMediaResource = true;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Layout-从外部设置视频信息资源">
    public void setTargetMediaResource(List<PLVMediaResource> mediaResources) {
        feedViewPagerAdapter.acceptMediaResource(mediaResources);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Layout-返回">
    public boolean onBackPressed() {
        if (ScreenUtils.isLandscape()) {
            PLVActivityOrientationManager.on((AppCompatActivity) getContext())
                    .requestOrientation(true)
                    .setLockOrientation(false);
            return true;
        }
        return false;
    }
    // </editor-fold>

}
