package net.polyv.android.player.common.ui.component;

import static net.polyv.android.player.sdk.foundation.graphics.ColorsKt.parseColor;
import static net.polyv.android.player.sdk.foundation.lang.PreconditionsKt.requireNotNull;
import static net.polyv.android.player.sdk.foundation.lang.StringsKt.format;

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

import net.polyv.android.player.business.scene.common.model.vo.PLVMediaBitRate;
import net.polyv.android.player.common.R;
import net.polyv.android.player.common.di.PLVMediaPlayerLocalProvider;
import net.polyv.android.player.common.modules.media.viewmodel.PLVMPMediaViewModel;
import net.polyv.android.player.common.utils.ui.PLVViewUtil;
import net.polyv.android.player.core.api.listener.event.PLVMediaPlayerOnInfoEvent;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

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

        requireNotNull(PLVMediaPlayerLocalProvider.localDependScope.on(this).current())
                .get(PLVMPMediaViewModel.class)
                .getOnChangeBitRateEvent()
                .observeUntilViewDetached(this, new Function1<PLVMediaBitRate, Unit>() {
                    @Override
                    public Unit invoke(PLVMediaBitRate mediaBitRate) {
                        currentBitRate = mediaBitRate;
                        showSwitchBitRateStarted();
                        return null;
                    }
                });

        requireNotNull(PLVMediaPlayerLocalProvider.localDependScope.on(this).current())
                .get(PLVMPMediaViewModel.class)
                .getOnInfoEvent()
                .observeUntilViewDetached(this, new Function1<PLVMediaPlayerOnInfoEvent, Unit>() {
                    @Override
                    public Unit invoke(PLVMediaPlayerOnInfoEvent onInfoEvent) {
                        if (onInfoEvent.getWhat() == PLVMediaPlayerOnInfoEvent.MEDIA_INFO_VIDEO_RENDERING_START) {
                            showSwitchBitRateFinished();
                        }
                        return null;
                    }
                });
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
                new ForegroundColorSpan(parseColor("#3F76FC")),
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
                new ForegroundColorSpan(parseColor("#3F76FC")),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );
        sb.append(getContext().getString(R.string.plv_media_player_ui_component_switch_bit_rate_finish_text_post));
        switchBitRateHintTv.setText(sb);

        PLVViewUtil.showViewForDuration(this, 3000);
    }

}
