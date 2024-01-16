package net.polyv.android.player.common.ui.component;

import static com.plv.foundationsdk.component.livedata.PLVLiveDataExt.observeUntilViewDetached;
import static com.plv.foundationsdk.utils.PLVSugarUtil.requireNotNull;

import androidx.lifecycle.Observer;
import android.content.Context;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.plv.foundationsdk.component.remember.PLVRememberState;
import com.plv.foundationsdk.component.remember.PLVRememberStateCompareResult;
import com.plv.foundationsdk.utils.PLVSugarUtil;
import com.plv.foundationsdk.utils.PLVTimeUtils;

import net.polyv.android.player.business.scene.common.model.api.vo.PLVVodVideoJsonVO;
import net.polyv.android.player.common.R;
import net.polyv.android.player.common.ui.localprovider.PLVMediaPlayerLocalProvider;
import net.polyv.android.player.common.ui.viewmodel.viewstate.PLVMediaPlayerControlViewState;

/**
 * @author Hoshiiro
 */
public class PLVMediaPlayerSeekProgressPreviewTextView extends FrameLayout {

    private TextView progressTv;
    private TextView durationTv;

    protected long currentDuration = 0;
    protected PLVMediaPlayerControlViewState currentControlViewState = null;
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

        observeUntilViewDetached(
                requireNotNull(PLVMediaPlayerLocalProvider.localMediaPlayer.on(this).current())
                        .getBusinessListenerRegistry()
                        .getVodVideoJson(),
                this,
                new Observer<PLVVodVideoJsonVO>() {
                    @Override
                    public void onChanged(@Nullable @org.jetbrains.annotations.Nullable PLVVodVideoJsonVO videoJsonVO) {
                        hasProgressImage = videoJsonVO != null && videoJsonVO.getProgressImage() != null;
                        onViewStateChanged();
                    }
                }
        );

        observeUntilViewDetached(
                requireNotNull(PLVMediaPlayerLocalProvider.localMediaPlayer.on(this).current())
                        .getStateListenerRegistry()
                        .getDurationState(),
                this,
                new Observer<Long>() {
                    @Override
                    public void onChanged(@Nullable @org.jetbrains.annotations.Nullable Long duration) {
                        currentDuration = duration == null ? 0 : duration;
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
        PLVRememberState.rememberStateOf(this, "onChangeTextSize")
                .compareLastAndSet(hasProgressImage)
                .ifNotEquals(new PLVSugarUtil.Consumer<PLVRememberStateCompareResult>() {
                    @Override
                    public void accept(PLVRememberStateCompareResult plvRememberStateCompareResult) {
                        onChangeTextSize();
                    }
                });

        PLVRememberState.rememberStateOf(this, "onChangeProgress")
                .compareLastAndSet(currentControlViewState)
                .ifNotEquals(new PLVSugarUtil.Consumer<PLVRememberStateCompareResult>() {
                    @Override
                    public void accept(PLVRememberStateCompareResult result) {
                        onChangeProgress();
                    }
                });

        PLVRememberState.rememberStateOf(this, "onChangeDuration")
                .compareLastAndSet(currentDuration)
                .ifNotEquals(new PLVSugarUtil.Consumer<PLVRememberStateCompareResult>() {
                    @Override
                    public void accept(PLVRememberStateCompareResult result) {
                        onChangeDuration();
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

    protected void onChangeTextSize() {
        final int textSizeSp = hasProgressImage ? 20 : 28;
        progressTv.setTextSize(textSizeSp);
        durationTv.setTextSize(textSizeSp);
    }

    protected void onChangeProgress() {
        if (currentControlViewState != null && currentControlViewState.progressSeekBarDragging) {
            progressTv.setText(PLVTimeUtils.generateTime(currentControlViewState.progressSeekBarDragPosition));
        }
    }

    protected void onChangeDuration() {
        durationTv.setText(PLVTimeUtils.generateTime(currentDuration));
    }

    protected void onChangeVisibility() {
        if (currentControlViewState == null) {
            return;
        }
        final boolean visible = currentControlViewState.progressSeekBarDragging;
        setVisibility(visible ? View.VISIBLE : View.GONE);
    }

}
