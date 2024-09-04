package net.polyv.android.player.common.utils.ui.image.glide.decoder;

import static net.polyv.android.player.sdk.foundation.lang.PreconditionsKt.requireNotNull;

import android.graphics.Bitmap;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.NonNull;

import com.bumptech.glide.load.Option;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.ResourceDecoder;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapResource;

import net.polyv.android.player.sdk.foundation.lang.Duration;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author Hoshiiro
 */
public class PLVSeekProgressPreviewImageDecoder implements ResourceDecoder<ByteBuffer, Bitmap> {

    public static final Option<Boolean> USE_SEEK_PROGRESS_PREVIEW_IMAGE_DECODER = Option.memory(PLVSeekProgressPreviewImageDecoder.class.getName(), false);
    public static final Option<Integer> SEEK_PROGRESS_PREVIEW_IMAGE_INDEX = Option.memory("PLV_SEEK_PROGRESS_PREVIEW_IMAGE_INDEX");

    private static final int PREVIEW_IMAGES_EACH_ROW = 50;
    private static final int PREVIEW_IMAGES_WIDTH = 160;
    private static final int PREVIEW_IMAGES_HEIGHT = 90;

    private final BitmapPool bitmapPool;

    public PLVSeekProgressPreviewImageDecoder(BitmapPool bitmapPool) {
        this.bitmapPool = bitmapPool;
    }

    public static int calculateIndex(long targetProgressSeconds, Duration interval) {
        return (int) (targetProgressSeconds / interval.toSeconds());
    }

    @Override
    public boolean handles(@NonNull @NotNull ByteBuffer source, @NonNull @NotNull Options options) throws IOException {
        return Boolean.TRUE.equals(options.get(USE_SEEK_PROGRESS_PREVIEW_IMAGE_DECODER))
                && options.get(SEEK_PROGRESS_PREVIEW_IMAGE_INDEX) != null;
    }

    @Override
    public Resource<Bitmap> decode(ByteBuffer source, int width, int height, Options options) throws IOException {
        final Rect region = getRegion(options);
        final BitmapRegionDecoder decoder = createDecoder(source);
        final Bitmap bitmap = decoder.decodeRegion(region, null);
        return BitmapResource.obtain(bitmap, bitmapPool);
    }

    private Rect getRegion(Options options) {
        final int index = requireNotNull(options.get(SEEK_PROGRESS_PREVIEW_IMAGE_INDEX));
        final int previewImageColumn = index % PREVIEW_IMAGES_EACH_ROW;
        final int previewImageRow = index / PREVIEW_IMAGES_EACH_ROW;
        return new Rect(
                previewImageColumn * PREVIEW_IMAGES_WIDTH,
                previewImageRow * PREVIEW_IMAGES_HEIGHT,
                previewImageColumn * PREVIEW_IMAGES_WIDTH + PREVIEW_IMAGES_WIDTH,
                previewImageRow * PREVIEW_IMAGES_HEIGHT + PREVIEW_IMAGES_HEIGHT
        );
    }

    private BitmapRegionDecoder createDecoder(ByteBuffer source) throws IOException {
        try {
            byte[] array = new byte[source.remaining()];
            source.get(array);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                return BitmapRegionDecoder.newInstance(array, 0, array.length);
            } else {
                return BitmapRegionDecoder.newInstance(array, 0, array.length, false);
            }
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

}