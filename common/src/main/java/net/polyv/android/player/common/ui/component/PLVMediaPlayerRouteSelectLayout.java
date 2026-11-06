package net.polyv.android.player.common.ui.component;

import static net.polyv.android.player.sdk.foundation.graphics.DisplaysKt.dp;
import static net.polyv.android.player.sdk.foundation.lang.PreconditionsKt.requireNotNull;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.polyv.android.player.business.scene.common.model.vo.PLVMediaRoute;
import net.polyv.android.player.common.R;
import net.polyv.android.player.common.di.PLVMediaPlayerLocalProvider;
import net.polyv.android.player.common.modules.media.viewmodel.PLVMPMediaViewModel;
import net.polyv.android.player.common.modules.media.viewmodel.viewstate.PLVMPMediaInfoViewState;
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.PLVMPMediaControllerViewModel;
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.viewstate.PLVMPMediaControllerFloatAction;
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.viewstate.PLVMPMediaControllerViewState;
import net.polyv.android.player.sdk.foundation.di.DependScope;

import java.util.ArrayList;
import java.util.List;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

/** 播放线路设置：软硬解、CDN 线路和 DNS。 */
public class PLVMediaPlayerRouteSelectLayout extends FrameLayout implements View.OnClickListener {

    private LinearLayout panel;
    private ImageView closeIv;
    private LinearLayout decodeContainer;
    private LinearLayout routeSection;
    private LinearLayout routeContainer;
    private LinearLayout dnsContainer;

    private List<PLVMediaRoute> supportRoutes = new ArrayList<>();
    private PLVMediaRoute currentRoute;
    private boolean useMediaCodec;
    private boolean httpDnsEnabled;

