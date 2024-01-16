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

import net.polyv.android.player.business.scene.common.model.vo.PLVMediaBitRate;
import net.polyv.android.player.business.scene.common.model.vo.PLVMediaOutputMode;
import net.polyv.android.player.common.ui.localprovider.PLVMediaPlayerLocalProvider;
import net.polyv.android.player.common.ui.viewmodel.PLVMediaPlayerControlViewModel;
import net.polyv.android.player.common.ui.viewmodel.action.PLVMediaPlayerControlAction;
import net.polyv.android.player.common.ui.viewmodel.viewstate.PLVMediaPlayerControlViewState;

/**
 * @author Hoshiiro
 */
public class PLVMediaPlayerBitRateTextView extends AppCompatTextView implements View.OnClickListener {

    protected PLVMediaBitRate currentMediaBitRate = null;
    protected PLVMediaOutputMode currentMediaOutputMode = null;
    protected PLVMediaPlayerControlViewState currentControlViewState = null;

    public PLVMediaPlayerBitRateTextView(Context context) {
        super(context);
    }

    public PLVMediaPlayerBitRateTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PLVMediaPlayerBitRateTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    {
        setOnClickListener(this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (isInEditMode()) return;

        observeUntilViewDetached(
                requireNotNull(PLVMediaPlayerLocalProvider.localMediaPlayer.on(this).current())
                        .getBusinessListenerRegistry()
                        .getCurrentMediaBitRate(),
                this,
                new Observer<PLVMediaBitRate>() {
                    @Override
                    public void onChanged(@Nullable @org.jetbrains.annotations.Nullable PLVMediaBitRate mediaBitRate) {
                        currentMediaBitRate = mediaBitRate;
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
                .compareLastAndSet(currentControlViewState, currentMediaBitRate, currentMediaOutputMode)
                .ifNotEquals(new PLVSugarUtil.Consumer<PLVRememberStateCompareResult>() {
                    @Override
                    public void accept(PLVRememberStateCompareResult result) {
                        onChangeVisibility();
                    }
                });

        PLVRememberState.rememberStateOf(this, "onChangeBitRateText")
                .compareLastAndSet(currentMediaBitRate)
                .ifNotEquals(new PLVSugarUtil.Consumer<PLVRememberStateCompareResult>() {
                    @Override
                    public void accept(PLVRememberStateCompareResult result) {
                        onChangeBitRateText();
                    }
                });
    }

    protected void onChangeVisibility() {
        boolean viewStateVisible = currentControlViewState != null
                && currentControlViewState.controllerVisible
                && !currentControlViewState.isOverlayLayoutVisible()
                && !currentControlViewState.progressSeekBarDragging
                && !currentControlViewState.controllerLocking
                && !(currentControlViewState.isFloatActionPanelVisible() && ScreenUtils.isLandscape());
        boolean visible = viewStateVisible
                && currentMediaBitRate != null
                && currentMediaOutputMode != PLVMediaOutputMode.AUDIO_ONLY;
        visible = visible || isRequireVisibleByOtherView();
        setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    protected boolean isRequireVisibleByOtherView() {
        if (currentControlViewState == null) {
            return false;
        }
        return ScreenUtils.isLandscape() && currentControlViewState.networkPoorIndicateLayoutVisible;
    }

    protected void onChangeBitRateText() {
        if (currentMediaBitRate != null) {
            setText(currentMediaBitRate.getName());
        }
    }

    @Override
    public void onClick(View v) {
        PLVMediaPlayerControlViewModel controlViewModel = PLVMediaPlayerLocalProvider.localControlViewModel.on(PLVMediaPlayerBitRateTextView.this).current();
        if (controlViewModel != null) {
            controlViewModel.requestControl(PLVMediaPlayerControlAction.showBitRateSelectLayout());
        }
    }
}
