package net.polyv.android.player.scenes.download

import android.os.Bundle
import androidx.core.view.ViewCompat
import androidx.appcompat.app.AppCompatActivity
import android.view.ViewGroup
import net.polyv.android.player.common.ui.router.PLVMediaPlayerRouter

/**
 * @author Hoshiiro
 */
class PLVMediaPlayerDownloadCenterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val contentView = PLVMediaPlayerDownloadCenterLayout(this)
        setContentView(contentView)
        updateWindowInsets()

        val bundle: Bundle? = intent.extras
        if (bundle != null) {
            val defaultTabIndex = bundle.getInt(PLVMediaPlayerRouter.KEY_DOWNLOAD_CENTER_TAB_INDEX, 0)
            contentView.setTabIndex(defaultTabIndex)
        }
    }

    private fun updateWindowInsets() {
        updateWindowInsetsAPI35()
    }

    // Android 15: force edge to edge
    private fun updateWindowInsetsAPI35() {
        val rootContent = findViewById<ViewGroup>(android.R.id.content).getChildAt(0)
        ViewCompat.setOnApplyWindowInsetsListener(rootContent) { v, insets ->
            v.setPadding(
                insets.systemWindowInsetLeft,
                insets.systemWindowInsetTop,
                insets.systemWindowInsetRight,
                insets.systemWindowInsetBottom
            )
            insets
        }
    }

}