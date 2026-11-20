package com.zunotech.app.ui;

import static org.junit.Assert.assertNotNull;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.zunotech.app.R;

import org.robolectric.RuntimeEnvironment;

/**
 * Builds laid-out Home, Topics, Saved and drawer view trees for the accessibility (10.12) and
 * state-screenshot tests. Views are inflated from the production layouts and put in the
 * loading, empty and error states the fragments produce, without starting any network work.
 */
final class ScreenStates {

    enum Screen {
        HOME(R.layout.fragment_home, "shimmer_blog", R.id.tv_nav_home),
        TOPICS(R.layout.fragment_categories, "shimmer_categories", R.id.tv_nav_topics),
        SAVED(R.layout.fragment_blog, "shimmer_blog", R.id.tv_nav_saved);

        final int layout;
        final String shimmerIdName;
        final int navId;

        Screen(int layout, String shimmerIdName, int navId) {
            this.layout = layout;
            this.shimmerIdName = shimmerIdName;
            this.navId = navId;
        }
    }

    enum State {LOADING, EMPTY, ERROR}

    static final int WIDTH_DP = 360;
    static final int HEIGHT_DP = 640;
    static final int DRAWER_WIDTH_DP = 320;

    private ScreenStates() {
    }

    static Context context(boolean night) {
        RuntimeEnvironment.setQualifiers("w" + WIDTH_DP + "dp-h" + HEIGHT_DP + "dp"
                + (night ? "-night" : "-notnight") + "-mdpi");
        // The app's night look comes from the AppTheme.Dark theme, not from values-night.
        return new ContextThemeWrapper(RuntimeEnvironment.getApplication(),
                night ? R.style.AppTheme_Dark : R.style.AppTheme);
    }

    private static void layOut(View root, Context ctx, int widthDp, int heightDp) {
        float density = ctx.getResources().getDisplayMetrics().density;
        int w = Math.round(widthDp * density);
        int h = Math.round(heightDp * density);
        root.measure(View.MeasureSpec.makeMeasureSpec(w, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(h, View.MeasureSpec.EXACTLY));
        root.layout(0, 0, w, h);
    }

    /** The main screen: toolbar, one fragment layout in the given state, and the bottom bar. */
    static View screen(Context ctx, Screen screen, State state) {
        LayoutInflater inflater = LayoutInflater.from(ctx);
        DrawerLayout drawer = (DrawerLayout) inflater.inflate(R.layout.activity_main, null, false);
        View nav = drawer.findViewById(R.id.nav_view);
        drawer.removeView(nav);

        FrameLayout container = drawer.findViewById(R.id.fragment);
        assertNotNull("fragment container", container);
        View content = inflater.inflate(screen.layout, container, false);
        container.addView(content);

        BottomNavigationView bar = drawer.findViewById(R.id.ll_bottom_nav);
        bar.setSelectedItemId(screen.navId);

        applyState(ctx, content, screen, state);
        layOut(drawer, ctx, WIDTH_DP, HEIGHT_DP);
        return drawer;
    }

    private static void applyState(Context ctx, View content, Screen screen, State state) {
        FrameLayout flEmpty = content.findViewById(R.id.fl_empty);
        assertNotNull("fl_empty", flEmpty);
        View list = content.findViewById(screen == Screen.HOME ? R.id.rv_home
                : screen == Screen.TOPICS ? R.id.rv : R.id.rv_blog);
        assertNotNull("list", list);
        int shimmerId = ctx.getResources().getIdentifier(screen.shimmerIdName, "id", ctx.getPackageName());
        View shimmer = content.findViewById(shimmerId);
        assertNotNull(screen.shimmerIdName, shimmer);

        list.setVisibility(View.GONE);
        flEmpty.removeAllViews();
        if (state == State.LOADING) {
            flEmpty.setVisibility(View.GONE);
            shimmer.setVisibility(View.VISIBLE);
            return;
        }
        shimmer.setVisibility(View.GONE);
        flEmpty.setVisibility(View.VISIBLE);
        View row = LayoutInflater.from(ctx).inflate(R.layout.row_empty, null, false);
        ((TextView) row.findViewById(R.id.tv_empty_msg)).setText(
                state == State.EMPTY ? R.string.err_no_data_found : R.string.err_internet_not_connected);
        flEmpty.addView(row);
    }

    /** The navigation drawer with header and the first item checked, laid out at drawer width. */
    static View drawer(Context ctx) {
        DrawerLayout drawer = (DrawerLayout) LayoutInflater.from(ctx).inflate(R.layout.activity_main, null, false);
        NavigationView nav = drawer.findViewById(R.id.nav_view);
        drawer.removeView(nav);
        nav.setCheckedItem(nav.getMenu().getItem(0).getItemId());
        FrameLayout host = new FrameLayout(ctx);
        host.addView(nav, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        layOut(host, ctx, DRAWER_WIDTH_DP, HEIGHT_DP);
        return host;
    }

    static Bitmap draw(View root) {
        Bitmap bmp = Bitmap.createBitmap(root.getWidth(), root.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);
        canvas.drawColor(Color.WHITE);
        root.draw(canvas);
        return bmp;
    }
}
