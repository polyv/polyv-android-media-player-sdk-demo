package net.polyv.android.player.common.ui.component;

import static net.polyv.android.player.sdk.foundation.lang.PreconditionsKt.requireNotNull;
import static net.polyv.android.player.sdk.foundation.lang.StringsKt.format;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import net.polyv.android.player.common.R;
import net.polyv.android.player.common.di.PLVMediaPlayerLocalProvider;
import net.polyv.android.player.common.modules.download.single.viewmodel.PLVMPDownloadItemViewModel;
import net.polyv.android.player.common.modules.download.single.viewmodel.viewstate.PLVMPDownloadItemViewState;
import net.polyv.android.player.common.ui.router.PLVMediaPlayerRouter;
import net.polyv.android.player.common.ui.router.RouterDestination;
import net.polyv.android.player.common.ui.router.RouterPayload;
import net.polyv.android.player.sdk.addon.download.common.model.vo.PLVMediaDownloadStatus;
import net.polyv.android.player.sdk.foundation.lang.PLVRememberState;
import net.polyv.android.player.sdk.foundation.lang.PLVRememberStateCompareResult;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

/**
 * @author Hoshiiro
 */
public class PLVMediaPlayerMoreLayoutDownloadActionView extends FrameLayout implements View.OnClickListener, View.OnLongClickListener {

    private ImageView downloadActionIv;
    private FrameLayout downloadProgressLayout;
    private ProgressBar downloadProgressBar;
    private TextView downloadProgressTv;
    private TextView downloadActionTv;

    private PLVMediaDownloadStatus downloadStatus = PLVMediaDownloadStatus.NOT_STARTED.INSTANCE;
    private float downloadProgress = 0;
    private boolean isVisible = true;

    public PLVMediaPlayerMoreLayoutDownloadActionView(@NonNull Context context) {
        super(context);
        init(null);
    }

    public PLVMediaPlayerMoreLayoutDownloadActionView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public PLVMediaPlayerMoreLayoutDownloadActionView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(@Nullable AttributeSet attrs) {
        LayoutInflater.from(getContext()).inflate(R.layout.plv_media_player_ui_component_more_download_action_layout, this);
        downloadActionIv = findViewById(R.id.plv_media_player_download_action_iv);
        downloadProgressLayout = findViewById(R.id.plv_media_player_download_progress_layout);
        downloadProgressBar = findViewById(R.id.plv_media_player_download_progress_bar);
        downloadProgressTv = findViewById(R.id.plv_media_player_download_progress_tv);
        downloadActionTv = findViewById(R.id.plv_media_player_download_action_tv);

        parseAttrs(attrs);

        setOnClickListener(this);
        setOnLongClickListener(this);
        onViewStateChanged();
    }

