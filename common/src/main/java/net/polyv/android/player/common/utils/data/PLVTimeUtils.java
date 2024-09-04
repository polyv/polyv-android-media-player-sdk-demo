package net.polyv.android.player.common.utils.data;

import static net.polyv.android.player.sdk.foundation.lang.Duration.millis;

import net.polyv.android.player.sdk.foundation.lang.Duration;

import java.util.Locale;

/**
 * @author Hoshiiro
 */
public class PLVTimeUtils {

    public static String formatTime(long milliseconds) {
        return formatTime(millis(milliseconds));
    }

    public static String formatTime(Duration duration) {
        if (duration.toHours() > 0) {
            return String.format(Locale.getDefault(), "%02d:%02d:%02d", duration.toHours(), duration.toMinutes() % 60, duration.toSeconds() % 60);
        } else {
            return String.format(Locale.getDefault(), "%02d:%02d", duration.toMinutes(), duration.toSeconds() % 60);
        }
    }

}
