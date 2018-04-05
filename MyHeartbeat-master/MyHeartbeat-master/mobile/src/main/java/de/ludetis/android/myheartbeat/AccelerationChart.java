package de.ludetis.android.myheartbeat;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

/**
 * Created by Adit on 2018-03-27.
 */

public class AccelerationChart implements Runnable {
    private boolean drawChart = true;
    private Handler handler;

    public AccelerationChart(Handler handler) {
        this.handler = handler;
    }

    @Override
    public void run() {
        while (drawChart) {
            Double acceleration1;
            Double acceleration2;
            Double acceleration3;

            try {
                Thread.sleep(100); // Speed up the X axis
                acceleration1 = MainActivity.accelerationQueue1.poll();
                acceleration2 = MainActivity.accelerationQueue2.poll();
                acceleration3 = MainActivity.accelerationQueue3.poll();

            } catch (InterruptedException e) {
                e.printStackTrace();
                continue;
            }
            if (acceleration1 == null)
                continue;

            // currentX value will be excced the limit of double type range
            // To overcome this problem comment of this line
            // currentX = (System.currentTimeMillis() / 1000) * 8 + 0.6;

            Message msgObj = handler.obtainMessage();
            Bundle b = new Bundle();
            b.putString("ACCELERATION_VALUE", String.valueOf(acceleration1 + "%%" + acceleration2 + "%%" + acceleration3));
            msgObj.setData(b);
            handler.sendMessage(msgObj);

        }
    }
}