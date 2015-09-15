package com.davengo.ga;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.davengo.ga.audio.Tone;
import com.davengo.ga.audio.Tone.Note;
import com.davengo.ga.common.byteDecoder.objects.SensorData;
import com.davengo.ga.common.configuration.Insole.HandSide;
import com.davengo.ga.receiver.bluetooth.BTReceiver;
import com.davengo.ga.receiver.bluetooth.BTReceiverPair;
import com.davengo.ga.util.MinMax;

import java.util.HashMap;
import java.util.Map;


public class ViewDataActivity extends Activity implements BTReceiver.Listener {

    private HashMap<BluetoothDevice, HandSide> deviceStringMap;

    private BTReceiverPair btReceiverPair;

    private TextView tvSensor1RightSide;
    private TextView tvSensor2RightSide;
    private TextView tvSensor3RightSide;
    private TextView tvSensor4RightSide;
    private TextView tvSensor5RightSide;
    private TextView tvSensor6RightSide;
    private TextView tvSensor7LeftSide;
    private TextView tvSensor8LeftSide;
    private TextView tvSensor9LeftSide;
    private TextView tvSensor10LeftSide;
    private TextView tvSensor11LeftSide;
    private TextView tvSensor12LeftSide;
    private TextView tvSensor13LeftSide;
    private TextView tvSensor14LeftSide;
    private TextView tvSensor15LeftSide;
    private TextView tvSensor16LeftSide;
    private TextView tvSensor17LeftSide;
    private TextView tvSensor18LeftSide;
    private TextView tvSensor19LeftSide;

    private TextView tvSensor1LeftSide;
    private TextView tvSensor2LeftSide;
    private TextView tvSensor3LeftSide;
    private TextView tvSensor4LeftSide;
    private TextView tvSensor5LeftSide;
    private TextView tvSensor6LeftSide;
    private TextView tvSensor7RightSide;
    private TextView tvSensor8RightSide;
    private TextView tvSensor9RightSide;
    private TextView tvSensor10RightSide;
    private TextView tvSensor11RightSide;
    private TextView tvSensor12RightSide;
    private TextView tvSensor13RightSide;
    private TextView tvSensor14RightSide;
    private TextView tvSensor15RightSide;
    private TextView tvSensor16RightSide;
    private TextView tvSensor17RightSide;
    private TextView tvSensor18RightSide;
    private TextView tvSensor19RightSide;
    AudioTrack audioTrack;
    int buffsize;


