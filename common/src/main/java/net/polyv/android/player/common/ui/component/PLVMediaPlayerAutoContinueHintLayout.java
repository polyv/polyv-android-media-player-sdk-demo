package net.polyv.android.player.common.ui.component;

import static net.polyv.android.player.sdk.foundation.graphics.ColorsKt.parseColor;
import static net.polyv.android.player.sdk.foundation.lang.Duration.seconds;
import static net.polyv.android.player.sdk.foundation.lang.PreconditionsKt.requireNotNull;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.TextView;

import net.polyv.android.player.business.scene.common.player.listener.event.PLVMediaPlayerAutoContinueEvent;
import net.polyv.android.player.common.R;
import net.polyv.android.player.common.di.PLVMediaPlayerLocalProvider;
import net.polyv.android.player.common.modules.media.viewmodel.PLVMPMediaViewModel;
import net.polyv.android.player.common.utils.data.PLVTimeUtils;
import net.polyv.android.player.common.utils.ui.PLVViewUtil;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

/**
 * @author Hoshiiro
 */
public class PLVMediaPlayerAutoContinueHintLayout extends FrameLayout {

    private TextView autoContinueHintTv;

    public PLVMediaPlayerAutoContinueHintLayout(@NonNull Context context) {
        super(context);
    }

    public PLVMediaPlayerAutoContinueHintLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PLVMediaPlayerAutoContinueHintLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    {
        LayoutInflater.from(getContext()).inflate(R.layout.plv_media_player_auto_continue_hint_layout, this);

        autoContinueHintTv = findViewById(R.id.plv_media_player_auto_continue_hint_tv);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (isInEditMode()) return;

        requireNotNull(PLVMediaPlayerLocalProvider.localDependScope.on(this).current())
                .get(PLVMPMediaViewModel.class)
                .getOnAutoContinueEvent()
                .observeUntilViewDetached(this, new Function1<PLVMediaPlayerAutoContinueEvent, Unit>() {
                    @Override
                    public Unit invoke(PLVMediaPlayerAutoContinueEvent event) {
                        showAutoContinueHint(event);
                        return null;
                    }
                });
    }

    protected String autoContinueHintPrefix() {
        return getContext().getString(R.string.plv_media_player_ui_component_auto_continue_hint_pre);
    }

    protected String autoContinueHintPostfix() {
        return getContext().getString(R.string.plv_media_player_ui_component_auto_continue_hint_post);
    }

    private void showAutoContinueHint(@NonNull PLVMediaPlayerAutoContinueEvent event) {
        final String timeText = PLVTimeUtils.formatTime(event.getStartPosition());
        final SpannableStringBuilder sb = new SpannableStringBuilder()
                .append(autoContinueHintPrefix())
                .append(timeText, new ForegroundColorSpan(parseColor("#FF8B00")), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                .append(autoContinueHintPostfix());

        autoContinueHintTv.setText(sb);
        PLVViewUtil.showViewForDuration(this, seconds(3).toMillis());
    }

}
