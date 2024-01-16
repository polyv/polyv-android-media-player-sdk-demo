package net.polyv.android.player.common.ui.component;

import static com.plv.foundationsdk.component.livedata.PLVLiveDataExt.observeUntilViewDetached;
import static com.plv.foundationsdk.utils.PLVSugarUtil.requireNotNull;

import android.app.Activity;
import androidx.lifecycle.Observer;
import android.content.Context;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.View;

import com.plv.foundationsdk.component.remember.PLVRememberState;
import com.plv.foundationsdk.component.remember.PLVRememberStateCompareResult;
import com.plv.foundationsdk.utils.PLVSugarUtil;
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;

import net.polyv.android.player.common.ui.localprovider.PLVMediaPlayerLocalProvider;
import net.polyv.android.player.common.ui.viewmodel.viewstate.PLVMediaPlayerControlViewState;

/**
 * @author Hoshiiro
 */
public class PLVMediaPlayerBackImageView extends AppCompatImageView implements View.OnClickListener {

    protected PLVMediaPlayerControlViewState currentControlViewState = null;

    public PLVMediaPlayerBackImageView(Context context) {
        super(context);
    }

    public PLVMediaPlayerBackImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PLVMediaPlayerBackImageView(Context context, AttributeSet attrs, int defStyleAttr) {
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
                && !(currentControlViewState.isFloatActionPanelVisible() && ScreenUtils.isLandscape())
                && !currentControlViewState.progressSeekBarDragging
                && !currentControlViewState.controllerLocking;
        visible = visible || isRequireVisibleByOtherView();
        setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    protected boolean isRequireVisibleByOtherView() {
        if (currentControlViewState == null) {
            return false;
        }
        return currentControlViewState.isOverlayLayoutVisible();
    }

    @Override
    public void onClick(View v) {
        ((Activity) getContext()).onBackPressed();
    }

}
