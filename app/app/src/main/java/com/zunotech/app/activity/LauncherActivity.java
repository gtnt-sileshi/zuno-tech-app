package com.zunotech.app.activity;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
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
 * Launcher screen.
 *
 * Returning users start from the cached app config and go straight to MainActivity without
 * inflating a layout or waiting on the network. MainActivity refreshes the config in the
 * background. Only the first launch (no cached config) shows the progress UI and waits for
 * the server.
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

        spHelper = new SPHelper(this);

        // Returning users: no layout, no network wait.
        if (canStartFromCache()) {
            openMainActivity();
            return;
        }

        setContentView(R.layout.activity_launcher);

        helper   = new Helper(this);
        dbHelper = new DBHelper(this);

        pb              = findViewById(R.id.pb_splash);
        tvLoadingStatus = findViewById(R.id.tv_loading_status);

        TextView tvVersion = findViewById(R.id.tv_version);
        tvVersion.setText(getString(R.string.version_name, BuildConfig.VERSION_NAME));

        loadAboutData();
    }

    /**
     * True when this is not the first launch and the cached config shows no blocking state.
     * Maintenance and a pending forced update still take the network path, so a flag the
     * server has cleared is picked up.
     */
    private boolean canStartFromCache() {
        if (Boolean.TRUE.equals(spHelper.getIsFirst())) return false;
        if (spHelper.getAboutJson().isEmpty()) return false;
        if (Boolean.TRUE.equals(spHelper.getIsMaintenance())) return false;
        return !(Boolean.TRUE.equals(Callback.getIsAppUpdate())
                && Callback.getAppNewVersion() != BuildConfig.VERSION_CODE);
    }

    // ── Progress bar helpers ──────────────────────────────────────────────

    /** Show the bar and status label, animate progress from its current value to target. */
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

    // ── Data loading (first launch, or maintenance / update pending) ──────

    private void loadAboutData() {
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
                proceedToApp();
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
                        proceedToApp();
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
            openMainActivity();
        }
    }

    private void openMainActivity() {
        // Preload the app-open ad so it can show on the next return to the foreground.
        // The launcher no longer waits for it.
        ((MyApplication) getApplication()).loadAd(this);

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, 0, 0);
        } else {
            overridePendingTransition(0, 0);
        }
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
