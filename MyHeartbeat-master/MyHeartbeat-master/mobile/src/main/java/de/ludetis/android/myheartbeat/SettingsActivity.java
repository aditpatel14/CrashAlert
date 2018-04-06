package de.ludetis.android.myheartbeat;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsActivity extends Activity {

    public static String emergencyNumber = "";
    public static String emergencyName = "";

    private EditText enterNum;
    private EditText enterName;
    private Button mButton;
    private Boolean contactInfoUpdated = false;
    public static Boolean allowTextingEmergency = false;

    CheckBox yourCheckBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dealwithit();

    }

    @Override
    public void onResume() {
        super.onResume();
    }


    public void dealwithit(){
        setContentView(R.layout.activity_settings);
        yourCheckBox = (CheckBox) findViewById (R.id.checkBoxCallEmergency);
        enterName = (EditText) findViewById(R.id.enterName);
        enterNum = (EditText) findViewById(R.id.enterNumber);

        if(allowTextingEmergency) {
            Toast.makeText(getBaseContext(), "Text allowed", Toast.LENGTH_SHORT).show();
            yourCheckBox.setChecked(true);
        }
        else{
            Toast.makeText(getBaseContext(), "Text NOT allowed", Toast.LENGTH_SHORT).show();
            yourCheckBox.setChecked(false);
        }


        //saves previously loaded data from file onto edit text
        enterName.setText(emergencyName);
        enterNum.setText(emergencyNumber);


        //if updated, changes the values and writes tofile
        mButton = (Button) findViewById(R.id.saveSettings);

        mButton.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View view) {
                        emergencyNumber = enterNum.getText().toString();
                        emergencyName = enterName.getText().toString();

//                    contactInfoUpdated = true;
                        Toast.makeText(getBaseContext(), "Save Pressed", Toast.LENGTH_SHORT).show();

                    }
                });

        //writes emergency info to file
        FileIO.writeEmergencyContactToFile(emergencyName, this);
        FileIO.writeEmergencyPhoneToFile(emergencyNumber, this);


        //deals with checkbox
        yourCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((CheckBox) v).isChecked()) {
                    allowTextingEmergency = true;
                    Toast.makeText(getBaseContext(), "Checked", Toast.LENGTH_SHORT).show();

                }
                else{
                    allowTextingEmergency = false;
                    Toast.makeText(getBaseContext(), "Unchecked", Toast.LENGTH_SHORT).show();
                }
            }
        });



//        allowTextingEmergency = ((CheckBox) findViewById(R.id.checkBoxCallEmergency)).isChecked();
    }


}
