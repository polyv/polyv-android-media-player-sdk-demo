package net.polyv.android.player.scenes.download

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import net.polyv.android.player.common.ui.router.PLVMediaPlayerRouter

/**
 * @author Hoshiiro
 */
class PLVMediaPlayerDownloadCenterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val contentView = PLVMediaPlayerDownloadCenterLayout(this)
        setContentView(contentView)

        val bundle: Bundle? = intent.extras
        if (bundle != null) {
            val defaultTabIndex = bundle.getInt(PLVMediaPlayerRouter.KEY_DOWNLOAD_CENTER_TAB_INDEX, 0)
            contentView.setTabIndex(defaultTabIndex)
        }
    }

}