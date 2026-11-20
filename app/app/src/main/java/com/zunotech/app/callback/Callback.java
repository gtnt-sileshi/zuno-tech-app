package com.zunotech.app.callback;

import com.zunotech.app.utils.advertising.AdConfig;
import java.io.Serial;
import java.io.Serializable;

import com.zunotech.app.BuildConfig;
import com.zunotech.app.item.ItemAbout;

public class Callback implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    // API URL — points to Laravel backend
    public static final String API_URL = BuildConfig.BASE_URL+"api/app";

    // TAG_API
    public static final String TAG_ROOT = BuildConfig.API_NAME;
    public static final String TAG_SUCCESS = "success";
    public static final String TAG_MSG = "MSG";


    private static String searchItem = "";
    public static String getSearchItem() {
        return searchItem;
    }
    public static void setSearchItem(String data) {
        searchItem = data;
    }

    // About Details -------------------------------------------------------------------------------
    private static ItemAbout itemAbout = new ItemAbout("", "", "", "", "", "", "");
    public static ItemAbout getItemAbout() {
        return itemAbout;
    }
    public static void setItemAbout(ItemAbout item) {
        itemAbout = item;
    }

    private static boolean isRecreate = false;
    public static boolean isRecreate() {
        return isRecreate;
    }
    public static void setRecreate(boolean recreate) {
        isRecreate = recreate;
    }

    private static int recentLimit = 10;
    public static int getRecentLimit() {
        return recentLimit;
    }
    public static void setRecentLimit(int limit) {
        recentLimit = limit;
    }

    // Update and dialog ---------------------------------------------------------------------------
    public static final String DIALOG_TYPE_UPDATE = "upgrade";
    public static final String DIALOG_TYPE_MAINTENANCE = "maintenance";
    public static final String DIALOG_TYPE_DEVELOPER = "developer";
    public static final String DIALOG_TYPE_VPN = "vpn";

    private static Boolean isAppUpdate = false;
    public static Boolean getIsAppUpdate() {
        return isAppUpdate;
    }
    public static void setIsAppUpdate(Boolean appUpdate) {
        isAppUpdate = appUpdate;
    }

    private static int appNewVersion = 1;
    public static int getAppNewVersion() {
        return appNewVersion;
    }
    public static void setAppNewVersion(int newVersion) {
        appNewVersion = newVersion;
    }

    private static String appUpdateDesc = "";
    public static String getAppUpdateDesc() {
        return appUpdateDesc;
    }
    public static void setAppUpdateDesc(String updateDesc) {
        appUpdateDesc = updateDesc;
    }

    private static String appRedirectUrl = "";
    public static String getAppRedirectUrl() {
        return appRedirectUrl;
    }
    public static void setAppRedirectUrl(String redirectUrl) {
        appRedirectUrl = redirectUrl;
    }

    // Advertising ---------------------------------------------------------------------------------
    public static final String PAGE_HOME = "banner_home";
    public static final String PAGE_POST_DETAILS = "post_details";
    public static final String PAGE_CAT_DETAILS = "category_details";
    public static final String PAGE_SEARCH = "search_page";
    public static final String PAGE_NATIVE_POST = "post_native";
    public static final String PAGE_NATIVE_CAT = "category_native";

    public static final String AD_TYPE_ADMOB = "admob";


    private static Boolean isAdsStatus = false;
    public static Boolean getIsAdsStatus() {
        return isAdsStatus;
    }
    public static void setIsAdsStatus(Boolean adsStatus) {
        isAdsStatus = adsStatus;
    }

    private static int adCount = 0;
    public static int getAdCount() {
        return adCount;
    }
    public static void setAdCount(int count) {
        adCount = count;
    }

    private static int interstitialAdShow = 5;
    // Minimum gap between two interstitials, regardless of the click counter.
    public static final long INTER_AD_MIN_INTERVAL_MS = 60_000L;
    // SystemClock.elapsedRealtime() of the last interstitial; 0 means none shown yet.
    private static long lastInterAdTime = 0;
    public static long getLastInterAdTime() {
        return lastInterAdTime;
    }
    public static void setLastInterAdTime(long time) {
        lastInterAdTime = time;
    }
    public static int getInterstitialAdShow() {
        return interstitialAdShow;
    }
    public static void setInterstitialAdShow(int count) {
        interstitialAdShow = count;
    }

    private static int nativeAdShow = 6;
    public static int getNativeAdShow() {
        return nativeAdShow;
    }
    public static void setNativeAdShow(int count) {
        nativeAdShow = count;
    }

    private static int rewardCredit = 5;
    public static int getRewardCredit() {
        return rewardCredit;
    }
    public static void setRewardCredit(int count) {
        rewardCredit = count;
    }

    // Parsed AdMob configuration; the single decision point for shouldRequest(format).
    private static AdConfig adConfig = AdConfig.fromJson(null);
    public static AdConfig getAdConfig() {
        return adConfig;
    }
    public static void setAdConfig(AdConfig config) {
        adConfig = config == null ? AdConfig.fromJson(null) : config;
    }

    // ADMOB ---------------------------------------------------------------------------------------
    private static String admobPublisherID = "";
    public static String getAdmobPublisherID() {
        return admobPublisherID;
    }
    public static void setAdmobPublisherID(String publisherID) {
        admobPublisherID = publisherID;
    }

    private static String admobBannerAdID = "";
    public static String getAdmobBannerAdID() {
        return admobBannerAdID;
    }
    public static void setAdmobBannerAdID(String bannerAdID) {
        admobBannerAdID = bannerAdID;
    }

    private static String admobInterstitialAdID = "";
    public static String getAdmobInterstitialAdID() {
        return admobInterstitialAdID;
    }
    public static void setAdmobInterstitialAdID(String interstitialAdID) {
        admobInterstitialAdID = interstitialAdID;
    }

    private static String admobRewardAdID = "";
    public static String getAdmobRewardAdID() {
        return admobRewardAdID;
    }
    public static void setAdmobRewardAdID(String rewardAdID) {
        admobRewardAdID = rewardAdID;
    }

    private static String admobNativeAdID = "";
    public static String getAdmobNativeAdID() {
        return admobNativeAdID;
    }
    public static void setAdmobNativeAdID(String nativeAdID) {
        admobNativeAdID = nativeAdID;
    }

    private static String admobOpenAdID = "";
    public static String getAdmobOpenAdID() {
        return admobOpenAdID;
    }
    public static void setAdmobOpenAdID(String openAdID) {
        admobOpenAdID = openAdID;
    }

