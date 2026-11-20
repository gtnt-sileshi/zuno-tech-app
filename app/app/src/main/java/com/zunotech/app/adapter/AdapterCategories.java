package com.zunotech.app.adapter;

import com.zunotech.app.utils.advertising.AdFormat;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import android.widget.ImageView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.zunotech.app.R;
import com.zunotech.app.callback.Callback;
import com.zunotech.app.item.ItemCat;
import com.zunotech.app.utils.ApplicationUtil;
import com.zunotech.app.utils.helper.Helper;

public class AdapterCategories extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Helper helper;
    Context context;
    List<ItemCat> arrayList;
    private boolean showFooter = true;
    private final RecyclerItemClickListener listener;
    private static final int VIEW_PROG = -1;
    private static final int VIEW_ADS = -2;
    Boolean isAdLoaded = false;
    List<NativeAd> mNativeAdsAdmob = new ArrayList<>();

    public AdapterCategories(Context context, List<ItemCat> arrayList, RecyclerItemClickListener listener) {
        this.context = context;
        this.arrayList = arrayList;
        this.listener = listener;
        helper = new Helper(context);
        // Image size is now fixed in layout_cat.xml — no columnWidth needed
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {

        private final ImageView logo;
        private final TextView title;
        private final View itemView2;

        MyViewHolder(View view) {
            super(view);

            title = view.findViewById(R.id.tv_title);
            logo = view.findViewById(R.id.iv_cat_logo);
            itemView2 = view.findViewById(R.id.rl_cat_item);
        }
    }

    private static class ADViewHolder extends RecyclerView.ViewHolder {

        RelativeLayout nativeAd;
        boolean isAdRequested = false;

        private ADViewHolder(View view) {
            super(view);
            nativeAd = view.findViewById(R.id.rl_native_ad);
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
        if (viewType == VIEW_PROG) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_progressbar, parent, false);
            return new ProgressViewHolder(v);
        } else if (viewType == VIEW_ADS) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.layout_native_ad, parent, false);
            return new ADViewHolder(itemView);
        } else {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.layout_cat, parent, false);
            return new MyViewHolder(itemView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof MyViewHolder myViewHolder) {

            myViewHolder.title.setText(arrayList.get(holder.getAbsoluteAdapterPosition()).getName());

            Picasso.get()
                    .load(arrayList.get(holder.getAbsoluteAdapterPosition()).getImage())
                    .centerCrop()
                    .resize(300,300)
                    .placeholder(R.drawable.material_design_default)
                    .error(R.drawable.material_design_default)
                    .into(myViewHolder.logo);

            myViewHolder.itemView2.setOnClickListener(v -> listener.onClickListener(
                    arrayList.get(holder.getAbsoluteAdapterPosition()),holder.getAbsoluteAdapterPosition()));

        } else if (holder instanceof ADViewHolder adViewHolder) {
            if (adViewHolder.nativeAd.getChildCount() == 0) {
                {
                        if (Boolean.TRUE.equals(isAdLoaded) && mNativeAdsAdmob.size() >= 5
                                && Callback.getAdConfig().shouldRequest(AdFormat.NATIVE)) {

                            int i = ApplicationUtil.getRandomValue(mNativeAdsAdmob.size() - 1);

                            @SuppressLint("InflateParams") NativeAdView adView = (NativeAdView) ((Activity) context)
                                    .getLayoutInflater().inflate(R.layout.layout_native_ad_admob, null);
                            populateUnifiedNativeAdView(mNativeAdsAdmob.get(i), adView);
                            adViewHolder.nativeAd.removeAllViews();
                            adViewHolder.nativeAd.addView(adView);

                            adViewHolder.nativeAd.setVisibility(View.VISIBLE);
                        }

                    }
            }
        } else {
            if (getItemCount() <= 10) {
                // Post to avoid "Cannot call this method while RecyclerView is
                // computing a layout" — notifyItemChanged must not be called
                // from inside onBindViewHolder directly.
                new android.os.Handler(android.os.Looper.getMainLooper())
                        .post(() -> notifyItemChanged(arrayList.size()));
            }
        }
    }

    @Override
    public long getItemId(int id) {
        return id;
    }

    public void hideHeader() {
        try {
            if (showFooter) {
                showFooter = false;
                notifyItemRemoved(arrayList.size());
            }
        } catch (Exception e) {
            Log.e("AdapterCategories", "hideHeader failed", e);
        }
    }

    public boolean isHeader(int position) {
        return position == arrayList.size();
    }

    @Override
    public int getItemCount() {
        return showFooter ? arrayList.size() + 1 : arrayList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (isHeader(position)) {
            return VIEW_PROG;
        } else if (arrayList.get(position) == null) {
            return VIEW_ADS;
        } else {
            return position;
        }
    }

    public ItemCat getItem(int pos) {
        return arrayList.get(pos);
    }

    public void addAds(List<NativeAd> arrayListNativeAds) {
        isAdLoaded = true;
        mNativeAdsAdmob.addAll(arrayListNativeAds);
        for (int i = 0; i < arrayList.size(); i++) {
            if(arrayList.get(i) == null) {
                notifyItemChanged(i);
            }
        }
    }

    private void populateUnifiedNativeAdView(NativeAd nativeAd, NativeAdView adView) {
        MediaView mediaView = adView.findViewById(R.id.ad_media);
        adView.setMediaView(mediaView);

        // Set other ad assets.
        adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
        adView.setBodyView(adView.findViewById(R.id.ad_body));
        adView.setCallToActionView(adView.findViewById(R.id.ad_call_to_action));
        adView.setIconView(adView.findViewById(R.id.ad_icon));
        adView.setPriceView(adView.findViewById(R.id.ad_price));
        adView.setStarRatingView(adView.findViewById(R.id.ad_stars));
        adView.setStoreView(adView.findViewById(R.id.ad_store));
        adView.setAdvertiserView(adView.findViewById(R.id.ad_advertiser));

        // The headline is guaranteed to be in every UnifiedNativeAd.
        ((TextView) Objects.requireNonNull(adView.getHeadlineView())).setText(nativeAd.getHeadline());

        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        if (nativeAd.getBody() == null) {
            Objects.requireNonNull(adView.getBodyView()).setVisibility(View.INVISIBLE);
        } else {
            Objects.requireNonNull(adView.getBodyView()).setVisibility(View.VISIBLE);
            ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
        }

        if (nativeAd.getCallToAction() == null) {
            Objects.requireNonNull(adView.getCallToActionView()).setVisibility(View.INVISIBLE);
        } else {
            Objects.requireNonNull(adView.getCallToActionView()).setVisibility(View.VISIBLE);
            ((Button) adView.getCallToActionView()).setText(nativeAd.getCallToAction());
        }

        if (nativeAd.getIcon() == null) {
            Objects.requireNonNull(adView.getIconView()).setVisibility(View.GONE);
        } else {
            ((ImageView) Objects.requireNonNull(adView.getIconView())).setImageDrawable(
                    nativeAd.getIcon().getDrawable());
            adView.getIconView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getPrice() == null) {
            Objects.requireNonNull(adView.getPriceView()).setVisibility(View.INVISIBLE);
        } else {
            Objects.requireNonNull(adView.getPriceView()).setVisibility(View.VISIBLE);
            ((TextView) adView.getPriceView()).setText(nativeAd.getPrice());
        }

        if (nativeAd.getStore() == null) {
            Objects.requireNonNull(adView.getStoreView()).setVisibility(View.INVISIBLE);
        } else {
            Objects.requireNonNull(adView.getStoreView()).setVisibility(View.VISIBLE);
            ((TextView) adView.getStoreView()).setText(nativeAd.getStore());
        }

        if (nativeAd.getStarRating() == null) {
            Objects.requireNonNull(adView.getStarRatingView()).setVisibility(View.INVISIBLE);
        } else {
            ((RatingBar) Objects.requireNonNull(adView.getStarRatingView()))
                    .setRating(nativeAd.getStarRating().floatValue());
            adView.getStarRatingView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getAdvertiser() == null) {
            Objects.requireNonNull(adView.getAdvertiserView()).setVisibility(View.INVISIBLE);
        } else {
            ((TextView) Objects.requireNonNull(adView.getAdvertiserView())).setText(nativeAd.getAdvertiser());
            adView.getAdvertiserView().setVisibility(View.VISIBLE);
        }

        // This method tells the Google Mobile Ads SDK that you have finished populating your
        // native ad view with this native ad. The SDK will populate the adView's MediaView
        // with the media content from this native ad.
        adView.setNativeAd(nativeAd);
    }

    public interface RecyclerItemClickListener{
        void onClickListener(ItemCat itemCat, int position);
    }
}