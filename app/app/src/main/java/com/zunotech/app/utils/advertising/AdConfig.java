package com.zunotech.app.utils.advertising;

import org.json.JSONObject;

/**
 * AdMob-only ad configuration parsed from the app_details response.
 * The "ad_network" key is ignored: AdMob IDs are always read from the admob_* keys.
 * Pure Java (no Android framework) so it can be unit tested on the JVM.
 */
public final class AdConfig {

    private final boolean adsEnabled;
    private final String publisherId;
    private final String bannerId;
    private final String interstitialId;
    private final String nativeId;
    private final String appOpenId;
    private final String rewardId;

    private AdConfig(boolean adsEnabled, String publisherId, String bannerId, String interstitialId,
                     String nativeId, String appOpenId, String rewardId) {
        this.adsEnabled = adsEnabled;
        this.publisherId = publisherId;
        this.bannerId = bannerId;
        this.interstitialId = interstitialId;
        this.nativeId = nativeId;
        this.appOpenId = appOpenId;
        this.rewardId = rewardId;
    }

    /** Never throws. Missing, null or malformed keys become empty strings; ads default to disabled. */
    public static AdConfig fromJson(JSONObject c) {
        if (c == null) {
            return new AdConfig(false, "", "", "", "", "", "");
        }
        boolean enabled = "true".equalsIgnoreCase(read(c, "ad_status").trim());
        return new AdConfig(
                enabled,
                read(c, "admob_publisher_id"),
                read(c, "admob_banner_unit_id"),
                read(c, "admob_interstitial_unit_id"),
                read(c, "admob_native_unit_id"),
                read(c, "admob_app_open_ad_unit_id"),
                read(c, "admob_reward_ad_unit_id"));
    }

    private static String read(JSONObject c, String key) {
        try {
            if (c.isNull(key)) {
                return "";
            }
            String v = c.optString(key, "");
            return v == null ? "" : v;
        } catch (Exception e) {
            return "";
        }
    }

    public boolean isAdsEnabled() {
        return adsEnabled;
    }

    public String publisherId() {
        return publisherId;
    }

    public String id(AdFormat format) {
        if (format == null) {
            return "";
        }
        switch (format) {
            case BANNER:
                return bannerId;
            case INTERSTITIAL:
                return interstitialId;
            case NATIVE:
                return nativeId;
            case APP_OPEN:
                return appOpenId;
            case REWARD:
                return rewardId;
            default:
                return "";
        }
    }

    /** True only when ads are enabled and an AdMob ID exists for the format. */
    public boolean shouldRequest(AdFormat format) {
        return adsEnabled && !id(format).trim().isEmpty();
    }
}
