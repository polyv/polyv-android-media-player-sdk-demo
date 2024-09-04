package net.polyv.android.player.common.ui.component;

import static net.polyv.android.player.sdk.foundation.graphics.ColorsKt.parseColor;
import static net.polyv.android.player.sdk.foundation.graphics.DisplaysKt.dp;
import static net.polyv.android.player.sdk.foundation.lang.PreconditionsKt.requireNotNull;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.polyv.android.player.business.scene.common.model.vo.PLVMediaBitRate;
import net.polyv.android.player.common.R;
import net.polyv.android.player.common.di.PLVMediaPlayerLocalProvider;
import net.polyv.android.player.common.modules.media.viewmodel.PLVMPMediaViewModel;
import net.polyv.android.player.common.modules.media.viewmodel.viewstate.PLVMPMediaInfoViewState;
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.PLVMPMediaControllerViewModel;
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.viewstate.PLVMPMediaControllerFloatAction;
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.viewstate.PLVMPMediaControllerViewState;
import net.polyv.android.player.sdk.foundation.collections.PLVSequences;
import net.polyv.android.player.sdk.foundation.di.DependScope;
import net.polyv.android.player.sdk.foundation.lang.PLVRememberState;
import net.polyv.android.player.sdk.foundation.lang.PLVRememberStateCompareResult;

import java.util.List;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

/**
 * @author Hoshiiro
 */
public class PLVMediaPlayerBitRateSelectLayoutLandscape extends FrameLayout implements View.OnClickListener {

    private ImageView bitRateSelectCloseIv;
    private LinearLayout bitRateSelectContainer;

    protected List<PLVMediaBitRate> currentSupportMediaBitRates = null;
    protected PLVMediaBitRate currentMediaBitRate = null;
    protected boolean isVisible = false;

    public PLVMediaPlayerBitRateSelectLayoutLandscape(@NonNull Context context) {
        super(context);
    }

    public PLVMediaPlayerBitRateSelectLayoutLandscape(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PLVMediaPlayerBitRateSelectLayoutLandscape(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    {
        LayoutInflater.from(getContext()).inflate(R.layout.plv_media_player_ui_component_bit_rate_select_layout, this);
        bitRateSelectCloseIv = findViewById(R.id.plv_media_player_bit_rate_select_close_iv);
        bitRateSelectContainer = findViewById(R.id.plv_media_player_bit_rate_select_container);

        setOnClickListener(this);
        bitRateSelectCloseIv.setOnClickListener(this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (isInEditMode()) return;

        requireNotNull(PLVMediaPlayerLocalProvider.localDependScope.on(this).current())
                .get(PLVMPMediaViewModel.class)
                .getMediaInfoViewState()
                .observeUntilViewDetached(this, new Function1<PLVMPMediaInfoViewState, Unit>() {
                    @Override
                    public Unit invoke(PLVMPMediaInfoViewState viewState) {
                        currentSupportMediaBitRates = viewState.getSupportBitRates();
                        currentMediaBitRate = viewState.getBitRate();
                        onViewStateChanged();
                        return null;
                    }
                });

        requireNotNull(PLVMediaPlayerLocalProvider.localDependScope.on(this).current())
                .get(PLVMPMediaControllerViewModel.class)
                .getMediaControllerViewState()
                .observeUntilViewDetached(this, new Function1<PLVMPMediaControllerViewState, Unit>() {
                    @Override
                    public Unit invoke(PLVMPMediaControllerViewState viewState) {
                        isVisible = viewState.getLastFloatActionLayout() == PLVMPMediaControllerFloatAction.BITRATE;
                        onViewStateChanged();
                        return null;
                    }
                });
    }

    protected void onViewStateChanged() {
        PLVRememberState.rememberStateOf(this, "onUpdateMediaBitRates")
                .compareLastAndSet(currentSupportMediaBitRates, currentMediaBitRate)
                .ifNotEquals(new Function1<PLVRememberStateCompareResult, Unit>() {
                    @Override
                    public Unit invoke(PLVRememberStateCompareResult result) {
                        onUpdateMediaBitRates();
                        return null;
                    }
                });

        PLVRememberState.rememberStateOf(this, "onChangeVisibility")
                .compareLastAndSet(isVisible)
                .ifNotEquals(new Function1<PLVRememberStateCompareResult, Unit>() {
                    @Override
                    public Unit invoke(PLVRememberStateCompareResult result) {
                        onChangeVisibility();
                        return null;
                    }
                });
    }

    protected void onUpdateMediaBitRates() {
        final DependScope dependScope = requireNotNull(PLVMediaPlayerLocalProvider.localDependScope.on(this).current());
        final PLVMPMediaViewModel mediaViewModel = dependScope.get(PLVMPMediaViewModel.class);
        final PLVMPMediaControllerViewModel mediaControllerViewModel = dependScope.get(PLVMPMediaControllerViewModel.class);
        if (currentSupportMediaBitRates == null || currentMediaBitRate == null) {
            bitRateSelectContainer.removeAllViews();
            return;
        }

        bitRateSelectContainer.removeAllViews();
        PLVSequences.wrap(currentSupportMediaBitRates)
                .map(new Function1<PLVMediaBitRate, TextView>() {
                    @Override
                    public TextView invoke(final PLVMediaBitRate mediaBitRate) {
                        TextView tv = new TextView(getContext());
                        tv.setText(mediaBitRate.getName());
                        if (mediaBitRate.equals(currentMediaBitRate)) {
                            tv.setTextColor(parseColor("#3F76FC"));
                        } else {
                            tv.setTextColor(Color.WHITE);
                        }
                        tv.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mediaViewModel.changeBitRate(mediaBitRate);
                                mediaControllerViewModel.changeControllerVisible(false);
                                closeLayout();
                            }
                        });
                        return tv;
                    }
                })
                .forEach(new Function1<TextView, Unit>() {
                    @Override
                    public Unit invoke(TextView textView) {
                        MarginLayoutParams lp = new MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        lp.topMargin = dp(24).px();
                        lp.bottomMargin = dp(24).px();
                        bitRateSelectContainer.addView(textView, lp);
                        return null;
                    }
                });
    }

    protected void onChangeVisibility() {
        setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        if (id == bitRateSelectCloseIv.getId()) {
            closeLayout();
        } else if (id == this.getId()) {
            closeLayout();
        }
    }

    private void closeLayout() {
        requireNotNull(PLVMediaPlayerLocalProvider.localDependScope.on(this).current())
                .get(PLVMPMediaControllerViewModel.class)
                .popFloatActionLayout();
    }

}
