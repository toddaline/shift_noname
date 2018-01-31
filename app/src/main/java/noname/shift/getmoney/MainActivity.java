package noname.shift.getmoney;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    Button getMoneyButton;
    EditText cardNumberText;
    EditText sumText;
    static final int REQUEST_CODE_PERMISSION_READ_CONTACTS = 0;
    static final int CARD_NUMBER_LENGTH = 16;

    private SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getMoneyButton = findViewById(R.id.getMoneyButton);
        cardNumberText = findViewById(R.id.cardNumber);
        sumText = findViewById(R.id.sumText);
        getMoneyButton.setEnabled(false);

        settings = getSharedPreferences(SharedPreferencesConstants.APP_PREFERENCES, Context.MODE_PRIVATE);

        getMoneyButton.setOnClickListener(view -> {

            // Запоминаем данные
            SharedPreferences.Editor editor = settings.edit();
            editor.putString(SharedPreferencesConstants.APP_PREFERENCES_CARD_NUMBER, cardNumberText.getText().toString());
            editor.putInt(SharedPreferencesConstants.APP_PREFERENCES_SUM, Integer.parseInt(sumText.getText().toString()));
            editor.apply();

            Intent intent = new Intent(MainActivity.this, ContactsActivity.class);
            startActivity(intent);
        });
        checkPermissions();
        cardNumberText.addTextChangedListener(textWatcher);
        sumText.addTextChangedListener(textWatcher);
    }

    private void checkPermissions() {
        int permissionStatus = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS);

        if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
        //    readContacts();
        } else {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_CONTACTS},
                    REQUEST_CODE_PERMISSION_READ_CONTACTS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_PERMISSION_READ_CONTACTS:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission granted
                //    readContacts();
                } else {
                    // permission denied
                }
        }
    }

    TextWatcher textWatcher = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            if (checkFields()) {
                getMoneyButton.setEnabled(true);
            } else {
                getMoneyButton.setEnabled(false);
            }
        }
    };

    boolean checkFields() {
        return cardNumberText.getText().length() == CARD_NUMBER_LENGTH
                && sumText.getText().length() != 0;
    }
}
