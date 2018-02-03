package noname.shift.getmoney;

import android.Manifest;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
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

import noname.shift.getmoney.models.Contact;
import noname.shift.getmoney.models.ListDbHelper;
import noname.shift.getmoney.presenters.ContactsAdapter;
import noname.shift.getmoney.presenters.ContactsHolder;
import noname.shift.getmoney.presenters.ContactsPresenter;
import noname.shift.getmoney.presenters.SharedPreferencesConstants;
import noname.shift.getmoney.views.ContactsView;


public class ContactsActivity extends AppCompatActivity implements ContactsView {
    private static final int REQUEST_CODE_PERMISSION_READ_CONTACTS = 0;
    private FloatingActionButton button;
    private RecyclerView recyclerView;
    private RVAdapter adapter;
    private ContactsPresenter contactsPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_contacts);


        Toolbar toolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        button = findViewById(R.id.fab);

        contactsPresenter = new ContactsPresenter(this, new ListDbHelper(this));
        recyclerView = findViewById(R.id.recycler_view_all_contacts);
        adapter = new RVAdapter();
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(adapter);
        button.setVisibility(View.INVISIBLE);

        if (checkPermissions()) {
            Log.i("table", "smth happened");
            if(savedInstanceState != null) {
                contactsPresenter.loadStateBoundle(savedInstanceState);
            }
            contactsPresenter.loadContacts(adapter, this.getContentResolver());
        }

        button.setOnClickListener(view -> {
            contactsPresenter.choiseContact(
                    getSharedPreferences(SharedPreferencesConstants.APP_PREFERENCES, Context.MODE_PRIVATE));
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (outState != null) {
            contactsPresenter.saveStateBoundle(outState);
        }
    }


    private boolean checkPermissions() {
        int permissionStatus = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS);
        if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS},
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
                    contactsPresenter.loadContacts(adapter, this.getContentResolver());
                } else {
                    finish();
                }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        SearchManager searchManager = (SearchManager) getSystemService(this.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(
                new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String s) {
                        contactsPresenter.submitText(s);
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String s) {
                        contactsPresenter.changeText(s);
                        return false;
                    }
                }
        );

        return true;
    }

    @Override
    public void goTargetContact() {
        Intent intent = new Intent(ContactsActivity.this, ListActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }


    @Override
    public void setVisibility() {
        button.setVisibility(View.VISIBLE);
    }

    @Override
    public void showMessage(String message) {
        Toast.makeText(ContactsActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void resetAdapter(ArrayList<Contact> items) {
        LinearLayoutManager verticalLinearLayoutManager;
        verticalLinearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(verticalLinearLayoutManager);

        RVAdapter adapter = new RVAdapter();
        recyclerView.setAdapter(adapter);
        adapter.update(items);
    }

    private class RVAdapter extends RecyclerView.Adapter<RVAdapter.ContactViewHolder> implements ContactsAdapter {

        private ArrayList<Contact> adapterContacts;

        @Override
        public ContactViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false);
            return new ContactViewHolder(v);
        }

        @Override
        public void update(ArrayList<Contact> contacts) {
            adapterContacts = contacts;
            notifyDataSetChanged();
        }


        @Override
        public void onBindViewHolder(ContactViewHolder holder, int position) {
            Log.i("position", Integer.toString(position));

            contactsPresenter.bindViewHolder(adapterContacts, holder, position);
        }


        @Override
        public int getItemCount() {
            if (adapterContacts == null) {
                return 0;
            } else {
                return adapterContacts.size();
            }
        }

        protected class ContactViewHolder extends RecyclerView.ViewHolder implements ContactsHolder {
            private CardView cardView;
            private CheckedTextView name;
            private TextView number;

            ContactViewHolder(View itemView) {
                super(itemView);
                cardView = itemView.findViewById(R.id.card);
                name = itemView.findViewById(R.id.name);
                number = itemView.findViewById(R.id.number);

                cardView.setOnClickListener(view -> {
                    contactsPresenter.changeContactState(this, name.getText().toString(), name.isChecked(), cardView);
                });
            }

            @Override
            public void setLabel(boolean check) {
                name.setChecked(check);
                cardView.setSelected(check);
            }

            @Override
            public void setNumber(String numbers) {
                number.setText(numbers);
            }

            @Override
            public void setName(String text) {
                name.setText(text);
            }
        }
    }


}
