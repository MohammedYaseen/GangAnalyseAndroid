package com.davengo.ga.receiver;

/**
 * Created by malkhameri on 03.08.2015.
 */
public interface Receiver {

    public enum ReceiverType {
        WLAN, BLUETOOTH
    }

    public void start();

    public void shutdown();
}
