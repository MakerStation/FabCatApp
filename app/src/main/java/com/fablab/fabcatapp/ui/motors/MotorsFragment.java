package com.fablab.fabcatapp.ui.motors;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.fablab.fabcatapp.R;
import com.fablab.fabcatapp.bluetooth.BluetoothConnect;

import java.util.ArrayList;

public class MotorsFragment extends Fragment {
    public static int[] motorPositions = {90, 90, 90, 50, 50, 50, 50, 120, 120, 120, 120};
    private int[] motorNumbers = {1, 0, 2, 3, 4, 7, 8, 6, 5, 10, 9};
    private ArrayList<Button> motorIncrementButtons = new ArrayList<>();
    private ArrayList<Button> motorDecrementButtons = new ArrayList<>();
    private int[] views = {R.id.headDegrees, R.id.neckDegrees, R.id.tailDegrees, R.id.frontLeftShoulderDegrees, R.id.frontRightShoulderDegrees, R.id.frontLeftKneeDegrees, R.id.frontRightKneeDegrees, R.id.backLeftShoulderDegrees, R.id.backRightShoulderDegrees, R.id.backLeftKneeDegrees, R.id.backRightKneeDegrees};

    @SuppressLint("ClickableViewAccessibility") //l'app non é indirizzata a persone cieche
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_motors, container, false);

        motorIncrementButtons.add(root.findViewById(R.id.headIncrementButton));
        motorDecrementButtons.add(root.findViewById(R.id.headDecrementButton));

        motorIncrementButtons.add(root.findViewById(R.id.neckIncrementButton));
        motorDecrementButtons.add(root.findViewById(R.id.neckDecrementButton));

        motorIncrementButtons.add(root.findViewById(R.id.tailIncrementButton));
        motorDecrementButtons.add(root.findViewById(R.id.tailDecrementButton));

        motorIncrementButtons.add(root.findViewById(R.id.frontLeftShoulderIncrementButton));
        motorDecrementButtons.add(root.findViewById(R.id.frontLeftShoulderDecrementButton));

        motorIncrementButtons.add(root.findViewById(R.id.frontRightShoulderIncrementButton));
        motorDecrementButtons.add(root.findViewById(R.id.frontRightShoulderDecrementButton));

        motorIncrementButtons.add(root.findViewById(R.id.frontLeftKneeIncrementButton));
        motorDecrementButtons.add(root.findViewById(R.id.frontLeftKneeDecrementButton));

        motorIncrementButtons.add(root.findViewById(R.id.frontRightKneeIncrementButton));
        motorDecrementButtons.add(root.findViewById(R.id.frontRightKneeDecrementButton));

        motorIncrementButtons.add(root.findViewById(R.id.backLeftShoulderIncrementButton));
        motorDecrementButtons.add(root.findViewById(R.id.backLeftShoulderDecrementButton));

        motorIncrementButtons.add(root.findViewById(R.id.backRightShoulderIncrementButton));
        motorDecrementButtons.add(root.findViewById(R.id.backRightShoulderDecrementButton));

        motorIncrementButtons.add(root.findViewById(R.id.backLeftKneeIncrementButton));
        motorDecrementButtons.add(root.findViewById(R.id.backLeftKneeDecrementButton));

        motorIncrementButtons.add(root.findViewById(R.id.backRightKneeIncrementButton));
        motorDecrementButtons.add(root.findViewById(R.id.backRightKneeDecrementButton));

        for (int i = 0; i < motorIncrementButtons.size(); i++) {
            final int j = i;
            motorIncrementButtons.get(i).setOnTouchListener((v, event) -> {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        BluetoothConnect.cat.moveMotor(root, motorNumbers[j], true, views[j]);
                        break;
                    case MotionEvent.ACTION_UP:
                        BluetoothConnect.cat.stopMovement(motorNumbers[j]);
                        break;
                }
                return true;
            });
        }

        for (int i = 0; i < motorDecrementButtons.size(); i++) {
            final int j = i;
            motorDecrementButtons.get(i).setOnTouchListener((v, event) -> {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        BluetoothConnect.cat.moveMotor(root, motorNumbers[j], false, views[j]);
                        break;
                    case MotionEvent.ACTION_UP:
                        BluetoothConnect.cat.stopMovement(motorNumbers[j]);
                        break;
                }
                return true;
            });
        }

        return root;
    }
}
