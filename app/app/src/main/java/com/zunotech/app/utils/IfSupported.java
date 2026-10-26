package com.zunotech.app.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.zunotech.app.R;
import com.zunotech.app.utils.ThemeManager;
import com.zunotech.app.utils.helper.SPHelper;


public class IfSupported {

    private static final String TAG = "IfSupported";

    private IfSupported() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Apply the current theme variant (Classic/Dark/DarkGrey/DarkBlue).
     * Must be called before setContentView() in each Activity.
     */
    public static void applyTheme(Activity activity) {
        activity.setTheme(ThemeManager.getThemeStyleRes(activity));
    }

    public static void isRTL(Activity mContext) {
        try {
            if (Boolean.TRUE.equals(new SPHelper(mContext).getIsRTL())) {
                Window window = mContext.getWindow();
                window.getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to isRTL", e);
        }
    }

    public static void isScreenshot(Activity mContext) {
        try {
            if (Boolean.TRUE.equals(new SPHelper(mContext).getIsScreenshot())) {
                Window window = mContext.getWindow();
                window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to isScreenshot", e);
        }
    }

    public static void hideStatusBar(Activity mContext) {
        if (mContext == null) {
            Log.e(TAG, "Activity context is null");
            return;
        }
        try {
            Window window = mContext.getWindow();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.setDecorFitsSystemWindows(false);
                WindowInsetsController controller = window.getDecorView().getWindowInsetsController();
                if (controller != null) {
                    controller.hide(WindowInsets.Type.statusBars());
                    controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
                }
            } else {
                View decorView = window.getDecorView();
                //noinspection deprecation
                decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to hide status bar", e);
        }
    }

    public static void keepScreenOn(Activity mContext) {
        if (mContext == null) {
            Log.e(TAG, "Activity context is null");
            return;
        }
        try {
            Window window = mContext.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } catch (Exception e) {
            Log.e(TAG, "Failed to keep screen on", e);
        }
    }

    /**
     * Tints the SearchView's text, hint, cursor, icons and the menu item icon itself
     * to match the current theme — always readable on both light and dark toolbars.
     * Call this right after inflating the search menu item.
     */
    public static void applySearchViewStyle(Context context, SearchView searchView) {
        applySearchViewStyle(context, searchView, null);
    }

    public static void applySearchViewStyle(Context context, SearchView searchView, android.view.MenuItem menuItem) {
        if (searchView == null || context == null) return;
        try {
            // Resolve ?ns_title (foreground text colour for the current theme)
            TypedValue tv = new TypedValue();
            context.getTheme().resolveAttribute(R.attr.ns_title, tv, true);
            int textColor = tv.data != 0 ? tv.data
                    : ContextCompat.getColor(context, R.color.ns_classic_title);

            // Hint is 60 % opacity of the text colour
            int hintColor = Color.argb(153,
                    Color.red(textColor), Color.green(textColor), Color.blue(textColor));

            // Resolve ?ns_primary for cursor/underline
            TypedValue tvp = new TypedValue();
            context.getTheme().resolveAttribute(R.attr.ns_primary, tvp, true);
            int primaryColor = tvp.data != 0 ? tvp.data
                    : ContextCompat.getColor(context, R.color.ns_classic_primary);

            // Tint the menu item icon (collapsed search icon in toolbar)
            if (menuItem != null) {
                android.graphics.drawable.Drawable icon = menuItem.getIcon();
                if (icon != null) {
                    icon = DrawableCompat.wrap(icon.mutate());
                    DrawableCompat.setTint(icon, textColor);
                    menuItem.setIcon(icon);
                }
            }

            // Apply to the inner EditText
            EditText et = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
            if (et != null) {
                et.setTextColor(textColor);
                et.setHintTextColor(hintColor);
                et.setBackgroundTintList(ColorStateList.valueOf(primaryColor));
            }

            // Tint the icons inside the SearchView widget
            int[] iconIds = {
                androidx.appcompat.R.id.search_button,
                androidx.appcompat.R.id.search_close_btn,
                androidx.appcompat.R.id.search_go_btn,
                androidx.appcompat.R.id.search_mag_icon
            };
            for (int id : iconIds) {
                ImageView iv = searchView.findViewById(id);
                if (iv != null) iv.setColorFilter(textColor);
            }

        } catch (Exception e) {
            Log.e(TAG, "applySearchViewStyle failed", e);
        }
    }
}
