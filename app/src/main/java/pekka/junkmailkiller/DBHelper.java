package pekka.junkmailkiller;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "JunkMailKillerDB";
    private static final String SETTINGS_TABLE_NAME = "SETTINGS";
    private static final String SETTINGS_COLUMN_ID = "ID";
    private static final String SETTINGS_COLUMN_KEY = "KEY";
    private static final String SETTINGS_COLUMN_VALUE = "VALUE";

    private static final String KEY_HOST = "HOST";
    private static final String KEY_USER = "USER";
    private static final String KEY_PASSWORD = "PASSWORD";
    private final String KEY_FREQ = "FREQ";
    private static final String KEY_KEYWORD = "KEYWORD";

    private HashMap hp;

    public DBHelper(Context context)
    {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE " + SETTINGS_TABLE_NAME + " (" + SETTINGS_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + SETTINGS_COLUMN_KEY + " TEXT NOT NULL, " + SETTINGS_COLUMN_VALUE +
                        " TEXT)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        //db.execSQL("DROP TABLE IF EXISTS contacts");
        //onCreate(db);
    }

    public void insertData(SQLiteDatabase db, String key, String value) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(SETTINGS_COLUMN_KEY, key);
        contentValues.put(SETTINGS_COLUMN_VALUE, value);
        db.insert(SETTINGS_TABLE_NAME, null, contentValues);
    }

    public void updateData(SQLiteDatabase db, String key, String value) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(SETTINGS_COLUMN_KEY, key);
        contentValues.put(SETTINGS_COLUMN_VALUE, value);
        db.update(SETTINGS_TABLE_NAME, contentValues, SETTINGS_COLUMN_KEY + " = ? ", new String[]{key});
    }

    public Integer deleteKeyword (String value)
    {
        String whereClause = SETTINGS_COLUMN_KEY + " = ? AND " + SETTINGS_COLUMN_VALUE + " = ? ";
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(SETTINGS_TABLE_NAME,
                whereClause,
                new String[] { KEY_KEYWORD, value });
    }

    public Settings readSettings() {
        ArrayList<String> values = new ArrayList<String>();
        Settings settings = new Settings();

        values = getValues(KEY_HOST);
        if (values.size() > 0) {
            settings.setHost(values.get(0));
        }
        values = getValues(KEY_USER);
        if (values.size() > 0) {
            settings.setUser(values.get(0));
        }
        values = getValues(KEY_PASSWORD);
        if (values.size() > 0) {
            settings.setPassword(values.get(0));
        }
        values = getValues(KEY_FREQ);
        if (values.size() > 0) {
            settings.setFreq(values.get(0));
        }
        values = getValues(KEY_KEYWORD);
        if (values.size() > 0) {
            settings.setKeyWords(values);
        }

        return settings;
    }


    public void insertOrUpdateSettings (String host, String user, String passwd, String freq)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();

        ArrayList<String> values = new ArrayList<String>();

        values = getValues(KEY_HOST);
        if (values.size() == 0) {
            insertData(db, KEY_HOST, host);
        } else {
            updateData(db, KEY_HOST, host);
        }

        values = getValues(KEY_USER);
        if (values.size() == 0) {
            insertData(db, KEY_USER, user);
        } else {
            updateData(db, KEY_USER, user);
        }

        values = getValues(KEY_PASSWORD);
        if (values.size() == 0) {
            insertData(db, KEY_PASSWORD, passwd);
        } else {
            updateData(db, KEY_PASSWORD, passwd);
        }

        values = getValues(KEY_FREQ);
        if (values.size() == 0) {
            insertData(db, KEY_FREQ, freq);
        } else {
            updateData(db, KEY_FREQ, freq);
        }

        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();

        return;
    }

    public void insertKeyword(String keyword)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();

        insertData(db, KEY_KEYWORD, keyword);

        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();

        return;
    }

    public int numberOfRows(){
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, SETTINGS_TABLE_NAME);
        return numRows;
    }

    public ArrayList<String> getValues(String key)
    {
        ArrayList<String> dataList = new ArrayList<String>();
        String sqlCommand = "SELECT * FROM " +  SETTINGS_TABLE_NAME + " where " + SETTINGS_COLUMN_KEY + " = '" + key + "' order by " + SETTINGS_COLUMN_VALUE;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery(sqlCommand, null);
        res.moveToFirst();

        while(res.isAfterLast() == false){
            int columnIndex = res.getColumnIndex(SETTINGS_COLUMN_VALUE);
            String value = res.getString(columnIndex);
            dataList.add(value);
            res.moveToNext();
        }
        return dataList;
    }
}
