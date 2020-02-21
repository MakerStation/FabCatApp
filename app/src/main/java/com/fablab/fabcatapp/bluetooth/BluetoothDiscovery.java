package com.fablab.fabcatapp.bluetooth;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.fablab.fabcatapp.MainActivity;
import com.fablab.fabcatapp.ui.bluetooth.BluetoothFragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

public class BluetoothDiscovery extends MainActivity implements Runnable {
    public Thread bluetoothDiscovery = new Thread(this);
    public int devicesFound = 0;
    private ArrayList<BluetoothDevice> availableDevices = new ArrayList<>();
    public static int countdown;
    private static boolean isDiscoveryRunning = false;

    private BluetoothAdapter getAdapter() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // If the adapter is null it means that the device does not support BluetoothDiscovery
            return null;
        } else {
            if (!mBluetoothAdapter.isEnabled()) {
                // We need to enable the BluetoothDiscovery, so we ask the user
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
                return mBluetoothAdapter;
            } else {
                return mBluetoothAdapter;
            }
        }
    }

    private BroadcastReceiver registerListener() {
        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @SuppressLint("SetTextI18n")
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    // A BluetoothDiscovery device was found
                    // Getting device information from the intent
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (device != null) {
                        System.out.println("Device found: " + device.getName() + "; MAC " + device.getAddress());
                        devicesFound++;
                        availableDevices.add(device);
                        BluetoothFragment.devicesFoundTextView.post(() -> BluetoothFragment.devicesFoundTextView.setText("Dispositivi trovati: " + devicesFound + " Size: " + availableDevices.size()));
                    } else {
                        System.out.println("***DISPOSITIVO NULLO TROVATO, LO IGNORIAMO...");
                    }
                }
            }
        };
        // Register the broadcast receiver
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        MainActivity.context.registerReceiver(broadcastReceiver, filter);

        return broadcastReceiver;
    }

    private ArrayList<BluetoothDevice> getPairedDevices(BluetoothAdapter adapter) {
        Set<BluetoothDevice> pairedDevices =  adapter.getBondedDevices();

        return new ArrayList<>(pairedDevices);
    }

    private void displayDevices(ArrayList<BluetoothDevice> pairedevices, ArrayList<BluetoothDevice> availableDevices, BluetoothAdapter adapter) {
        BluetoothFragment.bluetoothScrollViewLayout.post(() -> BluetoothFragment.bluetoothScrollViewLayout.removeAllViews());
        StringBuilder textToDisplay = new StringBuilder();
        ArrayList<String> macList = new ArrayList<>();
        ArrayList<BluetoothDevice> pairedAndAvailableDevices = new ArrayList<>();

        if(availableDevices.size() == 0) {
            textToDisplay.append("Nessun dispositivo disponibile nelle vicinanze, verificare che sia visibile.\n");
        } else {
            textToDisplay.append("Dispositivi disponibili: \n");
            for (BluetoothDevice device : availableDevices) {
                macList.add(device.getAddress());
                String deviceInfo = "Nome: " + device.getName() + " MAC: " + device.getAddress() + "\n";
                textToDisplay.append(deviceInfo);
            }
        }

        if (pairedevices.size() == 0) {
            textToDisplay.append("Nessun dispositivo associato trovato, associarne uno.");
        } else {
            textToDisplay.append("Dispositivi associati: \n");
            for (BluetoothDevice device : pairedevices) {
                String deviceInfo = "Nome: " + device.getName() + " MAC: " + device.getAddress() + "\n";
                textToDisplay.append(deviceInfo);
                if (macList.contains(device.getAddress())) {
                    pairedAndAvailableDevices.add(device);

                }
            }
        }

        if (pairedAndAvailableDevices.size() == 0) {
            textToDisplay.append("Devi associarti ad un dispositivo per connettertici.\n");
        } else {
            textToDisplay.append("Dispositivi a cui puoi connetterti: \n");
            for (BluetoothDevice device : pairedAndAvailableDevices) {
                String currentName = device.getName() + '\n';
                textToDisplay.append(currentName);
            }
        }

        String txt = "Available: " + availableDevices.size() + " Paired: " + pairedevices.size() + " total: " + pairedAndAvailableDevices.size() + " discovering: " + adapter.isDiscovering();
        textToDisplay.append(txt);

        TextView textViewToDisplay = new TextView(MainActivity.context);
        textViewToDisplay.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        textViewToDisplay.setText(textToDisplay);
        BluetoothFragment.bluetoothScrollViewLayout.post(() -> BluetoothFragment.bluetoothScrollViewLayout.addView(textViewToDisplay));

        for (BluetoothDevice device : pairedAndAvailableDevices) {
            Button currentButton = new Button(MainActivity.context);
            currentButton.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));

            currentButton.setText(device.getName());

            currentButton.setOnClickListener((v) -> new Handler(Looper.getMainLooper()).post(() -> {
                new AlertDialog.Builder(MainActivity.context).setTitle("Avviso").setMessage("Connessione a: " + device.getName())
                        .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                        })
                        .show();
                BluetoothConnect connect = new BluetoothConnect(device);
                connect.connect.start();
            }));

            BluetoothFragment.bluetoothScrollViewLayout.post(() -> BluetoothFragment.bluetoothScrollViewLayout.addView(currentButton));
        }
    }

    @SuppressLint("SetTextI18n")
    private void startDiscovery(BluetoothAdapter adapter, BroadcastReceiver broadcastReceiver) {
        if (adapter.isDiscovering()) {
            //nel caso sia in discovery la riavviamo
            adapter.cancelDiscovery();
        }
        adapter.startDiscovery();

        countdown = PreferenceManager.getDefaultSharedPreferences(MainActivity.context).getInt("discoveryCountdown", 10);
        while (countdown > 0) {
            try {
                Thread.sleep(1000);
                countdown -= 1;
                BluetoothFragment.discoveryCountdownTextView.post(() -> BluetoothFragment.discoveryCountdownTextView.setText("Tempo rimasto: " + BluetoothDiscovery.countdown + "s"));
            } catch (InterruptedException e) {
                System.out.println("*****INTERRUPTEDEXCEPTION: " + e.getMessage() + " " + Arrays.toString(e.getStackTrace()));
            }
        }
        BluetoothFragment.discoveryCountdownTextView.post(() -> BluetoothFragment.discoveryCountdownTextView.setText("Scansione completata"));
        adapter.cancelDiscovery();
        MainActivity.context.unregisterReceiver(broadcastReceiver);
        displayDevices(getPairedDevices(adapter), availableDevices, adapter);
    }

    public void run() {
        try {
            if (!isDiscoveryRunning) {
                isDiscoveryRunning = true;
                BluetoothAdapter adapter = getAdapter();
                if (adapter == null) {
                    new Handler(Looper.getMainLooper()).post(() -> MainActivity.createAlert("Il dispositivo non supporta il bluetoothDiscovery, perció non potrá essere usato.", "message", BluetoothFragment.root));
                } else {
                    try {
                        BroadcastReceiver receiver = registerListener();
                        startDiscovery(adapter, receiver);
                        isDiscoveryRunning = false;
                        bluetoothDiscovery = null;
                    } catch (Exception e) {
                        String message = "Stack: " + Arrays.toString(e.getStackTrace()) + " **CAUSA**: " + e.getCause() + "**MESSAGGIO**: " + e.getMessage();
                        TextView errorMsg = new TextView(MainActivity.context);
                        errorMsg.setLayoutParams(new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        ));
                        errorMsg.setText(message);
                        BluetoothFragment.bluetoothScrollViewLayout.post(() -> BluetoothFragment.bluetoothScrollViewLayout.addView(errorMsg));

                        new Handler(Looper.getMainLooper()).post(() -> MainActivity.createAlert("Errore nell'avvio del bluetoothDiscovery. Prova a riavviare l'app. Causa: " + e.getMessage(), "exit", BluetoothFragment.root));
                        isDiscoveryRunning = false;
                        bluetoothDiscovery = null;
                    }
                }
            } else {
                new Handler(Looper.getMainLooper()).post(() -> new AlertDialog.Builder(MainActivity.context).setTitle("Avviso").setMessage("Attendi la fine della scansione attuale.")
                        .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                        })
                        .show());
            }
        } catch (Exception e) {
            new Handler(Looper.getMainLooper()).post(() -> new AlertDialog.Builder(MainActivity.context).setTitle("Avviso").setMessage("Abbiamo riscontrato un errore nell'avvio della scansione controlla che il Bluetooth sia attivato.")
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    })
                    .show());
            isDiscoveryRunning = false;
            bluetoothDiscovery = null;
        }
    }
}