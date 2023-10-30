package net.polyv.android.player.common.ui.viewmodel.viewstate;

import android.support.annotation.NonNull;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * @author Hoshiiro
 */
public class PLVMediaPlayerControlViewState implements Cloneable {

    public boolean controllerVisible = true;
    public boolean controllerLocking = false;
    public boolean bitRateSelectLayoutVisible = false;
    public boolean speedSelectLayoutVisible = false;
    public boolean moreActionLayoutVisible = false;
    public boolean progressSeekBarDragging = false;
    public long progressSeekBarDragPosition = 0;
    public boolean errorOverlayLayoutVisible = false;
    public boolean completeOverlayLayoutVisible = false;
    public boolean networkPoorIndicateLayoutVisible = false;

    public boolean isFloatActionPanelVisible() {
        return bitRateSelectLayoutVisible || speedSelectLayoutVisible || moreActionLayoutVisible;
    }

    public boolean isOverlayLayoutVisible() {
        return errorOverlayLayoutVisible || completeOverlayLayoutVisible;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PLVMediaPlayerControlViewState viewState = (PLVMediaPlayerControlViewState) o;
        return controllerVisible == viewState.controllerVisible && controllerLocking == viewState.controllerLocking && bitRateSelectLayoutVisible == viewState.bitRateSelectLayoutVisible && speedSelectLayoutVisible == viewState.speedSelectLayoutVisible && moreActionLayoutVisible == viewState.moreActionLayoutVisible && progressSeekBarDragging == viewState.progressSeekBarDragging && progressSeekBarDragPosition == viewState.progressSeekBarDragPosition && errorOverlayLayoutVisible == viewState.errorOverlayLayoutVisible && completeOverlayLayoutVisible == viewState.completeOverlayLayoutVisible && networkPoorIndicateLayoutVisible == viewState.networkPoorIndicateLayoutVisible;
    }

    @Override
    public int hashCode() {
        return Objects.hash(controllerVisible, controllerLocking, bitRateSelectLayoutVisible, speedSelectLayoutVisible, moreActionLayoutVisible, progressSeekBarDragging, progressSeekBarDragPosition, errorOverlayLayoutVisible, completeOverlayLayoutVisible, networkPoorIndicateLayoutVisible);
    }

    @NonNull
    @NotNull
    @Override
    public PLVMediaPlayerControlViewState clone() {
        try {
            return (PLVMediaPlayerControlViewState) super.clone();
        } catch (CloneNotSupportedException e) {
            // not reachable
            return new PLVMediaPlayerControlViewState();
        }
    }

}
