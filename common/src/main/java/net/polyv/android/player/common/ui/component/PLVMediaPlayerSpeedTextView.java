package net.polyv.android.player.common.ui.component;

import static com.plv.foundationsdk.component.livedata.PLVLiveDataExt.observeUntilViewDetached;
import static com.plv.foundationsdk.utils.PLVSugarUtil.requireNotNull;

import androidx.lifecycle.Observer;
import android.content.Context;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.view.View;

import com.plv.foundationsdk.component.remember.PLVRememberState;
import com.plv.foundationsdk.component.remember.PLVRememberStateCompareResult;
import com.plv.foundationsdk.utils.PLVSugarUtil;
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;

import net.polyv.android.player.common.R;
import net.polyv.android.player.common.ui.localprovider.PLVMediaPlayerLocalProvider;
import net.polyv.android.player.common.ui.viewmodel.PLVMediaPlayerControlViewModel;
import net.polyv.android.player.common.ui.viewmodel.action.PLVMediaPlayerControlAction;
import net.polyv.android.player.common.ui.viewmodel.viewstate.PLVMediaPlayerControlViewState;

import java.util.Locale;

/**
 * @author Hoshiiro
 */
public class PLVMediaPlayerSpeedTextView extends AppCompatTextView implements View.OnClickListener {

    protected Float currentSpeed = null;
    protected PLVMediaPlayerControlViewState currentControlViewState = null;

    public PLVMediaPlayerSpeedTextView(Context context) {
        super(context);
    }

    public PLVMediaPlayerSpeedTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PLVMediaPlayerSpeedTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    {
        setOnClickListener(this);
        onViewStateChanged();
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
        PLVRememberState.rememberStateOf(this, "onChangeText")
                .compareLastAndSet(currentSpeed)
                .ifNotEquals(new PLVSugarUtil.Consumer<PLVRememberStateCompareResult>() {
                    @Override
                    public void accept(PLVRememberStateCompareResult result) {
                        onChangeText();
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

    protected void onChangeText() {
        if (currentSpeed == null) {
            setText(R.string.plv_media_player_ui_component_speed_hint_text);
        } else {
            setText(String.format(Locale.getDefault(), "%.1fx", currentSpeed));
        }
    }

    protected void onChangeVisibility() {
        if (currentControlViewState == null) {
            return;
        }
        final boolean visible = currentControlViewState.controllerVisible
                && !currentControlViewState.isOverlayLayoutVisible()
                && !currentControlViewState.progressSeekBarDragging
                && !currentControlViewState.controllerLocking
                && !(currentControlViewState.isFloatActionPanelVisible() && ScreenUtils.isLandscape());
        setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onClick(View v) {
        PLVMediaPlayerControlViewModel controlViewModel = PLVMediaPlayerLocalProvider.localControlViewModel.on(PLVMediaPlayerSpeedTextView.this).current();
        if (controlViewModel == null) {
            return;
        }
        controlViewModel.requestControl(PLVMediaPlayerControlAction.showSpeedSelectLayout());
    }

}