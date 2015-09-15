package com.davengo.ga;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.davengo.ga.common.CommonContext;
import com.davengo.ga.common.CommonInit;
import com.davengo.ga.common.configuration.Insole.HandSide;
import com.davengo.ga.common.util.AndroidUtil;
import com.davengo.ga.receiver.bluetooth.BTReceiverPair;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;


public class MainActivity extends Activity {
    private TextView mStatusTv;
    private Button mActivateBtn;
    private Button mPairedBtn;
    private Button mScanBtn;
    private Button settingsBtn;
    private Button connectDevicesBtn;
    private Button disconnectDevicesBtn;
    private Button recordingBtn;


    private ProgressDialog mProgressDlg;

    private ArrayList<BluetoothDevice> mDeviceList = new ArrayList<BluetoothDevice>();

    private HashMap<BluetoothDevice, HandSide> deviceStringMap = new HashMap<BluetoothDevice, HandSide>();

    private BluetoothAdapter mBluetoothAdapter;

    private CommonInit logic;

    private Context context;
    private Activity activity;

    private static boolean appStarted;
    private Set<BluetoothDevice> pairedDevices;
    private CommonContext commonContext;
    private BTReceiverPair btReceiverPair;
    private boolean devicesConnected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity = this;
        setContentView(R.layout.activity_main);
        File appDir = new File(Environment.getExternalStorageDirectory() + "/GangAnalyse");
        String dirPath = appDir.getPath();
        commonContext = CommonContext.getInstance();

        boolean success = true;
        if (!appDir.exists()) {
            success = appDir.mkdir();
        }
        if (success) {
            // Do something on success
        } else {
            // Do something else on failure
        }


        if (!appStarted) {
            new AppInitialize().execute("");
        }

        mStatusTv = (TextView) findViewById(R.id.tv_status);
        mActivateBtn = (Button) findViewById(R.id.btn_enable);
        mPairedBtn = (Button) findViewById(R.id.btn_view_paired);
        mScanBtn = (Button) findViewById(R.id.btn_scan);
        settingsBtn = (Button) findViewById(R.id.btn_setting);
        connectDevicesBtn = (Button) findViewById(R.id.btn_connect_devices);
        disconnectDevicesBtn = (Button) findViewById(R.id.btn_disconnect_devices);
        recordingBtn = (Button) findViewById(R.id.btn_record);

        refreshButtonsView();

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mProgressDlg = new ProgressDialog(this);

