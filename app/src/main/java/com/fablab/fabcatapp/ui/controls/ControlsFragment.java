package com.fablab.fabcatapp.ui.controls;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.fablab.fabcatapp.R;
import com.fablab.fabcatapp.bluetooth.BluetoothConnect;

public class ControlsFragment extends Fragment {
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_controls, container, false);

        Button button = root.findViewById(R.id.button);
        button.setOnClickListener((v) -> BluetoothConnect.cat.activatePitchRoll(root));

        return root;
    }
}
