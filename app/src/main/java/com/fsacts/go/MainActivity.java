package com.fsacts.go;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback{

    //Define variables
    GoogleMap mMap;
    MapView mapView;
    FloatingActionButton btn_save, img_home_main, img_list_main;
    TextView tv_date, tv_time, tv_address, tv_latitude, tv_longitude;
    Button btn_location;

    Boolean isPermissionGranted = false;

    FusedLocationProviderClient fusedLocationProviderClient;
    LocationManager locationManager;
    Location cuttentLocation;

    DBHelper databaseHelper;

    private static final LatLng MOUNTAIN_VIEW = new LatLng(37.4, -122.1);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Lock the screen to Portrait mode
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //Create reference to UI elements
        mapView = findViewById(R.id.mv_custommap);
        btn_save = findViewById(R.id.btn_save);
        tv_date = findViewById(R.id.tv_date);
        tv_time = findViewById(R.id.tv_time);
        tv_latitude = findViewById(R.id.tv_latitude);
        tv_longitude = findViewById(R.id.tv_longitude);
        tv_address = findViewById(R.id.tv_address);
        btn_location = findViewById(R.id.btn_location);
        img_home_main = findViewById(R.id.img_home_main);
        img_list_main = findViewById(R.id.img_list_main);

        btn_save.setImageTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.TINT_COLOR_DISABLE));

        //Populate map
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        mapView.getMapAsync(this);
        mapView.onCreate(savedInstanceState);

        //OnClick event for finding location
        btn_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getLocation();
            }
        });

        //OnClick event for List Image Button
        ImageButton listImageButton = (ImageButton) img_list_main.findViewById(R.id.img_list_main);
        listImageButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CustomList.class);
                startActivity(intent);
            }
        });

        //Initializing database
        databaseHelper = new DBHelper(this);

        //Save current location into sqlLight
        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (cuttentLocation != null){

                    LocationModel locationModel = null;

                    try{
                        locationModel = new LocationModel(-1, tv_date.getText().toString(), tv_time.getText().toString(), tv_latitude.getText().toString(), tv_longitude.getText().toString(), tv_address.getText().toString(), null, null);
                    }catch(Exception e){
                        Log.e("DB-Error", e.toString());
                    }

                    boolean success = databaseHelper.addOne(locationModel);

                    if(success == true){
                        Toast.makeText(MainActivity.this, "Your current location is saved", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(MainActivity.this, "This location is already being added!", Toast.LENGTH_SHORT).show();
                    }

                }else{
                    Toast.makeText(MainActivity.this, "Please track your location first", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }//End of onCreate

    //----------------------------------------------------------------------------------------------All the helper functions
    private void getLocation() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    //Populate current/local Date-Time & assign to UI elements
                    Date today = new Date();
                    SimpleDateFormat format_date = new SimpleDateFormat("MM/dd/yyyy");
                    SimpleDateFormat format_time = new SimpleDateFormat("hh:mm a");
                    String dateToStr = format_date.format(today);
                    String timeToStr = format_time.format(today);

                    tv_date.setText(dateToStr);
                    tv_time.setText(timeToStr);

                    cuttentLocation = location; //Updating the current location

                    //Now we can get the location and set the text views
                    updateUIValues(location);
                    
                }
            });
        }else{
            requestPermission();
        }
    }

    private void updateUIValues(Location location) {

        //Mark current location on Map
        LatLng lastlocation = null;

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Lat: " + location.getLatitude() + " Lon: " + location.getLongitude());
        mMap.addMarker(markerOptions);
        lastlocation = latLng;

        //Zoom to lastLocation
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lastlocation, 15.0f));

        //Enable btn_save
        btn_save.setEnabled(true);
        btn_save.setImageTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.TINT_COLOR_ENABLE));

        //Update all of the view objects with a new location
        tv_latitude.setText(String.valueOf(location.getLatitude()));
        tv_longitude.setText(String.valueOf(location.getLongitude()));

        //Address
        Geocoder geocoder = new Geocoder(MainActivity.this);
        try {
            List<Address> address = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            tv_address.setText(address.get(0).getAddressLine(0));
        }catch (Exception e){
            tv_address.setText("Unable to get street address");
        }
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 1);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 1){
            if(grantResults.length ==1 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                getLocation();
            }else{
                Toast.makeText(MainActivity.this, "Location is required! Please enable location from settings", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    //All the methods are important to populate Google map at Application initialization
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
    }

    @Override
    protected void onStart(){
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume(){
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause(){
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop(){
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        mapView.onSaveInstanceState(outState);
        Toast.makeText(MainActivity.this, "Go is accessing location", Toast.LENGTH_SHORT).show();
    }

}