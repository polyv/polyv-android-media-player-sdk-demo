package net.polyv.android.player.scenes.download.tab

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import net.polyv.android.player.common.modules.download.list.viewstate.PLVMPDownloadListItemViewState
import net.polyv.android.player.scenes.download.R
import net.polyv.android.player.scenes.download.list.PLVMediaPlayerDownloadAdapter
import net.polyv.android.player.sdk.foundation.lang.State

/**
 * @author Hoshiiro
 */
class PLVMediaPlayerDownloadTabFragment : Fragment() {

    private var fragmentRootView: View? = null
    private var downloadTabRv: RecyclerView? = null

    private val adapter = PLVMediaPlayerDownloadAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.plv_media_player_download_tab_fragment, container, false).apply {
            fragmentRootView = this@apply
            downloadTabRv = findViewById(R.id.plv_media_player_download_tab_rv)

            downloadTabRv?.adapter = adapter
            downloadTabRv?.layoutManager = LinearLayoutManager(
                inflater.context,
                LinearLayoutManager.VERTICAL,
                false
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        fragmentRootView = null
        downloadTabRv = null
    }

    fun updateList(list: List<State<PLVMPDownloadListItemViewState>>) {
        adapter.update(list)
    }

}