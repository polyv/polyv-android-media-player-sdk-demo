package net.polyv.android.player.common.ui.component;

import static com.plv.foundationsdk.component.livedata.PLVLiveDataExt.observeUntilViewDetached;
import static com.plv.foundationsdk.utils.PLVSugarUtil.requireNotNull;

import androidx.lifecycle.Observer;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.plv.foundationsdk.component.remember.PLVRememberState;
import com.plv.foundationsdk.component.remember.PLVRememberStateCompareResult;
import com.plv.foundationsdk.utils.PLVFormatUtils;
import com.plv.foundationsdk.utils.PLVSugarUtil;

import net.polyv.android.player.business.scene.common.model.vo.PLVMediaOutputMode;
import net.polyv.android.player.business.scene.common.player.IPLVMediaPlayer;
import net.polyv.android.player.common.R;
import net.polyv.android.player.common.ui.localprovider.PLVMediaPlayerLocalProvider;
import net.polyv.android.player.common.ui.viewmodel.PLVMediaPlayerControlViewModel;
import net.polyv.android.player.common.ui.viewmodel.action.PLVMediaPlayerControlAction;

/**
 * @author Hoshiiro
 */
public class PLVMediaPlayerMoreLayoutAudioModeActionView extends FrameLayout implements View.OnClickListener {

    private ImageView audioModeActionIv;
    private TextView audioModeActionTv;

    private int tintColorIconNormal = Color.WHITE;
    private int tintColorIconSelected = PLVFormatUtils.parseColor("#3F76FC");
    private int textColorNormal = PLVFormatUtils.parseColor("#CCFFFFFF");
    private int textColorSelected = PLVFormatUtils.parseColor("#CC3F76FC");

    protected PLVMediaOutputMode currentMediaOutputMode = null;

    public PLVMediaPlayerMoreLayoutAudioModeActionView(@NonNull Context context) {
        super(context);
        init(null);
    }

    public PLVMediaPlayerMoreLayoutAudioModeActionView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public PLVMediaPlayerMoreLayoutAudioModeActionView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(@Nullable AttributeSet attrs) {
        LayoutInflater.from(getContext()).inflate(R.layout.plv_media_player_ui_component_more_audio_mode_action_layout, this);
        audioModeActionIv = findViewById(R.id.plv_media_player_audio_mode_action_iv);
        audioModeActionTv = findViewById(R.id.plv_media_player_audio_mode_action_tv);

        parseAttrs(attrs);

        setOnClickListener(this);
    }

    private void parseAttrs(@Nullable AttributeSet attrs) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.PLVMediaPlayerMoreLayoutAudioModeActionView);
        tintColorIconNormal = typedArray.getColor(R.styleable.PLVMediaPlayerMoreLayoutAudioModeActionView_plvIconTintNormal, tintColorIconNormal);
        tintColorIconSelected = typedArray.getColor(R.styleable.PLVMediaPlayerMoreLayoutAudioModeActionView_plvIconTintSelected, tintColorIconSelected);
        textColorNormal = typedArray.getColor(R.styleable.PLVMediaPlayerMoreLayoutAudioModeActionView_plvTextColorNormal, textColorNormal);
        textColorSelected = typedArray.getColor(R.styleable.PLVMediaPlayerMoreLayoutAudioModeActionView_plvTextColorSelected, textColorSelected);
        typedArray.recycle();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (isInEditMode()) return;

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
    }

    protected void onViewStateChanged() {
        PLVRememberState.rememberStateOf(this, "onChangeColor")
                .compareLastAndSet(currentMediaOutputMode)
                .ifNotEquals(new PLVSugarUtil.Consumer<PLVRememberStateCompareResult>() {
                    @Override
                    public void accept(PLVRememberStateCompareResult result) {
                        onChangeColor();
                    }
                });
    }

    protected void onChangeColor() {
        if (currentMediaOutputMode == PLVMediaOutputMode.AUDIO_ONLY) {
            audioModeActionIv.setImageTintList(ColorStateList.valueOf(tintColorIconSelected));
            audioModeActionTv.setTextColor(textColorSelected);
        } else {
            audioModeActionIv.setImageTintList(ColorStateList.valueOf(tintColorIconNormal));
            audioModeActionTv.setTextColor(textColorNormal);
        }
    }

    @Override
    public void onClick(View v) {
        IPLVMediaPlayer mediaPlayer = PLVMediaPlayerLocalProvider.localMediaPlayer.on(PLVMediaPlayerMoreLayoutAudioModeActionView.this).current();
        PLVMediaPlayerControlViewModel viewModel = PLVMediaPlayerLocalProvider.localControlViewModel.on(PLVMediaPlayerMoreLayoutAudioModeActionView.this).current();
        if (mediaPlayer == null || viewModel == null) {
            return;
        }
        boolean currentIsAudioMode = mediaPlayer.getBusinessListenerRegistry().getCurrentMediaOutputMode().getValue() == PLVMediaOutputMode.AUDIO_ONLY;
        if (currentIsAudioMode) {
            mediaPlayer.changeMediaOutputMode(PLVMediaOutputMode.AUDIO_VIDEO);
        } else {
            mediaPlayer.changeMediaOutputMode(PLVMediaOutputMode.AUDIO_ONLY);
        }
        viewModel.requestControl(PLVMediaPlayerControlAction.closeFloatMenuLayout());
    }

}
