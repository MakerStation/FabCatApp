package com.fablab.fabcatapp.ui.functions;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.fablab.fabcatapp.MainActivity;
import com.fablab.fabcatapp.R;
import com.fablab.fabcatapp.ui.bluetooth.BluetoothFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Timer;
import java.util.TimerTask;

public class FunctionsFragment extends Fragment {
    private Timer pitchRollTimer;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_functions, container, false);

        FloatingActionButton fab = root.findViewById(R.id.fab);
        fab.setOnClickListener((view) -> BluetoothFragment.sendCustomCommand(view, getContext())); //is equal to (view) -> BluetoothConnect.sendCustomCommand(view);

        ToggleButton pitchRollButton = root.findViewById(R.id.pitchRollToggleButton);
        pitchRollButton.setOnCheckedChangeListener((v, checked) -> {
            if (checkCat(root)) {
                if (checked) {
                    BluetoothFragment.cat.activatePitchRoll(root);
                    startPitchRollTimer(root);
                } else {
                    pitchRollTimer.cancel();
                    pitchRollTimer = null;
                    TextView pitchTextView = root.findViewById(R.id.pitchTextView);
                    pitchTextView.setText(R.string.OFF);
                    TextView rollTextView = root.findViewById(R.id.rollTextView);
                    rollTextView.setText(R.string.OFF);
                    if (checkCat(root)) BluetoothFragment.cat.toggleFunction(root, 0);
                }
            }
        });

        ToggleButton autoBalanceButton = root.findViewById(R.id.autoBalanceToggleButton);
        autoBalanceButton.setOnCheckedChangeListener((v, checked) -> { if(checkCat(root)) BluetoothFragment.cat.toggleFunction(root, checked ? 21 : 20); });

        ToggleButton fallRecoveryButton = root.findViewById(R.id.fallRecoveryToggleButton);
        fallRecoveryButton.setOnCheckedChangeListener((v, checked) -> { if(checkCat(root)) BluetoothFragment.cat.toggleFunction(root, checked ? 11 : 10); });

        ImageButton meowButton = root.findViewById(R.id.meowButton);
        meowButton.setOnClickListener((v) -> { if(checkCat(root)) BluetoothFragment.cat.toggleFunction(root, 30); });



        return root;
    }

    private boolean checkCat(View view) {
        if (BluetoothFragment.cat != null) return true;
        else {
            MainActivity.createAlert("Cat not connected", view, true);
            return false;
        }
    }

    private void startPitchRollTimer(View root) {
        if (pitchRollTimer == null) {
            TextView pitchTextView = root.findViewById(R.id.pitchTextView);
            TextView rollTextView = root.findViewById(R.id.rollTextView);

            pitchRollTimer = new Timer();
            int delay = (int) Math.round(PreferenceManager.getDefaultSharedPreferences(getContext()).getInt("pitchRollDelay", 1000) /25.0);
            pitchRollTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    pitchTextView.post(() -> {
                        pitchTextView.setText(BluetoothFragment.cat.pitchRoll[0]);
                        rollTextView.setText(BluetoothFragment.cat.pitchRoll[1]);
                    });
                }
            }, 0, delay == 0 ? 1 : delay);
        } else {
            MainActivity.createCriticalErrorAlert("Critical error", "The buttons' normal behaviour has been compromised. It is compulsory to restart the app.", getContext());
        }
    }
}
