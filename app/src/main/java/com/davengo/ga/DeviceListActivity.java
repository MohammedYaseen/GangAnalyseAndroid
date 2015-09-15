package com.davengo.ga;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.davengo.ga.common.CommonContext;
import com.davengo.ga.common.configuration.ConfigUtil;
import com.davengo.ga.common.configuration.Insole.HandSide;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class DeviceListActivity extends Activity {
    private ListView mListView;
    private DeviceListAdapter mAdapter;
    private ArrayList<BluetoothDevice> mDeviceList;
    private Map<BluetoothDevice, HandSide> deviceStringMap = new HashMap<BluetoothDevice, HandSide>();
    private TextView rightSoleTextView;
    private TextView leftSoleTextView;
    private Button nextButton;
    private Context context;
    private String rightDevice, leftDevice;
    private CommonContext commonContext;
    private static String CONFIG_FILENAME = "config.xml";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_device_list);
        rightSoleTextView = (TextView) findViewById(R.id.rightSoleBLId);
        leftSoleTextView = (TextView) findViewById(R.id.leftSoleTextView);
        nextButton = (Button) findViewById(R.id.nextButton);

        context = this;
        commonContext = CommonContext.getInstance();

        rightSoleTextView.setText("Rechte Sohle: Nicht ausgewaehlt");
        leftSoleTextView.setText("Linke Sohle: Nicht ausgewaehlt");

        mDeviceList = getIntent().getExtras().getParcelableArrayList("device.list");

        // deviceStringMap = (HashMap<BluetoothDevice, String>) getIntent().getSerializableExtra("device.map");

        mListView = (ListView) findViewById(R.id.lv_paired);

        mAdapter = new DeviceListAdapter(this);

        mAdapter.setData(mDeviceList);
        mAdapter.setListener(new DeviceListAdapter.OnPairButtonClickListener() {
            @Override
            public void onPairButtonClick(int position, HandSide side) {
                BluetoothDevice device = mDeviceList.get(position);

                Log.i("Selected Device", device.getAddress() + " As " + side + " Sole.");
                deviceStringMap.put(device, side);
                // Remove colon from the bluetooth address
                String bluetoothId = device.getAddress().replace(":","");
                if (side == HandSide.RIGHT) {
                    commonContext.getConfiguration().getInsoles().getRightInsole().setBluetoothID(bluetoothId);
                    rightDevice = device.getAddress();
                    rightSoleTextView.setText("Rechte Sohle : " + device.getAddress());
                } else if (side == HandSide.LEFT) {
                    commonContext.getConfiguration().getInsoles().getLeftInsole().setBluetoothID(bluetoothId);
                    leftDevice = device.getAddress();
                    leftSoleTextView.setText("Linke Sohle : " + device.getAddress());
                } else if (side == null) {
                    if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                        unpairDevice(device);
                    } else {
                        showToast("Pairing...");
                        pairDevice(device);
                    }
                }
                // Save changes in config in XML
                File appDir = new File(Environment.getExternalStorageDirectory() + "/GangAnalyse");
                commonContext.setConfigExportPath(appDir.getPath()+"/" + CONFIG_FILENAME);
                ConfigUtil.saveInXML(commonContext);

/*                if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                    unpairDevice(device);
                } else {
                    showToast("Pairing...");

                    pairDevice(device);
                }*/
            }
        });

        mListView.setAdapter(mAdapter);

        registerReceiver(mPairReceiver, new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED));

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
/*                    if (!rightDevice.equals(leftDevice)) {
                        Intent intent = new Intent(context, ViewDataActivity.class);
                        intent.putExtra("device.map", (Serializable) deviceStringMap);
                        startActivity(intent);
                    } else {
                        showDialogBuilder("Linke und rechte Sohlen dürfen nicht gleich sein ..");
                    }*/
            }
        });
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(mPairReceiver);
        super.onDestroy();
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void pairDevice(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("createBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void unpairDevice(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("removeBond", (Class[]) null);
            method.invoke(device, (Object[]) null);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final BroadcastReceiver mPairReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                final int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                final int prevState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);

                if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING) {
                    showToast("Paired");
                } else if (state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDED) {
                    showToast("Unpaired");
                }

                mAdapter.notifyDataSetChanged();
            }
        }
    };

    private void showDialogBuilder(String message) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message);
        builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();

    }
}
