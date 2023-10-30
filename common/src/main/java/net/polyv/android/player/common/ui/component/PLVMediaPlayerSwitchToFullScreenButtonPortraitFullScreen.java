package net.polyv.android.player.common.ui.component;

import static com.plv.foundationsdk.component.livedata.PLVLiveDataExt.observeUntilViewDetached;
import static com.plv.foundationsdk.utils.PLVSugarUtil.requireNotNull;

import android.arch.lifecycle.Observer;
import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.plv.foundationsdk.component.remember.PLVRememberState;
import com.plv.foundationsdk.component.remember.PLVRememberStateCompareResult;
import com.plv.foundationsdk.utils.PLVSugarUtil;

import net.polyv.android.player.common.R;
import net.polyv.android.player.common.ui.localprovider.PLVMediaPlayerLocalProvider;
import net.polyv.android.player.common.utils.orientation.PLVActivityOrientationManager;

/**
 * @author Hoshiiro
 */
public class PLVMediaPlayerSwitchToFullScreenButtonPortraitFullScreen extends FrameLayout implements View.OnClickListener {

    protected Rect currentVideoSize = null;

    public PLVMediaPlayerSwitchToFullScreenButtonPortraitFullScreen(@NonNull Context context) {
        super(context);
    }

    public PLVMediaPlayerSwitchToFullScreenButtonPortraitFullScreen(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PLVMediaPlayerSwitchToFullScreenButtonPortraitFullScreen(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    {
        LayoutInflater.from(getContext()).inflate(R.layout.plv_media_player_ui_component_switch_to_full_screen_button_layout, this);
        setOnClickListener(this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (isInEditMode()) return;

        observeUntilViewDetached(
                requireNotNull(PLVMediaPlayerLocalProvider.localMediaPlayer.on(this).current())
                        .getStateListenerRegistry()
                        .getVideoSize(),
                this,
                new Observer<Rect>() {
                    @Override
                    public void onChanged(@Nullable @org.jetbrains.annotations.Nullable Rect rect) {
                        currentVideoSize = rect;
                        onViewStateChanged();
                    }
                }
        );
    }

    protected void onViewStateChanged() {
        PLVRememberState.rememberStateOf(this, "onChangeVisibility")
                .compareLastAndSet(currentVideoSize)
                .ifNotEquals(new PLVSugarUtil.Consumer<PLVRememberStateCompareResult>() {
                    @Override
                    public void accept(PLVRememberStateCompareResult result) {
                        onChangeVisibility();
                    }
                });
    }

    protected void onChangeVisibility() {
        if (currentVideoSize == null || currentVideoSize.width() == 0 || currentVideoSize.height() == 0) {
            return;
        }
        final boolean isPortraitVideo = currentVideoSize.width() < currentVideoSize.height();
        setVisibility(isPortraitVideo ? GONE : VISIBLE);
    }

    @Override
    public void onClick(View v) {
        PLVActivityOrientationManager.on((AppCompatActivity) getContext()).requestOrientation(false);
    }

}
