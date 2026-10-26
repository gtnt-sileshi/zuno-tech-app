# ============================================================
#  Zumi Pro — R8 / ProGuard rules
#  (identical strategy to Fanos Pro — same ad SDK stack)
# ============================================================

-repackageclasses ''
-allowaccessmodification
-overloadaggressively

-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# ── App entry points ─────────────────────────────────────────
-keep class com.zumipro.app.activity.MyApplication { <init>(); }
-keep class com.zumipro.app.service.ZumiProMessagingService { <init>(); }
-keep class com.zumipro.app.cast.CastOptionsProvider { <init>(); *; }

# ── Gson ─────────────────────────────────────────────────────
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep,allowobfuscation class * implements com.google.gson.TypeAdapterFactory
-keep,allowobfuscation class * implements com.google.gson.JsonSerializer
-keep,allowobfuscation class * implements com.google.gson.JsonDeserializer

# ── EventBus ─────────────────────────────────────────────────
-keepclassmembers class ** {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }

# ── Parcelable ───────────────────────────────────────────────
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# ── Media3 / ExoPlayer ───────────────────────────────────────
-keep,allowobfuscation class androidx.media3.common.MediaItem { *; }
-keep,allowobfuscation class androidx.media3.common.Player { *; }
-dontwarn androidx.media3.**
-dontnote androidx.media3.**

# ── AdMob ────────────────────────────────────────────────────
-keepclassmembers,allowobfuscation class com.google.android.gms.ads.** { public *; }
-dontwarn com.google.android.gms.**
-dontnote com.google.android.gms.**

# ── IronSource ───────────────────────────────────────────────
-keep,allowobfuscation public class com.ironsource.mediationsdk.IronSource {
    public static void init(...);
    public static void loadInterstitial(...);
    public static void showInterstitial(...);
    public static boolean isInterstitialReady(...);
    public static void loadBanner(...);
    public static ** createBanner(...);
    public static void setLevelPlayInterstitialListener(...);
}
-keep,allowobfuscation class com.ironsource.mediationsdk.IronSourceBannerLayout { *; }
-keep,allowobfuscation class com.ironsource.mediationsdk.ISBannerSize { *; }
-keep,allowobfuscation interface com.ironsource.mediationsdk.sdk.LevelPlayInterstitialListener { *; }
-keep,allowobfuscation class com.ironsource.mediationsdk.adunit.adapter.utility.AdInfo { *; }
-keep,allowobfuscation class com.ironsource.mediationsdk.logger.IronSourceError { *; }
-dontwarn com.ironsource.**
-dontnote com.ironsource.**
-dontnote com.ironsource.adapters.**

# ── Facebook Audience Network ────────────────────────────────
-keep,allowobfuscation public class com.facebook.ads.AudienceNetworkAds {
    public static void initialize(...);
}
-keep,allowobfuscation public class com.facebook.ads.AdSettings { *; }
-dontwarn com.facebook.ads.**
-dontnote com.facebook.ads.**

# ── AppLovin MAX ─────────────────────────────────────────────
-keep,allowobfuscation class com.applovin.sdk.AppLovinSdk {
    public static ** getInstance(...);
    public void setMediationProvider(...);
    public boolean isInitialized();
    public static void initializeSdk(...);
}
-keep,allowobfuscation class com.applovin.mediation.ads.MaxAdView { *; }
-keep,allowobfuscation interface com.applovin.mediation.MaxAdListener { *; }
-keep,allowobfuscation interface com.applovin.mediation.MaxRewardedAdListener { *; }
-keep,allowobfuscation class com.applovin.mediation.MaxAd { *; }
-keep,allowobfuscation class com.applovin.mediation.MaxError { *; }
-dontwarn com.applovin.**
-dontnote com.applovin.**

# ── StartApp ─────────────────────────────────────────────────
-keep,allowobfuscation class com.startapp.sdk.adsbase.StartAppSDK {
    public static void init(...);
    public static void setUserConsent(...);
}
-keep,allowobfuscation class com.startapp.sdk.adsbase.StartAppAd {
    public static void disableSplash();
    public <init>(...);
    public void loadAd(...);
    public void showAd(...);
    public boolean isReady();
}
-keep,allowobfuscation class com.startapp.sdk.ads.banner.Banner { *; }
-keep,allowobfuscation interface com.startapp.sdk.adsbase.adlisteners.AdDisplayListener { *; }
-keep,allowobfuscation class com.startapp.sdk.adsbase.Ad { *; }
-dontwarn com.startapp.**
-dontnote com.startapp.**

# ── Unity Ads ────────────────────────────────────────────────
-keep,allowobfuscation class com.unity3d.ads.UnityAds {
    public static void initialize(...);
    public static void show(...);
}
-keep,allowobfuscation class com.unity3d.services.banners.BannerView { *; }
-keep,allowobfuscation class com.unity3d.services.banners.UnityBannerSize { *; }
-keep,allowobfuscation interface com.unity3d.ads.IUnityAdsInitializationListener { *; }
-keep,allowobfuscation interface com.unity3d.ads.IUnityAdsShowListener { *; }
-dontwarn com.unity3d.**
-dontnote com.unity3d.**

# ── Yandex Ads ───────────────────────────────────────────────
-keep,allowobfuscation class com.yandex.mobile.ads.common.YandexAds {
    public static void initialize(...);
}
-keep,allowobfuscation class com.yandex.mobile.ads.banner.BannerAdView { *; }
-keep,allowobfuscation class com.yandex.mobile.ads.common.AdRequest { *; }
-keep,allowobfuscation interface com.yandex.mobile.ads.interstitial.InterstitialAdEventListener { *; }
-keep,allowobfuscation class com.yandex.mobile.ads.common.AdError { *; }
-dontwarn com.yandex.**
-dontnote com.yandex.**

# ── Firebase ─────────────────────────────────────────────────
-dontwarn com.google.firebase.**
-dontnote com.google.firebase.**

# ── WorkManager ──────────────────────────────────────────────
-keep class androidx.work.impl.WorkDatabase { <init>(); }
-keep class androidx.work.impl.WorkDatabase_Impl { <init>(); }
-keepclassmembers,allowobfuscation class androidx.work.** { *; }
-keepclassmembers class * extends androidx.work.Worker {
    <init>(android.content.Context, androidx.work.WorkerParameters);
}
-keepclassmembers class * extends androidx.work.ListenableWorker {
    <init>(android.content.Context, androidx.work.WorkerParameters);
}
-dontnote androidx.work.**

# ── Room ─────────────────────────────────────────────────────
-keep,allowobfuscation class * extends androidx.room.RoomDatabase { <init>(); }
-keep,allowobfuscation @androidx.room.Database class * { <init>(); }
-keepclassmembers,allowobfuscation @androidx.room.Dao class * { *; }
-keepclassmembers,allowobfuscation @androidx.room.Entity class * { *; }
-dontwarn androidx.room.**
-dontnote androidx.room.**

# ── Misc ─────────────────────────────────────────────────────
-dontwarn com.squareup.okhttp.**
-dontwarn org.apache.http.**
-dontwarn sun.misc.**
-dontwarn org.jetbrains.annotations.**
-dontwarn com.bytedance.**
-dontwarn com.yandex.metrica.**
-dontnote com.google.ads.mediation.**
-dontnote com.facebook.infer.**
-dontnote com.wortise.**
