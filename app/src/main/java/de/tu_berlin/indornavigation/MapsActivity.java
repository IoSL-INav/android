package de.tu_berlin.indornavigation;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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

        // Add a marker in Sydney and move the camera
        LatLng berlin = new LatLng(52.512870, 13.326820);
        mMap.addMarker(new MarkerOptions().position(berlin).title("TU Berlin"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(berlin, 15));

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng latLng) {
                // TODO Auto-generated method stub
                System.out.println("Taped LatLng: " + latLng.latitude + ", " + latLng.longitude);
            }
        });

        LatLngBounds bounds = new LatLngBounds(
                new LatLng(52.509620, 13.325453),       // South west corner
                new LatLng(52.509875, 13.327041));      // North east corner

        GroundOverlayOptions overlayOptions = new GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromResource(R.drawable.mensa_eg))
                .positionFromBounds(bounds);

        GroundOverlay overlay = mMap.addGroundOverlay(overlayOptions);

    }

}
