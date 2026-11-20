package com.zunotech.app.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.content.res.Configuration;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;

import com.zunotech.app.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Inflates activity_launcher.xml at several widths and checks the centered content column
 * (Requirements 4.1, 4.3) in light and night mode, checks that no chip views remain, checks the
 * four expected view ids, and compares a screenshot against a stored baseline.
 *
 * Baselines live in src/test/resources/screenshots/zuno-tech/. They are written when missing or when
 * -Dscreenshots.record=true or env SCREENSHOTS_RECORD=true is set.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34, application = android.app.Application.class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public class LauncherLayoutTest {

    static final int[] WIDTHS_DP = {320, 360, 411, 600};
    static final int HEIGHT_DP = 640;
    static final float MAX_GAP_DP = 24f;
    static final float CENTER_TOLERANCE_DP = 4f;

    /** Inflates, measures and lays out the launcher at the given width. */
    static View inflateLauncher(int widthDp, boolean night) {
        String qualifiers = "w" + widthDp + "dp-h" + HEIGHT_DP + "dp" + (night ? "-night" : "-notnight");
        RuntimeEnvironment.setQualifiers(qualifiers);
        Context base = RuntimeEnvironment.getApplication();
        Context ctx = new ContextThemeWrapper(base, R.style.AppTheme);
        View root = LayoutInflater.from(ctx).inflate(R.layout.activity_launcher, null, false);
        float density = ctx.getResources().getDisplayMetrics().density;
        int w = Math.round(widthDp * density);
        int h = Math.round(HEIGHT_DP * density);
        root.measure(View.MeasureSpec.makeMeasureSpec(w, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(h, View.MeasureSpec.EXACTLY));
        root.layout(0, 0, w, h);
        return root;
    }

    /** Children of the center column that take part in layout (not GONE), in order. */
    static List<View> columnChildren(View root) {
        ViewGroup column = root.findViewById(R.id.ll_center);
        assertNotNull("ll_center missing", column);
        List<View> out = new ArrayList<>();
        for (int i = 0; i < column.getChildCount(); i++) {
            View v = column.getChildAt(i);
            if (v.getVisibility() != View.GONE) out.add(v);
        }
        return out;
    }

    /** Left edge of a view relative to the root. */
    static float leftIn(View root, View v) {
        float x = 0;
        View cur = v;
        while (cur != root) {
            x += cur.getLeft();
            cur = (View) cur.getParent();
        }
        return x;
    }

    static void assertColumnLayout(int widthDp, boolean night) {
        View root = inflateLauncher(widthDp, night);
        float density = root.getResources().getDisplayMetrics().density;
        List<View> children = columnChildren(root);
        assertTrue("column needs at least 2 children", children.size() >= 2);

        for (int i = 1; i < children.size(); i++) {
            View prev = children.get(i - 1);
            View next = children.get(i);
            float gapDp = (next.getTop() - prev.getBottom()) / density;
            assertTrue("gap " + gapDp + "dp between child " + (i - 1) + " and " + i
                    + " at " + widthDp + "dp", gapDp <= MAX_GAP_DP);
        }

        float rootCenter = root.getWidth() / 2f;
        for (int i = 0; i < children.size(); i++) {
            View v = children.get(i);
            float center = leftIn(root, v) + v.getWidth() / 2f;
            float offDp = Math.abs(center - rootCenter) / density;
            assertTrue("child " + i + " off center by " + offDp + "dp at " + widthDp + "dp",
                    offDp <= CENTER_TOLERANCE_DP);
        }
    }

    @Test
    public void centerColumnIsTightAndCentered_320dp() {
        assertColumnLayout(320, false);
    }

    @Test
    public void centerColumnIsTightAndCentered_360dp() {
        assertColumnLayout(360, false);
    }

    @Test
    public void centerColumnIsTightAndCentered_411dp() {
        assertColumnLayout(411, false);
    }

    @Test
    public void centerColumnIsTightAndCentered_600dp() {
        assertColumnLayout(600, false);
    }

    // ---- Task 10.8: light and night coverage, ids, chips, screenshots ----

    static final String APP_NAME = "zuno-tech";
    static final int SCREENSHOT_WIDTH_DP = 360;
    /** Per-channel difference below which two pixels count as equal. */
    static final int CHANNEL_TOLERANCE = 8;
    /** Fraction of pixels allowed to differ. */
    static final double MAX_DIFFERING_FRACTION = 0.01;

    @Test
    public void columnLayoutAllWidthsLightAndNight() {
        for (boolean night : new boolean[]{false, true}) {
            for (int w : WIDTHS_DP) {
                assertColumnLayout(w, night);
            }
        }
    }

    @Test
    public void fourSplashIdsExistAndNoChipsAllWidthsLightAndNight() throws Exception {
        for (boolean night : new boolean[]{false, true}) {
            for (int w : WIDTHS_DP) {
                View root = inflateLauncher(w, night);
                String where = w + "dp night=" + night;
                assertNotNull("iv_splash_logo " + where, root.findViewById(R.id.iv_splash_logo));
                assertNotNull("tv_loading_status " + where, root.findViewById(R.id.tv_loading_status));
                assertNotNull("pb_splash " + where, root.findViewById(R.id.pb_splash));
                assertNotNull("tv_version " + where, root.findViewById(R.id.tv_version));
                assertNoChips(root, where);
            }
        }
        assertNoChipResources();
    }

    private static void assertNoChips(View v, String where) {
        String cls = v.getClass().getName().toLowerCase(java.util.Locale.ROOT);
        assertTrue("chip view class " + cls + " at " + where, !cls.contains("chip"));
        if (v.getId() != View.NO_ID) {
            String entry = v.getResources().getResourceEntryName(v.getId()).toLowerCase(java.util.Locale.ROOT);
            assertTrue("chip id " + entry + " at " + where, !entry.contains("chip"));
        }
        if (v instanceof ViewGroup) {
            ViewGroup g = (ViewGroup) v;
            for (int i = 0; i < g.getChildCount(); i++) assertNoChips(g.getChildAt(i), where);
        }
    }

    private static void assertNoChipResources() {
        for (Class<?> inner : R.class.getDeclaredClasses()) {
            for (Field f : inner.getDeclaredFields()) {
                assertTrue("splash chip resource " + inner.getSimpleName() + "." + f.getName(),
                        !f.getName().toLowerCase(java.util.Locale.ROOT).contains("splash_chip"));
            }
        }
    }

    @Test
    public void screenshotMatchesBaseline_light() throws IOException {
        checkScreenshot(false);
    }

    @Test
    public void screenshotMatchesBaseline_night() throws IOException {
        checkScreenshot(true);
    }

    private static boolean recordRequested() {
        return Boolean.getBoolean("screenshots.record")
                || "true".equalsIgnoreCase(System.getenv("SCREENSHOTS_RECORD"));
    }

    private static Bitmap render(boolean night) {
        View root = inflateLauncher(SCREENSHOT_WIDTH_DP, night);
        Bitmap bmp = Bitmap.createBitmap(root.getWidth(), root.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);
        canvas.drawColor(Color.WHITE);
        root.draw(canvas);
        return bmp;
    }

    private void checkScreenshot(boolean night) throws IOException {
        Bitmap actual = render(night);
        File file = new File("src/test/resources/screenshots/" + APP_NAME,
                "launcher_" + (night ? "night" : "light") + ".png");
        if (recordRequested() || !file.exists()) {
            File dir = file.getParentFile();
            if (!dir.isDirectory() && !dir.mkdirs()) throw new IOException("cannot create " + dir);
            try (FileOutputStream out = new FileOutputStream(file)) {
                assertTrue("PNG encode failed", actual.compress(Bitmap.CompressFormat.PNG, 100, out));
            }
            return;
        }
        Bitmap expected = android.graphics.BitmapFactory.decodeFile(file.getPath());
        assertNotNull("cannot decode baseline " + file, expected);
        assertEquals("width", expected.getWidth(), actual.getWidth());
        assertEquals("height", expected.getHeight(), actual.getHeight());
        long differing = 0;
        for (int y = 0; y < actual.getHeight(); y++) {
            for (int x = 0; x < actual.getWidth(); x++) {
                int a = actual.getPixel(x, y);
                int e = expected.getPixel(x, y);
                if (Math.abs(Color.red(a) - Color.red(e)) > CHANNEL_TOLERANCE
                        || Math.abs(Color.green(a) - Color.green(e)) > CHANNEL_TOLERANCE
                        || Math.abs(Color.blue(a) - Color.blue(e)) > CHANNEL_TOLERANCE
                        || Math.abs(Color.alpha(a) - Color.alpha(e)) > CHANNEL_TOLERANCE) {
                    differing++;
                }
            }
        }
        double fraction = differing / (double) ((long) actual.getWidth() * actual.getHeight());
        assertTrue("screenshot differs from " + file + " in " + (fraction * 100) + " percent of pixels"
                + " (rerun with -Dscreenshots.record=true or SCREENSHOTS_RECORD=true to update)",
                fraction <= MAX_DIFFERING_FRACTION);
    }
}
