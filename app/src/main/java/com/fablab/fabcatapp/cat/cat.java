package com.fablab.fabcatapp.cat;

import android.annotation.SuppressLint;
import android.util.SparseArray;
import android.view.View;
import android.widget.TextView;

import com.fablab.fabcatapp.MainActivity;
import com.fablab.fabcatapp.bluetooth.BluetoothConnect;
import com.fablab.fabcatapp.ui.motors.MotorsFragment;
import com.fablab.fabcatapp.ui.options.OptionsFragment;

import java.util.Timer;
import java.util.TimerTask;

public class cat {
    private final byte FUNCTIONSPREFIX = (byte) 221;
    private final byte TOGGLEFUNCTIONSPREFIX = (byte) 222;
    private final byte ON = (byte) 1;
    private final byte OFF = (byte) 0;
    private SparseArray<Timer> motorMovementTimer = new SparseArray<>();

    public void reset(View callingView) {
        BluetoothConnect.sendData(callingView, FUNCTIONSPREFIX, (byte) 1);
    }

    public void activatePitchRoll(View view) {
        byte delay = (byte) Math.round(OptionsFragment.pitchRollDelay /25.0); //25 da un warning di floating point con int
        if (delay == (byte) 0) {
            delay = (byte) 1;
        }
        BluetoothConnect.sendData(view, TOGGLEFUNCTIONSPREFIX, ON, delay);
    }

    public void moveMotor(View callingView, int motorId, boolean increment, int viewToUpdate) {
        if (BluetoothConnect.checkConnection(callingView)) {
            TextView currentTextView = callingView.findViewById(viewToUpdate);
            TimerTask task = new TimerTask() {
                @SuppressLint("SetTextI18n")
                @Override
                public void run() {
                    if ((MotorsFragment.motorPositions[motorId] == 180 && increment) || (MotorsFragment.motorPositions[motorId] == 0 && !increment)) {
                        MainActivity.createAlert("Limite raggiunto!", callingView, true);
                    } else if (increment) {
                        MotorsFragment.motorPositions[motorId]++;
                        BluetoothConnect.sendData(callingView, (byte) 220, (byte) motorId, (byte) MotorsFragment.motorPositions[motorId]);
                        currentTextView.post(() -> currentTextView.setText(MotorsFragment.motorPositions[motorId] + ""));
                    } else {
                        MotorsFragment.motorPositions[motorId]--;
                        BluetoothConnect.sendData(callingView, (byte) 220, (byte) motorId, (byte) MotorsFragment.motorPositions[motorId]);
                        currentTextView.post(() -> currentTextView.setText(MotorsFragment.motorPositions[motorId] + ""));
                    }
                }
            };
            motorMovementTimer.put(motorId, new Timer());

            motorMovementTimer.get(motorId).scheduleAtFixedRate(task, 0, 100);
        }
    }

    public void function(View callingView, int function) {
        BluetoothConnect.sendData(callingView, FUNCTIONSPREFIX, (byte) function);
    }

    public void stopMovement(int motorId) {
        if (motorMovementTimer.get(motorId) != null) { //se non si é connessi questo é nullo
            motorMovementTimer.get(motorId).cancel();
        }
    }
}
