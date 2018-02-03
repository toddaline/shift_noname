package noname.shift.getmoney.presenters;

import android.content.ContentResolver;
import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;

import noname.shift.getmoney.R;
import noname.shift.getmoney.models.Contact;
import noname.shift.getmoney.models.ContactReader;
import noname.shift.getmoney.models.ListDbHelper;
import noname.shift.getmoney.views.TargetContactsView;

public class TargetContactsPresenters {
    private TargetContactsView view;
    private ListDbHelper listDbHelper;
    private ArrayList<Contact> contacts = new ArrayList<>();
    private int checkCount = 0;

    public TargetContactsPresenters(TargetContactsView view, ListDbHelper listDbHelper){
        this.view = view;
        this.listDbHelper = listDbHelper;
    }

    public void loadContacts(ContactsAdapter adapter){
        LoadAsyncTask task = new LoadAsyncTask(adapter);
        task.execute();
    }

    public void deleteData(){
        listDbHelper.deleteTable();
    }
    public void bindViewHolder( ContactsHolder holder, int position){
        Contact target = contacts.get(position);

        holder.setLabel(target.isChecked());
        holder.setName(target.getName());
        holder.setNumber(target.getPhone());
        Log.i("arraySize", Integer.toString(contacts.size()));
    }

    public int getItemCount() {
        if(contacts != null) {
            return contacts.size();
        } else{
            return 0;
        }
    }

    public void pressButton(ContactsHolder holder, boolean status, int position) {
        UpdateAsyncTask asyncTask;
        if (status) {
            contacts.get(position).setChecked(false);
            holder.setLabel(false);
            asyncTask = new UpdateAsyncTask(position +1 ,false);
            asyncTask.execute();
            --checkCount;
        } else {
            contacts.get(position).setChecked(true);
            holder.setLabel(true);
            asyncTask = new UpdateAsyncTask(position +1 ,true);
            asyncTask.execute();
            ++checkCount;
        }
        if (checkCount == contacts.size()) {
            view.setButtonText(R.string.button_new_list);
        } else {
            view.setButtonText(R.string.send_message);
        }
    }

    public void sendMessage() {
        String number = "smsto:" + selectNumbers();
        Log.i("numbers", number);
        view.goSendMessage(number);
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

    private class LoadAsyncTask extends AsyncTask<Void, Void, Void> {

        private ContactsAdapter adapter;

        public LoadAsyncTask(ContactsAdapter adapter) {
            this.adapter = adapter;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            contacts = listDbHelper.displayDatabaseInfo();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            for(Contact contact: contacts){
                if(contact.isChecked()){
                    ++checkCount;
                }
            }
            adapter.update(contacts);
            Log.i("size", ": " + contacts.size());
        }
    }

    private class UpdateAsyncTask extends AsyncTask<Void, Void, Void> {

        private int position;
        private boolean checked;
        public UpdateAsyncTask(int position, boolean checked) {
            this.position = position;
            this.checked = checked;

        }

        @Override
        protected Void doInBackground(Void... voids) {
            listDbHelper.updateDatabase(position, checked);
            return null;
        }
    }

}
