package net.polyv.android.player.common.ui.component;

import static com.plv.foundationsdk.component.event.PLVEventKt.observeUntilViewDetached;
import static com.plv.foundationsdk.utils.PLVSugarUtil.requireNotNull;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.plv.foundationsdk.utils.PLVSugarUtil;

import net.polyv.android.player.common.R;
import net.polyv.android.player.common.ui.localprovider.PLVMediaPlayerLocalProvider;
import net.polyv.android.player.common.ui.viewmodel.action.PLVMediaPlayerControlAction;
import net.polyv.android.player.common.utils.ui.PLVViewUtil;

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

        observeUntilViewDetached(
                requireNotNull(PLVMediaPlayerLocalProvider.localControlViewModel.on(this).current())
                        .getControlActionEvent(),
                this,
                new PLVSugarUtil.Consumer<PLVMediaPlayerControlAction>() {
                    @Override
                    public void accept(PLVMediaPlayerControlAction action) {
                        if (action instanceof PLVMediaPlayerControlAction.HintBrightnessChanged) {
                            showBrightnessChanged(((PLVMediaPlayerControlAction.HintBrightnessChanged) action).brightness);
                        } else if (action instanceof PLVMediaPlayerControlAction.HintVolumeChanged) {
                            showVolumeChanged(((PLVMediaPlayerControlAction.HintVolumeChanged) action).volume);
                        }
                    }
                }
        );
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
