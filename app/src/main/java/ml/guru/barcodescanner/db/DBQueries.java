package ml.guru.barcodescanner.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;

import ml.guru.barcodescanner.model.Items;

public class DBQueries{
    private Context context;
    private SQLiteDatabase database;
    private DBHelper dbHelper;

    public DBQueries(Context context) {
        this.context = context;
    }

    public DBQueries open() throws SQLException {
        dbHelper = new DBHelper(context);
        database = dbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        dbHelper.close();
    }

    public boolean insertItem(Items item) {
        ContentValues values = new ContentValues();
        values.put(DBConstants.ITEM_ID, item.getItemId());
        values.put(DBConstants.ITEM_PRESENT, item.getPresent());
        values.put(DBConstants.ITEM_COUNT, item.getCount());
        return database.insert(DBConstants.STOCK_TABLE, null, values) > -1;
    }

    public boolean findItem(String itemid){
        database = dbHelper.getReadableDatabase();
        return database.query(DBConstants.STOCK_TABLE, new String[]{DBConstants.ITEM_ID},DBConstants.ITEM_ID + "= ?", new String[]{itemid},null,null,null).getCount() > 0;
    }

    public boolean checkItem(String itemid){
        database = dbHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBConstants.ITEM_PRESENT,1);
        return database.update(DBConstants.STOCK_TABLE, contentValues, DBConstants.ITEM_ID + "= ?", new String[]{itemid}) > 0;
    }

    public ArrayList<Items> readItems() {
        ArrayList<Items> list = new ArrayList<>();
        try {
            database = dbHelper.getReadableDatabase();
            Cursor cursor = database.rawQuery(DBConstants.SELECT_QUERY, null);
            list.clear();
            Log.v("DBQ", String.valueOf(cursor.getCount()));
            Log.v("DBQ", String.valueOf(cursor.getColumnCount()));
            if (cursor.getCount() > 0) {
                if (cursor.moveToFirst()) {
                    do {
                        String itemId = cursor.getString(cursor.getColumnIndex(DBConstants.ITEM_ID));
                        Boolean present = cursor.getInt(cursor.getColumnIndex(DBConstants.ITEM_PRESENT)) > 0;
                        Integer count = cursor.getInt(cursor.getColumnIndex(DBConstants.ITEM_COUNT));
                        Items item = new Items(itemId, present, count);
                        list.add(item);
                    } while (cursor.moveToNext());
                }
            }
            cursor.close();
        } catch (Exception e) {
            Log.v("Exception", e.getMessage());
        }
        return list;
    }
}
