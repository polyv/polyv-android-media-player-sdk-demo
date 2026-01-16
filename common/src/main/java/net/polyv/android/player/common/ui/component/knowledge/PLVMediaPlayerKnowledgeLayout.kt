package net.polyv.android.player.common.ui.component.knowledge

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import net.polyv.android.common.libs.lang.Duration.Companion.seconds
import net.polyv.android.common.libs.lang.graphic.dp
import net.polyv.android.common.libs.lang.ui.updateLayoutParams
import net.polyv.android.player.common.R
import net.polyv.android.player.common.di.PLVMediaPlayerLocalProvider
import net.polyv.android.player.common.modules.knowledge.PLVMediaPlayerKnowledgeViewModel
import net.polyv.android.player.common.modules.knowledge.vo.PLVMediaPlayerKnowledgeVO
import net.polyv.android.player.common.modules.media.viewmodel.PLVMPMediaViewModel
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.PLVMPMediaControllerViewModel
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.viewstate.PLVMPMediaControllerFloatAction
import net.polyv.android.player.common.ui.component.knowledge.adapter.PLVMediaPlayerKnowledgePointAdapter
import net.polyv.android.player.common.ui.component.knowledge.adapter.PLVMediaPlayerKnowledgeWordKeyAdapter
import net.polyv.android.player.common.ui.component.knowledge.adapter.PLVMediaPlayerKnowledgeWordTypeAdapter
import net.polyv.android.player.sdk.foundation.lang.watchStates

/**
 * @author suhongtao
 */
class PLVMediaPlayerKnowledgeLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val layoutRoot by lazy { findViewById<RelativeLayout>(R.id.plv_media_player_knowledge_layout_root) }
    private val knowledgeWordTypeRl by lazy { findViewById<RelativeLayout>(R.id.plv_media_player_knowledge_word_type_rl) }
    private val knowledgeCloseIv by lazy { findViewById<ImageView>(R.id.plv_media_player_knowledge_close_iv) }
    private val knowledgeWordTypeRv by lazy { findViewById<RecyclerView>(R.id.plv_media_player_knowledge_word_type_rv) }
    private val knowledgeWordKeyRv by lazy { findViewById<RecyclerView>(R.id.plv_media_player_knowledge_word_key_rv) }
    private val knowledgeDetailRv by lazy { findViewById<RecyclerView>(R.id.plv_media_player_knowledge_detail_rv) }

    private val wordTypeAdapter: PLVMediaPlayerKnowledgeWordTypeAdapter = PLVMediaPlayerKnowledgeWordTypeAdapter()
    private val wordKeyAdapter: PLVMediaPlayerKnowledgeWordKeyAdapter = PLVMediaPlayerKnowledgeWordKeyAdapter()
    private val knowledgePointAdapter: PLVMediaPlayerKnowledgePointAdapter = PLVMediaPlayerKnowledgePointAdapter()

    init {
        LayoutInflater.from(context).inflate(R.layout.plv_media_player_knowledge_layout, this)
        initRecyclerView()
        setOnClickCloseListener()
    }

    private fun initRecyclerView() {
        knowledgeWordTypeRv.setAdapter(wordTypeAdapter)
        knowledgeWordTypeRv.setLayoutManager(LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false))
        wordTypeAdapter.setOnItemClickListener { wordType ->
            val wordKeys = wordType.wordKeys
            val selectedWordKey = wordKeyAdapter.selectedWordKey
            wordKeyAdapter.setWordKeyList(wordKeys?.filterNotNull() ?: emptyList())
            if (selectedWordKey != null && wordKeys?.contains(selectedWordKey) == true) {
                knowledgePointAdapter.setKnowledgePoints(
                    wordKeyAdapter.selectedWordKey?.knowledgePoints?.filterNotNull() ?: emptyList()
                )
            } else {
                knowledgePointAdapter.setKnowledgePoints(emptyList())
            }
        }

        knowledgeWordKeyRv.setAdapter(wordKeyAdapter)
        knowledgeWordKeyRv.setLayoutManager(LinearLayoutManager(context))
        wordKeyAdapter.setOnItemClickListener { wordKey ->
            knowledgePointAdapter.setKnowledgePoints(wordKey.knowledgePoints?.filterNotNull() ?: emptyList())
        }

        knowledgeDetailRv.setAdapter(knowledgePointAdapter)
        knowledgeDetailRv.setLayoutManager(LinearLayoutManager(context))
        knowledgePointAdapter.setOnItemClickListener { knowledgePoint ->
            val dependScope = PLVMediaPlayerLocalProvider.localDependScope.on(this).current()
            val mediaViewModel = dependScope?.get<PLVMPMediaViewModel>()
            mediaViewModel?.seekTo(knowledgePoint.time?.seconds()?.toMillis() ?: 0)
        }
    }

    private fun setOnClickCloseListener() {
        fun onClose() {
            val dependScope = PLVMediaPlayerLocalProvider.localDependScope.on(this).current()
            val controllerViewModel = dependScope?.get<PLVMPMediaControllerViewModel>()
            controllerViewModel?.popFloatActionLayout()
        }
        this.setOnClickListener {
            onClose()
        }
        knowledgeCloseIv.setOnClickListener {
            onClose()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (isInEditMode) return

        val dependScope = PLVMediaPlayerLocalProvider.localDependScope.on(this).current()!!
        val controllerViewModel = dependScope.get<PLVMPMediaControllerViewModel>()
        val knowledgeViewModel = dependScope.get<PLVMediaPlayerKnowledgeViewModel>()

        watchStates {
            val knowledgeData = knowledgeViewModel.knowledgeData.value
            setKnowledgeData(knowledgeData)
        }.disposeOnDetached(this)

        watchStates {
            val lastFloatActionLayout = controllerViewModel.mediaControllerViewState.value?.lastFloatActionLayout
            val toShow = lastFloatActionLayout === PLVMPMediaControllerFloatAction.KNOWLEDGE
            val isShowing = visibility == VISIBLE
            if (toShow != isShowing) {
                visibility = if (toShow) VISIBLE else GONE
            }
        }.disposeOnDetached(this)
    }

    private fun setKnowledgeData(vo: PLVMediaPlayerKnowledgeVO?) {
        wordTypeAdapter.setWordTypeList(emptyList())
        wordKeyAdapter.setWordKeyList(emptyList())
        knowledgePointAdapter.setKnowledgePoints(emptyList())

        if (vo == null) {
            return
        }

        val fullScreen = vo.fullScreenStyle == true
        setFullScreenStyle(fullScreen)
        knowledgePointAdapter.setShowKnowledgePointDescription(fullScreen)

        wordTypeAdapter.setWordTypeList(vo.wordTypes?.filterNotNull() ?: emptyList())
        if (vo.wordTypes?.getOrNull(0) != null) {
            wordTypeAdapter.selectedWordType = vo.wordTypes[0]
            wordKeyAdapter.setWordKeyList(vo.wordTypes[0]!!.wordKeys?.filterNotNull() ?: emptyList())
            if (vo.wordTypes[0]!!.wordKeys?.getOrNull(0) != null) {
                wordKeyAdapter.selectedWordKey = vo.wordTypes[0]!!.wordKeys?.get(0)
                knowledgePointAdapter.setKnowledgePoints(
                    vo.wordTypes[0]!!.wordKeys!![0]!!.knowledgePoints?.filterNotNull() ?: emptyList()
                )
            }
        }
        wordTypeAdapter.notifyItemChanged(0)
        wordKeyAdapter.notifyItemChanged(0)
    }

    private fun setFullScreenStyle(isFullScreen: Boolean) {
        layoutRoot.updateLayoutParams<ViewGroup.LayoutParams> {
            width = if (isFullScreen) LayoutParams.MATCH_PARENT else 380.dp().px()
        }
    }

}
