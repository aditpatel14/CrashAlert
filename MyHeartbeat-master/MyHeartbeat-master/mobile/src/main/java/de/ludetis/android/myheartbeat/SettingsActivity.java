package de.ludetis.android.myheartbeat;

import android.app.Activity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

public class SettingsActivity extends Activity {

    public static String emergencyNumber = "";
    public static String emergencyName = "";

    private EditText enterNum;
    private EditText enterName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        enterName = (EditText) findViewById(R.id.enterName);
        enterNum = (EditText) findViewById(R.id.enterNumber);

        /*
        1. get name and number from textviews and save
        2. save the name and number to settings file(seperate files)

        3. in emergency activity, add name and number to contact info
        4. add conditional for sending text or not

         */

    }
}
