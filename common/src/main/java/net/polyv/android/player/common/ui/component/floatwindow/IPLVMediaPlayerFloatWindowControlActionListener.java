package net.polyv.android.player.common.ui.component.floatwindow;

import android.os.Bundle;
import android.support.annotation.NonNull;

/**
 * @author Hoshiiro
 */
public interface IPLVMediaPlayerFloatWindowControlActionListener {

    void onAfterFloatWindowShow(int reason);

    void onClickContentGoBack(@NonNull Bundle bundle);

    void onClickClose();

}
