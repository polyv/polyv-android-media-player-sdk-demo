package net.polyv.android.player.common.ui.component.more

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import net.polyv.android.player.common.R
import net.polyv.android.player.common.di.PLVMediaPlayerLocalProvider
import net.polyv.android.player.common.modules.knowledge.PLVMediaPlayerKnowledgeViewModel
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.PLVMPMediaControllerViewModel
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.viewstate.PLVMPMediaControllerFloatAction
import net.polyv.android.player.sdk.foundation.lang.watchStates

/**
 * @author Hoshiiro
 */
class PLVMediaPlayerMoreLayoutKnowledgeActionView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private val knowledgeActionIv by lazy { findViewById<ImageView>(R.id.plv_media_player_knowledge_action_iv) }
    private val knowledgeActionTv by lazy { findViewById<TextView>(R.id.plv_media_player_knowledge_action_tv) }

    init {
        LayoutInflater.from(context).inflate(R.layout.plv_media_player_ui_component_more_knowledge_action_layout, this)

        setOnClickListener {
            val dependScope = PLVMediaPlayerLocalProvider.localDependScope.on(this).current()
                ?: return@setOnClickListener
            val controllerViewModel = dependScope.get<PLVMPMediaControllerViewModel>()
            controllerViewModel.pushFloatActionLayout(PLVMPMediaControllerFloatAction.KNOWLEDGE)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (isInEditMode) return

        val dependScope = PLVMediaPlayerLocalProvider.localDependScope.on(this).current()!!
        val knowledgeViewModel = dependScope.get<PLVMediaPlayerKnowledgeViewModel>()
        watchStates {
            val hasKnowledgeContent = knowledgeViewModel.knowledgeData.value != null
            visibility = if (hasKnowledgeContent) View.VISIBLE else View.GONE
        }
    }

}
