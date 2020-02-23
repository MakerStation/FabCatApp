package com.fablab.fabcatapp.cat;

import android.view.View;

import com.fablab.fabcatapp.bluetooth.BluetoothConnect;
import com.fablab.fabcatapp.ui.bluetooth.BluetoothFragment;
import com.fablab.fabcatapp.ui.options.OptionsFragment;

public class cat {
    private final byte FUNCTIONSPREFIX = (byte) 221;
    private final byte TOGGLEFUNCTIONSPREFIX = (byte) 222;
    private final byte ON = (byte) 1;
    private final byte OFF = (byte) 0;

    public void reset() {
        BluetoothConnect.sendData(BluetoothFragment.root, FUNCTIONSPREFIX, (byte) 1);
    }

    public void activatePitchRoll(View view) {
        byte delay = (byte) Math.round(OptionsFragment.pitchRollDelay /25);
        if (delay == (byte) 0) {
            delay = (byte) 1;
        }
        BluetoothConnect.sendData(view, TOGGLEFUNCTIONSPREFIX, ON, delay);
    }
}
