package net.polyv.android.player.common.ui.component;

import static com.plv.foundationsdk.component.livedata.PLVLiveDataExt.observeUntilViewDetached;
import static com.plv.foundationsdk.utils.PLVAppUtils.postToMainThread;
import static com.plv.foundationsdk.utils.PLVSugarUtil.requireNotNull;

import android.arch.lifecycle.Observer;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.plv.foundationsdk.component.remember.PLVRememberState;
import com.plv.foundationsdk.component.remember.PLVRememberStateCompareResult;
import com.plv.foundationsdk.utils.PLVSugarUtil;

import net.polyv.android.player.business.scene.common.model.api.vo.PLVVodVideoJsonVO;
import net.polyv.android.player.common.R;
import net.polyv.android.player.common.ui.localprovider.PLVMediaPlayerLocalProvider;
import net.polyv.android.player.common.ui.viewmodel.viewstate.PLVMediaPlayerControlViewState;
import net.polyv.android.player.common.utils.ui.PLVRoundRectConstraintLayout;
import net.polyv.android.player.common.utils.ui.image.glide.decoder.PLVSeekProgressPreviewImageDecoder;

import org.jetbrains.annotations.NotNull;

/**
 * @author Hoshiiro
 */
public class PLVMediaPlayerSeekProgressPreviewLayout extends FrameLayout {

    private PLVMediaPlayerSeekProgressPreviewTextView seekProgressPreviewTv;
    private PLVRoundRectConstraintLayout seekProgressPreviewImageLayout;
    private ImageView seekProgressPreviewIv;

    protected boolean isProgressSeekBarDragging = false;
    protected long progressSeekBarPosition = 0;
    protected PLVVodVideoJsonVO videoJson = null;

    private boolean isLastLoadImageFinish = true;
    private boolean needUpdateImageOnLoadImageFinish = false;

    public PLVMediaPlayerSeekProgressPreviewLayout(@NonNull Context context) {
        super(context);
        initView();
    }

    public PLVMediaPlayerSeekProgressPreviewLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public PLVMediaPlayerSeekProgressPreviewLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.plv_media_player_ui_component_progress_preview_image_layout, this);

        seekProgressPreviewTv = findViewById(R.id.plv_media_player_seek_progress_preview_tv);
        seekProgressPreviewImageLayout = findViewById(R.id.plv_media_player_seek_progress_preview_image_layout);
        seekProgressPreviewIv = findViewById(R.id.plv_media_player_seek_progress_preview_iv);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (isInEditMode()) return;

        observeUntilViewDetached(
                requireNotNull(PLVMediaPlayerLocalProvider.localControlViewModel.on(this).current())
                        .getControlViewStateLiveData(),
                this,
                new Observer<PLVMediaPlayerControlViewState>() {
                    @Override
                    public void onChanged(@Nullable @org.jetbrains.annotations.Nullable PLVMediaPlayerControlViewState viewState) {
                        if (viewState == null) {
                            return;
                        }
                        isProgressSeekBarDragging = viewState.progressSeekBarDragging;
                        progressSeekBarPosition = viewState.progressSeekBarDragPosition;
                        onViewStateChanged();
                    }
                }
        );

        observeUntilViewDetached(
                requireNotNull(PLVMediaPlayerLocalProvider.localMediaPlayer.on(this).current())
                        .getBusinessListenerRegistry()
                        .getVodVideoJson(),
                this,
                new Observer<PLVVodVideoJsonVO>() {
                    @Override
                    public void onChanged(@Nullable @org.jetbrains.annotations.Nullable PLVVodVideoJsonVO videoJsonVO) {
                        videoJson = videoJsonVO;
                        onViewStateChanged();
                    }
                }
        );
    }

    protected void onViewStateChanged() {
        PLVRememberState.rememberStateOf(this, "updatePreviewImage")
                .compareLastAndSet(isProgressSeekBarDragging, progressSeekBarPosition, videoJson)
                .ifNotEquals(new PLVSugarUtil.Consumer<PLVRememberStateCompareResult>() {
                    @Override
                    public void accept(PLVRememberStateCompareResult plvRememberStateCompareResult) {
                        updatePreviewImage();
                    }
                });
    }

    protected void updatePreviewImage() {
        final boolean isVisible = isProgressSeekBarDragging
                && videoJson != null
                && videoJson.getProgressImage() != null;
        if (!isVisible) {
            seekProgressPreviewImageLayout.setVisibility(GONE);
            return;
        }
        seekProgressPreviewImageLayout.setVisibility(VISIBLE);
        innerUpdateImage();
    }

    private void innerUpdateImage() {
        if (!isLastLoadImageFinish) {
            needUpdateImageOnLoadImageFinish = true;
            return;
        }

        isLastLoadImageFinish = false;
        needUpdateImageOnLoadImageFinish = false;
        Glide.with(this)
                .load(videoJson.getProgressImage())
                .apply(
                        RequestOptions
                                .option(PLVSeekProgressPreviewImageDecoder.USE_SEEK_PROGRESS_PREVIEW_IMAGE_DECODER, true)
                                .set(
                                        PLVSeekProgressPreviewImageDecoder.SEEK_PROGRESS_PREVIEW_IMAGE_INDEX,
                                        PLVSeekProgressPreviewImageDecoder.calculateIndex(progressSeekBarPosition / 1000, videoJson)
                                )
                )
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable @org.jetbrains.annotations.Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        onLoadFinish();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        onLoadFinish();
                        return false;
                    }

                    private void onLoadFinish() {
                        postToMainThread(new Runnable() {
                            @Override
                            public void run() {
                                isLastLoadImageFinish = true;
                                if (needUpdateImageOnLoadImageFinish) {
                                    innerUpdateImage();
                                }
                            }
                        });
                    }
                })
                .into(new SimpleTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull @NotNull Drawable resource, @Nullable @org.jetbrains.annotations.Nullable Transition<? super Drawable> transition) {
                        seekProgressPreviewIv.setImageDrawable(resource);
                    }
                });
    }

}
