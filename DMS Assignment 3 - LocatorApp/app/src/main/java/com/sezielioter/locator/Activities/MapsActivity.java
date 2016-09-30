package com.sezielioter.locator.Activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.sezielioter.locator.R;

public class MapsActivity extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleMap mMap;
    private final static String TAG = "MapsActivity";//LogCat message TAG
    private final static int BOUNDS_PADDING=25, INITIAL_ZOOM=17;
    private LatLng myLocation, destination, tagLocation;
    private LatLngBounds.Builder boundsBuilder;
    private Marker destMarker, sourceMarker;
    private boolean hasTagSourceMarker;

    /**
     * Provides the entry point to Google Play services.
     */
    protected GoogleApiClient mGoogleApiClient;

    /**
     * Location Services elements
     */
    protected Location mLastLocation;
    protected Location mCurrentLocation;
    protected LocationRequest mLocationRequest;
    protected boolean mRequestingLocationUpdates = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Bundle bundle = getIntent().getExtras();
        destination = bundle.getParcelable(InitialOptionsActivity.DESTINATION);
        Log.d(TAG, "destination = " + destination);
        //myLocation = bundle.getParcelable(InitialOptionsActivity.TAG_LOCATION);
        tagLocation = bundle.getParcelable(InitialOptionsActivity.TAG_LOCATION);
        Log.d(TAG, "tagLocation = " + tagLocation);

        if(tagLocation != null) hasTagSourceMarker = true;
        else hasTagSourceMarker = false;
        Log.d(TAG, "hasTagSource Marker = " + hasTagSourceMarker);

        /**
         * Start Location Services requests
         */
        buildGoogleApiClient();                 //Builds the GoogleAPIClient below
        createLocationRequest();                //Sets LocationRequest intervals and others
        mRequestingLocationUpdates = true;      //Location requests will begin with this being true
    }

    /*********************************************************************
     ****************   Google Location Service   ************************
     *********************************************************************/

    /**
     * Builds a GoogleApiClient. Uses the addApi() method to request the LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    /**
     * Sets LocationRequest variables
     */
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(10000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * onStart called when activity becomes visible to the user
     */
    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.registerConnectionCallbacks(this);
        mGoogleApiClient.connect();
    }

    /**
     * onResume called when activity will start interacting with the user
     */
    @Override
    public void onResume() {
        super.onResume();
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;
            }
        });
        mGoogleApiClient.registerConnectionCallbacks(this);
    }

    /**
     * onPause called when user is leaving the fragment
     */@Override
    public void onPause() {
        super.onPause();
        stopLocationUpdates();
        mGoogleApiClient.unregisterConnectionCallbacks(this);
        Log.d(TAG, "Updates stopped");
    }

    /**
     * Fragment will be stopped
     */
    @Override
    public void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        mGoogleApiClient.unregisterConnectionCallbacks(this);
    }

    /**
     * Called after onDestroyView() to do final clean up of fragment state
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        mGoogleApiClient.unregisterConnectionCallbacks(this);
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mCurrentLocation == null) {
            Toast.makeText(this, R.string.no_location_detected, Toast.LENGTH_LONG).show();
        }

        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }

        mMap.setMyLocationEnabled(true);
        destMarker = mMap.addMarker(new MarkerOptions()
                                            .position(destination)
                                            .title("Destination")
                                            .snippet("Your target location")
                                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

        if(hasTagSourceMarker) sourceMarker = mMap.addMarker(new MarkerOptions()
                                            .position(tagLocation)
                                            .title("Start")
                                            .snippet("The location of the tag you scanned")
                                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
    }

    /**
     * Get the location updates using parameters set in createLocationRequest()
     */
    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = mCurrentLocation;
        mCurrentLocation = location;
        updateUI();
    }

    private void updateUI() {
        myLocation = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        boundsBuilder = new LatLngBounds.Builder();
            boundsBuilder.include(destMarker.getPosition());
            boundsBuilder.include(myLocation);
            if(hasTagSourceMarker) boundsBuilder.include(sourceMarker.getPosition());
        LatLngBounds mapBounds = boundsBuilder.build();

        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(mapBounds, BOUNDS_PADDING));

    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }
}

