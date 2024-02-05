package net.polyv.android.player.common.ui.component;

import static com.plv.foundationsdk.component.event.PLVEventKt.observeUntilViewDetached;
import static com.plv.foundationsdk.utils.PLVSugarUtil.format;
import static com.plv.foundationsdk.utils.PLVSugarUtil.requireNotNull;

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

import com.plv.foundationsdk.utils.PLVFormatUtils;
import com.plv.foundationsdk.utils.PLVSugarUtil;

import net.polyv.android.player.business.scene.common.model.vo.PLVMediaBitRate;
import net.polyv.android.player.common.R;
import net.polyv.android.player.common.ui.localprovider.PLVMediaPlayerLocalProvider;
import net.polyv.android.player.common.ui.viewmodel.action.PLVMediaPlayerControlAction;
import net.polyv.android.player.common.utils.ui.PLVViewUtil;
import net.polyv.android.player.core.api.listener.event.PLVMediaPlayerOnInfoEvent;

/**
 * @author Hoshiiro
 */
public class PLVMediaPlayerSwitchBitRateHintLayout extends FrameLayout {

    private TextView switchBitRateHintTv;

    protected PLVMediaBitRate currentBitRate = null;

    public PLVMediaPlayerSwitchBitRateHintLayout(@NonNull Context context) {
        super(context);
    }

    public PLVMediaPlayerSwitchBitRateHintLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PLVMediaPlayerSwitchBitRateHintLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    {
        LayoutInflater.from(getContext()).inflate(R.layout.plv_media_player_ui_component_switch_bit_rate_hint_layout, this);

        switchBitRateHintTv = findViewById(R.id.plv_media_player_switch_bit_rate_hint_tv);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (isInEditMode()) return;

        observeUntilViewDetached(
                requireNotNull(PLVMediaPlayerLocalProvider.localControlViewModel.on(this).current())
                        .getControlActionEvent(),
                this,
                new PLVSugarUtil.Consumer<PLVMediaPlayerControlAction>() {
                    @Override
                    public void accept(PLVMediaPlayerControlAction action) {
                        if (action instanceof PLVMediaPlayerControlAction.HintBitRateChanged) {
                            currentBitRate = ((PLVMediaPlayerControlAction.HintBitRateChanged) action).bitRate;
                            showSwitchBitRateStarted();
                        }
                    }
                }
        );

        observeUntilViewDetached(
                requireNotNull(PLVMediaPlayerLocalProvider.localMediaPlayer.on(this).current())
                        .getEventListenerRegistry().getOnInfo(),
                this,
                new PLVSugarUtil.Consumer<PLVMediaPlayerOnInfoEvent>() {
                    @Override
                    public void accept(PLVMediaPlayerOnInfoEvent onInfoEvent) {
                        if (onInfoEvent != null && onInfoEvent.getWhat() == PLVMediaPlayerOnInfoEvent.MEDIA_INFO_VIDEO_RENDERING_START) {
                            showSwitchBitRateFinished();
                        }
                    }
                }
        );
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        currentBitRate = null;
    }

    private void showSwitchBitRateStarted() {
        if (currentBitRate == null) {
            return;
        }

        final SpannableStringBuilder sb = new SpannableStringBuilder();
        sb.append(getContext().getString(R.string.plv_media_player_ui_component_switch_bit_rate_start_text_pre));
        sb.append(
                format(" {} ", currentBitRate.getName()),
                new ForegroundColorSpan(PLVFormatUtils.parseColor("#3F76FC")),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );
        sb.append(getContext().getString(R.string.plv_media_player_ui_component_switch_bit_rate_start_text_post));
        switchBitRateHintTv.setText(sb);

        PLVViewUtil.showViewForDuration(this, 3000);
    }

    private void showSwitchBitRateFinished() {
        if (currentBitRate == null) {
            return;
        }

        final SpannableStringBuilder sb = new SpannableStringBuilder();
        sb.append(getContext().getString(R.string.plv_media_player_ui_component_switch_bit_rate_finish_text_pre));
        sb.append(
                format(" {} ", currentBitRate.getName()),
                new ForegroundColorSpan(PLVFormatUtils.parseColor("#3F76FC")),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );
        sb.append(getContext().getString(R.string.plv_media_player_ui_component_switch_bit_rate_finish_text_post));
        switchBitRateHintTv.setText(sb);

        PLVViewUtil.showViewForDuration(this, 3000);
    }

}
