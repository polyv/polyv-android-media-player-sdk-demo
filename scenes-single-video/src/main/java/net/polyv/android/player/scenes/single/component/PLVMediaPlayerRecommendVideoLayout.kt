package net.polyv.android.player.scenes.single.component

import androidx.lifecycle.GenericLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import android.content.Context
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import net.polyv.android.common.libs.lang.state.MutableObserver
import net.polyv.android.common.libs.lang.state.MutableObserver.Companion.disposeAll
import net.polyv.android.player.business.scene.common.coroutine.PLVMediaPlayerGlobalCoroutineScope
import net.polyv.android.player.business.scene.common.model.datasource.PLVGeneralApiManager
import net.polyv.android.player.business.scene.common.model.vo.PLVMediaResource
import net.polyv.android.player.business.scene.common.model.vo.PLVVodMediaResource
import net.polyv.android.player.common.di.PLVMediaPlayerLocalProvider
import net.polyv.android.player.common.modules.media.viewmodel.PLVMPMediaViewModel
import net.polyv.android.player.common.ui.router.PLVMediaPlayerRouter.routerTo
import net.polyv.android.player.common.ui.router.RouterDestination
import net.polyv.android.player.common.ui.router.RouterPayload
import net.polyv.android.player.common.utils.data.PLVTimeUtils
import net.polyv.android.player.common.utils.ui.PLVRoundRectConstraintLayout
import net.polyv.android.player.core.api.listener.state.PLVMediaPlayerState
import net.polyv.android.player.scenes.single.R
import net.polyv.android.player.sdk.foundation.lang.Duration.Companion.seconds
import net.polyv.android.player.sdk.foundation.lang.watchStates
import net.polyv.android.player.sdk.foundation.log.logger
import kotlin.coroutines.resume

/**
 * @author Hoshiiro
 */
private const val TAG = "PLVMediaPlayerRecommendVideoLayout"

class PLVMediaPlayerRecommendVideoLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val recommendVideoRv by lazy { findViewById<RecyclerView>(R.id.plv_media_player_recommend_video_rv) }
    private val adapter = RecommendVideoAdapter()

    private var isPausePlayerWhenGoToOtherVideo = false

    init {
        LayoutInflater.from(context).inflate(R.layout.plv_media_player_recommend_video_layout, this, true)
        recommendVideoRv.layoutManager = LinearLayoutManager(context)
        recommendVideoRv.adapter = adapter
        adapter.onNavigateToRecommendVideo = { mediaResource ->
            onNavigateToRecommendVideo(mediaResource)
        }
        observeLifecycle()
    }

    private fun observeLifecycle() {
        (context as LifecycleOwner).lifecycle.addObserver(GenericLifecycleObserver { source, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (isPausePlayerWhenGoToOtherVideo) {
                    val dependScope = PLVMediaPlayerLocalProvider.localDependScope.on(this).current()
                    val viewModel = dependScope?.get<PLVMPMediaViewModel>()
                    viewModel?.start()
                    isPausePlayerWhenGoToOtherVideo = false
                }
            }
        })
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (isInEditMode) return

        watchStates {
            val dependScope = PLVMediaPlayerLocalProvider.localDependScope.on(this).current()
            val viewModel = dependScope?.get<PLVMPMediaViewModel>()
            val playerState = viewModel?.playerState?.value ?: PLVMediaPlayerState.STATE_IDLE
            if (isPausePlayerWhenGoToOtherVideo) {
                if (playerState in listOf(PLVMediaPlayerState.STATE_PREPARED, PLVMediaPlayerState.STATE_PLAYING)) {
                    viewModel?.pause()
                }
            }
        }.disposeOnDetached(this)
    }

    fun setVideos(videos: List<PLVMediaResource>) {
        PLVMediaPlayerGlobalCoroutineScope.launch(Dispatchers.Main) {
            runCatching {
                // 延迟初始化，避免影响视频加载速度
                withTimeout(2000) {
                    waitMediaPrepared()
                }
            }
            adapter.updateItems(videos)
        }
    }

    private suspend fun waitMediaPrepared(): Unit = suspendCancellableCoroutine {
        val dependScope = PLVMediaPlayerLocalProvider.localDependScope.on(this).current()
        val viewModel = dependScope?.get<PLVMPMediaViewModel>() ?: return@suspendCancellableCoroutine it.resume(Unit)
        val observers = mutableListOf<MutableObserver<*>>()
        watchStates {
            val playerState = viewModel.playerState.value ?: PLVMediaPlayerState.STATE_IDLE
            if (playerState >= PLVMediaPlayerState.STATE_PREPARED) {
                it.resume(Unit)
                observers.disposeAll()
            }
        }.addTo(observers)
        it.invokeOnCancellation {
            observers.disposeAll()
        }
    }

    private fun onNavigateToRecommendVideo(mediaResource: PLVMediaResource) {
        context.routerTo(
            RouterDestination.SceneSingle(
                RouterPayload.SceneSinglePayload(mediaResource)
            )
        )
        val dependScope = PLVMediaPlayerLocalProvider.localDependScope.on(this).current()
        val viewModel = dependScope?.get<PLVMPMediaViewModel>()
        val playerState = viewModel?.playerState?.value ?: PLVMediaPlayerState.STATE_IDLE
        val playerStatesToPause = listOf(
            PLVMediaPlayerState.STATE_IDLE,
            PLVMediaPlayerState.STATE_PREPARING,
            PLVMediaPlayerState.STATE_PREPARED,
            PLVMediaPlayerState.STATE_PLAYING
        )
        val isPausePlayer = playerStatesToPause.contains(playerState)
        if (isPausePlayer) {
            viewModel?.pause()
            isPausePlayerWhenGoToOtherVideo = true
        }
    }

}

