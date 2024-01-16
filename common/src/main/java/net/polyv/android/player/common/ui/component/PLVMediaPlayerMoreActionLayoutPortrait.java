package net.polyv.android.player.common.ui.component;

import static com.plv.foundationsdk.component.livedata.PLVLiveDataExt.observeUntilViewDetached;
import static com.plv.foundationsdk.utils.PLVSugarUtil.requireNotNull;

import androidx.lifecycle.Observer;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Group;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.plv.foundationsdk.component.remember.PLVRememberState;
import com.plv.foundationsdk.component.remember.PLVRememberStateCompareResult;
import com.plv.foundationsdk.utils.PLVSugarUtil;

import net.polyv.android.player.business.scene.common.model.vo.PLVMediaBitRate;
import net.polyv.android.player.business.scene.common.model.vo.PLVMediaOutputMode;
import net.polyv.android.player.common.R;
import net.polyv.android.player.common.ui.localprovider.PLVMediaPlayerLocalProvider;
import net.polyv.android.player.common.ui.viewmodel.PLVMediaPlayerControlViewModel;
import net.polyv.android.player.common.ui.viewmodel.action.PLVMediaPlayerControlAction;
import net.polyv.android.player.common.ui.viewmodel.viewstate.PLVMediaPlayerControlViewState;

import java.util.List;

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
    protected PLVMediaPlayerControlViewState currentControlViewState = null;

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

        observeUntilViewDetached(
                requireNotNull(PLVMediaPlayerLocalProvider.localMediaPlayer.on(this).current())
                        .getBusinessListenerRegistry()
                        .getSupportMediaOutputModes(),
                this,
                new Observer<List<PLVMediaOutputMode>>() {
                    @Override
                    public void onChanged(@Nullable @org.jetbrains.annotations.Nullable List<PLVMediaOutputMode> mediaOutputModes) {
                        currentSupportMediaOutputModes = mediaOutputModes;
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
                requireNotNull(PLVMediaPlayerLocalProvider.localMediaPlayer.on(this).current())
                        .getBusinessListenerRegistry()
                        .getCurrentMediaOutputMode(),
                this,
                new Observer<PLVMediaOutputMode>() {
                    @Override
                    public void onChanged(@Nullable @org.jetbrains.annotations.Nullable PLVMediaOutputMode mediaOutputMode) {
                        currentMediaOutputMode = mediaOutputMode;
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
        PLVRememberState.rememberStateOf(this, "onSupportMediaOutputModesChanged")
                .compareLastAndSet(currentSupportMediaOutputModes)
                .ifNotEquals(new PLVSugarUtil.Consumer<PLVRememberStateCompareResult>() {
                    @Override
                    public void accept(PLVRememberStateCompareResult result) {
                        onSupportMediaOutputModesChanged();
                    }
                });

        PLVRememberState.rememberStateOf(this, "onChangeMediaBitRateGroupVisibility")
                .compareLastAndSet(currentMediaBitRate, currentMediaOutputMode)
                .ifNotEquals(new PLVSugarUtil.Consumer<PLVRememberStateCompareResult>() {
                    @Override
                    public void accept(PLVRememberStateCompareResult result) {
                        onChangeMediaBitRateGroupVisibility();
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
        if (currentControlViewState == null) {
            return;
        }
        final boolean visible = currentControlViewState.moreActionLayoutVisible
                && !currentControlViewState.isOverlayLayoutVisible();
        setVisibility(visible ? View.VISIBLE : View.GONE);
        if (getParent() != null) {
            getParent().requestDisallowInterceptTouchEvent(!visible);
            getParent().requestDisallowInterceptTouchEvent(visible);
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
        PLVMediaPlayerControlViewModel controlViewModel = PLVMediaPlayerLocalProvider.localControlViewModel.on(this).current();
        if (controlViewModel == null) {
            return;
        }
        controlViewModel.requestControl(PLVMediaPlayerControlAction.closeFloatMenuLayout());
    }

}
