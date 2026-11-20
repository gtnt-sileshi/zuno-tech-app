package com.zunotech.app.utils.advertising;

import static org.junit.Assert.assertEquals;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;

import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Feature: remove-adnetworks-and-live-content
 * Property 1: Android ad configuration is AdMob-only
 *
 * <b>Validates: Requirements 1.5, 1.6</b>
 */
public class AdConfigPropertyTest {

    private static final String[] ADMOB_KEYS = {
            "admob_publisher_id", "admob_banner_unit_id", "admob_interstitial_unit_id",
            "admob_native_unit_id", "admob_app_open_ad_unit_id", "admob_reward_ad_unit_id"};

    private static final String[] DECOY_KEYS = {
            "applovin_banner_id", "applovin_interstitial_id", "unity_game_id", "unity_banner_id",
            "startapp_app_id", "yandex_banner_id", "ironsource_app_key", "mintegral_app_id",
            "pangle_banner_id", "banner_id", "interstitial_id", "native_id"};

    /** Per key: absent, JSON null, or an arbitrary string. */
    private Arbitrary<String> valueOrAbsent() {
        Arbitrary<String> text = Arbitraries.strings().ofMaxLength(20);
        return Arbitraries.oneOf(
                Arbitraries.just((String) null),
                Arbitraries.just("\u0000NULL"),
                text);
    }

    @Provide
    Arbitrary<Map<String, String>> admobFields() {
        Arbitrary<String>[] arbs = new Arbitrary[ADMOB_KEYS.length];
        for (int i = 0; i < arbs.length; i++) {
            arbs[i] = valueOrAbsent();
        }
        return Combinators.combine(java.util.Arrays.asList(arbs)).as(list -> {
            Map<String, String> m = new LinkedHashMap<>();
            for (int i = 0; i < ADMOB_KEYS.length; i++) {
                m.put(ADMOB_KEYS[i], list.get(i));
            }
            return m;
        });
    }

    @Provide
    Arbitrary<String> networks() {
        return Arbitraries.oneOf(
                Arbitraries.of("admob", "applovin", "unity", "startapp", "yandex", "", "ADMOB"),
                Arbitraries.strings().ofMaxLength(15));
    }

    @Provide
    Arbitrary<Map<String, String>> decoyFields() {
        return Arbitraries.strings().ofMinLength(1).ofMaxLength(12)
                .list().ofSize(DECOY_KEYS.length).map(list -> {
                    Map<String, String> m = new LinkedHashMap<>();
                    for (int i = 0; i < DECOY_KEYS.length; i++) {
                        m.put(DECOY_KEYS[i], list.get(i));
                    }
                    return m;
                });
    }

    private static void put(JSONObject json, String key, String value) throws Exception {
        if (value == null) {
            return;
        }
        json.put(key, "\u0000NULL".equals(value) ? JSONObject.NULL : value);
    }

    @Property(tries = 100)
    void adConfigReadsOnlyAdMobKeys(
            @ForAll("admobFields") Map<String, String> admob,
            @ForAll("decoyFields") Map<String, String> decoys,
            @ForAll("networks") String adNetwork,
            @ForAll boolean includeNetwork,
            @ForAll boolean includeDecoys,
            @ForAll("adStatus") String adStatus) throws Exception {
        JSONObject json = new JSONObject();
        if (includeNetwork) {
            json.put("ad_network", adNetwork);
        }
        if (adStatus != null) {
            json.put("ad_status", adStatus);
        }
        if (includeDecoys) {
            for (Map.Entry<String, String> e : decoys.entrySet()) {
                json.put(e.getKey(), e.getValue());
            }
        }
        for (Map.Entry<String, String> e : admob.entrySet()) {
            put(json, e.getKey(), e.getValue());
        }

        AdConfig config = AdConfig.fromJson(json);

        assertEquals(expected(admob, "admob_publisher_id"), config.publisherId());
        assertEquals(expected(admob, "admob_banner_unit_id"), config.id(AdFormat.BANNER));
        assertEquals(expected(admob, "admob_interstitial_unit_id"), config.id(AdFormat.INTERSTITIAL));
        assertEquals(expected(admob, "admob_native_unit_id"), config.id(AdFormat.NATIVE));
        assertEquals(expected(admob, "admob_app_open_ad_unit_id"), config.id(AdFormat.APP_OPEN));
        assertEquals(expected(admob, "admob_reward_ad_unit_id"), config.id(AdFormat.REWARD));
    }

    @Provide
    Arbitrary<String> adStatus() {
        return Arbitraries.of(null, "true", "false", "TRUE", "1", "");
    }

    private static String expected(Map<String, String> admob, String key) {
        String v = admob.get(key);
        return (v == null || "\u0000NULL".equals(v)) ? "" : v;
    }

    /**
     * Feature: remove-adnetworks-and-live-content
     * Property 2: Ad requests are allowed only for enabled formats with an AdMob ID
     *
     * <b>Validates: Requirements 1.4, 1.6, 1.7</b>
     */
    @Property(tries = 100)
    void shouldRequestOnlyWhenEnabledAndIdNonBlank(
            @ForAll("admobFields") Map<String, String> admob,
            @ForAll("blankOrIdFields") Map<String, String> ids,
            @ForAll boolean useBlankStrategy,
            @ForAll("adStatus") String adStatus) throws Exception {
        Map<String, String> fields = useBlankStrategy ? ids : admob;
        JSONObject json = new JSONObject();
        if (adStatus != null) {
            json.put("ad_status", adStatus);
        }
        for (Map.Entry<String, String> e : fields.entrySet()) {
            put(json, e.getKey(), e.getValue());
        }

        AdConfig config = AdConfig.fromJson(json);

        boolean enabled = adStatus != null && "true".equalsIgnoreCase(adStatus.trim());
        String[] keys = {"admob_banner_unit_id", "admob_interstitial_unit_id", "admob_native_unit_id",
                "admob_app_open_ad_unit_id", "admob_reward_ad_unit_id"};
        AdFormat[] formats = {AdFormat.BANNER, AdFormat.INTERSTITIAL, AdFormat.NATIVE,
                AdFormat.APP_OPEN, AdFormat.REWARD};
        for (int i = 0; i < formats.length; i++) {
            boolean hasId = !expected(fields, keys[i]).trim().isEmpty();
            assertEquals(formats[i].name(), enabled && hasId, config.shouldRequest(formats[i]));
        }
    }

    /** Ad unit IDs biased toward absent, null, empty, whitespace-only and real-looking values. */
    @Provide
    Arbitrary<Map<String, String>> blankOrIdFields() {
        Arbitrary<String> value = Arbitraries.oneOf(
                Arbitraries.just((String) null),
                Arbitraries.just("\u0000NULL"),
                Arbitraries.of("", " ", "   ", "\t", "\n", " \t\n "),
                Arbitraries.strings().alpha().numeric().ofMinLength(1).ofMaxLength(20)
                        .map(v -> "ca-app-pub-" + v));
        return value.list().ofSize(ADMOB_KEYS.length).map(list -> {
            Map<String, String> m = new LinkedHashMap<>();
            for (int i = 0; i < ADMOB_KEYS.length; i++) {
                m.put(ADMOB_KEYS[i], list.get(i));
            }
            return m;
        });
    }
}