private class RecommendVideoAdapter : RecyclerView.Adapter<RecommendVideoViewHolder>() {

    private val videos = mutableListOf<PLVMediaResource>()
    var onNavigateToRecommendVideo: (PLVMediaResource) -> Unit = {}

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecommendVideoViewHolder {
        return RecommendVideoViewHolder(parent)
    }

    override fun onBindViewHolder(
        holder: RecommendVideoViewHolder,
        position: Int
    ) {
        holder.bind(videos[position])
        holder.onNavigateToRecommendVideo = { mediaResource ->
            onNavigateToRecommendVideo(mediaResource)
        }
    }

    override fun getItemCount(): Int {
        return videos.size
    }

    fun updateItems(newVideos: List<PLVMediaResource>) {
        val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = videos.size

            override fun getNewListSize() = newVideos.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) = true

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return newVideos[newItemPosition] == videos[oldItemPosition]
            }
        })
        videos.clear()
        videos.addAll(newVideos)
        diff.dispatchUpdatesTo(this)
    }

}

private class RecommendVideoViewHolder(
    parent: ViewGroup
) : RecyclerView.ViewHolder(
    LayoutInflater.from(parent.context).inflate(R.layout.plv_media_player_recommend_video_item, parent, false)
) {

    private val recommendVideoCoverLayout by lazy { findViewById<PLVRoundRectConstraintLayout>(R.id.plv_media_player_recommend_video_cover_layout) }
    private val recommendVideoCoverIv by lazy { findViewById<ImageView>(R.id.plv_media_player_recommend_video_cover_iv) }
    private val recommendVideoTitleTv by lazy { findViewById<TextView>(R.id.plv_media_player_recommend_video_title_tv) }
    private val recommendVideoDurationTv by lazy { findViewById<TextView>(R.id.plv_media_player_recommend_video_duration_tv) }

    var onNavigateToRecommendVideo: (PLVMediaResource) -> Unit = {}

    fun bind(mediaResource: PLVMediaResource) {
        itemView.visibility = View.GONE
        when (mediaResource) {
            is PLVVodMediaResource -> {
                bindVodMediaResource(mediaResource)
            }
            else -> {}
        }
    }

    private fun bindVodMediaResource(mediaResource: PLVVodMediaResource) {
        PLVMediaPlayerGlobalCoroutineScope.launch(Dispatchers.Main) {
            val videoJsonResult = withContext(Dispatchers.IO) {
                PLVGeneralApiManager.getVideoJson(mediaResource.videoId)
            }
            if (videoJsonResult.isFailure) {
                itemView.visibility = View.GONE
                logger.error(TAG, "bindVodMediaResource: failed to load video json", videoJsonResult.exceptionOrNull())
                return@launch
            }
            itemView.visibility = View.VISIBLE
            val videoJson = videoJsonResult.getOrThrow()
            Glide.with(recommendVideoCoverIv).load(videoJson.first_image).into(recommendVideoCoverIv)
            recommendVideoTitleTv.text = videoJson.title
            val duration = PLVTimeUtils.formatTime(videoJson.durationSeconds.seconds())
            recommendVideoDurationTv.text = duration
        }
        itemView.setOnClickListener {
            onNavigateToRecommendVideo(mediaResource)
        }
    }

    private fun <T : View> findViewById(resId: Int) = itemView.findViewById<T>(resId)

}