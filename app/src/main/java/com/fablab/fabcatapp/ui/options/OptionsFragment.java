package com.fablab.fabcatapp.ui.options;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.fablab.fabcatapp.MainActivity;
import com.fablab.fabcatapp.R;

import java.util.Objects;


public class OptionsFragment extends Fragment {
    public static SharedPreferences preferences;
    public static int pitchRollDelay;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_options, container, false);

        EditText pitchRollDelay = root.findViewById(R.id.pitchRollDelayEditText);
        pitchRollDelay.setText(getString(R.string.empty_string_int, getPreferencesInt("pitchRollDelay", getContext())));
        pitchRollDelay.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                try {
                    setPreferencesInt("pitchRollDelay", Integer.parseInt(pitchRollDelay.getText().toString()), getContext());
                } catch (Exception e) {
                    MainActivity.createAlert("Insert a valid number!", root, false);
                    hideOptionsFragmentKeyboard(root);
                }
            }
        });

        EditText bluetoothDiscoveryCountdown = root.findViewById(R.id.bluetoothDiscoveryCountdown);
        bluetoothDiscoveryCountdown.setText(getString(R.string.empty_string_int, getPreferencesInt("discoveryCountdown", getContext())));
        bluetoothDiscoveryCountdown.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                try {
                    setPreferencesInt("discoveryCountdown", Integer.parseInt(bluetoothDiscoveryCountdown.getText().toString()), getContext());
                } catch (Exception e) {
                    MainActivity.createAlert("Insert a valid number!", root, false);
                    hideOptionsFragmentKeyboard(root);
                }
            }
        });

        Switch debugSwitch = root.findViewById(R.id.debugSwitch);
        debugSwitch.setChecked(getPreferencesBoolean("debug", getContext()));
        debugSwitch.setOnCheckedChangeListener((v, checked) -> setPreferencesBoolean("debug", checked, getContext()));

        return root;
    }

    private void hideOptionsFragmentKeyboard(View root) {
        Context context = getContext();
        if (context != null) {
            MainActivity.hideKeyboardFrom(context, root, context);
        } else {
            MainActivity.createOverlayAlert("Error", "Couldn't get the context of this view. It is recommended to restart the app.", Objects.requireNonNull(getActivity()).getApplicationContext());
        }
    }

    public static void fetchSettings(Context applicationContext) {
        preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);

        if (isAppFirstRun(applicationContext)) {
            setPreferencesInt("pitchRollDelay", 1000, applicationContext);
            setPreferencesInt("discoveryCountdown", 5, applicationContext);
            setPreferencesBoolean("debug", false, applicationContext);
        }
    }

    private static int getPreferencesInt(String key, Context applicationContext) {
        int obtainedValue;
        if (!preferences.contains(key)) {
            MainActivity.createOverlayAlert("Error", "Error while reading: " + key + ". It is recommended to restart the app.", applicationContext);
            obtainedValue = -1;
        } else {
            obtainedValue = preferences.getInt(key, -1);
        }
        return obtainedValue;
    }

    public static boolean isAppFirstRun(Context applicationContext) {
        if (preferences != null) {
            System.out.println("******AFR: R1: " + preferences.getBoolean("isAppFirstRun", true));
            return preferences.getBoolean("isAppFirstRun", true);
        } else {
            System.out.println("******AFR: R2");
            MainActivity.createOverlayAlert("Error", "Error while fetching preferences. It is recommended to restart the app.", applicationContext);
            return false;
        }
    }

    public static void setPreferencesBoolean(String key, boolean value, Context applicationContext) {
        if (preferences != null) {
            if (!preferences.edit().putBoolean(key, value).commit()) {
                MainActivity.createOverlayAlert("Error", "Error while writing: " + key + ". It is recommended to restart the app.", applicationContext);
            }
        } else {
            MainActivity.createPreferencesErrorAlert("Error", "It isn't possible for the app to save or read settings anymore due to an unknown error, that occurred during the app startup", applicationContext);
        }
    }

    public static boolean getPreferencesBoolean(String key, Context applicationContext) {
        if (preferences == null) {
            MainActivity.createPreferencesErrorAlert("Error", "It isn't possible for the app to save or read settings anymore due to an unknown error, that occurred during the app startup", applicationContext);
            return false;
        } else {
            return preferences.getBoolean(key, false);
        }

    }

    private static void setPreferencesInt(String key, int value, Context applicationContext) {
        if (preferences != null) {
            if (!preferences.edit().putInt(key, value).commit()) {
                MainActivity.createOverlayAlert("Error", "Error while writing: " + key + ". It is recommended to restart the app.", applicationContext);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}