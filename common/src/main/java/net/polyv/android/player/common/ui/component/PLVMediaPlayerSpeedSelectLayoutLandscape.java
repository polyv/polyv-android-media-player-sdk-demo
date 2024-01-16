package net.polyv.android.player.common.ui.component;

import static com.plv.foundationsdk.component.livedata.PLVLiveDataExt.observeUntilViewDetached;
import static com.plv.foundationsdk.ext.PLVViewGroupExt.children;
import static com.plv.foundationsdk.utils.PLVSugarUtil.listOf;
import static com.plv.foundationsdk.utils.PLVSugarUtil.requireNotNull;

import androidx.lifecycle.Observer;
import android.content.Context;
import android.graphics.Color;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.plv.foundationsdk.component.collection.PLVSequenceWrapper;
import com.plv.foundationsdk.component.remember.PLVRememberState;
import com.plv.foundationsdk.component.remember.PLVRememberStateCompareResult;
import com.plv.foundationsdk.utils.PLVFormatUtils;
import com.plv.foundationsdk.utils.PLVSugarUtil;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;

import net.polyv.android.player.business.scene.common.player.IPLVMediaPlayer;
import net.polyv.android.player.common.R;
import net.polyv.android.player.common.ui.localprovider.PLVMediaPlayerLocalProvider;
import net.polyv.android.player.common.ui.viewmodel.PLVMediaPlayerControlViewModel;
import net.polyv.android.player.common.ui.viewmodel.action.PLVMediaPlayerControlAction;
import net.polyv.android.player.common.ui.viewmodel.viewstate.PLVMediaPlayerControlViewState;

import java.util.List;
import java.util.Locale;

import kotlin.jvm.functions.Function1;

/**
 * @author Hoshiiro
 */
public class PLVMediaPlayerSpeedSelectLayoutLandscape extends FrameLayout implements View.OnClickListener {

    private static final List<Float> SUPPORT_SPEED_LIST = listOf(0.5F, 1.0F, 1.5F, 2.0F);

    private ImageView speedSelectCloseIv;
    private LinearLayout speedSelectContainer;

    protected Float currentSpeed = null;
    protected PLVMediaPlayerControlViewState currentControlViewState = null;

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
        PLVSequenceWrapper.wrap(SUPPORT_SPEED_LIST)
                .map(new Function1<Float, TextView>() {
                    @Override
                    public TextView invoke(final Float speed) {
                        TextView tv = new TextView(getContext());
                        tv.setText(String.format(Locale.getDefault(), "%.1fx", speed));
                        tv.setTag(speed);
                        tv.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                IPLVMediaPlayer mediaPlayer = PLVMediaPlayerLocalProvider.localMediaPlayer.on(PLVMediaPlayerSpeedSelectLayoutLandscape.this).current();
                                if (mediaPlayer != null) {
                                    mediaPlayer.setSpeed(speed);
                                }
                                closeLayout();
                            }
                        });
                        return tv;
                    }
                })
                .forEach(new PLVSugarUtil.Consumer<TextView>() {
                    @Override
                    public void accept(TextView textView) {
                        MarginLayoutParams lp = new MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        lp.topMargin = ConvertUtils.dp2px(24);
                        lp.bottomMargin = ConvertUtils.dp2px(24);
                        speedSelectContainer.addView(textView, lp);
                    }
                });
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (isInEditMode()) return;

        observeUntilViewDetached(
                requireNotNull(PLVMediaPlayerLocalProvider.localMediaPlayer.on(this).current())
                        .getStateListenerRegistry()
                        .getSpeed(),
                this,
                new Observer<Float>() {
                    @Override
                    public void onChanged(@Nullable @org.jetbrains.annotations.Nullable Float speed) {
                        currentSpeed = speed;
                        onViewStateChanged();
                    }
                }
        );

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
        PLVRememberState.rememberStateOf(this, "onUpdateCurrentSpeed")
                .compareLastAndSet(currentSpeed)
                .ifNotEquals(new PLVSugarUtil.Consumer<PLVRememberStateCompareResult>() {
                    @Override
                    public void accept(PLVRememberStateCompareResult result) {
                        onUpdateCurrentSpeed();
                    }
                });

        PLVRememberState.rememberStateOf(this, "onChangeVisibility")
                .compareLastAndSet(currentControlViewState)
                .ifNotEquals(new PLVSugarUtil.Consumer<PLVRememberStateCompareResult>() {
                    @Override
                    public void accept(PLVRememberStateCompareResult result) {
                        onChangeVisibility();
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
                ((TextView) child).setTextColor(PLVFormatUtils.parseColor("#3F76FC"));
            } else {
                ((TextView) child).setTextColor(Color.WHITE);
            }
        }
    }

    protected void onChangeVisibility() {
        if (currentControlViewState == null) {
            return;
        }
        final boolean visible = currentControlViewState.speedSelectLayoutVisible
                && !currentControlViewState.isOverlayLayoutVisible();
        setVisibility(visible ? View.VISIBLE : View.GONE);
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

    private void closeLayout() {
        PLVMediaPlayerControlViewModel controlViewModel = PLVMediaPlayerLocalProvider.localControlViewModel.on(this).current();
        if (controlViewModel == null) {
            return;
        }
        controlViewModel.requestControl(PLVMediaPlayerControlAction.closeFloatMenuLayout());
    }

}
