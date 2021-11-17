package com.shaikds.inventoryapp;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

import static com.shaikds.inventoryapp.InventoryContract.InventoryEntry.COLUMN_IMAGE;
import static com.shaikds.inventoryapp.InventoryContract.InventoryEntry.COLUMN_PRICE;
import static com.shaikds.inventoryapp.InventoryContract.InventoryEntry.COLUMN_QUANTITY;
import static com.shaikds.inventoryapp.InventoryContract.InventoryEntry.COLUMN_TITLE_NAME;
import static com.shaikds.inventoryapp.InventoryContract.InventoryEntry.TABLE_NAME;
import static com.shaikds.inventoryapp.InventoryContract.InventoryEntry._ID;

public class InventoryDbHelper extends SQLiteOpenHelper{
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "inventory.db";
    private static final String TAG = "InventoryDbHelper";


    public InventoryDbHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_ENTRIES = ("CREATE TABLE " + TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_TITLE_NAME + " TEXT NOT NULL, " +
                COLUMN_QUANTITY + " INTEGER DEFAULT 0, " +
                COLUMN_PRICE + " INTEGER NOT NULL, " +
                COLUMN_IMAGE + " BLOB);");
        db.execSQL(SQL_CREATE_ENTRIES);
        Log.d(TAG, "onCreate:" + db.getVersion());

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "onUpgrade:" + db.getVersion());

    }
}
