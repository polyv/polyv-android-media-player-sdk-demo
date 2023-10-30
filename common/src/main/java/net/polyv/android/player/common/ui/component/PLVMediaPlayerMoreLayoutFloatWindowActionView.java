package net.polyv.android.player.common.ui.component;

import static com.plv.foundationsdk.component.livedata.PLVLiveDataExt.observeUntilViewDetached;
import static com.plv.foundationsdk.utils.PLVSugarUtil.requireNotNull;

import android.arch.lifecycle.Observer;
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

import com.plv.foundationsdk.component.remember.PLVRememberState;
import com.plv.foundationsdk.component.remember.PLVRememberStateCompareResult;
import com.plv.foundationsdk.utils.PLVFormatUtils;
import com.plv.foundationsdk.utils.PLVSugarUtil;

import net.polyv.android.player.business.scene.common.model.vo.PLVMediaOutputMode;
import net.polyv.android.player.common.R;
import net.polyv.android.player.common.ui.component.floatwindow.PLVMediaPlayerFloatWindowManager;
import net.polyv.android.player.common.ui.localprovider.PLVMediaPlayerLocalProvider;
import net.polyv.android.player.common.ui.viewmodel.PLVMediaPlayerControlViewModel;
import net.polyv.android.player.common.ui.viewmodel.action.PLVMediaPlayerControlAction;

/**
 * @author Hoshiiro
 */
public class PLVMediaPlayerMoreLayoutFloatWindowActionView extends FrameLayout implements View.OnClickListener {

    private ImageView floatWindowActionIv;
    private TextView floatWindowActionTv;

    private int tintColorIconNormal = Color.WHITE;
    private int tintColorIconSelected = PLVFormatUtils.parseColor("#3F76FC");
    private int textColorNormal = PLVFormatUtils.parseColor("#CCFFFFFF");
    private int textColorSelected = PLVFormatUtils.parseColor("#CC3F76FC");

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

        observeUntilViewDetached(
                PLVMediaPlayerFloatWindowManager.getInstance()
                        .getFloatingViewShowState(),
                this,
                new Observer<Boolean>() {
                    @Override
                    public void onChanged(@Nullable @org.jetbrains.annotations.Nullable Boolean showing) {
                        currentFloatWindowShowing = showing;
                        onViewStateChanged();
                    }
                }
        );

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
                .compareLastAndSet(currentFloatWindowShowing)
                .ifNotEquals(new PLVSugarUtil.Consumer<PLVRememberStateCompareResult>() {
                    @Override
                    public void accept(PLVRememberStateCompareResult result) {
                        onChangeColor();
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
        PLVMediaPlayerControlViewModel viewModel = PLVMediaPlayerLocalProvider.localControlViewModel.on(PLVMediaPlayerMoreLayoutFloatWindowActionView.this).current();
        if (viewModel != null) {
            viewModel.requestControl(PLVMediaPlayerControlAction.launchFloatWindow());
            viewModel.requestControl(PLVMediaPlayerControlAction.closeFloatMenuLayout());
        }
    }
}
