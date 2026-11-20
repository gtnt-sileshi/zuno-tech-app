package com.zunotech.app.utils.helper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Looper;

import androidx.test.core.app.ApplicationProvider;

import com.zunotech.app.BuildConfig;
import com.zunotech.app.R;
import com.zunotech.app.callback.Callback;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowToast;

import java.io.File;

/**
 * Verifies the version 4 to 5 DBHelper upgrade (Requirements 3.5, 3.6, 3.7).
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34, application = android.app.Application.class)
public class DBHelperUpgradeTest {

    private static final String DB_NAME = BuildConfig.APPLICATION_ID + "_app.db";
    private static final String[] ABOUT_ROW = {
            "a@b.c", "author", "123", "https://example.com", "desc", "dev", "more"};

    private Context context;
    private File dbFile;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        dbFile = context.getDatabasePath(DB_NAME);
        deleteDb();
        assertNotNull(dbFile.getParentFile());
        //noinspection ResultOfMethodCallIgnored
        dbFile.getParentFile().mkdirs();
    }

    @After
    public void tearDown() {
        deleteDb();
    }

    private void deleteDb() {
        context.deleteDatabase(DB_NAME);
    }

    private SQLiteDatabase openRaw() {
        return SQLiteDatabase.openOrCreateDatabase(dbFile, null);
    }

    private void createAboutV4(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE about (email TEXT,author TEXT,contact TEXT,website TEXT,"
                + "description TEXT,developed TEXT,more_apps TEXT)");
        ContentValues cv = new ContentValues();
        String[] cols = {"email", "author", "contact", "website", "description", "developed", "more_apps"};
        for (int i = 0; i < cols.length; i++) cv.put(cols[i], ABOUT_ROW[i]);
        db.insert("about", null, cv);
    }

    /** Version 4 layout as shipped: about plus the legacy Live TV recent table. */
    private void createV4Database() {
        SQLiteDatabase db = openRaw();
        createAboutV4(db);
        db.execSQL("CREATE TABLE recent (id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "live_id TEXT,live_title TEXT,image TEXT)");
        for (int i = 1; i <= 3; i++) {
            ContentValues cv = new ContentValues();
            cv.put("live_id", String.valueOf(i));
            cv.put("live_title", "Channel " + i);
            cv.put("image", "img" + i);
            db.insert("recent", null, cv);
        }
        db.setVersion(4);
        db.close();
    }

    private boolean tableExists(SQLiteDatabase db, String name) {
        try (Cursor c = db.rawQuery(
                "SELECT 1 FROM sqlite_master WHERE name=?", new String[]{name})) {
            return c.getCount() > 0;
        }
    }

    private void assertAboutRowUnchanged(SQLiteDatabase db) {
        try (Cursor c = db.rawQuery("SELECT email,author,contact,website,description,developed,"
                + "more_apps FROM about", null)) {
            assertEquals(1, c.getCount());
            assertTrue(c.moveToFirst());
            for (int i = 0; i < ABOUT_ROW.length; i++) {
                assertEquals(ABOUT_ROW[i], c.getString(i));
            }
        }
    }

    @Test
    public void upgradeFromV4KeepsAboutDropsRecentWithinFiveSeconds() {
        createV4Database();

        long start = System.nanoTime();
        DBHelper helper = new DBHelper(context);
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;
        helper.close();

        assertTrue("Upgrade took " + elapsedMs + " ms", elapsedMs < 5000);

        SQLiteDatabase db = openRaw();
        try {
            assertEquals(5, db.getVersion());
            assertTrue(tableExists(db, "about"));
            assertFalse(tableExists(db, "recent"));
            assertAboutRowUnchanged(db);
        } finally {
            db.close();
        }
    }

    @Test
    public void upgradedHelperStillReadsAbout() {
        createV4Database();
        DBHelper helper = new DBHelper(context);
        try {
            assertTrue(helper.getAbout());
            assertEquals("author", Callback.getItemAbout().getAuthor());
        } finally {
            helper.close();
        }
    }

    @Test
    public void failedUpgradeRollsBackShowsToastAndKeepsExistingData() {
        // A view named "recent" makes "DROP TABLE IF EXISTS recent" fail inside onUpgrade.
        SQLiteDatabase raw = openRaw();
        createAboutV4(raw);
        raw.execSQL("CREATE TABLE legacy_src (id INTEGER, live_id TEXT)");
        raw.execSQL("INSERT INTO legacy_src VALUES (1, 'x')");
        raw.execSQL("CREATE VIEW recent AS SELECT id, live_id FROM legacy_src");
        raw.setVersion(4);
        raw.close();

        DBHelper helper = null;
        try {
            helper = new DBHelper(context); // must not throw
            shadowOf(Looper.getMainLooper()).idle();

            assertEquals(context.getString(R.string.local_data_upgrade_failed),
                    ShadowToast.getTextOfLatestToast());

            // Helper degrades to empty about-cache results and does not throw.
            helper.addToAbout();
            assertFalse(helper.getAbout());
        } finally {
            if (helper != null) helper.close();
        }

        SQLiteDatabase db = openRaw();
        try {
            assertEquals("transaction must have rolled back the version bump", 4, db.getVersion());
            assertTrue(tableExists(db, "recent"));
            assertAboutRowUnchanged(db);
            try (Cursor c = db.rawQuery("SELECT COUNT(*) FROM legacy_src", null)) {
                assertTrue(c.moveToFirst());
                assertEquals(1, c.getInt(0));
            }
        } finally {
            db.close();
        }
    }
}
