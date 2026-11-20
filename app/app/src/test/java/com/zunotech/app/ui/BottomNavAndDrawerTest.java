package com.zunotech.app.ui;

import com.zunotech.app.R;



import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.app.Application;
import android.content.Context;
import android.content.res.ColorStateList;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ApplicationProvider;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.navigation.NavigationBarView;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Task 10.10: bottom navigation and drawer checks.
 * Validates: Requirements 3.2, 4.3
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34, application = Application.class, qualifiers = "w320dp-h640dp-mdpi")
public class BottomNavAndDrawerTest {

    private static final int VIEW_TYPE_SEPARATOR = 2; // NavigationMenuPresenter.NavigationMenuAdapter

    private Context themed() {
        Context app = ApplicationProvider.getApplicationContext();
        return new ContextThemeWrapper(app, R.style.AppTheme);
    }

    private float dp(Context c, float v) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, c.getResources().getDisplayMetrics());
    }

    private BottomNavigationView inflateBottomNav(Context c) {
        View root = LayoutInflater.from(c).inflate(R.layout.content_main, new FrameLayout(c), false);
        BottomNavigationView bnv = root.findViewById(R.id.ll_bottom_nav);
        assertNotNull(bnv);
        FrameLayout host = new FrameLayout(c);
        ((ViewGroup) bnv.getParent()).removeView(bnv);
        host.addView(bnv, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        int w = Math.round(dp(c, 320));
        host.measure(View.MeasureSpec.makeMeasureSpec(w, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(Math.round(dp(c, 640)), View.MeasureSpec.AT_MOST));
        host.layout(0, 0, host.getMeasuredWidth(), host.getMeasuredHeight());
        return bnv;
    }

    private static void collectText(View v, List<TextView> out) {
        if (v instanceof TextView) out.add((TextView) v);
        if (v instanceof ViewGroup) {
            ViewGroup g = (ViewGroup) v;
            for (int i = 0; i < g.getChildCount(); i++) collectText(g.getChildAt(i), out);
        }
    }

    @Test
    public void bottomBarHasExactlyThreeRetainedItems() {
        BottomNavigationView bnv = inflateBottomNav(themed());
        Set<Integer> ids = new HashSet<>();
        for (int i = 0; i < bnv.getMenu().size(); i++) ids.add(bnv.getMenu().getItem(i).getItemId());
        assertEquals(new HashSet<>(Arrays.asList(R.id.tv_nav_home, R.id.tv_nav_topics, R.id.tv_nav_saved)), ids);
        assertEquals(3, bnv.getMenu().size());
    }

    @Test
    public void itemsAreAtLeast48dpAt320dpWidthWithVisibleLabels() {
        Context c = themed();
        BottomNavigationView bnv = inflateBottomNav(c);
        assertEquals(NavigationBarView.LABEL_VISIBILITY_LABELED, bnv.getLabelVisibilityMode());
        int min = Math.round(dp(c, 48));
        for (int id : new int[]{R.id.tv_nav_home, R.id.tv_nav_topics, R.id.tv_nav_saved}) {
            View item = bnv.findViewById(id);
            assertNotNull(item);
            assertTrue("width " + item.getWidth() + " < " + min, item.getWidth() >= min);
            assertTrue("height " + item.getHeight() + " < " + min, item.getHeight() >= min);
            CharSequence title = bnv.getMenu().findItem(id).getTitle();
            boolean labelVisible = false;
            List<TextView> tvs = new ArrayList<>();
            collectText(item, tvs);
            for (TextView tv : tvs) {
                if (title.toString().contentEquals(tv.getText()) && tv.getVisibility() == View.VISIBLE) {
                    labelVisible = true;
                }
            }
            assertTrue("label not visible for item " + id, labelVisible);
        }
    }

    @Test
    public void selectedItemHasNoActiveIndicatorBackground() {
        Context c = themed();
        BottomNavigationView bnv = inflateBottomNav(c);
        assertFalse("active indicator pill must be disabled", bnv.isItemActiveIndicatorEnabled());
        bnv.setSelectedItemId(R.id.tv_nav_topics);
        assertTrue(bnv.getMenu().findItem(R.id.tv_nav_topics).isChecked());
        View item = bnv.findViewById(R.id.tv_nav_topics);
        View indicator = item.findViewById(com.google.android.material.R.id.navigation_bar_item_active_indicator_view);
        if (indicator != null) {
            assertTrue("indicator must not be drawn", indicator.getVisibility() != View.VISIBLE || indicator.getBackground() == null);
        }
    }

    private String readRes(String rel) throws Exception {
        File f = new File("src/main/res/" + rel);
        assertTrue(f.getAbsolutePath(), f.exists());
        return new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8);
    }

    @Test
    public void drawerHeaderHasNoRawHexColors() throws Exception {
        String xml = readRes("layout/row_nav_header.xml").replaceAll("(?s)<!--.*?-->", "");
        assertFalse(Pattern.compile("#[0-9A-Fa-f]{3,8}\\b").matcher(xml).find());
    }

    @Test
    public void noDividerFollowsLastDrawerItem() throws Exception {
        // XML: the last element of the menu is a group (dividers only separate groups).
        String xml = readRes("menu/activity_main_drawer.xml").replaceAll("(?s)<!--.*?-->", "");
        assertTrue(xml.contains("group_stream") && xml.contains("group_more"));
        assertTrue(xml.indexOf("group_more") > xml.indexOf("group_stream"));
        assertTrue(xml.trim().endsWith("</group>\n</menu>") || xml.trim().matches("(?s).*</group>\\s*</menu>"));

        // Runtime: the NavigationView adapter ends with a real item, not a separator.
        Context c = themed();
        NavigationView nv = new NavigationView(new ContextThemeWrapper(c, R.style.ThemeOverlay_NavDrawer));
        nv.inflateMenu(R.menu.activity_main_drawer);
        nv.measure(View.MeasureSpec.makeMeasureSpec(Math.round(dp(c, 320)), View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(Math.round(dp(c, 640)), View.MeasureSpec.EXACTLY));
        nv.layout(0, 0, nv.getMeasuredWidth(), nv.getMeasuredHeight());
        RecyclerView rv = null;
        for (int i = 0; i < nv.getChildCount(); i++) {
            if (nv.getChildAt(i) instanceof RecyclerView) rv = (RecyclerView) nv.getChildAt(i);
        }
        assertNotNull(rv);
        RecyclerView.Adapter<?> a = rv.getAdapter();
        assertNotNull(a);
        int last = a.getItemCount() - 1;
        assertTrue(a.getItemCount() > 0);
        assertTrue("last row is a separator", a.getItemViewType(last) != VIEW_TYPE_SEPARATOR);
        // exactly one separator, between the two groups
        int separators = 0;
        for (int i = 0; i < a.getItemCount(); i++) {
            if (a.getItemViewType(i) == VIEW_TYPE_SEPARATOR) separators++;
        }
        assertEquals(1, separators);
    }

    @Test
    public void headerRendersOwnBrandColor() {
        Context c = themed();
        int primary = c.getColor(R.color.ns_classic_primary);
        int primarySub = c.getColor(R.color.ns_classic_primary_sub);

        TypedValue tv = new TypedValue();
        assertTrue(c.getTheme().resolveAttribute(R.attr.ns_primary, tv, true));
        assertEquals(primary, tv.data);
        assertTrue(c.getTheme().resolveAttribute(R.attr.ns_primary_sub, tv, true));
        assertEquals(primarySub, tv.data);

        View header = LayoutInflater.from(c).inflate(R.layout.row_nav_header, new FrameLayout(c), false);
        CardView card = findCard(header);
        assertNotNull(card);
        ColorStateList csl = card.getCardBackgroundColor();
        assertEquals(primarySub, csl.getDefaultColor());
    }

    private static CardView findCard(View v) {
        if (v instanceof CardView) return (CardView) v;
        if (v instanceof ViewGroup) {
            ViewGroup g = (ViewGroup) v;
            for (int i = 0; i < g.getChildCount(); i++) {
                CardView r = findCard(g.getChildAt(i));
                if (r != null) return r;
            }
        }
        return null;
    }
}
