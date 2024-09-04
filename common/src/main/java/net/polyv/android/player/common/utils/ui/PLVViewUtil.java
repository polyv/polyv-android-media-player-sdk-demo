package net.polyv.android.player.common.utils.ui;


import static net.polyv.android.player.sdk.foundation.lang.Duration.millis;
import static net.polyv.android.player.sdk.foundation.lang.ThreadsKt.postToMainThread;

import android.view.View;

import net.polyv.android.player.common.R;

import java.lang.ref.WeakReference;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;

/**
 * @author Hoshiiro
 */
public class PLVViewUtil {

    private static final int TAG_VIEW_UTIL_SHOW_DURATION = R.id.plv_view_util_show_view_for_duration_tag;

    public static void showViewForDuration(final View view, final long durationInMillis) {
        if (view == null) {
            return;
        }
        view.setVisibility(View.VISIBLE);
        view.setTag(TAG_VIEW_UTIL_SHOW_DURATION, System.currentTimeMillis() + durationInMillis - 100);
        final WeakReference<View> ref = new WeakReference<>(view);
        postToMainThread(millis(durationInMillis), new Function0<Unit>() {
            @Override
            public Unit invoke() {
                final View view = ref.get();
                if (view == null) {
                    return null;
                }
                final Object timestamp = view.getTag(TAG_VIEW_UTIL_SHOW_DURATION);
                final boolean shouldHide = !(timestamp instanceof Long) || ((Long) timestamp) <= System.currentTimeMillis();
                if (shouldHide) {
                    view.setVisibility(View.GONE);
                }
                return null;
            }
        });
    }

}
