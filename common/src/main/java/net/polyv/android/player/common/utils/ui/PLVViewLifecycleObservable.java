package net.polyv.android.player.common.utils.ui;

import net.polyv.android.player.sdk.foundation.lang.Consumer;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Hoshiiro
 */
public class PLVViewLifecycleObservable {

    private final Set<IViewLifecycleObserver> observers = new HashSet<>();

    public void addObserver(IViewLifecycleObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(IViewLifecycleObserver observer) {
        observers.remove(observer);
    }

    public void removeAllObserver() {
        observers.clear();
    }

    public void callObserver(Consumer<IViewLifecycleObserver> consumer) {
        for (IViewLifecycleObserver observer : new HashSet<>(observers)) {
            consumer.accept(observer);
        }
    }

    public interface IViewLifecycleObserver {

        void onDestroy(PLVViewLifecycleObservable observable);

    }

    public static abstract class AbsViewLifecycleObserver implements IViewLifecycleObserver {

        @Override
        public void onDestroy(PLVViewLifecycleObservable observable) {

        }

    }

}


