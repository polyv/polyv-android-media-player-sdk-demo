package net.polyv.android.player.common.ui.component;

import static com.plv.foundationsdk.component.event.PLVEventKt.observeUntilViewDetached;
import static com.plv.foundationsdk.utils.PLVSugarUtil.requireNotNull;
import static com.plv.foundationsdk.utils.PLVTimeUnit.seconds;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.plv.foundationsdk.utils.PLVFormatUtils;
import com.plv.foundationsdk.utils.PLVSugarUtil;
import com.plv.foundationsdk.utils.PLVTimeUtils;

import net.polyv.android.player.business.scene.common.player.listener.event.PLVMediaPlayerAutoContinueEvent;
import net.polyv.android.player.common.R;
import net.polyv.android.player.common.ui.localprovider.PLVMediaPlayerLocalProvider;
import net.polyv.android.player.common.utils.ui.PLVViewUtil;

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

        observeUntilViewDetached(
                requireNotNull(PLVMediaPlayerLocalProvider.localMediaPlayer.on(this).current())
                        .getBusinessListenerRegistry()
                        .getOnAutoContinueEvent(),
                this,
                new PLVSugarUtil.Consumer<PLVMediaPlayerAutoContinueEvent>() {
                    @Override
                    public void accept(PLVMediaPlayerAutoContinueEvent event) {
                        if (event != null) {
                            showAutoContinueHint(event);
                        }
                    }
                }
        );
    }

    protected String autoContinueHintPrefix() {
        return getContext().getString(R.string.plv_media_player_ui_component_auto_continue_hint_pre);
    }

    protected String autoContinueHintPostfix() {
        return getContext().getString(R.string.plv_media_player_ui_component_auto_continue_hint_post);
    }

    private void showAutoContinueHint(@NonNull PLVMediaPlayerAutoContinueEvent event) {
        final String timeText = PLVTimeUtils.generateTime(event.getStartPosition().toMillis());
        final SpannableStringBuilder sb = new SpannableStringBuilder()
                .append(autoContinueHintPrefix())
                .append(timeText, new ForegroundColorSpan(PLVFormatUtils.parseColor("#FF8B00")), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                .append(autoContinueHintPostfix());

        autoContinueHintTv.setText(sb);
        PLVViewUtil.showViewForDuration(this, seconds(3).toMillis());
    }

}
