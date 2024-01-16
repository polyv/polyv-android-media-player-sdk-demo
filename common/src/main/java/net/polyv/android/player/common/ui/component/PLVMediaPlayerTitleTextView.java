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

import net.polyv.android.player.business.scene.common.model.api.vo.PLVVodVideoJsonVO;
import net.polyv.android.player.common.ui.localprovider.PLVMediaPlayerLocalProvider;
import net.polyv.android.player.common.ui.viewmodel.viewstate.PLVMediaPlayerControlViewState;

/**
 * @author Hoshiiro
 */
public class PLVMediaPlayerTitleTextView extends AppCompatTextView {

    protected PLVVodVideoJsonVO currentVodVideoJson = null;
    protected PLVMediaPlayerControlViewState currentControlViewState = null;

    public PLVMediaPlayerTitleTextView(Context context) {
        super(context);
    }

    public PLVMediaPlayerTitleTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PLVMediaPlayerTitleTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (isInEditMode()) return;

        observeUntilViewDetached(
                requireNotNull(PLVMediaPlayerLocalProvider.localMediaPlayer.on(this).current())
                        .getBusinessListenerRegistry()
                        .getVodVideoJson(),
                this,
                new Observer<PLVVodVideoJsonVO>() {
                    @Override
                    public void onChanged(@Nullable @org.jetbrains.annotations.Nullable PLVVodVideoJsonVO vodVideoJson) {
                        currentVodVideoJson = vodVideoJson;
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
                .compareLastAndSet(currentVodVideoJson)
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
        setText(titleText());
    }

    protected void onChangeVisibility() {
        if (currentControlViewState == null) {
            return;
        }
        boolean visible = currentControlViewState.controllerVisible
                && !currentControlViewState.isOverlayLayoutVisible()
                && !currentControlViewState.progressSeekBarDragging
                && !currentControlViewState.controllerLocking
                && !(currentControlViewState.isFloatActionPanelVisible() && ScreenUtils.isLandscape());
        setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    protected String titleText() {
        if (currentVodVideoJson == null
                || currentVodVideoJson.getTitle() == null
                || currentVodVideoJson.getTitle().isEmpty()) {
            return null;
        } else {
            return currentVodVideoJson.getTitle();
        }
    }

}
