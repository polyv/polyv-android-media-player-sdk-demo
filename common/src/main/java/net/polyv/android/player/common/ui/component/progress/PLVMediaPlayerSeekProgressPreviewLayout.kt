package net.polyv.android.player.common.ui.component.progress

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import net.polyv.android.player.common.R
import net.polyv.android.player.common.di.PLVMediaPlayerLocalProvider
import net.polyv.android.player.common.modules.media.viewmodel.PLVMPMediaViewModel
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.PLVMPMediaControllerViewModel
import net.polyv.android.player.common.utils.ui.PLVRoundRectConstraintLayout
import net.polyv.android.player.common.utils.ui.image.glide.decoder.PLVSeekProgressPreviewImageDecoder
import net.polyv.android.player.sdk.foundation.lang.PLVRememberState.Companion.rememberStateOf
import net.polyv.android.player.sdk.foundation.lang.postToMainThread
import net.polyv.android.player.sdk.foundation.lang.watchStates

/**
 * @author Hoshiiro
 */
class PLVMediaPlayerSeekProgressPreviewLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val seekProgressPreviewTv by lazy { findViewById<PLVMediaPlayerSeekProgressPreviewTextView>(R.id.plv_media_player_seek_progress_preview_tv) }
    private val seekProgressPreviewImageLayout by lazy { findViewById<PLVRoundRectConstraintLayout>(R.id.plv_media_player_seek_progress_preview_image_layout) }
    private val seekProgressPreviewIv by lazy { findViewById<ImageView>(R.id.plv_media_player_seek_progress_preview_iv) }

    private var isLastLoadImageFinish = true
    private var needUpdateImageOnLoadImageFinish = false

    init {
        LayoutInflater.from(context).inflate(R.layout.plv_media_player_ui_component_progress_preview_image_layout, this)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (isInEditMode) return

        val dependScope = PLVMediaPlayerLocalProvider.localDependScope.on(this).current()!!

        watchStates {
            val controllerState = dependScope.get<PLVMPMediaControllerViewModel>().mediaControllerViewState.value
                ?: return@watchStates
            val infoState = dependScope.get<PLVMPMediaViewModel>().mediaInfoViewState.value ?: return@watchStates
            val isProgressSeekBarDragging = controllerState.progressSeekBarDragging
            val previewImageUrl = infoState.progressPreviewImage
            val previewImageInterval = infoState.progressPreviewImageInterval

            val isVisible = isProgressSeekBarDragging && previewImageUrl != null && previewImageInterval != null
            if (!isVisible) {
                seekProgressPreviewImageLayout.visibility = GONE
                return@watchStates
            }
            seekProgressPreviewImageLayout.visibility = VISIBLE

            rememberStateOf("updatePreviewImage")
                .compareLastAndSet(
                    controllerState.progressSeekBarDragPosition,
                    previewImageUrl,
                    previewImageInterval
                )
                .ifNotEquals {
                    updatePreviewImage()
                }
        }.disposeOnDetached(this)
    }

    private fun updatePreviewImage() {
        if (!isLastLoadImageFinish) {
            needUpdateImageOnLoadImageFinish = true
            return
        }
        isLastLoadImageFinish = false
        needUpdateImageOnLoadImageFinish = false

        val dependScope = PLVMediaPlayerLocalProvider.localDependScope.on(this).current() ?: return
        val previewImageUrl = dependScope.get<PLVMPMediaViewModel>().mediaInfoViewState.value?.progressPreviewImage
            ?: return
        val previewImageInterval = dependScope.get<PLVMPMediaViewModel>().mediaInfoViewState.value?.progressPreviewImageInterval
            ?: return
        val progressSeekBarPosition = dependScope.get<PLVMPMediaControllerViewModel>().mediaControllerViewState.value?.progressSeekBarDragPosition
            ?: return

        Glide.with(this)
            .load(previewImageUrl)
            .apply(
                RequestOptions
                    .option(PLVSeekProgressPreviewImageDecoder.USE_SEEK_PROGRESS_PREVIEW_IMAGE_DECODER, true)
                    .set(
                        PLVSeekProgressPreviewImageDecoder.SEEK_PROGRESS_PREVIEW_IMAGE_INDEX,
                        PLVSeekProgressPreviewImageDecoder.calculateIndex(
                            progressSeekBarPosition / 1000,
                            previewImageInterval
                        )
                    )
            )
            .listener(object : RequestListener<Drawable?> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable?>?,
                    isFirstResource: Boolean
                ): Boolean {
                    onLoadFinish()
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable?>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    onLoadFinish()
                    return false
                }

                fun onLoadFinish() {
                    postToMainThread {
                        isLastLoadImageFinish = true
                        if (needUpdateImageOnLoadImageFinish) {
                            updatePreviewImage()
                        }
                    }
                }
            })
            .into(object : SimpleTarget<Drawable?>() {
                override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable?>?) {
                    seekProgressPreviewIv!!.setImageDrawable(resource)
                }
            })
    }
}
