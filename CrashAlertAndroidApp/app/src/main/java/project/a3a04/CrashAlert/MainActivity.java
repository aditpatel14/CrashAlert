package project.a3a04.CrashAlert;

import android.Manifest;
import android.app.Activity;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;


public class MainActivity extends Activity implements SensorEventListener {

    private static String volumeVisual = "";
    private Handler handler;
    private SoundMeter mSensor;
//    private TextView volumeLevel, status;



    private SensorManager sensorManager;
    public static LineGraphSeries<DataPoint> series1;
    public static LineGraphSeries<DataPoint> series2;
    public static LineGraphSeries<DataPoint> series3;

    public static double currentX;
    private ThreadPoolExecutor liveChartExecutor;
    public static LinkedBlockingQueue<Double> accelerationQueue1 = new LinkedBlockingQueue<>(25);
    public static LinkedBlockingQueue<Double> accelerationQueue2 = new LinkedBlockingQueue<>(25);
    public static LinkedBlockingQueue<Double> accelerationQueue3 = new LinkedBlockingQueue<>(25);




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










        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        GraphView graph = (GraphView) findViewById(R.id.graph);

        series1 = new LineGraphSeries<>();
        series1.setColor(Color.DKGRAY);
        graph.addSeries(series1);

        series2 = new LineGraphSeries<>();
        series2.setColor(Color.BLUE);
        graph.addSeries(series2);

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
        graph.getViewport().setMinY(-50);
        graph.getViewport().setMaxY(50);

        currentX = 0;

        // Start chart thread
        liveChartExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
        if (liveChartExecutor != null)
            liveChartExecutor.execute(new AccelerationChart(new AccelerationChartHandler()));

//




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







        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onPause() {
        updateTextView(R.id.status, "Paused.");
        super.onPause();
        sensorManager.unregisterListener(this);

    }

    public void updateTextView(int text_id, String toThis) {
        TextView val = (TextView) findViewById(text_id);
        val.setText(toThis);
        return;
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

        accelerationQueue1.offer(x);
        accelerationQueue2.offer(y);
        accelerationQueue3.offer(z);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }


}