package com.fablab.fabcatapp.ui.options;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.fablab.fabcatapp.MainActivity;
import com.fablab.fabcatapp.R;
import com.fablab.fabcatapp.ui.bluetooth.BluetoothFragment;


public class OptionsFragment extends Fragment {
    public static SharedPreferences preferences;
    public static int pitchRollDelay;

    @SuppressLint("SetTextI18n")
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_options, container, false);

        EditText pitchRollDelay = root.findViewById(R.id.pitchRollDelayEditText);
        pitchRollDelay.setText(getPreferencesInt("pitchRollDelay") + ""); //se viene passato solo un int riceviamo android.content.res.Resources$NotFoundException perché tenta di usare String.valueOf e il nostro in per cercare la risorsa da R.string
        pitchRollDelay.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                try {
                    setPreferencesInt("pitchRollDelay", Integer.parseInt(pitchRollDelay.getText().toString()));
                } catch (Exception e) {
                    MainActivity.createAlert("Inserisci un numero valido!", root, false);
                    hideOptionsFragmentKeyboard(root);
                }
            }
        });

        EditText bluetoothDiscoveryCountdown = root.findViewById(R.id.bluetoothDiscoveryCountdown);
        bluetoothDiscoveryCountdown.setText(getPreferencesInt("discoveryCountdown") + ""); //se viene passato solo un int riceviamo android.content.res.Resources$NotFoundException perché tenta di usare String.valueOf e il nostro in per cercare la risorsa da R.string
        bluetoothDiscoveryCountdown.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                try {
                    setPreferencesInt("discoveryCountdown", Integer.parseInt(bluetoothDiscoveryCountdown.getText().toString()));
                } catch (Exception e) {
                    MainActivity.createAlert("Inserisci un numero valido!", root, false);
                    hideOptionsFragmentKeyboard(root);
                }
            }
        });

        return root;
    }

    private void hideOptionsFragmentKeyboard(View root) {
        Context context = getContext();
        if (context != null) {
            MainActivity.hideKeyboardFrom(context, root);
        } else {
            MainActivity.createOverlayAlert("Errore", "Abbiamo riscontrato un errore nell'ottenimento del contesto di questa schermata. Si consiglia di riavviare l'app.");
        }
    }

    public static void fetchSettings() {
        if (MainActivity.context != null) {
            preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.context);
        } else {
            MainActivity.createAlert("Errore nella lettura delle impostazioni, prova a riavviare l'applicazione", BluetoothFragment.root, false);
        }
        if (!isAppFirstRun()) {
            pitchRollDelay = getPreferencesInt("pitchRollDelay");
        } else {
            setPreferencesInt("pitchRollDelay", 1000);
        }
    }

    private static int getPreferencesInt(String key) {
        int obtainedValue;
        if (!preferences.contains(key)) {
            MainActivity.createOverlayAlert("Errore", "Errore nella lettura di: " + key + ". Si consiglia di riavviare l'app.");
            obtainedValue = -1;
        } else {
            obtainedValue = preferences.getInt(key, -1);
        }
        return obtainedValue;
    }

    public static boolean isAppFirstRun() {
        if (preferences != null) {
            System.out.println("******AFR: R1: " + preferences.getBoolean("isAppFirstRun", true));
            return preferences.getBoolean("isAppFirstRun", true);
        } else {
            System.out.println("******AFR: R2");
            MainActivity.createOverlayAlert("Errore", "Errore nella lettura delle preferenze. Si consiglia di riavviare l'app.");
            return false;
        }
    }

    public static void setPreferencesBoolean(String key, boolean value) {
        if (!preferences.edit().putBoolean(key, value).commit()) {
            MainActivity.createOverlayAlert("Errore", "Errore nella scrittura di " + key + ". Si consiglia di riavviare l'app.");
        }
    }

    public static void setPreferencesInt(String key, int value) {
        if (!preferences.edit().putInt(key, value).commit()) {
            MainActivity.createOverlayAlert("Errore", "Errore nella scrittura di " + key + ". Si consiglia di riavviare l'app.");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}