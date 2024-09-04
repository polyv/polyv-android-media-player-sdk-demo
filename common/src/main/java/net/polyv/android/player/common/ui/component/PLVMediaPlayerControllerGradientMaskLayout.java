package net.polyv.android.player.common.ui.component;

import static net.polyv.android.player.sdk.foundation.graphics.DisplaysKt.dp;
import static net.polyv.android.player.sdk.foundation.graphics.DisplaysKt.isLandscape;
import static net.polyv.android.player.sdk.foundation.lang.PreconditionsKt.requireNotNull;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import net.polyv.android.player.common.R;
import net.polyv.android.player.common.di.PLVMediaPlayerLocalProvider;
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.PLVMPMediaControllerViewModel;
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.viewstate.PLVMPMediaControllerViewState;
import net.polyv.android.player.sdk.foundation.lang.PLVRememberState;
import net.polyv.android.player.sdk.foundation.lang.PLVRememberStateCompareResult;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

/**
 * @author Hoshiiro
 */
public class PLVMediaPlayerControllerGradientMaskLayout extends FrameLayout {

    private float topMaskHeight = dp(100).px();
    private float bottomMaskHeight = dp(100).px();

    private View controllerGradientMaskTop;
    private View controllerGradientMaskBottom;

    protected boolean isVisible = false;

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

        requireNotNull(PLVMediaPlayerLocalProvider.localDependScope.on(this).current())
                .get(PLVMPMediaControllerViewModel.class)
                .getMediaControllerViewState()
                .observeUntilViewDetached(this, new Function1<PLVMPMediaControllerViewState, Unit>() {
                    @Override
                    public Unit invoke(PLVMPMediaControllerViewState viewState) {
                        isVisible = viewState.getControllerVisible()
                                && !viewState.isMediaStopOverlayVisible()
                                && !viewState.getControllerLocking()
                                && !(viewState.isFloatActionLayoutVisible() && isLandscape());
                        onViewStateChanged();
                        return null;
                    }
                });
    }

    protected void onViewStateChanged() {
        PLVRememberState.rememberStateOf(this, "onChangeVisibility")
                .compareLastAndSet(isVisible)
                .ifNotEquals(new Function1<PLVRememberStateCompareResult, Unit>() {
                    @Override
                    public Unit invoke(PLVRememberStateCompareResult result) {
                        onChangeVisibility();
                        return null;
                    }
                });
    }

    protected void onChangeVisibility() {
        setVisibility(isVisible ? VISIBLE : GONE);
    }

}
