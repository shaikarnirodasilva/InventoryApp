package com.shaikds.inventoryapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.shaikds.inventoryapp.InventoryContract.InventoryEntry;

import java.io.ByteArrayOutputStream;

public class InventoryActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    ItemCursorAdapter mCursorAdapter;
    InventoryDbHelper mInventoryHelper;
    private static final String TAG = "InventoryActivity";
    private ListView lv;
    private static int ITEM_LOADER = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);
        mInventoryHelper = new InventoryDbHelper(this);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            Toast.makeText(this, "Add A New Item", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(InventoryActivity.this, EditorActivity.class);
            startActivity(intent);
            finish();
        });
        lv = findViewById(R.id.catalog_lv);

        //if listview is empty show the empty view.
        View emptyView = findViewById(R.id.empty_view);
        lv.setEmptyView(emptyView);

        //set the adapter
        mCursorAdapter = new ItemCursorAdapter(this, null);
        lv.setAdapter(mCursorAdapter);

        //kick off the loader
        LoaderManager.getInstance(this).initLoader(ITEM_LOADER, null, this);
        //lv item click listener
        lv.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(InventoryActivity.this, EditorActivity.class);
            Uri currentItemUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, id);
            intent.setData(currentItemUri);

            startActivity(intent);

        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.inventory_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertFakeItem();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                //TODO: deleteAllItems();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void insertFakeItem() {
        Drawable d= getDrawable(R.drawable.logo); // the drawable (Captain Obvious, to the rescue!!!)
        Bitmap bitmap = ((BitmapDrawable)d).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] bitmapdata = stream.toByteArray();
        // Create a ContentValues object where column names are the keys,
        // and Toto's pet attributes are the values.
        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_TITLE_NAME, "Terrier");
        values.put(InventoryEntry.COLUMN_QUANTITY, 7);
        values.put(InventoryEntry.COLUMN_PRICE, 300);
        values.put(InventoryEntry.COLUMN_IMAGE, bitmapdata);


        //newUri = specific new item uri in DB(with id).
        Uri newUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {

        String[] projection = {InventoryEntry._ID,
                InventoryEntry.COLUMN_TITLE_NAME,
                InventoryEntry.COLUMN_QUANTITY,
                InventoryEntry.COLUMN_PRICE,
                InventoryEntry.COLUMN_IMAGE,
        };

        return new CursorLoader(this,
                InventoryEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor newCursor) {
        mCursorAdapter.swapCursor(newCursor);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }
}