    private Note[] NotesArray = Note.values();
    //{Note..A4, Note.A4$, Note.B4, Note.C4, Note.C4$, Note.D4, Note.D4$, Note.E4, Note.F4, Note.F4$, Note.G4, Note.G4$, Note.A5, Note.A5$, Note.B5, Note.C5, Note.C5$, Note.D5, Note.D5$, Note.E5, Note.F5, Note.F5$, Note.G5, Note.G5$, Note.A6, Note.A6$, Note.B6, Note.C6, Note.C6$, Note.D6, Note.D6$, Note.E6, Note.F6, Note.F6$, Note.G6, Note.G6$, Note.A7, Note.A7$, Note.B7, Note.C7, Note.C7$, Note.D7, Note.D7$, Note.E7, Note.F7, Note.F7$, Note.G7, Note.G7$, Note.A8, Note.A8$, Note.B8, Note.C8, Note.C8$, Note.D8, Note.D8$, Note.E8, Note.F8, Note.F8$, Note.G8, Note.G8$};
    private Map<MinMax, Note> averageAudioMap = new HashMap<MinMax, Note>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_data);
        double min = 25;
        double max = 92;
        MinMax minMax;
        for (Note note : NotesArray) {
            if(note.name().equals("REST")) continue;
            minMax = new MinMax(min, max);
            averageAudioMap.put(minMax, note);
            min = min + 67;
            max = max + 67;
        }
        // set the buffer size
        buffsize = AudioTrack.getMinBufferSize(Tone.Note.SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                Tone.Note.SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, buffsize,
                AudioTrack.MODE_STREAM);


        tvSensor1RightSide = (TextView) findViewById(R.id.tVSensor1RightSide);
        tvSensor2RightSide = (TextView) findViewById(R.id.tVSensor2RightSide);
        tvSensor3RightSide = (TextView) findViewById(R.id.tVSensor3RightSide);
        tvSensor4RightSide = (TextView) findViewById(R.id.tVSensor4RightSide);
        tvSensor5RightSide = (TextView) findViewById(R.id.tVSensor5RightSide);
        tvSensor6RightSide = (TextView) findViewById(R.id.tVSensor6RightSide);
        tvSensor7RightSide = (TextView) findViewById(R.id.tVSensor7RightSide);
        tvSensor8RightSide = (TextView) findViewById(R.id.tVSensor8RightSide);
        tvSensor9RightSide = (TextView) findViewById(R.id.tVSensor9RightSide);
        tvSensor10RightSide = (TextView) findViewById(R.id.tVSensor10RightSide);
        tvSensor11RightSide = (TextView) findViewById(R.id.tVSensor11RightSide);
        tvSensor12RightSide = (TextView) findViewById(R.id.tVSensor12RightSide);
        tvSensor13RightSide = (TextView) findViewById(R.id.tVSensor13RightSide);
        tvSensor14RightSide = (TextView) findViewById(R.id.tVSensor14RightSide);
        tvSensor15RightSide = (TextView) findViewById(R.id.tVSensor15RightSide);
        tvSensor16RightSide = (TextView) findViewById(R.id.tVSensor16RightSide);
        tvSensor17RightSide = (TextView) findViewById(R.id.tVSensor17RightSide);
        tvSensor18RightSide = (TextView) findViewById(R.id.tVSensor18RightSide);
        tvSensor19RightSide = (TextView) findViewById(R.id.tVSensor19RightSide);

        tvSensor1LeftSide = (TextView) findViewById(R.id.tVSensor1LeftSide);
        tvSensor2LeftSide = (TextView) findViewById(R.id.tVSensor2LeftSide);
        tvSensor3LeftSide = (TextView) findViewById(R.id.tVSensor3LeftSide);
        tvSensor4LeftSide = (TextView) findViewById(R.id.tVSensor4LeftSide);
        tvSensor5LeftSide = (TextView) findViewById(R.id.tVSensor5LeftSide);
        tvSensor6LeftSide = (TextView) findViewById(R.id.tVSensor6LeftSide);
        tvSensor7LeftSide = (TextView) findViewById(R.id.tVSensor7LeftSide);
        tvSensor8LeftSide = (TextView) findViewById(R.id.tVSensor8LeftSide);
        tvSensor9LeftSide = (TextView) findViewById(R.id.tVSensor9LeftSide);
        tvSensor10LeftSide = (TextView) findViewById(R.id.tVSensor10LeftSide);
        tvSensor11LeftSide = (TextView) findViewById(R.id.tVSensor11LeftSide);
        tvSensor12LeftSide = (TextView) findViewById(R.id.tVSensor12LeftSide);
        tvSensor13LeftSide = (TextView) findViewById(R.id.tVSensor13LeftSide);
        tvSensor14LeftSide = (TextView) findViewById(R.id.tVSensor14LeftSide);
        tvSensor15LeftSide = (TextView) findViewById(R.id.tVSensor15LeftSide);
        tvSensor16LeftSide = (TextView) findViewById(R.id.tVSensor16LeftSide);
        tvSensor17LeftSide = (TextView) findViewById(R.id.tVSensor17LeftSide);
        tvSensor18LeftSide = (TextView) findViewById(R.id.tVSensor18LeftSide);
        tvSensor19LeftSide = (TextView) findViewById(R.id.tVSensor19LeftSide);

        deviceStringMap = (HashMap<BluetoothDevice, HandSide>) getIntent().getSerializableExtra("device.map");
        btReceiverPair = new BTReceiverPair(deviceStringMap, this);
        btReceiverPair.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_view_data, menu);
        return true;
    }

    @Override
    public void onDestroy() {
        if (btReceiverPair != null) {
            btReceiverPair.shutdown();
        }
        if (audioTrack != null) {
            audioTrack.stop();
            audioTrack.release();
        }

        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void handleSensorData(final SensorData sensorData, final HandSide side) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                double average = calculateSensorValuesAverage(sensorData);
                Log.d("", "Average "+ side + " " + average);
                playTone(average);
                if (side == HandSide.RIGHT) {
                    outputSensorRight(sensorData);
                } else if (side == HandSide.LEFT) {
                    outputSensorLeft(sensorData);
                }
            }
        });


    }

    /**
     * print out the results
     */
    public void outputSensorRight(SensorData input) {
        if (input != null) {

            tvSensor1RightSide.setText(String.valueOf(input.getSensorValue(1)));
            tvSensor2RightSide.setText(String.valueOf(input.getSensorValue(2)));
            tvSensor3RightSide.setText(String.valueOf(input.getSensorValue(3)));
            tvSensor4RightSide.setText(String.valueOf(input.getSensorValue(4)));
            tvSensor5RightSide.setText(String.valueOf(input.getSensorValue(5)));
            tvSensor6RightSide.setText(String.valueOf(input.getSensorValue(6)));
            tvSensor7RightSide.setText(String.valueOf(input.getSensorValue(7)));
            tvSensor8RightSide.setText(String.valueOf(input.getSensorValue(8)));
            tvSensor9RightSide.setText(String.valueOf(input.getSensorValue(9)));
            tvSensor10RightSide.setText(String.valueOf(input.getSensorValue(19)));
            tvSensor11RightSide.setText(String.valueOf(input.getSensorValue(11)));
            tvSensor12RightSide.setText(String.valueOf(input.getSensorValue(12)));
            tvSensor13RightSide.setText(String.valueOf(input.getSensorValue(13)));
            tvSensor14RightSide.setText(String.valueOf(input.getSensorValue(14)));
            tvSensor15RightSide.setText(String.valueOf(input.getSensorValue(15)));
            tvSensor16RightSide.setText(String.valueOf(input.getSensorValue(16)));
            tvSensor17RightSide.setText(String.valueOf(input.getSensorValue(17)));
            tvSensor18RightSide.setText(String.valueOf(input.getSensorValue(18)));
            tvSensor19RightSide.setText(String.valueOf(input.getSensorValue(19)));
        }

    }

    /**
     * print out the results
     */
    public void outputSensorLeft(SensorData input) {
        if (input != null) {

            tvSensor1LeftSide.setText(String.valueOf(input.getSensorValue(1)));
            tvSensor2LeftSide.setText(String.valueOf(input.getSensorValue(2)));
            tvSensor3LeftSide.setText(String.valueOf(input.getSensorValue(3)));
            tvSensor4LeftSide.setText(String.valueOf(input.getSensorValue(4)));
            tvSensor5LeftSide.setText(String.valueOf(input.getSensorValue(5)));
            tvSensor6LeftSide.setText(String.valueOf(input.getSensorValue(6)));
            tvSensor7LeftSide.setText(String.valueOf(input.getSensorValue(7)));
            tvSensor8LeftSide.setText(String.valueOf(input.getSensorValue(8)));
            tvSensor9LeftSide.setText(String.valueOf(input.getSensorValue(9)));
            tvSensor10LeftSide.setText(String.valueOf(input.getSensorValue(19)));
            tvSensor11LeftSide.setText(String.valueOf(input.getSensorValue(11)));
            tvSensor12LeftSide.setText(String.valueOf(input.getSensorValue(12)));
            tvSensor13LeftSide.setText(String.valueOf(input.getSensorValue(13)));
            tvSensor14LeftSide.setText(String.valueOf(input.getSensorValue(14)));
            tvSensor15LeftSide.setText(String.valueOf(input.getSensorValue(15)));
            tvSensor16LeftSide.setText(String.valueOf(input.getSensorValue(16)));
            tvSensor17LeftSide.setText(String.valueOf(input.getSensorValue(17)));
            tvSensor18LeftSide.setText(String.valueOf(input.getSensorValue(18)));
            tvSensor19LeftSide.setText(String.valueOf(input.getSensorValue(19)));
        }

    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    private void playTone(double sensorValuesAverage) {
        Note note = null;

        Tone t;
        for (Map.Entry<MinMax, Note> entry : averageAudioMap.entrySet()) {
            if (sensorValuesAverage > entry.getKey().getMin() && sensorValuesAverage < entry.getKey().getMax()) {
                note = entry.getValue();
                break;
            }
        }
        if (note != null) {
            t = new Tone(note, 100);
            Log.d("","Note >>>> "+ note.name().toString());
            t.play(audioTrack, t.note, t.ms);
        }

    }

    private double calculateSensorValuesAverage(SensorData sensorData) {
        double sum = 0;
        for (int i = 1; i < 20; i++) {
            sum = sum + sensorData.getSensorValue(i);
        }
        return sum / 19;
    }

}
