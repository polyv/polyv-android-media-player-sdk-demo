package net.polyv.android.player.common.di;

import net.polyv.android.player.common.utils.ui.PLVViewLifecycleObservable;
import net.polyv.android.player.sdk.foundation.di.DependScope;
import net.polyv.android.player.sdk.foundation.lang.PLVLocalProvider;

/**
 * @author Hoshiiro
 */
public class PLVMediaPlayerLocalProvider {

    public static final PLVLocalProvider<PLVViewLifecycleObservable> localLifecycleObservable = new PLVLocalProvider<>();
    public static final PLVLocalProvider<DependScope> localDependScope = new PLVLocalProvider<>();

}
