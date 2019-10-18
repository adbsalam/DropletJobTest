package com.salam.dropletjobtest.registerActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.salam.dropletjobtest.R;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Location_Activity gets the Map ready
 * Finds Location of device
 * Loads a marker on the map
 * Adds the location to EditText
 * Passes Address details to Register Activity
 */
public class Location_Activity extends FragmentActivity implements OnMapReadyCallback {

    //Initialisations relating to Map fragment
    private GoogleMap mMap;
    private final float DEFAULT_ZOOM = 18;
    private View mapView;

    //Initialisations relating to Location
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Location mLastknownlocation;
    private LocationCallback locationCallback;

    // Strings to hold location data
    String nLatitude = "";
    String nLongitude = "";
    String uAddress;
    EditText et_uAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
        mapView = mapFragment.getView();

        //UI initialisation
        et_uAddress = findViewById(R.id.et_CurrentLocation);
        Button btn_Next = findViewById(R.id.btn_Next);

        // Passing STRING EXTRA for address to register activity.
        btn_Next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String adrtxt = et_uAddress.getText().toString();
                Intent start_Register = new Intent(Location_Activity.this, Register.class);
                start_Register.putExtra("ADR", adrtxt);
                startActivity(start_Register);
                finish();
            }
        });
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(Location_Activity.this);
    }

    /**
     * @param googleMap google Map fragment
     * Map ready with location button on the map
     */
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        //Engable locations on Map to show current location
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.setPadding(0,0, 40, 500);
        //Location button to center screen the current location, properties of location button
        if(mapView != null && mapView.findViewById(Integer.parseInt("1")) != null){
            View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);

        }
        // method to get location
        getDeviceLocation();
    }

    /**
     * @param requestCode request code as sent by Map ready results
     * @param resultCode result code if Map is ready
     * @param data intent data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //in case location request fails then run getDeviceLocation() again to try catching location again
            if(requestCode == RESULT_OK){
                getDeviceLocation();
            }
    }

    /**
     * Device Location Cordinates
     * move camera to current Location
     * Pass cordinates to getAddress() method
     */
    @SuppressLint("MissingPermission")
    private void getDeviceLocation(){
        //get users last known location from mFusedLocationProvider
        mFusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful()){
                    mLastknownlocation = task.getResult();
                    if (mLastknownlocation != null){
                        //move camera to lask known location if found
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastknownlocation.getLatitude(), mLastknownlocation.getLongitude()), DEFAULT_ZOOM));
                        LatLng cordinates = new LatLng(mLastknownlocation.getLatitude(), mLastknownlocation.getLongitude());
                        getAddress(cordinates);
                       // rippleBackground.startRippleAnimation();

                    }else {
                        //if no last known location is found than try to catch the current location
                        LocationRequest locationRequest = LocationRequest.create();
                        locationRequest.setInterval(1000);
                        locationRequest.setFastestInterval(5000);
                        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                        locationCallback = new LocationCallback(){
                            @Override
                            public void onLocationResult(LocationResult locationResult) {
                                super.onLocationResult(locationResult);
                                if (locationResult ==null){

                                    return;
                                }
                                mLastknownlocation = locationResult.getLastLocation();
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastknownlocation.getLongitude(), mLastknownlocation.getLatitude()), DEFAULT_ZOOM));
                                mFusedLocationProviderClient.removeLocationUpdates(locationCallback);
                                LatLng cordinates = new LatLng(mLastknownlocation.getLatitude(), mLastknownlocation.getLongitude());
                                getAddress(cordinates);
                            }
                        };
                        mFusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback, null);
                    }
                }else {

                    Toast.makeText(Location_Activity.this, getString(R.string.LOC_ERROR), Toast.LENGTH_LONG).show();
                }
            }
        });
    }


    /**
     * @param cordinates  of current location, Latitude and Longitude
     *convert Cordinates into Location Details
     */
    private void getAddress(LatLng cordinates) {
        String newAddress;
        Geocoder geocoder = new Geocoder(Location_Activity.this, Locale.getDefault());

        try{
            List<Address> addresses = geocoder.getFromLocation(cordinates.latitude, cordinates.longitude, 1);
            nLongitude = String.valueOf(cordinates.longitude);
            nLatitude = String.valueOf(cordinates.latitude);
            newAddress = addresses.get(0).getAddressLine(0);
            et_uAddress.setText(newAddress);
            uAddress = newAddress;

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}
