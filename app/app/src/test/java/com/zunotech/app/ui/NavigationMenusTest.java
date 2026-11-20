package com.zunotech.app.ui;

import static org.junit.Assert.assertEquals;

import android.content.Context;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.appcompat.view.menu.MenuBuilder;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import com.zunotech.app.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Bottom navigation and drawer menus contain only the retained entries (Requirements 3.2). */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34, application = android.app.Application.class)
public class NavigationMenusTest {

    private static List<Integer> flatIds(int menuRes) {
        Context ctx = RuntimeEnvironment.getApplication();
        Menu menu = new MenuBuilder(ctx);
        new MenuInflater(ctx).inflate(menuRes, menu);
        List<Integer> ids = new ArrayList<>();
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            ids.add(item.getItemId());
        }
        return ids;
    }

    @Test
    public void bottomNavHasOnlyHomeTopicsSaved() {
        assertEquals(Arrays.asList(R.id.tv_nav_home, R.id.tv_nav_topics, R.id.tv_nav_saved),
                flatIds(R.menu.bottom_nav_menu));
    }

    @Test
    public void drawerHasOnlyRetainedEntries() {
        assertEquals(Arrays.asList(EXPECTED_DRAWER), flatIds(R.menu.activity_main_drawer));
    }

    private static final Integer[] EXPECTED_DRAWER = {
            R.id.nav_home, R.id.nav_topics, R.id.nav_saved,
            R.id.nav_suggest,
            R.id.nav_settings};
}