    private void parseAttrs(@Nullable AttributeSet attrs) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.PLVMediaPlayerMoreLayoutDownloadActionView);
        int iconColor = typedArray.getColor(R.styleable.PLVMediaPlayerMoreLayoutDownloadActionView_plvIconTintNormal, Color.WHITE);
        int textColor = typedArray.getColor(R.styleable.PLVMediaPlayerMoreLayoutDownloadActionView_plvTextColorNormal, Color.WHITE);
        typedArray.recycle();
        downloadActionIv.setColorFilter(iconColor);
        downloadActionTv.setTextColor(textColor);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (isInEditMode()) return;

        requireNotNull(PLVMediaPlayerLocalProvider.localDependScope.on(this).current())
                .get(PLVMPDownloadItemViewModel.class)
                .getDownloadItem()
                .observeUntilViewDetached(this, new Function1<PLVMPDownloadItemViewState, Unit>() {
                    @Override
                    public Unit invoke(PLVMPDownloadItemViewState viewState) {
                        if (viewState == null) {
                            return null;
                        }
                        downloadStatus = viewState.getStatus();
                        downloadProgress = viewState.getProgress();
                        isVisible = viewState.isVisible();
                        onViewStateChanged();
                        return null;
                    }
                });
    }

    private void onViewStateChanged() {
        PLVRememberState.rememberStateOf(this, "updateDownloadStatus")
                .compareLastAndSet(downloadStatus)
                .ifNotEquals(new Function1<PLVRememberStateCompareResult, Unit>() {
                    @Override
                    public Unit invoke(PLVRememberStateCompareResult result) {
                        updateDownloadStatus();
                        return null;
                    }
                });

        PLVRememberState.rememberStateOf(this, "updateDownloadProgress")
                .compareLastAndSet(downloadProgress)
                .ifNotEquals(new Function1<PLVRememberStateCompareResult, Unit>() {
                    @Override
                    public Unit invoke(PLVRememberStateCompareResult result) {
                        updateDownloadProgress();
                        return null;
                    }
                });

        PLVRememberState.rememberStateOf(this, "updateVisibleState")
                .compareLastAndSet(isVisible)
                .ifNotEquals(new Function1<PLVRememberStateCompareResult, Unit>() {
                    @Override
                    public Unit invoke(PLVRememberStateCompareResult plvRememberStateCompareResult) {
                        updateVisibleState();
                        return null;
                    }
                });
    }

    private void updateDownloadStatus() {
        final String downloadText;
        final boolean isShowDownloadProgress;
        if (downloadStatus instanceof PLVMediaDownloadStatus.DOWNLOADING) {
            downloadText = getContext().getString(R.string.plv_media_player_ui_component_download_text_downloading);
            isShowDownloadProgress = true;
        } else if (downloadStatus instanceof PLVMediaDownloadStatus.WAITING) {
            downloadText = getContext().getString(R.string.plv_media_player_ui_component_download_text_waiting);
            isShowDownloadProgress = true;
        } else if (downloadStatus instanceof PLVMediaDownloadStatus.COMPLETED) {
            downloadText = getContext().getString(R.string.plv_media_player_ui_component_download_text_completed);
            isShowDownloadProgress = false;
        } else if (downloadStatus instanceof PLVMediaDownloadStatus.ERROR) {
            downloadText = getContext().getString(R.string.plv_media_player_ui_component_download_text_failed);
            isShowDownloadProgress = false;
        } else {
            downloadText = getContext().getString(R.string.plv_media_player_ui_component_download_text);
            isShowDownloadProgress = false;
        }
        downloadActionTv.setText(downloadText);
        downloadActionIv.setVisibility(isShowDownloadProgress ? GONE : VISIBLE);
        downloadProgressLayout.setVisibility(isShowDownloadProgress ? VISIBLE : GONE);
    }

    private void updateDownloadProgress() {
        int progressPercent = (int) (downloadProgress * 100);
        downloadProgressBar.setProgress(progressPercent);
        downloadProgressTv.setText(format("{}%", progressPercent));
    }

    private void updateVisibleState() {
        setVisibility(isVisible ? VISIBLE : GONE);
    }

    @Override
    public void onClick(View v) {
        if (downloadStatus instanceof PLVMediaDownloadStatus.NOT_STARTED
                || downloadStatus instanceof PLVMediaDownloadStatus.PAUSED
                || downloadStatus instanceof PLVMediaDownloadStatus.ERROR) {
            requireNotNull(PLVMediaPlayerLocalProvider.localDependScope.on(this).current())
                    .get(PLVMPDownloadItemViewModel.class)
                    .startDownload();
        } else {
            gotoDownloadCenter();
        }
    }

    @Override
    public boolean onLongClick(View v) {
        gotoDownloadCenter();
        return true;
    }

    private void gotoDownloadCenter() {
        final boolean gotoDownloadingTab = downloadStatus instanceof PLVMediaDownloadStatus.PAUSED
                || downloadStatus instanceof PLVMediaDownloadStatus.WAITING
                || downloadStatus instanceof PLVMediaDownloadStatus.DOWNLOADING
                || downloadStatus instanceof PLVMediaDownloadStatus.ERROR;
        PLVMediaPlayerRouter.finish(RouterDestination.DownloadCenter.class);
        PLVMediaPlayerRouter.routerTo(getContext(),
                new RouterDestination.DownloadCenter(
                        new RouterPayload.DownloadCenterPayload(gotoDownloadingTab ? 1 : 0)
                )
        );
    }

}
