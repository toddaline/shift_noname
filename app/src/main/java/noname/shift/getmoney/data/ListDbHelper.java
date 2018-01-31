package noname.shift.getmoney.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import static noname.shift.getmoney.data.ListContract.ListEntry;

public class ListDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "contacts.db";

    /**
     * Версия базы данных. При изменении схемы увеличить на единицу
     */
    private static final int DATABASE_VERSION = 1;

    public ListDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        final String SQL_CREATE_TABLE = "CREATE TABLE " + ListEntry.TABLE_NAME + " ("
                + ListEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ListEntry.COLUMN_NAME + " TEXT NOT NULL, "
                + ListEntry.COLUMN_PHONE + " TEXT NOT NULL, "
                + ListEntry.COLUMN_PAID + " INTEGER NOT NULL DEFAULT 0);";

        // Запускаем создание таблицы
        sqLiteDatabase.execSQL(SQL_CREATE_TABLE);

        Log.i("table", "table created");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ListEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
        Log.i("upgrade", "version: " + DATABASE_VERSION);
    }

}
