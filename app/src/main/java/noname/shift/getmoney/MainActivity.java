package noname.shift.getmoney;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    Button getMoneyButton;
    EditText cardNumberText;
    EditText sumText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getMoneyButton = findViewById(R.id.getMoneyButton);
        cardNumberText = findViewById(R.id.cardNumber);
        sumText = findViewById(R.id.sumText);

        getMoneyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }
}
