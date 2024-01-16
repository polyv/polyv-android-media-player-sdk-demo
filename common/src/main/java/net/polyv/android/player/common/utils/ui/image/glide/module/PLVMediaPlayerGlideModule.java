package net.polyv.android.player.common.utils.ui.image.glide.module;

import android.content.Context;
import android.graphics.Bitmap;
import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.LibraryGlideModule;

import net.polyv.android.player.common.utils.ui.image.glide.decoder.PLVSeekProgressPreviewImageDecoder;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

/**
 * @author Hoshiiro
 */
@GlideModule
public class PLVMediaPlayerGlideModule extends LibraryGlideModule {

    @Override
    public void registerComponents(@NonNull @NotNull Context context, @NonNull @NotNull Glide glide, @NonNull @NotNull Registry registry) {
        registry.prepend(ByteBuffer.class, Bitmap.class, new PLVSeekProgressPreviewImageDecoder(glide.getBitmapPool()));
    }

}
