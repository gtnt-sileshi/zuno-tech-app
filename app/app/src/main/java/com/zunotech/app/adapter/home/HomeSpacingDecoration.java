package com.zunotech.app.adapter.home;

import android.content.Context;
import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zunotech.app.R;

/**
 * Vertical rhythm for the Home screen. On the vertical home list it puts 16 dp above every
 * section except the first. On a horizontal section list it puts 8 dp above the cards, which is
 * the gap between the section header and its content.
 */
public class HomeSpacingDecoration extends RecyclerView.ItemDecoration {

    private final int sectionGapPx;
    private final int headerGapPx;

    public HomeSpacingDecoration(Context context) {
        this(context.getResources().getDimensionPixelSize(R.dimen._16sdp),
                context.getResources().getDimensionPixelSize(R.dimen._8sdp));
    }

    public HomeSpacingDecoration(int sectionGapPx, int headerGapPx) {
        this.sectionGapPx = sectionGapPx;
        this.headerGapPx = headerGapPx;
    }

    /**
     * Top offset of a row in the vertical home list. The first row has none, and the ads row has
     * none so that no space is reserved when no ad loads (the ad container adds its own margin).
     */
    public static int topOffset(int position, boolean adsRow, int sectionGapPx) {
        return (position <= 0 || adsRow) ? 0 : sectionGapPx;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                               @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);
        if (position == RecyclerView.NO_POSITION) {
            return;
        }
        RecyclerView.LayoutManager lm = parent.getLayoutManager();
        if (lm instanceof LinearLayoutManager llm && llm.getOrientation() == RecyclerView.HORIZONTAL) {
            outRect.top = headerGapPx;
            return;
        }
        RecyclerView.Adapter<?> adapter = parent.getAdapter();
        boolean adsRow = adapter != null && adapter.getItemViewType(position) == AdapterHome.VIEW_ADS;
        outRect.top = topOffset(position, adsRow, sectionGapPx);
    }
}
