package net.polyv.android.player.scenes.download

import android.app.Activity
import android.content.Context
import android.graphics.Color
import androidx.fragment.app.FragmentActivity
import androidx.viewpager.widget.ViewPager
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import net.polyv.android.player.common.modules.download.list.PLVMPDownloadListViewModel
import net.polyv.android.player.common.utils.ui.PLVRoundRectConstraintLayout
import net.polyv.android.player.scenes.download.tab.PLVMediaPlayerDownloadTabAdapter
import net.polyv.android.player.sdk.foundation.graphics.parseColor
import net.polyv.android.player.sdk.foundation.lang.DerivedState
import net.polyv.android.player.sdk.foundation.lang.MutableState

/**
 * @author Hoshiiro
 */
internal class PLVMediaPlayerDownloadCenterLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle), View.OnClickListener {

    private val downloadBackIv: ImageView by lazy { findViewById(R.id.plv_media_player_download_back_iv) }
    private val downloadTabLayout: LinearLayout by lazy { findViewById(R.id.plv_media_player_download_tab_layout) }
    private val downloadCompletedTab: LinearLayout by lazy { findViewById(R.id.plv_media_player_download_completed_tab) }
    private val downloadCompletedTabTv: TextView by lazy { findViewById(R.id.plv_media_player_download_completed_tab_tv) }
    private val downloadCompletedTabIndicator: PLVRoundRectConstraintLayout by lazy { findViewById(R.id.plv_media_player_download_completed_tab_indicator) }
    private val downloadingTab: LinearLayout by lazy { findViewById(R.id.plv_media_player_downloading_tab) }
    private val downloadingTabTv: TextView by lazy { findViewById(R.id.plv_media_player_downloading_tab_tv) }
    private val downloadingTabIndicator: PLVRoundRectConstraintLayout by lazy { findViewById(R.id.plv_media_player_downloading_tab_indicator) }
    private val downloadTabViewPager: ViewPager by lazy { findViewById(R.id.plv_media_player_download_tab_view_pager) }
    private val downloadListIsEmptyLayout: LinearLayout by lazy { findViewById(R.id.plv_media_player_download_list_is_empty_layout) }

    private val tabAdapter = PLVMediaPlayerDownloadTabAdapter((context as FragmentActivity).supportFragmentManager)

    private val downloadListViewModel = PLVMPDownloadListViewModel
    private val currentSelectTab = MutableState(0)
    private val downloadListEmptyHintVisibleState = DerivedState {
        if (currentSelectTab.value == 0) {
            downloadListViewModel.downloadedList.value?.list?.isEmpty() ?: true
        } else {
            downloadListViewModel.downloadingList.value?.list?.isEmpty() ?: true
        }
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.plv_media_player_download_center_layout, this)

        downloadBackIv.setOnClickListener(this)
        downloadCompletedTab.setOnClickListener(this)
        downloadingTab.setOnClickListener(this)

        downloadTabViewPager.adapter = tabAdapter
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        downloadListViewModel.downloadedList
            .observeUntilViewDetached(this) { viewState ->
                val count = viewState.list.count()
                val countText = if (count > 0) "($count)" else ""
                downloadCompletedTabTv.text = "${context.getString(net.polyv.android.player.common.R.string.plv_media_player_ui_component_download_text_completed)}$countText"
                tabAdapter.completedFragment.updateList(viewState.list)
            }

        downloadListViewModel.downloadingList
            .observeUntilViewDetached(this) { viewState ->
                val count = viewState.list.count()
                val countText = if (count > 0) "($count)" else ""
                downloadingTabTv.text = "${context.getString(net.polyv.android.player.common.R.string.plv_media_player_ui_component_download_text_downloading)}$countText"
                tabAdapter.downloadingFragment.updateList(viewState.list)
            }

        currentSelectTab
            .observeUntilViewDetached(this) { tabIndex ->
                downloadTabViewPager.setCurrentItem(tabIndex, true)

                val isSelectCompletedTab = tabIndex == 0
                downloadCompletedTabTv.setTextColor(if (isSelectCompletedTab) Color.WHITE else parseColor("#99FFFFFF"))
                downloadCompletedTabIndicator.visibility = if (isSelectCompletedTab) View.VISIBLE else View.GONE
                downloadingTabTv.setTextColor(if (isSelectCompletedTab) parseColor("#99FFFFFF") else Color.WHITE)
                downloadingTabIndicator.visibility = if (isSelectCompletedTab) View.GONE else View.VISIBLE
            }

        downloadListEmptyHintVisibleState
            .observeUntilViewDetached(this) { listEmpty ->
                downloadListIsEmptyLayout.visibility = if (listEmpty) View.VISIBLE else View.GONE
            }
    }

    fun setTabIndex(index: Int) {
        currentSelectTab.setValue(index.coerceIn(0, 1))
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            downloadBackIv.id -> (context as Activity).finish()
            downloadCompletedTab.id -> currentSelectTab.setValue(0)
            downloadingTab.id -> currentSelectTab.setValue(1)
            else -> {}
        }
    }

}