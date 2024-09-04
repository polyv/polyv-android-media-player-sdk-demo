package net.polyv.android.player.common.ui.component;

import static net.polyv.android.player.sdk.foundation.lang.PreconditionsKt.requireNotNull;
import static net.polyv.android.player.sdk.foundation.lang.ThreadsKt.postToMainThread;

import android.content.Context;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

import net.polyv.android.player.common.R;
import net.polyv.android.player.common.di.PLVMediaPlayerLocalProvider;
import net.polyv.android.player.common.modules.media.viewmodel.PLVMPMediaViewModel;
import net.polyv.android.player.common.modules.media.viewmodel.viewstate.PLVMPMediaInfoViewState;
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.PLVMPMediaControllerViewModel;
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.viewstate.PLVMPMediaControllerViewState;
import net.polyv.android.player.common.utils.ui.PLVRoundRectConstraintLayout;
import net.polyv.android.player.common.utils.ui.image.glide.decoder.PLVSeekProgressPreviewImageDecoder;
import net.polyv.android.player.sdk.foundation.lang.Duration;
import net.polyv.android.player.sdk.foundation.lang.PLVRememberState;
import net.polyv.android.player.sdk.foundation.lang.PLVRememberStateCompareResult;

import org.jetbrains.annotations.NotNull;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;

/**
 * @author Hoshiiro
 */
public class PLVMediaPlayerSeekProgressPreviewLayout extends FrameLayout {

    private PLVMediaPlayerSeekProgressPreviewTextView seekProgressPreviewTv;
    private PLVRoundRectConstraintLayout seekProgressPreviewImageLayout;
    private ImageView seekProgressPreviewIv;

    protected boolean isProgressSeekBarDragging = false;
    protected long progressSeekBarPosition = 0;
    protected String previewImageUrl = null;
    protected Duration previewImageInterval = null;

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

        requireNotNull(PLVMediaPlayerLocalProvider.localDependScope.on(this).current())
                .get(PLVMPMediaControllerViewModel.class)
                .getMediaControllerViewState()
                .observeUntilViewDetached(this, new Function1<PLVMPMediaControllerViewState, Unit>() {
                    @Override
                    public Unit invoke(PLVMPMediaControllerViewState viewState) {
                        isProgressSeekBarDragging = viewState.getProgressSeekBarDragging();
                        progressSeekBarPosition = viewState.getProgressSeekBarDragPosition();
                        onViewStateChanged();
                        return null;
                    }
                });

        requireNotNull(PLVMediaPlayerLocalProvider.localDependScope.on(this).current())
                .get(PLVMPMediaViewModel.class)
                .getMediaInfoViewState()
                .observeUntilViewDetached(this, new Function1<PLVMPMediaInfoViewState, Unit>() {
                    @Override
                    public Unit invoke(PLVMPMediaInfoViewState viewState) {
                        previewImageUrl = viewState.getProgressPreviewImage();
                        previewImageInterval = viewState.getProgressPreviewImageInterval();
                        onViewStateChanged();
                        return null;
                    }
                });
    }

    protected void onViewStateChanged() {
        PLVRememberState.rememberStateOf(this, "updatePreviewImage")
                .compareLastAndSet(isProgressSeekBarDragging, progressSeekBarPosition, previewImageUrl, previewImageInterval)
                .ifNotEquals(new Function1<PLVRememberStateCompareResult, Unit>() {
                    @Override
                    public Unit invoke(PLVRememberStateCompareResult result) {
                        updatePreviewImage();
                        return null;
                    }
                });
    }

    protected void updatePreviewImage() {
        final boolean isVisible = isProgressSeekBarDragging
                && previewImageUrl != null
                && previewImageInterval != null;
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
                .load(previewImageUrl)
                .apply(
                        RequestOptions
                                .option(PLVSeekProgressPreviewImageDecoder.USE_SEEK_PROGRESS_PREVIEW_IMAGE_DECODER, true)
                                .set(
                                        PLVSeekProgressPreviewImageDecoder.SEEK_PROGRESS_PREVIEW_IMAGE_INDEX,
                                        PLVSeekProgressPreviewImageDecoder.calculateIndex(progressSeekBarPosition / 1000, previewImageInterval)
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
                        postToMainThread(new Function0<Unit>() {
                            @Override
                            public Unit invoke() {
                                isLastLoadImageFinish = true;
                                if (needUpdateImageOnLoadImageFinish) {
                                    innerUpdateImage();
                                }
                                return null;
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
