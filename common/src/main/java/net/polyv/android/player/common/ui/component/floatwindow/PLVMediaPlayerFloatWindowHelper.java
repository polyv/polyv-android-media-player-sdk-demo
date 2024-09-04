package net.polyv.android.player.common.ui.component.floatwindow;

import static net.polyv.android.player.sdk.foundation.graphics.DisplaysKt.dp;
import static net.polyv.android.player.sdk.foundation.graphics.DisplaysKt.getScreenHeight;
import static net.polyv.android.player.sdk.foundation.graphics.DisplaysKt.getScreenWidth;

import android.graphics.Point;
import android.graphics.Rect;
import androidx.annotation.Nullable;

/**
 * @author Hoshiiro
 */
public class PLVMediaPlayerFloatWindowHelper {

    private static final int FLOAT_WINDOW_WIDTH_LANDSCAPE = dp(176).px();
    private static final int FLOAT_WINDOW_WIDTH_PORTRAIT = dp(153).px();
    private static final int FLOAT_WINDOW_SIZE_MAX = dp(278).px();

    @Nullable
    public static Rect calculateFloatWindowPosition(@Nullable Rect videoSize) {
        Point currentFloatWindowLocation = PLVMediaPlayerFloatWindowManager.getInstance().getFloatWindowLocation();
        Rect newFloatWindowLocation = newFloatWindowLocation(videoSize);
        if (currentFloatWindowLocation == null || newFloatWindowLocation == null) {
            return newFloatWindowLocation;
        }
        int screenWidth = getScreenWidth().px();
        if (currentFloatWindowLocation.x < screenWidth / 2) {
            return new Rect(
                    currentFloatWindowLocation.x,
                    currentFloatWindowLocation.y,
                    currentFloatWindowLocation.x + newFloatWindowLocation.width(),
                    currentFloatWindowLocation.y + newFloatWindowLocation.height()
            );
        } else {
            return new Rect(
                    newFloatWindowLocation.left,
                    currentFloatWindowLocation.y,
                    newFloatWindowLocation.right,
                    currentFloatWindowLocation.y + newFloatWindowLocation.height()
            );
        }
    }

    @Nullable
    private static Rect newFloatWindowLocation(@Nullable Rect videoSize) {
        boolean isPortrait = videoSize.height() > videoSize.width();
        double whRatio = ((double) videoSize.width()) / videoSize.height();
        int width = isPortrait ? FLOAT_WINDOW_WIDTH_PORTRAIT : FLOAT_WINDOW_WIDTH_LANDSCAPE;
        int height = (int) (width / whRatio);
        if (height > FLOAT_WINDOW_SIZE_MAX) {
            height = FLOAT_WINDOW_SIZE_MAX;
            width = (int) (height * whRatio);
        }
        int left = getScreenWidth().px() - width - dp(6).px();
        int top = getScreenHeight().px() - height - dp(42).px();
        return new Rect(left, top, left + width, top + height);
    }

}
