package com.sparkycuber.stms;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;

import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.location.LocationListener;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Random;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private GoogleMap mMap;


    //Play Service Location
    private static final int MY_PERMISSION_REQUEST_CODE = 7192; //My Birthday :D
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 300193;

    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    private static int UPDATE_INTERVAL = 5000; //5 SECS
    private static int FATEST_INTERVAL = 3000;
    private static int DISPLACEMENT = 10;

    DatabaseReference ref;
    GeoFire geoFire;

    Marker mCurrent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        ref= FirebaseDatabase.getInstance().getReference("MyLocation");
        geoFire =new  GeoFire (ref);

        setUpLocation();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode)
        {
            case MY_PERMISSION_REQUEST_CODE:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    if(checkPlayServices())
                    {
                        buildGoogleApiClient();
                        createLocationRequest();
                        displayLocation();
                    }
                }
                break;
        }
    }

    private  void setUpLocation() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            //Request runtime permission
            ActivityCompat.requestPermissions(this,new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION

            },MY_PERMISSION_REQUEST_CODE);
        }
        else
        {
            if(checkPlayServices())
            {
                buildGoogleApiClient();
                createLocationRequest();
                displayLocation();

            }
        }
    }

    private void displayLocation() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if(mLastLocation != null)
        {
            final double latitude = mLastLocation.getLatitude();
            final double longitude = mLastLocation.getLongitude();

            //update to firebase
            geoFire.setLocation("You", new GeoLocation(latitude, longitude),
                    new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {

                                    //add marker
                                    if(mCurrent !=null)
                                        mCurrent.remove(); //remove old marker
                                    mCurrent = mMap.addMarker(new MarkerOptions()
                                            .position(new LatLng(latitude,longitude))
                                            .title("You"));
                                    //move camera to this position
                                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude,longitude),12.0f));

                                }

                        });



            Log.d("EDMTSEV",String.format("Your location was changed : %f / %f",latitude,longitude));
        }
        else
            Log.d("EDMTSEV","Can't get your location");

    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);

    }

    private void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if(resultCode != ConnectionResult.SUCCESS)
        {
            if(GooglePlayServicesUtil.isUserRecoverableError(resultCode))
                GooglePlayServicesUtil.getErrorDialog(resultCode,this,PLAY_SERVICES_RESOLUTION_REQUEST).show();
            else
            {
                Toast.makeText(this,"This device is not supported",Toast.LENGTH_SHORT).show();
                finish();
            }
            return false;
        }
        return true;
    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        //create dangerous area
        LatLng dangerous_area = new LatLng(37.7533, -122.4056);
        mMap.addCircle(new CircleOptions()
                .center(dangerous_area)
                .radius(500)   //in meters  =>500 m
                .strokeColor(Color.BLUE)
                .fillColor(0x220000FF)
                .strokeWidth(5.0f)

        );

        //Add geoquery here
        //0.5f =0.5 km = 500 m
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(dangerous_area.latitude, dangerous_area.longitude), 0.5f);
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                sendNotfication("EDMTDEV", String.format("%s entered the dangerous area", key));
            }

            @Override
            public void onKeyExited(String key) {
                sendNotfication("EDMTDEV", String.format("%s is no longer in the dangerous area", key));
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                Log.d("MOVE", String.format("%s moved within the dangerous area [%f/%f]", key, location.latitude, location.longitude));
            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
                Log.e("ERROR", "" + error);

            }

        });
    }

    private void sendNotfication(String title, String content) {
        Notification.Builder builder = new Notification.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle(title)
                .setContentText(content);
        NotificationManager manager = (NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent =new Intent(this,MapsActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this,0,intent, PendingIntent.FLAG_IMMUTABLE);
        builder.setContentIntent(contentIntent);
        Notification notification =builder.build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notification.defaults |= Notification.DEFAULT_SOUND;

        manager.notify(new Random().nextInt(),notification);
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        startLocationUpdates();
    }

    private void startLocationUpdates() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest,this);

    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        displayLocation();
    }


}








