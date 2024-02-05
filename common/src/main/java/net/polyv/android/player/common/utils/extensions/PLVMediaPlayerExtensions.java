package net.polyv.android.player.common.utils.extensions;

import static com.plv.foundationsdk.utils.PLVSugarUtil.clamp;

import androidx.annotation.NonNull;

import com.plv.foundationsdk.component.exts.Nullables;
import com.plv.foundationsdk.utils.PLVSugarUtil;

import net.polyv.android.player.business.scene.common.player.IPLVMediaPlayer;

/**
 * @author Hoshiiro
 */
public class PLVMediaPlayerExtensions {

    public static void seekTo(@NonNull final IPLVMediaPlayer mediaPlayer, long position) {
        final long duration = Nullables.of(new PLVSugarUtil.Supplier<Long>() {
            @Override
            public Long get() {
                return mediaPlayer.getStateListenerRegistry().getDurationState().getValue();
            }
        }).getOrDefault(0L);
        final long nextPosition = clamp(position, 0, duration);
        mediaPlayer.seek(nextPosition);
        if (nextPosition < duration) {
            mediaPlayer.start();
        } else {
            mediaPlayer.pause();
        }
    }

}
