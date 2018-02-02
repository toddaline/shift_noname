package noname.shift.getmoney;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import noname.shift.getmoney.presenters.SharedPreferencesConstants;

public class SplashActivity extends AppCompatActivity {

    private static final int DELAY = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences settings = getSharedPreferences(SharedPreferencesConstants.APP_PREFERENCES, Context.MODE_PRIVATE);
        boolean hasTable = settings.getBoolean(SharedPreferencesConstants.APP_PREFERENCES_HAS_TABLE, false);

        new Handler().postDelayed(() -> {
            if (hasTable) {
                Intent intent = new Intent(this, ListActivity.class);
                startActivity(intent);
                finish();
            } else {
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }, DELAY);
    }


}
