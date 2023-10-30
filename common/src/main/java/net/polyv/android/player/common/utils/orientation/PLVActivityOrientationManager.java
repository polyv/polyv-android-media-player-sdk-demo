package net.polyv.android.player.common.utils.orientation;

import android.app.Activity;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.OrientationEventListener;

import com.plv.foundationsdk.component.viewmodel.PLVViewModels;
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;

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
        return PLVViewModels.on(activity.getViewModelStore())
                .setFactory(new ViewModelProvider.Factory() {
                    @NonNull
                    @Override
                    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                        return (T) new PLVActivityOrientationManager(activity);
                    }
                })
                .get(PLVActivityOrientationManager.class);
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
                ScreenUtils.setPortrait(activity);
            } else {
                ScreenUtils.setLandscape(activity);
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
                if (ScreenUtils.isPortrait() || lockOrientation || !followSystemAutoRotate || !isSystemAutoRotateEnable()) {
                    return;
                }
                Activity activity1 = activityRef.get();
                if (activity1 != null) {
                    ScreenUtils.setPortrait(activity1);
                }
            }

            private void onOrientationLandscape() {
                if (ScreenUtils.isLandscape() || lockOrientation || !followSystemAutoRotate || !isSystemAutoRotateEnable()) {
                    return;
                }
                Activity activity1 = activityRef.get();
                if (activity1 != null) {
                    ScreenUtils.setLandscape(activity1);
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
            if (ScreenUtils.isPortrait()) {
                ScreenUtils.setPortrait(activity);
            } else {
                ScreenUtils.setLandscape(activity);
            }
        }
    }

}
