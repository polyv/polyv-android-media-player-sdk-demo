package net.polyv.android.player.common.utils.orientation;

import static net.polyv.android.player.sdk.foundation.graphics.DisplaysKt.isLandscape;
import static net.polyv.android.player.sdk.foundation.graphics.DisplaysKt.isPortrait;
import static net.polyv.android.player.sdk.foundation.graphics.DisplaysKt.setLandscape;
import static net.polyv.android.player.sdk.foundation.graphics.DisplaysKt.setPortrait;

import android.app.Activity;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.view.OrientationEventListener;

import net.polyv.android.player.sdk.PLVDeviceManager;

import java.lang.ref.WeakReference;


/**
 * @author Hoshiiro
 */
public class PLVActivityOrientationManager extends ViewModel {

    private final WeakReference<Activity> activityRef;

    @Nullable
    private OrientationEventListener orientationEventListener = null;
    @Nullable
    private Boolean lastRequestOrientationPortrait = null;
    private boolean followSystemAutoRotate = false;
    private boolean lockOrientation = false;

    public static PLVActivityOrientationManager on(@NonNull final AppCompatActivity activity) {
        return new ViewModelProvider(
                activity.getViewModelStore(),
                new ViewModelProvider.Factory() {
                    @NonNull
                    @Override
                    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                        return (T) new PLVActivityOrientationManager(activity);
                    }
                }
        ).get(PLVActivityOrientationManager.class);
    }

    private PLVActivityOrientationManager(Activity activity) {
        this.activityRef = new WeakReference<>(activity);
    }

    public PLVActivityOrientationManager setFollowSystemAutoRotate(boolean followSystemAutoRotate) {
        this.followSystemAutoRotate = followSystemAutoRotate;
        return this;
    }

    public PLVActivityOrientationManager setLockOrientation(boolean lockOrientation) {
        this.lockOrientation = lockOrientation;
        return this;
    }

    public PLVActivityOrientationManager requestOrientation(boolean isPortrait) {
        lastRequestOrientationPortrait = isPortrait;
        Activity activity = activityRef.get();
        if (activity != null) {
            if (isPortrait) {
                setPortrait(activity);
            } else {
                setLandscape(activity);
            }
        }
        return this;
    }

    public void start() {
        stop();
        final Activity activity = activityRef.get();
        if (activity == null) {
            return;
        }
        final Context context = activity.getApplicationContext();

        orientationEventListener = new OrientationEventListener(context) {

            @Override
            public void onOrientationChanged(int orientation) {
                boolean isOrientationPortrait = orientation >= 0 && orientation <= 45 || orientation >= 315 && orientation <= 360 || orientation >= 135 && orientation <= 225;
                if (lastRequestOrientationPortrait != null) {
                    if (lastRequestOrientationPortrait == isOrientationPortrait) {
                        lastRequestOrientationPortrait = null;
                    } else {
                        return;
                    }
                }
                if (isOrientationPortrait) {
                    onOrientationPortrait();
                } else {
                    onOrientationLandscape();
                }
            }

            private void onOrientationPortrait() {
                if (isPortrait() || lockOrientation || !followSystemAutoRotate || !isSystemAutoRotateEnable()) {
                    return;
                }
                Activity activity1 = activityRef.get();
                if (activity1 != null) {
                    setPortrait(activity1);
                }
            }

            private void onOrientationLandscape() {
                if (isLandscape() || lockOrientation || !followSystemAutoRotate || !isSystemAutoRotateEnable()) {
                    return;
                }
                Activity activity1 = activityRef.get();
                if (activity1 != null) {
                    setLandscape(activity1);
                }
            }

            private boolean isSystemAutoRotateEnable() {
                return PLVDeviceManager.isAutoRotateEnable(context);
            }

        };

        disableSystemAutoRotate();
        orientationEventListener.enable();
    }

    public void stop() {
        if (orientationEventListener != null) {
            orientationEventListener.disable();
        }
        orientationEventListener = null;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        stop();
    }

    private void disableSystemAutoRotate() {
        Activity activity = activityRef.get();
        if (activity != null) {
            if (isPortrait()) {
                setPortrait(activity);
            } else {
                setLandscape(activity);
            }
        }
    }

}
