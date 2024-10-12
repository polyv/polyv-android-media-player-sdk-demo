package net.polyv.android.player.common.ui.component;

import static net.polyv.android.player.sdk.foundation.app.PLVApplicationContext.getString;
import static net.polyv.android.player.sdk.foundation.graphics.DisplaysKt.isLandscape;
import static net.polyv.android.player.sdk.foundation.lang.PreconditionsKt.requireNotNull;
import static net.polyv.android.player.sdk.foundation.lang.StringsKt.format;
import static net.polyv.android.player.sdk.foundation.lang.ThreadsKt.postToWorkerThread;

import android.content.Context;
import android.graphics.Bitmap;
import androidx.appcompat.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.View;

import net.polyv.android.player.common.R;
import net.polyv.android.player.common.di.PLVMediaPlayerLocalProvider;
import net.polyv.android.player.common.modules.media.viewmodel.PLVMPMediaViewModel;
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.PLVMPMediaControllerViewModel;
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.viewstate.PLVMPMediaControllerViewState;
import net.polyv.android.player.common.utils.ui.PLVDebounceClicker;
import net.polyv.android.player.common.utils.ui.PLVToast;
import net.polyv.android.player.sdk.foundation.di.DependScope;
import net.polyv.android.player.sdk.foundation.lang.PLVRememberState;
import net.polyv.android.player.sdk.foundation.lang.PLVRememberStateCompareResult;
import net.polyv.android.player.sdk.foundation.log.PLVMediaPlayerLogger;

import java.io.File;
import java.io.FileOutputStream;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;

/**
 * @author Hoshiiro
 */
public class PLVMediaPlayerScreenshotImageView extends AppCompatImageView {

    private static final String TAG = PLVMediaPlayerScreenshotImageView.class.getSimpleName();

    private boolean isVisible = false;

    public PLVMediaPlayerScreenshotImageView(Context context) {
        super(context);
        init();
    }

    public PLVMediaPlayerScreenshotImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PLVMediaPlayerScreenshotImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setImageResource(R.drawable.plv_media_player_screenshot_icon);
        setOnClickListener(new PLVDebounceClicker.OnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                saveScreenshot();
            }
        }));
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (isInEditMode()) return;

        requireNotNull(PLVMediaPlayerLocalProvider.localDependScope.on(this).current())
                .get(PLVMPMediaControllerViewModel.class)
                .getMediaControllerViewState()
                .observeUntilViewDetached(this, new Function1<PLVMPMediaControllerViewState, Unit>() {
                    @Override
                    public Unit invoke(PLVMPMediaControllerViewState viewState) {
                        isVisible = viewState.getControllerVisible()
                                && !viewState.isMediaStopOverlayVisible()
                                && !viewState.getControllerLocking()
                                && !(viewState.isFloatActionLayoutVisible() && isLandscape());
                        onViewStateChanged();
                        return null;
                    }
                });
    }

    private void onViewStateChanged() {
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

    private void updateVisibleState() {
        setVisibility(isVisible ? VISIBLE : GONE);
    }

    private void saveScreenshot() {
        final DependScope dependScope = PLVMediaPlayerLocalProvider.localDependScope.on(this).current();
        if (dependScope == null) {
            return;
        }
        final PLVMPMediaViewModel viewModel = dependScope.get(PLVMPMediaViewModel.class);
        postToWorkerThread(new Function0<Unit>() {
            @Override
            public Unit invoke() {
                final Bitmap bitmap = viewModel.screenshot();
                if (bitmap == null) {
                    PLVMediaPlayerLogger.error(TAG, "screenshot bitmap is null");
                    showToastScreenshotResult(getString(R.string.plv_media_player_ui_component_screenshot_save_failed));
                    return null;
                }
                final File outputFile = new File(getContext().getExternalFilesDir(null), format("screenshot/{}.png", System.currentTimeMillis()));
                outputFile.getParentFile().mkdirs();
                try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                    showToastScreenshotResult(getString(R.string.plv_media_player_ui_component_screenshot_save_success, "\n" + outputFile.getAbsolutePath()));
                } catch (Throwable e) {
                    PLVMediaPlayerLogger.error(TAG, "screenshot fail to save file: " + outputFile.getAbsolutePath(), e);
                    showToastScreenshotResult(getString(R.string.plv_media_player_ui_component_screenshot_save_failed));
                }
                return null;
            }
        });
    }

    private void showToastScreenshotResult(String msg) {
        PLVToast.Builder.context(getContext())
                .setText(msg)
                .setMaxLines(Integer.MAX_VALUE)
                .longDuration()
                .show();
    }

}
