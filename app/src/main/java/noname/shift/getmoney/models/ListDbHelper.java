package noname.shift.getmoney.models;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

import noname.shift.getmoney.presenters.SharedPreferencesConstants;

public class ListDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "contacts.db";
    private static final int DATABASE_VERSION = 1;
    private static final int errorInsertDb = -1;

    private Context context;
    public ListDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        final String SQL_CREATE_TABLE = "CREATE TABLE " + ListContract.ListEntry.TABLE_NAME + " ("
                + ListContract.ListEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ListContract.ListEntry.COLUMN_NAME + " TEXT NOT NULL, "
                + ListContract.ListEntry.COLUMN_PHONE + " TEXT NOT NULL, "
                + ListContract.ListEntry.COLUMN_PAID + " INTEGER NOT NULL DEFAULT 0);";

        sqLiteDatabase.execSQL(SQL_CREATE_TABLE);

        Log.i("table", "table created");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ListContract.ListEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
        Log.i("upgrade", "version: " + DATABASE_VERSION);
    }

    public void recreateTable() {
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ListContract.ListEntry.TABLE_NAME);

        final String SQL_CREATE_TABLE = "CREATE TABLE " + ListContract.ListEntry.TABLE_NAME + " ("
                + ListContract.ListEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ListContract.ListEntry.COLUMN_NAME + " TEXT NOT NULL, "
                + ListContract.ListEntry.COLUMN_PHONE + " TEXT NOT NULL, "
                + ListContract.ListEntry.COLUMN_PAID + " INTEGER NOT NULL DEFAULT 0);";

        sqLiteDatabase.execSQL(SQL_CREATE_TABLE);
        sqLiteDatabase.close();
    }

    public boolean insertContact(String name, String phoneNumber) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ListContract.ListEntry.COLUMN_NAME, name);
        values.put(ListContract.ListEntry.COLUMN_PHONE, phoneNumber);
        values.put(ListContract.ListEntry.COLUMN_PAID, ListContract.ListEntry.FALSE);

        long newRowId = db.insert(ListContract.ListEntry.TABLE_NAME, null, values);

        if (newRowId == errorInsertDb) {
            // Если ID  -1, значит произошла ошибка
            return false;
        } else{
            db.close();
            return true;
        }
    }

    public void deleteTable() {
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ListContract.ListEntry.TABLE_NAME);
        sqLiteDatabase.close();
    }

    public void updateDatabase(int id, boolean status) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues cv = new ContentValues();
        if (status) {
            cv.put(ListContract.ListEntry.COLUMN_PAID, ListContract.ListEntry.TRUE);
        } else {
            cv.put(ListContract.ListEntry.COLUMN_PAID, ListContract.ListEntry.FALSE);
        }

        Log.i("id to update", Integer.toString(id));
        db.update(ListContract.ListEntry.TABLE_NAME, cv, ListContract.ListEntry._ID + " = ?", new String[] { Integer.toString(id) });
        db.close();
    }

    public ArrayList<Contact> displayDatabaseInfo() {
        // Создадим и откроем для чтения базу данных
        ArrayList<Contact> contacts = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        // Зададим условие для выборки - список столбцов
        String[] projection = {
                ListContract.ListEntry._ID,
                ListContract.ListEntry.COLUMN_NAME,
                ListContract.ListEntry.COLUMN_PHONE,
                ListContract.ListEntry.COLUMN_PAID};

        // Делаем запрос
        try (Cursor cursor = db.query(
                ListContract.ListEntry.TABLE_NAME,           // таблица
                projection,                     // столбцы
                null,                  // столбцы для условия WHERE
                null,               // значения для условия WHERE
                null,                  // Don't group the rows
                null,                   // Don't filter by row groups
                null)) {

            // Узнаем индекс каждого столбца
            int idColumnIndex = cursor.getColumnIndex(ListContract.ListEntry._ID);
            int nameColumnIndex = cursor.getColumnIndex(ListContract.ListEntry.COLUMN_NAME);
            int phoneColumnIndex = cursor.getColumnIndex(ListContract.ListEntry.COLUMN_PHONE);
            int paidColumnIndex = cursor.getColumnIndex(ListContract.ListEntry.COLUMN_PAID);

            Log.i("rows", "id: " + idColumnIndex + " name: " + nameColumnIndex + " phone: " + phoneColumnIndex + " paid: " + paidColumnIndex);

            // Проходим через все ряды
            while (cursor.moveToNext()) {
                // Используем индекс для получения строки или числа
                int currentID = cursor.getInt(idColumnIndex);
                String currentName = cursor.getString(nameColumnIndex);
                String currentPhone = cursor.getString(phoneColumnIndex);
                int currentStatus = cursor.getInt(paidColumnIndex);
                Contact contact = new Contact(currentName, currentPhone);
                contact.setChecked(currentStatus);
                contacts.add(contact);

                Log.i("table", "id: " + currentID + " name: " + currentName + ", " + currentPhone + ", " + currentStatus);
            }
        }
        db.close();
        return contacts;
    }
}
