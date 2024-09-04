package net.polyv.android.player.common.ui.component;

import static net.polyv.android.player.sdk.foundation.graphics.ColorsKt.parseColor;
import static net.polyv.android.player.sdk.foundation.lang.PreconditionsKt.requireNotNull;
import static net.polyv.android.player.sdk.foundation.lang.StringsKt.format;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import net.polyv.android.player.business.scene.common.model.vo.PLVMediaBitRate;
import net.polyv.android.player.business.scene.common.model.vo.PLVMediaOutputMode;
import net.polyv.android.player.common.R;
import net.polyv.android.player.common.di.PLVMediaPlayerLocalProvider;
import net.polyv.android.player.common.modules.media.viewmodel.PLVMPMediaViewModel;
import net.polyv.android.player.sdk.foundation.collections.PLVSequences;
import net.polyv.android.player.sdk.foundation.lang.PLVRememberState;
import net.polyv.android.player.sdk.foundation.lang.PLVRememberStateCompareResult;

import java.util.List;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

/**
 * @author Hoshiiro
 */
public class PLVMediaPlayerNetworkPoorIndicateLayout extends FrameLayout implements View.OnClickListener {

    // <editor-fold defaultstate="collapsed" desc="变量">

    private TextView networkPoorIndicateTv;
    private ImageView networkPoorIndicateCloseIv;

    protected boolean isVisible = false;
    protected PLVMediaBitRate alternativeBitRate = null;

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造函数">

    public PLVMediaPlayerNetworkPoorIndicateLayout(@NonNull Context context) {
        super(context);
    }

    public PLVMediaPlayerNetworkPoorIndicateLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PLVMediaPlayerNetworkPoorIndicateLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化方法">

    {
        LayoutInflater.from(getContext()).inflate(R.layout.plv_media_player_ui_component_network_poor_indicate_layout, this);

        networkPoorIndicateTv = findViewById(R.id.plv_media_player_network_poor_indicate_tv);
        networkPoorIndicateCloseIv = findViewById(R.id.plv_media_player_network_poor_indicate_close_iv);

        networkPoorIndicateCloseIv.setOnClickListener(this);
        onViewStateChanged();
    }

    // </editor-fold>

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (isInEditMode()) return;

        final PLVMPMediaViewModel mediaViewModel = requireNotNull(PLVMediaPlayerLocalProvider.localDependScope.on(this).current())
                .get(PLVMPMediaViewModel.class);

        mediaViewModel
                .getNetworkPoorEvent()
                .observeUntilViewDetached(this, new Function1<Long, Unit>() {
                    @Override
                    public Unit invoke(Long aLong) {
                        final List<PLVMediaBitRate> supportBitRates = mediaViewModel.getMediaInfoViewState().getValue().getSupportBitRates();
                        final PLVMediaBitRate currentBitRate = mediaViewModel.getMediaInfoViewState().getValue().getBitRate();
                        final PLVMediaOutputMode outputMode = mediaViewModel.getMediaInfoViewState().getValue().getOutputMode();
                        if (supportBitRates.isEmpty() || currentBitRate == null || outputMode == PLVMediaOutputMode.AUDIO_ONLY) {
                            return null;
                        }
                        PLVMediaBitRate nextDowngradeBitRate = PLVSequences.wrap(supportBitRates)
                                .filter(new Function1<PLVMediaBitRate, Boolean>() {
                                    @Override
                                    public Boolean invoke(PLVMediaBitRate mediaBitRate) {
                                        return mediaBitRate.getIndex() < currentBitRate.getIndex();
                                    }
                                })
                                .maxBy(new Function1<PLVMediaBitRate, Comparable>() {
                                    @Override
                                    public Comparable invoke(PLVMediaBitRate mediaBitRate) {
                                        return mediaBitRate.getIndex();
                                    }
                                });
                        if (nextDowngradeBitRate == null) {
                            return null;
                        }
                        alternativeBitRate = nextDowngradeBitRate;
                        isVisible = true;
                        onViewStateChanged();
                        return null;
                    }
                });
    }

    protected void onViewStateChanged() {
        PLVRememberState.rememberStateOf(this, "onNetworkPoorIndicate")
                .compareLastAndSet(isVisible, alternativeBitRate)
                .ifNotEquals(new Function1<PLVRememberStateCompareResult, Unit>() {
                    @Override
                    public Unit invoke(PLVRememberStateCompareResult result) {
                        onNetworkPoorIndicate();
                        return null;
                    }
                });
    }

    protected void onNetworkPoorIndicate() {
        setVisibility(isVisible ? View.VISIBLE : View.GONE);
        setIndicateText();
    }

    private void setIndicateText() {
        if (alternativeBitRate == null) {
            return;
        }
        SpannableStringBuilder sb = new SpannableStringBuilder();
        sb.append(getContext().getString(R.string.plv_media_player_ui_component_network_poor_hint_text));
        sb.append(
                format(getContext().getString(R.string.plv_media_player_ui_component_network_poor_switch_bitrate_action_text), alternativeBitRate.getName()),
                new ClickableSpan() {
                    @Override
                    public void onClick(@NonNull View widget) {
                        onClickDowngradeBitRate();
                    }

                    @Override
                    public void updateDrawState(@NonNull TextPaint ds) {
                        super.updateDrawState(ds);
                        ds.setColor(parseColor("#3F76FC"));
                        ds.setUnderlineText(false);
                    }
                },
                SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        networkPoorIndicateTv.setText(sb);
        networkPoorIndicateTv.setMovementMethod(LinkMovementMethod.getInstance());
    }

    // <editor-fold defaultstate="collapsed" desc="点击事件">

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == networkPoorIndicateCloseIv.getId()) {
            isVisible = false;
            onViewStateChanged();
        }
    }

    private void onClickDowngradeBitRate() {
        requireNotNull(PLVMediaPlayerLocalProvider.localDependScope.on(this).current())
                .get(PLVMPMediaViewModel.class)
                .changeBitRate(alternativeBitRate);
    }

    // </editor-fold>

}
