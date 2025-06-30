package net.polyv.android.player.scenes.download.list

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import net.polyv.android.player.common.modules.download.list.viewstate.PLVMPDownloadListItemViewState
import net.polyv.android.player.common.ui.router.PLVMediaPlayerRouter
import net.polyv.android.player.common.ui.router.PLVMediaPlayerRouter.routerTo
import net.polyv.android.player.common.ui.router.RouterDestination
import net.polyv.android.player.common.ui.router.RouterPayload
import net.polyv.android.player.common.utils.data.PLVTimeUtils
import net.polyv.android.player.common.utils.ui.PLVRoundRectConstraintLayout
import net.polyv.android.player.scenes.download.R
import net.polyv.android.player.scenes.download.list.DownloadViewHolder.ViewType.Companion.viewType
import net.polyv.android.player.sdk.addon.download.common.model.vo.PLVMediaDownloadStatus
import net.polyv.android.player.sdk.foundation.graphics.getScreenWidth
import net.polyv.android.player.sdk.foundation.lang.FileSize.Companion.bytes
import net.polyv.android.player.sdk.foundation.lang.MutableObserver
import net.polyv.android.player.sdk.foundation.lang.MutableObserver.Companion.disposeAll
import net.polyv.android.player.sdk.foundation.lang.State
import net.polyv.android.player.sdk.foundation.lang.toFixed
import java.util.Locale

/**
 * @author Hoshiiro
 */
internal class PLVMediaPlayerDownloadAdapter : RecyclerView.Adapter<DownloadViewHolder>() {

    private val downloadListItems: MutableList<State<PLVMPDownloadListItemViewState>> = mutableListOf()

    override fun getItemViewType(position: Int): Int {
        return downloadListItems[position].value?.viewType() ?: 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DownloadViewHolder {
        return when (DownloadViewHolder.ViewType.values()[viewType]) {
            DownloadViewHolder.ViewType.COMPLETED -> DownloadCompletedViewHolder(parent)
            DownloadViewHolder.ViewType.DOWNLOADING -> DownloadingViewHolder(parent)
        }
    }

    override fun getItemCount(): Int = downloadListItems.size

    override fun onBindViewHolder(holder: DownloadViewHolder, position: Int) {
        holder.bind(downloadListItems[position])
    }

    override fun onViewRecycled(holder: DownloadViewHolder) {
        holder.unbind()
    }

    fun update(list: List<State<PLVMPDownloadListItemViewState>>) {
        val oldList = downloadListItems.toMutableList()
        val newList = list.toMutableList()
        downloadListItems.clear()
        downloadListItems.addAll(list)

        DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = oldList.size

            override fun getNewListSize() = newList.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return oldList[oldItemPosition].value?.downloader == newList[newItemPosition].value?.downloader
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return oldList[oldItemPosition].value?.equals(newList[newItemPosition].value) ?: false
            }
        }).dispatchUpdatesTo(this)
    }

}

internal sealed class DownloadViewHolder(view: View) : ViewHolder(view) {

