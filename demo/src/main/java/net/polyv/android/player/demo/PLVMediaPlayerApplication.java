package net.polyv.android.player.demo;

import android.app.Application;

import com.plv.foundationsdk.log.PLVCommonLog;

/**
 * @author Hoshiiro
 */
public class PLVMediaPlayerApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        PLVCommonLog.setDebug(true);
    }

}
