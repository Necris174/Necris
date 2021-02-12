package ru.locarus.androidtrackerapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.util.Hex;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

import static ru.locarus.androidtrackerapp.Constants.TAG;

public class MainActivity extends AppCompatActivity {
    private TextView textView;
    private Button start;
    private String LocationUpdateTime;
    private static final int REQUEST_CODE_LOCATION_PERMISSION = 1;
    private List<Point> list;
    private TextView viewServerConnection;
    private TextView viewLocationService;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PointsDbOpenHelper pointsDbOpenHelper = new PointsDbOpenHelper(this);
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("Status"));
        sharedPreferences = this.getSharedPreferences("ButtonStatus", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();


        start = findViewById(R.id.button_start);
        start.setText(sharedPreferences.getString("ButtonStatus","Старт"));
        viewLocationService = findViewById(R.id.location);
        viewServerConnection = findViewById(R.id.server);


        start.setOnClickListener(view -> {
            if (start.getText().equals("Старт")) {
                if (ContextCompat.checkSelfPermission(
                        getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_LOCATION_PERMISSION);
                } else {
                    startLocationService();
                    start.setText("Стоп");
                    editor.putString("ButtonStatus", start.getText().toString());
                    editor.apply();
                }
            } else {
                stopLocationService();
                start.setText("Старт");
                editor.putString("ButtonStatus", start.getText().toString());
                editor.apply();

            }
        });



    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        editor.putString("ButtonStatus", start.getText().toString());
        editor.apply();
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String s = intent.getStringExtra("status");
            if (s.equals(Constants.ACTION_START_LOCATION_SERVICE)){
                viewLocationService.setTextColor(Color.GREEN);
            } else if (s.equals(Constants.ACTION_STOP_LOCATION_SERVICE)){
                viewLocationService.setTextColor(Color.RED);
            } else if (s.equals("start")){
                viewServerConnection.setTextColor(Color.GREEN);
            }else if (s.equals("stop")){
                viewServerConnection.setTextColor(Color.RED);
            }
        }
    };
    public void onClickMap(View view) {
        Intent map = new Intent(this,MapsActivity.class);
        startActivity(map);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("StartOrStop", start.getText().toString());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_LOCATION_PERMISSION && grantResults.length>0){
            if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
                startLocationService();
            } else {
                Toast.makeText(this,"Permissions denied",Toast.LENGTH_SHORT).show();
            }
        }
    }



    private void startLocationService(){
            Log.d(TAG, "startLocationService");
            Intent intent = new Intent(getApplicationContext(),LocationService.class);
            intent.setAction(Constants.ACTION_START_LOCATION_SERVICE);
            startService(intent);
            Toast.makeText(this,"Location service started", Toast.LENGTH_SHORT).show();
    }
    private void stopLocationService (){
        Log.d(TAG, "stopLocationService");
            Intent intent = new Intent(getApplicationContext(),LocationService.class);
            intent.setAction(Constants.ACTION_STOP_LOCATION_SERVICE);
            startService(intent);
            Toast.makeText(this,"Location service stopped", Toast.LENGTH_SHORT).show();

    }

    public void onClickSettings(View view) {
        Intent map = new Intent(this,SettingsActivity.class);
        startActivity(map);
    }
}