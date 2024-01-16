package net.polyv.android.player.common.ui.component;

import static com.plv.foundationsdk.component.livedata.PLVLiveDataExt.observeUntilViewDetached;
import static com.plv.foundationsdk.utils.PLVSugarUtil.requireNotNull;

import androidx.lifecycle.Observer;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.plv.foundationsdk.component.remember.PLVRememberState;
import com.plv.foundationsdk.component.remember.PLVRememberStateCompareResult;
import com.plv.foundationsdk.utils.PLVSugarUtil;

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
public class PLVMediaPlayerMoreActionLayoutLandscape extends FrameLayout implements View.OnClickListener {

    private ImageView moreActionCloseIv;
    private ConstraintLayout moreActionContainer;
    private PLVMediaPlayerMoreLayoutAudioModeActionView moreAudioModeActionView;

    protected List<PLVMediaOutputMode> currentSupportMediaOutputModes = null;
    protected PLVMediaPlayerControlViewState currentControlViewState = null;

    public PLVMediaPlayerMoreActionLayoutLandscape(@NonNull Context context) {
        super(context);
    }

    public PLVMediaPlayerMoreActionLayoutLandscape(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PLVMediaPlayerMoreActionLayoutLandscape(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    {
        LayoutInflater.from(getContext()).inflate(R.layout.plv_media_player_ui_component_more_action_layout_landscape, this);
        moreActionCloseIv = findViewById(R.id.plv_media_player_more_action_close_iv);
        moreActionContainer = findViewById(R.id.plv_media_player_more_action_container);
        moreAudioModeActionView = findViewById(R.id.plv_media_player_more_audio_mode_action_view);

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

    protected void onChangeVisibility() {
        if (currentControlViewState == null) {
            return;
        }
        final boolean visible = currentControlViewState.moreActionLayoutVisible
                && !currentControlViewState.isOverlayLayoutVisible();
        setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == moreActionCloseIv.getId()) {
            closeLayout();
        } else if (id == this.getId()) {
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
