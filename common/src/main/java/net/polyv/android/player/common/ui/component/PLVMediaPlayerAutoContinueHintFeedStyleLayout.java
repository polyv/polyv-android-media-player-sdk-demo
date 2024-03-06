package net.polyv.android.player.common.ui.component;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import net.polyv.android.player.common.R;

import org.jetbrains.annotations.NotNull;

/**
 * @author Hoshiiro
 */
public class PLVMediaPlayerAutoContinueHintFeedStyleLayout extends PLVMediaPlayerAutoContinueHintLayout {

    public PLVMediaPlayerAutoContinueHintFeedStyleLayout(@NonNull @NotNull Context context) {
        super(context);
    }

    public PLVMediaPlayerAutoContinueHintFeedStyleLayout(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PLVMediaPlayerAutoContinueHintFeedStyleLayout(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected String autoContinueHintPrefix() {
        return getContext().getString(R.string.plv_media_player_ui_component_auto_continue_hint_feed_pre);
    }

    @Override
    protected String autoContinueHintPostfix() {
        return getContext().getString(R.string.plv_media_player_ui_component_auto_continue_hint_feed_post);
    }

}
