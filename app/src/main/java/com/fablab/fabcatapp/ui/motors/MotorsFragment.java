package com.fablab.fabcatapp.ui.motors;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.fablab.fabcatapp.MainActivity;
import com.fablab.fabcatapp.R;
import com.fablab.fabcatapp.ui.bluetooth.BluetoothFragment;

import java.util.ArrayList;

public class MotorsFragment extends Fragment {
    public static int[] motorPositions = {90, 90, 90, 50, 50, 50, 50, 120, 120, 120, 120};
    private int[] motorNumbers = {1, 0, 2, 3, 4, 7, 8, 6, 5, 10, 9};
    private ArrayList<Button> motorIncrementButtons = new ArrayList<>();
    private ArrayList<Button> motorDecrementButtons = new ArrayList<>();
    private int[] views = {R.id.head, R.id.neck, R.id.tail, R.id.frontLeftShoulder, R.id.frontRightShoulder, R.id.frontLeftKnee, R.id.frontRightKnee, R.id.backLeftShoulder, R.id.backRightShoulder, R.id.backLeftKnee, R.id.backRightKnee};
    private int[] stringResources = {R.string.head_motor, R.string.neck_motor, R.string.tail_motor, R.string.front_left_shoulder_motor, R.string.front_right_shoulder_motor, R.string.front_left_knee_motor, R.string.front_right_knee_motor, R.string.back_left_shoulder_motor, R.string.back_right_shoulder_motor, R.string.back_left_knee_motor, R.string.back_right_knee_motor};
    private int[] stringResourcesOff = {R.string.head_motor_off, R.string.neck_motor_off, R.string.tail_motor_off, R.string.front_left_shoulder_motor_off, R.string.front_right_shoulder_motor_off, R.string.front_left_knee_motor_off, R.string.front_right_knee_motor_off, R.string.back_left_shoulder_motor_off, R.string.back_right_shoulder_motor_off, R.string.back_left_knee_motor_off, R.string.back_right_knee_motor_off};

    @SuppressLint("ClickableViewAccessibility")
    //app not for blind people
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

        for (int i = 0; i < views.length; i++) {
            TextView currentDegreesView = root.findViewById(views[i]);
            if (motorPositions[i] == -1) {
                currentDegreesView.setText(getString(stringResourcesOff[i]));
                motorIncrementButtons.get(i).setEnabled(false);
                motorDecrementButtons.get(i).setEnabled(false);
            } else {
                currentDegreesView.setText(getString(stringResources[i], motorPositions[i]));
                motorIncrementButtons.get(i).setEnabled(true);
                motorDecrementButtons.get(i).setEnabled(true);
            }
        }

        for (int i = 0; i < motorIncrementButtons.size(); i++) {
            final int j = i;
            motorIncrementButtons.get(i).setOnTouchListener((v, event) -> {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (BluetoothFragment.cat != null) {
                            BluetoothFragment.cat.moveMotor(root, motorNumbers[j], true, views[j], stringResources[j], getContext());
                        } else {
                            MainActivity.createAlert("Not connected!", root, true);
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        if (BluetoothFragment.cat != null) {
                            BluetoothFragment.cat.stopMovement(motorNumbers[j]);
                        } else {
                            MainActivity.createAlert("Not connected!", root, true);
                        }
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
                        if (BluetoothFragment.cat != null) {
                            BluetoothFragment.cat.moveMotor(root, motorNumbers[j], false, views[j], stringResources[j], getContext());
                        } else {
                            MainActivity.createAlert("Not connected!", root, true);
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        if (BluetoothFragment.cat != null) {
                            BluetoothFragment.cat.stopMovement(motorNumbers[j]);
                        } else {
                            MainActivity.createAlert("Not connected!", root, true);
                        }
                        break;
                }
                return true;
            });
        }

        setHasOptionsMenu(true);

        return root;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.add("Reset all Threads").setOnMenuItemClickListener((menuItem) -> {
            if (BluetoothFragment.cat != null) {
                BluetoothFragment.cat.stopAllMovementThreads(getView());
            } else {
                MainActivity.createAlert("Not connected!", menuItem.getActionView(), true);
            }

            return false;
        });
        super.onCreateOptionsMenu(menu, inflater);
    }
}
