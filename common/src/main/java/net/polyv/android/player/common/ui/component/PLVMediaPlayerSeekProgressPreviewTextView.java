package net.polyv.android.player.common.ui.component;

import static net.polyv.android.player.sdk.foundation.lang.PreconditionsKt.requireNotNull;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import net.polyv.android.player.common.R;
import net.polyv.android.player.common.di.PLVMediaPlayerLocalProvider;
import net.polyv.android.player.common.modules.media.viewmodel.PLVMPMediaViewModel;
import net.polyv.android.player.common.modules.media.viewmodel.viewstate.PLVMPMediaInfoViewState;
import net.polyv.android.player.common.modules.media.viewmodel.viewstate.PLVMPMediaPlayViewState;
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.PLVMPMediaControllerViewModel;
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.viewstate.PLVMPMediaControllerViewState;
import net.polyv.android.player.common.utils.data.PLVTimeUtils;
import net.polyv.android.player.sdk.foundation.lang.PLVRememberState;
import net.polyv.android.player.sdk.foundation.lang.PLVRememberStateCompareResult;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

/**
 * @author Hoshiiro
 */
public class PLVMediaPlayerSeekProgressPreviewTextView extends FrameLayout {

    private TextView progressTv;
    private TextView durationTv;

    protected long currentDuration = 0;
    protected long dragPosition = 0;
    protected boolean isVisible = false;
    protected boolean hasProgressImage = false;

    public PLVMediaPlayerSeekProgressPreviewTextView(Context context) {
        super(context);
    }

    public PLVMediaPlayerSeekProgressPreviewTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PLVMediaPlayerSeekProgressPreviewTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    {
        LayoutInflater.from(getContext()).inflate(R.layout.plv_media_player_ui_component_preview_image_progress_text_layout, this);
        progressTv = findViewById(R.id.plv_media_player_ui_component_media_progress_text_progress);
        durationTv = findViewById(R.id.plv_media_player_ui_component_media_progress_text_duration);
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
                        hasProgressImage = viewState.getProgressPreviewImage() != null;
                        onViewStateChanged();
                        return null;
                    }
                });

        requireNotNull(PLVMediaPlayerLocalProvider.localDependScope.on(this).current())
                .get(PLVMPMediaViewModel.class)
                .getMediaPlayViewState()
                .observeUntilViewDetached(this, new Function1<PLVMPMediaPlayViewState, Unit>() {
                    @Override
                    public Unit invoke(PLVMPMediaPlayViewState viewState) {
                        currentDuration = viewState.getDuration();
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
                        isVisible = viewState.getProgressSeekBarDragging();
                        dragPosition = viewState.getProgressSeekBarDragPosition();
                        onViewStateChanged();
                        return null;
                    }
                });
    }

    protected void onViewStateChanged() {
        PLVRememberState.rememberStateOf(this, "onChangeTextSize")
                .compareLastAndSet(hasProgressImage)
                .ifNotEquals(new Function1<PLVRememberStateCompareResult, Unit>() {
                    @Override
                    public Unit invoke(PLVRememberStateCompareResult result) {
                        onChangeTextSize();
                        return null;
                    }
                });

        PLVRememberState.rememberStateOf(this, "onChangeProgress")
                .compareLastAndSet(dragPosition)
                .ifNotEquals(new Function1<PLVRememberStateCompareResult, Unit>() {
                    @Override
                    public Unit invoke(PLVRememberStateCompareResult result) {
                        onChangeProgress();
                        return null;
                    }
                });

        PLVRememberState.rememberStateOf(this, "onChangeDuration")
                .compareLastAndSet(currentDuration)
                .ifNotEquals(new Function1<PLVRememberStateCompareResult, Unit>() {
                    @Override
                    public Unit invoke(PLVRememberStateCompareResult result) {
                        onChangeDuration();
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

    protected void onChangeTextSize() {
        final int textSizeSp = hasProgressImage ? 20 : 28;
        progressTv.setTextSize(textSizeSp);
        durationTv.setTextSize(textSizeSp);
    }

    protected void onChangeProgress() {
        progressTv.setText(PLVTimeUtils.formatTime(dragPosition));
    }

    protected void onChangeDuration() {
        durationTv.setText(PLVTimeUtils.formatTime(currentDuration));
    }

    protected void onChangeVisibility() {
        setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

}
