package net.polyv.android.player.common.ui.component;

import static net.polyv.android.player.sdk.foundation.collections.CollectionsKt.listOf;
import static net.polyv.android.player.sdk.foundation.graphics.ColorsKt.parseColor;
import static net.polyv.android.player.sdk.foundation.graphics.DisplaysKt.dp;
import static net.polyv.android.player.sdk.foundation.lang.PreconditionsKt.requireNotNull;
import static net.polyv.android.player.sdk.foundation.ui.ViewGroupsKt.children;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.polyv.android.player.common.R;
import net.polyv.android.player.common.di.PLVMediaPlayerLocalProvider;
import net.polyv.android.player.common.modules.media.viewmodel.PLVMPMediaViewModel;
import net.polyv.android.player.common.modules.media.viewmodel.viewstate.PLVMPMediaPlayViewState;
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.PLVMPMediaControllerViewModel;
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.viewstate.PLVMPMediaControllerFloatAction;
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.viewstate.PLVMPMediaControllerViewState;
import net.polyv.android.player.sdk.foundation.collections.PLVSequences;
import net.polyv.android.player.sdk.foundation.lang.PLVRememberState;
import net.polyv.android.player.sdk.foundation.lang.PLVRememberStateCompareResult;

import java.util.List;
import java.util.Locale;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

/**
 * @author Hoshiiro
 */
public class PLVMediaPlayerSpeedSelectLayoutLandscape extends FrameLayout implements View.OnClickListener {

    private static final List<Float> SUPPORT_SPEED_LIST = listOf(0.5F, 1.0F, 1.5F, 2.0F);

    private ImageView speedSelectCloseIv;
    private LinearLayout speedSelectContainer;

    protected Float currentSpeed = null;
    protected boolean isVisible = false;

    public PLVMediaPlayerSpeedSelectLayoutLandscape(@NonNull Context context) {
        super(context);
    }

    public PLVMediaPlayerSpeedSelectLayoutLandscape(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PLVMediaPlayerSpeedSelectLayoutLandscape(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    {
        LayoutInflater.from(getContext()).inflate(R.layout.plv_media_player_ui_component_speed_select_layout, this);
        speedSelectCloseIv = findViewById(R.id.plv_media_player_speed_select_close_iv);
        speedSelectContainer = findViewById(R.id.plv_media_player_speed_select_container);

        initSpeedSelectLayout();

        setOnClickListener(this);
        speedSelectCloseIv.setOnClickListener(this);
    }

    protected void initSpeedSelectLayout() {
        PLVSequences.wrap(SUPPORT_SPEED_LIST)
                .map(new Function1<Float, TextView>() {
                    @Override
                    public TextView invoke(final Float speed) {
                        TextView tv = new TextView(getContext());
                        tv.setText(String.format(Locale.getDefault(), "%.1fx", speed));
                        tv.setTag(speed);
                        tv.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                onSelectSpeed(speed);
                            }
                        });
                        return tv;
                    }
                })
                .forEach(new Function1<TextView, Unit>() {
                    @Override
                    public Unit invoke(TextView textView) {
                        MarginLayoutParams lp = new MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        lp.topMargin = dp(24).px();
                        lp.bottomMargin = dp(24).px();
                        speedSelectContainer.addView(textView, lp);
                        return null;
                    }
                });
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (isInEditMode()) return;

        requireNotNull(PLVMediaPlayerLocalProvider.localDependScope.on(this).current())
                .get(PLVMPMediaViewModel.class)
                .getMediaPlayViewState()
                .observeUntilViewDetached(this, new Function1<PLVMPMediaPlayViewState, Unit>() {
                    @Override
                    public Unit invoke(PLVMPMediaPlayViewState viewState) {
                        currentSpeed = viewState.getSpeed();
                        onViewStateChanged();
                        return null;
                    }
                });

        requireNotNull(PLVMediaPlayerLocalProvider.localDependScope.on(this).current())
                .get(PLVMPMediaControllerViewModel.class)
                .getMediaControllerViewState()
                .observeUntilViewDetached(this, new Function1<PLVMPMediaControllerViewState, Unit>() {
                    @Override
                    public Unit invoke(PLVMPMediaControllerViewState viewState) {
                        isVisible = viewState.getLastFloatActionLayout() == PLVMPMediaControllerFloatAction.SPEED
                                && !viewState.isMediaStopOverlayVisible();
                        return null;
                    }
                });
    }

    protected void onViewStateChanged() {
        PLVRememberState.rememberStateOf(this, "onUpdateCurrentSpeed")
                .compareLastAndSet(currentSpeed)
                .ifNotEquals(new Function1<PLVRememberStateCompareResult, Unit>() {
                    @Override
                    public Unit invoke(PLVRememberStateCompareResult result) {
                        onUpdateCurrentSpeed();
                        return null;
                    }
                });

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

    protected void onUpdateCurrentSpeed() {
        if (currentSpeed == null) {
            return;
        }
        for (View child : children(speedSelectContainer)) {
            if (!(child instanceof TextView)) {
                return;
            }
            if (child.getTag() instanceof Float && ((float) child.getTag()) == currentSpeed) {
                ((TextView) child).setTextColor(parseColor("#3F76FC"));
            } else {
                ((TextView) child).setTextColor(Color.WHITE);
            }
        }
    }

    protected void onChangeVisibility() {
        setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        if (id == speedSelectCloseIv.getId()) {
            closeLayout();
        } else if (id == this.getId()) {
            closeLayout();
        }
    }

    private void onSelectSpeed(float speed) {
        requireNotNull(PLVMediaPlayerLocalProvider.localDependScope.on(this).current())
                .get(PLVMPMediaViewModel.class)
                .setSpeed(speed);
        closeLayout();
    }

    private void closeLayout() {
        requireNotNull(PLVMediaPlayerLocalProvider.localDependScope.on(this).current())
                .get(PLVMPMediaControllerViewModel.class)
                .popFloatActionLayout();
    }

}
