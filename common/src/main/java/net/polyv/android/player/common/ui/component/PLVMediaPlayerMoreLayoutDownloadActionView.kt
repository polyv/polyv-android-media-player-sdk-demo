package net.polyv.android.player.common.ui.component

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnLongClickListener
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import net.polyv.android.player.common.R
import net.polyv.android.player.common.di.PLVMediaPlayerLocalProvider
import net.polyv.android.player.common.modules.download.single.viewmodel.PLVMPDownloadItemViewModel
import net.polyv.android.player.common.ui.router.PLVMediaPlayerRouter.routerTo
import net.polyv.android.player.common.ui.router.RouterDestination.DownloadCenter
import net.polyv.android.player.common.ui.router.RouterPayload.DownloadCenterPayload
import net.polyv.android.player.sdk.addon.download.common.model.vo.PLVMediaDownloadStatus
import net.polyv.android.player.sdk.addon.download.common.model.vo.PLVMediaDownloadStatus.COMPLETED
import net.polyv.android.player.sdk.addon.download.common.model.vo.PLVMediaDownloadStatus.DOWNLOADING
import net.polyv.android.player.sdk.addon.download.common.model.vo.PLVMediaDownloadStatus.NOT_STARTED
import net.polyv.android.player.sdk.addon.download.common.model.vo.PLVMediaDownloadStatus.PAUSED
import net.polyv.android.player.sdk.addon.download.common.model.vo.PLVMediaDownloadStatus.WAITING
import net.polyv.android.player.sdk.foundation.lang.PLVRememberState.Companion.rememberStateOf
import net.polyv.android.player.sdk.foundation.lang.format

/**
 * @author Hoshiiro
 */
class PLVMediaPlayerMoreLayoutDownloadActionView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), View.OnClickListener, OnLongClickListener {
    private val downloadActionIv by lazy { findViewById<ImageView>(R.id.plv_media_player_download_action_iv) }
    private val downloadProgressLayout by lazy { findViewById<FrameLayout>(R.id.plv_media_player_download_progress_layout) }
    private val downloadProgressBar by lazy { findViewById<ProgressBar>(R.id.plv_media_player_download_progress_bar) }
    private val downloadProgressTv by lazy { findViewById<TextView>(R.id.plv_media_player_download_progress_tv) }
    private val downloadActionTv by lazy { findViewById<TextView>(R.id.plv_media_player_download_action_tv) }

    private var downloadStatus: PLVMediaDownloadStatus = NOT_STARTED
    private var downloadProgress = 0f
    private var isVisible = false

    init {
        LayoutInflater.from(context).inflate(R.layout.plv_media_player_ui_component_more_download_action_layout, this)

        parseAttrs(attrs)

        setOnClickListener(this)
        setOnLongClickListener(this)
        onViewStateChanged()
    }

    private fun parseAttrs(attrs: AttributeSet?) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.PLVMediaPlayerMoreLayoutDownloadActionView)
        val iconColor = typedArray.getColor(
            R.styleable.PLVMediaPlayerMoreLayoutDownloadActionView_plv_icon_tint_normal,
            Color.WHITE
        )
        val textColor = typedArray.getColor(
            R.styleable.PLVMediaPlayerMoreLayoutDownloadActionView_plv_text_color_normal,
            Color.WHITE
        )
        typedArray.recycle()
        downloadActionIv.setColorFilter(iconColor)
        downloadActionTv.setTextColor(textColor)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (isInEditMode) return

        PLVMediaPlayerLocalProvider.localDependScope.on(this).current()!!
            .get<PLVMPDownloadItemViewModel>()
            .downloadItem
            .observeUntilViewDetached(this) { viewState ->
                if (viewState == null) {
                    return@observeUntilViewDetached
                }
                downloadStatus = viewState.status
                downloadProgress = viewState.progress
                isVisible = viewState.isVisible
                onViewStateChanged()
            }
    }

    private fun onViewStateChanged() {
        rememberStateOf("updateDownloadStatus")
            .compareLastAndSet(downloadStatus)
            .ifNotEquals {
                updateDownloadStatus()
            }

        rememberStateOf("updateDownloadProgress")
            .compareLastAndSet(downloadProgress)
            .ifNotEquals {
                updateDownloadProgress()
            }

        rememberStateOf("updateVisibleState")
            .compareLastAndSet(isVisible)
            .ifNotEquals {
                updateVisibleState()
            }
    }

    private fun updateDownloadStatus() {
        val downloadText: String?
        val isShowDownloadProgress: Boolean
        when (downloadStatus) {
            is DOWNLOADING -> {
                downloadText = context.getString(R.string.plv_media_player_ui_component_download_text_downloading)
                isShowDownloadProgress = true
            }

            is WAITING -> {
                downloadText = context.getString(R.string.plv_media_player_ui_component_download_text_waiting)
                isShowDownloadProgress = true
            }

            is COMPLETED -> {
                downloadText = context.getString(R.string.plv_media_player_ui_component_download_text_completed)
                isShowDownloadProgress = false
            }

            is PLVMediaDownloadStatus.ERROR -> {
                downloadText = context.getString(R.string.plv_media_player_ui_component_download_text_failed)
                isShowDownloadProgress = false
            }

            else -> {
                downloadText = context.getString(R.string.plv_media_player_ui_component_download_text)
                isShowDownloadProgress = false
            }
        }
        downloadActionTv.text = downloadText
        downloadActionIv.setVisibility(if (isShowDownloadProgress) GONE else VISIBLE)
        downloadProgressLayout.visibility = if (isShowDownloadProgress) VISIBLE else GONE
    }

    private fun updateDownloadProgress() {
        val progressPercent = (downloadProgress * 100).toInt()
        downloadProgressBar.progress = progressPercent
        downloadProgressTv.text = "{}%".format(progressPercent)
    }

    private fun updateVisibleState() {
        visibility = if (isVisible) VISIBLE else GONE
    }

    override fun onClick(v: View?) {
        if (downloadStatus is NOT_STARTED
            || downloadStatus is PAUSED
            || downloadStatus is PLVMediaDownloadStatus.ERROR
        ) {
            PLVMediaPlayerLocalProvider.localDependScope.on(this).current()!!
                .get<PLVMPDownloadItemViewModel>()
                .startDownload()
        } else {
            gotoDownloadCenter()
        }
    }

    override fun onLongClick(v: View?): Boolean {
        gotoDownloadCenter()
        return true
    }

    private fun gotoDownloadCenter() {
        val gotoDownloadingTab = downloadStatus is PAUSED
                || downloadStatus is WAITING
                || downloadStatus is DOWNLOADING
                || downloadStatus is PLVMediaDownloadStatus.ERROR
        context.routerTo(
            DownloadCenter(
                DownloadCenterPayload(if (gotoDownloadingTab) 1 else 0)
            )
        )
    }
}
