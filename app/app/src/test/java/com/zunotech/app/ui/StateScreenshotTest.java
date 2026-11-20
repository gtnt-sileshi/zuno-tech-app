package com.zunotech.app.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Task 10.12: screenshots of the loading, empty and error states of Home, Topics and Saved in light
 * and night mode, compared with baselines in src/test/resources/screenshots/zuno-tech/ (files
 * state_&lt;screen&gt;_&lt;state&gt;_&lt;mode&gt;.png). Baselines are written when missing or when
 * -Dscreenshots.record=true or env SCREENSHOTS_RECORD=true is set.
 *
 * Validates: Requirements 3.2, 4.3
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34, application = android.app.Application.class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public class StateScreenshotTest {

    static final String APP_NAME = "zuno-tech";
    /** Per-channel difference below which two pixels count as equal. */
    static final int CHANNEL_TOLERANCE = 8;
    /** Fraction of pixels allowed to differ. */
    static final double MAX_DIFFERING_FRACTION = 0.01;

    private static boolean recordRequested() {
        return Boolean.getBoolean("screenshots.record")
                || "true".equalsIgnoreCase(System.getenv("SCREENSHOTS_RECORD"));
    }

    @Test
    public void statesMatchBaselines_light() throws IOException {
        checkAll(false);
    }

    @Test
    public void statesMatchBaselines_night() throws IOException {
        checkAll(true);
    }

    private void checkAll(boolean night) throws IOException {
        Context ctx = ScreenStates.context(night);
        List<String> problems = new ArrayList<>();
        for (ScreenStates.Screen screen : ScreenStates.Screen.values()) {
            for (ScreenStates.State state : ScreenStates.State.values()) {
                Bitmap actual = ScreenStates.draw(ScreenStates.screen(ctx, screen, state));
                String name = "state_" + screen.name().toLowerCase(Locale.ROOT) + "_"
                        + state.name().toLowerCase(Locale.ROOT) + "_" + (night ? "night" : "light") + ".png";
                String problem = compare(actual, new File("src/test/resources/screenshots/" + APP_NAME, name));
                if (problem != null) problems.add(problem);
            }
        }
        assertTrue(String.join("\n", problems), problems.isEmpty());
    }

    /** Returns null when the screenshot matches (or was recorded), otherwise a description. */
    private static String compare(Bitmap actual, File file) throws IOException {
        if (recordRequested() || !file.exists()) {
            File dir = file.getParentFile();
            if (!dir.isDirectory() && !dir.mkdirs()) throw new IOException("cannot create " + dir);
            try (FileOutputStream out = new FileOutputStream(file)) {
                assertTrue("PNG encode failed", actual.compress(Bitmap.CompressFormat.PNG, 100, out));
            }
            return null;
        }
        Bitmap expected = BitmapFactory.decodeFile(file.getPath());
        assertNotNull("cannot decode baseline " + file, expected);
        assertEquals("width " + file, expected.getWidth(), actual.getWidth());
        assertEquals("height " + file, expected.getHeight(), actual.getHeight());
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
        if (fraction <= MAX_DIFFERING_FRACTION) return null;
        return "screenshot differs from " + file + " in " + (fraction * 100) + " percent of pixels"
                + " (rerun with -Dscreenshots.record=true or SCREENSHOTS_RECORD=true to update)";
    }
}
