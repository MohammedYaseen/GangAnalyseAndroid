package com.davengo.ga.receiver.bluetooth;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.davengo.ga.common.CommonContext;
import com.davengo.ga.common.applicationStates.CalibrationStateProperty;
import com.davengo.ga.common.applicationStates.CalibrationStateProperty.CalibrationState;
import com.davengo.ga.common.byteDecoder.ByteDecoder;
import com.davengo.ga.common.byteDecoder.Data;
import com.davengo.ga.common.byteDecoder.Decoder;
import com.davengo.ga.common.byteDecoder.objects.SensorData;
import com.davengo.ga.common.calibrator.Calibrator;
import com.davengo.ga.common.configuration.Insole.HandSide;
import com.davengo.ga.common.receiver.DataSwitch;
import com.davengo.ga.common.util.BaseThread;
import com.davengo.ga.receiver.Receiver;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by malkhameri on 06.08.2015.
 */
public class BTReceiver extends BaseThread implements Receiver {

    private String TAG = "BTReceiver";
    //private Context context;
    private BTReceiverPair btReceiverPair;
    protected static Context context;
    private Activity activity;
    private CommonContext commonContext;
    private Calibrator calibrator;
    private HandSide handSide;
    private BTConnection connection;
    private InputStream inStreamOne;
    private Decoder copyOfDecoder = new ByteDecoder();
    private Decoder decoder = new ByteDecoder();
    private Listener dataListener;

    private final String CONNECTION_ERROR_TITEL = "Verbindungs Fehler";
    private final String CONNECTION_ERROR_HEAD = "Ein Verbindungsproblem wurde festgestellt";
    private DataSwitch dataSwitch;
    private boolean headerFound;

    public static interface Listener {
        public void handleSensorData(SensorData sensorData, HandSide side);
    }


    public BTReceiver(HandSide handSide, BTConnection connection,
                      BTReceiverPair btReceiverPair, Activity activity) {
        this.handSide = handSide;
        this.connection = connection;
        this.btReceiverPair = btReceiverPair;
        this.activity = activity;
        this.context = activity.getApplicationContext();
    }


    @Override
    protected void beforeRun() {
        commonContext = CommonContext.getInstance();
        /*
         * create calibrator if left and right CalibrationTable exist
		 */
        if (commonContext.getStateHolder().getCalibrationStateProperty().get() == CalibrationState.READY) {
            try {
                calibrator = new Calibrator(
                        commonContext.getLeftCalibrationTable(),
                        commonContext.getRightCalibrationTable(), commonContext
                        .getConfiguration().getUsedSensors());
            } catch (Calibrator.CalibratorException e) {
                e.printStackTrace();
            }
            Log.i(TAG, "Started Calibrator");
        } else if (commonContext.getStateHolder().getCalibrationStateProperty()
                .get() == CalibrationStateProperty.CalibrationState.NOT_READY) {
            showToast("Warnung Sensor Kalibrierung nicht möglich, Es wurden keine Kalibrierungsdaten hinterlegt. Bitte hinterlegen Sie Kalibrierungsdaten in den Einstellungen");

            Log.w(TAG, "no calibrationTable found. no calibration possible");

            btReceiverPair.shutdown();
            return;
            // TODO throw exception for shutdown other BaseThreads???
        } else if (commonContext.getStateHolder().getCalibrationStateProperty()
                .get() == CalibrationState.INVALID) {
            showToast("Warnung Sensor Kalibrierung nicht möglich Die hinterlegten Kalibrierungsdaten kÃ¶nnen nicht verwendet werden. Bitte Ã¼berprÃ¼fen Sie die Kalibrierungsdaten");
            Log.w(TAG, "calibrationTable invalid. no calibration possible");
            btReceiverPair.shutdown();
            return;
        }

		/*
         * open BT-Connection
		 */
        Log.i(TAG, "open BT connection");

        try {
            connection.openBtConnection();
            inStreamOne = connection.getDataInputStream();

        } catch (IOException e) {
            btConnectionErrorMessage();
            Log.e(TAG, "open BT InputStream failed: ", e);
            btReceiverPair.shutdown();
            pause();
            return;
        }

		/*
         * create DataSwitch
		 */
        dataSwitch = new DataSwitch(commonContext, calibrator);

        if (isConnected()) {
            btReceiverPair.isConnected(this);
            Log.i(TAG, handSide.toString() + " BT is connected");
        } else {
            beforeRun();
        }

    }

