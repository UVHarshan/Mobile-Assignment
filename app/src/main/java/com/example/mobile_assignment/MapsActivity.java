package com.example.mobile_assignment;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentContainerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.mobile_assignment.databinding.ActivityMapsBinding;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private LocationListener locationListener;
    private LocationManager locationManager;
    private final long MIN_TIME = 1000;
    private final long MIN_DISTANCE = 5;
    private LatLng latLng;

    Button btnSend, btnStopAlert;
    private double lat;
    private double lng;

    EditText editTextPhone;
    private Handler handler;
    private Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                PackageManager.PERMISSION_GRANTED);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                PackageManager.PERMISSION_GRANTED);

        ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.SEND_SMS}, PackageManager.PERMISSION_GRANTED);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                try {
                    latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(latLng).title("Latitude :" + latLng.latitude + " & Longitude :" + latLng.longitude));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                } catch (SecurityException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }
        };
        try {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, locationListener);
        } catch (SecurityException e) {
            e.printStackTrace();
        }

        handler = new Handler();
        btnSend = findViewById(R.id.button);
        btnStopAlert = findViewById(R.id.button2);
        editTextPhone = findViewById(R.id.editTextPhone);

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                repeatSms();
            }
        });

        btnStopAlert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (runnable != null)
                    handler.removeCallbacks(runnable);
            }
        });

    }

    private void repeatSms() {
        runnable = new Runnable() {
            @Override
            public void run() {
                String mobileNo = editTextPhone.getText().toString();
                if (lat != 0 && lng != 0) {
                    String message = "Uvindu Gunasekara IM/2017/025. Please help me" + "\n" + "http://maps.google.com/maps?saddr=" + lat + "," + lng;
                    SmsManager smsManager = SmsManager.getDefault();
                    StringBuffer smsBody = new StringBuffer();
                    smsBody.append(Uri.parse(message));
                    SmsManager.getDefault().sendTextMessage(mobileNo, null, smsBody.toString(), null, null);
                    btnStopAlert.setVisibility(View.VISIBLE);
                }
                repeatSms();
            }
        };
        handler.postDelayed(runnable, 60000);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        /*LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));*/

        showCurrentLocation();
    }

    public void showCurrentLocation() {

        this.mMap.getUiSettings().setMyLocationButtonEnabled(true);
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        @SuppressLint("MissingPermission")
        Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider
                (criteria, false));

        lat = location.getLatitude();
        lng = location.getLongitude();

        if (location != null) {
            mMap.clear();
            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(location.getLatitude(), location.getLongitude()))
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_location_pin)));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng
                    (location.getLatitude(), location.getLongitude()), 13));
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(location.getLatitude(), location.getLongitude()))      // Sets the center of the map to location user
                    .zoom(17)                                                                // Sets the zoom
                    //.bearing(90)                                                          // Sets the orientation of the camera to east
                    //.tilt(40)                                                            // Sets the tilt of the camera to 30 degrees
                    .build();                                                             // Creates a CameraPosition from the builder
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
    }


}