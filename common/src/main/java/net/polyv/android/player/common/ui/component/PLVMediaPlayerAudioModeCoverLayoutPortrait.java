package net.polyv.android.player.common.ui.component;

import static com.plv.foundationsdk.component.livedata.PLVLiveDataExt.observeUntilViewDetached;
import static com.plv.foundationsdk.utils.PLVSugarUtil.nullable;
import static com.plv.foundationsdk.utils.PLVSugarUtil.requireNotNull;
import static com.plv.foundationsdk.utils.PLVTimeUnit.seconds;

import android.animation.ObjectAnimator;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import com.plv.foundationsdk.component.remember.PLVRememberState;
import com.plv.foundationsdk.component.remember.PLVRememberStateCompareResult;
import com.plv.foundationsdk.utils.PLVSugarUtil;

import net.polyv.android.player.business.scene.common.model.api.vo.PLVVodVideoJsonVO;
import net.polyv.android.player.business.scene.common.model.vo.PLVMediaOutputMode;
import net.polyv.android.player.business.scene.common.player.IPLVMediaPlayer;
import net.polyv.android.player.common.R;
import net.polyv.android.player.common.ui.localprovider.PLVMediaPlayerLocalProvider;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * @author Hoshiiro
 */
public class PLVMediaPlayerAudioModeCoverLayoutPortrait extends FrameLayout implements View.OnClickListener {

    private static final Interpolator audioRotationInterpolator = new LinearInterpolator();

    private View audioModeImageWrap;
    private CircleImageView audioModeImageIv;
    private TextView audioModeHintTv;
    private ImageView audioModeHintIv;
    private LinearLayout switchVideoModeLl;

    protected PLVMediaOutputMode currentMediaOutputMode = null;
    protected PLVVodVideoJsonVO currentVodVideoJson = null;

    private ObjectAnimator animator;

    public PLVMediaPlayerAudioModeCoverLayoutPortrait(@NonNull Context context) {
        super(context);
    }

    public PLVMediaPlayerAudioModeCoverLayoutPortrait(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PLVMediaPlayerAudioModeCoverLayoutPortrait(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    {
        LayoutInflater.from(getContext()).inflate(R.layout.plv_media_player_ui_component_audio_mode_cover_layout_portrait, this);
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
                requireNotNull(PLVMediaPlayerLocalProvider.localMediaPlayer.on(this).current())
                        .getBusinessListenerRegistry()
                        .getVodVideoJson(),
                this,
                new Observer<PLVVodVideoJsonVO>() {
                    @Override
                    public void onChanged(@Nullable @org.jetbrains.annotations.Nullable PLVVodVideoJsonVO vodVideoJson) {
                        currentVodVideoJson = vodVideoJson;
                        onViewStateChanged();
                    }
                }
        );
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
                .compareLastAndSet(currentVodVideoJson)
                .ifNotEquals(new PLVSugarUtil.Consumer<PLVRememberStateCompareResult>() {
                    @Override
                    public void accept(PLVRememberStateCompareResult result) {
                        onLoadFirstImage();
                    }
                });

        PLVRememberState.rememberStateOf(this, "onChangeVisibility")
                .compareLastAndSet(currentMediaOutputMode)
                .ifNotEquals(new PLVSugarUtil.Consumer<PLVRememberStateCompareResult>() {
                    @Override
                    public void accept(PLVRememberStateCompareResult result) {
                        onChangeVisibility();
                    }
                });
    }

    protected void onLoadFirstImage() {
        String url = nullable(new PLVSugarUtil.Supplier<String>() {
            @Override
            public String get() {
                return currentVodVideoJson.getFirst_image();
            }
        });
        audioModeImageIv.setImageDrawable(null);
        Glide.with(audioModeImageIv)
                .load(url)
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
            IPLVMediaPlayer mediaPlayer = PLVMediaPlayerLocalProvider.localMediaPlayer.on(this).current();
            if (mediaPlayer == null) {
                return;
            }
            boolean currentIsAudioMode = mediaPlayer.getBusinessListenerRegistry().getCurrentMediaOutputMode().getValue() == PLVMediaOutputMode.AUDIO_ONLY;
            if (currentIsAudioMode) {
                mediaPlayer.changeMediaOutputMode(PLVMediaOutputMode.AUDIO_VIDEO);
            }
        }
    }

}
