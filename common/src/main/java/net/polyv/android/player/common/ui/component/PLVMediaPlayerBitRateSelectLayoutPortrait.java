package net.polyv.android.player.common.ui.component;

import static net.polyv.android.player.sdk.foundation.graphics.ColorsKt.parseColor;
import static net.polyv.android.player.sdk.foundation.graphics.DisplaysKt.dp;
import static net.polyv.android.player.sdk.foundation.lang.PreconditionsKt.requireNotNull;
import static net.polyv.android.player.sdk.foundation.ui.ViewGroupsKt.children;

import android.content.Context;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.polyv.android.player.business.scene.common.model.vo.PLVMediaBitRate;
import net.polyv.android.player.common.di.PLVMediaPlayerLocalProvider;
import net.polyv.android.player.common.modules.media.viewmodel.PLVMPMediaViewModel;
import net.polyv.android.player.common.modules.media.viewmodel.viewstate.PLVMPMediaInfoViewState;
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.PLVMPMediaControllerViewModel;
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
public class PLVMediaPlayerBitRateSelectLayoutPortrait extends LinearLayout {

    private static final int TEXT_COLOR_SELECTED = parseColor("#3F76FC");
    private static final int TEXT_COLOR_NORMAL = parseColor("#333333");

    protected List<PLVMediaBitRate> currentSupportMediaBitRates = null;
    protected PLVMediaBitRate currentMediaBitRate = null;

    public PLVMediaPlayerBitRateSelectLayoutPortrait(Context context) {
        super(context);
    }

    public PLVMediaPlayerBitRateSelectLayoutPortrait(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PLVMediaPlayerBitRateSelectLayoutPortrait(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    {
        setOrientation(HORIZONTAL);
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
    }

    protected void onViewStateChanged() {
        PLVRememberState.rememberStateOf(this, "onUpdateSupportMediaBitRates")
                .compareLastAndSet(currentSupportMediaBitRates)
                .ifNotEquals(new Function1<PLVRememberStateCompareResult, Unit>() {
                    @Override
                    public Unit invoke(PLVRememberStateCompareResult result) {
                        onUpdateSupportMediaBitRates();
                        return null;
                    }
                });

        PLVRememberState.rememberStateOf(this, "onUpdateCurrentMediaBitRate")
                .compareLastAndSet(currentMediaBitRate)
                .ifNotEquals(new Function1<PLVRememberStateCompareResult, Unit>() {
                    @Override
                    public Unit invoke(PLVRememberStateCompareResult result) {
                        onUpdateCurrentMediaBitRate();
                        return null;
                    }
                });
    }

    protected void onUpdateSupportMediaBitRates() {
        removeAllViews();
        if (currentSupportMediaBitRates == null || currentSupportMediaBitRates.isEmpty()) {
            return;
        }
        PLVSequences.wrap(currentSupportMediaBitRates)
                .map(new Function1<PLVMediaBitRate, TextView>() {
                    @Override
                    public TextView invoke(final PLVMediaBitRate mediaBitRate) {
                        TextView tv = new TextView(getContext());
                        tv.setText(mediaBitRate.getName());
                        tv.setTag(mediaBitRate);
                        tv.setTextSize(12);
                        tv.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                final DependScope dependScope = requireNotNull(PLVMediaPlayerLocalProvider.localDependScope.on(PLVMediaPlayerBitRateSelectLayoutPortrait.this).current());
                                final PLVMPMediaViewModel mediaViewModel = dependScope.get(PLVMPMediaViewModel.class);
                                final PLVMPMediaControllerViewModel mediaControllerViewModel = dependScope.get(PLVMPMediaControllerViewModel.class);
                                mediaViewModel.changeBitRate(mediaBitRate);
                                mediaControllerViewModel.popFloatActionLayout();
                            }
                        });
                        return tv;
                    }
                })
                .forEach(new Function1<TextView, Unit>() {
                    @Override
                    public Unit invoke(TextView textView) {
                        MarginLayoutParams layoutParams = new MarginLayoutParams(dp(40).px(), dp(17).px());
                        layoutParams.setMarginEnd(dp(28).px());
                        addView(textView, layoutParams);
                        return null;
                    }
                });
        onUpdateCurrentMediaBitRate();
    }

    protected void onUpdateCurrentMediaBitRate() {
        for (View child : children(this)) {
            if (!(child instanceof TextView)) {
                continue;
            }
            if (child.getTag() == currentMediaBitRate) {
                ((TextView) child).setTextColor(TEXT_COLOR_SELECTED);
            } else {
                ((TextView) child).setTextColor(TEXT_COLOR_NORMAL);
            }
        }
    }

}
