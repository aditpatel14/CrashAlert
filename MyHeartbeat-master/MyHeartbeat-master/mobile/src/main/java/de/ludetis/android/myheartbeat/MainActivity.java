package de.ludetis.android.myheartbeat;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;


public class MainActivity extends  Activity implements SensorEventListener {

    private static String volumeVisual = "";
    private Handler handler;
    private SoundMeter mSensor;
    private TextView heartBeatsView;

    //expert measurements
    public int volumeExpert = -1;
    public int heartExpert = -1;
    public double accExpert = -1;
    public static int volumeExpertAtCrash = -1;
    public static int heartExpertAtCrash  = -1;
    public static double accExpertAtCrash  = -1;
    double accThreshold = 1;
    int volThreshold = 4;
    int heartThreshold = 70;
    public static boolean dialogTriggered = false;


    private SensorManager sensorManager;
    public static LineGraphSeries<DataPoint> series1;
    public static LineGraphSeries<DataPoint> series2;
    public static LineGraphSeries<DataPoint> series3;

    public static double currentX;
    private ThreadPoolExecutor liveChartExecutor;
    public static LinkedBlockingQueue<Double> accelerationQueue1X = new LinkedBlockingQueue<>(25);
    public static LinkedBlockingQueue<Double> accelerationQueue2Y = new LinkedBlockingQueue<>(25);
    public static LinkedBlockingQueue<Double> accelerationQueue3Z = new LinkedBlockingQueue<>(25);