        mProgressDlg.setCancelable(false);
        mProgressDlg.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                mBluetoothAdapter.cancelDiscovery();
            }
        });

        if (mBluetoothAdapter == null) {
            showUnsupported();
        } else {
            mPairedBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

                    if (pairedDevices == null || pairedDevices.size() == 0) {
                        showToast("Keine gekoppelte Geräte gefunden");
                    } else {
                        ArrayList<BluetoothDevice> list = new ArrayList<BluetoothDevice>();

                        list.addAll(pairedDevices);

                        Intent intent = new Intent(MainActivity.this, DeviceListActivity.class);

                        intent.putParcelableArrayListExtra("device.list", list);

                        startActivity(intent);
                    }
                }
            });

            mScanBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    mBluetoothAdapter.startDiscovery();
                }
            });

            mActivateBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mBluetoothAdapter.isEnabled()) {
                        mBluetoothAdapter.disable();

                        showDisabled();
                    } else {
                        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

                        startActivityForResult(intent, 1000);
                    }
                }
            });

            connectDevicesBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    pairedDevices = mBluetoothAdapter.getBondedDevices();
                    if (pairedDevices == null || pairedDevices.size() == 0) {
                        showToast("Keine gekoppelte Geräte gefunden ...");
                    } else {
                        // Connecting devices by starting the receivers
                        ArrayList<BluetoothDevice> list = new ArrayList<BluetoothDevice>();
                        for (BluetoothDevice device : pairedDevices) {
                            if (device.getAddress().replace(":", "").equals(commonContext.getConfiguration().getInsoles().getRightInsole().getBluetoothID())) {
                                deviceStringMap.put(device, HandSide.RIGHT);
                            } else if (device.getAddress().replace(":", "").equals(commonContext.getConfiguration().getInsoles().getLeftInsole().getBluetoothID())) {
                                deviceStringMap.put(device, HandSide.LEFT);
                            }
                        }
                        btReceiverPair = new BTReceiverPair(deviceStringMap, activity);
                        btReceiverPair.start();
                        devicesConnected = true;
                        refreshButtonsView();
                    }
                }
            });
            disconnectDevicesBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (btReceiverPair != null) {
                        btReceiverPair.shutdown();
                        devicesConnected = false;
                        refreshButtonsView();
                    }
                }
            });

            settingsBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                    startActivity(intent);
                }
            });

            if (mBluetoothAdapter.isEnabled()) {
                showEnabled();
            } else {
                showDisabled();
            }
            recordingBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, RecordingActivity.class);
                    startActivity(intent);
                }
            });
        }

        IntentFilter filter = new IntentFilter();

        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        registerReceiver(mReceiver, filter);
    }

    @Override
    public void onPause() {
        if (mBluetoothAdapter != null) {
            if (mBluetoothAdapter.isDiscovering()) {
                mBluetoothAdapter.cancelDiscovery();
            }
        }

        super.onPause();
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(mReceiver);
        if (btReceiverPair != null) {
            btReceiverPair.shutdown();
            devicesConnected = false;
        }
        super.onDestroy();
    }

    private void showEnabled() {
        mStatusTv.setText("Bluetooth ist AN");
        mStatusTv.setTextColor(Color.BLUE);

        mActivateBtn.setText("Deaktivieren");
        mActivateBtn.setEnabled(true);

        mPairedBtn.setEnabled(true);
        mScanBtn.setEnabled(true);
        settingsBtn.setEnabled(true);
        refreshButtonsView();

    }

    private void showDisabled() {
        mStatusTv.setText("Bluetooth ist AUS");
        mStatusTv.setTextColor(Color.RED);

        mActivateBtn.setText("Aktivieren");
        mActivateBtn.setEnabled(true);

        mPairedBtn.setEnabled(false);
        mScanBtn.setEnabled(false);
        settingsBtn.setEnabled(false);
        refreshButtonsView();
        connectDevicesBtn.setEnabled(false);
    }

    private void showUnsupported() {
        mStatusTv.setText("Bluetooth ist nicht unterstützt in diesem Gerät ..");

        mActivateBtn.setText("Aktivieren");
        mActivateBtn.setEnabled(false);

        mPairedBtn.setEnabled(false);
        mScanBtn.setEnabled(false);
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

                if (state == BluetoothAdapter.STATE_ON) {
                    showToast("Enabled");

                    showEnabled();
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                mDeviceList = new ArrayList<BluetoothDevice>();
                mProgressDlg.setMessage("Scanen ...");
                mProgressDlg.show();
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                mProgressDlg.dismiss();
                Intent newIntent = new Intent(MainActivity.this, DeviceListActivity.class);
                newIntent.putParcelableArrayListExtra("device.list", mDeviceList);
                newIntent.putExtra("device.map", (Serializable) deviceStringMap);

                startActivity(newIntent);
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mDeviceList.add(device);
                deviceStringMap.put(device, null);
                showToast("Gerät gefunden " + device.getName());
            }
        }
    };

    private class AppInitialize extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mProgressDlg = new ProgressDialog(activity);
                    mProgressDlg.setMessage("App Initialisieren ...");
                    mProgressDlg.setCancelable(false);
                    mProgressDlg.show();
                }
            });
            // Init Call
            logic = new CommonInit();
            AndroidUtil.setAndroidAppContext(context);
            logic.init();
            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
            mProgressDlg.dismiss();
            appStarted = true;
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }
    private void refreshButtonsView(){
        if(devicesConnected){
            connectDevicesBtn.setEnabled(false);
            disconnectDevicesBtn.setEnabled(true);
            recordingBtn.setEnabled(true);
        } else {
            connectDevicesBtn.setEnabled(true);
            disconnectDevicesBtn.setEnabled(false);
            recordingBtn.setEnabled(false);
        }
    }
}


