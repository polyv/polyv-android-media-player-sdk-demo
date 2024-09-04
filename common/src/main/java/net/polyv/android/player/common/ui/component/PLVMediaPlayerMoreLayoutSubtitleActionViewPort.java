package net.polyv.android.player.common.ui.component;

import static net.polyv.android.player.sdk.foundation.lang.PreconditionsKt.requireNotNull;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import net.polyv.android.player.common.R;
import net.polyv.android.player.common.di.PLVMediaPlayerLocalProvider;
import net.polyv.android.player.common.modules.media.viewmodel.PLVMPMediaViewModel;
import net.polyv.android.player.common.modules.media.viewmodel.viewstate.PLVMPMediaInfoViewState;
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.PLVMPMediaControllerViewModel;
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.viewstate.PLVMPMediaControllerFloatAction;
import net.polyv.android.player.sdk.foundation.lang.PLVRememberState;
import net.polyv.android.player.sdk.foundation.lang.PLVRememberStateCompareResult;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

/**
 * @author Hoshiiro
 */
public class PLVMediaPlayerMoreLayoutSubtitleActionViewPort extends FrameLayout implements View.OnClickListener {

    private ImageView subtitleActionIv;
    private TextView subtitleActionTv;

    private boolean isEnable = false;
    private boolean isVisible = false;

    public PLVMediaPlayerMoreLayoutSubtitleActionViewPort(@NonNull Context context) {
        super(context);
        init();
    }

    public PLVMediaPlayerMoreLayoutSubtitleActionViewPort(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PLVMediaPlayerMoreLayoutSubtitleActionViewPort(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.plv_media_player_ui_component_more_subtitle_action_layout_port, this);
        subtitleActionIv = findViewById(R.id.plv_media_player_subtitle_action_iv);
        subtitleActionTv = findViewById(R.id.plv_media_player_subtitle_action_tv);

        setOnClickListener(this);
        onViewStateChanged();
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
                        isEnable = viewState.getCurrentSubtitle() != null && !viewState.getCurrentSubtitle().isEmpty();
                        isVisible = !viewState.getSupportSubtitles().isEmpty();
                        onViewStateChanged();
                        return null;
                    }
                });
    }

    private void onViewStateChanged() {
        PLVRememberState.rememberStateOf(this, "updateEnableState")
                .compareLastAndSet(isEnable)
                .ifNotEquals(new Function1<PLVRememberStateCompareResult, Unit>() {
                    @Override
                    public Unit invoke(PLVRememberStateCompareResult result) {
                        updateEnableState();
                        return null;
                    }
                });

        PLVRememberState.rememberStateOf(this, "updateVisibility")
                .compareLastAndSet(isVisible)
                .ifNotEquals(new Function1<PLVRememberStateCompareResult, Unit>() {
                    @Override
                    public Unit invoke(PLVRememberStateCompareResult result) {
                        updateVisibility();
                        return null;
                    }
                });
    }

    private void updateEnableState() {
        if (isEnable) {
            subtitleActionIv.setImageResource(R.drawable.plv_media_player_more_subtitle_action_icon_port_enabled);
        } else {
            subtitleActionIv.setImageResource(R.drawable.plv_media_player_more_subtitle_action_icon_port_disabled);
        }
    }

    private void updateVisibility() {
        setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onClick(View v) {
        requireNotNull(PLVMediaPlayerLocalProvider.localDependScope.on(this).current())
                .get(PLVMPMediaControllerViewModel.class)
                .popFloatActionLayout();
        requireNotNull(PLVMediaPlayerLocalProvider.localDependScope.on(this).current())
                .get(PLVMPMediaControllerViewModel.class)
                .pushFloatActionLayout(PLVMPMediaControllerFloatAction.SUBTITLE);
    }

}
