package net.polyv.android.player.scenes.feed;

import static net.polyv.android.player.sdk.foundation.graphics.DisplaysKt.dp;
import static net.polyv.android.player.sdk.foundation.graphics.DisplaysKt.isLandscape;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.Toast;

import net.polyv.android.player.business.scene.common.model.vo.PLVMediaResource;
import net.polyv.android.player.common.utils.data.PLVStatefulData;
import net.polyv.android.player.common.utils.feed.PLVFeedViewPager;
import net.polyv.android.player.common.utils.feed.PLVFeedViewPagerAdapter;
import net.polyv.android.player.common.utils.orientation.PLVActivityOrientationManager;
import net.polyv.android.player.scenes.feed.item.PLVMediaPlayerFeedVideoItemFragment;
import net.polyv.android.player.scenes.feed.pager.PLVMediaPlayerFeedViewPager;
import net.polyv.android.player.scenes.feed.viewmodel.IPLVMediaPlayerFeedVideoDataViewModel;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

/**
 * @author Hoshiiro
 */
public class PLVMediaPlayerFeedVideoLayout extends FrameLayout {

    // <editor-fold defaultstate="collapsed" desc="Layout-属性">

    private IPLVMediaPlayerFeedVideoDataViewModel feedViewModel;

    private PLVMediaPlayerFeedViewPager feedViewPager;

    private final PLVFeedViewPagerAdapter<PLVMediaPlayerFeedVideoItemFragment, PLVMediaResource> feedViewPagerAdapter = new PLVFeedViewPagerAdapter<>(((FragmentActivity) getContext()).getSupportFragmentManager());

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
        initViewPager();  // 设置 ViewPager 回调
    }

    private void initLayout() {
        setKeepScreenOn(true);
        LayoutInflater.from(getContext()).inflate(R.layout.plv_media_player_feed_video_layout, this);
        feedViewPager = findViewById(R.id.plv_media_player_feed_video_view_pager);
    }

    // 设置 feedViewModel 从HTTP接口获取到数据后的处理逻辑
    private void initViewModel() {
        feedViewModel.getOnReceiveMediaResourceEvent().observe(new Function1<PLVStatefulData<List<PLVMediaResource>>, Unit>() {
            @Override
            public Unit invoke(PLVStatefulData<List<PLVMediaResource>> result) {
                result
                        .ifSuccess(new PLVStatefulData.SuccessHandler<List<PLVMediaResource>>() {
                            @Override
                            public void success(List<PLVMediaResource> data) {
                                if (isRequestingRefreshMediaResource) {
                                    feedViewPagerAdapter.setFeedResources(data);
                                } else {
                                    feedViewPagerAdapter.appendFeedResources(data);
                                }
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
                return null;
            }
        });
    }

    // 设置 ViewPager 回调
    private void initViewPager() {
        feedViewPagerAdapter
                .setOnCreateFeedViewListener(new PLVFeedViewPagerAdapter.OnCreateFeedViewListener<PLVMediaPlayerFeedVideoItemFragment>() {
                    @Override
                    public PLVMediaPlayerFeedVideoItemFragment onCreateFeedView(int position) {
                        return new PLVMediaPlayerFeedVideoItemFragment();
                    }
                })
                .setOnBindFeedViewListener(new PLVFeedViewPagerAdapter.OnBindFeedViewListener<PLVMediaPlayerFeedVideoItemFragment, PLVMediaResource>() {
                    @Override
                    public void onBindFeedView(@NonNull @NotNull PLVMediaPlayerFeedVideoItemFragment feedView, PLVMediaResource resource) {
                        feedView.setMediaResource(resource);
                    }
                });

        feedViewPager
                .setOnOverScrollListener(new PLVFeedViewPager.OnOverScrollListener() {
                    @Override
                    public void onOverScroll(float topOverScroll, float bottomOverScroll) {
                        updateViewPagerPosition(topOverScroll, bottomOverScroll);
                    }

                    @Override
                    public void onFinishOverScroll(float topOverScroll, float bottomOverScroll) {
                        updateViewPagerPosition(0, 0);
                    }

                    private void updateViewPagerPosition(float topOverScroll, float bottomOverScroll) {
                        MarginLayoutParams lp = (MarginLayoutParams) feedViewPager.getLayoutParams();
                        lp.topMargin = Math.min((int) (-topOverScroll * 0.75F), dp(100).px());
                        lp.bottomMargin = Math.min((int) (bottomOverScroll * 0.75F), dp(100).px());
                        feedViewPager.setLayoutParams(lp);
                    }
                })
                .setOnLoadFeedResourceListener(new PLVFeedViewPager.OnLoadFeedResourceListener() {
                    @Override
                    public void onRequestLoadFeedResource(int fromIndex) {
                        if (isRequestingMediaResource) {
                            return;
                        }
                        feedViewModel.requireMoreMediaResource(fromIndex);
                        isRequestingMediaResource = true;
                        isRequestingRefreshMediaResource = fromIndex == 0;
                    }
                });

        feedViewPager.setAdapter(feedViewPagerAdapter);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Layout-从外部设置视频信息资源">
    public void setTargetMediaResource(List<PLVMediaResource> mediaResources) {
        feedViewPagerAdapter.setFeedResources(mediaResources);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Layout-返回">
    public boolean onBackPressed() {
        if (isLandscape()) {
            PLVActivityOrientationManager.on((AppCompatActivity) getContext())
                    .requestOrientation(true)
                    .setLockOrientation(false);
            return true;
        }
        return false;
    }
    // </editor-fold>

}
