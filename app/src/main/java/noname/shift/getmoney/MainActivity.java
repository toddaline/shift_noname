package noname.shift.getmoney;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;

import noname.shift.getmoney.presenters.SharedPreferencesConstants;
import ru.tinkoff.decoro.Mask;
import ru.tinkoff.decoro.MaskImpl;
import ru.tinkoff.decoro.parser.UnderscoreDigitSlotsParser;
import ru.tinkoff.decoro.slots.PredefinedSlots;
import ru.tinkoff.decoro.slots.Slot;
import ru.tinkoff.decoro.watchers.FormatWatcher;
import ru.tinkoff.decoro.watchers.MaskFormatWatcher;

public class MainActivity extends AppCompatActivity {

    private static final int CARD_NUMBER_LENGTH = 19;

    private Button getMoneyButton;
    private EditText cardNumberText;
    private EditText sumText;
    private SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getMoneyButton = findViewById(R.id.getMoneyButton);
        cardNumberText = findViewById(R.id.cardNumber);
        sumText = findViewById(R.id.sumText);
        getMoneyButton.setEnabled(false);

        MaskImpl mask = MaskImpl.createTerminated(PredefinedSlots.CARD_NUMBER_STANDART_MASKABLE);
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


    boolean checkFields() {
        return cardNumberText.getText().toString().length() == CARD_NUMBER_LENGTH
                && sumText.getText().length() != 0;
    }
}