    //Heartbeat Handler, gets updates from smart watch
    private Handler handlerHeart = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // message from API client! message from wear! The contents is the heartbeat.
            if(heartBeatsView != null) {
                heartBeatsView.setText(Integer.toString(msg.what));
                heartExpert = msg.what;
                checkAccidentLevels();
//                Toast.makeText(getApplicationContext(),Integer.toString(msg.what)+"",Toast.LENGTH_SHORT).show();
            }
        }
    };

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Toast.makeText(getBaseContext(), "Loading...", Toast.LENGTH_LONG).show();
        setContentView(R.layout.activity_main);

        // heart beat code----------------------------------------------------------------------
        heartBeatsView = (TextView) findViewById(R.id.heartbeat);


        // Sound-based code----------------------------------------------------------------------
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

                        //updates static expert
                        volumeExpert = volumeToSend;
                        updateTextView(R.id.volumeLevel, "Volume: " + String.valueOf(volumeToSend));
                        checkAccidentLevels();

                        //Updates volume bars
                        volumeVisual = "";
                        for( int i=0; i<volumeToSend; i++){
                            volumeVisual += "|";
                        }
                        updateTextView(R.id.volumeBars, "Volume: " + String.valueOf(volumeVisual));
                        Log.d("Amplify",String.valueOf(volumeToSend));
                        updateTextView(R.id.status, "VOLUME AMPLITUDE LEVELS:");
                        handler.postDelayed(this, 250); // amount of delay between every cycle of volume level detection + sending the data  out
                    }
                });
            }
        };

        // Is this line necessary? --- YES IT IS, or else the loop never runs
        // this tells Java to run "r"
        handler.postDelayed(r, 250);



        // accelration-based code----------------------------------------------------------------
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        GraphView graph = (GraphView) findViewById(R.id.graph);

        //add x to graph
        series1 = new LineGraphSeries<>();
        series1.setColor(Color.DKGRAY);
        graph.addSeries(series1);

        //add y to graph
        series2 = new LineGraphSeries<>();
        series2.setColor(Color.BLUE);
        graph.addSeries(series2);

        //add z to graph
        series3 = new LineGraphSeries<>();
        series3.setColor(Color.RED);
        graph.addSeries(series3);

        // activate horizontal zooming and scrolling
        graph.getViewport().setScalable(true);
        // activate horizontal scrolling
        graph.getViewport().setScrollable(true);
        // activate horizontal and vertical zooming and scrolling
        graph.getViewport().setScalableY(true);
        // activate vertical scrolling
        graph.getViewport().setScrollableY(true);

        // To set a fixed manual viewport use this:
        // set manual X bounds
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0.5);
        graph.getViewport().setMaxX(25.5);

        // set manual Y bounds
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(-25);
        graph.getViewport().setMaxY(25);

        currentX = 0;

        // Start chart thread
        liveChartExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
        if (liveChartExecutor != null) {
            liveChartExecutor.execute(new AccelerationChart(new AccelerationChartHandler()));
        }


    }

    public void checkAccidentLevels() {



        if(accExpert >= accThreshold && volumeExpert >= volThreshold && heartExpert >= heartThreshold ){

             volumeExpertAtCrash = volumeExpert;
             heartExpertAtCrash  = heartExpert;
             accExpertAtCrash  = accExpert;
//            Toast.makeText(getBaseContext(), "ACCIDENT!!!", Toast.LENGTH_LONG).show();

            if (dialogTriggered == false){
                dialogTriggered = true;

                AlertDialog dialog = new AlertDialog.Builder(this)
                        .setTitle("Accident False Alarm?")
                        .setMessage("Will Contact Emergency if no response in 7 seconds")
                        .setPositiveButton("Yes - THIS IS A FALSE ALARM", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialogTriggered = false;
                                Log.d("MainActivity", "YES - I'm Okay");
                            }
                        })
                        .setNegativeButton("No - Contact Emergency", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.d("MainActivity", "NO - Contact Emergency ");
                                openEmergencyController();


                            }
                        })
                        .create();
                dialog.setCanceledOnTouchOutside(false);
                dialog.setCancelable(false);
                dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    private static final int AUTO_DISMISS_MILLIS = 7000;
                    @Override
                    public void onShow(final DialogInterface dialog) {
                        final Button defaultButton = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE);
                        final CharSequence positiveButtonText = defaultButton.getText();
                        new CountDownTimer(AUTO_DISMISS_MILLIS, 100) {
                            @Override
                            public void onTick(long millisUntilFinished) {
                                defaultButton.setText(String.format(
                                        Locale.getDefault(), "%s (%d)",
                                        positiveButtonText,
                                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) + 1 //add one so it never displays zero
                                ));
                            }
                            @Override
                            public void onFinish() {
                                if (((AlertDialog) dialog).isShowing()) {
                                    dialog.dismiss();
                                    openEmergencyController();
                                }
                            }
                        }.start();
                    }
                });

                dialog.show();


            }


        }
    }

    public void openEmergencyController(){
        //creates intent to start new activity


        Intent myIntent = new Intent(this, EmergencyController.class);
        //starts emergency controller activity
        startActivity(myIntent);
        // closes current activity
//        this.finish();

    }

    @Override
    public void onResume() {
        super.onResume();
        Toast.makeText(getBaseContext(), "RESUMED", Toast.LENGTH_LONG).show();
        dialogTriggered = false;


        // heart beat code----------------------------------------------------------------------
        // register our handlerHeart with the DataLayerService. This ensures we get messages whenever the service receives something.
        DataLayerListenerService.setHandler(handlerHeart);

        // Sound-based code----------------------------------------------------------------------
//        updateTextView(R.id.status, "On resume, need to initiate sound sensor.");
        try {
            mSensor.start();
            Toast.makeText(getBaseContext(), "Sound sensor initiated.", Toast.LENGTH_SHORT).show();
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            Toast.makeText(getBaseContext(), "On resume, sound sensor messed up...", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

        // accelration-based code----------------------------------------------------------------
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);


        // check experts----------------------------------------------------------------
        checkAccidentLevels();

    }

    @Override
    public void onPause() {
        // unregister our handlerHeart so the service does not need to send its messages anywhere.
        DataLayerListenerService.setHandler(null);

        updateTextView(R.id.status, "Paused.");

        //this is prevent the app from opening crash dialog, when not on screen
        dialogTriggered = true;

        super.onPause();
        sensorManager.unregisterListener(this);

    }

    public void updateTextView(int text_id, String toThis) {
        TextView val = (TextView) findViewById(text_id);
        val.setText(toThis);
        return;
    }

    public void openPastCrash(View view){
        Intent intent = new Intent(this, PastCrash.class);
        startActivity(intent);
    }

    public void openSettings(View view){
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            getAccelerometer(sensorEvent);
        }
    }

    private void getAccelerometer(SensorEvent event) {
        float[] values = event.values;
        // Movement
        double x = values[0];
        double y = values[1];
        double z = values[2];

        double accelerationSquareRoot = (x * x + y * y + z * z) / (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH);
        double acceleration = Math.sqrt(accelerationSquareRoot);
        accExpert = acceleration;
        updateTextView(R.id.accLevel, "" + String.valueOf(acceleration).substring(0, 10));

        accelerationQueue1X.offer(x);
        accelerationQueue2Y.offer(y);
        accelerationQueue3Z.offer(z);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }


}


