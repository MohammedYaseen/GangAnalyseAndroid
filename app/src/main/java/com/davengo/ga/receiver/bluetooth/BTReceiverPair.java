package com.davengo.ga.receiver.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.os.AsyncTask;
import android.util.Log;

import com.davengo.ga.common.CommonContext;
import com.davengo.ga.common.configuration.Insole.HandSide;
import com.davengo.ga.receiver.Receiver;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by malkhameri on 06.08.2015.
 */
public class BTReceiverPair implements Receiver {

    //private static final Log log = LogFactory.getLog(BTReceiverPair.class);

    private HashSet<BTReceiver> btReceiverSet;
    private HashMap<BluetoothDevice, HandSide> deviceStringMap;
    private Activity activity;

    private final String CONNECTION_ERROR_TITEL = "Verbindungsfehler";
    private final String CONNECTION_ERROR_HEAD = "Ein Verbindungsproblem wurde festgestellt";
    private CommonContext commonContext;

    public BTReceiverPair(HashMap<BluetoothDevice, HandSide
            > deviceMap, Activity activity) {
        super();
        this.deviceStringMap = deviceMap;
        commonContext = CommonContext.getInstance();
        btReceiverSet = new HashSet<BTReceiver>();
        this.activity = activity;
    }

    @Override
    public void start() {

        Log.i("", "open BT connection");
        // BottomPane.showMessage("Bluetooth Verbindung wird hergestellt");
        showToast("Bluetooth Verbindung wird hergestellt");

        new StartReceiverTask().execute("");

/*        for (Map.Entry<BluetoothDevice, HandSide> entry : deviceStringMap.entrySet()) {
            if(entry.getValue() == HandSide.RIGHT){
                BTConnection btConnection = new BTConnection(
                        entry.getKey());
                BTReceiver btReceiver = new BTReceiver(entry.getValue(),
                        btConnection, BTReceiverPair.this, activity);
                btReceiver.start();
                btReceiver.waitForReady();
            }

            // btReceiverSet.add(btReceiver);

        }*/

    }

    private void btConnectionErrorMessage() {
        showToast("Beim Verbindungsaufbau ist ein Fehler aufgetreten!");
/*        Init.showErrorAlert(CONNECTION_ERROR_TITEL, CONNECTION_ERROR_HEAD,
                "Stellen Sie sicher das Ihre Messgeräte verbindungsbereit sind.");
        BottomPane
                .showMessage("Beim Verbindungsaufbau ist ein Fehler aufgetreten!");*/
    }

    public void isConnected(BTReceiver btReceiver) {
        btReceiverSet.add(btReceiver);
        showToast("Verbindung wurde hergestellt");

/*        if (btReceiverSet.size() == 2) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    context.getStateHolder().getReceiverStateProperty()
                            .set(ReceiverState.ON);
                    log.info("BT connected");
                    BottomPane.showMessage("Verbindung wurde hergestellt");
                }
            });
        }*/
    }

    public void isDisconnected(BTReceiver btReceiver) {
        btReceiverSet.remove(btReceiver);
        showToast("Verbindung getrennt");
        if (commonContext.getAnalyzer() != null)
            commonContext.getAnalyzer().shutdown();
/*        if (btReceiverSet.isEmpty()) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    context.getStateHolder().getReceiverStateProperty()
                            .set(ReceiverState.OFF);
                    BottomPane.showMessage("Verbindung getrennt");
                    log.info("BT connection closed");

                }
            });
            if (context.getAnalyzer() != null)
                context.getAnalyzer().shutdown();
            BlueCoveImpl.shutdown();
        }*/
    }

    @Override
    public void shutdown() {
        Iterator<BTReceiver> iterator = btReceiverSet.iterator();
        while (iterator.hasNext()) {
            iterator.next().shutdown();
        }

/*        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                context.getStateHolder().getReceiverStateProperty()
                        .set(ReceiverState.OFF);
            }
        });*/

    }

    private void showToast(String message) {

        //Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    private class StartReceiverTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            for (Map.Entry<BluetoothDevice, HandSide> entry : deviceStringMap.entrySet()) {
                if (entry.getValue() == HandSide.RIGHT) {
                    BTConnection btConnection = new BTConnection(
                            entry.getKey());
                    BTReceiver btReceiver = new BTReceiver(entry.getValue(),
                            btConnection, BTReceiverPair.this, activity);
                    btReceiver.start();
                    //btReceiver.waitForReady();
                }

                // btReceiverSet.add(btReceiver);

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

