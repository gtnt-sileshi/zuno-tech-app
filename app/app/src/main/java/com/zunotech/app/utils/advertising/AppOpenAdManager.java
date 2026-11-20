package com.zunotech.app.utils.advertising;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.appopen.AppOpenAd;

import java.util.Date;

import com.zunotech.app.activity.LauncherActivity;
import com.zunotech.app.activity.MainActivity;
import com.zunotech.app.callback.Callback;
import com.zunotech.app.interfaces.OnShowAdCompleteListener;

public class AppOpenAdManager {

    private AppOpenAd appOpenAd = null;
    private boolean isLoadingAd = false;
    private long loadTime = 0;
    Context context;

    private Boolean isShowingAd = false;
    public Boolean getIsShowingAd() {
        return isShowingAd;
    }

    public AppOpenAdManager(Context context) {
        this.context = context;
    }

    public void loadAd(Context context) {
        if (Boolean.TRUE.equals(Callback.getIsAppOpenAdShown())) {
            return;
        }
        if (!Callback.getAdConfig().shouldRequest(AdFormat.APP_OPEN)) {
            return;
        }
        if (isLoadingAd || isAdAvailable()) {
            return;
        }

        isLoadingAd = true;

        AdRequest request = new AdRequest.Builder().build();
        AppOpenAd.load(context, Callback.getAdmobOpenAdID(), request, new AppOpenAd.AppOpenAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull AppOpenAd ad) {
                appOpenAd = ad;
                isLoadingAd = false;
                loadTime = (new Date()).getTime();
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                isLoadingAd = false;
            }
        });
    }

    private boolean wasLoadTimeLessThanNHoursAgo() {
        long dateDifference = (new Date()).getTime() - loadTime;
        long numMilliSecondsPerHour = 3600000;
        return (dateDifference < (numMilliSecondsPerHour * 4));
    }

    public boolean isAdAvailable() {
        return appOpenAd != null && wasLoadTimeLessThanNHoursAgo();
    }

    public void showAdIfAvailable(@NonNull final Activity activity) {
        showAdIfAvailable(activity, () -> {});
    }

    public void showAdIfAvailable(@NonNull final Activity activity,
                                  @NonNull OnShowAdCompleteListener onShowAdCompleteListener) {
        if (Boolean.TRUE.equals(isShowingAd)) {
            return;
        }

        if (!isAdAvailable()) {
            onShowAdCompleteListener.onShowAdComplete();
            checkAndReloadAd(activity);
            return;
        }

        handleAdMobAd(activity, onShowAdCompleteListener);
    }

    private void handleAdMobAd(Activity activity, OnShowAdCompleteListener onShowAdCompleteListener) {
        appOpenAd.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdDismissedFullScreenContent() {
                appOpenAd = null;
                isShowingAd = false;
                navigateIfLauncher(activity);
                onShowAdCompleteListener.onShowAdComplete();
                if (new GDPRChecker(context).canLoadAdOpenAds()) loadAd(activity);
            }

            @Override
            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                appOpenAd = null;
                isShowingAd = false;
                onShowAdCompleteListener.onShowAdComplete();
                if (new GDPRChecker(context).canLoadAdOpenAds()) loadAd(activity);
            }

            @Override
            public void onAdShowedFullScreenContent() {
                Callback.setIsAppOpenAdShown(true);
                isShowingAd = true;
            }
        });
        isShowingAd = true;
        appOpenAd.show(activity);
    }

    private void navigateIfLauncher(Activity activity) {
        if (activity instanceof LauncherActivity) {
            Intent intent = new Intent(activity, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            activity.startActivity(intent);
            activity.finish();
        }
    }

    private void checkAndReloadAd(Activity activity) {
        if (new GDPRChecker(context).canLoadAdOpenAds()) {
            loadAd(activity);
        }
    }
}
