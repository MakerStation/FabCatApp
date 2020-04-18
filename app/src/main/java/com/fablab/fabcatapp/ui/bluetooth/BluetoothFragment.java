package com.fablab.fabcatapp.ui.bluetooth;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

//import com.bumptech.glide.Glide;
import com.fablab.fabcatapp.MainActivity;
import com.fablab.fabcatapp.R;
import com.fablab.fabcatapp.cat.Cat;
import com.fablab.fabcatapp.ui.options.OptionsFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;

public class BluetoothFragment extends Fragment {
    public static boolean ignoreInStreamInterruption = false;
    private static BluetoothSocket bluetoothSocket;
    private static OutputStream outStream;
    private static boolean connected;

    private ArrayList<BluetoothDevice> availableDevices = new ArrayList<>();
    private static int countdown = 0;
    private static boolean isDiscoveryRunning = false;
    public static Cat cat;

    private View root;
    private CountDownTimer countDownTimer;

    public static boolean connectionUnexpectedlyClosed = false;
    public static Exception latestException;

    private ArrayList<Button> connectButtons = new ArrayList<>();
    private ArrayList<BluetoothDevice> pairedAndAvailableDevices = new ArrayList<>();

    private static BluetoothAdapter adapter;


    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_bluetooth, container, false);

        FloatingActionButton fab = root.findViewById(R.id.fab);
        fab.setOnClickListener((view) -> BluetoothFragment.sendCustomCommand(view, getContext()));

        LinearLayout bluetoothScrollViewLayout = root.findViewById(R.id.devicesLayout);

        Button discoveryOrDisconnectButton = root.findViewById(R.id.startDiscoveryOrDisconnect);
        if (connected) {
            discoveryOrDisconnectButton.setText(R.string.disconnect);
            discoveryOrDisconnectButton.setOnClickListener((v) -> disconnectBluetooth(root));
        } else {
            discoveryOrDisconnectButton.setOnClickListener((v) -> {
                bluetoothScrollViewLayout.removeAllViews();
                try {
                    if (!isDiscoveryRunning) {
                        setupDiscovery();
                    } else {
                        MainActivity.createAlert("Wait for the current running scan to finish!", root, true);
                    }
                } catch (Exception e) {
                    if (OptionsFragment.getPreferencesBoolean("debug", getContext())) {
                        TextView errorMsg = new TextView(getContext());
                        errorMsg.setLayoutParams(new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        ));
                        errorMsg.setText(getResources().getString(R.string.exception, e.getMessage(), Arrays.toString(e.getStackTrace())));
                        bluetoothScrollViewLayout.addView(errorMsg);
                    }

                    MainActivity.createAlert("We encountered an error while scanning, you can try to restart the app.", root, false);
                }
            });
        }

        this.root = root;

        return root;
    }

    //------------discovery---------------

    private void setupDiscovery() {
        LinearLayout bluetoothScrollViewLayout = root.findViewById(R.id.devicesLayout);
        
        try {
            isDiscoveryRunning = true;
            adapter = getAdapter();
            if (adapter == null) {
                new Handler(Looper.getMainLooper()).post(() -> MainActivity.createAlert("Your device doesn't support Bluetooth therefore you won't be able to use it. Error code 2x03", root, false));
            } else {
                try {
                    BroadcastReceiver receiver = registerListener();
                    startDiscovery(adapter, receiver);
                } catch (Exception e) {
                    String message = "Stack: " + Arrays.toString(e.getStackTrace()) + " **Cause**: " + e.getCause() + "**Message**: " + e.getMessage();
                    TextView errorMsg = new TextView(getContext());
                    errorMsg.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    ));
                    errorMsg.setText(message);
                    bluetoothScrollViewLayout.addView(errorMsg);

                    MainActivity.createAlert("There was an error while starting the discovery, you can try to restart the app. Error code 2x04 Cause: " + e.getMessage(), root, false);
                    isDiscoveryRunning = false;
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage() + Arrays.toString(e.getStackTrace()));
            MainActivity.createAlert("We encountered an error, please make sure that your Bluetooth is enabled. Error code 2x05 Cause: " + e.getMessage(), root, false);
            isDiscoveryRunning = false;
        }
    }

    private void startDiscovery(BluetoothAdapter adapter, BroadcastReceiver broadcastReceiver) {
        TextView discoveryCountdownTextView = root.findViewById(R.id.discoveryCountdown);
        discoveryCountdownTextView.setTextColor(ContextCompat.getColor(requireContext(),  (OptionsFragment.getPreferencesBoolean("DarkTheme", requireContext()) ? R.color.textColorPrimary : R.color.menuBackGround)));

        pairedAndAvailableDevices.clear(); //otherwise old paired and available devices will be listed too
        connectButtons.clear();

        if (adapter.isDiscovering()) {
            //restart discovery if it's already running
            adapter.cancelDiscovery();
        }
        adapter.startDiscovery();

        Button startDiscoveryOrDisconnectButton = root.findViewById(R.id.startDiscoveryOrDisconnect);
        startDiscoveryOrDisconnectButton.setText(getString(R.string.cancel_discovery));
        startDiscoveryOrDisconnectButton.setOnClickListener((v) -> cancelDiscovery());

        countdown = PreferenceManager.getDefaultSharedPreferences(getContext()).getInt("discoveryCountdown", 10);

        //1010 because we need to add countdown*10 seconds to make the timer match the correct time, as onTick is only called after the previous one has returned and loses some milliseconds.
        countDownTimer = new CountDownTimer(countdown*1010, 1000) {
            public void onTick(long millisUntilFinished) {
                discoveryCountdownTextView.setText(countdown == 1 ? getResources().getString(R.string.countdown_one, countdown) : getResources().getString(R.string.countdown_other, countdown));
                countdown--;
            }

            public void onFinish() {
                if (getContext() != null) discoveryCountdownTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.scanComplete));
                else contextNotFound();

                discoveryCountdownTextView.setText(R.string.scan_complete);
                adapter.cancelDiscovery();
                isDiscoveryRunning = false;
                scanFinished(broadcastReceiver, adapter);
                countDownTimer = null;
            }
        }.start();
    }

    private void cancelDiscovery() {
        if (adapter != null)
            adapter.cancelDiscovery();
        else
            MainActivity.createAlert("Couldn't cancel discovery: adapter is null. Error code 2x06", root, true);

        if (countDownTimer != null)
            countDownTimer.cancel();
        else
            MainActivity.createAlert("Couldn't cancel discovery: timer is null. Error code 2x07", root, true);

        if (!connected)
            setDiscoveryOrDisconnectButtonState(true);
        else
            MainActivity.createCriticalErrorAlert("Critical error", "A critical error has occurred, click on restart to restart the app. Error code: 2x02", requireContext());

        isDiscoveryRunning = false;
        TextView discoveryCountDownTextView = root.findViewById(R.id.discoveryCountdown);
        discoveryCountDownTextView.setText("");
    }

    private void scanFinished(BroadcastReceiver broadcastReceiver, BluetoothAdapter adapter) {
        setDiscoveryOrDisconnectButtonState(true);

        if (getContext() != null)
            getContext().unregisterReceiver(broadcastReceiver);
        else contextNotFound();
        displayDevices(getPairedDevices(adapter), availableDevices, adapter);
    }

    private BluetoothAdapter getAdapter() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            // If the adapter is null it means that the device does not support BluetoothDiscovery
            return null;
        } else {
            if (!bluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
                return bluetoothAdapter;
            } else {
                return bluetoothAdapter;
            }
        }
    }

    private BroadcastReceiver registerListener() {
        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (device != null) {
                        availableDevices.add(device);
                    }
                }
            }
        };
        // Register the broadcast receiver
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        if (getContext() != null) getContext().registerReceiver(broadcastReceiver, filter);
        else contextNotFound();

        return broadcastReceiver;
    }

    private ArrayList<BluetoothDevice> getPairedDevices(BluetoothAdapter adapter) {
        Set<BluetoothDevice> pairedDevices =  adapter.getBondedDevices();

        return new ArrayList<>(pairedDevices);
    }

    private void displayDevices(ArrayList<BluetoothDevice> pairedDevices, ArrayList<BluetoothDevice> availableDevices, BluetoothAdapter adapter) {
        LinearLayout bluetoothScrollViewLayout = root.findViewById(R.id.devicesLayout);
        bluetoothScrollViewLayout.removeAllViews();
        StringBuilder textToDisplay = new StringBuilder();
        StringBuilder bluetoothNotices = new StringBuilder();
        ArrayList<String> macList = new ArrayList<>();

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

        if (OptionsFragment.getPreferencesBoolean("debug", getContext())) {
            TextView textViewToDisplay = new TextView(getContext());
            textViewToDisplay.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            textViewToDisplay.setText(textToDisplay);
            bluetoothScrollViewLayout.addView(textViewToDisplay);
        } else {
            TextView bluetoothNoticesView = new TextView(getContext());
            bluetoothNoticesView.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            bluetoothNoticesView.setText(bluetoothNotices);
            bluetoothScrollViewLayout.addView(bluetoothNoticesView);
        }

        for (BluetoothDevice device : pairedAndAvailableDevices) {
            Button currentButton = new Button(getContext(), null, R.style.Widget_AppCompat_Button_Colored);
            currentButton.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));

            currentButton.setText(device.getName());
            currentButton.setOnClickListener((v) -> {
                Button discoveryOrDisconnectButton = root.findViewById(R.id.startDiscoveryOrDisconnect);
                discoveryOrDisconnectButton.setText(R.string.connecting);
                discoveryOrDisconnectButton.setEnabled(false);

                for (int i = 0; i < connectButtons.size(); i++) {
                    connectButtons.get(i).setOnClickListener((v2) -> MainActivity.createAlert("Already connecting!", root, true));
                }

                AlertDialog animationDialog = new AlertDialog.Builder(requireContext(), R.style.DarkTheme_AnimationDialog).setCancelable(false).setTitle("Connecting").create();

                startConnectionAnimation(animationDialog);

                new CountDownTimer(2000, 400) {
                    public void onTick(long millisUntilFinished) {
                    }

                    public void onFinish() {
                        connect(device, animationDialog);
                    }
                }.start();
            });
            try {
                currentButton.setBackground(requireContext().getDrawable(R.drawable.device_button));
            } catch (Exception e)  {
                MainActivity.createAlert("Error while getting drawable. Try restarting the app. Error code 3x01", root, false);
            }
               
            currentButton.setTextSize(15);
            LinearLayout.LayoutParams ll = (LinearLayout.LayoutParams)currentButton.getLayoutParams();
            ll.gravity = Gravity.CENTER;
            ll.topMargin = 5;
            currentButton.setLayoutParams(ll);
            bluetoothScrollViewLayout.addView(currentButton);
            connectButtons.add(currentButton);
        }
    }


    //-----------connect--------------

    private void startConnectionAnimation(AlertDialog animationDialog) {
        ImageView animation = new ImageView(requireContext());
        animation.setLayoutParams(new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
        ));

        int animationId = View.generateViewId();
        animation.setId(animationId);

        ConstraintLayout animationLayout = new ConstraintLayout(requireContext());
        animationLayout.addView(animation);

        animationDialog.show();
        animationDialog.addContentView(animationLayout, new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT));

        //Glide.with(requireContext()).load((OptionsFragment.getPreferencesBoolean("DarkTheme", requireContext())) ? R.drawable.connecting_dark : R.drawable.connecting_light).into((ImageView) animationLayout.findViewById(animationId)); //cast is necessary due to Target<Drawable> being ambiguous
    }

    private void stopConnectionAnimation(AlertDialog animationDialog) {
        animationDialog.dismiss();
    }

    private void connect(BluetoothDevice device, AlertDialog animationDialog) {
        LinearLayout bluetoothScrollViewLayout = root.findViewById(R.id.devicesLayout);
        TextView discoveryCountdownTextView = root.findViewById(R.id.discoveryCountdown);

        final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
        try {
            bluetoothSocket = device.createRfcommSocketToServiceRecord(myUUID);
        } catch (IOException e) {
            MainActivity.createAlert("Socket creation failed ): Error code 2x08", root, false);
        }

        try {
            bluetoothSocket.connect();
            cat = new Cat();
            connected = true;
            Button discoveryOrDisconnectButton = root.findViewById(R.id.startDiscoveryOrDisconnect);
            discoveryOrDisconnectButton.setEnabled(true);

            setDiscoveryOrDisconnectButtonState(false);
            ignoreInStreamInterruption = false;
            MainActivity.createAlert("Connection successful.", root, false);
            bluetoothScrollViewLayout.removeAllViews();
            discoveryCountdownTextView.setText("");
            stopConnectionAnimation(animationDialog);

            TextView output = new TextView(getContext());
            bluetoothScrollViewLayout.addView(output);

            outStream = bluetoothSocket.getOutputStream();

            InputStream in = bluetoothSocket.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            new Thread() {
                @Override
                public void run() {
                    try {
                        String message;
                        while ((message = br.readLine()) != null) {
//                    TextView output = new TextView(getContext());
//                    bluetoothScrollViewLayout.addView(output);
//                    final String msgToLambda = message;
//                    output.post(() -> output.append(msgToLambda + "\n"));
//
//                    bluetoothScrollView.post(() -> bluetoothScrollView.fullScroll(View.FOCUS_DOWN));
                            if (message.startsWith("220")) {
                                message = message.substring(3);
                                cat.pitchRollChanged(message.substring(0, 3), message.substring(3, 6));
                            }
                        }
                    } catch (Exception e) {
                        if (!ignoreInStreamInterruption) {
                            latestException = e;
                            connectionUnexpectedlyClosed = true;
                            setDiscoveryOrDisconnectButtonState(true);
                        }
                        outStream = null;
                        connected = false;
                        enableConnectButtons();
                    }
                }
            }.start();
        } catch (Exception e) {
            try {
                bluetoothSocket.close();
            } catch (NullPointerException | IOException ie) {
                if (!OptionsFragment.getPreferencesBoolean("debug", getContext()))
                    MainActivity.createAlert("Could not close the client socket. Enable debug mode for further information. Error code 2x09", root, false);
                else
                    MainActivity.createOverlayAlert("Error", "Couldn't close the client socket, error code 2x09. Cause: " + ie.getCause() + " \n Stack trace: " + Arrays.toString(e.getStackTrace()), requireContext());

            } finally {
                connected = false;
                setDiscoveryOrDisconnectButtonState(true);
                for (int i = 0; i < connectButtons.size(); i++) {
                    connectButtons.get(i).setClickable(true);
                }
            }
            MainActivity.createOverlayAlert("Error", "Connection failed. Error code 2x10. Cause: " + e.getMessage() + (OptionsFragment.getPreferencesBoolean("debug", requireContext()) ? "\nStack trace: " + Arrays.toString(e.getStackTrace()) : "\nEnable debug for further information."), getContext());
            outStream = null;
            connected = false;
            enableConnectButtons();
        }
    }

    private void enableConnectButtons() {
        for (int i = 0; i < connectButtons.size(); i++) {
            int j = i;
            connectButtons.get(i).setOnClickListener((v) -> {
                for (int k = 0; k < connectButtons.size(); k++) {
                    connectButtons.get(k).setOnClickListener((v2) -> MainActivity.createAlert("Already connecting!", root, true));
                }

                Button discoveryOrDisconnectButton = root.findViewById(R.id.startDiscoveryOrDisconnect);
                discoveryOrDisconnectButton.setText(R.string.connecting);
                discoveryOrDisconnectButton.setEnabled(false);

                AlertDialog animationDialog = new AlertDialog.Builder(requireContext(), R.style.DarkTheme_AnimationDialog).setCancelable(false).setTitle("Connecting").create();

                startConnectionAnimation(animationDialog);

                connect(pairedAndAvailableDevices.get(j), animationDialog);
            });
        }
    }

    private void disconnectBluetooth(View root) {
        try {
            ignoreInStreamInterruption = true;
            bluetoothSocket.close();
            setDiscoveryOrDisconnectButtonState(true);
            connected = false;
        } catch (IOException e) {
            new Handler(Looper.getMainLooper()).post(() -> MainActivity.createAlert("Error while closing the client socket: disconnected. Error code 2x11", root, false));
        } catch (NullPointerException e) {
            new Handler(Looper.getMainLooper()).post(() -> MainActivity.createOverlayAlert("Error", "Error while closing the client socket. Error code 2x12  Cause: " + e.getMessage(), requireContext()));
        }
    }

    public static void sendData(View callingView, byte pref, byte cmd, byte... extra) {
        if (checkConnection()) {
            byte[] command = new byte[2 + extra.length];
            command[0] = pref;
            command[1] = cmd;
            System.arraycopy(extra, 0, command, 2, extra.length + 2 - 2);
            try {
                outStream.write(command);
                outStream.flush();
            } catch (IOException e) {
                String msg = "Couldn't write command. Error code 2x13. Cause: " + e.getMessage();
                MainActivity.createAlert(msg, callingView, false);
            }
        } else {
            MainActivity.createAlert("Not connected!", callingView, true);
        }
    }

    public static boolean checkConnection() {
        return outStream != null;
    }

    public static void sendCustomCommand(View view, Context applicationContext) {
        boolean DarkTheme = OptionsFragment.getPreferencesBoolean("DarkTheme", applicationContext);
        AlertDialog.Builder dialog = new AlertDialog.Builder(applicationContext, DarkTheme ? R.style.DialogTheme : R.style.Theme_AppCompat_Light_Dialog);
        dialog.setTitle("Command parameters");
        dialog.setMessage("Type in the prefix then the command e.g. (221, 1)");

        LinearLayout layout = new LinearLayout(applicationContext);
        layout.setOrientation(LinearLayout.VERTICAL); //without this you can only put one view at once

        EditText prefixInput = new EditText(applicationContext);
        if (DarkTheme) prefixInput.setTextColor(Color.WHITE);
        prefixInput.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        prefixInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL); //input is floating point

        layout.addView(prefixInput);
        EditText cmdInput = new EditText(applicationContext);
        if (DarkTheme) cmdInput.setTextColor(Color.WHITE);
        cmdInput.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        cmdInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL); //input is floating point
        layout.addView(cmdInput);
        dialog.setView(layout);
        dialog.setPositiveButton("Done", (dialog1, which) -> {
            try {
                sendData(view, (byte) Integer.parseInt(prefixInput.getText().toString()), (byte) Integer.parseInt(cmdInput.getText().toString()));
            } catch (NumberFormatException e) {
                MainActivity.createAlert("Please insert a valid number", view, false);
            }
        });
        AlertDialog alertDialog = dialog.create();
        alertDialog.show();
    }

    private void setDiscoveryOrDisconnectButtonState(boolean discoveryOrDisconnect) {
        LinearLayout bluetoothScrollViewLayout = root.findViewById(R.id.devicesLayout);
        Button discoveryOrDisconnectButton = root.findViewById(R.id.startDiscoveryOrDisconnect);
        if (discoveryOrDisconnect) {
            discoveryOrDisconnectButton.post(() -> discoveryOrDisconnectButton.setText(getString(R.string.scan_devices)));
            discoveryOrDisconnectButton.setOnClickListener((v) -> {
                bluetoothScrollViewLayout.removeAllViews();
                try {
                    if (!isDiscoveryRunning) {
                        setupDiscovery();
                    }
                } catch (Exception e) {
                    TextView errorMsg = new TextView(getContext());
                    errorMsg.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    ));
                    errorMsg.setText(getString(R.string.exception, e.getMessage(), Arrays.toString(e.getStackTrace())));
                    bluetoothScrollViewLayout.addView(errorMsg);

                    MainActivity.createAlert("We encountered an error while scanning, you can try to restart the app.", root, false);
                }
            });
        } else {
            discoveryOrDisconnectButton.setText(getString(R.string.disconnect));
            if (connected) {
                discoveryOrDisconnectButton.setOnClickListener((v) -> disconnectBluetooth(root));
            } else {
                MainActivity.createAlert("Already disconnected!", root, true);
            }
        }
    }

    private void contextNotFound() {
        MainActivity.createAlert("There was an error while updating the countdown. You should restart the app. Error code 5x01", root, false);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (countDownTimer != null && isDiscoveryRunning) {
            countDownTimer.cancel();
            if (getAdapter() != null) getAdapter().cancelDiscovery();
            else MainActivity.createOverlayAlert("Error", "We had an error cancelling the device discovery. It is recommended to restart the app. Error code 2x06", getContext());
            isDiscoveryRunning = false;
        }
    }
}