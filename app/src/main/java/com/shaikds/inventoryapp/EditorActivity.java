package com.shaikds.inventoryapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static com.shaikds.inventoryapp.InventoryContract.InventoryEntry.COLUMN_IMAGE;
import static com.shaikds.inventoryapp.InventoryContract.InventoryEntry.COLUMN_PRICE;
import static com.shaikds.inventoryapp.InventoryContract.InventoryEntry.COLUMN_QUANTITY;
import static com.shaikds.inventoryapp.InventoryContract.InventoryEntry.COLUMN_TITLE_NAME;
import static com.shaikds.inventoryapp.InventoryContract.InventoryEntry.CONTENT_URI;
import static com.shaikds.inventoryapp.InventoryContract.InventoryEntry._ID;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int PICK_IMAGE = 3;
    private static final int EXISTING_ITEM_LOADER = 0;

    private int startNum = 0 ;
    boolean mItemHasChanged = false;
    private Uri currentItemUri;
    EditText etName, etPrice;
    NumberPicker npQuantity;
    ImageView ivImg;
    Button btnDeleteItem, btnContactSupplier;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mItemHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        //Find Views By ID
        etName = findViewById(R.id.edit_item_name);
        etPrice = findViewById(R.id.edit_item_price);
        npQuantity = findViewById(R.id.edit_item_quantity);
        ivImg = findViewById(R.id.edit_photo);
        btnContactSupplier = findViewById(R.id.edit_item_btn_order);
        btnDeleteItem = findViewById(R.id.edit_item_btn_delete);

        SharedPreferences sp = getSharedPreferences("your_prefs", EditorActivity.MODE_PRIVATE);
        startNum = sp.getInt("your_int_key", 3);

        //get the intent uri by get data of the intent sent us to this activity.
        Intent intent = getIntent();
        currentItemUri = intent.getData();
        if (currentItemUri == null) {
            //no uri --> new item.
            setTitle("Add An Item");
            //remove unnecessary buttons that can create bugs.
            btnContactSupplier.setVisibility(View.GONE);
            btnDeleteItem.setVisibility(View.GONE);


        } else {
            //uri not null ---> existing item.
            setTitle("Edit An Item");
            btnContactSupplier.setVisibility(View.VISIBLE);
            btnDeleteItem.setVisibility(View.VISIBLE);

            getSupportLoaderManager().initLoader(EXISTING_ITEM_LOADER, null, this);

        }

        npQuantity.setMinValue(0);
        npQuantity.setMaxValue(9999);
        npQuantity.setOnValueChangedListener((picker, oldVal, newVal) -> {
        });

        ivImg.setOnClickListener(v -> {
            Intent intent1 = new Intent();
            intent1.setType("image/*");
            intent1.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent1, "Select Picture"), PICK_IMAGE);
        });
        btnDeleteItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditorActivity.this.showDeleteConfirmationDialog();
            }
        });
        btnContactSupplier.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        etName.setOnTouchListener(mTouchListener);
        etPrice.setOnTouchListener(mTouchListener);
        ivImg.setOnTouchListener(mTouchListener);
        npQuantity.setOnTouchListener(mTouchListener);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.editor_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                //if everything is ok with validation then continue saving.
                if (fieldValidation()) {
                    saveItem();
                }
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    //save Item to local database.
    private void saveItem() {
        // Im not checking the fields because I already did it when clicking save button.
        //It wont save the item if the fields didnt pass the validation.
        String name = etName.getText().toString().trim();
        int price = Integer.parseInt(String.valueOf(etPrice.getText()).trim());
        int quantity = npQuantity.getValue();
        if (ivImg.getDrawable() == null) {
            ivImg.setImageResource(R.drawable.logo);
        }
        Bitmap photo = ((BitmapDrawable) ivImg.getDrawable()).getBitmap();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        photo.compress(Bitmap.CompressFormat.PNG, 100, bos);
        byte[] bArray = bos.toByteArray();

        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE_NAME, name);
        values.put(COLUMN_QUANTITY, quantity);
        values.put(COLUMN_PRICE, price);
        values.put(COLUMN_IMAGE, bArray);

        if (currentItemUri == null) {
            //new item
            Uri insertUri = getContentResolver().insert(CONTENT_URI, values);
            if (insertUri == null) {
                Toast.makeText(this, "Error saving item", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Item saved successfully", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, InventoryActivity.class);
                startActivity(intent);
                finish();
            }

        } else {
            //existing item
            int updatedRows = getContentResolver().update(currentItemUri, values, null, null);
            if (updatedRows == 0) {
                Toast.makeText(this, "Update Failed", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Update Success", Toast.LENGTH_SHORT).show();
                finish();
            }
        }


        //finish the activity after saving the pet.
        finish();
    }

    //delete specific item when it's not new then finish activity.
    private void deleteItem(Uri itemUri) {
        int delete = getContentResolver().delete(itemUri, null, null);
        if (delete != 0) {
            Toast.makeText(this, "Item Deleted", Toast.LENGTH_SHORT).show();
            addOneToShared();
            finish();
        } else {
            Toast.makeText(this, "Error with deleting item.", Toast.LENGTH_SHORT).show();
            finish();
        }//finish the activity.
    }

    //before saving an item check all fields.
    private boolean fieldValidation() {
        if (etName.getText().toString().equals(null) || etName.getText().toString().equals("")) {
            etName.setError("Name cant be null");
            return false;
        }
        if (etPrice.getText().toString().equals(null) || etPrice.getText().toString().equals("")) {
            etPrice.setError("Price cant be null");
            return false;
        }
        return true;
    }

    //save Image and put it into the image view.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {
            ivImg.setImageBitmap(null);
            Uri imageUri = data.getData();
            ivImg.setImageURI(imageUri);
        }
    }

    //alert dialog for deleting pet.
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Delete this item?");
        builder.setPositiveButton("Yes", (dialog, id) -> {
            // User clicked the "Delete" button, so delete the pet.
            deleteItem(currentItemUri);
        });
        builder.setNegativeButton("Cancel", (dialog, id) -> {
            // User clicked the "Cancel" button, so dismiss the dialog
            // and continue editing the pet.
            if (dialog != null) {
                dialog.dismiss();
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    //alert dialog when leaving the activity.
    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardBtnClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Do You Wish To Leave?");
        builder.setNegativeButton("I want to stay", (dialog, which) -> {
            if (dialog != null) {
                dialog.dismiss();
            }
        });
        builder.setPositiveButton("Yes", discardBtnClickListener);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        if (!mItemHasChanged) {
            //item isn't changed
            super.onBackPressed();
        } else {
            DialogInterface.OnClickListener discardButtonClickListener =
                    (dialogInterface, i) -> {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    };
            showUnsavedChangesDialog(discardButtonClickListener);
        }
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        String[] projection = {
                _ID,
                COLUMN_TITLE_NAME,
                COLUMN_QUANTITY,
                COLUMN_PRICE,
                COLUMN_IMAGE
        };

        return new CursorLoader(this,
                currentItemUri,
                projection,
                null,
                null,
                null);

    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }
        //we use cursor move to first because we have only 1 item in our cursor.
        if (cursor.moveToFirst()) {
            //get the indexes of columns we need
            int nameColumnIndex = cursor.getColumnIndex(COLUMN_TITLE_NAME);
            int quantityColumnIndex = cursor.getColumnIndex(COLUMN_QUANTITY);
            int priceColumnIndex = cursor.getColumnIndex(COLUMN_PRICE);
            int imgColumnIndex = cursor.getColumnIndex(COLUMN_IMAGE);

            //get the values
            String name = cursor.getString(nameColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            String price = String.valueOf(cursor.getInt(priceColumnIndex));
            byte[] img = cursor.getBlob(imgColumnIndex);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(img);
            Bitmap imgBitmap = BitmapFactory.decodeStream(inputStream);

            //set all the values in the right UI fields.
            etName.setText(name);
            npQuantity.setValue(quantity);
            etPrice.setText(price);
            ivImg.setImageBitmap(imgBitmap);
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        etName.setText("");
        etPrice.setText("");
        ivImg.setImageResource(R.drawable.ic_launcher_background);
        npQuantity.setValue(0);
    }

    public void addOneToShared(){
        SharedPreferences sp = getSharedPreferences("id", EditorActivity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("id",startNum+=1);
        editor.commit();
    }
}
