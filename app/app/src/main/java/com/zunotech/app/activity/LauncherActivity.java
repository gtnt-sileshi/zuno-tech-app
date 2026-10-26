package com.zunotech.app.activity;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.zunotech.app.BuildConfig;
import com.zunotech.app.R;
import com.zunotech.app.callback.Callback;
import com.zunotech.app.executor.LoadAbout;
import com.zunotech.app.interfaces.AboutListener;
import com.zunotech.app.utils.helper.DBHelper;
import com.zunotech.app.utils.helper.Helper;
import com.zunotech.app.utils.helper.SPHelper;
import com.zunotech.app.utils.IfSupported;

/**
 * Splash / launcher screen.
 *
 * Redesigned: deep-space background, Z-mark logo, content-pillar chips,
 * animated horizontal fill bar (indeterminate shimmer → progress fill).
 */
public class LauncherActivity extends AppCompatActivity {

    private Helper helper;
    private SPHelper spHelper;
    private DBHelper dbHelper;

    private ProgressBar pb;
    private TextView tvLoadingStatus;

    /** Animates the progress bar from its current value to a target value. */
    private ObjectAnimator progressAnimator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        IfSupported.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        helper   = new Helper(this);
        spHelper = new SPHelper(this);
        dbHelper = new DBHelper(this);

        pb              = findViewById(R.id.pb_splash);
        tvLoadingStatus = findViewById(R.id.tv_loading_status);

        TextView tvVersion = findViewById(R.id.tv_version);
        tvVersion.setText(getString(R.string.version_name, BuildConfig.VERSION_NAME));