    init {
        itemView.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {
                onAttached()
            }

            override fun onViewDetachedFromWindow(v: View) {

            }
        })
    }

    abstract fun bind(item: State<PLVMPDownloadListItemViewState>)
    abstract fun onAttached()
    abstract fun unbind()

    protected fun observeCoverImage(item: State<PLVMPDownloadListItemViewState>, imageView: ImageView) =
        item.observeUntilViewDetached(imageView) { viewState ->
            Glide.with(itemView)
                .load(viewState.coverImage)
                .apply(RequestOptions.placeholderOf(R.drawable.plv_media_player_download_item_default_cover_image))
                .into(imageView)
        }

    protected fun observeDuration(item: State<PLVMPDownloadListItemViewState>, textView: TextView) =
        item.observeUntilViewDetached(textView) { viewState ->
            textView.text = PLVTimeUtils.formatTime(viewState.duration)
        }

    protected fun observeTitle(item: State<PLVMPDownloadListItemViewState>, textView: TextView) =
        item.observeUntilViewDetached(textView) { viewState ->
            textView.text = viewState.title
        }

    protected fun observeBitRateFileSize(item: State<PLVMPDownloadListItemViewState>, textView: TextView) =
        item.observeUntilViewDetached(textView) { viewState ->
            val bitRateText = viewState.bitRate.name
            val size = viewState.fileSize.bytes().toSuitableUnit()
            val sizeText = "${String.format(Locale.getDefault(), "%.1f", size.value)} ${size.unit.abbr}"
            textView.text = "$bitRateText:$sizeText"
        }

    protected fun observeProgress(item: State<PLVMPDownloadListItemViewState>, progressBar: ProgressBar) =
        item.observeUntilViewDetached(progressBar) { viewState ->
            progressBar.progress = (viewState.progress * 100).toInt()
        }

    protected fun observeDownloadIcon(
        item: State<PLVMPDownloadListItemViewState>,
        icon: ImageView
    ): MutableObserver<*> {
        icon.setOnClickListener {
            if (item.value?.downloadStatus?.isRunningDownload() == true) {
                item.value?.pauseDownload()
            } else {
                item.value?.startDownload()
            }
        }

        return item.observeUntilViewDetached(icon) { viewState ->
            if (viewState.downloadStatus.isRunningDownload()) {
                icon.setImageResource(R.drawable.plv_media_player_download_item_download_icon_to_pause)
            } else {
                icon.setImageResource(R.drawable.plv_media_player_download_item_download_icon_to_start)
            }
        }
    }

    protected fun observeStatusProgress(item: State<PLVMPDownloadListItemViewState>, textView: TextView) =
        item.observeUntilViewDetached(textView) { viewState ->
            val status = when (viewState.downloadStatus) {
                is PLVMediaDownloadStatus.NOT_STARTED, PLVMediaDownloadStatus.PAUSED -> itemView.context.getString(net.polyv.android.player.common.R.string.plv_media_player_ui_component_download_text_paused)
                is PLVMediaDownloadStatus.WAITING -> itemView.context.getString(net.polyv.android.player.common.R.string.plv_media_player_ui_component_download_text_waiting)
                is PLVMediaDownloadStatus.DOWNLOADING -> itemView.context.getString(net.polyv.android.player.common.R.string.plv_media_player_ui_component_download_text_downloading)
                is PLVMediaDownloadStatus.COMPLETED -> itemView.context.getString(net.polyv.android.player.common.R.string.plv_media_player_ui_component_download_text_completed)
                is PLVMediaDownloadStatus.ERROR -> itemView.context.getString(net.polyv.android.player.common.R.string.plv_media_player_ui_component_download_text_failed)
            }
            val progressText = "${(viewState.progress * 100).toFixed(1)}%"
            val downloadSpeed = viewState.downloadBytesPerSecond.bytes().toSuitableUnit()
            val downloadSpeedText = "(${downloadSpeed.value.toFixed(1)} ${downloadSpeed.unit.abbr}/s)"
            textView.text = when (viewState.downloadStatus) {
                is PLVMediaDownloadStatus.DOWNLOADING -> "$status $progressText $downloadSpeedText"
                is PLVMediaDownloadStatus.WAITING -> "$status $progressText"
                else -> status
            }
        }

    protected fun <T : View> findViewById(id: Int): T = itemView.findViewById(id)

    enum class ViewType {
        COMPLETED,
        DOWNLOADING
        ;

        companion object {
            fun PLVMPDownloadListItemViewState.viewType(): Int = when (this.downloadStatus) {
                is PLVMediaDownloadStatus.COMPLETED -> COMPLETED.ordinal
                else -> DOWNLOADING.ordinal
            }
        }
    }

}

internal class DownloadCompletedViewHolder(
    parent: ViewGroup
) : DownloadViewHolder(
    LayoutInflater.from(parent.context).inflate(R.layout.plv_media_player_download_item_completed_layout, parent, false)
) {

    private val downloadItemRoot: HorizontalScrollView by lazy { findViewById<HorizontalScrollView>(R.id.plv_media_player_download_item_root) }
    private val downloadItemLayout: ConstraintLayout by lazy { findViewById<ConstraintLayout>(R.id.plv_media_player_download_item_layout) }
    private val downloadCoverImageLayout: PLVRoundRectConstraintLayout by lazy {
        findViewById<PLVRoundRectConstraintLayout>(
            R.id.plv_media_player_download_cover_image_layout
        )
    }
    private val downloadCoverImageIv: ImageView by lazy { findViewById<ImageView>(R.id.plv_media_player_download_cover_image_iv) }
    private val downloadDurationTv: TextView by lazy { findViewById<TextView>(R.id.plv_media_player_download_duration_tv) }
    private val downloadTitleTv: TextView by lazy { findViewById<TextView>(R.id.plv_media_player_download_title_tv) }
    private val downloadFileSizeTv: TextView by lazy { findViewById<TextView>(R.id.plv_media_player_download_file_size_tv) }
    private val downloadItemDeleteTv: TextView by lazy { findViewById<TextView>(R.id.plv_media_player_download_item_delete_tv) }

    private var item: State<PLVMPDownloadListItemViewState>? = null
    private val observers: MutableList<MutableObserver<*>> = mutableListOf()

    init {
        downloadItemLayout.layoutParams = downloadItemLayout.layoutParams.apply {
            width = getScreenWidth().px()
        }
    }

    override fun bind(item: State<PLVMPDownloadListItemViewState>) {
        unbind()
        this.item = item
        downloadItemRoot.scrollTo(0, 0)
    }

    override fun onAttached() {
        observers.disposeAll()
        observers.clear()
        val item = this.item ?: return

        observeCoverImage(item, downloadCoverImageIv).addTo(observers)
        observeDuration(item, downloadDurationTv).addTo(observers)
        observeTitle(item, downloadTitleTv).addTo(observers)
        observeBitRateFileSize(item, downloadFileSizeTv).addTo(observers)
        downloadItemDeleteTv.setOnClickListener { item.value?.deleteDownload() }
        downloadItemLayout.setOnClickListener { downloadItemRoot.smoothScrollTo(0, 0) }
        downloadCoverImageIv.setOnClickListener { gotoDownloadedSingleVideo(item) }
    }

    private fun gotoDownloadedSingleVideo(item: State<PLVMPDownloadListItemViewState>) {
        val downloader = item.value?.downloader ?: return
        PLVMediaPlayerRouter.finish(RouterDestination.SceneSingle::class.java, RouterDestination.SceneFeed::class.java)
        itemView.context.routerTo(
            RouterDestination.SceneSingle(
                RouterPayload.SceneSinglePayload(
                    downloader.mediaResource,
                    enterFromDownloadCenter = true
                )
            )
        )
    }

    override fun unbind() {
        this.item = null
    }

}

