package noname.shift.getmoney;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import android.provider.ContactsContract;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

import noname.shift.getmoney.data.ListContract.ListEntry;
import noname.shift.getmoney.data.ListDbHelper;

public class ContactsActivity extends AppCompatActivity {

    Button button;
    ArrayList<Contact> contacts = new ArrayList<>();
    ArrayList<Contact> checkedContacts = new ArrayList<>();
    ListDbHelper dbHelper;
    RecyclerView rv;
    RVAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_contacts);

        button = findViewById(R.id.button_target);

//        ctvName.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (ctvName.isChecked())
//                    ctvName.setChecked(false);
//                else {
//                    ctvName.setChecked(true);
//                }
//            }
//        });

        dbHelper = new ListDbHelper(this);
        recreateTable(dbHelper);
        rv = findViewById(R.id.recycler_view_all_contacts);
        adapter = new RVAdapter(contacts);
        Log.i("size", ": " + contacts.size());
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        rv.setLayoutManager(mLayoutManager);
        rv.setAdapter(adapter);

        Log.i("table", "smth happened");

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (checkSelectedContactsNumber()) {
                    for (Contact contact : checkedContacts) {
                        insertContact(contact.getName(), contact.getPhone());
                    }

                    countSum();
                    Intent intent = new Intent(ContactsActivity.this, ListActivity.class);
                    startActivity(intent);

                } else {
                    Toast.makeText(ContactsActivity.this, "Выберите от 1 до 10 контактов из списка", Toast.LENGTH_SHORT).show();
                }
            }
        });

        MyAsyncTask task = new MyAsyncTask();
        task.execute();
    }

    public class RVAdapter extends RecyclerView.Adapter<RVAdapter.ContactViewHolder> {

        ArrayList<Contact> contacts;

        RVAdapter(ArrayList<Contact> contacts) {
            this.contacts = contacts;
        }

        @Override
        public ContactViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false);
            return new ContactViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ContactViewHolder holder, int position) {
            holder.name.setText(contacts.get(position).getName());
            holder.number.setText(contacts.get(position).getPhone());
        }

        @Override
        public int getItemCount() {
            return contacts.size();
        }


        class ContactViewHolder extends RecyclerView.ViewHolder {
            CardView cv;
            CheckedTextView name;
            TextView number;

            ContactViewHolder(View itemView) {
                super(itemView);
                cv = itemView.findViewById(R.id.card);
                name = itemView.findViewById(R.id.name);
                number = itemView.findViewById(R.id.number);
                name.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (name.isChecked()) {
                            name.setChecked(false);
                        } else {
                            name.setChecked(true);
                        }
                    }
                });
            }
        }
    }

    private void countSum() {
        SharedPreferences settings = getSharedPreferences(SharedPreferencesConstants.APP_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        double sum = (double) settings.getInt(SharedPreferencesConstants.APP_PREFERENCES_SUM, 0)/checkedContacts.size();
        Log.i("sum", Double.toString(sum));
        editor.putInt(SharedPreferencesConstants.APP_PREFERENCES_AVERAGE_SUM, (int) Math.ceil(sum));
        Log.i("finalSum", Integer.toString((int) Math.ceil(sum)));
        editor.apply();
    }

    private boolean checkSelectedContactsNumber() {

        return checkedContacts.size() > 0 && checkedContacts.size() <= 10;
    }

    private void recreateTable(ListDbHelper dbHelper) {
        SQLiteDatabase sqLiteDatabase = dbHelper.getReadableDatabase();
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ListEntry.TABLE_NAME);

        final String SQL_CREATE_TABLE = "CREATE TABLE " + ListEntry.TABLE_NAME + " ("
                + ListEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ListEntry.COLUMN_NAME + " TEXT NOT NULL, "
                + ListEntry.COLUMN_PHONE + " TEXT NOT NULL, "
                + ListEntry.COLUMN_PAID + " INTEGER NOT NULL DEFAULT 0);";

        // Запускаем создание таблицы
        sqLiteDatabase.execSQL(SQL_CREATE_TABLE);
        sqLiteDatabase.close();
    }

    private void insertContact(String name, String phoneNumber) {

        // Gets the database in write mode
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        // Создаем объект ContentValues, где имена столбцов ключи,
        // а информация о госте является значениями ключей
        ContentValues values = new ContentValues();
        values.put(ListEntry.COLUMN_NAME, name);
        values.put(ListEntry.COLUMN_PHONE, phoneNumber);
        values.put(ListEntry.COLUMN_PAID, ListEntry.FALSE);

        long newRowId = db.insert(ListEntry.TABLE_NAME, null, values);

        // Выводим сообщение в успешном случае или при ошибке
        if (newRowId == -1) {
            // Если ID  -1, значит произошла ошибка
            Toast.makeText(this, "Ошибка", Toast.LENGTH_SHORT).show();
        }
        db.close();
    }

    private class MyAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            getAll(ContactsActivity.this);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            adapter.notifyDataSetChanged();
            Log.i("size", ": " + contacts.size());
        }
    }

    private static final String DISPLAY_NAME = ContactsContract.Contacts.DISPLAY_NAME;
    private static final String HAS_PHONE_NUMBER = ContactsContract.Contacts.HAS_PHONE_NUMBER;
    private static final String PHONE_NUMBER = ContactsContract.CommonDataKinds.Phone.NUMBER;
    private static final String PHONE_CONTACT_ID = ContactsContract.CommonDataKinds.Phone.CONTACT_ID;

    public ArrayList<Contact> getAll(Context context) {
        ContentResolver cr = context.getContentResolver();

        Cursor pCur = cr.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{PHONE_NUMBER, PHONE_CONTACT_ID, DISPLAY_NAME, HAS_PHONE_NUMBER},
                null,
                null,
                null
        );

        int count = 0;
        if (pCur != null) {
            if (pCur.getCount() > 0) {
                HashMap<String, ArrayList<String>> phones = new HashMap<>();
                while (pCur.moveToNext()) {
                    String contactName = pCur.getString(pCur.getColumnIndex(DISPLAY_NAME));

                    /*для нескольких телефонов - оставить один?*/
                    ArrayList<String> curPhones = new ArrayList<>();

                    if (phones.containsKey(contactName)) {
                        continue;
            //            curPhones = phones.get(contactName);

                    }

                    if (pCur.getInt(pCur.getColumnIndex(HAS_PHONE_NUMBER)) > 0) {
                        curPhones.add(pCur.getString(pCur.getColumnIndex(PHONE_NUMBER)));
                        Log.i("cur1", contactName);
                        Log.i("cur", pCur.getString(pCur.getColumnIndex(PHONE_NUMBER)));
                    }

                    phones.put(contactName, curPhones);
                    contacts.add(new Contact(contactName, curPhones.get(0)));
                    //   if (count == 3) {
                        checkedContacts.add(new Contact(contactName, curPhones.get(0)));
                //    }
                    count++;
                }
            }
            pCur.close();
        }
        return null;
    }
}
