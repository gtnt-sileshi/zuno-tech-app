package com.zunotech.app.ui;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Task 10.12: scans the layouts and drawables edited by tasks 10.2 to 10.6 for raw hex colors
 * (#RGB, #RRGGBB, #AARRGGBB) outside decorative splash art. Colors there must come from theme
 * attributes or color resources.
 *
 * One documented exemption: vector icons (root element vector) use the pure white mask color
 * #FFFFFF / #FFF, which the bottom bar tints at runtime through app:itemIconTint.
 *
 * Validates: Requirements 3.2, 4.3
 */
public class LayoutTokenScanTest {

    private static final Pattern HEX = Pattern.compile("#[0-9A-Fa-f]{3,8}\\b");
    private static final Pattern WHITE = Pattern.compile("#(?:[Ff]{6}|[Ff]{8}|[Ff]{3})\\b");

    private static final List<String> LAYOUTS = Arrays.asList(
            "fragment_home", "layout_home_cat", "layout_home_blog", "layout_home_ui_banner",
            "layout_home_ui_banner_rv", "layout_home_ui_categories", "item_home_banner",
            "layout_native_ad_admob", "layout_baner_ad", "row_empty", "row_nav_header", "content_main",
            "layout_skeleton_blog", "layout_skeleton_categories", "skeleton_blog_detail",
            "skeleton_item_blog_post", "skeleton_item_category");

    /** Bottom navigation, drawer and skeleton drawables and color selectors. */
    private static final List<String> RESOURCES = Arrays.asList(
            "drawable/bg_bottom_nav", "drawable/bg_nav_header", "drawable/bg_nav_item_active",
            "drawable/bg_nav_item_selector", "drawable/bg_nav_section_divider",
            "drawable/skeleton_bone", "drawable/skeleton_pill",
            "drawable/ic_bnav_home", "drawable/ic_bnav_home_filled", "drawable/ic_bnav_home_outlined",
            "drawable/ic_bnav_topics", "drawable/ic_bnav_topics_filled", "drawable/ic_bnav_topics_outlined",
            "drawable/ic_bnav_saved", "drawable/ic_bnav_saved_filled", "drawable/ic_bnav_saved_outlined",
            "color/bottom_nav_item_color", "color/nav_item_icon_color", "color/nav_item_text_color",
            "menu/bottom_nav_menu", "menu/activity_main_drawer");

    private static String read(File f) throws Exception {
        return new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8)
                .replaceAll("(?s)<!--.*?-->", "");
    }

    @Test
    public void editedLayoutsAndNavigationDrawablesHaveNoRawHexColors() throws Exception {
        List<String> files = new ArrayList<>();
        for (String l : LAYOUTS) files.add("layout/" + l);
        files.addAll(RESOURCES);

        List<String> found = new ArrayList<>();
        int scanned = 0;
        for (String rel : files) {
            File f = new File("src/main/res/" + rel + ".xml");
            if (!f.exists()) {
                // Drawables and color selectors differ slightly between the apps; layouts must exist.
                assertTrue("missing " + f.getPath(), !rel.startsWith("layout/"));
                continue;
            }
            scanned++;
            String xml = read(f);
            boolean vector = xml.contains("<vector");
            Matcher m = HEX.matcher(xml);
            while (m.find()) {
                if (vector && WHITE.matcher(m.group()).matches()) continue; // tinted mask color
                found.add(rel + ".xml: " + m.group());
            }
        }
        assertTrue("scanned only " + scanned + " files", scanned >= LAYOUTS.size());
        assertTrue("Raw hex colors found (use theme attributes or color resources):\n"
                + String.join("\n", found), found.isEmpty());
    }
}
