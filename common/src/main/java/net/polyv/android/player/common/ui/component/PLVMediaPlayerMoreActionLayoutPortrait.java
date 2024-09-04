package net.polyv.android.player.common.ui.component;

import static net.polyv.android.player.sdk.foundation.lang.PreconditionsKt.requireNotNull;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.Group;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.polyv.android.player.business.scene.common.model.vo.PLVMediaBitRate;
import net.polyv.android.player.business.scene.common.model.vo.PLVMediaOutputMode;
import net.polyv.android.player.common.R;
import net.polyv.android.player.common.di.PLVMediaPlayerLocalProvider;
import net.polyv.android.player.common.modules.media.viewmodel.PLVMPMediaViewModel;
import net.polyv.android.player.common.modules.media.viewmodel.viewstate.PLVMPMediaInfoViewState;
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.PLVMPMediaControllerViewModel;
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.viewstate.PLVMPMediaControllerFloatAction;
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.viewstate.PLVMPMediaControllerViewState;
import net.polyv.android.player.sdk.foundation.lang.PLVRememberState;
import net.polyv.android.player.sdk.foundation.lang.PLVRememberStateCompareResult;

import java.util.List;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

/**
 * @author Hoshiiro
 */
public class PLVMediaPlayerMoreActionLayoutPortrait extends FrameLayout implements View.OnClickListener {

    private ImageView moreActionCloseIv;
    private ConstraintLayout moreLayoutActionContainer;
    private PLVMediaPlayerMoreLayoutAudioModeActionView moreAudioModeActionView;
    private TextView moreBitRateHintTv;
    private LinearLayout moreBitRateLl;
    private Group moreBitRateGroup;
    private TextView moreSpeedHintTv;
    private LinearLayout moreSpeedLl;

    protected List<PLVMediaOutputMode> currentSupportMediaOutputModes = null;
    protected PLVMediaBitRate currentMediaBitRate = null;
    protected PLVMediaOutputMode currentMediaOutputMode = null;
    protected boolean isVisible = false;

    public PLVMediaPlayerMoreActionLayoutPortrait(@NonNull Context context) {
        super(context);
    }

    public PLVMediaPlayerMoreActionLayoutPortrait(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PLVMediaPlayerMoreActionLayoutPortrait(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    {
        LayoutInflater.from(getContext()).inflate(R.layout.plv_media_player_ui_component_more_action_layout_portrait, this);
        moreActionCloseIv = findViewById(R.id.plv_media_player_more_action_close_iv);
        moreLayoutActionContainer = findViewById(R.id.plv_media_player_more_layout_action_container);
        moreAudioModeActionView = findViewById(R.id.plv_media_player_more_audio_mode_action_view);
        moreBitRateHintTv = findViewById(R.id.plv_media_player_more_bit_rate_hint_tv);
        moreBitRateLl = findViewById(R.id.plv_media_player_more_bit_rate_ll);
        moreBitRateGroup = findViewById(R.id.plv_media_player_more_bit_rate_group);
        moreSpeedHintTv = findViewById(R.id.plv_media_player_more_speed_hint_tv);
        moreSpeedLl = findViewById(R.id.plv_media_player_more_speed_ll);

        setOnClickListener(this);
        moreActionCloseIv.setOnClickListener(this);
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
                        currentSupportMediaOutputModes = viewState.getSupportOutputModes();
                        currentMediaBitRate = viewState.getBitRate();
                        currentMediaOutputMode = viewState.getOutputMode();
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
                        isVisible = viewState.getLastFloatActionLayout() == PLVMPMediaControllerFloatAction.MORE
                                && !viewState.isMediaStopOverlayVisible();
                        onViewStateChanged();
                        return null;
                    }
                });
    }

    protected void onViewStateChanged() {
        PLVRememberState.rememberStateOf(this, "onSupportMediaOutputModesChanged")
                .compareLastAndSet(currentSupportMediaOutputModes)
                .ifNotEquals(new Function1<PLVRememberStateCompareResult, Unit>() {
                    @Override
                    public Unit invoke(PLVRememberStateCompareResult result) {
                        onSupportMediaOutputModesChanged();
                        return null;
                    }
                });

        PLVRememberState.rememberStateOf(this, "onChangeMediaBitRateGroupVisibility")
                .compareLastAndSet(currentMediaBitRate, currentMediaOutputMode)
                .ifNotEquals(new Function1<PLVRememberStateCompareResult, Unit>() {
                    @Override
                    public Unit invoke(PLVRememberStateCompareResult result) {
                        onChangeMediaBitRateGroupVisibility();
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

    protected void onSupportMediaOutputModesChanged() {
        if (currentSupportMediaOutputModes == null) {
            return;
        }
        boolean showAudioModeAction = currentSupportMediaOutputModes.contains(PLVMediaOutputMode.AUDIO_ONLY);
        moreAudioModeActionView.setVisibility(showAudioModeAction ? View.VISIBLE : View.GONE);
    }

    protected void onChangeMediaBitRateGroupVisibility() {
        boolean visible = currentMediaBitRate != null && currentMediaOutputMode != PLVMediaOutputMode.AUDIO_ONLY;
        moreBitRateGroup.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    protected void onChangeVisibility() {
        setVisibility(isVisible ? View.VISIBLE : View.GONE);
        if (getParent() != null) {
            getParent().requestDisallowInterceptTouchEvent(!isVisible);
            getParent().requestDisallowInterceptTouchEvent(isVisible);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == moreActionCloseIv.getId()) {
            closeLayout();
        } else if (id == getId()) {
            closeLayout();
        }
    }

    private void closeLayout() {
        requireNotNull(PLVMediaPlayerLocalProvider.localDependScope.on(this).current())
                .get(PLVMPMediaControllerViewModel.class)
                .popFloatActionLayout();
    }

}
