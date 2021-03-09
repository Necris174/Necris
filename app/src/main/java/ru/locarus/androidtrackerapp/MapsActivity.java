package ru.locarus.androidtrackerapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.fragment.app.FragmentActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CustomCap;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private Double lat = 55.189769;
    private Double lon = 61.365417;
    private Point point;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    List <LatLng> latlnglist = new ArrayList<>();


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
        }
        return super.onOptionsItemSelected(item);
    }
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
         // Стрелка выхода из Map
        ActionBar actionBar = this.getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        sharedPreferences = this.getSharedPreferences("LatLng", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("custom"));

    }
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
                 point = (Point) intent.getExtras().getSerializable("point");
                 lat = point.getLatitude();
                 lon = point.getLongitude();
                 Log.d("Array", "Size" + latlnglist.size() + "");
                 if (latlnglist.size()>2){
                     latlnglist.remove(0);
                     latlnglist.add(new LatLng(lat,lon));
                     Log.d("Array", "Array: " + latlnglist.get(0) + latlnglist.get(1) + latlnglist.get(2));
                 }
                 else {
                     latlnglist.add(new LatLng(lat,lon));

                     Log.d("Array", "Array 1:  " + latlnglist.toString());
                 }

                 switch (latlnglist.size()){
                     case (1):

                         drawPoint();
                         break;
                     case (2):
                         if(latlnglist.get(0).equals(latlnglist.get(1))){
                             drawPoint();
                         } else {
                             Log.d("Array", "Рисуем 2 точки");
                             drawTrek();
                         }
                         break;
                     case (3):
                         if(latlnglist.get(0).equals(latlnglist.get(1))&&latlnglist.get(1).equals(latlnglist.get(2))){
                             drawPoint();
                         } else {
                             Log.d("Array", "Рисуем 3 точки");
                             drawTrek();
                         }
                         drawTrek();
                         break;
                 }
//               if (latlnglist.size()>2) {
//                   mMap.clear();
//                   Polyline polyline1 = mMap.addPolyline(new PolylineOptions()
//                           .clickable(true)
//                           .addAll(latlnglist));
//                   polyline1.setTag("tes1");
//                   polyline1.setEndCap(new CustomCap(BitmapDescriptorFactory.fromResource(R.drawable._34324)));
//                   polyline1.setWidth(12);
//                   polyline1.setColor(0xff000000);
//                   polyline1.setJointType(JointType.ROUND);
//
//                   // Position the map's camera near Alice Springs in the center of Australia,
//                   // and set the zoom factor so most of Australia shows on the screen.
//                   mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlnglist.get(0), 12));
//               } else {
//                   mMap.clear();
//                   mMap.addMarker(new MarkerOptions()
//                           .position(latlnglist.get(0))
//                           .title("Marker in Sydney")
//                           .icon(BitmapDescriptorFactory.fromResource(R.drawable.pngwing)));
//            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlnglist.get(0),12f));
//            mMap.moveCamera(CameraUpdateFactory.newLatLng(latlnglist.get(0)));
//               }
//                 mMap.clear();
//            mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(sydney,12f));
//            mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
           }
    };

     public void drawTrek (){
         mMap.clear();
         Polyline polyline1 = mMap.addPolyline(new PolylineOptions()
                 .clickable(true)
                 .addAll(latlnglist));
         polyline1.setTag("tes1");
         polyline1.setEndCap(new CustomCap(BitmapDescriptorFactory.fromResource(R.drawable._34324)));
         polyline1.setWidth(12);
         polyline1.setColor(0xff000000);
         polyline1.setJointType(JointType.ROUND);
         mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlnglist.get(0), 12));
     }
     public void drawPoint(){
         Log.d("Array", "Рисуем точку");
        mMap.clear();
         mMap.addMarker(new MarkerOptions()
                 .position(latlnglist.get(0))
                 .title("Marker in Sydney")
                 .icon(BitmapDescriptorFactory.fromResource(R.drawable.pngwing)));
         mMap.moveCamera(CameraUpdateFactory.newLatLng(latlnglist.get(0)));
         mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlnglist.get(0),12f));
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onDestroy();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.addMarker(new MarkerOptions()
                .position(new LatLng(lat,lon))
                .title("Marker in Sydney")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.pngwing)));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(lat,lon)));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat,lon),12));

        }

}