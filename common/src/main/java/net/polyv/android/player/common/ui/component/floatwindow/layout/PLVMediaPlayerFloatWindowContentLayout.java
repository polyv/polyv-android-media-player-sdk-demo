package net.polyv.android.player.common.ui.component.floatwindow.layout;

import static com.plv.foundationsdk.component.livedata.PLVLiveDataExt.observeForeverUntilViewDetached;
import static com.plv.foundationsdk.utils.PLVSugarUtil.requireNotNull;

import androidx.lifecycle.Observer;
import android.content.Context;
import android.graphics.Rect;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.plv.foundationsdk.utils.PLVTimeUnit;

import net.polyv.android.player.business.scene.common.player.IPLVMediaPlayer;
import net.polyv.android.player.common.R;
import net.polyv.android.player.common.ui.component.floatwindow.PLVMediaPlayerFloatWindowHelper;
import net.polyv.android.player.common.ui.component.floatwindow.PLVMediaPlayerFloatWindowManager;
import net.polyv.android.player.common.ui.localprovider.PLVMediaPlayerLocalProvider;
import net.polyv.android.player.common.utils.extensions.PLVMediaPlayerExtensions;

/**
 * @author Hoshiiro
 */
public class PLVMediaPlayerFloatWindowContentLayout extends FrameLayout implements View.OnClickListener {

    private FrameLayout floatWindowMediaContainer;
    private ImageView floatWindowSeekBackwardIv;
    private PLVMediaPlayerFloatWindowPlayButton floatWindowPlayBtn;
    private ImageView floatWindowSeekForwardIv;
    private ImageView floatWindowCloseIv;
    private ImageView floatWindowGoBackIv;

    private boolean controllerVisible = true;

    @Nullable
    private View.OnClickListener onClickCloseListener = null;
    @Nullable
    private View.OnClickListener onClickGoBackListener = null;

    public PLVMediaPlayerFloatWindowContentLayout(@NonNull Context context) {
        super(context);
        initView();
    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.plv_media_player_float_window_layout, this);

        floatWindowMediaContainer = findViewById(R.id.plv_media_player_float_window_media_container);
        floatWindowSeekBackwardIv = findViewById(R.id.plv_media_player_float_window_seek_backward_iv);
        floatWindowPlayBtn = findViewById(R.id.plv_media_player_float_window_play_btn);
        floatWindowSeekForwardIv = findViewById(R.id.plv_media_player_float_window_seek_forward_iv);
        floatWindowCloseIv = findViewById(R.id.plv_media_player_float_window_close_iv);
        floatWindowGoBackIv = findViewById(R.id.plv_media_player_float_window_go_back_iv);

        floatWindowMediaContainer.setOnClickListener(this);
        floatWindowSeekBackwardIv.setOnClickListener(this);
        floatWindowSeekForwardIv.setOnClickListener(this);
        floatWindowCloseIv.setOnClickListener(this);
        floatWindowGoBackIv.setOnClickListener(this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (isInEditMode()) return;

        observeForeverUntilViewDetached(
                requireNotNull(PLVMediaPlayerLocalProvider.localMediaPlayer.on(this).current())
                        .getStateListenerRegistry()
                        .getVideoSize(),
                this,
                new Observer<Rect>() {
                    @Override
                    public void onChanged(@Nullable @org.jetbrains.annotations.Nullable Rect rect) {
                        if (rect == null) {
                            return;
                        }
                        IPLVMediaPlayer mediaPlayer = PLVMediaPlayerLocalProvider.localMediaPlayer.on(PLVMediaPlayerFloatWindowContentLayout.this).current();
                        Rect floatWindowPosition = PLVMediaPlayerFloatWindowHelper.calculateFloatWindowPosition(mediaPlayer);
                        if (floatWindowPosition != null) {
                            PLVMediaPlayerFloatWindowManager
                                    .getInstance()
                                    .setFloatingSize(floatWindowPosition.width(), floatWindowPosition.height())
                                    .setFloatingPosition(floatWindowPosition.left, floatWindowPosition.top);
                        }
                    }
                }
        );
    }

    public ViewGroup getContainer() {
        return floatWindowMediaContainer;
    }

    public PLVMediaPlayerFloatWindowContentLayout setOnClickCloseListener(final OnClickListener onClickCloseListener) {
        this.onClickCloseListener = onClickCloseListener;
        return this;
    }

    public PLVMediaPlayerFloatWindowContentLayout setOnClickContentGoBackListener(final OnClickListener onClickContentGoBackListener) {
        this.onClickGoBackListener = onClickContentGoBackListener;
        return this;
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        if (id == floatWindowMediaContainer.getId()) {
            switchVisibility();
        } else if (id == floatWindowCloseIv.getId()) {
            if (onClickCloseListener != null) {
                onClickCloseListener.onClick(v);
            }
        } else if (id == floatWindowGoBackIv.getId()) {
            if (onClickGoBackListener != null) {
                onClickGoBackListener.onClick(v);
            }
        } else if (id == floatWindowSeekForwardIv.getId()) {
            seekOffset(PLVTimeUnit.seconds(10).toMillis());
        } else if (id == floatWindowSeekBackwardIv.getId()) {
            seekOffset(PLVTimeUnit.seconds(-10).toMillis());
        }
    }

    private void seekOffset(long offset) {
        final IPLVMediaPlayer mediaPlayer = PLVMediaPlayerLocalProvider.localMediaPlayer.on(this).current();
        if (mediaPlayer == null) {
            return;
        }
        PLVMediaPlayerExtensions.seekTo(mediaPlayer, mediaPlayer.getCurrentPosition() + offset);
    }

    private void switchVisibility() {
        controllerVisible = !controllerVisible;
        floatWindowSeekBackwardIv.setVisibility(controllerVisible ? VISIBLE : GONE);
        floatWindowPlayBtn.setVisibility(controllerVisible ? VISIBLE : GONE);
        floatWindowSeekForwardIv.setVisibility(controllerVisible ? VISIBLE : GONE);
        floatWindowCloseIv.setVisibility(controllerVisible ? VISIBLE : GONE);
        floatWindowGoBackIv.setVisibility(controllerVisible ? VISIBLE : GONE);
    }

}
