package com.zunotech.app.executor;

import com.zunotech.app.utils.advertising.AdConfig;
import com.zunotech.app.utils.advertising.AdFormat;
import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import com.zunotech.app.callback.Callback;
import com.zunotech.app.callback.Method;
import com.zunotech.app.interfaces.AboutListener;
import com.zunotech.app.item.ItemAbout;
import com.zunotech.app.utils.ApplicationUtil;
import com.zunotech.app.utils.AsyncTaskExecutor;
import com.zunotech.app.utils.helper.Helper;
import com.zunotech.app.utils.helper.SPHelper;

public class LoadAbout extends AsyncTaskExecutor<String, String, String> {

    private final Helper helper;
    private final SPHelper spHelper;
    private final AboutListener aboutListener;
    private String verifyStatus = "0";
    private String message = "";

    public LoadAbout(Context context, AboutListener aboutListener) {
        this.aboutListener = aboutListener;
        helper = new Helper(context);
        spHelper = new SPHelper(context);
    }

    @Override
    protected void onPreExecute() {
        aboutListener.onStart();
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String strings) {
        try {
            String json = ApplicationUtil.responsePost(Callback.API_URL, helper.getAPIRequest(
                    Method.METHOD_APP_DETAILS,
                    0, "", "", "", "", "",
                    "", "", "", "", "", "", null));
            String result = parse(json);
            // Keep the last good response so the next start can skip the network wait.
            if ("1".equals(result) && "0".equals(verifyStatus)) {
                spHelper.setAboutJson(json);
            }
            return result;
        } catch (Exception ee) {
            return "0";
        }
    }
    /**
     * Applies the last successful app_details response from disk. Returns true if a cached
     * response existed and was applied. Cheap enough to call on the main thread at startup.
     */
    public static boolean applyCached(Context context) {
        try {
            String json = new SPHelper(context).getAboutJson();
            if (json == null || json.isEmpty()) {
                return false;
            }
            return "1".equals(new LoadAbout(context, null).parse(json));
        } catch (Exception e) {
            return false;
        }
    }
    private String parse(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray jsonArray = jsonObject.getJSONArray(Callback.TAG_ROOT);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject c = jsonArray.getJSONObject(i);

                if (!c.has(Callback.TAG_SUCCESS) || c.has("app_name")) {

                    // App Details -----------------------------------------------------------------
                    String email = c.getString("app_email");
                    String author = c.getString("app_author");
                    String contact = c.getString("app_contact");
                    String website = c.getString("app_website");
                    String description = c.getString("app_description");
                    String developed = c.getString("app_developed_by");
                    String moreApps = "";
                    if (c.has("more_apps_url")){
                        moreApps = c.getString("more_apps_url");
                    }
                    Callback.setItemAbout(new ItemAbout(email, author, contact, website, description, developed, moreApps));

                    // API Latest Limit ------------------------------------------------------------
                    Callback.setRecentLimit(Integer.parseInt(c.optString("api_latest_limit","10")));

                    // isSupported -----------------------------------------------------------------
                    Boolean isRtl         = Boolean.parseBoolean(c.optString("isRTL", "false"));
                    Boolean isMaintenance = Boolean.parseBoolean(c.optString("isMaintenance", "false"));
                    Boolean isScreenshot  = Boolean.parseBoolean(c.optString("isScreenshot", "false"));
                    Boolean isApk         = Boolean.parseBoolean(c.optString("isAPK", "false"));
                    Boolean isVpn         = Boolean.parseBoolean(c.optString("isVPN", "false"));
                    spHelper.setIsSupported(isRtl, isMaintenance, isScreenshot, isApk, isVpn);

                    // AppUpdate -------------------------------------------------------------------
                    if (c.has("app_update_status")){
                        Boolean isAppUpdate = Boolean.parseBoolean(c.getString("app_update_status"));
                        Callback.setIsAppUpdate(isAppUpdate);
                        if(!c.getString("app_new_version").isEmpty()) {
                            int appNew = Integer.parseInt(c.getString("app_new_version"));
                            Callback.setAppNewVersion(appNew);
                        }
                        Callback.setAppUpdateDesc(c.getString("app_update_desc"));
                        Callback.setAppRedirectUrl(c.getString("app_redirect_url"));
                    }

                    // Ads Network -----------------------------------------------------------------
                    if (c.has("ad_status")){
                        // AdMob only: ad_network is ignored, IDs always come from admob_* keys.
                        AdConfig adConfig = AdConfig.fromJson(c);
                        Callback.setAdConfig(adConfig);
                        Boolean isAds = adConfig.isAdsEnabled();
                        Callback.setIsAdsStatus(isAds);
                        spHelper.setIsads(isAds);
                        if (Boolean.TRUE.equals(isAds)){
                            Callback.setAdmobPublisherID(adConfig.publisherId());
                            Callback.setAdmobBannerAdID(adConfig.id(AdFormat.BANNER));
                            Callback.setAdmobInterstitialAdID(adConfig.id(AdFormat.INTERSTITIAL));
                            Callback.setAdmobNativeAdID(adConfig.id(AdFormat.NATIVE));
                            Callback.setAdmobOpenAdID(adConfig.id(AdFormat.APP_OPEN));
                            Callback.setAdmobRewardAdID(adConfig.id(AdFormat.REWARD));

                            // ADS PLACEMENT -------------------------------------------------------
                            Callback.setIsOpenAd(Boolean.parseBoolean(c.getString("app_open_ad_on_start")));
                            Callback.setIsBannerAdHome(Boolean.parseBoolean(c.getString("banner_home")));
                            Callback.setIsBannerAdPostDetails(Boolean.parseBoolean(c.getString("banner_post_details")));
                            Callback.setIsBannerAdCatDetails(Boolean.parseBoolean(c.getString("banner_category_details")));
                            Callback.setIsBannerAdSearch(Boolean.parseBoolean(c.getString("banner_search")));
                            Callback.setIsInterAd(Boolean.parseBoolean(c.getString("interstitial_post_list")));
                            Callback.setIsNativeAdPost(Boolean.parseBoolean(c.getString("native_ad_post_list")));
                            Callback.setIsNativeAdCat(Boolean.parseBoolean(c.getString("native_ad_category_list")));
                            Callback.setIsRewardAd(Boolean.parseBoolean(c.getString("reward_ad_on")));

                            // GLOBAL CONFIGURATION
                            Callback.setInterstitialAdShow(Integer.parseInt(c.optString("interstital_ad_click","5")));
                            Callback.setNativeAdShow(Integer.parseInt(c.optString("native_position","6")));
                            Callback.setRewardCredit(Integer.parseInt(c.optString("reward_credit","1")));
                        } else {
                            // Ads are disabled — clear all placement flags so stale values
                            // from a previous session where ads were enabled don't linger.
                            Callback.setIsOpenAd(false);
                            Callback.setIsBannerAdHome(false);
                            Callback.setIsBannerAdPostDetails(false);
                            Callback.setIsBannerAdCatDetails(false);
                            Callback.setIsBannerAdSearch(false);
                            Callback.setIsInterAd(false);
                            Callback.setIsNativeAdPost(false);
                            Callback.setIsNativeAdCat(false);
                            Callback.setIsRewardAd(false);
                        }
                    }

                    // Player ----------------------------------------------------------------------
                    if(!c.getString("player_package_name").isEmpty()) {
                        String applicationID = c.getString("player_package_name");
                        spHelper.setHLSVideoPlayer(applicationID);
                    }

                } else {
                    verifyStatus = c.getString(Callback.TAG_SUCCESS);
                    message = c.getString(Callback.TAG_MSG);
                }
            }
            return "1";
        } catch (Exception ee) {
            return "0";
        }
    }

    @Override
    protected void onPostExecute(String s) {
        aboutListener.onEnd(s, verifyStatus, message);
    }
}