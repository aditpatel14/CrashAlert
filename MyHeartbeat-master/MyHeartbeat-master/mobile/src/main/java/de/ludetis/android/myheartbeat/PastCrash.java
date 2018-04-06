package de.ludetis.android.myheartbeat;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class PastCrash extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_past_crash);

        String info = FileIO.readFromRecords(this);
        updateTextView(R.id.records, info);

    }


    public void updateTextView(int text_id, String toThis) {
        TextView val = (TextView) findViewById(text_id);
        val.setText(toThis);
        return;
    }


}
