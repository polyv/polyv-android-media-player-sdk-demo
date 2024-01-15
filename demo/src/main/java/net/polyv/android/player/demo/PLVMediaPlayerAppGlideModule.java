package net.polyv.android.player.demo;

import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;

/**
 * 如果项目本身已经有 AppGlideModule，请移除本类，否则会导致 Glide 初始化失败
 *
 * @author Hoshiiro
 * @see net.polyv.android.player.common.utils.ui.image.glide.module.PLVMediaPlayerGlideModule
 */
@GlideModule
public class PLVMediaPlayerAppGlideModule extends AppGlideModule {
}
