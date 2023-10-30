package net.polyv.android.player.demo.scene.feed.pager;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.plv.foundationsdk.component.exts.Nullables;
import com.plv.foundationsdk.utils.PLVSugarUtil;

import net.polyv.android.player.business.scene.common.model.vo.PLVMediaResource;
import net.polyv.android.player.demo.scene.feed.item.PLVMediaPlayerFeedVideoItemFragment;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Hoshiiro
 */
public class PLVMediaPlayerFeedViewPagerAdapter extends FragmentStatePagerAdapter {

    // list 存储 所有的视频数据
    private final List<PLVMediaResource> mediaResourceList = new ArrayList<>();

    // map 存储 index 对应的 fragment
    private final Map<Integer, WeakReference<PLVMediaPlayerFeedVideoItemFragment>> fragmentRefMap = new HashMap<>();

    public PLVMediaPlayerFeedViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(final int position) {
        // 尝试复用的旧的fragment
        PLVMediaPlayerFeedVideoItemFragment fragment = Nullables.of(new PLVSugarUtil.Supplier<PLVMediaPlayerFeedVideoItemFragment>() {
            @Override
            public PLVMediaPlayerFeedVideoItemFragment get() {
                return fragmentRefMap.get(position).get();
            }
        }).getOrNull();

        // 没有可复用的旧的fragment，就需要新创建
        if (fragment == null) {
            fragment = new PLVMediaPlayerFeedVideoItemFragment();
            fragmentRefMap.put(position, new WeakReference<>(fragment));
        }

        // 更新fragment对应的视频数据
        if (mediaResourceList.size() > position) {
            fragment.setMediaResource(mediaResourceList.get(position));
        }

        // 返回fragment
        return fragment;
    }

    @Override
    public int getCount() {
        return mediaResourceList.size();
    }

    // 追加数据
    public void acceptMediaResource(List<PLVMediaResource> mediaResources) {
        if (mediaResources == null) {
            return;
        }
        int fromIndex = mediaResourceList.size();
        mediaResourceList.addAll(mediaResources);
        notifyDataSetChanged();

        for (int i = fromIndex; i < mediaResourceList.size(); i++) {
            final int index = i;
            PLVMediaPlayerFeedVideoItemFragment fragment = Nullables.of(new PLVSugarUtil.Supplier<PLVMediaPlayerFeedVideoItemFragment>() {
                @Override
                public PLVMediaPlayerFeedVideoItemFragment get() {
                    return fragmentRefMap.get(index).get();
                }
            }).getOrNull();
            if (fragment != null) {
                fragment.setMediaResource(mediaResourceList.get(index));
            }
        }
    }

    // 清除所有数据
    public void clearMediaResource() {
        mediaResourceList.clear();
        notifyDataSetChanged();
    }

}
