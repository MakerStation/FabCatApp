package com.fablab.fabcatapp.ui.functions;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.fablab.fabcatapp.R;
import com.fablab.fabcatapp.bluetooth.BluetoothConnect;

public class FunctionsFragment extends Fragment {
    @SuppressLint("StaticFieldLeak")
    private static View root;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (root == null) {
            root = inflater.inflate(R.layout.fragment_functions, container, false);

            ToggleButton pitchRollButton = root.findViewById(R.id.pitchRollToggleButton);
            pitchRollButton.setOnCheckedChangeListener((v, checked) -> {
                if (checked) {
                    BluetoothConnect.cat.activatePitchRoll(root);
                } else {
                    BluetoothConnect.cat.toggleFunction(root, 0);
                    TextView pitchTextView = root.findViewById(R.id.pitchTextView);
                    pitchTextView.setText(R.string.OFF);
                    TextView rollTextView = root.findViewById(R.id.rollTextView);
                    rollTextView.setText(R.string.OFF);
                }
            });

            ToggleButton autoBalanceButton = root.findViewById(R.id.autoBalanceToggleButton);
            autoBalanceButton.setOnCheckedChangeListener((v, checked) -> BluetoothConnect.cat.toggleFunction(root, checked ? 21 : 20));

            ToggleButton fallRecoveryButton = root.findViewById(R.id.fallRecoveryToggleButton);
            fallRecoveryButton.setOnCheckedChangeListener((v, checked) -> BluetoothConnect.cat.toggleFunction(root, checked ? 11 : 10));

            ImageButton meowButton = root.findViewById(R.id.meowButton);
            meowButton.setOnClickListener((v) -> BluetoothConnect.cat.toggleFunction(root, 30));
        }

        return root;
    }
}
