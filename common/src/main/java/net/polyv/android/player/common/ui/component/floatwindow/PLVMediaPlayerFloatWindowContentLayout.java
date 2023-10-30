package net.polyv.android.player.common.ui.component.floatwindow;

import static com.plv.foundationsdk.component.livedata.PLVLiveDataExt.observeForeverUntilViewDetached;
import static com.plv.foundationsdk.utils.PLVSugarUtil.requireNotNull;

import android.arch.lifecycle.Observer;
import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import net.polyv.android.player.business.scene.common.player.IPLVMediaPlayer;
import net.polyv.android.player.common.R;
import net.polyv.android.player.common.ui.localprovider.PLVMediaPlayerLocalProvider;

/**
 * @author Hoshiiro
 */
public class PLVMediaPlayerFloatWindowContentLayout extends FrameLayout {

    private FrameLayout floatWindowMediaContainer;
    private ImageView floatWindowCloseIv;

    public PLVMediaPlayerFloatWindowContentLayout(@NonNull Context context) {
        super(context);
        initView();
    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.plv_media_player_float_window_layout, this);
        floatWindowMediaContainer = findViewById(R.id.plv_media_player_float_window_media_container);
        floatWindowCloseIv = findViewById(R.id.plv_media_player_float_window_close_iv);
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
        floatWindowCloseIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onClickCloseListener != null) {
                    onClickCloseListener.onClick(v);
                }
            }
        });
        return this;
    }

    public PLVMediaPlayerFloatWindowContentLayout setOnClickContentGoBackListener(final OnClickListener onClickContentGoBackListener) {
        floatWindowMediaContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onClickContentGoBackListener != null) {
                    onClickContentGoBackListener.onClick(v);
                }
            }
        });
        return this;
    }

}
