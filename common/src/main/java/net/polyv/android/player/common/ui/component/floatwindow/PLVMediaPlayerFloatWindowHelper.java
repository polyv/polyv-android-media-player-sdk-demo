package net.polyv.android.player.common.ui.component.floatwindow;

import android.graphics.Point;
import android.graphics.Rect;
import androidx.annotation.Nullable;

import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;

import net.polyv.android.player.business.scene.common.player.IPLVMediaPlayer;

/**
 * @author Hoshiiro
 */
public class PLVMediaPlayerFloatWindowHelper {

    private static final int FLOAT_WINDOW_WIDTH_LANDSCAPE = ConvertUtils.dp2px(176);
    private static final int FLOAT_WINDOW_WIDTH_PORTRAIT = ConvertUtils.dp2px(153);
    private static final int FLOAT_WINDOW_SIZE_MAX = ConvertUtils.dp2px(278);

    @Nullable
    public static Rect calculateFloatWindowPosition(@Nullable IPLVMediaPlayer mediaPlayer) {
        Point currentFloatWindowLocation = PLVMediaPlayerFloatWindowManager.getInstance().getFloatWindowLocation();
        Rect newFloatWindowLocation = newFloatWindowLocation(mediaPlayer);
        if (currentFloatWindowLocation == null || newFloatWindowLocation == null) {
            return newFloatWindowLocation;
        }
        int screenWidth = ScreenUtils.getScreenOrientatedWidth();
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
    private static Rect newFloatWindowLocation(@Nullable IPLVMediaPlayer mediaPlayer) {
        if (mediaPlayer == null) {
            return null;
        }
        Rect videoSize = mediaPlayer.getStateListenerRegistry().getVideoSize().getValue();
        if (videoSize == null) {
            return null;
        }
        boolean isPortrait = videoSize.height() > videoSize.width();
        double whRatio = ((double) videoSize.width()) / videoSize.height();
        int width = isPortrait ? FLOAT_WINDOW_WIDTH_PORTRAIT : FLOAT_WINDOW_WIDTH_LANDSCAPE;
        int height = (int) (width / whRatio);
        if (height > FLOAT_WINDOW_SIZE_MAX) {
            height = FLOAT_WINDOW_SIZE_MAX;
            width = (int) (height * whRatio);
        }
        int left = ScreenUtils.getScreenOrientatedWidth() - width - ConvertUtils.dp2px(6);
        int top = ScreenUtils.getScreenOrientatedHeight() - height - ConvertUtils.dp2px(42);
        return new Rect(left, top, left + width, top + height);
    }

}
