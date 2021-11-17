package com.shaikds.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayInputStream;

import static com.shaikds.inventoryapp.InventoryContract.*;
import static com.shaikds.inventoryapp.InventoryContract.InventoryEntry.*;


public class ItemCursorAdapter extends CursorAdapter {
    TextView tvQuantity;

    public ItemCursorAdapter(Context context, Cursor c) {
        super(context, c);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        //return the inflated item layout for each item in cursor
        return LayoutInflater.from(context).inflate(R.layout.item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        //find the views in item.xml
        TextView tvName = view.findViewById(R.id.item_name);
        tvQuantity = view.findViewById(R.id.item_quantity);
        TextView tvPrice = view.findViewById(R.id.item_price);
        ImageView ivImg = view.findViewById(R.id.item_img);
        Button btnSale = view.findViewById(R.id.item_btn_sale);

        int pos = cursor.getPosition();
        tvQuantity.setTag(pos);
        btnSale.setTag(pos);
        //get the data from cursor and save it to variables
        String updatedName = cursor.getString(cursor.getColumnIndex(COLUMN_TITLE_NAME));
        int updatedQuantity = cursor.getInt(cursor.getColumnIndex(COLUMN_QUANTITY));
        String updatedQuantityString = String.valueOf(updatedQuantity);
        int updatedPrice = cursor.getInt(cursor.getColumnIndex(COLUMN_PRICE));
        String updatedPriceString = String.valueOf(updatedPrice);
        byte[] updatedImg = cursor.getBlob(cursor.getColumnIndex(COLUMN_IMAGE));
        ByteArrayInputStream inputStream = new ByteArrayInputStream(updatedImg);
        Bitmap imgBitmap = BitmapFactory.decodeStream(inputStream);

        //set the updated data and img in the right views.
        tvName.setText("Name : " + updatedName);
        tvQuantity.setText("Quantity : " + updatedQuantityString);
        tvPrice.setText("Price : " + updatedPriceString + " NS .");
        ivImg.setImageBitmap(imgBitmap);
        btnSale.setOnClickListener(v -> {
            Integer position = (Integer) v.getTag();
            cursor.moveToPosition(position);
            int quantityIndex = cursor.getColumnIndex(COLUMN_QUANTITY);
            int nowQuantity = cursor.getInt(quantityIndex);
            int idIndex = cursor.getColumnIndex(_ID);
            int nowId = cursor.getInt(idIndex);
            decreaseQuantity(v, nowQuantity, nowId);
        });
    }

    /**
     * METHODS
     **/
    private void decreaseQuantity(View v, int quantity, int columnID) {
        if (quantity > 0) {
            quantity -= 1;
            ContentValues values = new ContentValues();
            values.put(COLUMN_QUANTITY, quantity);
            Uri uri = ContentUris.withAppendedId(CONTENT_URI, columnID);
            int update = v.getContext().getContentResolver().update(uri, values, null, null);
            notifyDataSetChanged();
        } else {
            //get the right id of button clicked and set error in it.
            Object newTvQuantity =  v.getTag();
            View viewWithTag = v.findViewWithTag(newTvQuantity);
            TextView newQuantity = (TextView) v.findViewById(viewWithTag.getId());
            newQuantity.setError("Quantity is not negative.");
        }
    }
}
