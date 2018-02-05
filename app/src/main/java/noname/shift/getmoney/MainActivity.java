package noname.shift.getmoney;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import io.card.payment.CardIOActivity;
import io.card.payment.CreditCard;
import noname.shift.getmoney.presenters.SharedPreferencesConstants;
import ru.tinkoff.decoro.MaskImpl;
import ru.tinkoff.decoro.slots.PredefinedSlots;
import ru.tinkoff.decoro.watchers.FormatWatcher;
import ru.tinkoff.decoro.watchers.MaskFormatWatcher;

public class MainActivity extends AppCompatActivity {

    private static final int CARD_NUMBER_LENGTH = 19;
    private static final int MY_SCAN_REQUEST_CODE = 10;
    private static final String ERROR_SCAN = "Can't scan card";

    private Button getMoneyButton;
    private EditText cardNumberText;
    private EditText sumText;
    private SharedPreferences settings;
    private MaskImpl mask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getMoneyButton = findViewById(R.id.getMoneyButton);
        cardNumberText = findViewById(R.id.cardNumber);
        sumText = findViewById(R.id.sumText);
        getMoneyButton.setEnabled(false);

        Toolbar toolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        mask = MaskImpl.createTerminated(PredefinedSlots.CARD_NUMBER_STANDART_MASKABLE);
        FormatWatcher formatWatcher = new MaskFormatWatcher(mask);
        formatWatcher.installOn(cardNumberText);

        settings = getSharedPreferences(SharedPreferencesConstants.APP_PREFERENCES, Context.MODE_PRIVATE);

        getMoneyButton.setOnClickListener(view -> {

            SharedPreferences.Editor editor = settings.edit();
            editor.putString(SharedPreferencesConstants.APP_PREFERENCES_CARD_NUMBER, cardNumberText.getText().toString());
            editor.putInt(SharedPreferencesConstants.APP_PREFERENCES_SUM, Integer.parseInt(sumText.getText().toString()));
            editor.apply();
            Intent intent = new Intent(MainActivity.this, ContactsActivity.class);
            startActivity(intent);
        });

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

        cardNumberText.addTextChangedListener(textWatcher);
        sumText.addTextChangedListener(textWatcher);
    }

    //    @Override
//    public boolean onOptionsItemSelected(MenuItem item){
//
//
//        return super.onOptionsItemSelected(item);
//    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_card, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if(item.getItemId() == R.id.camera_item){
            Intent scanIntent = new Intent(this, CardIOActivity.class);

            // customize these values to suit your needs.
            scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_EXPIRY, false); // default: false
            scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_CVV, false); // default: false
            scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_POSTAL_CODE, false); // default: false

            // MY_SCAN_REQUEST_CODE is arbitrary and is only used within this activity.
            startActivityForResult(scanIntent, MY_SCAN_REQUEST_CODE);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == MY_SCAN_REQUEST_CODE) {

            if (data != null && data.hasExtra(CardIOActivity.EXTRA_SCAN_RESULT)) {
                CreditCard scanResult = data.getParcelableExtra(CardIOActivity.EXTRA_SCAN_RESULT);

                // Never log a raw card number. Avoid displaying it, but if necessary use getFormattedCardNumber()
                String resultDisplayStr = scanResult.getFormattedCardNumber();
                Log.i("library", resultDisplayStr);
                cardNumberText.setText(resultDisplayStr);


            } else {
                Toast.makeText(MainActivity.this, ERROR_SCAN, Toast.LENGTH_SHORT).show();
            }
        }
    }



    boolean checkFields() {
        return cardNumberText.getText().toString().length() == CARD_NUMBER_LENGTH
                && sumText.getText().length() != 0;
    }
}
