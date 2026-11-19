package com.zunotech.app.utils.advertising;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import androidx.annotation.NonNull;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.zunotech.app.callback.Callback;
public class AdManagerInterAdmob {
    // AdMob interstitials expire after about one hour.
    private static final long AD_EXPIRY_MS = 55 * 60 * 1000L;
    private static final int MAX_RETRIES = 4;
    private static final long BASE_RETRY_DELAY_MS = 5_000L;
    private static final Handler HANDLER = new Handler(Looper.getMainLooper());
    private static InterstitialAd interAd;
    private static long loadedAt = 0;
    private static boolean isLoading = false;
    private static int retryCount = 0;
    private final Context ctx;
    public AdManagerInterAdmob(Context ctx) {
        Context app = ctx.getApplicationContext();
        this.ctx = app != null ? app : ctx;
    }
    /** Starts a load unless one is running or a fresh ad is already cached. Call on the main thread. */
    public void createAd() {
        retryCount = 0;
        load();
    }
    private void load() {
        if (isLoading || hasValidAd()) {
            return;
        }
        String unitId = Callback.getAdmobInterstitialAdID();
        if (unitId == null || unitId.isEmpty() || !new GDPRChecker(ctx).canLoadAd()) {
            return;
        }
        isLoading = true;
        InterstitialAd.load(ctx, unitId, new AdRequest.Builder().build(), new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                isLoading = false;
                retryCount = 0;
                setAd(interstitialAd);
            }
            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                isLoading = false;
                interAd = null;
                scheduleRetry();
            }
        });
    }
    private void scheduleRetry() {
        if (retryCount >= MAX_RETRIES) {
            return;
        }
        long delay = BASE_RETRY_DELAY_MS << retryCount; // 5s, 10s, 20s, 40s
        retryCount++;
        HANDLER.postDelayed(this::load, delay);
    }
    private static boolean hasValidAd() {
        return interAd != null && SystemClock.elapsedRealtime() - loadedAt < AD_EXPIRY_MS;
    }
    /** Returns the cached ad, or null if none is loaded or it has expired. */
    public InterstitialAd getAd() {
        if (interAd != null && !hasValidAd()) {
            interAd = null;
        }
        return interAd;
    }
    public static void setAd(InterstitialAd interstitialAd) {
        interAd = interstitialAd;
        loadedAt = interstitialAd != null ? SystemClock.elapsedRealtime() : 0;
    }
}
