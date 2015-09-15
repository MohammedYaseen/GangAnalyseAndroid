package com.davengo.ga.receiver.bluetooth;

/**
 * Created by malkhameri on 03.08.2015.
 */

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.davengo.ga.common.byteDecoder.objects.OffObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;


public class BTConnection {

    private InputStream inputStream;
    private BluetoothDevice remoteDevice;

    private BluetoothSocket bluetoothSocket = null;
    private static final String UUID_SERIAL_PORT_PROFILE
            = "00001101-0000-1000-8000-00805F9B34FB";

    public BTConnection(BluetoothDevice remoteDevice) {
        this.remoteDevice = remoteDevice;
        BluetoothAdapter blAdapter = BluetoothAdapter.getDefaultAdapter();
        //blAdapter.getRemoteDevice("");
    }

    public void openBtConnection() throws IOException {

        bluetoothSocket = remoteDevice.createInsecureRfcommSocketToServiceRecord(getSerialPortUUID());
        bluetoothSocket.connect();

/*        RemoteDeviceHelper.authenticate(remoteDevice, "0000");

        String connectionURL = "btspp://" + remoteDevice.getBluetoothAddress()
                + ":1;authenticate=true;encrypt=false;master=false";
        streamConnection = (StreamConnection) Connector.open(connectionURL);*/

    }

    public InputStream getDataInputStream() throws IOException {
        if (inputStream == null)
            inputStream = bluetoothSocket.getInputStream();;
        return inputStream;
    }

    public void closeDataInputStream() throws IOException {
        getDataInputStream().close();
    }

    public void closeBTConnection() throws IOException {
        shutdownDevice();
       // bluetoothSocket.close();
        //RemoteDeviceHelper.removeAuthentication(remoteDevice);
        //streamConnection.close();
    }

    public boolean isConnected() {
        return (bluetoothSocket != null);
    }

    public void shutdownDevice() throws IOException {
        OffObject offObject = new OffObject();
        byte[] offByte = offObject.createOFFArray();

        OutputStream dataOutStream = bluetoothSocket.getOutputStream();
        Log.i(">>>>>>>>>>>>>>", "shutdownDevice");
       for (int i = 0; i < 100; i++) {
            // System.out.println("SHUTDOWN - "
            // + remoteDevice.getBluetoothAddress());
            dataOutStream.write(offByte);
            dataOutStream.flush();
        }
        dataOutStream.close();
    }

    public UUID getSerialPortUUID() {
        return  UUID.fromString(UUID_SERIAL_PORT_PROFILE);
    }
}