internal class DownloadingViewHolder(
    parent: ViewGroup
) : DownloadViewHolder(
    LayoutInflater.from(parent.context)
        .inflate(R.layout.plv_media_player_download_item_downloading_layout, parent, false)
) {

    private val downloadItemRoot: HorizontalScrollView by lazy { findViewById<HorizontalScrollView>(R.id.plv_media_player_download_item_root) }
    private val downloadItemLayout: ConstraintLayout by lazy { findViewById<ConstraintLayout>(R.id.plv_media_player_download_item_layout) }
    private val downloadCoverImageLayout: PLVRoundRectConstraintLayout by lazy {
        findViewById<PLVRoundRectConstraintLayout>(
            R.id.plv_media_player_download_cover_image_layout
        )
    }
    private val downloadCoverImageIv: ImageView by lazy { findViewById<ImageView>(R.id.plv_media_player_download_cover_image_iv) }
    private val downloadDurationTv: TextView by lazy { findViewById<TextView>(R.id.plv_media_player_download_duration_tv) }
    private val downloadTitleTv: TextView by lazy { findViewById<TextView>(R.id.plv_media_player_download_title_tv) }
    private val downloadFileSizeTv: TextView by lazy { findViewById<TextView>(R.id.plv_media_player_download_file_size_tv) }
    private val downloadStatusProgressTv: TextView by lazy { findViewById<TextView>(R.id.plv_media_player_download_status_progress_tv) }
    private val downloadStatusProgressBar: ProgressBar by lazy { findViewById<ProgressBar>(R.id.plv_media_player_download_status_progress_bar) }
    private val downloadItemDownloadIcon: ImageView by lazy { findViewById<ImageView>(R.id.plv_media_player_download_item_download_icon) }
    private val downloadItemDeleteTv: TextView by lazy { findViewById<TextView>(R.id.plv_media_player_download_item_delete_tv) }

    private var item: State<PLVMPDownloadListItemViewState>? = null
    private val observers: MutableList<MutableObserver<*>> = mutableListOf()

    init {
        downloadItemLayout.layoutParams = downloadItemLayout.layoutParams.apply {
            width = getScreenWidth().px()
        }
    }

    override fun bind(item: State<PLVMPDownloadListItemViewState>) {
        unbind()
        this.item = item
        downloadItemRoot.scrollTo(0, 0)
    }

    override fun onAttached() {
        observers.disposeAll()
        observers.clear()
        val item = this.item ?: return

        observeCoverImage(item, downloadCoverImageIv).addTo(observers)
        observeDuration(item, downloadDurationTv).addTo(observers)
        observeTitle(item, downloadTitleTv).addTo(observers)
        observeBitRateFileSize(item, downloadFileSizeTv).addTo(observers)
        observeProgress(item, downloadStatusProgressBar).addTo(observers)
        observeDownloadIcon(item, downloadItemDownloadIcon).addTo(observers)
        observeStatusProgress(item, downloadStatusProgressTv).addTo(observers)
        downloadItemDeleteTv.setOnClickListener { item.value?.deleteDownload() }
        downloadItemLayout.setOnClickListener { downloadItemRoot.smoothScrollTo(0, 0) }
    }

    override fun unbind() {
        this.item = null
    }

}