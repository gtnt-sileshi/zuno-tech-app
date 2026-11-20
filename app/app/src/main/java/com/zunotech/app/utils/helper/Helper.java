package com.zunotech.app.utils.helper;

import com.zunotech.app.utils.advertising.AdFormat;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresPermission;
import android.widget.CheckBox;

import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Arrays;

import com.zunotech.app.BuildConfig;
import com.zunotech.app.R;
import com.zunotech.app.callback.Callback;
import com.zunotech.app.callback.Method;
import com.zunotech.app.dialog.DialogUtil;
import com.zunotech.app.interfaces.InterAdListener;
import com.zunotech.app.interfaces.RewardAdListener;
import com.zunotech.app.utils.ApplicationUtil;
import com.zunotech.app.utils.advertising.AdManagerInterAdmob;
import com.zunotech.app.utils.advertising.AdmobBannerCache;
import com.zunotech.app.utils.advertising.GDPRChecker;
import com.zunotech.app.utils.advertising.RewardAdAdmob;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class Helper {

    private static final java.util.concurrent.atomic.AtomicBoolean ADMOB_INIT_STARTED =
            new java.util.concurrent.atomic.AtomicBoolean(false);
    private final Context ctx;
    private InterAdListener interAdListener;
    boolean isRewarded = false;

    public Helper(Context ctx) {
        this.ctx = ctx;
    }

    public Helper(Context ctx, InterAdListener interAdListener) {
        this.ctx = ctx;
        this.interAdListener = interAdListener;
    }

    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    public boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            Network network = cm.getActiveNetwork();
            if (network != null) {
                NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);
                return capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
            }
        }
        return false;
    }

    public RequestBody getAPIRequest(String helper_name, int page, String itemID, String catID,
                                     String searchText, String reportMessage, String deviceID,
                                     String name, String email, String gender,
                                     String password, String authID, String loginType, File file) {
        JsonObject jsObj = new JsonObject();
        jsObj.addProperty("helper_name", helper_name);
        jsObj.addProperty("application_id", ctx.getPackageName());

        // ── App details ────────────────────────────────────────────
        if (Method.METHOD_APP_DETAILS.equals(helper_name)) {
            // no extra params needed

        // ── Blog (core) ────────────────────────────────────────────
        } else if (Method.METHOD_BLOG_POSTS.equals(helper_name)) {
            jsObj.addProperty("page", String.valueOf(page));
        } else if (Method.METHOD_BLOG_POST.equals(helper_name)) {
            jsObj.addProperty("post_id", itemID);
            jsObj.addProperty("device_id", deviceID);
        } else if (Method.METHOD_BLOG_BY_CAT.equals(helper_name)) {
            jsObj.addProperty("cat_id", catID);
            jsObj.addProperty("page", String.valueOf(page));
        } else if (Method.METHOD_BLOG_FEATURED.equals(helper_name)) {
            jsObj.addProperty("limit", String.valueOf(page));
        } else if (Method.METHOD_BLOG_TRENDING.equals(helper_name)) {
            jsObj.addProperty("page", String.valueOf(page));
        } else if (Method.METHOD_BLOG_SEARCH.equals(helper_name)) {
            jsObj.addProperty("page", String.valueOf(page));
            jsObj.addProperty("search_text", searchText);
        } else if (Method.METHOD_BLOG_TAGS.equals(helper_name)) {
            // no extra params
        } else if (Method.METHOD_BLOG_BY_TAG.equals(helper_name)) {
            jsObj.addProperty("tag_id", catID);
            jsObj.addProperty("page", String.valueOf(page));
        } else if (Method.METHOD_BLOG_RECENT.equals(helper_name)) {
            jsObj.addProperty("post_ids", itemID);
        } else if (Method.METHOD_BLOG_COMMENTS.equals(helper_name)) {
            jsObj.addProperty("post_id", itemID);
            jsObj.addProperty("page", String.valueOf(page));
        } else if (Method.METHOD_BLOG_COMMENT_POST.equals(helper_name)) {
            jsObj.addProperty("post_id", itemID);
            jsObj.addProperty("parent_id", catID);
            jsObj.addProperty("author_name", name);
            jsObj.addProperty("author_email", email);
            jsObj.addProperty("content", reportMessage);
            jsObj.addProperty("device_id", deviceID);
        } else if (Method.METHOD_SAVE_POST.equals(helper_name)) {
            jsObj.addProperty("post_id", itemID);
            jsObj.addProperty("device_id", deviceID);
        } else if (Method.METHOD_SAVED_POSTS.equals(helper_name)) {
            jsObj.addProperty("page", String.valueOf(page));
            jsObj.addProperty("device_id", deviceID);
            jsObj.addProperty("search_text", searchText);

        // ── Home & navigation ──────────────────────────────────────
        } else if (Method.METHOD_HOME.equals(helper_name)) {
            // no extra params
        } else if (Method.METHOD_HOME_DETAILS.equals(helper_name)) {
            jsObj.addProperty("id", itemID);
        } else if (Method.METHOD_CAT.equals(helper_name)) {
            jsObj.addProperty("page", String.valueOf(page));
            jsObj.addProperty("search_text", searchText);
            if (loginType != null && !loginType.isEmpty()) {
                jsObj.addProperty("search_type", loginType);
            }

        // ── Reports & suggestions (device-based) ──────────────────
        } else if (Method.METHOD_REPORT.equals(helper_name)) {
            jsObj.addProperty("post_id", itemID);
            jsObj.addProperty("device_id", deviceID);
            jsObj.addProperty("report_title", searchText);
            jsObj.addProperty("report_msg", reportMessage);
        } else if (Method.METHOD_SUGGESTION.equals(helper_name)) {
            jsObj.addProperty("suggest_title", searchText);
            jsObj.addProperty("suggest_message", reportMessage);
            jsObj.addProperty("device_id", deviceID);

        }
        // All requests use multipart form with base64-encoded JSON in "data" field
        return new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("data", ApplicationUtil.toBase64(jsObj.toString()))
                .build();
    }

    public void initializeAds() {
        // Initialise once per process, off the main thread (as Google recommends).
        // The completion callback runs on the main thread.
        if (ADMOB_INIT_STARTED.compareAndSet(false, true)) {
            final Context appCtx = ctx.getApplicationContext();
            new Thread(() -> MobileAds.initialize(appCtx, initializationStatus -> {
                // Preload as soon as the SDK is ready so the first eligible click has an ad.
                if (Boolean.TRUE.equals(Callback.getIsInterAd())
                        && Callback.getAdConfig().shouldRequest(AdFormat.INTERSTITIAL)) {
                    new AdManagerInterAdmob(appCtx).createAd();
                }
            }), "admob-init").start();
        }
    }

    public Object showBannerAd(LinearLayout linearLayout, String page) {
        if (isBannerAd(page)){
            if (!(ctx instanceof Activity) || !Callback.getAdConfig().shouldRequest(AdFormat.BANNER)) {
                return null;
            }
            return AdmobBannerCache.attach((Activity) ctx, linearLayout);
        } else {
            return null;
        }
    }

    public void showInterAd(final int pos, final String type) {
        if (isInterAd()){
                    final AdManagerInterAdmob adManagerInterAdmob = new AdManagerInterAdmob(ctx);
                    if (adManagerInterAdmob.getAd() != null) {
                        adManagerInterAdmob.getAd().setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdDismissedFullScreenContent() {
                                AdManagerInterAdmob.setAd(null);
                                adManagerInterAdmob.createAd();
                                interAdListener.onClick(pos, type);
                                super.onAdDismissedFullScreenContent();
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(@NonNull @NotNull com.google.android.gms.ads.AdError adError) {
                                AdManagerInterAdmob.setAd(null);
                                adManagerInterAdmob.createAd();
                                interAdListener.onClick(pos, type);
                                super.onAdFailedToShowFullScreenContent(adError);
                            }
                        });
                        adManagerInterAdmob.getAd().show((Activity) ctx);
                    } else {
                        // No ad ready: keep the slot so the next click is eligible again.
                        Callback.setAdCount(Math.max(0, Callback.getInterstitialAdShow() - 1));
                        Callback.setLastInterAdTime(0);
                        adManagerInterAdmob.createAd();
                        interAdListener.onClick(pos, type);
                    }
        } else {
            interAdListener.onClick(pos, type);
        }
    }

    public void showRewardAds(int pos, RewardAdListener rewardAdListener) {
        if (Boolean.TRUE.equals(Callback.getIsRewardAd())
                && Boolean.TRUE.equals(Callback.getIsAdsStatus())) {
            if (Boolean.TRUE.equals(new SPHelper(ctx).getIsRewardAdWarned())) {
                loadRewardAds(rewardAdListener, pos);
            } else {
                openRewardVideoAdAlert(rewardAdListener, pos);
            }
        } else {
            rewardAdListener.onClick(false, pos);
        }
    }


    public void loadRewardAds(RewardAdListener rewardAdListener, int pos) {
        if (new GDPRChecker(ctx).canLoadAd()
                && Callback.getAdConfig().shouldRequest(AdFormat.REWARD)) {
                    final RewardAdAdmob rewardAdAdmob = new RewardAdAdmob(ctx);
                    if (rewardAdAdmob.getAd() != null) {
                        rewardAdAdmob.getAd().setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdDismissedFullScreenContent() {
                                RewardAdAdmob.setAd(null);
                                rewardAdAdmob.createAd();
                                if (isRewarded) {
                                    rewardAdListener.onClick(true, pos);
                                }
                                super.onAdDismissedFullScreenContent();
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(@NonNull @NotNull com.google.android.gms.ads.AdError adError) {
                                RewardAdAdmob.setAd(null);
                                rewardAdAdmob.createAd();
                                rewardAdListener.onClick(false, pos);
                                super.onAdFailedToShowFullScreenContent(adError);
                            }
                        });
                        rewardAdAdmob.getAd().show((Activity) ctx, rewardItem -> isRewarded = true);
                    } else {
                        RewardAdAdmob.setAd(null);
                        rewardAdAdmob.createAd();
                        rewardAdListener.onClick(false, pos);
                    }
        } else {
            rewardAdListener.onClick(false, pos);
        }
    }

    public void openRewardVideoAdAlert(RewardAdListener rewardAdListener, int pos) {
        @SuppressLint("InflateParams") View view = ((Activity) ctx).getLayoutInflater().inflate(R.layout.row_bottom_videoad, null);

        BottomSheetDialog dialog = new BottomSheetDialog(ctx);
        dialog.setContentView(view);
        dialog.show();

        CheckBox cb = dialog.findViewById(R.id.cb_videoad);

        dialog.findViewById(R.id.btn_bottom_logout).setOnClickListener(view1 -> {
            dialog.dismiss();
            new SPHelper(ctx).setIsRewardAdWarned(cb.isChecked());
            loadRewardAds(rewardAdListener, pos);
        });
        dialog.findViewById(R.id.btn_bottom_cancel).setOnClickListener(view1 -> dialog.dismiss());
        dialog.findViewById(R.id.ll_checkbox).setOnClickListener(view1 -> cb.setChecked(true));
    }

    private boolean isInterAd() {
        if (isNetworkAvailable() && Boolean.TRUE.equals(Callback.getIsInterAd())
                && Boolean.TRUE.equals(Callback.getIsAdsStatus())
                && Callback.getAdConfig().shouldRequest(AdFormat.INTERSTITIAL)
                && new GDPRChecker(ctx).canLoadAd()) {
            Callback.setAdCount(Callback.getAdCount() + 1);
            long now = SystemClock.elapsedRealtime();
            long last = Callback.getLastInterAdTime();
            boolean countReached = Callback.getAdCount() >= Math.max(1, Callback.getInterstitialAdShow());
            boolean intervalPassed = last == 0 || now - last >= Callback.INTER_AD_MIN_INTERVAL_MS;
            if (countReached && intervalPassed) {
                Callback.setAdCount(0);
                Callback.setLastInterAdTime(now);
                return true;
            }
            return false;
        } else {
            return false;
        }
    }

    private boolean isBannerAd(String page) {
        if (isNetworkAvailable() && Boolean.TRUE.equals(Callback.getIsAdsStatus())
                && new GDPRChecker(ctx).canLoadAd()) {
            switch (page) {
                case Callback.PAGE_HOME:
                    return Callback.getIsBannerAdHome();
                case Callback.PAGE_POST_DETAILS:
                    return Callback.getIsBannerAdPostDetails();
                case Callback.PAGE_CAT_DETAILS:
                    return Callback.getIsBannerAdCatDetails();
                case Callback.PAGE_SEARCH:
                    return Callback.getIsBannerAdSearch();
                default:
                    return true;
            }
        } else {
            return false;
        }
    }

    public boolean canLoadNativeAds(Context context, String page) {
        if (Boolean.TRUE.equals(Callback.getIsAdsStatus())
                && new GDPRChecker(context).canLoadAd()) {
            if (page.equals(Callback.PAGE_NATIVE_POST)){
                return Callback.getIsNativeAdPost();
            } else if (page.equals(Callback.PAGE_NATIVE_CAT)){
                return Callback.getIsNativeAdCat();
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
}