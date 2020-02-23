package com.fablab.fabcatapp;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;

import com.fablab.fabcatapp.bluetooth.BluetoothConnect;
import com.fablab.fabcatapp.ui.options.OptionsFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    public static Context context;
    private static String currentPermissionRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(BluetoothConnect::sendCustomCommand); //equivale a (view) -> BluetoothConnect.sendCustomCommand(view);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_bluetooth, R.id.nav_home, R.id.nav_options)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        preInitPermissionCheck();

        context = this;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        OptionsFragment.fetchSettings();
        if (OptionsFragment.isAppFirstRun()) {
            OptionsFragment.setPreferencesBoolean("isAppFirstRun", false);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        menu.getItem(0).setOnMenuItemClickListener(item -> {
            BluetoothConnect.cat.reset();
            return false;
        });
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
    private void preInitPermissionCheck() {
        if (!checkLocationPermission()) {
            System.out.println("*****PERMESSO GPS NON AVUTO");
            currentPermissionRequest = "GPS";
            ActivityCompat.requestPermissions(this, new String[]{
                    android.Manifest.permission.ACCESS_FINE_LOCATION //AGGIUNGI PERMESSO A MANIFEST SEMPRE
            }, 1);
        } else {
            System.out.println("*****PERMESSO GPS AVUTO");
        }
        if (!checkBluetoothPermission()) {
            System.out.println("******PERMESSO DEL BLUETOOTH NON AVUTO");
            currentPermissionRequest = "BLUETOOTH";
            ActivityCompat.requestPermissions(this, new String[] {
                    Manifest.permission.BLUETOOTH
            }, 1);
            currentPermissionRequest = "BLUETOOTH_ADMIN";
            ActivityCompat.requestPermissions(this, new String[] {
                    Manifest.permission.BLUETOOTH_ADMIN
            }, 1);
        } else {
            System.out.println("****PERMESSO BLUETOOTH AVUTO");
        }
    }

    private boolean checkBluetoothPermission() {
        String permission = "android.permission.BLUETOOTH";
        int res = this.checkCallingOrSelfPermission(permission);
        String permission2 = "android.permission.BLUETOOTH_ADMIN";
        int res2 = this.checkCallingOrSelfPermission(permission2);
        return (res == PackageManager.PERMISSION_GRANTED && res2 == PackageManager.PERMISSION_GRANTED);
    }

    private boolean checkLocationPermission(){

        String permission = "android.permission.ACCESS_FINE_LOCATION";

        int res = this.checkCallingOrSelfPermission(permission);

        return (res == PackageManager.PERMISSION_GRANTED);

    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        System.out.println("*****CONTROLLO PERMESSI ESEGUITO");

        if (requestCode == 1) {

            if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                switch (currentPermissionRequest) {
                    case "GPS":
                        exitDueTo("GPS_PERMISSION");
                        break;
                    case "BLUETOOTH":
                    case "BLUETOOTH_ADMIN":
                        exitDueTo("BLUETOOTH_PERMISSION");
                        break;
                    default:
                        exitDueTo("UNKNOWN_PERMISSION_ERROR");
                        break;
                }

            }

        }
    }

    public void exitDueTo(@NonNull String cause) {
        switch (cause) {
            case "GPS_PERMISSION": {
                new AlertDialog.Builder(context).setTitle("Avvio fallito").setMessage("L'app necessita l'accesso al GPS per funzionare.").setPositiveButton(android.R.string.yes, (dialog, which) -> finishAndRemoveTask()).show();
            }
            break;
            case "BLUETOOTH_PERMISSION": {
                new AlertDialog.Builder(context).setTitle("Avvio fallito").setMessage("L'app necessita l'accesso al bluetooth per funzionare.").setPositiveButton(android.R.string.yes, (dialog, which) -> finishAndRemoveTask()).show();
            }
            break;
            case "UNKNOWN_PERMISSION_ERROR": {
                new AlertDialog.Builder(context).setTitle("Errore critico").setMessage("A causa di un errore sconosciuto nella richiesta dei permessi l'app non puó funzionare. É necessario un riavvio.").setPositiveButton(android.R.string.yes, (dialog, which) -> finishAndRemoveTask()).show();
            }

            break;
        }
    }

    public static void createAlert(String message, View view) {
        Snackbar.make(view, message, Snackbar.LENGTH_LONG).setAction("Action", null).show();
    }

    public static void createOverlayAlert(String title, String message) {
        if (MainActivity.context != null) {
            new AlertDialog.Builder(MainActivity.context, R.style.DialogTheme).setTitle(title).setMessage(message).setPositiveButton(android.R.string.yes, null).show();
        }
    }

    public static void createCriticalErrorAlert(String title, String message) {
        new AlertDialog.Builder(MainActivity.context, R.style.DialogTheme).setTitle(title).setMessage(message).setPositiveButton("Riavvia", (dialog, which) -> {
                    android.os.Process.killProcess(android.os.Process.myPid());
                    System.exit(1);
                }
        ).show();
    }

    public static void hideKeyboardFrom(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        } else {
            createOverlayAlert("Errore", "Abbiamo riscontrato un errore nella rimozione della tastiera.");
        }
    }


    @Override //quando si clicca fuori da un edittext perde focus e chiama l'event listener
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if ( v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int)event.getRawX(), (int)event.getRawY())) {
                    System.out.println("*********TOUCH EVENT");
                    v.clearFocus();
                    hideKeyboardFrom(this, v);
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }
}
