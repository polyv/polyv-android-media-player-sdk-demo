package net.polyv.android.player.common.ui.component;

import static com.plv.foundationsdk.component.livedata.PLVLiveDataExt.observeUntilViewDetached;
import static com.plv.foundationsdk.utils.PLVSugarUtil.requireNotNull;

import androidx.lifecycle.Observer;
import android.content.Context;
import android.graphics.Color;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
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
import net.polyv.android.player.common.R;
import net.polyv.android.player.common.ui.localprovider.PLVMediaPlayerLocalProvider;
import net.polyv.android.player.common.ui.viewmodel.PLVMediaPlayerControlViewModel;
import net.polyv.android.player.common.ui.viewmodel.action.PLVMediaPlayerControlAction;
import net.polyv.android.player.common.ui.viewmodel.viewstate.PLVMediaPlayerControlViewState;

import java.util.List;

import kotlin.jvm.functions.Function1;

/**
 * @author Hoshiiro
 */
public class PLVMediaPlayerBitRateSelectLayoutLandscape extends FrameLayout implements View.OnClickListener {

    private ImageView bitRateSelectCloseIv;
    private LinearLayout bitRateSelectContainer;

    protected List<PLVMediaBitRate> currentSupportMediaBitRates = null;
    protected PLVMediaBitRate currentMediaBitRate = null;
    protected PLVMediaPlayerControlViewState currentControlViewState = null;

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

        observeUntilViewDetached(
                requireNotNull(PLVMediaPlayerLocalProvider.localControlViewModel.on(this).current())
                        .getControlViewStateLiveData(),
                this,
                new Observer<PLVMediaPlayerControlViewState>() {
                    @Override
                    public void onChanged(@Nullable @org.jetbrains.annotations.Nullable PLVMediaPlayerControlViewState viewState) {
                        currentControlViewState = viewState;
                        onViewStateChanged();
                    }
                }
        );
    }

    protected void onViewStateChanged() {
        PLVRememberState.rememberStateOf(this, "onUpdateMediaBitRates")
                .compareLastAndSet(currentSupportMediaBitRates, currentMediaBitRate)
                .ifNotEquals(new PLVSugarUtil.Consumer<PLVRememberStateCompareResult>() {
                    @Override
                    public void accept(PLVRememberStateCompareResult result) {
                        onUpdateMediaBitRates();
                    }
                });

        PLVRememberState.rememberStateOf(this, "onChangeVisibility")
                .compareLastAndSet(currentControlViewState)
                .ifNotEquals(new PLVSugarUtil.Consumer<PLVRememberStateCompareResult>() {
                    @Override
                    public void accept(PLVRememberStateCompareResult result) {
                        onChangeVisibility();
                    }
                });
    }

    protected void onUpdateMediaBitRates() {
        final IPLVMediaPlayer mediaPlayer = PLVMediaPlayerLocalProvider.localMediaPlayer.on(this).current();
        if (mediaPlayer == null) {
            return;
        }
        if (currentSupportMediaBitRates == null || currentMediaBitRate == null) {
            bitRateSelectContainer.removeAllViews();
            return;
        }

        bitRateSelectContainer.removeAllViews();
        PLVSequenceWrapper.wrap(currentSupportMediaBitRates)
                .map(new Function1<PLVMediaBitRate, TextView>() {
                    @Override
                    public TextView invoke(final PLVMediaBitRate mediaBitRate) {
                        TextView tv = new TextView(getContext());
                        tv.setText(mediaBitRate.getName());
                        if (mediaBitRate.equals(currentMediaBitRate)) {
                            tv.setTextColor(PLVFormatUtils.parseColor("#3F76FC"));
                        } else {
                            tv.setTextColor(Color.WHITE);
                        }
                        tv.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mediaPlayer.changeBitRate(mediaBitRate);
                                closeLayout();
                            }
                        });
                        return tv;
                    }
                })
                .forEach(new PLVSugarUtil.Consumer<TextView>() {
                    @Override
                    public void accept(TextView textView) {
                        MarginLayoutParams lp = new MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        lp.topMargin = ConvertUtils.dp2px(24);
                        lp.bottomMargin = ConvertUtils.dp2px(24);
                        bitRateSelectContainer.addView(textView, lp);
                    }
                });
    }

    protected void onChangeVisibility() {
        if (currentControlViewState == null) {
            return;
        }
        final boolean visible = currentControlViewState.bitRateSelectLayoutVisible
                && !currentControlViewState.isOverlayLayoutVisible();
        setVisibility(visible ? View.VISIBLE : View.GONE);
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
        PLVMediaPlayerControlViewModel controlViewModel = PLVMediaPlayerLocalProvider.localControlViewModel.on(this).current();
        if (controlViewModel != null) {
            controlViewModel.requestControl(PLVMediaPlayerControlAction.closeFloatMenuLayout());
        }
    }

}
