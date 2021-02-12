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
import android.widget.TextView;

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
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Date;
import java.util.List;

import static ru.locarus.androidtrackerapp.Constants.TAG;

public class LocationService extends Service {
    private SharedPreferences sharedPreferences;
    private Thread threadSocket;
    private Thread pointsAdd;
    private int interval;
    private Socket socket;
    private BufferedReader in; // поток чтения из сокета
    private BufferedWriter out; // поток чтения в сокет
    private String SERVER_IP;
    private int SERVER_PORT;
    private List<Point> list;
    private double latitude = 0;
    private double longitude = 0;
    private float speed = 0;
    private long time = 0 ;
    private double altitude = 0;





    private final PointsDbOpenHelper pointsDbOpenHelper = new PointsDbOpenHelper(this);

    private LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            if (locationResult != null && locationResult.getLastLocation() != null) {
                    sendStatus(Constants.ACTION_START_LOCATION_SERVICE);
                    latitude = locationResult.getLastLocation().getLatitude();
                    longitude= locationResult.getLastLocation().getLongitude();
                    speed = locationResult.getLastLocation().getSpeed();
                    time  = locationResult.getLastLocation().getTime();
                    altitude = locationResult.getLastLocation().getAltitude();
            } else {
                sendStatus(Constants.ACTION_STOP_LOCATION_SERVICE);
                Log.d(TAG, "Point = null");
            }
        }
    };

    private void sendStatus (String string) {
        Intent intent = new Intent("Status");
        intent.putExtra("status", string);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void sendMessage(Point point) {

        Intent intent = new Intent("custom");
        // You can also include some extra data.
        intent.putExtra("point", point);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

    }
    public void dBWorker(){
        long tmp = 0;
        while (!Thread.currentThread().isInterrupted()) {
            if (latitude!= 0) {
                if (time!= tmp) {
                    synchronized (pointsDbOpenHelper) {
                        pointsDbOpenHelper.addPoint(new Point(latitude, longitude, speed, time, altitude));
                    }
                    Log.d(TAG, "Latitude: " + latitude + " Longitude " + longitude + " Time: " + time);
                    sendMessage(new Point(latitude, longitude));
                    tmp = time;
                } else {
                    synchronized (pointsDbOpenHelper) {
                        pointsDbOpenHelper.addPoint(new Point(latitude, longitude, speed, new Date().getTime(), altitude));
                    }
                    Log.d(TAG, "Latitude: " + latitude + " Longitude " + longitude + " Time: " + new Date().getTime());
                    sendMessage(new Point(latitude, longitude));
                }
            }
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                Log.d(TAG, "Thread sleep "+ e);
            }
        }
    }
    public void serverWork() {
        while (!Thread.currentThread().isInterrupted()) {
            SERVER_IP = sharedPreferences.getString("server_address", "lserver1.ru");
            SERVER_PORT = Integer.parseInt(sharedPreferences.getString("port_address", "1145"));
            closeConnection();
            try {
                socket = new Socket();
                socket.connect(new InetSocketAddress(SERVER_IP, SERVER_PORT), 10000);
                sendStatus("start");
                // потоки чтения из сокета / записи в сокет, и чтения с консоли
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                String str = "";
                    // Пакет авторизации
                String crc = Crc16.crc16("2.0;1111111111;NA;");
                String result = "#L#2.0;1111111111;NA;" + crc + "\r\n";
                out.write(result);
                out.flush();// отправляем на сервер
                // ждем сообщения с сервера
                str = in.readLine();

                if (str != null) {
                    Log.d(TAG, "Ответ авторизации: " + str);
                }
                if (str == null) {
                    Log.d(TAG, "No response");

                    } else {
                        if (str.equals("#AL#1")) {

                            StringBuilder stringBuilder = new StringBuilder();
                            synchronized (pointsDbOpenHelper) {
                                list = pointsDbOpenHelper.getAllPoints();
                            }
                            if (!list.isEmpty()) {
                                for (Point point : list) {
                                    if (point.getLongitude() < 180 && point.getLatitude() < 180) {
                                        stringBuilder.append(GettingPointsString.getString(point));
                                    }
                                }
                                String send = "#B#" + stringBuilder.toString() + Crc16.crc16(stringBuilder.toString()) + "\r\n";
                                Log.d(TAG, send);
                                out.write(send);
                                out.flush();
                                str = in.readLine();
                                Log.d(TAG, "Response: " + str);
                                if (str != null) {
                                    synchronized (pointsDbOpenHelper) {
                                        for (Point point : list) {
                                            pointsDbOpenHelper.deletePoint(point);
                                        }
                                    }
                                }
                            }
                        }
                    }

                    try {
                        Thread.sleep(60000);
                    } catch (InterruptedException e) {
                        Log.d(TAG, "Interrupt: " + e);
                        sendStatus("stop");
                    }
        } catch (IOException e) {
            Log.d(TAG, "Socket closed " + e.getMessage());
                closeConnection();
                sendStatus("stop");
                try {
                    Thread.sleep(60000);
                } catch (InterruptedException ie) {
                    Log.d(TAG, "Thread sleep: " + ie);
                }
        }

    }
    }
    private void closeConnection(){
        if (socket!=null&&!socket.isClosed()){
            try{
                socket.close();
            }catch (IOException e){
                Log.d(TAG, "Ошибка при закрытии сокета" + e.getMessage());
            } finally {
                socket = null;
            }
        }
        socket = null;
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
        Intent resultIntent = new Intent(this,MainActivity.class);
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

        interval = Integer.parseInt(sharedPreferences.getString("frequency", "10"));
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(interval*1000);
        locationRequest.setFastestInterval(2000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
        threadSocket.start();
        pointsAdd.start();
    }
    private void stopLocationService() {
        if (threadSocket!=null&&pointsAdd!=null) {
            threadSocket.interrupt();
            pointsAdd.interrupt();
        }
        LocationServices.getFusedLocationProviderClient(this)
                .removeLocationUpdates(locationCallback);
        stopForeground(true);
        stopSelf();
        sendStatus(Constants.ACTION_STOP_LOCATION_SERVICE);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (threadSocket!=null&&pointsAdd!=null) {
            threadSocket.interrupt();
            pointsAdd.interrupt();
        }
        closeConnection();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
         sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        if (intent!= null){
            String action = intent.getAction();
            if(action != null){
                if(action.equals((Constants.ACTION_START_LOCATION_SERVICE))){
                    threadSocket =new MyThread(this::serverWork);
                    pointsAdd =new MyThread(this::dBWorker);
                    startLocationService();
                }else if(action.equals(Constants.ACTION_STOP_LOCATION_SERVICE)){
                    stopLocationService();
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }
}