    @Override
    protected void cycle() {
        // Log.i(TAG, "cycle " + TAG);
        //new ReceivingDataTask().execute();
        int sleepTime = 100;
        int tmp = 0;
        try {
            if (inStreamOne != null) {
                DataInputStream dataInputStream = new DataInputStream(inStreamOne);
                byte[] header = new byte[2];


                while (header[0] != 0xAA && header[1] != 0x55) {
                    tmp++;
                    dataInputStream.read(header);
/*                    if (header[0] == 0x10 || header[1] == 0x10) {
                        break;
                    } else {
                        dataInputStream.read(header);
                    }*/
                }

                int length = dataInputStream.readUnsignedShort();
                //byte[] buffer = new byte[length + 1];

                //int length = inStreamOne.available();

                byte[] buffer = new byte[length + 1]; // buffer store for the stream
                int bytes = dataInputStream.read(buffer);// bytes returned from read()

                Data data = decoder.decode(buffer);

                // Log.d(TAG, "decoded data = " + data);
                // Data dataOne = (SensorData) data;

                dataSwitch.put(data, handSide);

/*                if (sensorData != null && sensorData instanceof SensorData) {

                    ((Listener) activity).handleSensorData(sensorData, handSide);

                    Log.i("Data .......", ">>> TEST_OUTPUT: ");
                    Log.i("Data .......", "DataToString: " + sensorData.toString());
                    Log.i("Data .......", "Type: " + sensorData.getType() + "\n");
                    Log.i("Data .......", ">CHECK DATA:");
                    Log.i("Data .......", "Time: " + sensorData.getTime());
                    Log.i("Data .......", "SensorData: " + handSide.toString()
                            + Arrays.toString(sensorData.getSensorData()));
                    Log.i("Data .......", "Sensor13: " + sensorData.getSensorValue(13));
                }*/
            }
            //dataSwitch.put(dataOne, handSide);
        } catch (IOException e1) {
            Log.e(TAG, "read BT inputdatastream failed: ", e1);
        }
/*        int diff = sleepTime - tmp;
        if (diff > 0) {
            sleepSafe(diff);
        }*/
        //Log.i(TAG, "diff >>>>>>>> " + diff);
        //sleepSafe(100);
    }

    @Override
    protected void afterRun() {
        try {
            if (connection != null) {
                connection.closeBTConnection();
                connection.closeDataInputStream();
                Log.d(TAG, handSide.toString() + " BT is disconnected");
                btReceiverPair.isDisconnected(this);
            }
        } catch (IOException e) {
            Log.e(TAG, handSide.toString() + " disconnect BT failed: ", e);
        }

        // if (!isConnected()) {
        //
        // } else {
        // // afterRun();
        // }

    }

    public boolean isConnected() {
        return connection.isConnected();
    }

    @Override
    protected String getThreadName() {
        return "BT-Receiver " + handSide.toString();
    }

    @Override
    public synchronized void shutdown() {
        this.pause();
        super.shutdown();
    }

    private void btConnectionErrorMessage() {
        showToast("Beim Verbindungsaufbau ist ein Fehler aufgetreten!");
/*        Init.showErrorAlert(CONNECTION_ERROR_TITEL, CONNECTION_ERROR_HEAD,
                "Stellen Sie sicher das Ihre Messgeräte verbindungsbereit sind.");
        BottomPane
                .showMessage("Beim Verbindungsaufbau ist ein Fehler aufgetreten!");*/
    }

    private void showToast(final String message) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private class ReceivingDataTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                if (inStreamOne != null) {
                    DataInputStream dataInputStream = new DataInputStream(inStreamOne);
                    byte[] header = new byte[2];
                    while (header[0] != 0xAA && header[1] != 0x55) {
                        dataInputStream.read(header);
/*                    if(header[0] == 0x10 || header[1] == 0x10){
                        break;
                    } else {
                        dataInputStream.read(header);
                    }*/
                    }
                    int length = dataInputStream.readUnsignedShort();
                    //byte[] buffer = new byte[length + 1];

                    //int length = inStreamOne.available();

                    byte[] buffer = new byte[length + 1]; // buffer store for the stream
                    int bytes = dataInputStream.read(buffer);// bytes returned from read()

                    Data data = decoder.decode(buffer);

                    // Log.d(TAG, "decoded data = " + data);
                    // Data dataOne = (SensorData) data;

                    dataSwitch.put(data, handSide);

/*                if (sensorData != null && sensorData instanceof SensorData) {

                    ((Listener) activity).handleSensorData(sensorData, handSide);

                    Log.i("Data .......", ">>> TEST_OUTPUT: ");
                    Log.i("Data .......", "DataToString: " + sensorData.toString());
                    Log.i("Data .......", "Type: " + sensorData.getType() + "\n");
                    Log.i("Data .......", ">CHECK DATA:");
                    Log.i("Data .......", "Time: " + sensorData.getTime());
                    Log.i("Data .......", "SensorData: " + handSide.toString()
                            + Arrays.toString(sensorData.getSensorData()));
                    Log.i("Data .......", "Sensor13: " + sensorData.getSensorValue(13));
                }*/
                }
                //dataSwitch.put(dataOne, handSide);
            } catch (IOException e1) {
                Log.e(TAG, "read BT inputdatastream failed: ", e1);
            }

            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {

        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }
}

