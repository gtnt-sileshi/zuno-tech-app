package com.zunotech.app.utils.helper;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.zunotech.app.BuildConfig;
import com.zunotech.app.R;
import com.zunotech.app.callback.Callback;
import com.zunotech.app.item.ItemAbout;
import com.zunotech.app.utils.EncryptData;

public class DBHelper extends SQLiteOpenHelper {

    private static final String TAG = "DBHelper";
    private static final int DATABASE_VERSION = 5;
    private static final String DATABASE_NAME = BuildConfig.APPLICATION_ID + "_" + "app.db";
    /** Null when the local database could not be opened or upgraded. */
    private final SQLiteDatabase db;
    EncryptData encryptData;
    final Context context;

    private static final String TABLE_ABOUT  = "about";
    /** Legacy recent-items table, dropped when upgrading to version 5. */
    private static final String LEGACY_TABLE_RECENT = "recent";

    private static final String TAG_ABOUT_EMAIL     = "email";
    private static final String TAG_ABOUT_AUTHOR    = "author";
    private static final String TAG_ABOUT_CONTACT   = "contact";
    private static final String TAG_ABOUT_WEBSITE   = "website";
    private static final String TAG_ABOUT_DESC      = "description";
    private static final String TAG_ABOUT_DEVELOPED = "developed";
    private static final String TAG_ABOUT_MORE_APP  = "more_apps";

    private final String[] columnsAbout = {
            TAG_ABOUT_EMAIL, TAG_ABOUT_AUTHOR, TAG_ABOUT_CONTACT, TAG_ABOUT_WEBSITE,
            TAG_ABOUT_DESC, TAG_ABOUT_DEVELOPED, TAG_ABOUT_MORE_APP
    };

    private static final String CREATE_TABLE_ABOUT = "CREATE TABLE IF NOT EXISTS " + TABLE_ABOUT + " ("
            + TAG_ABOUT_EMAIL     + " TEXT,"
            + TAG_ABOUT_AUTHOR    + " TEXT,"
            + TAG_ABOUT_CONTACT   + " TEXT,"
            + TAG_ABOUT_WEBSITE   + " TEXT,"
            + TAG_ABOUT_DESC      + " TEXT,"
            + TAG_ABOUT_DEVELOPED + " TEXT,"
            + TAG_ABOUT_MORE_APP  + " TEXT"
            + ")";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        encryptData = new EncryptData(context);
        this.context = context;
        SQLiteDatabase opened = null;
        try {
            // SQLiteOpenHelper opens lazily, so open here for the catch to cover create/upgrade.
            opened = getWritableDatabase();
        } catch (SQLiteException e) {
            Log.e(TAG, "Local database could not be opened or upgraded", e);
            new Handler(Looper.getMainLooper()).post(() ->
                    Toast.makeText(this.context, R.string.local_data_upgrade_failed, Toast.LENGTH_LONG).show());
        }
        db = opened;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_ABOUT);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.beginTransaction();
        try {
            if (oldVersion < 5) {
                db.execSQL("DROP TABLE IF EXISTS " + LEGACY_TABLE_RECENT);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    // About ---------------------------------------------------------------------------------------
    public void addToAbout() {
        if (db == null) return;
        try {
            db.delete(TABLE_ABOUT, null, null);
            ContentValues contentValues = new ContentValues();
            contentValues.put(TAG_ABOUT_EMAIL,     Callback.getItemAbout().getEmail());
            contentValues.put(TAG_ABOUT_AUTHOR,    Callback.getItemAbout().getAuthor());
            contentValues.put(TAG_ABOUT_CONTACT,   Callback.getItemAbout().getContact());
            contentValues.put(TAG_ABOUT_WEBSITE,   Callback.getItemAbout().getWebsite());
            contentValues.put(TAG_ABOUT_DESC,      Callback.getItemAbout().getAppDesc());
            contentValues.put(TAG_ABOUT_DEVELOPED, Callback.getItemAbout().getDevelopedBY());
            db.insert(TABLE_ABOUT, null, contentValues);
        } catch (Exception e) {
            Log.e(TAG, "Error add to about", e);
        }
    }

    @SuppressLint("Range")
    public Boolean getAbout() {
        if (db == null) return false;
        try (Cursor c = db.query(TABLE_ABOUT, columnsAbout, null, null, null, null, null)) {
            if (c != null && c.moveToFirst()) {
                String email     = c.getString(c.getColumnIndex(TAG_ABOUT_EMAIL));
                String author    = c.getString(c.getColumnIndex(TAG_ABOUT_AUTHOR));
                String contact   = c.getString(c.getColumnIndex(TAG_ABOUT_CONTACT));
                String website   = c.getString(c.getColumnIndex(TAG_ABOUT_WEBSITE));
                String desc      = c.getString(c.getColumnIndex(TAG_ABOUT_DESC));
                String developed = c.getString(c.getColumnIndex(TAG_ABOUT_DEVELOPED));
                String moreApps  = c.getString(c.getColumnIndex(TAG_ABOUT_MORE_APP));
                Callback.setItemAbout(new ItemAbout(email, author, contact, website, desc, developed, moreApps));
                return true;
            }
        }
        return false;
    }

    @Override
    public synchronized void close() {
        if (db != null && db.isOpen()) {
            db.close();
            super.close();
        }
    }
}
