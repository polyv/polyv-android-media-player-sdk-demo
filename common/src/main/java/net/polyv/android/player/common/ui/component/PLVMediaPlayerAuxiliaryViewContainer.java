package net.polyv.android.player.common.ui.component;

import static com.plv.foundationsdk.component.livedata.PLVLiveDataExt.observeUntilViewDetached;
import static com.plv.foundationsdk.utils.PLVSugarUtil.requireNotNull;

import androidx.lifecycle.Observer;
import android.content.Context;
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

import net.polyv.android.player.business.scene.common.model.vo.PLVMediaPlayStage;
import net.polyv.android.player.common.R;
import net.polyv.android.player.common.ui.localprovider.PLVMediaPlayerLocalProvider;

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

        observeUntilViewDetached(
                requireNotNull(PLVMediaPlayerLocalProvider.localMediaPlayer.on(this).current())
                        .getBusinessListenerRegistry()
                        .getCurrentPlayStage(),
                this,
                new Observer<PLVMediaPlayStage>() {
                    @Override
                    public void onChanged(@Nullable @org.jetbrains.annotations.Nullable PLVMediaPlayStage playStage) {
                        isAdvertShowing = playStage != null && playStage.isAuxiliaryStage();
                        onViewStateChanged();
                    }
                }
        );
    }

    protected void onViewStateChanged() {
        PLVRememberState.rememberStateOf(this, "onAdvertShowStateChanged")
                .compareLastAndSet(isAdvertShowing)
                .ifNotEquals(new PLVSugarUtil.Consumer<PLVRememberStateCompareResult>() {
                    @Override
                    public void accept(PLVRememberStateCompareResult plvRememberStateCompareResult) {
                        onAdvertShowStateChanged();
                    }
                });
    }

    protected void onAdvertShowStateChanged() {
        setVisibility(isAdvertShowing ? VISIBLE : GONE);
    }

}