//Advertising isSupported ----------------------------------------------------------------------
    private static Boolean isOpenAd = false;
    public static Boolean getIsOpenAd() {
        return isOpenAd;
    }
    public static void setIsOpenAd(Boolean openAd) {
        isOpenAd = openAd;
    }

    private static Boolean isAppOpenAdShown = false;
    public static Boolean getIsAppOpenAdShown() {
        return isAppOpenAdShown;
    }
    public static void setIsAppOpenAdShown(Boolean appOpenAdShown) {
        isAppOpenAdShown = appOpenAdShown;
    }

    private static Boolean isBannerAdHome = false;
    public static Boolean getIsBannerAdHome() {
        return isBannerAdHome;
    }
    public static void setIsBannerAdHome(Boolean bannerAdHome) {
        isBannerAdHome = bannerAdHome;
    }

    private static Boolean isBannerAdPostDetails = false;
    public static Boolean getIsBannerAdPostDetails() {
        return isBannerAdPostDetails;
    }
    public static void setIsBannerAdPostDetails(Boolean bannerAdPostDetails) {
        isBannerAdPostDetails = bannerAdPostDetails;
    }

    private static Boolean isBannerAdCatDetails = false;
    public static Boolean getIsBannerAdCatDetails() {
        return isBannerAdCatDetails;
    }
    public static void setIsBannerAdCatDetails(Boolean bannerAdCatDetails) {
        isBannerAdCatDetails = bannerAdCatDetails;
    }

    private static Boolean isBannerAdSearch = false;
    public static Boolean getIsBannerAdSearch() {
        return isBannerAdSearch;
    }
    public static void setIsBannerAdSearch(Boolean bannerAdSearch) {
        isBannerAdSearch = bannerAdSearch;
    }

    private static Boolean isInterAd = false;
    public static Boolean getIsInterAd() {
        return isInterAd;
    }
    public static void setIsInterAd(Boolean interAd) {
        isInterAd = interAd;
    }

    private static Boolean isNativeAdPost = false;
    public static Boolean getIsNativeAdPost() {
        return isNativeAdPost;
    }
    public static void setIsNativeAdPost(Boolean nativeAdPost) {
        isNativeAdPost = nativeAdPost;
    }

    private static Boolean isNativeAdCat = false;
    public static Boolean getIsNativeAdCat() {
        return isNativeAdCat;
    }
    public static void setIsNativeAdCat(Boolean nativeAdCat) {
        isNativeAdCat = nativeAdCat;
    }

    private static Boolean isRewardAd = false;
    public static Boolean getIsRewardAd() {
        return isRewardAd;
    }
    public static void setIsRewardAd(Boolean rewardAd) {
        isRewardAd = rewardAd;
    }

}