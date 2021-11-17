package com.shaikds.inventoryapp;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.shaikds.inventoryapp.InventoryContract.InventoryEntry;

import static com.shaikds.inventoryapp.InventoryContract.InventoryEntry.*;

public class InventoryProvider extends ContentProvider {
    private static UriMatcher mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private static final int INVENTORY = 100;
    private static final int INVENTORY_ITEM = 101;
    private static final String LOG_TAG = "InventoryProvider";
    public static InventoryDbHelper dbHelper;

    // Uri that are included in uri matcher. URI DICTIONARY
    static {
        mUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY,
                InventoryContract.PATH_INVENTORY
                , INVENTORY);
        mUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY,
                InventoryContract.PATH_INVENTORY + "/#" +
                        "", INVENTORY_ITEM);

    }


    @Override
    public boolean onCreate() {
        dbHelper = new InventoryDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase database = dbHelper.getReadableDatabase();

        SQLiteDatabase mDb = dbHelper.getReadableDatabase();
        Cursor cursor;
        int match = mUriMatcher.match(uri);


        switch (match) {
            case INVENTORY:
                cursor = mDb.query(TABLE_NAME,
                        projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case INVENTORY_ITEM:
                selection = _ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                cursor = database.query(TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);

                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);

        }
        //cursor.notify
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final int match = mUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                return insertPet(uri, values);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        final int match = mUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                getContext().getContentResolver().notifyChange(uri, null);
                return db.delete(TABLE_NAME, selection, selectionArgs);
            case INVENTORY_ITEM:
                selection = _ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                int rowsDeleted = db.delete(TABLE_NAME, selection, selectionArgs);
                if (rowsDeleted != 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return rowsDeleted;
            default:
                throw new IllegalArgumentException("Delete is not supports for " + uri);

        }
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        final int match = mUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                return updatePet(uri, values, selection, selectionArgs);
            case INVENTORY_ITEM:
                // SELECT the ID of the specific item in DB.
                selection = _ID + "=?";
                // ID of the row
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                return updatePet(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("update is not supported for:" + uri.toString());
        }
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = mUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                return CONTENT_LIST_TYPE;
            case INVENTORY_ITEM:
                return CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    /**
     * ---- METHODS ---- /
     **/

    // checks for the insertPet method.
    private boolean isOkay(ContentValues values) {
        boolean isOk = true;
        String name = values.getAsString(COLUMN_TITLE_NAME);
        Integer price = values.getAsInteger(COLUMN_PRICE);
        Integer quantity = values.getAsInteger(COLUMN_QUANTITY);

        if (name == null) {
            isOk = false;
            throw new IllegalArgumentException("Pet requires a name");
        }
        if (price <= 0) {
            isOk = false;
            throw new IllegalArgumentException("Pet requires a valid price");
        }
        if (quantity < 0) {
            isOk = false;
            throw new IllegalArgumentException("Pet requires a valid quantity");

        }
        return isOk;

    }

    // Inserting the pet and return the URI of the new inventory item.(Includes data validation)
    private Uri insertPet(Uri uri, ContentValues values) {
        //data validation checks. if something not good, there will be an exception thrown.
        isOkay(values);
        //if everything went good so keep saving.
        SQLiteDatabase mDb = dbHelper.getWritableDatabase();
        //Insert values to db , if id of the row is ==-1 --> failed to insert the row.
        long id = mDb.insert(TABLE_NAME, null, values);
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;

        }

        getContext().getContentResolver().notifyChange(uri, null);
        //return specific inventory item uri.
        return ContentUris.withAppendedId(uri, id);

    }

    // Updating the item and return the number of rows that have changed.(Includes data validation)
    private int updatePet(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (values.containsKey(COLUMN_TITLE_NAME)) {
            final String name = values.getAsString(COLUMN_TITLE_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Inventory REQUIRES a name.");
            }
        }
        if (values.containsKey(COLUMN_QUANTITY)) {
            final int quantity = values.getAsInteger(COLUMN_QUANTITY);
            if (quantity < 0) {
                throw new IllegalArgumentException("Quantity cant be a negative number");
            }
        }
        if (values.containsKey(COLUMN_PRICE)) {
            final int price = values.getAsInteger(COLUMN_PRICE);
            if (price <= 0) {
                throw new IllegalArgumentException("Price cant be 0 or a negative number");
            }
        }
        if (values.size() <= 0) {             // If there are no values to update, then don't try to update the database
            return 0;
        }//Every thing is fine so
        selection = InventoryEntry._ID + "=?";
        selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
        // Get db helper writable.
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        // Return number of rows that have changed.
        int rowsUpdated = db.update(TABLE_NAME, values, selection, selectionArgs);
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);

        }
        return rowsUpdated;
    }
}