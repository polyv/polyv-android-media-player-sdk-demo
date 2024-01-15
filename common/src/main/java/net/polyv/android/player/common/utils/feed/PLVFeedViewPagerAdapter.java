package net.polyv.android.player.common.utils.feed;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.plv.foundationsdk.component.exts.Nullables;
import com.plv.foundationsdk.utils.PLVSugarUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Hoshiiro
 */
public class PLVFeedViewPagerAdapter<T extends Fragment, R> extends FragmentStatePagerAdapter {

    private final List<R> feedResources = new ArrayList<>();

    /**
     * 缓存Fragment，防止重复创建
     * <p>
     * key: position, value: weak fragment
     */
    private final Map<Integer, WeakReference<T>> weakItemMap = new HashMap<>();

    @Nullable
    private OnCreateFeedViewListener<T> onCreateFeedViewListener = null;
    @Nullable
    private OnBindFeedViewListener<T, R> onBindFeedViewListener = null;

    public PLVFeedViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public T getItem(final int position) {
        T item = Nullables.of(new PLVSugarUtil.Supplier<T>() {
            @Override
            public T get() {
                return weakItemMap.get(position).get();
            }
        }).getOrNull();

        if (item == null && onCreateFeedViewListener != null) {
            item = onCreateFeedViewListener.onCreateFeedView(position);
            weakItemMap.put(position, new WeakReference<>(item));
        }

        if (item != null && feedResources.size() > position && onBindFeedViewListener != null) {
            onBindFeedViewListener.onBindFeedView(item, feedResources.get(position));
        }

        return item;
    }

    @Override
    public int getCount() {
        return feedResources.size();
    }

    public void appendFeedResources(@Nullable List<R> feedResources) {
        if (feedResources == null) {
            return;
        }
        final int fromIndex = this.feedResources.size();

        this.feedResources.addAll(feedResources);
        notifyDataSetChanged();

        bindNewResources(fromIndex, feedResources);
    }

    public void setFeedResources(@Nullable List<R> feedResources) {
        clearFeedResources();
        appendFeedResources(feedResources);
    }

    public void clearFeedResources() {
        this.feedResources.clear();
        notifyDataSetChanged();
    }

    public PLVFeedViewPagerAdapter<T, R> setOnCreateFeedViewListener(@Nullable OnCreateFeedViewListener<T> onCreateFeedViewListener) {
        this.onCreateFeedViewListener = onCreateFeedViewListener;
        return this;
    }

    public PLVFeedViewPagerAdapter<T, R> setOnBindFeedViewListener(@Nullable OnBindFeedViewListener<T, R> onBindFeedViewListener) {
        this.onBindFeedViewListener = onBindFeedViewListener;
        return this;
    }

    private void bindNewResources(int fromIndex, List<R> newResources) {
        for (int i = 0; i < newResources.size(); i++) {
            final int index = fromIndex + i;
            T item = Nullables.of(new PLVSugarUtil.Supplier<T>() {
                @Override
                public T get() {
                    return weakItemMap.get(index).get();
                }
            }).getOrNull();
            if (item != null && onBindFeedViewListener != null) {
                onBindFeedViewListener.onBindFeedView(item, newResources.get(i));
            }
        }
    }

    public interface OnCreateFeedViewListener<T> {
        T onCreateFeedView(int position);
    }

    public interface OnBindFeedViewListener<T, R> {
        void onBindFeedView(@NonNull T feedView, R resource);
    }

}
