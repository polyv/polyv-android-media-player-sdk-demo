package net.polyv.android.player.common.utils.ui;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.view.View;

/**
 * @author Hoshiiro
 */
public abstract class PLVOnDoubleClickListener implements View.OnClickListener {

    private static final long DELAY_DOUBLE_CLICK_CHECK = 200;
    private static final int MSG_DELAY_SINGLE_CLICK = 1;

    private final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what == MSG_DELAY_SINGLE_CLICK) {
                onSingleClick();
            }
        }
    };

    private long lastClickTimestamp;

    @Override
    public void onClick(View v) {
        if (lastClickTimestamp > System.currentTimeMillis() - DELAY_DOUBLE_CLICK_CHECK) {
            onDoubleClick();
            handler.removeMessages(MSG_DELAY_SINGLE_CLICK);
        } else {
            lastClickTimestamp = System.currentTimeMillis();
            handler.sendEmptyMessageDelayed(MSG_DELAY_SINGLE_CLICK, DELAY_DOUBLE_CLICK_CHECK);
        }
    }

    protected void onSingleClick() {

    }

    protected void onDoubleClick() {

    }

}
