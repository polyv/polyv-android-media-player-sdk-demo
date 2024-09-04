package net.polyv.android.player.common.ui.component;

import static net.polyv.android.player.sdk.foundation.lang.Duration.seconds;
import static net.polyv.android.player.sdk.foundation.lang.PreconditionsKt.requireNotNull;

import android.animation.ObjectAnimator;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import net.polyv.android.player.business.scene.common.model.vo.PLVMediaOutputMode;
import net.polyv.android.player.common.R;
import net.polyv.android.player.common.di.PLVMediaPlayerLocalProvider;
import net.polyv.android.player.common.modules.media.viewmodel.PLVMPMediaViewModel;
import net.polyv.android.player.common.modules.media.viewmodel.viewstate.PLVMPMediaInfoViewState;
import net.polyv.android.player.sdk.foundation.lang.PLVRememberState;
import net.polyv.android.player.sdk.foundation.lang.PLVRememberStateCompareResult;

import de.hdodenhof.circleimageview.CircleImageView;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

/**
 * @author Hoshiiro
 */
public class PLVMediaPlayerAudioModeCoverLayoutLandscape extends FrameLayout implements View.OnClickListener {

    private static final Interpolator audioRotationInterpolator = new LinearInterpolator();

    private View audioModeImageWrap;
    private CircleImageView audioModeImageIv;
    private TextView audioModeHintTv;
    private ImageView audioModeHintIv;
    private LinearLayout switchVideoModeLl;

    protected PLVMediaOutputMode currentMediaOutputMode = null;
    protected String audioCoverImageUrl = null;

    private ObjectAnimator animator = null;

    public PLVMediaPlayerAudioModeCoverLayoutLandscape(@NonNull Context context) {
        super(context);
    }

    public PLVMediaPlayerAudioModeCoverLayoutLandscape(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PLVMediaPlayerAudioModeCoverLayoutLandscape(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    {
        LayoutInflater.from(getContext()).inflate(R.layout.plv_media_player_ui_component_audio_mode_cover_layout_landscape, this);
        audioModeImageWrap = findViewById(R.id.plv_media_player_audio_mode_image_wrap);
        audioModeImageIv = findViewById(R.id.plv_media_player_audio_mode_image_iv);
        audioModeHintTv = findViewById(R.id.plv_media_player_audio_mode_hint_tv);
        audioModeHintIv = findViewById(R.id.plv_media_player_audio_mode_hint_iv);
        switchVideoModeLl = findViewById(R.id.plv_media_player_switch_video_mode_ll);

        switchVideoModeLl.setOnClickListener(this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (isInEditMode()) return;

        rotateAudioModeImage();

        requireNotNull(PLVMediaPlayerLocalProvider.localDependScope.on(this).current())
                .get(PLVMPMediaViewModel.class)
                .getMediaInfoViewState()
                .observeUntilViewDetached(this, new Function1<PLVMPMediaInfoViewState, Unit>() {
                    @Override
                    public Unit invoke(PLVMPMediaInfoViewState viewState) {
                        currentMediaOutputMode = viewState.getOutputMode();
                        audioCoverImageUrl = viewState.getAudioModeCoverImage();
                        onViewStateChanged();
                        return null;
                    }
                });
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (animator != null) {
            animator.cancel();
            animator = null;
        }
    }

    protected void rotateAudioModeImage() {
        animator = ObjectAnimator.ofFloat(audioModeImageIv, "rotation", audioModeImageIv.getRotation(), audioModeImageIv.getRotation() + 360F);
        animator.setDuration(seconds(30).toMillis());
        animator.setRepeatCount(ObjectAnimator.INFINITE);
        animator.setInterpolator(audioRotationInterpolator);
        animator.start();
    }

    protected void onViewStateChanged() {
        PLVRememberState.rememberStateOf(this, "onLoadFirstImage")
                .compareLastAndSet(audioCoverImageUrl)
                .ifNotEquals(new Function1<PLVRememberStateCompareResult, Unit>() {
                    @Override
                    public Unit invoke(PLVRememberStateCompareResult result) {
                        onLoadFirstImage();
                        return null;
                    }
                });

        PLVRememberState.rememberStateOf(this, "onChangeVisibility")
                .compareLastAndSet(currentMediaOutputMode)
                .ifNotEquals(new Function1<PLVRememberStateCompareResult, Unit>() {
                    @Override
                    public Unit invoke(PLVRememberStateCompareResult result) {
                        onChangeVisibility();
                        return null;
                    }
                });
    }

    protected void onLoadFirstImage() {
        audioModeImageIv.setImageDrawable(null);
        if (audioCoverImageUrl == null) {
            audioModeImageIv.setImageResource(R.drawable.plv_media_player_audio_mode_cover_placeholder);
            return;
        }
        Glide.with(audioModeImageIv)
                .load(audioCoverImageUrl)
                .apply(new RequestOptions()
                        .placeholder(R.drawable.plv_media_player_audio_mode_cover_placeholder)
                        .error(R.drawable.plv_media_player_audio_mode_cover_placeholder)
                )
                .into(audioModeImageIv);
    }

    protected void onChangeVisibility() {
        if (currentMediaOutputMode == PLVMediaOutputMode.AUDIO_ONLY) {
            setVisibility(View.VISIBLE);
        } else {
            setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == switchVideoModeLl.getId()) {
            requireNotNull(PLVMediaPlayerLocalProvider.localDependScope.on(this).current())
                    .get(PLVMPMediaViewModel.class)
                    .changeMediaOutputMode(PLVMediaOutputMode.AUDIO_VIDEO);
        }
    }

}
