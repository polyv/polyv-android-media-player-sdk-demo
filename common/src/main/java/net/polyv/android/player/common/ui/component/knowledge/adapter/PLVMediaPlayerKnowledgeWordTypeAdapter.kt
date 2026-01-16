package net.polyv.android.player.common.ui.component.knowledge.adapter

import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import net.polyv.android.player.common.modules.knowledge.vo.PLVMediaPlayerKnowledgeVO.WordType
import net.polyv.android.player.common.ui.component.knowledge.adapter.PLVMediaPlayerKnowledgeWordTypeAdapter.WordTypeViewHolder
import net.polyv.android.player.common.ui.component.knowledge.widget.PLVMediaPlayerKnowledgeWordTypeView

/**
 * @author suhongtao
 */
class PLVMediaPlayerKnowledgeWordTypeAdapter : RecyclerView.Adapter<WordTypeViewHolder?>() {

    private val wordTypeList = mutableListOf<WordType>()
    var selectedWordType: WordType? = null

    private var onItemClickListener: OnItemClickListener? = null

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): WordTypeViewHolder {
        return WordTypeViewHolder(PLVMediaPlayerKnowledgeWordTypeView(viewGroup.context))
    }

    override fun onBindViewHolder(wordTypeViewHolder: WordTypeViewHolder, i: Int) {
        val wordType = wordTypeList[i]
        wordTypeViewHolder.bind(wordType)
        wordTypeViewHolder.wordTypeView.setSelected(wordType == selectedWordType)
        wordTypeViewHolder.wordTypeView.setOnClickListener {
            selectedWordType = wordType
            onItemClickListener?.onClick(wordType)
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int {
        return wordTypeList.size
    }

    fun setWordTypeList(wordTypes: List<WordType>) {
        this.wordTypeList.clear()
        wordTypeList.addAll(wordTypes)
        notifyDataSetChanged()
    }

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener?) {
        this.onItemClickListener = onItemClickListener
    }

    class WordTypeViewHolder(
        val wordTypeView: PLVMediaPlayerKnowledgeWordTypeView
    ) : RecyclerView.ViewHolder(wordTypeView) {
        fun bind(wordType: WordType) {
            wordTypeView.setWordTypeName(wordType.name)
        }
    }

    fun interface OnItemClickListener {
        fun onClick(wordType: WordType)
    }
}
