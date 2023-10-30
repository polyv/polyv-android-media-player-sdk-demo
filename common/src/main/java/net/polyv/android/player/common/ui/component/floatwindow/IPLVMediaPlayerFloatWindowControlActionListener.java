package net.polyv.android.player.common.ui.component.floatwindow;

import android.support.annotation.Nullable;

import net.polyv.android.player.business.scene.common.model.vo.PLVMediaResource;

/**
 * @author Hoshiiro
 */
public interface IPLVMediaPlayerFloatWindowControlActionListener {

    void onAfterFloatWindowShow();

    void onClickContentGoBack(@Nullable PLVMediaResource saveMediaResource);

    void onClickClose();

}
