package net.polyv.android.player.scenes.download.tab

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter

/**
 * @author Hoshiiro
 */
class PLVMediaPlayerDownloadTabAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {

    val completedFragment = PLVMediaPlayerDownloadTabFragment()
    val downloadingFragment = PLVMediaPlayerDownloadTabFragment()

    private val fragments = listOf(completedFragment, downloadingFragment)

    override fun getCount(): Int {
        return fragments.count()
    }

    override fun getItem(position: Int): Fragment {
        return fragments[position]
    }

}