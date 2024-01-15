package net.polyv.android.player.common.ui.localprovider;

import com.plv.foundationsdk.component.localprovider.PLVLocalProvider;

import net.polyv.android.player.business.scene.auxiliary.player.IPLVAuxiliaryMediaPlayer;
import net.polyv.android.player.business.scene.common.player.IPLVMediaPlayer;
import net.polyv.android.player.common.ui.viewmodel.PLVMediaPlayerControlViewModel;
import net.polyv.android.player.common.utils.ui.PLVViewLifecycleObservable;

/**
 * @author Hoshiiro
 */
public class PLVMediaPlayerLocalProvider {

    public static final PLVLocalProvider<IPLVMediaPlayer> localMediaPlayer = new PLVLocalProvider<>();
    public static final PLVLocalProvider<IPLVAuxiliaryMediaPlayer> localAuxiliaryMediaPlayer = new PLVLocalProvider<>();
    public static final PLVLocalProvider<PLVMediaPlayerControlViewModel> localControlViewModel = new PLVLocalProvider<>();
    public static final PLVLocalProvider<PLVViewLifecycleObservable> localLifecycleObservable = new PLVLocalProvider<>();

}
