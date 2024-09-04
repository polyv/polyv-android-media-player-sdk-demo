package net.polyv.android.player.common.ui.component;

import static net.polyv.android.player.sdk.foundation.lang.PreconditionsKt.requireNotNull;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import net.polyv.android.player.common.R;
import net.polyv.android.player.common.di.PLVMediaPlayerLocalProvider;
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.PLVMPMediaControllerViewModel;
import net.polyv.android.player.common.utils.ui.PLVViewUtil;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

/**
 * @author Hoshiiro
 */
public class PLVMediaPlayerBrightnessVolumeHintLayout extends FrameLayout {

    private ImageView brightnessVolumeIv;
    private ProgressBar brightnessVolumeProgress;

    public PLVMediaPlayerBrightnessVolumeHintLayout(@NonNull Context context) {
        super(context);
    }

    public PLVMediaPlayerBrightnessVolumeHintLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PLVMediaPlayerBrightnessVolumeHintLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    {
        LayoutInflater.from(getContext()).inflate(R.layout.plv_media_player_ui_component_brightness_volume_hint_layout, this);
        brightnessVolumeIv = findViewById(R.id.plv_media_player_brightness_volume_iv);
        brightnessVolumeProgress = findViewById(R.id.plv_media_player_brightness_volume_progress);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (isInEditMode()) return;

        requireNotNull(PLVMediaPlayerLocalProvider.localDependScope.on(this).current())
                .get(PLVMPMediaControllerViewModel.class)
                .getBrightnessUpdateEvent()
                .observeUntilViewDetached(this, new Function1<Integer, Unit>() {
                    @Override
                    public Unit invoke(Integer brightness) {
                        showBrightnessChanged(brightness);
                        return null;
                    }
                });

        requireNotNull(PLVMediaPlayerLocalProvider.localDependScope.on(this).current())
                .get(PLVMPMediaControllerViewModel.class)
                .getVolumeUpdateEvent()
                .observeUntilViewDetached(this, new Function1<Integer, Unit>() {
                    @Override
                    public Unit invoke(Integer volume) {
                        showVolumeChanged(volume);
                        return null;
                    }
                });
    }

    protected void showBrightnessChanged(int brightness) {
        brightnessVolumeIv.setImageResource(R.drawable.plv_media_player_brightness_hint_icon);
        brightnessVolumeProgress.setProgress(brightness);
        PLVViewUtil.showViewForDuration(this, 2000);
    }

    protected void showVolumeChanged(int volume) {
        brightnessVolumeIv.setImageResource(R.drawable.plv_media_player_volume_hint_icon);
        brightnessVolumeProgress.setProgress(volume);
        PLVViewUtil.showViewForDuration(this, 2000);
    }

}
