package noname.shift.getmoney.presenters;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.LocationListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;

import noname.shift.getmoney.models.Contact;
import noname.shift.getmoney.models.ContactReader;
import noname.shift.getmoney.models.ListDbHelper;
import noname.shift.getmoney.views.ContactsView;


public class ContactsPresenter {

    private static final String needAddContacts = "Выберите от 1 до 10 контактов из списка";
    private static final String limitContacts = "Выберите не более 10 контактов из списка";
    private static final String errorSaveDb = "Ошибка";
    private static final String keyState = "key";

    private ContactsView view;
    private ListDbHelper listDbHelper;
    private ArrayList<Contact> contacts = new ArrayList<>();
    private int checkCount = 0;
    private HashMap<String, Boolean> choiseContacts = null;

    public ContactsPresenter(ContactsView view, ListDbHelper listDbHelper) {
        this.view = view;
        this.listDbHelper = listDbHelper;
    }

    public void choiseContact(SharedPreferences settings) {
        if (checkCount > 0) {
            SaveAsyncTask save = new SaveAsyncTask(checkCount, settings);
            save.execute();
            view.goTargetContact();
        } else {
            view.showMessage(needAddContacts);
        }
    }

    public void loadContacts(ContactsAdapter adapter, ContentResolver contentResolver) {
        MyAsyncTask task = new MyAsyncTask(adapter, contentResolver);
        task.execute();
        view.setVisibility();
    }

    public void submitText(String s) {
        ArrayList<Contact> searhItems = new ArrayList<>();
        s = s.toLowerCase();
        for (Contact item : contacts) {
            if (item.getName().toLowerCase().equals(s)) {
                searhItems.add(item);
            }
        }
        view.resetAdapter(searhItems);
    }

    public void changeText(String s) {

        Log.i("change", Integer.toString(checkCount));
        ArrayList<Contact> searhItems = new ArrayList<>();
        s = s.toLowerCase();

        for (Contact item : contacts) {
            if (compareString(item.getName().toLowerCase(), s)) {
                searhItems.add(item);
            }
        }
        view.resetAdapter(searhItems);
    }

    public void bindViewHolder(ArrayList<Contact> adapterContacts, ContactsHolder holder, int position) {
        int pos = getContactState(adapterContacts.get(position).getName());

        if (pos != -1) {
            boolean checked = contacts.get(pos).isChecked();
            holder.setLabel(checked);
        }

        holder.setName(adapterContacts.get(position).getName());
        holder.setNumber(adapterContacts.get(position).getPhone());
    }

    public void changeContactState(ContactsHolder holder, String name, boolean state, CardView cardView) {

        Log.i("count", Integer.toString(checkCount));
        if (state) {
            holder.setLabel(false);
            cardView.setSelected(false);
            --checkCount;
        } else {
            if (checkCount == 10) {
                view.showMessage(limitContacts);
                return;
            } else {
                holder.setLabel(true);
                cardView.setSelected(true);
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

    public void saveStateBoundle(Bundle outState) {
        HashMap<String, Boolean> choiseContacts = new HashMap<>();
        for (Contact contact : contacts) {
            if (contact.isChecked()) {
                choiseContacts.put(contact.getPhone(), true);
            }
        }
        outState.putSerializable(keyState, choiseContacts);
    }

    public void loadStateBoundle(Bundle inState) {
        choiseContacts = (HashMap<String, Boolean>) inState.getSerializable(keyState);
    }

    private int getContactState(String name) {
        for (int i = 0; i < contacts.size(); ++i) {
            if (contacts.get(i).getName().equals(name)) {
                return i;
            }
        }
        return -1;
    }

    private void calculateShare(int contactsCount, SharedPreferences settings) {
        SharedPreferences.Editor editor = settings.edit();
        BigDecimal allSum = new BigDecimal(settings.getInt(SharedPreferencesConstants.APP_PREFERENCES_SUM, 0));
        BigDecimal allContacts = new BigDecimal(contactsCount);
        Log.i("divide", "contacts: " + allContacts.intValue() + "allSum: " + allContacts.intValue() );
        BigDecimal result = allSum.divide(allContacts, BigDecimal.ROUND_HALF_UP);
        result = result.setScale(0, BigDecimal.ROUND_CEILING);
        editor.putInt(SharedPreferencesConstants.APP_PREFERENCES_AVERAGE_SUM, result.intValue());
        Log.i("finalSum", Integer.toString(result.intValue()));
        editor.apply();
    }


    private class MyAsyncTask extends AsyncTask<Void, Void, Void> {

        private ContactsAdapter adapter;
        private ContentResolver contentResolver;

        public MyAsyncTask(ContactsAdapter adapter, ContentResolver contentResolver) {
            this.adapter = adapter;
            this.contentResolver = contentResolver;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            contacts = new ContactReader().readAll(contentResolver);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if(choiseContacts != null) {
                for (int i = 0; i < contacts.size(); ++i) {
                    if (choiseContacts.get(contacts.get(i).getPhone()) != null) {
                        contacts.get(i).setChecked(true);
                        ++checkCount;
                    }
                }
            }
            adapter.update(contacts);
            Log.i("size", ": " + contacts.size());
        }
    }

    private boolean compareString(String main, String search) {
        if (search.length() > main.length()) {
            return false;
        }
        for (int i = 0; i < search.length(); ++i) {
            if (!(main.charAt(i) == search.charAt(i))) {
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
                    if (!success) {
                        view.showMessage(errorSaveDb);
                    }
                }
            }
            calculateShare(count, settings);
            return null;
        }
    }
}
