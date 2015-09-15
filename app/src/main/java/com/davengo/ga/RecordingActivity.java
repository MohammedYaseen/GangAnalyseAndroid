package com.davengo.ga;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.davengo.ga.audio.Tone;
import com.davengo.ga.audio.Tone.Note;
import com.davengo.ga.common.CommonContext;
import com.davengo.ga.common.ControllerImpl;
import com.davengo.ga.common.analyzer.Analyzer;
import com.davengo.ga.common.applicationStates.ApplicationModeProperty;
import com.davengo.ga.common.applicationStates.RecordStateProperty;
import com.davengo.ga.common.byteDecoder.objects.SensorData;
import com.davengo.ga.common.configuration.Insole.HandSide;
import com.davengo.ga.common.csvTools.CSVExporter;
import com.davengo.ga.util.MinMax;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class RecordingActivity extends Activity implements RecordingThread.Listener {

    //private BTReceiverPair btReceiverPair;
    private Button recordingBtn;
    private boolean recordingStarted;
    private TextView rightSoleBLId;
    private TextView leftSoleBLId;
    private TextView rightSoleValuesAverage;
    private TextView leftSoleValuesAverage;
    CommonContext commonContext;
    AudioTrack audioTrack;
    int buffsize;
    private Tone.Note[] NotesArray = Tone.Note.values();
    private Map<MinMax, Note> averageAudioMap = new HashMap<MinMax, Note>();

    private HashMap<BluetoothDevice, HandSide> deviceStringMap = new HashMap<BluetoothDevice, HandSide>();


    private BluetoothAdapter mBluetoothAdapter;
    private Activity activity;
    private RecordingThread recordingThread;
    private ProgressDialog mProgressDlg;
    private Set<BluetoothDevice> pairedDevices;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recording);
        activity = this;
        commonContext = CommonContext.getInstance();

        commonContext.setController(new ControllerImpl());

        mProgressDlg = new ProgressDialog(this);

        double min = 25;
        double max = 92;
        MinMax minMax;
        for (Tone.Note note : NotesArray) {
            if (note.name().equals("REST")) continue;
            minMax = new MinMax(min, max);
            averageAudioMap.put(minMax, note);
            min = min + 67;
            max = max + 67;
        }
        // set the buffer size
        buffsize = AudioTrack.getMinBufferSize(Note.SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);

        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                Note.SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, buffsize,
                AudioTrack.MODE_STREAM);

        audioTrack.play();


