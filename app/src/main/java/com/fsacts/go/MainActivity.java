package com.fsacts.go;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.CurrentLocationRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Granularity;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("MM/dd/yyyy").withLocale(Locale.US);
    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("hh:mm a").withLocale(Locale.US);

    private GoogleMap googleMap;
    private MapView mapView;
    private FloatingActionButton btnSave;
    private TextView tvDate;
    private TextView tvTime;
    private TextView tvAddress;
    private TextView tvLatitude;
    private TextView tvLongitude;
    private Button btnLocation;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private DBHelper databaseHelper;
    private Location currentLocation;
    private long capturedAtMillis;
    private ExecutorService geocodeExecutor;
    private ActivityResultLauncher<String[]> permissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mapView = findViewById(R.id.mv_custommap);
        btnSave = findViewById(R.id.btn_save);
        tvDate = findViewById(R.id.tv_date);
        tvTime = findViewById(R.id.tv_time);
        tvAddress = findViewById(R.id.tv_address);
        tvLatitude = findViewById(R.id.tv_latitude);
        tvLongitude = findViewById(R.id.tv_longitude);
        btnLocation = findViewById(R.id.btn_location);
        FloatingActionButton imgListMain = findViewById(R.id.img_list_main);
        FloatingActionButton imgHomeMain = findViewById(R.id.img_home_main);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        databaseHelper = new DBHelper(this);
        geocodeExecutor = Executors.newSingleThreadExecutor();

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        btnSave.setEnabled(false);
        btnSave.setImageTintList(ContextCompat.getColorStateList(this, R.color.TINT_COLOR_DISABLE));

        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    Boolean fineGranted = result.get(Manifest.permission.ACCESS_FINE_LOCATION);
                    Boolean coarseGranted = result.get(Manifest.permission.ACCESS_COARSE_LOCATION);
                    if (Boolean.TRUE.equals(fineGranted) || Boolean.TRUE.equals(coarseGranted)) {
                        fetchCurrentLocation();
                    } else {
                        Toast.makeText(this, "Location permission is required to track your position.", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        btnLocation.setOnClickListener(view -> getLocation());

        imgListMain.setOnClickListener(view ->
                startActivity(new Intent(MainActivity.this, CustomList.class))
        );

        imgHomeMain.setOnClickListener(view ->
                Toast.makeText(MainActivity.this, "You are already on the main screen.", Toast.LENGTH_SHORT).show()
        );

        btnSave.setOnClickListener(view -> saveCurrentLocation());
    }

    private void getLocation() {
        if (hasLocationPermission()) {
            fetchCurrentLocation();
        } else {
            permissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @SuppressLint("MissingPermission")
    private void fetchCurrentLocation() {
        CurrentLocationRequest request = new CurrentLocationRequest.Builder()
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
                .setDurationMillis(15000)
                .setMaxUpdateAgeMillis(10000)
                .build();

        fusedLocationProviderClient
                .getCurrentLocation(request, new CancellationTokenSource().getToken())
                .addOnSuccessListener(this, location -> {
                    if (location == null) {
                        Toast.makeText(this, "Unable to determine your current location. Please try again.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    updateUiValues(location);
                })
                .addOnFailureListener(this, error ->
                        Toast.makeText(this, "Location lookup failed: " + error.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void updateUiValues(@NonNull Location location) {
        currentLocation = location;
        capturedAtMillis = System.currentTimeMillis();

        Instant capturedAt = Instant.ofEpochMilli(capturedAtMillis);
        tvDate.setText(DATE_FORMATTER.format(capturedAt.atZone(ZoneId.systemDefault())));
        tvTime.setText(TIME_FORMATTER.format(capturedAt.atZone(ZoneId.systemDefault())));
        tvLatitude.setText(formatCoordinate(location.getLatitude()));
        tvLongitude.setText(formatCoordinate(location.getLongitude()));

        btnSave.setEnabled(true);
        btnSave.setImageTintList(ContextCompat.getColorStateList(this, R.color.TINT_COLOR_ENABLE));

        if (googleMap != null) {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            googleMap.clear();
            googleMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title("Current location"));
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.5f));
            if (hasLocationPermission()) {
                googleMap.setMyLocationEnabled(true);
            }
        }

        resolveAddress(location);
    }

    private void resolveAddress(@NonNull Location location) {
        if (!Geocoder.isPresent()) {
            tvAddress.setText("Lat " + formatCoordinate(location.getLatitude()) + ", Lon " + formatCoordinate(location.getLongitude()));
            return;
        }

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1, new Geocoder.GeocodeListener() {
                @Override
                public void onGeocode(@NonNull List<Address> addresses) {
                    if (addresses.isEmpty()) {
                        tvAddress.setText("Unable to get street address");
                    } else {
                        tvAddress.setText(addresses.get(0).getAddressLine(0));
                    }
                }

                @Override
                public void onError(String errorMessage) {
                    tvAddress.setText("Unable to get street address");
                }
            });
            return;
        }

        geocodeExecutor.execute(() -> {
            String addressText = "Unable to get street address";
            try {
                List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                if (addresses != null && !addresses.isEmpty()) {
                    addressText = addresses.get(0).getAddressLine(0);
                }
            } catch (Exception ignored) {
            }

            final String finalAddressText = addressText;
            runOnUiThread(() -> tvAddress.setText(finalAddressText));
        });
    }

    private void saveCurrentLocation() {
        if (currentLocation == null) {
            Toast.makeText(this, "Please track your location first.", Toast.LENGTH_SHORT).show();
            return;
        }

        LocationModel locationModel = new LocationModel(
                -1,
                capturedAtMillis,
                tvDate.getText().toString(),
                tvTime.getText().toString(),
                currentLocation.getLatitude(),
                currentLocation.getLongitude(),
                tvAddress.getText().toString(),
                "New Location",
                ""
        );

        boolean success = databaseHelper.addOne(locationModel);
        if (success) {
            Toast.makeText(this, "Your current location is saved.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "That location is already in your journal.", Toast.LENGTH_SHORT).show();
        }
    }

    private String formatCoordinate(double value) {
        return String.format(Locale.US, "%.5f", value);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        if (hasLocationPermission()) {
            googleMap.setMyLocationEnabled(true);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    protected void onStop() {
        mapView.onStop();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        mapView.onDestroy();
        geocodeExecutor.shutdownNow();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
}
