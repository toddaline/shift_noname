package noname.shift.getmoney;

import android.Manifest;
import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import noname.shift.getmoney.data.ListContract.ListEntry;
import noname.shift.getmoney.data.ListDbHelper;

import static noname.shift.getmoney.MainActivity.REQUEST_CODE_PERMISSION_READ_CONTACTS;

public class ContactsActivity extends AppCompatActivity {
    private static final String DISPLAY_NAME = ContactsContract.Contacts.DISPLAY_NAME;
    private static final String HAS_PHONE_NUMBER = ContactsContract.Contacts.HAS_PHONE_NUMBER;
    private static final String PHONE_NUMBER = ContactsContract.CommonDataKinds.Phone.NUMBER;
    private static final String PHONE_CONTACT_ID = ContactsContract.CommonDataKinds.Phone.CONTACT_ID;

    private Button button;
    private ArrayList<Contact> contacts = new ArrayList<>();
    // private ArrayList<Contact> checkedContacts = new ArrayList<>();
    private ListDbHelper dbHelper;
    private RecyclerView rv;
    private RVAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_contacts);

        Toolbar toolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        button = findViewById(R.id.button_target);

        dbHelper = new ListDbHelper(this);
        // recreateTable(dbHelper);
        rv = findViewById(R.id.recycler_view_all_contacts);
        adapter = new RVAdapter(contacts);
        Log.i("size", ": " + contacts.size());
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        rv.setLayoutManager(mLayoutManager);
        rv.setAdapter(adapter);
        button.setVisibility(View.INVISIBLE);

        button.setOnClickListener(view -> {

            if (adapter.getCheckCount() > 0) {
                recreateTable(dbHelper);
                for (Contact contact : contacts) {
                    if (contact.isChecked()) {
                        insertContact(contact.getName(), contact.getPhone());
                    }
                }
                countSum(adapter.getCheckCount());
                Intent intent = new Intent(ContactsActivity.this, ListActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);

            } else {
                Toast.makeText(ContactsActivity.this, "Выберите от 1 до 10 контактов из списка", Toast.LENGTH_SHORT).show();
            }
        });

        if (checkPermissions()) {
            Log.i("table", "smth happened");
            loadContacts();
        }
    }

    private void loadContacts() {
        MyAsyncTask task = new MyAsyncTask();
        task.execute();
        button.setVisibility(View.VISIBLE);
    }

    private boolean checkPermissions() {
        int permissionStatus = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS);
        if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_CONTACTS},
                    REQUEST_CODE_PERMISSION_READ_CONTACTS);
            return false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_PERMISSION_READ_CONTACTS:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    loadContacts();
                    // permission granted
                    //    readContacts();
                } else {
                    finish();
                    // permission denied
                }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        SearchManager searchManager = ( SearchManager ) getSystemService( this.SEARCH_SERVICE );
        SearchView searchView = ( SearchView ) menu.findItem( R.id.action_search).getActionView();
        searchView.setSearchableInfo( searchManager.getSearchableInfo( getComponentName() ) );
        searchView.setOnQueryTextListener(
                new SearchView.OnQueryTextListener()
                {
                    @Override
                    public boolean onQueryTextSubmit( String s ) {
                        ArrayList<Contact> searhItems = new ArrayList<>();
                        s = s.toLowerCase();
                        for (Contact item: contacts) {
                            if (item.getName().toLowerCase().equals(s)) {
                                searhItems.add(item);
                            }
                        }

                        initRecyclerView(searhItems);
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String s)
                    {
                        ArrayList<Contact> searhItems = new ArrayList<>();
                        s = s.toLowerCase();
                        Log.i("change", Integer.toString(adapter.checkCount));
                        for (Contact item: contacts) {
                            if (compareString(item.getName().toLowerCase(), s)) {
                                searhItems.add(item);
                            }
                        }

                        initRecyclerView(searhItems);
                        return false;
                    }
                }
        );
        return true;
    }

    void changeContactState(String name, boolean state) {
        for (Contact contact : contacts) {
            if (contact.getName().equals(name)) {
                contact.setChecked(state);
                return;
            }
        }
    }

    int getContactState(String name) {
        for (int i = 0; i < contacts.size(); ++i) {
            if (contacts.get(i).getName().equals(name)) {
                return i;
            }
        }
        return -1;
    }

    private class RVAdapter extends RecyclerView.Adapter<RVAdapter.ContactViewHolder> {

        private ArrayList<Contact> adapterContacts;
        private int checkCount = 0;

        int getCheckCount() {
            return checkCount;
        }

        RVAdapter(ArrayList<Contact> contacts) {
            this.adapterContacts = contacts;
        }

        @Override
        public ContactViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
           View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false);
           return new ContactViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ContactViewHolder holder, int position) {
            holder.name.setText(adapterContacts.get(position).getName());
            holder.number.setText(adapterContacts.get(position).getPhone());
            Log.i("position", Integer.toString(position));
            int pos = getContactState(adapterContacts.get(position).getName());
            holder.position = pos;
            if (pos != -1) {
                if (contacts.get(pos).isChecked()) {
                    holder.name.setChecked(true);
                } else {
                    holder.name.setChecked(false);
                }
            }
        }

        @Override
        public int getItemCount() {
            return adapterContacts.size();
        }

        class ContactViewHolder extends RecyclerView.ViewHolder {
            private CardView cv;
            private CheckedTextView name;
            private TextView number;
            private int position;

            ContactViewHolder(View itemView) {
                super(itemView);
                cv = itemView.findViewById(R.id.card);
                name = itemView.findViewById(R.id.name);
                number = itemView.findViewById(R.id.number);

                name.setOnClickListener(view -> {
                    if (name.isChecked()) {
                //        contacts.get(position).setChecked(false);
                        changeContactState(name.getText().toString(), false);
                        name.setChecked(false);
                        checkCount--;
                        Log.i("count", Integer.toString(checkCount));
                    } else {
                        if (checkCount == 10) {
                            Toast.makeText(ContactsActivity.this, "Выберите не более 10 контактов из списка", Toast.LENGTH_SHORT).show();
                            return;
                        }
                //        contacts.get(position).setChecked(true);
                        changeContactState(name.getText().toString(), true);
                        name.setChecked(true);
                        checkCount++;
                        Log.i("count", Integer.toString(checkCount));
                    }
                });
            }
        }
    }

    private void initRecyclerView(ArrayList<Contact> items) {
        LinearLayoutManager verticalLinearLayoutManager;
        verticalLinearLayoutManager = new LinearLayoutManager(this);
        rv.setLayoutManager(verticalLinearLayoutManager);

        RVAdapter adapter = new RVAdapter(items);
        rv.setAdapter(adapter);
    }

    private boolean compareString(String main, String search){
        if(search.length() > main.length()){
            return false;
        }
        for (int i = 0;  i < search.length(); ++i){
            if(!(main.charAt(i) == search.charAt(i))){
                return false;
            }
        }
        return true;
    }

    private void countSum(int contactsCount) {
        SharedPreferences settings = getSharedPreferences(SharedPreferencesConstants.APP_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        double sum = (double) settings.getInt(SharedPreferencesConstants.APP_PREFERENCES_SUM, 0)/contactsCount;
        Log.i("sum", Double.toString(sum));
        editor.putInt(SharedPreferencesConstants.APP_PREFERENCES_AVERAGE_SUM, (int) Math.ceil(sum));
        Log.i("finalSum", Integer.toString((int) Math.ceil(sum)));
        editor.apply();
    }

    // private boolean checkSelectedContactsNumber() {

    //     return checkedContacts.size() > 0 && checkedContacts.size() <= 10;
    // }

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
                        // checkedContacts.add(new Contact(contactName, curPhones.get(0)));
                //    }
                    count++;
                }
                Collections.sort(contacts, (o1, o2) -> o1.getName().compareTo(o2.getName()));
            }
            pCur.close();
        }
        return null;
    }
}
