package com.zunotech.app.utils.advertising;
import android.app.Activity;
import android.content.Context;
import android.content.MutableContextWrapper;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import java.util.ArrayList;
import java.util.List;
import com.zunotech.app.callback.Callback;
/**
 * Keeps adaptive AdMob banners alive between screens. A banner that already holds an ad is moved
 * into the next screen's container instead of requesting a new one, so opening a screen does not
 * cost an ad request. Each banner is built on a MutableContextWrapper so a cached view never holds
 * a destroyed Activity.
 */
public final class AdmobBannerCache {
    private static final int MAX_CACHED = 3;
    private static final Handler HANDLER = new Handler(Looper.getMainLooper());
    private static final List<Entry> POOL = new ArrayList<>();
    private static class Entry {
        AdView view;
        MutableContextWrapper wrapper;
        String key;
        boolean inUse;
        boolean loaded;
        boolean loading;
    }
    private AdmobBannerCache() {
    }
    /** Puts an adaptive banner into container, reusing a cached one when possible. Main thread only. */
    public static AdView attach(@NonNull Activity activity, @NonNull ViewGroup container) {
        for (int i = 0; i < container.getChildCount(); i++) {
            View child = container.getChildAt(i);
            if (child instanceof AdView) {
                return (AdView) child; // this container already shows a cached banner
            }
        }
        AdSize size = adaptiveSize(activity, container);
        String key = Callback.getAdmobBannerAdID() + "|" + size.getWidth() + "x" + size.getHeight();
        Entry entry = findFree(key);
        if (entry == null) {
            entry = create(activity, key, size);
        }
        entry.inUse = true;
        entry.wrapper.setBaseContext(activity);
        if (entry.view.getParent() instanceof ViewGroup) {
            ((ViewGroup) entry.view.getParent()).removeView(entry.view);
        }
        container.addView(entry.view);
        entry.view.resume();
        if (!entry.loaded && !entry.loading) {
            load(entry);
        }
        final Entry attached = entry;
        container.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(@NonNull View v) {
                // no-op
            }
            @Override
            public void onViewDetachedFromWindow(@NonNull View v) {
                v.removeOnAttachStateChangeListener(this);
                // Defer: the container is still dispatching its detach.
                HANDLER.post(() -> release(attached, container));
            }
        });
        return entry.view;
    }
    private static AdSize adaptiveSize(Activity activity, ViewGroup container) {
        DisplayMetrics dm = activity.getResources().getDisplayMetrics();
        int widthPx = container.getWidth() > 0 ? container.getWidth() : dm.widthPixels;
        int widthDp = Math.max(1, Math.round(widthPx / dm.density));
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, widthDp);
    }
    private static Entry findFree(String key) {
        for (Entry e : POOL) {
            if (!e.inUse && e.key.equals(key)) {
                return e;
            }
        }
        return null;
    }
    private static Entry create(Activity activity, String key, AdSize size) {
        Entry e = new Entry();
        e.key = key;
        e.wrapper = new MutableContextWrapper(activity.getApplicationContext());
        e.view = new AdView(e.wrapper);
        e.view.setAdUnitId(Callback.getAdmobBannerAdID());
        e.view.setAdSize(size);
        e.view.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                e.loaded = true;
                e.loading = false;
            }
            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError error) {
                e.loaded = false;
                e.loading = false;
            }
        });
        POOL.add(e);
        return e;
    }
    private static void load(Entry e) {
        e.loading = true;
        e.view.loadAd(new AdRequest.Builder().build());
    }
    private static void release(Entry e, ViewGroup container) {
        if (container.isAttachedToWindow()) {
            return; // re-attached before the deferred release ran
        }
        container.removeView(e.view);
        e.view.pause();
        e.wrapper.setBaseContext(((Context) e.wrapper.getApplicationContext()));
        e.inUse = false;
        if (POOL.size() > MAX_CACHED) {
            POOL.remove(e);
            e.view.destroy();
        }
    }
}
