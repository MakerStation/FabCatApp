package com.fablab.fabcatapp.ui.bluetooth;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.fablab.fabcatapp.MainActivity;
import com.fablab.fabcatapp.R;
import com.fablab.fabcatapp.bluetooth.BluetoothConnect;
import com.fablab.fabcatapp.bluetooth.BluetoothDiscovery;

import java.util.Arrays;

public class BluetoothFragment extends Fragment {
    @SuppressLint("StaticFieldLeak")
    private static BluetoothDiscovery discovery;
    @SuppressLint("StaticFieldLeak")
    private static View root;

    @SuppressLint("SetTextI18n")
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (root == null) {
            root = inflater.inflate(R.layout.fragment_bluetooth, container, false);

            LinearLayout bluetoothScrollViewLayout = root.findViewById(R.id.devicesLayout);

            Button discoveryOrDisconnectButton = root.findViewById(R.id.startDiscoveryOrDisconnect);
            if (BluetoothConnect.connected) {
                discoveryOrDisconnectButton.setText("Disconnect");
                discoveryOrDisconnectButton.setOnClickListener((v) -> discovery.connect.disconnectBluetooth());
            } else {
                discoveryOrDisconnectButton.setOnClickListener((v) -> {
                    bluetoothScrollViewLayout.removeAllViews();
                    discovery = new BluetoothDiscovery(getContext(), root);
                    try {
                        if (!discovery.bluetoothDiscovery.isAlive()) {
                            discovery.bluetoothDiscovery.start();
                        }
                    } catch (Exception e) {
                        TextView errorMsg = new TextView(getContext());
                        errorMsg.setLayoutParams(new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        ));
                        errorMsg.setText("M: " + e.getMessage() + " Stack: " + Arrays.toString(e.getStackTrace()));
                        bluetoothScrollViewLayout.addView(errorMsg);

                        MainActivity.createAlert("We encountered an error while scanning, you can try to restart the app.", root, false);
                    }
                });
            }
        }

        return root;
    }

    @SuppressLint("SetTextI18n")
    public static void setDiscoveryOrDisconnectButtonState(boolean discoveryOrDisconnect, View root, Context applicationContext) {
        LinearLayout bluetoothScrollViewLayout = root.findViewById(R.id.devicesLayout);
        Button discoveryOrDisconnectButton = root.findViewById(R.id.startDiscoveryOrDisconnect);
        if (discoveryOrDisconnect) {
            discoveryOrDisconnectButton.post(() -> discoveryOrDisconnectButton.setText("Scan devices"));
            discoveryOrDisconnectButton.setOnClickListener((v) -> {
                bluetoothScrollViewLayout.removeAllViews();
                discovery = new BluetoothDiscovery(applicationContext, root);
                try {
                    if (!discovery.bluetoothDiscovery.isAlive()) {
                        discovery.bluetoothDiscovery.start();
                    }
                } catch (Exception e) {
                    TextView errorMsg = new TextView(applicationContext);
                    errorMsg.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    ));
                    errorMsg.setText("M: " + e.getMessage() + " Stack: " + Arrays.toString(e.getStackTrace()));
                    bluetoothScrollViewLayout.addView(errorMsg);

                    MainActivity.createAlert("We encountered an error while scanning, you can try to restart the app.", root, false);
                }
            });
            System.out.println("startDiscoveryOrDisconnect set to discovery");
        } else {
            discoveryOrDisconnectButton.post(() -> {
                discoveryOrDisconnectButton.setText("Disconnect");
                if (discovery.connect != null) {
                    discoveryOrDisconnectButton.setOnClickListener((v) -> discovery.connect.disconnectBluetooth());
                } else {
                    MainActivity.createAlert("Already disconnected!", root, true);
                }
            });
            System.out.println("startDiscoveryOrDisconnect set to disconnect");
        }
    }
}