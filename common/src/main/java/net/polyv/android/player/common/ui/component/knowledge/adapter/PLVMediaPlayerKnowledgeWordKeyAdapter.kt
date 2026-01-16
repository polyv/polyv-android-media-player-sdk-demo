package net.polyv.android.player.common.ui.component.knowledge.adapter

import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import net.polyv.android.player.common.modules.knowledge.vo.PLVMediaPlayerKnowledgeVO.WordType.WordKey
import net.polyv.android.player.common.modules.knowledge.vo.PLVMediaPlayerKnowledgeVO.WordType.WordKey.KnowledgePoint
import net.polyv.android.player.common.ui.component.knowledge.adapter.PLVMediaPlayerKnowledgeWordKeyAdapter.WordKeyViewHolder
import net.polyv.android.player.common.ui.component.knowledge.widget.PLVMediaPlayerKnowledgeWordKeyView

/**
 * @author suhongtao
 */
class PLVMediaPlayerKnowledgeWordKeyAdapter : RecyclerView.Adapter<WordKeyViewHolder?>() {

    private val wordKeyList = mutableListOf<WordKey>()
    var selectedWordKey: WordKey? = null

    private var onItemClickListener: OnItemClickListener? = null

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): WordKeyViewHolder {
        return WordKeyViewHolder(PLVMediaPlayerKnowledgeWordKeyView(viewGroup.context))
    }

    override fun onBindViewHolder(wordKeyViewHolder: WordKeyViewHolder, i: Int) {
        val wordKey = wordKeyList[i]
        wordKeyViewHolder.bind(wordKey)
        wordKeyViewHolder.wordKeyView.setSelected(wordKey == selectedWordKey)
        wordKeyViewHolder.wordKeyView.setOnClickListener {
            selectedWordKey = wordKey
            onItemClickListener?.onClick(wordKey)
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int {
        return wordKeyList.size
    }

    fun setWordKeyList(list: List<WordKey>) {
        this.wordKeyList.clear()
        this.wordKeyList.addAll(list)
        notifyDataSetChanged()
    }

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener?) {
        this.onItemClickListener = onItemClickListener
    }

    class WordKeyViewHolder(
        val wordKeyView: PLVMediaPlayerKnowledgeWordKeyView
    ) : RecyclerView.ViewHolder(wordKeyView) {
        fun bind(wordKey: WordKey) {
            val points: List<KnowledgePoint?>? = wordKey.knowledgePoints
            val size = points?.size ?: 0
            wordKeyView.setWordKey(wordKey.name)
            wordKeyView.setKnowledgePointCount(size)
        }
    }

    fun interface OnItemClickListener {
        fun onClick(wordKey: WordKey)
    }
}
