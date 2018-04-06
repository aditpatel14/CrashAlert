package de.ludetis.android.myheartbeat;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


public class EmergencyController extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;

    private Location mLastLocation;
    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;

    // UI elements
    private TextView lblLocation;
    double latitude = 0.0;
    double longitude = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_controller);

        // Update Emergency Contact Info from files to screen---------------------
        SettingsActivity.emergencyName = FileIO.readEmergencyContactFromFile(this);
        SettingsActivity.emergencyNumber = FileIO.readEmergencyPhoneFromFile(this);
        updateTextView(R.id.emergencycontactinfo, SettingsActivity.emergencyName + "\n" + SettingsActivity.emergencyNumber);


        //Appends crash information to local data base----------------------------
        Date c = Calendar.getInstance().getTime();
        System.out.println("Current time => " + c);

        SimpleDateFormat df = new SimpleDateFormat("MMM-dd-yyyy HH:mm:ss");
        String formattedDate = df.format(c);

        String pastRecords = FileIO.readFromRecords(this);
        String newRecord =
                "\n CRASH AT: " + formattedDate + "\n"
                        + "\nVolume: " + MainActivity.volumeExpertAtCrash + "\n"
                        + "\nAcc: " + (MainActivity.accExpertAtCrash + "").substring(0, 10) + "\n"
                        + "\nHeart Rate: " + MainActivity.heartExpertAtCrash + "\n\n\n\n";

        FileIO.writeToRecords(newRecord + "\n" + pastRecords, this);

        updateTextView(R.id.crashinfo, newRecord);


        //Add user location-----------------------------------------------------
        lblLocation = (TextView) findViewById(R.id.location);
        // First we need to check availability of play services
        if (checkPlayServices()) {
            // Building the GoogleApi client
            buildGoogleApiClient();
        }
        displayLocation();


    }

    public void updateTextView(int text_id, String toThis) {
        TextView val = (TextView) findViewById(text_id);
        val.setText(toThis);
        return;
    }

    private void sendEmergencyText() {
        if (SettingsActivity.allowTextingEmergency == true && SettingsActivity.emergencyNumber.length() == 10) {
            Toast.makeText(getBaseContext(), "Sending SOS Text", Toast.LENGTH_SHORT).show();
            sendSms(SettingsActivity.emergencyNumber, "TESTING: Emergency @ Lat:" + latitude + " Long" + longitude);
        } else {
            Toast.makeText(getBaseContext(), "Sending SOS Text Not Sent, change settings", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Method to display the location on UI
     */
    private void displayLocation() {

        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (mLastLocation != null) {
            latitude = mLastLocation.getLatitude();
            longitude = mLastLocation.getLongitude();

            lblLocation.setText("Lat: " + latitude + "\n Long: " + longitude);
            sendEmergencyText();
        } else {
            lblLocation.setText("(Couldn't get the location. Make sure location is enabled on the device)");
        }
    }

    /**
     * Creating google api client object
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    /**
     * Method to verify google play services on the device
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(getApplicationContext(),
                        "This device is not supported.", Toast.LENGTH_LONG)
                        .show();
                finish();
            }
            return false;
        }
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        checkPlayServices();
    }

    /**
     * Google api callback methods
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = "
                + result.getErrorCode());
    }

    @Override
    public void onConnected(Bundle arg0) {

        // Once connected with google api, get the location
        displayLocation();
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        mGoogleApiClient.connect();
    }

    private void sendSms(String phonenumber, String message) {
        SmsManager manager = SmsManager.getDefault();

//        PendingIntent piSend = PendingIntent.getBroadcast(this, 0, new Intent(SMS_SENT), 0);
//        PendingIntent piDelivered = PendingIntent.getBroadcast(this, 0, new Intent(SMS_DELIVERED), 0);

        int length = message.length();

        if (length > 160) {
            ArrayList<String> messagelist = manager.divideMessage(message);

            manager.sendMultipartTextMessage(phonenumber, null, messagelist, null, null);
        } else {
            manager.sendTextMessage(phonenumber, null, message, null, null);
        }

    }


}
