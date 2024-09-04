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
import net.polyv.android.player.common.ui.component.floatwindow.PLVMediaPlayerFloatWindowManager;
import net.polyv.android.player.common.utils.floatwindow.enums.PLVFloatWindowLaunchReason;
import net.polyv.android.player.sdk.foundation.lang.PLVRememberState;
import net.polyv.android.player.sdk.foundation.lang.PLVRememberStateCompareResult;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

/**
 * @author Hoshiiro
 */
public class PLVMediaPlayerMoreLayoutFloatWindowActionView extends FrameLayout implements View.OnClickListener {

    private ImageView floatWindowActionIv;
    private TextView floatWindowActionTv;

    private int tintColorIconNormal = Color.WHITE;
    private int tintColorIconSelected = parseColor("#3F76FC");
    private int textColorNormal = parseColor("#CCFFFFFF");
    private int textColorSelected = parseColor("#CC3F76FC");

    protected Boolean currentFloatWindowShowing = null;
    protected PLVMediaOutputMode currentMediaOutputMode = null;

    public PLVMediaPlayerMoreLayoutFloatWindowActionView(@NonNull Context context) {
        super(context);
        init(null);
    }

    public PLVMediaPlayerMoreLayoutFloatWindowActionView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public PLVMediaPlayerMoreLayoutFloatWindowActionView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(@Nullable AttributeSet attrs) {
        LayoutInflater.from(getContext()).inflate(R.layout.plv_media_player_ui_component_more_float_window_action_layout, this);
        floatWindowActionIv = findViewById(R.id.plv_media_player_float_window_action_iv);
        floatWindowActionTv = findViewById(R.id.plv_media_player_float_window_action_tv);

        parseAttrs(attrs);

        setOnClickListener(this);
    }

    private void parseAttrs(@Nullable AttributeSet attrs) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.PLVMediaPlayerMoreLayoutFloatWindowActionView);
        tintColorIconNormal = typedArray.getColor(R.styleable.PLVMediaPlayerMoreLayoutFloatWindowActionView_plvIconTintNormal, tintColorIconNormal);
        tintColorIconSelected = typedArray.getColor(R.styleable.PLVMediaPlayerMoreLayoutFloatWindowActionView_plvIconTintSelected, tintColorIconSelected);
        textColorNormal = typedArray.getColor(R.styleable.PLVMediaPlayerMoreLayoutFloatWindowActionView_plvTextColorNormal, textColorNormal);
        textColorSelected = typedArray.getColor(R.styleable.PLVMediaPlayerMoreLayoutFloatWindowActionView_plvTextColorSelected, textColorSelected);
        typedArray.recycle();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (isInEditMode()) return;

        PLVMediaPlayerFloatWindowManager.getInstance()
                .getFloatingViewShowState()
                .observeUntilViewDetached(this, new Function1<Boolean, Unit>() {
                    @Override
                    public Unit invoke(Boolean showing) {
                        currentFloatWindowShowing = showing;
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
                        currentMediaOutputMode = viewState.getOutputMode();
                        onViewStateChanged();
                        return null;
                    }
                });
    }

    protected void onViewStateChanged() {
        PLVRememberState.rememberStateOf(this, "onChangeColor")
                .compareLastAndSet(currentFloatWindowShowing)
                .ifNotEquals(new Function1<PLVRememberStateCompareResult, Unit>() {
                    @Override
                    public Unit invoke(PLVRememberStateCompareResult result) {
                        onChangeColor();
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

    protected void onChangeColor() {
        if (currentFloatWindowShowing == null) {
            return;
        }
        if (currentFloatWindowShowing) {
            floatWindowActionIv.setImageTintList(ColorStateList.valueOf(tintColorIconSelected));
            floatWindowActionTv.setTextColor(textColorSelected);
        } else {
            floatWindowActionIv.setImageTintList(ColorStateList.valueOf(tintColorIconNormal));
            floatWindowActionTv.setTextColor(textColorNormal);
        }
    }

    protected void onChangeVisibility() {
        boolean show = currentMediaOutputMode == PLVMediaOutputMode.AUDIO_VIDEO;
        setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onClick(View v) {
        requireNotNull(PLVMediaPlayerLocalProvider.localDependScope.on(this).current())
                .get(PLVMPMediaControllerViewModel.class)
                .launchFloatWindow(PLVFloatWindowLaunchReason.MANUAL);
        requireNotNull(PLVMediaPlayerLocalProvider.localDependScope.on(this).current())
                .get(PLVMPMediaControllerViewModel.class)
                .popFloatActionLayout();
    }
}
