package com.fablab.fabcatapp.bluetooth;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;

import com.fablab.fabcatapp.MainActivity;
import com.fablab.fabcatapp.ui.bluetooth.BluetoothFragment;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothConnect extends MainActivity implements Runnable {
    private final BluetoothSocket bluetoothSocket;
    public Thread connect = new Thread(this);
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");


    public BluetoothConnect(BluetoothDevice device) {
        // Use a temporary object that is later assigned to bluetoothSocket
        // because bluetoothSocket is final.
        BluetoothSocket tmp = null;

        try {
            // Get a BluetoothSocket to connect with the given BluetoothDevice.
            // MY_UUID is the app's UUID string, also used in the server code.
            tmp = device.createRfcommSocketToServiceRecord(myUUID);
        } catch (IOException e) {
            new Handler(Looper.getMainLooper()).post(() -> MainActivity.createAlert("Socket's create() method failed", "message", BluetoothFragment.root));
        }
        bluetoothSocket = tmp;
    }

    @SuppressLint("SetTextI18n")
    public void run() {
        try {
            bluetoothSocket.connect();

            //successo
            new Handler(Looper.getMainLooper()).post(() -> MainActivity.createAlert("La connessione è stata stabilita con successo.", "message", BluetoothFragment.root));
            BluetoothFragment.bluetoothScrollViewLayout.post(() -> {
                BluetoothFragment.bluetoothScrollViewLayout.removeAllViews();
                BluetoothFragment.devicesFoundTextView.setText("Connesso");
                BluetoothFragment.discoveryCountdownTextView.setText("");

                BluetoothFragment.output = new TextView(MainActivity.context);
                BluetoothFragment.bluetoothScrollViewLayout.addView(BluetoothFragment.output);
            });

            OutputStream out = bluetoothSocket.getOutputStream();
            InputStream in = bluetoothSocket.getInputStream();

            DataOutputStream btout = new DataOutputStream(out);

            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            try {
                btout.writeChars("*****Prova");
                System.out.println("***MESSAGGIO MANDATO");
                System.out.println("*********LEGGO STREAM");
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
                new Handler(Looper.getMainLooper()).post(() -> MainActivity.createAlert("Instrem interrrotto: disconnesso", "message", BluetoothFragment.root));
            }
        } catch (Exception e) {
            // Unable to connect; close the socket and return.
            try {
                bluetoothSocket.close();
            } catch (IOException closeException) {
                new Handler(Looper.getMainLooper()).post(() -> MainActivity.createAlert("Could not close the client socket", "message", BluetoothFragment.root));
            }
            new Handler(Looper.getMainLooper()).post(() -> MainActivity.createAlert("Connessione fallita: " + e.getMessage(), "message", BluetoothFragment.root));
        }
    }

    // Closes the client socket and causes the thread to finish.
    public void cancel() {
        try {
            bluetoothSocket.close();
        } catch (IOException e) {
            new Handler(Looper.getMainLooper()).post(() -> MainActivity.createAlert("Could not close the client socket", "message", BluetoothFragment.root));
        }
    }

    private void sendData(String message, OutputStream outStream) {
        byte[] msgBuffer = message.getBytes();
        try {
            System.out.println("***MANDO MESSAGGIO");
            outStream.write(msgBuffer);
            outStream.flush();
            System.out.println("***FATTO");
        } catch (IOException e) {
            String msg = "In onResume() and an exception occurred during write: " + e.getMessage();
            new Handler(Looper.getMainLooper()).post(() -> MainActivity.createAlert(msg, "message", BluetoothFragment.root));
        }
        new Handler(Looper.getMainLooper()).post(() -> MainActivity.createAlert("messaggio mandato", "message", BluetoothFragment.root));
    }
}