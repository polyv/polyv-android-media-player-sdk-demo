package net.polyv.android.player.common.modules.knowledge.di

import net.polyv.android.player.common.modules.knowledge.PLVMediaPlayerKnowledgeViewModel
import net.polyv.android.player.sdk.foundation.di.dependModule

/**
 * @author Hoshiiro
 */
internal val knowledgeModule = dependModule {
    provide { PLVMediaPlayerKnowledgeViewModel() }
}