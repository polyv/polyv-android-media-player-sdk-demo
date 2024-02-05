package net.polyv.android.player.common.ui.component;

import static com.plv.foundationsdk.component.livedata.PLVLiveDataExt.observeUntilViewDetached;
import static com.plv.foundationsdk.ext.PLVViewGroupExt.children;
import static com.plv.foundationsdk.utils.PLVSugarUtil.requireNotNull;

import android.arch.lifecycle.Observer;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.plv.foundationsdk.component.collection.PLVSequenceWrapper;
import com.plv.foundationsdk.component.remember.PLVRememberState;
import com.plv.foundationsdk.component.remember.PLVRememberStateCompareResult;
import com.plv.foundationsdk.utils.PLVFormatUtils;
import com.plv.foundationsdk.utils.PLVSugarUtil;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;

import net.polyv.android.player.business.scene.common.model.vo.PLVMediaBitRate;
import net.polyv.android.player.business.scene.common.player.IPLVMediaPlayer;
import net.polyv.android.player.common.ui.localprovider.PLVMediaPlayerLocalProvider;
import net.polyv.android.player.common.ui.viewmodel.PLVMediaPlayerControlViewModel;
import net.polyv.android.player.common.ui.viewmodel.action.PLVMediaPlayerControlAction;

import java.util.List;

import kotlin.jvm.functions.Function1;

/**
 * @author Hoshiiro
 */
public class PLVMediaPlayerBitRateSelectLayoutPortrait extends LinearLayout {

    private static final int TEXT_COLOR_SELECTED = PLVFormatUtils.parseColor("#3F76FC");
    private static final int TEXT_COLOR_NORMAL = PLVFormatUtils.parseColor("#333333");

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

        observeUntilViewDetached(
                requireNotNull(PLVMediaPlayerLocalProvider.localMediaPlayer.on(this).current())
                        .getBusinessListenerRegistry()
                        .getSupportMediaBitRates(),
                this,
                new Observer<List<PLVMediaBitRate>>() {
                    @Override
                    public void onChanged(@Nullable @org.jetbrains.annotations.Nullable List<PLVMediaBitRate> mediaBitRates) {
                        currentSupportMediaBitRates = mediaBitRates;
                        onViewStateChanged();
                    }
                }
        );

        observeUntilViewDetached(
                requireNotNull(PLVMediaPlayerLocalProvider.localMediaPlayer.on(this).current())
                        .getBusinessListenerRegistry()
                        .getCurrentMediaBitRate(),
                this,
                new Observer<PLVMediaBitRate>() {
                    @Override
                    public void onChanged(@Nullable @org.jetbrains.annotations.Nullable PLVMediaBitRate mediaBitRate) {
                        currentMediaBitRate = mediaBitRate;
                        onViewStateChanged();
                    }
                }
        );
    }

    protected void onViewStateChanged() {
        PLVRememberState.rememberStateOf(this, "onUpdateSupportMediaBitRates")
                .compareLastAndSet(currentSupportMediaBitRates)
                .ifNotEquals(new PLVSugarUtil.Consumer<PLVRememberStateCompareResult>() {
                    @Override
                    public void accept(PLVRememberStateCompareResult result) {
                        onUpdateSupportMediaBitRates();
                    }
                });

        PLVRememberState.rememberStateOf(this, "onUpdateCurrentMediaBitRate")
                .compareLastAndSet(currentMediaBitRate)
                .ifNotEquals(new PLVSugarUtil.Consumer<PLVRememberStateCompareResult>() {
                    @Override
                    public void accept(PLVRememberStateCompareResult result) {
                        onUpdateCurrentMediaBitRate();
                    }
                });
    }

    protected void onUpdateSupportMediaBitRates() {
        removeAllViews();
        if (currentSupportMediaBitRates == null || currentSupportMediaBitRates.isEmpty()) {
            return;
        }
        PLVSequenceWrapper.wrap(currentSupportMediaBitRates)
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
                                IPLVMediaPlayer mediaPlayer = PLVMediaPlayerLocalProvider.localMediaPlayer.on(v).current();
                                PLVMediaPlayerControlViewModel viewModel = PLVMediaPlayerLocalProvider.localControlViewModel.on(v).current();
                                if (mediaPlayer != null) {
                                    mediaPlayer.changeBitRate(mediaBitRate);
                                }
                                if (viewModel != null) {
                                    viewModel.requestControl(PLVMediaPlayerControlAction.hintBitRateChanged(mediaBitRate));
                                    viewModel.requestControl(PLVMediaPlayerControlAction.closeFloatMenuLayout());
                                }
                            }
                        });
                        return tv;
                    }
                })
                .forEach(new PLVSugarUtil.Consumer<TextView>() {
                    @Override
                    public void accept(TextView textView) {
                        MarginLayoutParams layoutParams = new MarginLayoutParams(ConvertUtils.dp2px(40), ConvertUtils.dp2px(17));
                        layoutParams.setMarginEnd(ConvertUtils.dp2px(28));
                        addView(textView, layoutParams);
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
