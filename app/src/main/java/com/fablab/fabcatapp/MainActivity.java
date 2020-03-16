package com.fablab.fabcatapp;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;

import com.fablab.fabcatapp.ui.bluetooth.BluetoothFragment;
import com.fablab.fabcatapp.ui.options.OptionsFragment;
import com.google.android.material.snackbar.Snackbar;

import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
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

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private AppBarConfiguration mAppBarConfiguration;
    private static String currentPermissionRequest;
    private static boolean canCreateSnackBar = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("DarkTheme", false)) {
            setTheme(R.style.DarkTheme);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        mAppBarConfiguration = new AppBarConfiguration.Builder(R.id.nav_bluetooth, R.id.nav_home, R.id.nav_options, R.id.nav_motors, R.id.nav_positions, R.id.nav_functions).setDrawerLayout(drawer).build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        preInitPermissionCheck();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        OptionsFragment.fetchSettings(this);
        if (OptionsFragment.isAppFirstRun(this)) {
            OptionsFragment.setPreferencesBoolean("isAppFirstRun", false, this);
        }

        Context context = this;
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                if (BluetoothFragment.connectionUnexpectedlyClosed) {
                    new Handler(Looper.getMainLooper()).post(() -> MainActivity.createOverlayAlert("Disconnected", OptionsFragment.getPreferencesBoolean("debug", context) ? "InStream interrupted Cause: " + BluetoothFragment.latestException.getMessage() + "\nStack: " + Arrays.toString(BluetoothFragment.latestException.getStackTrace()) : "Connection closed by the remote host.", context));
                    BluetoothFragment.connectionUnexpectedlyClosed = false;
                    BluetoothFragment.latestException = null;
                }
            }
        };
        Timer timer = new Timer();
        timer.schedule(timerTask, 0, 5000);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
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
            System.out.println("*****GPS PERMISSION NOT RECEIVED");
            currentPermissionRequest = "GPS";
            ActivityCompat.requestPermissions(this, new String[]{ //always add permission to manifest.xml
                    android.Manifest.permission.ACCESS_FINE_LOCATION
            }, 1);
        } else {
            System.out.println("*****GPS PERMISSION RECEIVED");
        }
        if (!checkBluetoothPermission()) {
            System.out.println("******BLUETOOTH PERMISSION NOT RECEIVED");
            currentPermissionRequest = "BLUETOOTH";
            ActivityCompat.requestPermissions(this, new String[] {
                    Manifest.permission.BLUETOOTH
            }, 1);
            currentPermissionRequest = "BLUETOOTH_ADMIN";
            ActivityCompat.requestPermissions(this, new String[] {
                    Manifest.permission.BLUETOOTH_ADMIN
            }, 1);
        } else {
            System.out.println("****BLUETOOTH PERMISSION RECEIVED");
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

        System.out.println("*****PERMISSION CHECK DONE");

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
                new AlertDialog.Builder(getApplicationContext()).setTitle("Start failure").setMessage("The app requires the GPS to work.").setPositiveButton(android.R.string.yes, (dialog, which) -> finishAndRemoveTask()).show();
            }
            break;
            case "BLUETOOTH_PERMISSION": {
                new AlertDialog.Builder(getApplicationContext()).setTitle("Start failure").setMessage("The app requires the Bluetooth to work.").setPositiveButton(android.R.string.yes, (dialog, which) -> finishAndRemoveTask()).show();
            }
            break;
            case "UNKNOWN_PERMISSION_ERROR": {
                new AlertDialog.Builder(getApplicationContext()).setTitle("Critical error").setMessage("Due to an unknown error in the permission request the app can't work. A restart is necessary.").setPositiveButton(android.R.string.yes, (dialog, which) -> finishAndRemoveTask()).show();
            }

            break;
        }
    }

    public static void createAlert(String message, View view, boolean wait) {
        if (canCreateSnackBar) {
            Snackbar.make(view, message, Snackbar.LENGTH_LONG).setAction("Action", null).show();
            if (wait) {
                waitForSnackBarClosure();
            }
        }
    }

    private static void waitForSnackBarClosure() {
        //if we do thread.sleep the touch is disabled, but the snackBar doesn't pop out
        canCreateSnackBar = false;

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                canCreateSnackBar = true;
            }
        }, 1000);
    }

    public static void createOverlayAlert(String title, String message, Context applicationContext) {
        if (applicationContext != null) {
            new AlertDialog.Builder(applicationContext, OptionsFragment.getPreferencesBoolean("DarkTheme", applicationContext) ? R.style.Theme_AppCompat_Light_Dialog : R.style.DialogTheme).setTitle(title).setMessage(message).setPositiveButton(android.R.string.yes, null).show();

        }
    }

    public static void createCriticalErrorAlert(String title, String message, Context applicationContext) {
        AlertDialog.Builder builder = new AlertDialog.Builder(applicationContext, OptionsFragment.getPreferencesBoolean("DarkTheme", applicationContext) ?  R.style.Theme_AppCompat_Light_Dialog : R.style.DialogTheme).setTitle(title).setMessage(message).setPositiveButton("Restart", (dialog, which) -> {
                    Intent splashScreenActivity = new Intent(applicationContext, SplashScreen.class);
                    int intentId = 111111; //random number
                    PendingIntent appRestartIntent = PendingIntent.getActivity(applicationContext, intentId,    splashScreenActivity, PendingIntent.FLAG_CANCEL_CURRENT);
                    AlarmManager alarmManager = (AlarmManager)applicationContext.getSystemService(Context.ALARM_SERVICE);
                    if (alarmManager != null) {
                        alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + 100, appRestartIntent);
                    }
                    android.os.Process.killProcess(android.os.Process.myPid());
                    System.exit(1);
                }
        );
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.show();
    }

    public static void createPreferencesErrorAlert(String title, String message, Context applicationContext) {
        new AlertDialog.Builder(applicationContext, R.style.DialogTheme).setTitle(title).setMessage(message).setPositiveButton("Attempt automatic fix", (dialog, which) -> {
                    OptionsFragment.preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);
                    if (OptionsFragment.preferences != null) {
                        createOverlayAlert("Success", "The automatic fix worked! :D", applicationContext);
                    }
                }
        ).setNegativeButton("Restart the app", (dialog, which) -> {
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
        }).setNeutralButton("Ignore", null).show();
    }

    public static void hideKeyboardFrom(Context context, View view, Context applicationContext) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        } else {
            createOverlayAlert("Error", "We encountered an error while removing the keyboard.", applicationContext);
        }
    }

    @Override //when you click out of an EditText, it loses focus and it calls the event listener
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (BluetoothFragment.connectionUnexpectedlyClosed) {
            createOverlayAlert("Disconnected", OptionsFragment.getPreferencesBoolean("debug", this) ? "InStream interrupted Cause: " + BluetoothFragment.latestException.getMessage() + "\nStack: " + Arrays.toString(BluetoothFragment.latestException.getStackTrace()) : "Connection closed by the remote host.", this);
            BluetoothFragment.connectionUnexpectedlyClosed = false;
            BluetoothFragment.latestException = null;
        }
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    v.clearFocus();
                    hideKeyboardFrom(this, v, getApplicationContext());
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }

    public static void createNotification(String notificationTitle, String notificationText, String notificationTitleDetailMode, String notificationsTextDetailMode, Context context) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context.getApplicationContext(), "notify_001");
        Intent ii = new Intent(context.getApplicationContext(), MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, ii, 0);

        NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();
        bigText.bigText(notificationsTextDetailMode); //detail mode is the "expanded" notification
        bigText.setBigContentTitle(notificationTitleDetailMode);
        bigText.setSummaryText(SplashScreen.compactAppVersion);

        mBuilder.setContentIntent(pendingIntent);
        mBuilder.setSmallIcon(R.mipmap.ic_launcher);
        mBuilder.setContentTitle(notificationTitle);
        mBuilder.setContentText(notificationText);
        mBuilder.setPriority(Notification.PRIORITY_MAX);
        mBuilder.setStyle(bigText);

        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("notify_001",
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            if (mNotificationManager != null) {
                mNotificationManager.createNotificationChannel(channel);
            }
        }

        if (mNotificationManager != null) {
            mNotificationManager.notify(0, mBuilder.build());
        }
    }
}