        loadAboutData();
    }

    // ── Progress bar helpers ──────────────────────────────────────────────

    /** Show the bar and status label, animate progress from 0 → target over durationMs. */
    private void showProgress(int targetProgress, long durationMs, String statusText) {
        if (pb.getVisibility() != View.VISIBLE) {
            pb.setProgress(0);
            pb.setVisibility(View.VISIBLE);
        }
        if (statusText != null) {
            tvLoadingStatus.setText(statusText);
            tvLoadingStatus.setVisibility(View.VISIBLE);
        }
        if (progressAnimator != null) progressAnimator.cancel();
        progressAnimator = ObjectAnimator.ofInt(pb, "progress", pb.getProgress(), targetProgress);
        progressAnimator.setDuration(durationMs);
        progressAnimator.setInterpolator(new DecelerateInterpolator());
        progressAnimator.start();
    }

    /** Fill to 100 % then hide bar + label. */
    private void completeAndHideProgress(Runnable onComplete) {
        showProgress(100, 400, null);
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            pb.setVisibility(View.GONE);
            tvLoadingStatus.setVisibility(View.INVISIBLE);
            if (onComplete != null) onComplete.run();
        }, 450);
    }

    /** Pulse the bar from current → 85 % (waiting for ad / network). */
    private void startWaitingPulse() {
        showProgress(70, 1800, getString(R.string.splash_loading));
    }

    // ── Data loading ──────────────────────────────────────────────────────

    private void loadAboutData() {
        // Animate to 15 % immediately to show something is happening
        showProgress(15, 400, getString(R.string.splash_loading));

        if (!helper.isNetworkAvailable()) {
            if (Boolean.TRUE.equals(spHelper.getIsFirst())) {
                hideProgressInstant();
                errorDialog(getString(R.string.err_internet_not_connected),
                        getString(R.string.err_try_internet_connected));
                return;
            }
            try {
                dbHelper.getAbout();
                showProgress(80, 600, null);
                new Handler(Looper.getMainLooper()).postDelayed(this::proceedToApp, 650);
            } catch (Exception e) {
                hideProgressInstant();
                errorDialog(getString(R.string.err_internet_not_connected),
                        getString(R.string.err_try_internet_connected));
            }
            return;
        }

        LoadAbout loadAbout = new LoadAbout(LauncherActivity.this, new AboutListener() {
            @Override
            public void onStart() {
                showProgress(30, 600, getString(R.string.splash_loading));
            }

            @Override
            public void onEnd(String success, String verifyStatus, String message) {
                if (isFinishing()) return;
                if ("1".equals(success)) {
                    if (!"-1".equals(verifyStatus) && !"-2".equals(verifyStatus)) {
                        dbHelper.addToAbout();
                        showProgress(80, 500, null);
                        new Handler(Looper.getMainLooper()).postDelayed(
                                LauncherActivity.this::proceedToApp, 550);
                    } else {
                        hideProgressInstant();
                        errorDialog(getString(R.string.err_unauthorized_access), message);
                    }
                } else {
                    hideProgressInstant();
                    errorDialog(getString(R.string.err_server),
                            getString(R.string.err_server_not_connected));
                }
            }
        });
        loadAbout.execute();
    }

    private void hideProgressInstant() {
        if (progressAnimator != null) progressAnimator.cancel();
        pb.setVisibility(View.GONE);
        tvLoadingStatus.setVisibility(View.INVISIBLE);
    }

    // ── App flow ──────────────────────────────────────────────────────────

    private void proceedToApp() {
        if (Boolean.TRUE.equals(Callback.getIsAppUpdate())
                && Callback.getAppNewVersion() != BuildConfig.VERSION_CODE) {
            openDialogActivity(Callback.DIALOG_TYPE_UPDATE);
        } else if (Boolean.TRUE.equals(spHelper.getIsMaintenance())) {
            openDialogActivity(Callback.DIALOG_TYPE_MAINTENANCE);
        } else {
            spHelper.setIsFirst(false);
            MyApplication app = (MyApplication) getApplication();
            app.loadAd(LauncherActivity.this);
            openMainActivity();
        }
    }

    private void openMainActivity() {
        startWaitingPulse();

        // Poll every 200 ms until the ad is loaded or the 8-second timeout expires.
        final int POLL_INTERVAL_MS = 200;
        final int MAX_WAIT_MS      = 8000;
        final int[] elapsed        = {0};

        Handler handler = new Handler(Looper.getMainLooper());
        Runnable pollRunnable = new Runnable() {
            @Override
            public void run() {
                if (isFinishing()) return;

                MyApplication app = (MyApplication) getApplication();

                if (app.getAppOpenAdManager().isAdAvailable()
                        && Boolean.TRUE.equals(!app.getAppOpenAdManager().getIsShowingAd())) {
                    completeAndHideProgress(() ->
                            app.getAppOpenAdManager().showAdIfAvailable(LauncherActivity.this));
                    return;
                }

                elapsed[0] += POLL_INTERVAL_MS;
                if (elapsed[0] >= MAX_WAIT_MS) {
                    completeAndHideProgress(() -> {
                        Intent intent = new Intent(LauncherActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    });
                    return;
                }

                handler.postDelayed(this, POLL_INTERVAL_MS);
            }
        };
        // Minimum 1-second splash before we start polling
        handler.postDelayed(pollRunnable, 1000);
    }

    private void openDialogActivity(String type) {
        Intent intent = new Intent(LauncherActivity.this, DialogActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("from", type);
        startActivity(intent);
        finish();
    }

    private void errorDialog(String title, String message) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                LauncherActivity.this, R.style.dialogTheme);
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setCancelable(false);
        if (title.equals(getString(R.string.err_internet_not_connected))
                || title.equals(getString(R.string.err_server_not_connected))) {
            alertDialog.setNegativeButton(getString(R.string.retry),
                    (dialog, which) -> loadAboutData());
        }
        alertDialog.setPositiveButton(getString(R.string.exit),
                (dialog, which) -> finish());
        alertDialog.show();
    }

    @Override
    protected void onDestroy() {
        if (progressAnimator != null) progressAnimator.cancel();
        try {
            if (dbHelper != null) dbHelper.close();
        } catch (Exception e) {
            Log.e("LauncherActivity", "Error in closing", e);
        }
        super.onDestroy();
    }
}
