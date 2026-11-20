package com.zunotech.app.fragment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.robolectric.Shadows.shadowOf;

import android.content.Context;
import android.net.ConnectivityManager;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.test.core.app.ApplicationProvider;

import com.zunotech.app.R;
import com.zunotech.app.item.ItemPost;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

import java.util.ArrayList;

/**
 * Home with every section empty or unknown shows fl_empty and hides rv_home (Requirements 3.8, 4.3).
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34, application = android.app.Application.class)
public class HomeEmptyStateTest {

    @Test
    public void everySectionEmptyShowsEmptyStateAndHidesList() {
        // No active network, so onCreateView does not start the API call.
        Context app = ApplicationProvider.getApplicationContext();
        ConnectivityManager cm = (ConnectivityManager) app.getSystemService(Context.CONNECTIVITY_SERVICE);
        shadowOf(cm).setActiveNetworkInfo(null);

        ActivityController<AppCompatActivity> controller = Robolectric.buildActivity(AppCompatActivity.class);
        controller.get().setTheme(R.style.AppTheme);
        AppCompatActivity activity = controller.setup().get();
        FragmentHome fragment = new FragmentHome();
        activity.getSupportFragmentManager().beginTransaction()
                .add(android.R.id.content, fragment, "home").commitNow();

        assertNotNull(fragment.rv);
        assertNotNull(fragment.frameLayout);

        // Start from a visible list, then deliver sections that all produce no row.
        fragment.rv.setVisibility(View.VISIBLE);
        fragment.frameLayout.setVisibility(View.GONE);
        fragment.arrayList.clear();

        ArrayList<ItemPost> sections = new ArrayList<>();
        sections.add(new ItemPost("1", "Banner", "slider", ""));
        sections.add(new ItemPost("2", "Cats", "category", ""));
        sections.add(new ItemPost("3", "Blog", "blog", "latest"));
        sections.add(new ItemPost("4", "Mystery", "unknown_type", ""));
        fragment.loadHomeEnd(sections);

        assertEquals(View.VISIBLE, fragment.frameLayout.getVisibility());
        assertEquals(View.GONE, fragment.rv.getVisibility());
    }
}