    public PLVMediaPlayerRouteSelectLayout(Context context) { super(context); init(); }
    public PLVMediaPlayerRouteSelectLayout(Context context, @Nullable AttributeSet attrs) { super(context, attrs); init(); }
    public PLVMediaPlayerRouteSelectLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); init(); }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.plv_media_player_ui_component_route_select_layout, this);
        panel = findViewById(R.id.plv_media_player_route_panel);
        closeIv = findViewById(R.id.plv_media_player_route_close_iv);
        decodeContainer = findViewById(R.id.plv_media_player_decode_container);
        routeSection = findViewById(R.id.plv_media_player_route_section);
        routeContainer = findViewById(R.id.plv_media_player_route_container);
        dnsContainer = findViewById(R.id.plv_media_player_dns_container);
        setOnClickListener(this);
        closeIv.setOnClickListener(this);
        setVisibility(GONE);
        updatePanelLayout();
    }

    @Override protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        // Feed 会在竖屏阶段预先创建横屏布局，挂载时需按当前方向重新计算面板位置。
        updatePanelLayout();
        if (isInEditMode()) return;
        DependScope scope = requireNotNull(PLVMediaPlayerLocalProvider.localDependScope.on(this).current());
        scope.get(PLVMPMediaViewModel.class).getMediaInfoViewState()
                .observeUntilViewDetached(this, new Function1<PLVMPMediaInfoViewState, Unit>() {
                    @Override public Unit invoke(PLVMPMediaInfoViewState state) {
                        supportRoutes = state.getSupportRoutes();
                        currentRoute = state.getRoute();
                        useMediaCodec = state.getUseMediaCodec();
                        httpDnsEnabled = state.getHttpDnsEnabled();
                        rebuildOptions();
                        return Unit.INSTANCE;
                    }
                });
        scope.get(PLVMPMediaControllerViewModel.class).getMediaControllerViewState()
                .observeUntilViewDetached(this, new Function1<PLVMPMediaControllerViewState, Unit>() {
                    @Override public Unit invoke(PLVMPMediaControllerViewState state) {
                        setVisibility(state.getLastFloatActionLayout() == PLVMPMediaControllerFloatAction.ROUTE
                                ? VISIBLE : GONE);
                        return Unit.INSTANCE;
                    }
                });
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updatePanelLayout();
    }

    private void updatePanelLayout() {
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) panel.getLayoutParams();
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            lp.width = Math.min(getResources().getDisplayMetrics().widthPixels,
                    getResources().getDisplayMetrics().heightPixels);
            lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
            lp.gravity = Gravity.END;
        } else {
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
            lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            lp.gravity = Gravity.BOTTOM;
        }
        panel.setLayoutParams(lp);
    }

    private void rebuildOptions() {
        decodeContainer.removeAllViews();
        addOptionRows(decodeContainer, listOf(
                option(getResources().getString(R.string.plv_media_player_ui_component_hardware_decode),
                        getResources().getString(R.string.plv_media_player_ui_component_hardware_decode_desc), useMediaCodec,
                        () -> changeDecodeMode(true)),
                option(getResources().getString(R.string.plv_media_player_ui_component_software_decode),
                        getResources().getString(R.string.plv_media_player_ui_component_software_decode_desc), !useMediaCodec,
                        () -> changeDecodeMode(false))));

        routeSection.setVisibility(supportRoutes.size() > 1 ? VISIBLE : GONE);
        routeContainer.removeAllViews();
        List<Option> routeOptions = new ArrayList<>();
        for (PLVMediaRoute route : supportRoutes) {
            routeOptions.add(option(
                    getResources().getString(R.string.plv_media_player_ui_component_route_name, route.getIndex() + 1),
                    getResources().getString(route.getIndex() == 0
                            ? R.string.plv_media_player_ui_component_route_recommended_desc
                            : R.string.plv_media_player_ui_component_route_backup_desc),
                    route.equals(currentRoute), () -> changeRoute(route)));
        }
        addOptionRows(routeContainer, routeOptions);

        dnsContainer.removeAllViews();
        addOptionRows(dnsContainer, listOf(
                option(getResources().getString(R.string.plv_media_player_ui_component_http_dns),
                        getResources().getString(R.string.plv_media_player_ui_component_http_dns_desc), httpDnsEnabled,
                        () -> changeHttpDns(true)),
                option(getResources().getString(R.string.plv_media_player_ui_component_local_dns),
                        getResources().getString(R.string.plv_media_player_ui_component_local_dns_desc), !httpDnsEnabled,
                        () -> changeHttpDns(false))));
    }

    private List<Option> listOf(Option first, Option second) {
        List<Option> result = new ArrayList<>();
        result.add(first);
        result.add(second);
        return result;
    }

    private Option option(String title, String description, boolean selected, Runnable action) {
        return new Option(title, description, selected, action);
    }

    private void addOptionRows(LinearLayout container, List<Option> options) {
        for (int index = 0; index < options.size(); index += 2) {
            LinearLayout row = new LinearLayout(getContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, dp(64).px());
            if (index > 0) rowLp.topMargin = dp(12).px();
            container.addView(row, rowLp);
            addOptionView(row, options.get(index), true);
            if (index + 1 < options.size()) {
                addOptionView(row, options.get(index + 1), false);
            } else {
                View space = new View(getContext());
                row.addView(space, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1F));
            }
        }
    }

    private void addOptionView(LinearLayout row, Option option, boolean first) {
        LinearLayout item = new LinearLayout(getContext());
        item.setOrientation(LinearLayout.VERTICAL);
        item.setGravity(Gravity.CENTER_VERTICAL);
        item.setPadding(dp(12).px(), 0, dp(12).px(), 0);
        item.setBackgroundResource(option.selected
                ? R.drawable.plv_media_player_route_item_selected_bg
                : R.drawable.plv_media_player_route_item_normal_bg);

        TextView title = new TextView(getContext());
        title.setText(option.title);
        title.setTextSize(14);
        title.setTextColor(option.selected ? Color.rgb(63, 118, 252) : Color.rgb(51, 51, 51));
        item.addView(title, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView description = new TextView(getContext());
        description.setText(option.description);
        description.setTextSize(12);
        description.setTextColor(Color.rgb(153, 153, 153));
        description.setSingleLine(true);
        LinearLayout.LayoutParams descLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        descLp.topMargin = dp(4).px();
        item.addView(description, descLp);
        item.setOnClickListener(v -> {
            if (!option.selected) option.action.run();
            closeLayout();
        });

        LinearLayout.LayoutParams itemLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1F);
        if (first) itemLp.rightMargin = dp(6).px(); else itemLp.leftMargin = dp(6).px();
        row.addView(item, itemLp);
    }

    private void changeDecodeMode(boolean enabled) {
        dependScope().get(PLVMPMediaViewModel.class).changeDecodeMode(enabled);
    }

    private void changeRoute(PLVMediaRoute route) {
        dependScope().get(PLVMPMediaViewModel.class).changeRoute(route);
    }

    private void changeHttpDns(boolean enabled) {
        dependScope().get(PLVMPMediaViewModel.class).changeHttpDns(enabled);
    }

    private DependScope dependScope() {
        return requireNotNull(PLVMediaPlayerLocalProvider.localDependScope.on(this).current());
    }

    private void closeLayout() {
        dependScope().get(PLVMPMediaControllerViewModel.class).popFloatActionLayout();
    }

    @Override public void onClick(View v) {
        if (v == this || v == closeIv) closeLayout();
    }

    private static class Option {
        final String title;
        final String description;
        final boolean selected;
        final Runnable action;
        Option(String title, String description, boolean selected, Runnable action) {
            this.title = title;
            this.description = description;
            this.selected = selected;
            this.action = action;
        }
    }
}
