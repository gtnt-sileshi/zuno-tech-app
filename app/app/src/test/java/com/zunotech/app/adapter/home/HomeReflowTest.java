package com.zunotech.app.adapter.home;

import static org.junit.Assert.assertEquals;

import android.content.Context;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ApplicationProvider;

import com.zunotech.app.item.ItemCat;
import com.zunotech.app.item.ItemData;
import com.zunotech.app.item.ItemHomeSlider;
import com.zunotech.app.item.ItemPost;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Arrays;
import java.util.List;

/**
 * Home reflow: empty and unknown sections add no row, and adjacent rows are separated by the
 * decoration spacing (Requirements 3.8, 4.3).
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34, application = android.app.Application.class)
public class HomeReflowTest {

    private static final int SECTION_GAP = 16;
    private static final int HEADER_GAP = 8;

    private static ItemPost banner(boolean withItem) {
        ItemPost p = new ItemPost("1", "Banner", "slider", "");
        if (withItem) {
            p.getArrayListBanner().add(new ItemHomeSlider("1", "t", "i", "img"));
        }
        return p;
    }

    private static ItemPost category(boolean withItem) {
        ItemPost p = new ItemPost("2", "Cats", "category", "");
        if (withItem) {
            p.getArrayListCategories().add(new ItemCat("1", "n", "img"));
        }
        return p;
    }

    private static ItemPost blog(boolean withItem) {
        ItemPost p = new ItemPost("3", "Blog", "blog", "latest");
        if (withItem) {
            p.getArrayListLive().add(new ItemData("1", "t", "thumb"));
        }
        return p;
    }

    private static List<ItemPost> fakeSections() {
        return Arrays.asList(
                banner(true),
                category(false),                                   // empty
                new ItemPost("9", "Mystery", "unknown_type", ""),  // unknown
                category(true),
                null,
                blog(false),                                       // empty
                blog(true),
                new ItemPost("100", "ads", "ads", "ads"));
    }

    private static RecyclerView.Adapter<RecyclerView.ViewHolder> adapter(
            int count, int width, int height, java.util.function.IntUnaryOperator viewType) {
        return new RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int type) {
                TextView tv = new TextView(parent.getContext());
                tv.setLayoutParams(new RecyclerView.LayoutParams(width, height));
                return new RecyclerView.ViewHolder(tv) { };
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            }

            @Override
            public int getItemViewType(int position) {
                return viewType.applyAsInt(position);
            }

            @Override
            public int getItemCount() {
                return count;
            }
        };
    }

    private static void layOut(RecyclerView rv, int w, int h) {
        rv.measure(View.MeasureSpec.makeMeasureSpec(w, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(h, View.MeasureSpec.EXACTLY));
        rv.layout(0, 0, w, h);
    }

    @Test
    public void emptyAndUnknownSectionsAddNoRows() {
        List<ItemPost> rows = AdapterHome.buildRows(fakeSections());
        assertEquals(4, rows.size());
        assertEquals("slider", rows.get(0).getType());
        assertEquals("category", rows.get(1).getType());
        assertEquals("blog", rows.get(2).getType());
        assertEquals("ads", rows.get(3).getType());
    }

    @Test
    public void topOffsetIsZeroForFirstAndAdsRowsAndGapOtherwise() {
        assertEquals(0, HomeSpacingDecoration.topOffset(0, false, SECTION_GAP));
        assertEquals(SECTION_GAP, HomeSpacingDecoration.topOffset(1, false, SECTION_GAP));
        assertEquals(SECTION_GAP, HomeSpacingDecoration.topOffset(2, false, SECTION_GAP));
        assertEquals(0, HomeSpacingDecoration.topOffset(3, true, SECTION_GAP));
    }

    @Test
    public void laidOutRowsAreSeparatedByDecorationSpacing() {
        Context context = ApplicationProvider.getApplicationContext();
        final List<ItemPost> rows = AdapterHome.buildRows(fakeSections());

        RecyclerView rv = new RecyclerView(context);
        rv.setLayoutManager(new LinearLayoutManager(context));
        rv.setAdapter(adapter(rows.size(), ViewGroup.LayoutParams.MATCH_PARENT, 50,
                pos -> AdapterHome.viewTypeOf(rows.get(pos).getType())));
        HomeSpacingDecoration decoration = new HomeSpacingDecoration(SECTION_GAP, HEADER_GAP);
        rv.addItemDecoration(decoration);
        layOut(rv, 400, 2000);

        assertEquals(rows.size(), rv.getChildCount());
        int[] expectedTop = {0, SECTION_GAP, SECTION_GAP, 0}; // last row is the ads row
        for (int i = 0; i < rv.getChildCount(); i++) {
            Rect out = new Rect();
            decoration.getItemOffsets(out, rv.getChildAt(i), rv, new RecyclerView.State());
            assertEquals("row " + i, expectedTop[i], out.top);
        }
        View first = rv.getChildAt(0);
        View second = rv.getChildAt(1);
        View third = rv.getChildAt(2);
        assertEquals(SECTION_GAP, second.getTop() - first.getBottom());
        assertEquals(SECTION_GAP, third.getTop() - second.getBottom());
        assertEquals(0, rv.getChildAt(3).getTop() - third.getBottom());
    }

    @Test
    public void horizontalSectionListPutsHeaderGapAboveCards() {
        Context context = ApplicationProvider.getApplicationContext();
        RecyclerView rv = new RecyclerView(context);
        rv.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        rv.setAdapter(adapter(2, 50, 50, pos -> 0));
        HomeSpacingDecoration decoration = new HomeSpacingDecoration(SECTION_GAP, HEADER_GAP);
        rv.addItemDecoration(decoration);
        layOut(rv, 400, 200);

        assertEquals(2, rv.getChildCount());
        for (int i = 0; i < rv.getChildCount(); i++) {
            Rect out = new Rect();
            decoration.getItemOffsets(out, rv.getChildAt(i), rv, new RecyclerView.State());
            assertEquals(HEADER_GAP, out.top);
        }
    }
}
