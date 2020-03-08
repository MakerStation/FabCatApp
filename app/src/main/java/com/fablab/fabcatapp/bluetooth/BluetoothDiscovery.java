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
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.fablab.fabcatapp.MainActivity;
import com.fablab.fabcatapp.R;
import com.fablab.fabcatapp.ui.bluetooth.BluetoothFragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

public class BluetoothDiscovery extends MainActivity implements Runnable {
    public Thread bluetoothDiscovery = new Thread(this);
    private ArrayList<BluetoothDevice> availableDevices = new ArrayList<>();
    public static int countdown;
    private static boolean isDiscoveryRunning = false;
    private Context applicationContext;

    public BluetoothDiscovery(Context applicationContext) {
        this.applicationContext = applicationContext;
    }

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
                        availableDevices.add(device);
                    } else {
                        System.out.println("***DISPOSITIVO NULLO TROVATO, LO IGNORIAMO...");
                    }
                }
            }
        };
        // Register the broadcast receiver
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        applicationContext.registerReceiver(broadcastReceiver, filter);

        return broadcastReceiver;
    }

    private ArrayList<BluetoothDevice> getPairedDevices(BluetoothAdapter adapter) {
        Set<BluetoothDevice> pairedDevices =  adapter.getBondedDevices();

        return new ArrayList<>(pairedDevices);
    }

    private void displayDevices(ArrayList<BluetoothDevice> pairedevices, ArrayList<BluetoothDevice> availableDevices, BluetoothAdapter adapter) {
        BluetoothFragment.bluetoothScrollViewLayout.post(() -> BluetoothFragment.bluetoothScrollViewLayout.removeAllViews());
        StringBuilder textToDisplay = new StringBuilder();
        StringBuilder bluetoothNotices = new StringBuilder();
        ArrayList<String> macList = new ArrayList<>();
        ArrayList<BluetoothDevice> pairedAndAvailableDevices = new ArrayList<>();

        if(availableDevices.size() == 0) {
            textToDisplay.append("Nessun dispositivo disponibile nelle vicinanze, verificare che sia visibile.\n");
            bluetoothNotices.append("Nessun dispositivo disponibile nelle vicinanze, verificare che sia visibile.\n");
        } else {
            textToDisplay.append("Dispositivi disponibili: \n");
            for (BluetoothDevice device : availableDevices) {
                macList.add(device.getAddress());
                String deviceInfo = "Nome: " + device.getName() + " MAC: " + device.getAddress() + "\n";
                textToDisplay.append(deviceInfo);
            }
        }

        if (pairedevices.size() == 0) {
            textToDisplay.append("Nessun dispositivo associato trovato, associarne uno.\n");
            bluetoothNotices.append("Nessun dispositivo associato trovato, associarne uno.\n");
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
            bluetoothNotices.append("Devi associarti ad un dispositivo per connettertici.\n");
        } else {
            textToDisplay.append("Dispositivi a cui puoi connetterti: \n");
            for (BluetoothDevice device : pairedAndAvailableDevices) {
                String currentName = device.getName() + '\n';
                textToDisplay.append(currentName);
            }
        }

        String txt = "Available: " + availableDevices.size() + " Paired: " + pairedevices.size() + " total: " + pairedAndAvailableDevices.size() + " discovering: " + adapter.isDiscovering();
        textToDisplay.append(txt);

        if (PreferenceManager.getDefaultSharedPreferences(applicationContext).getBoolean("debug", false)) {
            TextView textViewToDisplay = new TextView(applicationContext);
            textViewToDisplay.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            textViewToDisplay.setText(textToDisplay);
            BluetoothFragment.bluetoothScrollViewLayout.post(() -> BluetoothFragment.bluetoothScrollViewLayout.addView(textViewToDisplay));
        } else {
            TextView bluetoothNoticesView = new TextView(applicationContext);
            bluetoothNoticesView.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            bluetoothNoticesView.setText(bluetoothNotices);
            BluetoothFragment.bluetoothScrollViewLayout.post(() -> BluetoothFragment.bluetoothScrollViewLayout.addView(bluetoothNoticesView));
        }

        for (BluetoothDevice device : pairedAndAvailableDevices) {
            Button currentButton = new Button(applicationContext, null, R.style.Widget_AppCompat_Button_Colored);
            currentButton.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));

            currentButton.setText(device.getName());
            currentButton.setOnClickListener((v) -> new Handler(Looper.getMainLooper()).post(() -> {
                BluetoothConnect connect = new BluetoothConnect(device, applicationContext);
                connect.connect.start();
            }));
            currentButton.setBackground(applicationContext.getDrawable(R.drawable.device_button));
            currentButton.setTextSize(15);
            LinearLayout.LayoutParams ll = (LinearLayout.LayoutParams)currentButton.getLayoutParams();
            ll.gravity = Gravity.CENTER;
            currentButton.setLayoutParams(ll);
            BluetoothFragment.bluetoothScrollViewLayout.post(() -> BluetoothFragment.bluetoothScrollViewLayout.addView(currentButton));
        }
    }

    @SuppressLint("SetTextI18n")
    private void startDiscovery(BluetoothAdapter adapter, BroadcastReceiver broadcastReceiver) {
        BluetoothFragment.discoveryCountdownTextView.setTextColor(ContextCompat.getColor(applicationContext, R.color.textColorPrimary));
        if (adapter.isDiscovering()) {
            //nel caso sia in discovery la riavviamo
            adapter.cancelDiscovery();
        }
        adapter.startDiscovery();

        countdown = PreferenceManager.getDefaultSharedPreferences(applicationContext).getInt("discoveryCountdown", 10);
        while (countdown > 0) {
            try {
                Thread.sleep(1000);
                countdown -= 1;
                BluetoothFragment.discoveryCountdownTextView.post(() -> BluetoothFragment.discoveryCountdownTextView.setText(BluetoothDiscovery.countdown + " seconds left"));
            } catch (InterruptedException e) {
                System.out.println("*****INTERRUPTEDEXCEPTION: " + e.getMessage() + " " + Arrays.toString(e.getStackTrace()));
            }
        }
        BluetoothFragment.discoveryCountdownTextView.post(() -> {
            BluetoothFragment.discoveryCountdownTextView.setTextColor(ContextCompat.getColor(applicationContext, R.color.scanComplete));
            BluetoothFragment.discoveryCountdownTextView.setText("Scan complete");
        });
        adapter.cancelDiscovery();
        applicationContext.unregisterReceiver(broadcastReceiver);
        displayDevices(getPairedDevices(adapter), availableDevices, adapter);
    }

    public void run() {
        try {
            if (!isDiscoveryRunning) {
                isDiscoveryRunning = true;
                BluetoothAdapter adapter = getAdapter();
                if (adapter == null) {
                    new Handler(Looper.getMainLooper()).post(() -> MainActivity.createAlert("Il dispositivo non supporta il Bluetooth, perció non potrá essere usato.", BluetoothFragment.root, false));
                } else {
                    try {
                        BroadcastReceiver receiver = registerListener();
                        startDiscovery(adapter, receiver);
                        isDiscoveryRunning = false;
                        bluetoothDiscovery = null;
                    } catch (Exception e) {
                        String message = "Stack: " + Arrays.toString(e.getStackTrace()) + " **CAUSA**: " + e.getCause() + "**MESSAGGIO**: " + e.getMessage();
                        TextView errorMsg = new TextView(applicationContext);
                        errorMsg.setLayoutParams(new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        ));
                        errorMsg.setText(message);
                        BluetoothFragment.bluetoothScrollViewLayout.post(() -> BluetoothFragment.bluetoothScrollViewLayout.addView(errorMsg));

                        MainActivity.createAlert("Errore nell'avvio del bluetoothDiscovery. Prova a riavviare l'app. Causa: " + e.getMessage(), BluetoothFragment.root, false);
                        isDiscoveryRunning = false;
                        bluetoothDiscovery = null;
                    }
                }
            } else {
                MainActivity.createAlert("Attendi la fine della scansione attuale", BluetoothFragment.root, true);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage() + Arrays.toString(e.getStackTrace()));
            MainActivity.createAlert("Abbiamo riscontrato un errore. Controlla che il Bluetooth sia acceso.", BluetoothFragment.root, false);
            isDiscoveryRunning = false;
            bluetoothDiscovery = null;
        }
    }
}