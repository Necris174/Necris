package ru.locarus.androidtrackerapp;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import static ru.locarus.androidtrackerapp.Constants.TAG;

public class LocationService extends Service {
    Thread threadSocket;
    private int interval;
    private String action;
    private Socket socket;
    private BufferedReader in; // поток чтения из сокета
    private BufferedWriter out; // поток чтения в сокет
    private String SERVER_IP = "91.201.40.21";
    private int SERVER_PORT = 1145;

    PointsDbOpenHelper pointsDbOpenHelper = new PointsDbOpenHelper(this);


    private LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            if (locationResult != null && locationResult.getLastLocation() != null) {

                Point point = new Point(locationResult.getLastLocation().getLatitude(),
                        locationResult.getLastLocation().getLongitude(),
                        locationResult.getLastLocation().getSpeed(),
                        locationResult.getLastLocation().getTime(),locationResult.getLastLocation().getAltitude());
                pointsDbOpenHelper.addPoint(point);
                Log.d(TAG, point.getLatitude() + ", " + point.getLatitude() + " " + point.getAltitude());
                sendMessage(point);

                Log.d(TAG, "Широта: " + pointsDbOpenHelper.getPoint(1).getLatitude()+
                        "Долгота: "+ pointsDbOpenHelper.getPoint(1).getLongitude()+
                        "Скороть: " + pointsDbOpenHelper.getPoint(1).getSpeed());

            }
        }
    };

    private void sendMessage(Point point) {

        Intent intent = new Intent("custom");
        // You can also include some extra data.
        intent.putExtra("point", point);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

    }

    public void serverWork() {
        try {
            socket = new Socket(SERVER_IP, SERVER_PORT);
        } catch (IOException e) {
            Log.d(TAG, "TCPClient: Socket failed");
        }
        try {

            // потоки чтения из сокета / записи в сокет, и чтения с консоли
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            String str;
            while (true){
                String crc = Crc16.crc16("2.0;1111111111;NA;");
                String result = "#L#2.0;1111111111;NA;"+ crc +"\r\n";
                Log.d(TAG, result);
                out.write(result);

                out.flush();// отправляем на сервер
                str = in.readLine(); // ждем сообщения с сервера
                Log.d(TAG,"Server response " + str) ;
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            try {
                if (!socket.isClosed()) {
                    socket.close();
                    in.close();
                    out.close();
                }
            } catch (IOException ignored) {}
        }
    }

    

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void startLocationService() {
        String channelid = "location_notification_channel";
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Intent resultIntent = new Intent();
        PendingIntent pendingIntent = PendingIntent.getActivity(
                getApplicationContext(),
                0,
                resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                getApplicationContext(),
                channelid
        );
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle("Location Service");
        builder.setDefaults(NotificationCompat.DEFAULT_ALL);
        builder.setContentText("Running");
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(false);
        builder.setPriority(NotificationCompat.PRIORITY_MAX);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager != null
                    && notificationManager.getNotificationChannel(channelid) == null) {
                NotificationChannel notificationChannel = new NotificationChannel(channelid,
                        "Location Service",
                        NotificationManager.IMPORTANCE_HIGH
                );
                notificationChannel.setDescription("This channel is used by location service");
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        interval = Integer.parseInt(sharedPreferences.getString("frequency", "500000"));
        Log.d(TAG,interval + "");
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(4000);
        locationRequest.setFastestInterval(2000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.getFusedLocationProviderClient(this)
                .requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        startForeground(Constants.LOCATION_SERVICE_ID,builder.build());
    }
    private void stopLocationService() {
        LocationServices.getFusedLocationProviderClient(this)
                .removeLocationUpdates(locationCallback);
        stopForeground(true);
        stopSelf();
        threadSocket.interrupt();
    }







    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        threadSocket =new Thread(new Runnable() {
            @Override
            public void run() {
                serverWork();
            }
        });
        threadSocket.start();
        if (intent!= null){
            String action = intent.getAction();
            if(action != null){
                if(action.equals((Constants.ACTION_START_LOCATION_SERVICE))){
                    startLocationService();
                }else if(action.equals(Constants.ACTION_STOP_LOCATION_SERVICE)){
                    stopLocationService();


                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }
}
