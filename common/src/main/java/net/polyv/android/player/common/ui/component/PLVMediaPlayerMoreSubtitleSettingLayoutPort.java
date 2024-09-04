package net.polyv.android.player.common.ui.component;

import static net.polyv.android.player.sdk.foundation.graphics.ColorsKt.parseColor;
import static net.polyv.android.player.sdk.foundation.lang.PreconditionsKt.requireNotNull;
import static net.polyv.android.player.sdk.foundation.ui.ViewGroupsKt.children;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import net.polyv.android.player.business.scene.common.model.vo.PLVMediaSubtitle;
import net.polyv.android.player.common.R;
import net.polyv.android.player.common.di.PLVMediaPlayerLocalProvider;
import net.polyv.android.player.common.modules.media.viewmodel.PLVMPMediaViewModel;
import net.polyv.android.player.common.modules.media.viewmodel.viewstate.PLVMPMediaInfoViewState;
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.PLVMPMediaControllerViewModel;
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.viewstate.PLVMPMediaControllerFloatAction;
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.viewstate.PLVMPMediaControllerViewState;
import net.polyv.android.player.sdk.foundation.collections.PLVSequences;
import net.polyv.android.player.sdk.foundation.lang.PLVRememberState;
import net.polyv.android.player.sdk.foundation.lang.PLVRememberStateCompareResult;

import java.util.ArrayList;
import java.util.List;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

/**
 * @author Hoshiiro
 */
