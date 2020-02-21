package com.fablab.fabcatapp.ui.bluetooth;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.fablab.fabcatapp.MainActivity;
import com.fablab.fabcatapp.R;
import com.fablab.fabcatapp.bluetooth.BluetoothDiscovery;

import java.util.Arrays;

public class BluetoothFragment extends Fragment {
    @SuppressLint("StaticFieldLeak")
    public static LinearLayout bluetoothScrollViewLayout;
    @SuppressLint("StaticFieldLeak")
    public static TextView discoveryCountdownTextView;
    @SuppressLint("StaticFieldLeak")
    public static TextView output;
    @SuppressLint("StaticFieldLeak")
    public static ScrollView bluetoothScrollview;
    @SuppressLint("StaticFieldLeak")
    public static View root;
    @SuppressLint("StaticFieldLeak")
    public static Button discoveryOrDisconnectButton;


    @SuppressLint("SetTextI18n")
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (root == null) {
            root = inflater.inflate(R.layout.fragment_bluetooth, container, false);

            discoveryCountdownTextView = root.findViewById(R.id.discoveryCountdown);


            discoveryOrDisconnectButton = root.findViewById(R.id.startDiscoveryOrDisconnect);
            discoveryOrDisconnectButton.setOnClickListener((v) -> {
                BluetoothDiscovery bluetooth = new BluetoothDiscovery();
                try {
                    if (!bluetooth.bluetoothDiscovery.isAlive()) {
                        bluetooth.bluetoothDiscovery.start();
                    }
                    MainActivity.createAlert("Scansione avviata!", "message", root);
                } catch (Exception e) {
                    TextView errorMsg = new TextView(MainActivity.context);
                    errorMsg.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    ));
                    errorMsg.setText("M: " + e.getMessage() + " Stack: " + Arrays.toString(e.getStackTrace()));
                    bluetoothScrollViewLayout.addView(errorMsg);

                    MainActivity.createAlert("Abbiamo riscontrato un errore nella scansione dei dispositivi, prova a riavviare l'app.", "message", root);
                }
            });

            bluetoothScrollview = root.findViewById(R.id.devices);

            bluetoothScrollViewLayout = root.findViewById(R.id.devicesLayout);
        }

        return root;
    }
    @SuppressLint("SetTextI18n")
    public static void resetDiscoveryOrDisconnectButtonState() {
        BluetoothFragment.discoveryOrDisconnectButton.post(() -> {
            BluetoothFragment.discoveryOrDisconnectButton.setText("Scansiona dispositivi");
            BluetoothFragment.discoveryOrDisconnectButton.setOnClickListener((v) -> {
                BluetoothDiscovery bluetooth = new BluetoothDiscovery();
                try {
                    if (!bluetooth.bluetoothDiscovery.isAlive()) {
                        bluetooth.bluetoothDiscovery.start();
                    }
                    MainActivity.createAlert("Scansione avviata!", "message", BluetoothFragment.root);
                } catch (Exception e) {
                    TextView errorMsg = new TextView(MainActivity.context);
                    errorMsg.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    ));
                    errorMsg.setText("M: " + e.getMessage() + " Stack: " + Arrays.toString(e.getStackTrace()));
                    BluetoothFragment.bluetoothScrollViewLayout.addView(errorMsg);

                    MainActivity.createAlert("Abbiamo riscontrato un errore nella scansione dei dispositivi, prova a riavviare l'app.", "message", BluetoothFragment.root);
                }
            });
        });
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}