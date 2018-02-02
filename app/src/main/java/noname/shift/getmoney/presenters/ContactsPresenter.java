package noname.shift.getmoney.presenters;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

import noname.shift.getmoney.ContactsActivity;
import noname.shift.getmoney.models.Contact;
import noname.shift.getmoney.models.ContactReader;
import noname.shift.getmoney.models.ListDbHelper;
import noname.shift.getmoney.views.ContactsView;


public class ContactsPresenter {

    private ContactsView view;
    private ListDbHelper listDbHelper;
    private ArrayList<Contact> contacts = new ArrayList<>();
    private int checkCount = 0;

    public ContactsPresenter(ContactsView view, ListDbHelper listDbHelper){
        this.view = view;
        this.listDbHelper = listDbHelper;
    }

    public void choiseContact(SharedPreferences settings){
        if (checkCount > 0) {
            SaveAsyncTask save = new SaveAsyncTask(checkCount, settings);
            save.execute();
            view.goTargetContact();
        } else {
            view.showMessage("Выберите от 1 до 10 контактов из списка");
        }
    }
    public void loadContacts(ContactsAdapter adapter, Context context) {
        MyAsyncTask task = new MyAsyncTask(adapter, context);
        task.execute();
        view.setVisibility();
    }

    public void submitText(String s ){
        ArrayList<Contact> searhItems = new ArrayList<>();
        s = s.toLowerCase();
        for (Contact item: contacts) {
            if (item.getName().toLowerCase().equals(s)) {
                searhItems.add(item);
            }
        }
       view.resetAdapter(searhItems);
    }

    public void changeText(String s){

        Log.i("change", Integer.toString(checkCount));
        ArrayList<Contact> searhItems = new ArrayList<>();
        s = s.toLowerCase();

        for (Contact item: contacts) {
            if (compareString(item.getName().toLowerCase(), s)) {
                searhItems.add(item);
            }
        }
        view.resetAdapter(searhItems);
    }

    public void bindViewHolder(ArrayList<Contact> adapterContacts, ContactsHolder holder, int position){
        int pos = getContactState(adapterContacts.get(position).getName());

        if (pos != -1) {
            boolean checked = contacts.get(pos).isChecked();
            holder.setLabel(checked);
        }

        holder.setName(adapterContacts.get(position).getName());
        holder.setNumber(adapterContacts.get(position).getPhone());
    }

    public void changeContactState(ContactsHolder holder, String name, boolean state) {

        Log.i("count", Integer.toString(checkCount));
        if (state) {
            holder.setLabel(false);
            --checkCount;
        } else {
            if (checkCount == 10) {
                view.showMessage("Выберите не более 10 контактов из списка");
                return;
            } else {
                holder.setLabel(true);
                ++checkCount;
            }
        }
        for (Contact contact : contacts) {
            if (contact.getName().equals(name)) {
                contact.setChecked(!state);
                break;
            }
        }




    }

    private int getContactState(String name) {
        for (int i = 0; i < contacts.size(); ++i) {
            if (contacts.get(i).getName().equals(name)) {
                return i;
            }
        }
        return -1;
    }

    private void countSum(int contactsCount, SharedPreferences settings) {
        SharedPreferences.Editor editor = settings.edit();
        double sum = (double) settings.getInt(SharedPreferencesConstants.APP_PREFERENCES_SUM, 0)/contactsCount;
        Log.i("sum", Double.toString(sum));
        editor.putInt(SharedPreferencesConstants.APP_PREFERENCES_AVERAGE_SUM, (int) Math.ceil(sum));
        Log.i("finalSum", Integer.toString((int) Math.ceil(sum)));
        editor.apply();
    }



    private class MyAsyncTask extends AsyncTask<Void, Void, Void> {

        private ContactsAdapter adapter;
        private Context context;

        public MyAsyncTask(ContactsAdapter adapter, Context context){
            this.adapter = adapter;
            this.context = context;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            contacts = new ContactReader().readAll(context);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            adapter.update(contacts);
            Log.i("size", ": " + contacts.size());
        }
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

    private class SaveAsyncTask extends AsyncTask<Void, Void, Void> {
        private int count;
        private SharedPreferences settings;

        public SaveAsyncTask(int count, SharedPreferences settings) {
            this.count = count;
            this.settings = settings;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            listDbHelper.recreateTable();
            for (Contact contact : contacts) {
                if (contact.isChecked()) {
                    boolean success = listDbHelper.insertContact(contact.getName(), contact.getPhone());
                    if(!success){
                        view.showMessage("Ошибка");
                    }
                }
            }
            countSum(count, settings);
            return null;
        }
    }



}
