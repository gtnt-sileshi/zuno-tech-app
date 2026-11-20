package com.zunotech.app.adapter.home;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager2.widget.ViewPager2;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.zunotech.app.R;
import com.zunotech.app.activity.BlogPostActivity;
import com.zunotech.app.activity.MainActivity;
import com.zunotech.app.callback.Callback;
import com.zunotech.app.callback.Method;
import com.zunotech.app.fragment.online.FragmentBlog;
import com.zunotech.app.fragment.online.FragmentCategories;
import com.zunotech.app.interfaces.InterAdListener;
import com.zunotech.app.item.ItemPost;
import com.zunotech.app.utils.helper.Helper;
import com.zunotech.app.utils.helper.SPHelper;
import com.zunotech.app.utils.recycler.RecyclerItemClickListener;

public class AdapterHome extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Context context;
    Helper helper;
    SPHelper spHelper;
    List<ItemPost> arrayList;
    private boolean showFooter = true;
    int clickPos = 0;

    private static final int VIEW_PROG = 0;
    private static final int VIEW_BANNER = 1;
    private static final int VIEW_CATEGORIES = 2;
    static final int VIEW_ADS = 3;
    /** Section type that this adapter does not render; such sections are filtered out. */
    static final int VIEW_NONE = 4;
    private static final int VIEW_BLOG = 5;
    private static final String TYPE_BLOG = "blog";

    /** Maps an API section type to a view type, or {@link #VIEW_NONE} when it is not rendered. */
    static int viewTypeOf(String type) {
        if (type == null) {
            return VIEW_NONE;
        }
        return switch (type) {
            case "slider" -> VIEW_BANNER;
            case "category" -> VIEW_CATEGORIES;
            case "blog" -> VIEW_BLOG;
            case "ads" -> VIEW_ADS;
            default -> VIEW_NONE;
        };
    }

    /**
     * Returns the sections that produce a row, in their original order. A section is kept only
     * when its type is known and it has at least one item, so no header, spacer or placeholder
     * row is created for empty or unknown sections. The "ads" row has no items and is kept.
     */
    public static List<ItemPost> buildRows(List<ItemPost> sections) {
        List<ItemPost> rows = new ArrayList<>();
        if (sections == null) {
            return rows;
        }
        for (ItemPost section : sections) {
            if (section != null && hasRow(section)) {
                rows.add(section);
            }
        }
        return rows;
    }

    private static boolean hasRow(ItemPost section) {
        return switch (viewTypeOf(section.getType())) {
            case VIEW_BANNER -> !section.getArrayListBanner().isEmpty();
            case VIEW_CATEGORIES -> !section.getArrayListCategories().isEmpty();
            case VIEW_BLOG -> !section.getArrayListLive().isEmpty();
            case VIEW_ADS -> true;
            default -> false;
        };
    }

    Boolean ads = true;

    public AdapterHome(Context context, List<ItemPost> arrayList) {
        this.context = context;
        this.arrayList = buildRows(arrayList);
        helper = new Helper(context, interAdListener);
        spHelper = new SPHelper(context);
    }

    static class BannerHolder extends RecyclerView.ViewHolder {

        ViewPager2 viewPager2;
        HomePagerAdapter homePagerAdapter;

        BannerHolder(View view) {
            super(view);
            viewPager2 = view.findViewById(R.id.viewPager_home);
        }
    }

    class CategoriesHolder extends RecyclerView.ViewHolder {

        RecyclerView rv;
        AdapterHomeCategories adapterHomeCategories;
        TextView title;
        LinearLayout viewAll;

        CategoriesHolder(View view) {
            super(view);
            rv = view.findViewById(R.id.rv_home_cat);
            title = view.findViewById(R.id.tv_home_title);
            viewAll = view.findViewById(R.id.ll_home_view_all);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
            rv.setLayoutManager(linearLayoutManager);
            rv.setItemAnimator(new DefaultItemAnimator());
            rv.addItemDecoration(new HomeSpacingDecoration(context));
        }
    }

    class BlogHolder extends RecyclerView.ViewHolder {
        RecyclerView rv;
        AdapterHomeBlog adapter;
        TextView title;
        LinearLayout viewAll;

        BlogHolder(View view) {
            super(view);
            rv = view.findViewById(R.id.rv_home_cat);
            title = view.findViewById(R.id.tv_home_title);
            viewAll = view.findViewById(R.id.ll_home_view_all);
            rv.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
            rv.setItemAnimator(new DefaultItemAnimator());
            rv.addItemDecoration(new HomeSpacingDecoration(context));
        }
    }

    static class LatestAds extends RecyclerView.ViewHolder {

        LinearLayout adView;

        LatestAds(View view) {
            super(view);
            adView = view.findViewById(R.id.ll_adView);
            // No space is reserved until an ad is attached to the container.
            final int gap = view.getResources().getDimensionPixelSize(R.dimen._16sdp);
            adView.setOnHierarchyChangeListener(new ViewGroup.OnHierarchyChangeListener() {
                @Override
                public void onChildViewAdded(View parent, View child) {
                    setTopMargin(gap);
                }

                @Override
                public void onChildViewRemoved(View parent, View child) {
                    setTopMargin(adView.getChildCount() == 0 ? 0 : gap);
                }

                private void setTopMargin(int px) {
                    ViewGroup.LayoutParams lp = adView.getLayoutParams();
                    if (lp instanceof ViewGroup.MarginLayoutParams mlp && mlp.topMargin != px) {
                        mlp.topMargin = px;
                        adView.setLayoutParams(mlp);
                    }
                }
            });
        }
    }

    private static class ProgressViewHolder extends RecyclerView.ViewHolder {

        private final ProgressBar progressBar;

        private ProgressViewHolder(View v) {
            super(v);
            progressBar = v.findViewById(R.id.progressBar);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_BANNER) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.layout_home_ui_banner, parent, false);
            return new BannerHolder(itemView);
        } else if (viewType == VIEW_CATEGORIES) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.layout_home_ui_categories, parent, false);
            return new CategoriesHolder(itemView);
        } else if (viewType == VIEW_BLOG) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.layout_home_ui_categories, parent, false);
            return new BlogHolder(itemView);
        } else if (viewType == VIEW_ADS) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.layout_baner_ad, parent, false);
            return new LatestAds(itemView);
        } else {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_progressbar, parent, false);
            return new ProgressViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof BannerHolder bannerHolder) {
            if (bannerHolder.homePagerAdapter == null) {
                bannerHolder.viewPager2.setFocusable(false);
                bannerHolder.homePagerAdapter = new HomePagerAdapter(context,
                        arrayList.get(holder.getAbsoluteAdapterPosition()).getArrayListBanner());
                bannerHolder.viewPager2.setAdapter(bannerHolder.homePagerAdapter);
                if (bannerHolder.homePagerAdapter.getItemCount() > 2) {
                    bannerHolder.viewPager2.setCurrentItem(1, false);
                }
            }
        }
        else if (holder instanceof CategoriesHolder categoriesHolder) {

            categoriesHolder.title.setText(arrayList.get(holder.getAbsoluteAdapterPosition()).getTitle());

            categoriesHolder.adapterHomeCategories = new AdapterHomeCategories(
                    arrayList.get(holder.getAbsoluteAdapterPosition()).getArrayListCategories());
            categoriesHolder.rv.setAdapter(categoriesHolder.adapterHomeCategories);

            categoriesHolder.rv.addOnItemTouchListener(new RecyclerItemClickListener(context, (view, position1) -> {
                clickPos = holder.getAbsoluteAdapterPosition();
                helper.showInterAd(position1, context.getString(R.string.categories));
            }));

            categoriesHolder.viewAll.setOnClickListener(v -> {
                FragmentCategories albums = new FragmentCategories();
                Bundle bundle = new Bundle();
                bundle.putBoolean("ishome", true);
                bundle.putString("id", arrayList.get(holder.getAbsoluteAdapterPosition()).getId());
                albums.setArguments(bundle);
                FragmentTransaction ft = ((AppCompatActivity) context).getSupportFragmentManager().beginTransaction();
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                ft.hide(((AppCompatActivity) context).getSupportFragmentManager()
                        .getFragments()
                        .get(((AppCompatActivity) context).getSupportFragmentManager().getBackStackEntryCount()));
                ft.add(R.id.fragment, albums, context.getString(R.string.categories));
                ft.addToBackStack(context.getString(R.string.categories));
                ft.commit();
                Objects.requireNonNull(((MainActivity) context).getSupportActionBar()).setTitle(
                        arrayList.get(holder.getAbsoluteAdapterPosition()).getTitle());
                ((MainActivity) context).bottomNavigationView(5);
            });
        }
        else if (holder instanceof BlogHolder blogHolder) {
            ItemPost section = arrayList.get(holder.getAbsoluteAdapterPosition());
            blogHolder.title.setText(section.getTitle());
            blogHolder.adapter = new AdapterHomeBlog(section.getArrayListLive(), position1 -> {
                clickPos = holder.getAbsoluteAdapterPosition();
                helper.showInterAd(position1, TYPE_BLOG);
            });
            blogHolder.rv.setAdapter(blogHolder.adapter);
            if ("latest".equals(section.getSections())) {
                blogHolder.viewAll.setVisibility(View.VISIBLE);
                blogHolder.viewAll.setOnClickListener(v -> {
                    FragmentBlog all = FragmentBlog.newInstance(
                        Method.METHOD_BLOG_POSTS, "", "", section.getTitle());
                    FragmentTransaction ft = ((AppCompatActivity) context).getSupportFragmentManager().beginTransaction();
                    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                    ft.hide(((AppCompatActivity) context).getSupportFragmentManager()
                            .getFragments()
                            .get(((AppCompatActivity) context).getSupportFragmentManager().getBackStackEntryCount()));
                    ft.add(R.id.fragment, all, section.getTitle());
                    ft.addToBackStack(section.getTitle());
                    ft.commit();
                    Objects.requireNonNull(((MainActivity) context).getSupportActionBar()).setTitle(section.getTitle());
                    ((MainActivity) context).bottomNavigationView(5);
                });
            } else {
                blogHolder.viewAll.setVisibility(View.GONE);
            }
        }
        else if (holder instanceof LatestAds latestAds) {
            if (Boolean.TRUE.equals(ads)){
                ads = false;
                helper.showBannerAd(latestAds.adView, Callback.PAGE_HOME);
            }
        }
    }

    @Override
    public long getItemId(int id) {
        return id;
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public void hideHeader() {
        if (showFooter) {
            showFooter = false;
            notifyItemRemoved(arrayList.size());
        }
    }

    public boolean isHeader(int position) {
        return arrayList.get(position) == null;
    }

    @Override
    public int getItemViewType(int position) {
        // Unknown types were filtered out by buildRows, so every row has a real view type.
        return viewTypeOf(arrayList.get(position).getType());
    }

    InterAdListener interAdListener = (position, type) -> {
        if (type.equals(TYPE_BLOG)) {
            Intent intent = new Intent(context, BlogPostActivity.class);
            intent.putExtra(BlogPostActivity.EXTRA_POST_ID, arrayList.get(clickPos).getArrayListLive().get(position).getId());
            context.startActivity(intent);
        } else if (type.equals(context.getString(R.string.categories))){
            FragmentBlog blogFrag = FragmentBlog.newInstance(
                Method.METHOD_BLOG_BY_CAT,
                arrayList.get(clickPos).getArrayListCategories().get(position).getId(), "",
                arrayList.get(clickPos).getArrayListCategories().get(position).getName());
            ((AppCompatActivity) context).getSupportFragmentManager()
                .beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .add(R.id.fragment, blogFrag, arrayList.get(clickPos).getArrayListCategories().get(position).getName())
                .addToBackStack(arrayList.get(clickPos).getArrayListCategories().get(position).getName())
                .commit();
        }
    };
}