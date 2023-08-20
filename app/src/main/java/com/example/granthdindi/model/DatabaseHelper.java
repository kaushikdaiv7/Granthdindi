package com.example.granthdindi.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "database_cart";
    public static final String TABLE_NAME = "table_cart";

    public DatabaseHelper(Context context){
        super(context,DATABASE_NAME,null,1);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = " CREATE TABLE " + TABLE_NAME +"(englishName TEXT PRIMARY KEY, marathiName TEXT, " +
                "imgUrl TEXT, stocks INTEGER, price INTEGER, quantity INTEGER)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+ TABLE_NAME);
        onCreate(db);
    }

    public boolean insertData(String englishName, String marathiName, String imgUrl, int stocks, int price, int quantity){
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("englishName",englishName);
        contentValues.put("marathiName",marathiName);
        contentValues.put("imgUrl",imgUrl);
        contentValues.put("stocks",stocks);
        contentValues.put("price",price);
        contentValues.put("quantity",quantity);
        database.insert(TABLE_NAME,null,contentValues);
        return true;
    }

    public ArrayList getList(){
        SQLiteDatabase database = this.getReadableDatabase();
        ArrayList<Book> cart_list = new ArrayList<>();
        //create cursor to select all values
        Cursor cursor = database.rawQuery("SELECT * FROM "+ TABLE_NAME,null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()){
            Book book = new Book();
            book.setEnglishName(cursor.getString(cursor.getColumnIndex("englishName")));
            book.setMarathiName(cursor.getString(cursor.getColumnIndex("marathiName")));
            book.setImgUrl(cursor.getString(cursor.getColumnIndex("imgUrl")));
            book.setStocks(cursor.getInt(cursor.getColumnIndex("stocks")));
            book.setPrice(cursor.getInt(cursor.getColumnIndex("price")));
            book.setQuantity(cursor.getInt(cursor.getColumnIndex("quantity")));
            cart_list.add(book);
            cursor.moveToNext();
        }
        return cart_list;
    }

    public void deleteData(String englishName){
        SQLiteDatabase database = getWritableDatabase();
        database.delete(TABLE_NAME,"englishName=?",new String[]{englishName});
    }

    public boolean doesExists(String englishName){
        SQLiteDatabase database = getWritableDatabase();
        String Query = "SELECT * FROM "+TABLE_NAME+" WHERE "+ "englishName=?";
        Cursor cursor = database.rawQuery(Query,new String[]{englishName});
        if(cursor.getCount() <= 0){
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }

    public void updateData(Book book){
        SQLiteDatabase database = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("quantity",book.getQuantity());
        database.update(TABLE_NAME,contentValues,"englishName=?",new String[]{book.getEnglishName()});
    }

    public void deleteAll()
    {
        SQLiteDatabase database = getWritableDatabase();
        database.execSQL("delete from "+ TABLE_NAME);
        database.close();
    }
}

