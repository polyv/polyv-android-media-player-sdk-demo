package net.polyv.android.player.scenes.feed

import android.content.Context
import androidx.fragment.app.FragmentActivity
import androidx.appcompat.app.AppCompatActivity
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.Toast
import net.polyv.android.player.business.scene.common.model.vo.PLVMediaResource
import net.polyv.android.player.common.utils.feed.PLVFeedViewPager.OnLoadFeedResourceListener
import net.polyv.android.player.common.utils.feed.PLVFeedViewPager.OnOverScrollListener
import net.polyv.android.player.common.utils.feed.PLVFeedViewPagerAdapter
import net.polyv.android.player.common.utils.orientation.PLVActivityOrientationManager
import net.polyv.android.player.scenes.feed.item.PLVMediaPlayerFeedVideoItemFragment
import net.polyv.android.player.scenes.feed.pager.PLVMediaPlayerFeedViewPager
import net.polyv.android.player.scenes.feed.viewmodel.IPLVMediaPlayerFeedVideoDataViewModel
import net.polyv.android.player.sdk.foundation.graphics.dp
import net.polyv.android.player.sdk.foundation.graphics.isLandscape
import kotlin.math.min

/**
 * @author Hoshiiro
 */
class PLVMediaPlayerFeedVideoLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    // <editor-fold defaultstate="collapsed" desc="Layout-属性">
    private var feedViewModel: IPLVMediaPlayerFeedVideoDataViewModel? = null

    private val feedViewPager by lazy { findViewById<PLVMediaPlayerFeedViewPager>(R.id.plv_media_player_feed_video_view_pager) }

    private val feedViewPagerAdapter = PLVFeedViewPagerAdapter<PLVMediaPlayerFeedVideoItemFragment, PLVMediaResource>((context as FragmentActivity).supportFragmentManager)

    private var isRequestingMediaResource = false
    private var isRequestingRefreshMediaResource = false

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Layout-初始化">
    fun init(feedViewModel: IPLVMediaPlayerFeedVideoDataViewModel) {
        this.feedViewModel = feedViewModel

        initLayout()
        initViewModel()
        initViewPager()
    }

    private fun initLayout() {
        keepScreenOn = true
        LayoutInflater.from(context).inflate(R.layout.plv_media_player_feed_video_layout, this)
    }

    // 设置 feedViewModel 从HTTP接口获取到数据后的处理逻辑
    private fun initViewModel() {
        feedViewModel!!.getOnReceiveMediaResourceEvent().observe { result ->
            result
                .ifSuccess { data ->
                    if (isRequestingRefreshMediaResource) {
                        feedViewPagerAdapter.setFeedResources(data)
                    } else {
                        feedViewPagerAdapter.appendFeedResources(data)
                    }
                }
                .ifError { _, _ ->
                    Toast.makeText(context, R.string.plv_media_player_load_new_media_resource_fail, Toast.LENGTH_SHORT)
                        .show()
                }
            isRequestingMediaResource = false
            isRequestingRefreshMediaResource = false
        }
    }

    // 设置 ViewPager 回调
    private fun initViewPager() {
        feedViewPagerAdapter
            .setOnCreateFeedViewListener { PLVMediaPlayerFeedVideoItemFragment() }
            .setOnBindFeedViewListener { feedView, resource -> feedView.setMediaResource(resource) }

        feedViewPager
            .setOnOverScrollListener(object : OnOverScrollListener {
                override fun onOverScroll(topOverScroll: Float, bottomOverScroll: Float) {
                    updateViewPagerPosition(topOverScroll, bottomOverScroll)
                }

                override fun onFinishOverScroll(topOverScroll: Float, bottomOverScroll: Float) {
                    updateViewPagerPosition(0f, 0f)
                }

                fun updateViewPagerPosition(topOverScroll: Float, bottomOverScroll: Float) {
                    val lp = feedViewPager.layoutParams as MarginLayoutParams
                    lp.topMargin = min((-topOverScroll * 0.75f).toInt().toDouble(), 100.dp().px().toDouble()).toInt()
                    lp.bottomMargin = min(
                        (bottomOverScroll * 0.75f).toInt().toDouble(),
                        100.dp().px().toDouble()
                    ).toInt()
                    feedViewPager.layoutParams = lp
                }
            })
            .setOnLoadFeedResourceListener(OnLoadFeedResourceListener { fromIndex ->
                if (isRequestingMediaResource) {
                    return@OnLoadFeedResourceListener
                }
                feedViewModel!!.requireMoreMediaResource(fromIndex)
                isRequestingMediaResource = true
                isRequestingRefreshMediaResource = fromIndex == 0
            })

        feedViewPager.adapter = feedViewPagerAdapter
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Layout-从外部设置视频信息资源">
    fun setTargetMediaResource(mediaResources: List<PLVMediaResource>?) {
        feedViewPagerAdapter.setFeedResources(mediaResources)
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Layout-返回">
    fun onBackPressed(): Boolean {
        if (isLandscape()) {
            PLVActivityOrientationManager.on((context as AppCompatActivity))
                .requestOrientation(true)
                .setLockOrientation(false)
            return true
        }
        return false
    }
// </editor-fold>
}
