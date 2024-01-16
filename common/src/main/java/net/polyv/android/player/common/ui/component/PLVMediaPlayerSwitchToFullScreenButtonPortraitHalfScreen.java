package net.polyv.android.player.common.ui.component;

import static com.plv.foundationsdk.component.livedata.PLVLiveDataExt.observeUntilViewDetached;
import static com.plv.foundationsdk.utils.PLVSugarUtil.requireNotNull;

import androidx.lifecycle.Observer;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.View;

import com.plv.foundationsdk.component.remember.PLVRememberState;
import com.plv.foundationsdk.component.remember.PLVRememberStateCompareResult;
import com.plv.foundationsdk.utils.PLVSugarUtil;

import net.polyv.android.player.common.ui.localprovider.PLVMediaPlayerLocalProvider;
import net.polyv.android.player.common.ui.viewmodel.viewstate.PLVMediaPlayerControlViewState;
import net.polyv.android.player.common.utils.orientation.PLVActivityOrientationManager;

/**
 * @author Hoshiiro
 */
public class PLVMediaPlayerSwitchToFullScreenButtonPortraitHalfScreen extends AppCompatImageView implements View.OnClickListener {

    protected PLVMediaPlayerControlViewState currentControlViewState = null;

    public PLVMediaPlayerSwitchToFullScreenButtonPortraitHalfScreen(@NonNull Context context) {
        super(context);
    }

    public PLVMediaPlayerSwitchToFullScreenButtonPortraitHalfScreen(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PLVMediaPlayerSwitchToFullScreenButtonPortraitHalfScreen(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
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
        boolean visible = currentControlViewState.controllerVisible && !currentControlViewState.isOverlayLayoutVisible();
        setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onClick(View v) {
        PLVActivityOrientationManager.on((AppCompatActivity) getContext()).requestOrientation(false);
    }

}
