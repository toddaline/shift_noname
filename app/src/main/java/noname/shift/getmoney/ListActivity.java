package noname.shift.getmoney;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import java.util.Calendar;

import noname.shift.getmoney.models.Contact;
import noname.shift.getmoney.models.ListDbHelper;
import noname.shift.getmoney.presenters.ContactsAdapter;
import noname.shift.getmoney.presenters.ContactsHolder;
import noname.shift.getmoney.presenters.SharedPreferencesConstants;
import noname.shift.getmoney.presenters.TargetContactsPresenters;
import noname.shift.getmoney.views.TargetContactsView;

public class ListActivity extends AppCompatActivity implements TargetContactsView{
    private static String messageText;
    private static String smsBody = "sms_body";

    private Button button;
    private SharedPreferences settings;
    private TargetContactsPresenters presenters;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_target_contacts);
        button = findViewById(R.id.button_send);

        RecyclerView recyclerView = findViewById(R.id.recycler_view__target_contacts);

        RVAdapter adapter = new RVAdapter();
        presenters = new TargetContactsPresenters(this, new ListDbHelper(this));
        presenters.loadContacts(adapter);
        button.setText(R.string.send_message);


        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(adapter);

        settings = getSharedPreferences(SharedPreferencesConstants.APP_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(SharedPreferencesConstants.APP_PREFERENCES_HAS_TABLE, true);
        editor.apply();

        button.setOnClickListener(view -> {
            if (button.getText().toString().equals(getResources().getString(R.string.send_message))) {
                initMessage();
                presenters.sendMessage();
            } else {
                presenters.deleteData();
                settings = getSharedPreferences(SharedPreferencesConstants.APP_PREFERENCES, Context.MODE_PRIVATE);
                SharedPreferences.Editor newEditor = settings.edit();
                newEditor.putBoolean(SharedPreferencesConstants.APP_PREFERENCES_HAS_TABLE, false);
                newEditor.apply();
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void initMessage() {
        messageText = "Привет! Скидываемся по "
                + settings.getInt(SharedPreferencesConstants.APP_PREFERENCES_AVERAGE_SUM, 0)
                + " рублей на карту "
                + settings.getString(SharedPreferencesConstants.APP_PREFERENCES_CARD_NUMBER, "");
    }

    @Override
    public void goSendMessage(String number) {
        Intent sms = new Intent(Intent.ACTION_SENDTO, Uri.parse(number));

        sms.putExtra(smsBody, messageText);
        String chooser_message = "Choose application to send message";
        startActivity(Intent.createChooser(sms, chooser_message));
    }

    @Override
    public void setButtonText(String text) {
        button.setText(text);
    }

    @Override
    public void setButtonText(int idText) {
        button.setText(idText);
    }

    private class RVAdapter extends RecyclerView.Adapter<RVAdapter.ContactViewHolder> implements ContactsAdapter {

        @Override
        public void update(ArrayList<Contact> contacts) {

        }

        @Override
        public ContactViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false);
            return new ContactViewHolder(v);
        }

        @Override
        public void onBindViewHolder(RVAdapter.ContactViewHolder holder, int position) {
            Log.i("position", Integer.toString(position));
            holder.setPozition(position);
            presenters.bindViewHolder(holder, position);
        }

        @Override
        public int getItemCount() {
            return presenters.getItemCount();
        }

        protected class ContactViewHolder extends RecyclerView.ViewHolder implements ContactsHolder {
            private CardView cardView;
            private CheckedTextView name;
            private TextView number;
            private int pozition;

            ContactViewHolder(View itemView) {
                super(itemView);
                cardView = itemView.findViewById(R.id.card);
                name = itemView.findViewById(R.id.name);
                number = itemView.findViewById(R.id.number);

                cardView.setOnClickListener(view -> {
                    presenters.pressButton(this, name.isChecked(), pozition);
                });
            }

            void setPozition(int pozition){
                this.pozition = pozition;
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
