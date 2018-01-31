package noname.shift.getmoney;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import noname.shift.getmoney.data.ListDbHelper;
import static noname.shift.getmoney.data.ListContract.ListEntry;

public class ListActivity extends AppCompatActivity {

    /*какой-то list*
    какой-то адаптер
    кнопка отправки сообщения
     */

    private ListDbHelper dbHelper;
    Button button; //TODO: заменить

    SharedPreferences settings;
    static String messageText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_target_contacts);
        button = findViewById(R.id.button_send);
        dbHelper = new ListDbHelper(this);

        settings = getSharedPreferences(SharedPreferencesConstants.APP_PREFERENCES, Context.MODE_PRIVATE);
        button.setOnClickListener(view -> {
            initMessage();
            sendMessage(view);
        });
    }

    private void initMessage() {
        messageText = "Привет! Скидываемся по "
                + settings.getInt(SharedPreferencesConstants.APP_PREFERENCES_AVERAGE_SUM, 0)
                + " рублей на карту "
                + settings.getString(SharedPreferencesConstants.APP_PREFERENCES_CARD_NUMBER, "");
    }

    @Override
    protected void onStart() {
        super.onStart();
        displayDatabaseInfo();
    }

    public void sendMessage(View v) {
        String number = "smsto:" + selectNumbers();
    //    number = "smsto:" + "89513772523";
        Log.i("numbers", number);
    //    String messageText = "hello";
        Intent sms = new Intent(Intent.ACTION_SENDTO, Uri.parse(number));

        sms.putExtra("sms_body", messageText);
        startActivity(sms);
    }

    private String selectNumbers() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        StringBuilder numbers = new StringBuilder();

        String[] projection = {
                ListEntry.COLUMN_PHONE,
                ListEntry.COLUMN_PAID };

        try (Cursor cursor = db.query(
                ListEntry.TABLE_NAME,           // таблица
                projection,                     // столбцы
                null,                  // столбцы для условия WHERE
                null,               // значения для условия WHERE
                null,                  // Don't group the rows
                null,                   // Don't filter by row groups
                null)) {

            int phoneColumnIndex = cursor.getColumnIndex(ListEntry.COLUMN_PHONE);
            int paidColumnIndex = cursor.getColumnIndex(ListEntry.COLUMN_PAID);

            while (cursor.moveToNext()) {
                String currentPhone = cursor.getString(phoneColumnIndex);
                int currentStatus = cursor.getInt(paidColumnIndex);
                if (currentStatus == ListEntry.FALSE) {
                    numbers.append("; ").append(currentPhone);
                }
            }
        }
        db.close();
        return numbers.substring(2);
    }

    private void displayDatabaseInfo() {
        // Создадим и откроем для чтения базу данных
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Зададим условие для выборки - список столбцов
        String[] projection = {
                ListEntry._ID,
                ListEntry.COLUMN_NAME,
                ListEntry.COLUMN_PHONE,
                ListEntry.COLUMN_PAID };


        // Делаем запрос

//        TextView displayTextView = (TextView) findViewById(R.id.text_view_info);

        try (Cursor cursor = db.query(
                ListEntry.TABLE_NAME,           // таблица
                projection,                     // столбцы
                null,                  // столбцы для условия WHERE
                null,               // значения для условия WHERE
                null,                  // Don't group the rows
                null,                   // Don't filter by row groups
                null)) {
//            displayTextView.setText("Таблица содержит " + cursor.getCount() + " гостей.\n\n");
//            displayTextView.append(GuestEntry._ID + " - " +
//                    GuestEntry.COLUMN_NAME + " - " +
//                    GuestEntry.COLUMN_CITY + " - " +
//                    GuestEntry.COLUMN_GENDER + " - " +
//                    GuestEntry.COLUMN_AGE + "\n");

            // Узнаем индекс каждого столбца
            int idColumnIndex = cursor.getColumnIndex(ListEntry._ID);
            int nameColumnIndex = cursor.getColumnIndex(ListEntry.COLUMN_NAME);
            int phoneColumnIndex = cursor.getColumnIndex(ListEntry.COLUMN_PHONE);
            int paidColumnIndex = cursor.getColumnIndex(ListEntry.COLUMN_PAID);

            Log.i("rows", "id: " + idColumnIndex + " name: " + nameColumnIndex + " phone: " + phoneColumnIndex + " paid: " + paidColumnIndex);

            // Проходим через все ряды
            while (cursor.moveToNext()) {
                // Используем индекс для получения строки или числа
                int currentID = cursor.getInt(idColumnIndex);
                String currentName = cursor.getString(nameColumnIndex);
                String currentPhone = cursor.getString(phoneColumnIndex);
                int currentStatus = cursor.getInt(paidColumnIndex);
                // Выводим значения каждого столбца

                Log.i("table", currentName + ", " + currentPhone + ", " + currentStatus);
            }
        }
        db.close();
    }
}
