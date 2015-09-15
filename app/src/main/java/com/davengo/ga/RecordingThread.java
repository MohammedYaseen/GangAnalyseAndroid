package com.davengo.ga;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.util.Log;

import com.davengo.ga.audio.Tone;
import com.davengo.ga.audio.Tone.Note;
import com.davengo.ga.common.CommonContext;
import com.davengo.ga.common.analyzer.ResultData;
import com.davengo.ga.common.byteDecoder.objects.SensorData;
import com.davengo.ga.common.configuration.Insole.HandSide;
import com.davengo.ga.common.util.BaseThread;
import com.davengo.ga.receiver.bluetooth.BTReceiverPair;
import com.davengo.ga.util.MinMax;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

import static android.media.AudioManager.STREAM_MUSIC;

/**
 * Created by malkhameri on 25.08.2015.
 */
public class RecordingThread extends BaseThread {

    private String TAG = "RecordingThread";

    private Set<BluetoothDevice> pairedDevices;
    private CommonContext commonContext;
    private HashMap<BluetoothDevice, HandSide> deviceStringMap = new HashMap<BluetoothDevice, HandSide>();
    private BTReceiverPair btReceiverPair;
    private Activity activity;
    private LinkedBlockingQueue<ResultData> resultDataQueue;

    private Note[] NotesArray = Note.values();
    private Map<MinMax, Note> averageAudioMap = new HashMap<MinMax, Note>();
    AudioTrack audioTrack;
    int buffereSize;

    public static interface Listener {
        public void handleSensorData(SensorData sensorData, HandSide side);
        public void handleSensorData2(double sensorsAverage, HandSide side);
    }


    public RecordingThread(Set<BluetoothDevice> pairedDevices, Activity activity) {
        this.pairedDevices = pairedDevices;
        this.activity = activity;
    }

    @Override
    protected void cycle() {
        sleepSafe(100);
        Log.i(TAG, "cycle " + TAG);
        ResultData resultData = null;
        try {
            resultData = commonContext.getResultDataFromQueue();
/*            resultDataQueue = commonContext.getResultDataQueue();
            //int cap = resultDataQueue.remainingCapacity();
            if (resultDataQueue.size() > 0) {
                resultData = commonContext.getResultDataFromQueue();
            }*/
        } catch (InterruptedException e1) {
            Log.d(TAG, "InterruptedException: " + e1.getMessage());
            return;
        }
        if (resultData != null) {
            SensorData sensorData = resultData.getResultSensorData();
            if (sensorData != null) {
                double avg = Math.round(calculateSensorValuesAverage(sensorData));
                playTone(avg);
                ((Listener) activity).handleSensorData2(avg, sensorData.getSide());
                //((Listener) activity).handleSensorData(sensorData, sensorData.getSide());
                Log.i("Data .......", ">>> TEST_OUTPUT: ");
                Log.i("Data .......", "DataToString: " + sensorData.toString());
                Log.i("Data .......", "Type: " + sensorData.getType() + "\n");
                Log.i("Data .......", ">CHECK DATA:");
                Log.i("Data .......", "Time: " + sensorData.getTime());
                Log.i("Data .......", "SensorData: " + sensorData.getSide().toString()
                        + Arrays.toString(sensorData.getSensorData()));
                Log.i("Data .......", "Sensor13: " + sensorData.getSensorValue(13));
            }
        }
     //sleepSafe(100);
    }

    @Override
    protected void beforeRun() {
        commonContext = CommonContext.getInstance();

/*        ArrayList<BluetoothDevice> list = new ArrayList<BluetoothDevice>();
        for (BluetoothDevice device : pairedDevices) {
            if (device.getAddress().replace(":", "").equals(commonContext.getConfiguration().getInsoles().getRightInsole().getBluetoothID())) {
                deviceStringMap.put(device, HandSide.RIGHT);
            } else if (device.getAddress().replace(":", "").equals(commonContext.getConfiguration().getInsoles().getLeftInsole().getBluetoothID())) {
                deviceStringMap.put(device, HandSide.LEFT);
            }
        }
        btReceiverPair = new BTReceiverPair(deviceStringMap, activity);
        btReceiverPair.start();*/

        // ********************************************************
        double min = 25;
        double max = 92;
        MinMax minMax;
        for (Note note : NotesArray) {
            if (note.name().equals("REST")) continue;
            minMax = new MinMax(min, max);
            averageAudioMap.put(minMax, note);
            min = min + 67;
            max = max + 67;
        }
        // set the buffer size
        buffereSize = AudioTrack.getMinBufferSize(Note.SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);

        audioTrack = new AudioTrack(STREAM_MUSIC,
                Note.SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, buffereSize,
                AudioTrack.MODE_STREAM);

        audioTrack.play();
        // **************************************************************************

    }

    @Override
    protected void afterRun() {
        if (btReceiverPair != null) {
            btReceiverPair.shutdown();
        }
    }

    @Override
    protected String getThreadName() {
        return "RecordingThread";
    }

    private double calculateSensorValuesAverage(SensorData sensorData) {
        double sum = 0;
        for (int i = 1; i < 20; i++) {
            sum = sum + sensorData.getSensorValue(i);
        }
        return sum / 19;
    }
    private void playTone(double sensorValuesAverage) {
        Note note = null;
        Tone tone;
        for (Map.Entry<MinMax, Note> entry : averageAudioMap.entrySet()) {
            if (sensorValuesAverage > entry.getKey().getMin() && sensorValuesAverage < entry.getKey().getMax()) {
                note = entry.getValue();
                break;
            }
        }
        if (note != null) {
            tone = new Tone(note, 100);
            Log.d("", "Note >>>> " + note.name().toString());
            tone.play(audioTrack, tone.note, tone.ms);
        }

    }

}
