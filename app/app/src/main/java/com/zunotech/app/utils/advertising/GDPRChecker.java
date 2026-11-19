package com.zunotech.app.utils.advertising;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.UserMessagingPlatform;
import com.zunotech.app.callback.Callback;
/**
 * Google UMP consent handling. Ads are requested only when UMP reports canRequestAds().
 */
public class GDPRChecker {
    private static final String TAG = "GDPRChecker";
    private Activity activity;
    private final Context context;
    public GDPRChecker(Activity activity) {
        this.activity = activity;
        this.context = activity.getApplicationContext();
    }
    public GDPRChecker(Context context) {
        Context app = context.getApplicationContext();
        this.context = app != null ? app : context;
    }
    /** Refreshes consent info and shows the consent form when UMP requires it. */
    public void check() {
        if (activity == null) {
            return;
        }
        ConsentRequestParameters parameters = new ConsentRequestParameters.Builder()
                // For testing outside the EEA, add .setConsentDebugSettings(...) here in debug builds only.
                .setTagForUnderAgeOfConsent(false)
                .build();
        ConsentInformation info = UserMessagingPlatform.getConsentInformation(activity);
        info.requestConsentInfoUpdate(activity, parameters,
                () -> UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity, formError -> {
                    if (formError != null) {
                        Log.e(TAG, "Consent form error: " + formError.getMessage());
                    }
                    if (info.canRequestAds()) {
                        onConsentReady();
                    }
                }),
                requestError -> {
                    Log.e(TAG, "Consent info update failed: " + requestError.getMessage());
                    // Consent from a previous session can still allow ad requests.
                    if (info.canRequestAds()) {
                        onConsentReady();
                    }
                });
    }
    private void onConsentReady() {
        if (Boolean.TRUE.equals(Callback.getIsInterAd()) && Boolean.TRUE.equals(Callback.getIsAdsStatus())) {
            new AdManagerInterAdmob(context).createAd();
        }
    }
    public boolean canLoadAd() {
        return UserMessagingPlatform.getConsentInformation(context).canRequestAds();
    }
    public boolean canLoadAdOpenAds() {
        return !Callback.getIsAppOpenAdShown() && canLoadAd();
    }
}
