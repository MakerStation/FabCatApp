package com.fablab.fabcatapp.cat;

import android.content.Context;
import android.util.SparseArray;
import android.view.View;
import android.widget.TextView;

import com.fablab.fabcatapp.MainActivity;
import com.fablab.fabcatapp.R;
import com.fablab.fabcatapp.ui.bluetooth.BluetoothFragment;
import com.fablab.fabcatapp.ui.motors.MotorsFragment;
import com.fablab.fabcatapp.ui.options.OptionsFragment;

import java.util.Timer;
import java.util.TimerTask;

public class Cat {
    public String[] pitchRoll = new String[2];
    private SparseArray<Timer> motorMovementTimer = new SparseArray<>();
    private int[][] positions = {
            {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},           // shutdown
            {90, 90, 90, 50, 50, 50, 50, 120, 120, 120, 120},       // start position
            {90, 90, 90, 80, 80, 80, 80, 112, 112, 112, 112},       // calibration position
            {90, 90, 90, 90, 90, 90, 90, 90, 90, 90, 90},           // all motors 90 degrees
            {90, 90, 90, 80, 80, 80, 80, 40, 40, 40, 40},           // straight legs
            {90, 150, 120, 20, 18, 20, 20, 168, 170, 168, 168}};    // crouched position

    public void activatePitchRoll(View view) {
        byte delay = (byte) Math.round(OptionsFragment.pitchRollDelay /25.0); //25 throws a floating point with int division
        if (delay == (byte) 0) {
            delay = (byte) 1;
        }
        BluetoothFragment.sendData(view, (byte) 222, (byte) 1, delay);
    }

    public void pitchRollChanged(String pitch, String roll) {
        pitchRoll[0] = pitch;
        pitchRoll[1] = roll;
    }

    public void moveMotor(View callingView, int motorId, boolean increment, int viewToUpdate, Context applicationContext) {
        if (BluetoothFragment.checkConnection(callingView)) {
            TextView currentTextView = callingView.findViewById(viewToUpdate);
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    if ((MotorsFragment.motorPositions[motorId] == 180 && increment) || (MotorsFragment.motorPositions[motorId] == 0 && !increment)) {
                        MainActivity.createAlert("Limit reached!", callingView, true);
                    } else if (increment) {
                        MotorsFragment.motorPositions[motorId]++;
                        BluetoothFragment.sendData(callingView, (byte) 220, (byte) motorId, (byte) MotorsFragment.motorPositions[motorId]);
                        currentTextView.post(() -> currentTextView.setText(applicationContext.getResources().getString(R.string.empty_string, MotorsFragment.motorPositions[motorId])));
                    } else {
                        MotorsFragment.motorPositions[motorId]--;
                        BluetoothFragment.sendData(callingView, (byte) 220, (byte) motorId, (byte) MotorsFragment.motorPositions[motorId]);
                        currentTextView.post(() -> currentTextView.setText(applicationContext.getResources().getString(R.string.empty_string, MotorsFragment.motorPositions[motorId])));
                    }
                }
            };
            motorMovementTimer.put(motorId, new Timer());

            motorMovementTimer.get(motorId).scheduleAtFixedRate(task, 0, 100);
        }
    }

    public void function(View callingView, int function) {
        BluetoothFragment.sendData(callingView, (byte) 221, (byte) function);
        MotorsFragment.motorPositions = positions[function];
    }

    public void toggleFunction(View callingView, int function) {
        BluetoothFragment.sendData(callingView, (byte) 222, (byte) function);
    }

    public void stopMovement(int motorId) {
        if (motorMovementTimer.get(motorId) != null) {
            motorMovementTimer.get(motorId).cancel();
            motorMovementTimer.remove(motorId);
        }
    }

    public void stopAllMovementThreads(View callingView) {
        for(int i = 0; i < motorMovementTimer.size(); i++) {
            int key = motorMovementTimer.keyAt(i);

            if (motorMovementTimer.get(key) != null) {
                motorMovementTimer.get(key).cancel();
            } else {
                MainActivity.createAlert("Couldn't terminate Timer, Timer is null.", callingView, true);
            }
        }
        motorMovementTimer.clear();
    }
}
