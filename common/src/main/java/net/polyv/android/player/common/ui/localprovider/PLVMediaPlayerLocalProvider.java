package net.polyv.android.player.common.ui.localprovider;

import com.plv.foundationsdk.component.localprovider.PLVLocalProvider;

import net.polyv.android.player.business.scene.common.player.IPLVMediaPlayer;
import net.polyv.android.player.common.ui.viewmodel.PLVMediaPlayerControlViewModel;

/**
 * @author Hoshiiro
 */
public class PLVMediaPlayerLocalProvider {

    public static final PLVLocalProvider<IPLVMediaPlayer> localMediaPlayer = new PLVLocalProvider<>();
    public static final PLVLocalProvider<PLVMediaPlayerControlViewModel> localControlViewModel = new PLVLocalProvider<>();

}
