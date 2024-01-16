package net.polyv.android.player.common.ui.component;

import static com.plv.foundationsdk.component.livedata.PLVLiveDataExt.observeUntilViewDetached;
import static com.plv.foundationsdk.utils.PLVSugarUtil.requireNotNull;

import androidx.lifecycle.Observer;
import android.content.Context;
import android.content.res.TypedArray;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.plv.foundationsdk.component.remember.PLVRememberState;
import com.plv.foundationsdk.component.remember.PLVRememberStateCompareResult;
import com.plv.foundationsdk.utils.PLVSugarUtil;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;

import net.polyv.android.player.common.R;
import net.polyv.android.player.common.ui.localprovider.PLVMediaPlayerLocalProvider;
import net.polyv.android.player.common.ui.viewmodel.viewstate.PLVMediaPlayerControlViewState;

/**
 * @author Hoshiiro
 */
public class PLVMediaPlayerControllerGradientMaskLayout extends FrameLayout {

    private float topMaskHeight = ConvertUtils.dp2px(100);
    private float bottomMaskHeight = ConvertUtils.dp2px(100);

    private View controllerGradientMaskTop;
    private View controllerGradientMaskBottom;

    protected PLVMediaPlayerControlViewState currentControlViewState = null;

    public PLVMediaPlayerControllerGradientMaskLayout(@NonNull Context context) {
        super(context);
        init(null);
    }

    public PLVMediaPlayerControllerGradientMaskLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public PLVMediaPlayerControllerGradientMaskLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(@Nullable AttributeSet attrs) {
        LayoutInflater.from(getContext()).inflate(R.layout.plv_media_player_ui_component_controller_gradient_mask_layout, this);
        controllerGradientMaskTop = findViewById(R.id.plv_media_player_controller_gradient_mask_top);
        controllerGradientMaskBottom = findViewById(R.id.plv_media_player_controller_gradient_mask_bottom);

        parseAttrs(attrs);
        initHeight(controllerGradientMaskTop, topMaskHeight);
        initHeight(controllerGradientMaskBottom, bottomMaskHeight);
    }

    private void parseAttrs(@Nullable AttributeSet attrs) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.PLVMediaPlayerControllerGradientMaskLayout);
        topMaskHeight = typedArray.getDimension(R.styleable.PLVMediaPlayerControllerGradientMaskLayout_plvTopMaskHeight, topMaskHeight);
        bottomMaskHeight = typedArray.getDimension(R.styleable.PLVMediaPlayerControllerGradientMaskLayout_plvBottomMaskHeight, bottomMaskHeight);
        typedArray.recycle();
    }

    private void initHeight(View view, float height) {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = (int) height;
        view.setLayoutParams(layoutParams);
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
                        currentControlViewState = viewState;
                        onViewStateChanged();
                    }
                }
        );
    }

    protected void onViewStateChanged() {
        PLVRememberState.rememberStateOf(this, "onChangeVisibility")
                .compareLastAndSet(currentControlViewState)
                .ifNotEquals(new PLVSugarUtil.Consumer<PLVRememberStateCompareResult>() {
                    @Override
                    public void accept(PLVRememberStateCompareResult result) {
                        onChangeVisibility();
                    }
                });
    }

    protected void onChangeVisibility() {
        if (currentControlViewState == null) {
            return;
        }
        boolean visible = currentControlViewState.controllerVisible
                && !currentControlViewState.isOverlayLayoutVisible()
                && !currentControlViewState.controllerLocking
                && !(currentControlViewState.isFloatActionPanelVisible() && ScreenUtils.isLandscape());
        setVisibility(visible ? VISIBLE : GONE);
    }

}
