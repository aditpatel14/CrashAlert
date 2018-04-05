package de.ludetis.android.myheartbeat;

import android.os.Handler;
import android.os.Message;

import com.jjoe64.graphview.series.DataPoint;
import static de.ludetis.android.myheartbeat.MainActivity.currentX;

/**
 * Created by Adit on 2018-03-27.
 */

public class AccelerationChartHandler extends Handler {
    @Override
    public void handleMessage(Message msg) {
        Double acceleration1 = 0.0D;
        Double acceleration2 = 0.0D;
        Double acceleration3 = 0.0D;

        if (!msg.getData().getString("ACCELERATION_VALUE").equals(null) && !msg.getData().getString("ACCELERATION_VALUE").equals("null")) {
            String string = msg.getData().getString("ACCELERATION_VALUE");

            String[] parts = string.split("%%");
            acceleration1 = Double.parseDouble(parts[0]);
            acceleration2 = Double.parseDouble(parts[1]);
            acceleration3 = Double.parseDouble(parts[2]);


//                acceleration1 = (Double.parseDouble(msg.getData().getString("ACCELERATION_VALUE")));
        }
        MainActivity.series1.appendData(new DataPoint(currentX, acceleration1), true, 30);
        MainActivity.series2.appendData(new DataPoint(currentX, acceleration2), true, 30);
        MainActivity.series3.appendData(new DataPoint(currentX, acceleration3), true, 30);


        currentX = currentX + 1;

    }
}