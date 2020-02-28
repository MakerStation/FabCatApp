package com.fablab.fabcatapp.bluetooth;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
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
import java.util.UUID;

public class BluetoothConnect extends MainActivity implements Runnable {
    private final BluetoothSocket bluetoothSocket;
    public Thread connect = new Thread(this);
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static boolean ignoreInstreamInterruption = false;
    public static OutputStream outStream;
    public static cat cat = new cat();

    public BluetoothConnect(BluetoothDevice device) {
        BluetoothSocket tmp = null;

        try {
            tmp = device.createRfcommSocketToServiceRecord(myUUID);
        } catch (IOException e) {
            new Handler(Looper.getMainLooper()).post(() -> MainActivity.createAlert("Socket creation failed ):", BluetoothFragment.root, false));
        }
        bluetoothSocket = tmp;
    }

    @SuppressLint("SetTextI18n")
    public void run() {
        try {
            bluetoothSocket.connect();
            ignoreInstreamInterruption = false;
            new Handler(Looper.getMainLooper()).post(() -> MainActivity.createAlert("La connessione è stata stabilita con successo.", BluetoothFragment.root, false));
            BluetoothFragment.bluetoothScrollViewLayout.post(() -> {
                BluetoothFragment.bluetoothScrollViewLayout.removeAllViews();
                BluetoothFragment.discoveryOrDisconnectButton.setText("Disconnetti");
                BluetoothFragment.discoveryOrDisconnectButton.setOnClickListener((view) -> disconnectBluetooth());
                BluetoothFragment.discoveryCountdownTextView.setText("");

                BluetoothFragment.output = new TextView(MainActivity.context);
                BluetoothFragment.bluetoothScrollViewLayout.addView(BluetoothFragment.output);
            });

            outStream = bluetoothSocket.getOutputStream();

            InputStream in = bluetoothSocket.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            try {
                String message;
                while ((message = br.readLine()) != null) {
                    System.out.println("##########ricevuto: " + message);
                    if (BluetoothFragment.output == null) {
                        BluetoothFragment.output = new TextView(MainActivity.context);
                        BluetoothFragment.bluetoothScrollViewLayout.addView(BluetoothFragment.output);
                    }
                    final String msgToLambda = message;
                    BluetoothFragment.output.post(() -> BluetoothFragment.output.append(msgToLambda + "\n"));

                    BluetoothFragment.bluetoothScrollview.post(() -> BluetoothFragment.bluetoothScrollview.fullScroll(View.FOCUS_DOWN));
                }
            } catch (Exception e) {
                if (!ignoreInstreamInterruption) {
                    new Handler(Looper.getMainLooper()).post(() -> MainActivity.createOverlayAlert("Disconesso", "Instrem interrupted: disconnected"));
                }
                BluetoothFragment.resetDiscoveryOrDisconnectButtonState();
                outStream = null;
            }
        } catch (Exception e) {
            // Unable to connect; close the socket and return.
            try {
                bluetoothSocket.close();
            } catch (IOException closeException) {
                new Handler(Looper.getMainLooper()).post(() -> MainActivity.createAlert("Could not close the client socket", BluetoothFragment.root, false));
            }
            new Handler(Looper.getMainLooper()).post(() -> MainActivity.createOverlayAlert("Error", "Connection failed: " + e.getMessage()));
        }
    }

    public void disconnectBluetooth() {
        try {
            ignoreInstreamInterruption = true;
            bluetoothSocket.close();
        } catch (IOException e) {
            new Handler(Looper.getMainLooper()).post(() -> MainActivity.createAlert("Could not close the client socket", BluetoothFragment.root, false));
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

    public static void sendCustomCommand(View view) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.context, R.style.DialogTheme);
        dialog.setTitle("Command parameters");
        dialog.setMessage("Type in the prefix then the command e.g. (221, 1)");

        LinearLayout layout = new LinearLayout(MainActivity.context);
        layout.setOrientation(LinearLayout.VERTICAL); //se non si imposta questa roba puoi mostrare solo una view alla volta

        EditText prefixInput = new EditText(MainActivity.context);
        prefixInput.setTextColor(Color.WHITE);
        prefixInput.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        prefixInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL); //per mettere solo l'input a decimale

        layout.addView(prefixInput);
        EditText cmdInput = new EditText(MainActivity.context);
        cmdInput.setTextColor(Color.WHITE);
        cmdInput.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        cmdInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL); //per mettere solo l'input a decimale
        layout.addView(cmdInput);
        dialog.setView(layout);
        dialog.setPositiveButton("Fine", (dialog1, which) -> {
            try {
                sendData(view, (byte) Integer.parseInt(prefixInput.getText().toString()), (byte) Integer.parseInt(cmdInput.getText().toString()));
            } catch (NumberFormatException e) {
                MainActivity.createAlert("Inserisci un numero valido", view, false);
            }
        });
        dialog.show();
    }

    public static boolean checkConnection(View callingView) {
        if (outStream == null) {
            MainActivity.createAlert("Non sei connesso!", callingView, true);
            return false;
        } else {
            return true;
        }
    }
}