public class PLVMediaPlayerMoreSubtitleSettingLayoutPort extends FrameLayout implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private ImageView moreSubtitleSettingBackIv;
    private TextView moreSubtitleSettingShowSwitchLabel;
    private Switch moreSubtitleSettingShowSwitch;
    private LinearLayout moreSubtitleContainer;

    protected boolean isVisible = false;
    protected boolean isSubtitleEnable = false;
    protected List<List<PLVMediaSubtitle>> supportSubtitleList = null;
    protected List<PLVMediaSubtitle> selectedSubtitle = null;

    private List<PLVMediaSubtitle> lastSelectedSubtitle = null;

    public PLVMediaPlayerMoreSubtitleSettingLayoutPort(@NonNull Context context) {
        super(context);
    }

    public PLVMediaPlayerMoreSubtitleSettingLayoutPort(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PLVMediaPlayerMoreSubtitleSettingLayoutPort(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    {
        LayoutInflater.from(getContext()).inflate(R.layout.plv_media_player_ui_component_more_subtitle_setting_layout_port, this);
        moreSubtitleSettingBackIv = findViewById(R.id.plv_media_player_more_subtitle_setting_back_iv);
        moreSubtitleSettingShowSwitchLabel = findViewById(R.id.plv_media_player_more_subtitle_setting_show_switch_label);
        moreSubtitleSettingShowSwitch = findViewById(R.id.plv_media_player_more_subtitle_setting_show_switch);
        moreSubtitleContainer = findViewById(R.id.plv_media_player_more_subtitle_container);

        setOnClickListener(this);
        moreSubtitleSettingBackIv.setOnClickListener(this);
        moreSubtitleSettingShowSwitch.setOnCheckedChangeListener(this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (isInEditMode()) return;

        requireNotNull(PLVMediaPlayerLocalProvider.localDependScope.on(this).current())
                .get(PLVMPMediaViewModel.class)
                .getMediaInfoViewState()
                .observeUntilViewDetached(this, new Function1<PLVMPMediaInfoViewState, Unit>() {
                    @Override
                    public Unit invoke(PLVMPMediaInfoViewState viewState) {
                        isSubtitleEnable = viewState.getCurrentSubtitle() != null && !viewState.getCurrentSubtitle().isEmpty();
                        supportSubtitleList = viewState.getSupportSubtitles();
                        selectedSubtitle = viewState.getCurrentSubtitle();
                        if (selectedSubtitle != null && !selectedSubtitle.isEmpty()) {
                            lastSelectedSubtitle = selectedSubtitle;
                        }
                        onViewStateChanged();
                        return null;
                    }
                });

        requireNotNull(PLVMediaPlayerLocalProvider.localDependScope.on(this).current())
                .get(PLVMPMediaControllerViewModel.class)
                .getMediaControllerViewState()
                .observeUntilViewDetached(this, new Function1<PLVMPMediaControllerViewState, Unit>() {
                    @Override
                    public Unit invoke(PLVMPMediaControllerViewState viewState) {
                        isVisible = viewState.getLastFloatActionLayout() == PLVMPMediaControllerFloatAction.SUBTITLE
                                && !viewState.isMediaStopOverlayVisible();
                        onViewStateChanged();
                        return null;
                    }
                });
    }

    protected void onViewStateChanged() {
        PLVRememberState.rememberStateOf(this, "onChangeVisibility")
                .compareLastAndSet(isVisible)
                .ifNotEquals(new Function1<PLVRememberStateCompareResult, Unit>() {
                    @Override
                    public Unit invoke(PLVRememberStateCompareResult result) {
                        onChangeVisibility();
                        return null;
                    }
                });

        PLVRememberState.rememberStateOf(this, "onSupportSubtitleListChanged")
                .compareLastAndSet(supportSubtitleList)
                .ifNotEquals(new Function1<PLVRememberStateCompareResult, Unit>() {
                    @Override
                    public Unit invoke(PLVRememberStateCompareResult result) {
                        onSupportSubtitleListChanged();
                        return null;
                    }
                });

        PLVRememberState.rememberStateOf(this, "onSelectedSubtitleChanged")
                .compareLastAndSet(selectedSubtitle)
                .ifNotEquals(new Function1<PLVRememberStateCompareResult, Unit>() {
                    @Override
                    public Unit invoke(PLVRememberStateCompareResult result) {
                        onSelectedSubtitleChanged();
                        return null;
                    }
                });

        PLVRememberState.rememberStateOf(this, "onSubtitleEnableChanged")
                .compareLastAndSet(isSubtitleEnable)
                .ifNotEquals(new Function1<PLVRememberStateCompareResult, Unit>() {
                    @Override
                    public Unit invoke(PLVRememberStateCompareResult result) {
                        onSubtitleEnableChanged();
                        return null;
                    }
                });
    }

    protected void onSubtitleEnableChanged() {
        moreSubtitleContainer.setVisibility(isSubtitleEnable ? View.VISIBLE : View.GONE);
        moreSubtitleSettingShowSwitch.setChecked(isSubtitleEnable);
    }

    protected void onSupportSubtitleListChanged() {
        moreSubtitleContainer.removeAllViews();
        if (supportSubtitleList == null) {
            return;
        }
        PLVSequences.wrap(supportSubtitleList)
                .map(new Function1<List<PLVMediaSubtitle>, SubtitleItemLayout>() {
                    @Override
                    public SubtitleItemLayout invoke(List<PLVMediaSubtitle> subtitle) {
                        return new SubtitleItemLayout(getContext(), subtitle);
                    }
                })
                .forEach(new Function1<SubtitleItemLayout, Unit>() {
                    @Override
                    public Unit invoke(SubtitleItemLayout subtitleItemLayout) {
                        moreSubtitleContainer.addView(subtitleItemLayout);
                        return null;
                    }
                });
    }

    protected void onSelectedSubtitleChanged() {
        PLVSequences.wrap(children(moreSubtitleContainer))
                .filter(new Function1<View, Boolean>() {
                    @Override
                    public Boolean invoke(View view) {
                        return view instanceof SubtitleItemLayout;
                    }
                })
                .forEach(new Function1<View, Unit>() {
                    @Override
                    public Unit invoke(View view) {
                        ((SubtitleItemLayout) view).updateSelectState();
                        return null;
                    }
                });
    }

    protected void onChangeVisibility() {
        setVisibility(isVisible ? View.VISIBLE : View.GONE);
        if (getParent() != null) {
            getParent().requestDisallowInterceptTouchEvent(!isVisible);
            getParent().requestDisallowInterceptTouchEvent(isVisible);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView.getId() == moreSubtitleSettingShowSwitch.getId()) {
            if (!isChecked) {
                requireNotNull(PLVMediaPlayerLocalProvider.localDependScope.on(this).current())
                        .get(PLVMPMediaViewModel.class)
                        .setShowSubtitles(new ArrayList<PLVMediaSubtitle>());
            } else if (lastSelectedSubtitle != null) {
                requireNotNull(PLVMediaPlayerLocalProvider.localDependScope.on(this).current())
                        .get(PLVMPMediaViewModel.class)
                        .setShowSubtitles(lastSelectedSubtitle);
            }
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == moreSubtitleSettingBackIv.getId()) {
            backToMoreLayout();
        } else if (id == getId()) {
            closeLayout();
        }
    }

    private void closeLayout() {
        requireNotNull(PLVMediaPlayerLocalProvider.localDependScope.on(this).current())
                .get(PLVMPMediaControllerViewModel.class)
                .popFloatActionLayout();
    }

    private void backToMoreLayout() {
        closeLayout();
        requireNotNull(PLVMediaPlayerLocalProvider.localDependScope.on(this).current())
                .get(PLVMPMediaControllerViewModel.class)
                .pushFloatActionLayout(PLVMPMediaControllerFloatAction.MORE);
    }

    private class SubtitleItemLayout extends FrameLayout implements View.OnClickListener {

        private final List<PLVMediaSubtitle> subtitle;
        private TextView moreSubtitleItemText;

        public SubtitleItemLayout(@NonNull Context context, List<PLVMediaSubtitle> subtitle) {
            super(context);
            this.subtitle = subtitle;
            initView();
        }

        private void initView() {
            LayoutInflater.from(getContext()).inflate(R.layout.plv_media_player_ui_component_more_subtitle_item_port, this);

            moreSubtitleItemText = findViewById(R.id.plv_media_player_more_subtitle_item_text);

            moreSubtitleItemText.setText(getShowSubtitleText());
            setOnClickListener(this);
        }

        public void updateSelectState() {
            if (this.subtitle.equals(selectedSubtitle)) {
                setSelected(true);
                moreSubtitleItemText.setTextColor(parseColor("#3F76FC"));
            } else {
                setSelected(false);
                moreSubtitleItemText.setTextColor(parseColor("#CC000000"));
            }
        }

        private String getShowSubtitleText() {
            if (subtitle.isEmpty()) {
                return "";
            } else if (subtitle.size() == 1) {
                return subtitle.get(0).getName();
            } else {
                return getContext().getString(R.string.plv_media_player_ui_component_subtitle_setting_double_subtitle_prefix) +
                        PLVSequences.wrap(subtitle).joinToString("/", new Function1<PLVMediaSubtitle, CharSequence>() {
                            @Override
                            public CharSequence invoke(PLVMediaSubtitle subtitle) {
                                return subtitle.getName();
                            }
                        });
            }
        }

        @Override
        public void onClick(View v) {
            requireNotNull(PLVMediaPlayerLocalProvider.localDependScope.on(this).current())
                    .get(PLVMPMediaViewModel.class)
                    .setShowSubtitles(subtitle);
        }
    }

}
