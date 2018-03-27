package project.a3a04.CrashAlert;

import android.Manifest;
import android.app.Activity;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity {

    private static String volumeVisual = "";
    private Handler handler;
    private SoundMeter mSensor;
//    private TextView volumeLevel, status;


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Toast.makeText(getBaseContext(), "Loading...", Toast.LENGTH_LONG).show();
        setContentView(R.layout.activity_main);


        // Check for permissions
        int permission = ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        // If we don't have permissions, ask user for permissions
        if (permission != PackageManager.PERMISSION_GRANTED) {
            String[] PERMISSIONS_STORAGE = {
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.RECORD_AUDIO

            };
            int REQUEST_EXTERNAL_STORAGE = 1;

            ActivityCompat.requestPermissions(
                    MainActivity.this,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }



//        //Initialize views
//        TextView volumeLevel = (TextView) findViewById(R.id.volumeLevel);
//        TextView status = (TextView) findViewById(R.id.status);
//        TextView volumeBars = (TextView) findViewById(R.id.volumeBars);


        // Sound-based code
        mSensor = new SoundMeter();

        try {
            mSensor.start();
            Toast.makeText(getBaseContext(), "Sound sensor initiated.", Toast.LENGTH_SHORT).show();
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        handler = new Handler();

        final Runnable r = new Runnable() {

            public void run() {
                //mSensor.start();
                Log.d("Amplify","HERE");
                Toast.makeText(getBaseContext(), "Working!", Toast.LENGTH_LONG).show();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Get the volume from 0 to 255 in 'int'
                        double volume = 10 * mSensor.getTheAmplitude() / 32768;
                        int volumeToSend = (int) volume;
                        updateTextView(R.id.volumeLevel, "Volume: " + String.valueOf(volumeToSend));

                        volumeVisual = "";
                        for( int i=0; i<volumeToSend; i++){
                            volumeVisual += "|";
                        }

                        updateTextView(R.id.volumeBars, "Volume: " + String.valueOf(volumeVisual));
                        Log.d("Amplify",String.valueOf(volumeToSend));
                        updateTextView(R.id.status, "Showing Volume Levels:");
                        handler.postDelayed(this, 250); // amount of delay between every cycle of volume level detection + sending the data  out
                    }
                });
            }
        };

        // Is this line necessary? --- YES IT IS, or else the loop never runs
        // this tells Java to run "r"
        handler.postDelayed(r, 250);

    }

    @Override
    public void onResume() {
        super.onResume();

        updateTextView(R.id.status, "On resume, need to initiate sound sensor.");
        // Sound based code
        try {
            mSensor.start();
            Toast.makeText(getBaseContext(), "Sound sensor initiated.", Toast.LENGTH_SHORT).show();
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            Toast.makeText(getBaseContext(), "On resume, sound sensor messed up...", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        updateTextView(R.id.status, "Paused.");
        super.onPause();
    }

    public void updateTextView(int text_id, String toThis) {
        TextView val = (TextView) findViewById(text_id);
        val.setText(toThis);
        return;
    }


}