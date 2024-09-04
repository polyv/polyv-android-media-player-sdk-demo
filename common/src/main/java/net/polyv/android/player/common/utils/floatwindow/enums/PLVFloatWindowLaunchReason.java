package net.polyv.android.player.common.utils.floatwindow.enums;

/**
 * @author Hoshiiro
 */
public class PLVFloatWindowLaunchReason {

    public final int code;

    private PLVFloatWindowLaunchReason(int code) {
        this.code = code;
    }

    public static final PLVFloatWindowLaunchReason MANUAL = new PLVFloatWindowLaunchReason(1);
    public static final PLVFloatWindowLaunchReason BACKGROUND_STATE_CHANGED = new PLVFloatWindowLaunchReason(2);

}
