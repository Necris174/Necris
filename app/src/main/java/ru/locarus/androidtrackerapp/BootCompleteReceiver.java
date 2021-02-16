package ru.locarus.androidtrackerapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.preference.PreferenceManager;

public class BootCompleteReceiver extends BroadcastReceiver {



    @Override
    public void onReceive(Context context, Intent intent) {
                SharedPreferences sharedPreferences = context.getSharedPreferences("ButtonStatus", Context.MODE_PRIVATE);
                String


            Intent service = new Intent(context,LocationService.class);
            service.setAction(Constants.ACTION_START_LOCATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(service);
        } else {
            context.startService(service);
        }
    }
}
