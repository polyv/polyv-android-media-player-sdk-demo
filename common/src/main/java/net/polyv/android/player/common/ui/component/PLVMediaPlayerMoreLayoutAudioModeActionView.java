package net.polyv.android.player.common.ui.component;

import static net.polyv.android.player.sdk.foundation.graphics.ColorsKt.parseColor;
import static net.polyv.android.player.sdk.foundation.lang.PreconditionsKt.requireNotNull;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import net.polyv.android.player.business.scene.common.model.vo.PLVMediaOutputMode;
import net.polyv.android.player.common.R;
import net.polyv.android.player.common.di.PLVMediaPlayerLocalProvider;
import net.polyv.android.player.common.modules.media.viewmodel.PLVMPMediaViewModel;
import net.polyv.android.player.common.modules.media.viewmodel.viewstate.PLVMPMediaInfoViewState;
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.PLVMPMediaControllerViewModel;
import net.polyv.android.player.sdk.foundation.lang.PLVRememberState;
import net.polyv.android.player.sdk.foundation.lang.PLVRememberStateCompareResult;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

/**
 * @author Hoshiiro
 */
public class PLVMediaPlayerMoreLayoutAudioModeActionView extends FrameLayout implements View.OnClickListener {

    private ImageView audioModeActionIv;
    private TextView audioModeActionTv;

    private int tintColorIconNormal = Color.WHITE;
    private int tintColorIconSelected = parseColor("#3F76FC");
    private int textColorNormal = parseColor("#CCFFFFFF");
    private int textColorSelected = parseColor("#CC3F76FC");

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

        requireNotNull(PLVMediaPlayerLocalProvider.localDependScope.on(this).current())
                .get(PLVMPMediaViewModel.class)
                .getMediaInfoViewState()
                .observeUntilViewDetached(this, new Function1<PLVMPMediaInfoViewState, Unit>() {
                    @Override
                    public Unit invoke(PLVMPMediaInfoViewState viewState) {
                        currentMediaOutputMode = viewState.getOutputMode();
                        onViewStateChanged();
                        return null;
                    }
                });
    }

    protected void onViewStateChanged() {
        PLVRememberState.rememberStateOf(this, "onChangeColor")
                .compareLastAndSet(currentMediaOutputMode)
                .ifNotEquals(new Function1<PLVRememberStateCompareResult, Unit>() {
                    @Override
                    public Unit invoke(PLVRememberStateCompareResult result) {
                        onChangeColor();
                        return null;
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
        boolean currentIsAudioMode = currentMediaOutputMode == PLVMediaOutputMode.AUDIO_ONLY;
        requireNotNull(PLVMediaPlayerLocalProvider.localDependScope.on(this).current())
                .get(PLVMPMediaViewModel.class)
                .changeMediaOutputMode(currentIsAudioMode ? PLVMediaOutputMode.AUDIO_VIDEO : PLVMediaOutputMode.AUDIO_ONLY);
        requireNotNull(PLVMediaPlayerLocalProvider.localDependScope.on(this).current())
                .get(PLVMPMediaControllerViewModel.class)
                .popFloatActionLayout();
    }

}