/*        Date date = new Date();
        SimpleDateFormat ft = new SimpleDateFormat("yyyyMMdd_HHmm");
        String formattedDateTime = ft.format(date);*/

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        recordingBtn = (Button) findViewById(R.id.btn_record);
        rightSoleBLId = (TextView) findViewById(R.id.rightSoleBLId);
        leftSoleBLId = (TextView) findViewById(R.id.leftSoleBLId);
        rightSoleValuesAverage = (TextView) findViewById(R.id.rightSoleAverage);
        leftSoleValuesAverage = (TextView) findViewById(R.id.leftSoleAverage);

        rightSoleBLId.setText(commonContext.getConfiguration().getInsoles().getRightInsole().getBluetoothID());
        leftSoleBLId.setText(commonContext.getConfiguration().getInsoles().getLeftInsole().getBluetoothID());

        recordingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (recordingStarted) {
                    recordingStarted = false;
                    recordingBtn.setBackgroundColor(Color.GREEN);
                    recordingBtn.setText("Start");

/*                    mProgressDlg = new ProgressDialog(v.getContext());
                    mProgressDlg.setMessage("Daten werden gespeichert ...");
                    mProgressDlg.setCancelable(false);
                    mProgressDlg.show();*/

                    stopRecord();
                } else {
                    startRecord();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_recording, menu);
        return true;
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

    private void playTone(double sensorValuesAverage) {
        Note note = null;

        Tone tone;
        for (Map.Entry<MinMax, Tone.Note> entry : averageAudioMap.entrySet()) {
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

    private double calculateSensorValuesAverage(SensorData sensorData) {
        double sum = 0;
        for (int i = 1; i < 20; i++) {
            sum = sum + sensorData.getSensorValue(i);
        }
        return sum / 19;
    }

    /*    @Override
        public void handleSensorData(SensorData sensorData, final String side) {
            final double average = Math.round(calculateSensorValuesAverage(sensorData));
            playTone(average);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(side.equals("RIGHT")){
                        rightSoleValuesAverage.setText(String.valueOf(average));
                    } else if (side.equals("LEFT")){
                        leftSoleValuesAverage.setText(String.valueOf(average));
                    }
                }
            });


        }*/
    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void handleSensorData(final SensorData sensorData, final HandSide side) {
/*        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final double average = Math.round(calculateSensorValuesAverage(sensorData));
                playTone(average);
                if (side == HandSide.RIGHT) {
                    rightSoleValuesAverage.setText(String.valueOf(average));
                } else if (side == HandSide.LEFT) {
                    leftSoleValuesAverage.setText(String.valueOf(average));
                }
            }
        });*/
    }

    @Override
    public void handleSensorData2(final double sensorsAverage, final HandSide side) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (side == HandSide.RIGHT) {
                    rightSoleValuesAverage.setText(String.valueOf(sensorsAverage));
                } else if (side == HandSide.LEFT) {
                    leftSoleValuesAverage.setText(String.valueOf(sensorsAverage));
                }
            }
        });
    }

    private void startRecord() {
        pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices == null || pairedDevices.size() == 0) {
            showToast("No Paired Devices Found");
        } else {
            recordingStarted = true;
            recordingBtn.setText("Stop");
            recordingBtn.setBackgroundColor(Color.RED);
            commonContext.getStateHolder().getApplicationModeProperty().set(ApplicationModeProperty.ApplicationMode.ONLINE);
            commonContext.setCalibratedImport(false);

            new StartRecordingTask().execute();

/*            recordingThread = new RecordingThread(pairedDevices, activity);
            recordingThread.start();

            commonContext.setAnalyzer(new Analyzer());
            commonContext.getAnalyzer().start();
            commonContext.getAnalyzer().waitForReady();
            commonContext.getController().startRecord();*/

        }
    }

    private void stopRecord() {
        new StoreDataTask().execute();

        //commonContext.getController().stopRecord();
        //mProgressDlg.dismiss();
    }
    private class StoreDataTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mProgressDlg = new ProgressDialog(activity);
                    mProgressDlg.setMessage("Daten werden gespeichert ....");
                    mProgressDlg.setCancelable(false);
                    mProgressDlg.show();
                }
            });

            // ***************************************
/*            if (btReceiverPair != null) {
                btReceiverPair.shutdown();
            }*/
            // ***************************************

            if (recordingThread != null) {
                recordingThread.shutdown();
            }

            commonContext.getAnalyzer().shutdown();
            commonContext.getStateHolder().getRecordStateProperty()
                    .set(RecordStateProperty.RecordState.OFF);
            // Save recorded data into csv File
            Date date = new Date();
            SimpleDateFormat ft = new SimpleDateFormat("yyyyMMdd_HHmm");
            String formattedDateTime = ft.format(date);
            String filename = formattedDateTime + "_GA_Export" + ".csv";
            // TODO use appDir from MainActivity
            File appDir = new File(Environment.getExternalStorageDirectory() + "/GangAnalyse");
            File file = new File(appDir, filename);
            //ControllerImpl controller = new ControllerImpl();
            commonContext.getController().save(file, CSVExporter.ExportOption.FULLDATA);
            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
            mProgressDlg.dismiss();
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}
    }

    private class StartRecordingTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {

/*            // Connecting devices by starting the receivers
            ArrayList<BluetoothDevice> list = new ArrayList<BluetoothDevice>();
            for (BluetoothDevice device : pairedDevices) {
                if (device.getAddress().replace(":", "").equals(commonContext.getConfiguration().getInsoles().getRightInsole().getBluetoothID())) {
                    deviceStringMap.put(device, HandSide.RIGHT);
                } else if (device.getAddress().replace(":", "").equals(commonContext.getConfiguration().getInsoles().getLeftInsole().getBluetoothID())) {
                    deviceStringMap.put(device, HandSide.LEFT);
                }
            }
            btReceiverPair = new BTReceiverPair(deviceStringMap, activity);
            btReceiverPair.start();*/

            // Start Recording thread
            recordingThread = new RecordingThread(pairedDevices, activity);
            recordingThread.start();

            // Start Analyzer thread
            commonContext.setAnalyzer(new Analyzer());
            commonContext.getAnalyzer().start();
            //commonContext.getAnalyzer().waitForReady();
            commonContext.getController().startRecord();

            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
            mProgressDlg.dismiss();
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}
    }

}
