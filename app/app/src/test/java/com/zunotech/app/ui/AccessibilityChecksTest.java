package com.zunotech.app.ui;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckPreset;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResult.AccessibilityCheckResultType;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityHierarchyCheck;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityHierarchyCheckResult;
import com.google.android.apps.common.testing.accessibility.framework.Parameters;
import com.google.android.apps.common.testing.accessibility.framework.uielement.AccessibilityHierarchyAndroid;
import com.google.android.apps.common.testing.accessibility.framework.utils.contrast.BitmapImage;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Task 10.12: Accessibility Test Framework checks on Home, Topics, Saved (loading, empty and error
 * states) and the drawer, in light and night mode. Espresso's AccessibilityChecks.enable() needs an
 * instrumented device, so the same framework checks (preset LATEST: touch target size, speakable
 * text, text and image contrast and the rest) run directly on the laid-out Robolectric view tree,
 * with a drawn screenshot for the contrast checks. Any ERROR result fails the test. WARNING results
 * (ATF's low-confidence contrast estimates) are printed, not asserted.
 *
 * Validates: Requirements 3.2, 4.3
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34, application = android.app.Application.class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public class AccessibilityChecksTest {

    /**
     * Known ATF errors that are reported, not hidden. Key: scenario|check|view resource name.
     * Empty today: ATF reports the known brand-token contrast pairs from ContrastTokensTest only
     * as WARNINGs, never as ERRORs, for these screens.
     */
    private static final Set<String> KNOWN_ERRORS = new HashSet<>();

    private static List<String> check(String scenario, View root) {
        AccessibilityHierarchyAndroid hierarchy = AccessibilityHierarchyAndroid.newBuilder(root)
                .setObtainCharacterLocations(false) // needs an attached window; not used by these checks
                .build();
        Parameters params = new Parameters();
        params.putScreenCapture(new BitmapImage(ScreenStates.draw(root)));
        List<String> errors = new ArrayList<>();
        for (AccessibilityHierarchyCheck c : AccessibilityCheckPreset
                .getAccessibilityHierarchyChecksForPreset(AccessibilityCheckPreset.LATEST)) {
            for (AccessibilityHierarchyCheckResult r : c.runCheckOnHierarchy(hierarchy, null, params)) {
                String name = r.getElement() == null ? "?" : String.valueOf(r.getElement().getResourceName());
                String key = scenario + "|" + c.getClass().getSimpleName() + "|" + name;
                if (r.getType() == AccessibilityCheckResultType.WARNING) {
                    System.out.println("a11y WARNING " + key + " : " + r.getMessage(Locale.ENGLISH));
                }
                if (r.getType() != AccessibilityCheckResultType.ERROR) continue;
                String line = key + " " + r.getElement().getClassName() + " bounds="
                        + r.getElement().getBoundsInScreen() + " : " + r.getMessage(Locale.ENGLISH);
                if (KNOWN_ERRORS.contains(key)) {
                    System.out.println("a11y KNOWN " + line);
                } else {
                    errors.add(line);
                }
            }
        }
        return errors;
    }

    private static void run(boolean night) {
        Context ctx = ScreenStates.context(night);
        String mode = night ? "night" : "light";
        List<String> errors = new ArrayList<>();
        for (ScreenStates.Screen screen : ScreenStates.Screen.values()) {
            for (ScreenStates.State state : ScreenStates.State.values()) {
                errors.addAll(check(screen + "/" + state + "/" + mode, ScreenStates.screen(ctx, screen, state)));
            }
        }
        errors.addAll(check("DRAWER/" + mode, ScreenStates.drawer(ctx)));
        assertTrue("Accessibility errors:\n" + String.join("\n", errors), errors.isEmpty());
    }

    @Test
    public void homeTopicsSavedAndDrawerHaveNoAccessibilityErrors_light() {
        run(false);
    }

    @Test
    public void homeTopicsSavedAndDrawerHaveNoAccessibilityErrors_night() {
        run(true);
    }

    /** Control: the harness really reports an unlabeled, undersized clickable view. */
    @Test
    public void harnessDetectsMissingDescriptionAndSmallTouchTarget() {
        Context ctx = ScreenStates.context(false);
        FrameLayout root = new FrameLayout(ctx);
        ImageButton bad = new ImageButton(ctx);
        bad.setImageResource(android.R.drawable.ic_menu_add);
        root.addView(bad, new FrameLayout.LayoutParams(20, 20));
        root.measure(View.MeasureSpec.makeMeasureSpec(200, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(200, View.MeasureSpec.EXACTLY));
        root.layout(0, 0, 200, 200);
        List<String> errors = check("CONTROL", root);
        assertFalse("expected errors for an unlabeled 20 px button", errors.isEmpty());
        String all = String.join("\n", errors);
        assertTrue(all, all.contains("SpeakableTextPresentCheck"));
        assertTrue(all, all.contains("TouchTargetSizeCheck"));
    }
}
