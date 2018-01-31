package noname.shift.getmoney;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.TextView;

import java.util.ArrayList;

import noname.shift.getmoney.data.ListDbHelper;
import static noname.shift.getmoney.data.ListContract.ListEntry;

public class ListActivity extends AppCompatActivity {

    private ListDbHelper dbHelper;
    Button button;
    ArrayList<Contact> contacts;

    SharedPreferences settings;
    static String messageText;
    RecyclerView rv;
    RVAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_target_contacts);
        button = findViewById(R.id.button_send);
        contacts = new ArrayList<>();
        dbHelper = new ListDbHelper(this);

        rv = findViewById(R.id.recycler_view__target_contacts);

        adapter = new RVAdapter(contacts);
        displayDatabaseInfo();
        Log.i("size", ": " + contacts.size());
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        rv.setLayoutManager(mLayoutManager);
        rv.setAdapter(adapter);

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
    //    displayDatabaseInfo();
    //    adapter.notifyDataSetChanged();
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

        StringBuilder numbers = new StringBuilder();

        for (Contact contact : contacts) {
            if (!contact.isChecked()) {
                numbers.append("; ").append(contact.getPhone());
            }
        }
        return numbers.substring(2);
    }

    private void updateDatabase(int id, boolean status) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues cv = new ContentValues();
        if (status) {
            cv.put(ListEntry.COLUMN_PAID, ListEntry.TRUE);
        } else {
            cv.put(ListEntry.COLUMN_PAID, ListEntry.FALSE);
        }

        Log.i("id to update", Integer.toString(id));
        db.update(ListEntry.TABLE_NAME, cv, ListEntry._ID + " = ?", new String[] { Integer.toString(id) });
        db.close();
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
        try (Cursor cursor = db.query(
                ListEntry.TABLE_NAME,           // таблица
                projection,                     // столбцы
                null,                  // столбцы для условия WHERE
                null,               // значения для условия WHERE
                null,                  // Don't group the rows
                null,                   // Don't filter by row groups
                null)) {

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
                Contact contact = new Contact(currentName, currentPhone);
                contact.setChecked(currentStatus);
                contacts.add(contact);

                Log.i("table", "id: " + currentID + " name: " + currentName + ", " + currentPhone + ", " + currentStatus);
            }
        }
        db.close();
    }

    public class RVAdapter extends RecyclerView.Adapter<RVAdapter.ContactViewHolder> {

        ArrayList<Contact> contacts;
        int checkCount = 0;

        public int getCheckCount() {
            return checkCount;
        }

        RVAdapter(ArrayList<Contact> contacts) {
            this.contacts = contacts;
        }

        @Override
        public ContactViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false);
            return new ContactViewHolder(v);
        }

        @Override
        public void onBindViewHolder(RVAdapter.ContactViewHolder holder, int position) {
            holder.name.setText(contacts.get(position).getName());
            holder.number.setText(contacts.get(position).getPhone());
            Log.i("position", Integer.toString(position));
            Log.i("arraySize", Integer.toString(contacts.size()));
            holder.position = position;
            if (contacts.get(position).isChecked()) {
                holder.name.setChecked(true);
            } else {
                holder.name.setChecked(false);
            }
        }

        @Override
        public int getItemCount() {
            return contacts.size();
        }

        class ContactViewHolder extends RecyclerView.ViewHolder {
            CardView cv;
            CheckedTextView name;
            TextView number;
            int position;

            ContactViewHolder(View itemView) {
                super(itemView);
                cv = itemView.findViewById(R.id.card);
                name = itemView.findViewById(R.id.name);
                number = itemView.findViewById(R.id.number);

                name.setOnClickListener(view -> {
                    if (name.isChecked()) {
                        contacts.get(position).setChecked(false);
                        name.setChecked(false);
                        updateDatabase(position + 1, false);
                        checkCount--;
                    } else {
                        if (checkCount == contacts.size()) {
                            Log.i("check", "change button");
                        }
                        contacts.get(position).setChecked(true);
                        name.setChecked(true);
                        updateDatabase(position + 1, true);
                        checkCount++;
                    }
                });
            }
        }
    }
}
