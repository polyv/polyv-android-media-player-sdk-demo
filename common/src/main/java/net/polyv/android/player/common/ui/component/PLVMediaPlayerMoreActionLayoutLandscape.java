package net.polyv.android.player.common.ui.component;

import static net.polyv.android.player.sdk.foundation.lang.PreconditionsKt.requireNotNull;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

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
public class PLVMediaPlayerMoreActionLayoutLandscape extends FrameLayout implements View.OnClickListener {

    private ImageView moreActionCloseIv;
    private ConstraintLayout moreActionContainer;
    private PLVMediaPlayerMoreLayoutAudioModeActionView moreAudioModeActionView;

    protected List<PLVMediaOutputMode> currentSupportMediaOutputModes = null;
    protected boolean isVisible = false;

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

        requireNotNull(PLVMediaPlayerLocalProvider.localDependScope.on(this).current())
                .get(PLVMPMediaViewModel.class)
                .getMediaInfoViewState()
                .observeUntilViewDetached(this, new Function1<PLVMPMediaInfoViewState, Unit>() {
                    @Override
                    public Unit invoke(PLVMPMediaInfoViewState viewState) {
                        currentSupportMediaOutputModes = viewState.getSupportOutputModes();
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
                        isVisible = viewState.getLastFloatActionLayout() == PLVMPMediaControllerFloatAction.MORE;
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

    protected void onChangeVisibility() {
        setVisibility(isVisible ? View.VISIBLE : View.GONE);
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
        requireNotNull(PLVMediaPlayerLocalProvider.localDependScope.on(this).current())
                .get(PLVMPMediaControllerViewModel.class)
                .popFloatActionLayout();
    }

}
