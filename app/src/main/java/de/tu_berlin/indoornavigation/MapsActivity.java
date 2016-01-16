package de.tu_berlin.indoornavigation;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = MapsActivity.class.toString();

    private TextView currentFloorText;
    private GoogleMap mMap;
    private Marker marker;
    private ArrayList<GroundOverlay> overlays = new ArrayList<>();
    private int currentFloor = 0;
    private int numberOfFloors = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        currentFloorText = (TextView) findViewById(R.id.current_floor_text);
        currentFloorText.setText("EG");
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

        LatLng mensaCenter = new LatLng(52.50969128322999, 13.326051905751228);

        // add marker in center of mensa and move camera there
        marker = mMap.addMarker(new MarkerOptions().position(mensaCenter).title("Mensa"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mensaCenter, 24));

        // on map click change position of marker
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng latLng) {
                marker.setPosition(latLng);
            }
        });

        // add map overlay, set anchor in lower righter corner, set position of anchor, set width
        // of overlay in meters, rotate overlay clockwise (in degrees)
        GroundOverlayOptions overlayEGOptions = new GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromResource(R.drawable.mensa_eg))
                .anchor(1, 1).position(new LatLng(52.509490, 13.326278), 51.88f).bearing(26);

        // add overlay to map
        overlays.add(mMap.addGroundOverlay(overlayEGOptions));

        // add second overlay, first floor
        GroundOverlayOptions overlay1GOptions = new GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromResource(R.drawable.mensa_1g))
                .anchor(1, 1).position(new LatLng(52.509490, 13.326278), 51.88f).bearing(26);
        overlays.add(mMap.addGroundOverlay(overlay1GOptions));
        overlays.get(overlays.size() - 1).setVisible(false);

    }

    /**
     * On button clicked, shares position of marker
     *
     * @param view
     */
    public void sharePosition(View view) {

        Log.d(TAG, "Floor: " + currentFloor + " Marker position is: " + marker.getPosition());

        String url = "http://piazza.snet.tu-berlin.de/users/me/location/";

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("userLon", marker.getPosition().longitude);
            jsonObject.put("userLat", marker.getPosition().latitude);
            jsonObject.put("userBuilding", "mensa"); //TODO: remove
            jsonObject.put("userFloor", currentFloor); //TODO: floor format
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Request a string response from the provided URL.
        JsonObjectRequest putRequest = new JsonObjectRequest(Request.Method.PUT, url, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "Response is: " + response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "That didn't work!" + error.toString());
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> params = new HashMap<String, String>();
                params.put("Cookie", "connect.sid=" + AuthUtils.token);

                return params;
            }
        };
        ;

        VolleyQueueSingleton.getInstance(getApplicationContext()).addToRequestQueue(putRequest);

    }

    public void changeFloorUp(View view) {
        Log.d(TAG, "Up.");

        if (currentFloor != numberOfFloors - 1) {
            currentFloor += 1;
            selectFloor(currentFloor);
            setCurrentFloorText(currentFloor);
        }
    }

    public void changeFloorDown(View view) {
        Log.d(TAG, "Down.");

        if (currentFloor > 0) {
            currentFloor -= 1;
            selectFloor(currentFloor);
            setCurrentFloorText(currentFloor);
        }
    }

    private void selectFloor(int floor) {
        for (int i = 0; i < overlays.size(); i++) {
            if (i == floor) {
                overlays.get(i).setVisible(true);
            } else {
                overlays.get(i).setVisible(false);
            }
        }
    }

    private void setCurrentFloorText(int floor) {
        if (floor == 0) {
            currentFloorText.setText("EG");
        } else {
            currentFloorText.setText(floor + "G");
        }
    }

}
