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
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.fablab.fabcatapp.MainActivity;
import com.fablab.fabcatapp.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

public class BluetoothDiscovery extends MainActivity implements Runnable {
    public Thread bluetoothDiscovery = new Thread(this);
    private ArrayList<BluetoothDevice> availableDevices = new ArrayList<>();
    public static int countdown;
    private static boolean isDiscoveryRunning = false;
    private Context applicationContext;
    private View bluetoothFragmentRoot;
    private LinearLayout bluetoothScrollViewLayout;
    private TextView discoveryCountdownTextView;
    public BluetoothConnect connect;


    public BluetoothDiscovery(Context applicationContext, View bluetoothFragmentRoot) {
        this.applicationContext = applicationContext;
        this.bluetoothFragmentRoot = bluetoothFragmentRoot;
        this.bluetoothScrollViewLayout = bluetoothFragmentRoot.findViewById(R.id.devicesLayout);
        this.discoveryCountdownTextView = bluetoothFragmentRoot.findViewById(R.id.discoveryCountdown);
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

    private void displayDevices(ArrayList<BluetoothDevice> pairedDevices, ArrayList<BluetoothDevice> availableDevices, BluetoothAdapter adapter) {
        bluetoothScrollViewLayout.post(() -> bluetoothScrollViewLayout.removeAllViews());
        StringBuilder textToDisplay = new StringBuilder();
        StringBuilder bluetoothNotices = new StringBuilder();
        ArrayList<String> macList = new ArrayList<>();
        ArrayList<BluetoothDevice> pairedAndAvailableDevices = new ArrayList<>();

        if(availableDevices.size() == 0) {
            textToDisplay.append("No devices available, make sure the device is visible.\n");
            bluetoothNotices.append("No devices available, make sure the device is visible.\n");
        } else {
            textToDisplay.append("Devices available: \n");
            for (BluetoothDevice device : availableDevices) {
                macList.add(device.getAddress());
                String deviceInfo = "Nome: " + device.getName() + " MAC: " + device.getAddress() + "\n";
                textToDisplay.append(deviceInfo);
            }
        }

        if (pairedDevices.size() == 0) {
            textToDisplay.append("No devices available, pair one.\n");
            bluetoothNotices.append("No devices available, pair one.\n");
        } else {
            textToDisplay.append("Devices paired: \n");
            for (BluetoothDevice device : pairedDevices) {
                String deviceInfo = "Nome: " + device.getName() + " MAC: " + device.getAddress() + "\n";
                textToDisplay.append(deviceInfo);
                if (macList.contains(device.getAddress())) {
                    pairedAndAvailableDevices.add(device);

                }
            }
        }

        if (pairedAndAvailableDevices.size() == 0) {
            textToDisplay.append("You have to pair to a device to connect to it.\n");
            bluetoothNotices.append("You have to pair to a device to connect to it.\n");
        } else {
            textToDisplay.append("Devices you can connect to: \n");
            for (BluetoothDevice device : pairedAndAvailableDevices) {
                String currentName = device.getName() + '\n';
                textToDisplay.append(currentName);
            }
        }

        String txt = "Available: " + availableDevices.size() + " Paired: " + pairedDevices.size() + " total: " + pairedAndAvailableDevices.size() + " discovering: " + adapter.isDiscovering();
        textToDisplay.append(txt);

        if (PreferenceManager.getDefaultSharedPreferences(applicationContext).getBoolean("debug", false)) {
            TextView textViewToDisplay = new TextView(applicationContext);
            textViewToDisplay.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            textViewToDisplay.setText(textToDisplay);
            bluetoothScrollViewLayout.post(() -> bluetoothScrollViewLayout.addView(textViewToDisplay));
        } else {
            TextView bluetoothNoticesView = new TextView(applicationContext);
            bluetoothNoticesView.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            bluetoothNoticesView.setText(bluetoothNotices);
            bluetoothScrollViewLayout.post(() -> bluetoothScrollViewLayout.addView(bluetoothNoticesView));
        }

        for (BluetoothDevice device : pairedAndAvailableDevices) {
            Button currentButton = new Button(applicationContext, null, R.style.Widget_AppCompat_Button_Colored);
            currentButton.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));

            currentButton.setText(device.getName());
            currentButton.setOnClickListener((v) -> new Handler(Looper.getMainLooper()).post(() -> {
                connect = new BluetoothConnect(device, applicationContext, bluetoothFragmentRoot);
                connect.connect.start();
            }));
            currentButton.setBackground(applicationContext.getDrawable(R.drawable.device_button));
            currentButton.setTextSize(15);
            LinearLayout.LayoutParams ll = (LinearLayout.LayoutParams)currentButton.getLayoutParams();
            ll.gravity = Gravity.CENTER;
            ll.topMargin = 5;
            currentButton.setLayoutParams(ll);
            bluetoothScrollViewLayout.post(() -> bluetoothScrollViewLayout.addView(currentButton));
        }
    }

    @SuppressLint("SetTextI18n")
    private void startDiscovery(BluetoothAdapter adapter, BroadcastReceiver broadcastReceiver) {
        if (adapter.isDiscovering()) {
            //reboot discovery if it's already running
            adapter.cancelDiscovery();
        }
        adapter.startDiscovery();

        countdown = PreferenceManager.getDefaultSharedPreferences(applicationContext).getInt("discoveryCountdown", 10);
        while (countdown > 0) {
            try {
                discoveryCountdownTextView.post(() -> discoveryCountdownTextView.setTextColor(ContextCompat.getColor(applicationContext, R.color.textColorPrimary)));
                discoveryCountdownTextView.post(() -> discoveryCountdownTextView.setText(countdown + (countdown == 1 ? " second left" : " seconds left")));
                Thread.sleep(1000);
                countdown -= 1;
            } catch (InterruptedException e) {
                System.out.println("*****InterruptedException: " + e.getMessage() + " " + Arrays.toString(e.getStackTrace()));
            }
        }
        discoveryCountdownTextView.post(() -> {
            discoveryCountdownTextView.setTextColor(ContextCompat.getColor(applicationContext, R.color.scanComplete));
            discoveryCountdownTextView.setText("Scan complete");
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
                    new Handler(Looper.getMainLooper()).post(() -> MainActivity.createAlert("Your device doesn't support Bluetooth therefore you won't be able to use it.", bluetoothFragmentRoot, false));
                } else {
                    try {
                        BroadcastReceiver receiver = registerListener();
                        startDiscovery(adapter, receiver);
                        isDiscoveryRunning = false;
                        bluetoothDiscovery = null;
                    } catch (Exception e) {
                        String message = "Stack: " + Arrays.toString(e.getStackTrace()) + " **Cause**: " + e.getCause() + "**Message**: " + e.getMessage();
                        TextView errorMsg = new TextView(applicationContext);
                        errorMsg.setLayoutParams(new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        ));
                        errorMsg.setText(message);
                        bluetoothScrollViewLayout.post(() -> bluetoothScrollViewLayout.addView(errorMsg));

                        MainActivity.createAlert("There was an error while starting the discovery, you can try to restart the app. Cause: " + e.getMessage(), bluetoothFragmentRoot, false);
                        isDiscoveryRunning = false;
                        bluetoothDiscovery = null;
                    }
                }
            } else {
                MainActivity.createAlert("Wait for the current running scan to finish!", bluetoothFragmentRoot, true);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage() + Arrays.toString(e.getStackTrace()));
            MainActivity.createAlert("We encountered an error, please make sure that your Bluetooth is enabled. Cause: " + e.getMessage(), bluetoothFragmentRoot, false);
            isDiscoveryRunning = false;
            bluetoothDiscovery = null;
        }
    }
}