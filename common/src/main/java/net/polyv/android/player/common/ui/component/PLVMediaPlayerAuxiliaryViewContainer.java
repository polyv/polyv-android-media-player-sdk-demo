package net.polyv.android.player.common.ui.component;

import static net.polyv.android.player.sdk.foundation.lang.PreconditionsKt.requireNotNull;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import net.polyv.android.player.common.R;
import net.polyv.android.player.common.di.PLVMediaPlayerLocalProvider;
import net.polyv.android.player.common.modules.auxiliary.viewmodel.PLVMPAuxiliaryViewModel;
import net.polyv.android.player.common.modules.auxiliary.viewmodel.viewstate.PLVMPAuxiliaryInfoViewState;
import net.polyv.android.player.sdk.foundation.lang.PLVRememberState;
import net.polyv.android.player.sdk.foundation.lang.PLVRememberStateCompareResult;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

/**
 * @author Hoshiiro
 */
public class PLVMediaPlayerAuxiliaryViewContainer extends FrameLayout {

    private FrameLayout auxiliaryVideoViewContainer;
    private PLVMediaPlayerAuxiliaryCountDownTextView auxiliaryCountDownTv;

    private boolean isAdvertShowing = false;

    public PLVMediaPlayerAuxiliaryViewContainer(@NonNull Context context) {
        super(context);
        initView();
    }

    public PLVMediaPlayerAuxiliaryViewContainer(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public PLVMediaPlayerAuxiliaryViewContainer(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.plv_media_player_ui_component_auxiliary_view_container_layout, this);

        auxiliaryVideoViewContainer = findViewById(R.id.plv_media_player_auxiliary_video_view_container);
        auxiliaryCountDownTv = findViewById(R.id.plv_media_player_auxiliary_count_down_tv);

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 点击事件拦截，防止点击到下层
            }
        });
        onViewStateChanged();
    }

    public void setAuxiliaryVideoView(View auxiliaryVideoView) {
        auxiliaryVideoViewContainer.removeAllViews();
        if (auxiliaryVideoView != null && auxiliaryVideoView.getParent() != null) {
            ((ViewGroup) auxiliaryVideoView.getParent()).removeView(auxiliaryVideoView);
        }
        if (auxiliaryVideoView != null && auxiliaryVideoView.getParent() == null) {
            auxiliaryVideoViewContainer.addView(auxiliaryVideoView);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (isInEditMode()) return;

        requireNotNull(PLVMediaPlayerLocalProvider.localDependScope.on(this).current())
                .get(PLVMPAuxiliaryViewModel.class)
                .getAuxiliaryInfoViewState()
                .observeUntilViewDetached(this, new Function1<PLVMPAuxiliaryInfoViewState, Unit>() {
                    @Override
                    public Unit invoke(PLVMPAuxiliaryInfoViewState viewState) {
                        isAdvertShowing = viewState != null && viewState.getStage().isAuxiliaryStage();
                        onViewStateChanged();
                        return null;
                    }
                });
    }

    protected void onViewStateChanged() {
        PLVRememberState.rememberStateOf(this, "onAdvertShowStateChanged")
                .compareLastAndSet(isAdvertShowing)
                .ifNotEquals(new Function1<PLVRememberStateCompareResult, Unit>() {
                    @Override
                    public Unit invoke(PLVRememberStateCompareResult result) {
                        onAdvertShowStateChanged();
                        return null;
                    }
                });
    }

    protected void onAdvertShowStateChanged() {
        setVisibility(isAdvertShowing ? VISIBLE : GONE);
    }

}
