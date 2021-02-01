package ru.locarus.androidtrackerapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.util.Hex;

import java.util.List;

import static ru.locarus.androidtrackerapp.Constants.TAG;

public class MainActivity extends AppCompatActivity {
    private TextView textView;
    private Button start;
    private String LocationUpdateTime;
    private static final int REQUEST_CODE_LOCATION_PERMISSION = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PointsDbOpenHelper pointsDbOpenHelper = new PointsDbOpenHelper(this);


        textView = findViewById(R.id.textView2);
        start = findViewById(R.id.button_start);



        start.setOnClickListener(view -> {
            if (start.getText().equals("Старт")) {
                if (ContextCompat.checkSelfPermission(
                        getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_LOCATION_PERMISSION);
                } else {
                    Log.d("LOCATION", "старт");
                    startLocationService();
                    start.setText("СТОП");
                }
            } else {
                stopLocationService();
                start.setText("Старт");

            }
        });

    }
    public void onClickMap(View view) {
        Intent map = new Intent(this,MapsActivity.class);
        startActivity(map);
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

            Log.d("LOCATION", "startLocationService");
            Intent intent = new Intent(getApplicationContext(),LocationService.class);
            intent.setAction(Constants.ACTION_START_LOCATION_SERVICE);
            startService(intent);
            Toast.makeText(this,"Location setvice started", Toast.LENGTH_SHORT).show();

    }
    private void stopLocationService (){
        Log.d("LOCATION", "stoptLocationService");
            Intent intent = new Intent(getApplicationContext(),LocationService.class);
            intent.setAction(Constants.ACTION_STOP_LOCATION_SERVICE);
            startService(intent);
            Toast.makeText(this,"Location setvice stopped", Toast.LENGTH_SHORT).show();

    }

    public void onClickSettings(View view) {
        Intent map = new Intent(this,SettingsActivity.class);
        startActivity(map);
    }
}