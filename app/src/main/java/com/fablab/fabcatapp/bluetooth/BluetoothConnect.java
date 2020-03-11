package com.fablab.fabcatapp.bluetooth;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.fablab.fabcatapp.MainActivity;
import com.fablab.fabcatapp.R;
import com.fablab.fabcatapp.cat.cat;
import com.fablab.fabcatapp.ui.bluetooth.BluetoothFragment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.UUID;

public class BluetoothConnect extends MainActivity implements Runnable {
    private final BluetoothSocket bluetoothSocket;
    public Thread connect = new Thread(this);
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static boolean ignoreInStreamInterruption = false;
    public static OutputStream outStream;
    public static cat cat = new cat();
    private Context applicationContext;
    private View bluetoothFragmentRoot;
    private LinearLayout bluetoothScrollViewLayout;
    private TextView discoveryCountdownTextView;
    private ScrollView bluetoothScrollView;
    public static boolean connected;

    public BluetoothConnect(BluetoothDevice device, Context applicationContext, View bluetoothFragmentRoot) {
        BluetoothSocket tmp = null;
        this.bluetoothFragmentRoot = bluetoothFragmentRoot;
        this.applicationContext = applicationContext;
        this.bluetoothScrollViewLayout = bluetoothFragmentRoot.findViewById(R.id.devicesLayout);
        this.discoveryCountdownTextView = bluetoothFragmentRoot.findViewById(R.id.discoveryCountdown);
        this.bluetoothScrollView = bluetoothFragmentRoot.findViewById(R.id.devices);

        try {
            tmp = device.createRfcommSocketToServiceRecord(myUUID);
        } catch (IOException e) {
            new Handler(Looper.getMainLooper()).post(() -> MainActivity.createAlert("Socket creation failed ):", bluetoothFragmentRoot, false));
        }
        bluetoothSocket = tmp;
    }

    @SuppressLint("SetTextI18n")
    public void run() {
        try {
            bluetoothSocket.connect();
            connected = true;
            BluetoothFragment.setDiscoveryOrDisconnectButtonState(false, bluetoothFragmentRoot, applicationContext);
            ignoreInStreamInterruption = false;
            new Handler(Looper.getMainLooper()).post(() -> MainActivity.createAlert("Connection successful.", bluetoothFragmentRoot, false));
            bluetoothScrollViewLayout.post(() -> {
                bluetoothScrollViewLayout.removeAllViews();
                discoveryCountdownTextView.setText("");

                TextView output = new TextView(applicationContext);
                bluetoothScrollViewLayout.addView(output);
            });

            outStream = bluetoothSocket.getOutputStream();

            InputStream in = bluetoothSocket.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            try {
                String message;
                while ((message = br.readLine()) != null) {
                    System.out.println("##########ricevuto: " + message);
//                    TextView output = new TextView(applicationContext);
//                    bluetoothScrollViewLayout.addView(output);
//                    final String msgToLambda = message;
//                    output.post(() -> output.append(msgToLambda + "\n"));
//
//                    bluetoothScrollView.post(() -> bluetoothScrollView.fullScroll(View.FOCUS_DOWN));
                    if (message.startsWith("220")) {
                        message = message.substring(3);
                        cat.pitchRollChanged(message.substring(0, 3), message.substring(3, 6), applicationContext);
                    }
                }
            } catch (Exception e) {
                if (!ignoreInStreamInterruption) {
                    new Handler(Looper.getMainLooper()).post(() -> MainActivity.createOverlayAlert("Disconnected", "InStream interrupted Cause: " + e.getMessage() + " Stack: " + Arrays.toString(e.getStackTrace()), applicationContext));
                    BluetoothFragment.setDiscoveryOrDisconnectButtonState(true, bluetoothFragmentRoot, applicationContext);
                }
                outStream = null;
                connected = false;
            }
        } catch (Exception e) {
            try {
                bluetoothSocket.close();
            } catch (IOException closeException) {
                new Handler(Looper.getMainLooper()).post(() -> MainActivity.createAlert("Could not close the client socket", bluetoothFragmentRoot, false));
            } finally {
                connected = false;
                BluetoothFragment.setDiscoveryOrDisconnectButtonState(true, bluetoothFragmentRoot, applicationContext);
            }
            new Handler(Looper.getMainLooper()).post(() -> MainActivity.createOverlayAlert("Error", "Connection failed: " + e.getMessage(), applicationContext));
        }
    }

    public void disconnectBluetooth() {
        try {
            ignoreInStreamInterruption = true;
            connected = false;
            BluetoothFragment.setDiscoveryOrDisconnectButtonState(true, bluetoothFragmentRoot, applicationContext);
            bluetoothSocket.close();
        } catch (IOException e) {
            new Handler(Looper.getMainLooper()).post(() -> MainActivity.createAlert("Error while closing the client socket: disconnected", bluetoothFragmentRoot, false));
        }
    }

    public static void sendData(View callingView, byte pref, byte cmd, byte... extra) {
        if (checkConnection(callingView)) {
            byte[] command = new byte[2 + extra.length];
            command[0] = pref;
            command[1] = cmd;
            System.arraycopy(extra, 0, command, 2, extra.length + 2 - 2);
            try {
                outStream.write(command);
                outStream.flush();
            } catch (IOException e) {
                String msg = "Couldn't write command: " + e.getMessage();
                MainActivity.createAlert(msg, callingView, false);
            }
        }
    }

    public static void sendCustomCommand(View view, Context applicationContext) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(applicationContext, R.style.DialogTheme);
        dialog.setTitle("Command parameters");
        dialog.setMessage("Type in the prefix then the command e.g. (221, 1)");

        LinearLayout layout = new LinearLayout(applicationContext);
        layout.setOrientation(LinearLayout.VERTICAL); //without this you can only put one view at once

        EditText prefixInput = new EditText(applicationContext);
        prefixInput.setTextColor(Color.WHITE);
        prefixInput.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        prefixInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL); //input is floating point

        layout.addView(prefixInput);
        EditText cmdInput = new EditText(applicationContext);
        cmdInput.setTextColor(Color.WHITE);
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
        dialog.show();
    }

    public static boolean checkConnection(View callingView) {
        if (outStream == null) {
            MainActivity.createAlert("Not connected!", callingView, true);
            return false;
        } else {
            return true;
        }
    }
}