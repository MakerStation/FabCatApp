package com.fablab.fabcatapp.ui.positions;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.fablab.fabcatapp.MainActivity;
import com.fablab.fabcatapp.R;
import com.fablab.fabcatapp.ui.bluetooth.BluetoothFragment;

public class PositionsFragment extends Fragment {
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_positions, container, false);

        Button shutdownButton = root.findViewById(R.id.shutdownButton);
        Button startButton = root.findViewById(R.id.startButton);
        Button calibrationButton = root.findViewById(R.id.calibrationButton);
        Button motor90DegreesButton = root.findViewById(R.id.motor90DegreesButton);
        Button straightLegButton = root.findViewById(R.id.straightLegButton);
        Button crouchedButton = root.findViewById(R.id.crouchedButton);

        shutdownButton.setOnClickListener((v) -> { if(checkCat(root)) BluetoothFragment.cat.function(root, 0); });

        startButton.setOnClickListener((v) -> { if(checkCat(root)) BluetoothFragment.cat.function(root, 1); });

        calibrationButton.setOnClickListener((v) ->{ if(checkCat(root)) BluetoothFragment.cat.function(root, 2); });

        motor90DegreesButton.setOnClickListener((v) -> { if(checkCat(root)) BluetoothFragment.cat.function(root, 3); });

        straightLegButton.setOnClickListener((v) -> { if(checkCat(root)) BluetoothFragment.cat.function(root, 4); });

        crouchedButton.setOnClickListener((v) -> { if(checkCat(root)) BluetoothFragment.cat.function(root, 5); });

        return root;
    }

    private boolean checkCat(View view) {
        if (BluetoothFragment.cat != null) return true;
        else {
            MainActivity.createAlert("Cat not connected", view, true);
            return false;
        }
    }
}
