package de.tu_berlin.indoornavigation;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = MapsActivity.class.toString();
    // MSI based flood names for different buildings
    private String[] mensaFloorNames = {"Mensa 1. OG", "Mensa 2.  OG"};
    private String[] libraryFloorNames = {"Erdgeschoss", "1. Obergeschoss",
            "2. Obergeschoss", "3. Obergeschoss", "4. Obergeschoss"};
    // hold data about open building
    private String buildingName = null;
    private LatLng buildingCenter = null;
    private LinkedList<String> buildingFloorNames;
    private int numberOfFloors;
    // map and marker data
    private TextView currentFloorText;
    private GoogleMap mMap;
    private Marker marker;
    private ArrayList<GroundOverlay> overlays = new ArrayList<>();
    private int currentFloor = 0;
    private LinkedList<MarkerOptions>[] friendsMarkerOptions;
    private LinkedList<CircleOptions>[] friendsMarkersCircleOptions;
    private LinkedList<Marker> friendsMarkers = new LinkedList<>();
    private LinkedList<Circle> friendsMarkersCircles = new LinkedList<>();

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

        if (getIntent().getExtras().getString("id").equals("mensa")) {
            buildingCenter = new LatLng(52.50969128322999, 13.326051905751228);
            buildingName = "Mensa";
            buildingFloorNames = new LinkedList<>(Arrays.asList(mensaFloorNames));
            numberOfFloors = 2;
        } else if (getIntent().getExtras().getString("id").equals("library")) {
            buildingCenter = new LatLng(52.5104373136039, 13.330666981637478);
            buildingName = "BIB";
            buildingFloorNames = new LinkedList<>(Arrays.asList(libraryFloorNames));
            numberOfFloors = 5;
        }

        // initialize linked lists
        friendsMarkerOptions = new LinkedList[numberOfFloors];
        for (int i = 0; i < friendsMarkerOptions.length; i++) {
            friendsMarkerOptions[i] = new LinkedList<>();
        }
        friendsMarkersCircleOptions = new LinkedList[numberOfFloors];
        for (int i = 0; i < friendsMarkersCircleOptions.length; i++) {
            friendsMarkersCircleOptions[i] = new LinkedList<>();
        }

        // show friends
        showFriends(null);

        // add marker in buildingCenter of mensa and move camera there
        marker = mMap.addMarker(new MarkerOptions().position(buildingCenter).title(buildingName));
        marker.setVisible(false);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(buildingCenter, 22));

        // on map click change position of marker
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng latLng) {
                marker.setPosition(latLng);

                CheckBox pinpointLocationCheckbox = (CheckBox) findViewById(R.id
                        .pinpoint_location_checkbox);

                if (pinpointLocationCheckbox.isChecked()) {
                    LocationSharingSingleton.getInstance().setPinpointedCoordinates(marker.getPosition());
                    LocationSharingSingleton.getInstance().setPinpointedBuildingName(buildingName);
                    LocationSharingSingleton.getInstance().setPinpointedFloor(buildingFloorNames
                            .get(currentFloor));
                }

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
     * On button clicked, shares position of marker to the backend
     *
     * @param view
     */
    public void pinpointLocation(View view) {

        Log.d(TAG, "Building: " + buildingName + " Floor: " + buildingFloorNames.get(currentFloor) +
                " Marker position is: " + marker.getPosition());

        CheckBox pinpointLocationCheckbox = (CheckBox) findViewById(R.id
                .pinpoint_location_checkbox);

        if (pinpointLocationCheckbox.isChecked()) {
            LocationSharingSingleton.getInstance().setPinpointedCoordinates(marker.getPosition());
            LocationSharingSingleton.getInstance().setPinpointedBuildingName(buildingName);
            LocationSharingSingleton.getInstance().setPinpointedFloor(buildingFloorNames
                    .get(currentFloor));
            marker.setVisible(true);
        } else {
            LocationSharingSingleton.getInstance().setPinpointedCoordinates(null);
            LocationSharingSingleton.getInstance().setPinpointedBuildingName(null);
            LocationSharingSingleton.getInstance().setPinpointedFloor(null);
            marker.setVisible(false);
        }

    }

    /**
     * On button clicked updates locations of friends and shows them on map
     *
     * @param view
     */
    public void showFriends(View view) {

        // remove old markers and circles
        for (int i = 0; i < friendsMarkerOptions.length; i++) {
            friendsMarkerOptions[i].clear();
        }

        for (int i = 0; i < friendsMarkersCircleOptions.length; i++) {
            friendsMarkersCircleOptions[i].clear();
        }

        // backend url
        String url = PropertiesSingleton.getInstance().getBackendServerUrl() +
                "/hotspots/6ace7b9015209eb1c2e871c/active_friends/"; // TODO: change

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String responseStr) {
                        Log.d(TAG, "Looking for friends. Response is: " + responseStr);
                        try {
                            JSONObject response = new JSONObject(responseStr);
                            JSONArray friends = response.getJSONArray("friends");

                            // show each friend on the map
                            for (int i = 0; i < friends.length(); i++) {

                                double lng = 0;
                                double lat = 0;
                                String buildingName = null;
                                String buildingFloor = null;

                                JSONObject friend = friends.getJSONObject(i);
                                String name = friend.getString("name");
                                JSONObject location = friend.getJSONObject("location");
                                if (location.has("coordinates")) {
                                    JSONArray coordinates = location.getJSONArray("coordinates");
                                    lng = coordinates.getDouble(0);
                                    lat = coordinates.getDouble(1);
                                }
                                int accuracyIndicator = location.getInt("accuracyIndicator");

                                if (location.has("building")) {
                                    buildingName = location.getString("building");
                                }

                                if (location.has("floor")) {
                                    buildingFloor = location.getString("floor");
                                }

                                Log.d(TAG, "location info: lat, lng, accuracy. " + lat + " " + lng
                                        + " " + accuracyIndicator + " building: " + buildingName
                                        + " floor: " + buildingFloor);

                                // add marker options and circle options
                                LatLng position = new LatLng(lat, lng);
                                int strokeColor = 0xffff0000; //red outline
                                int shadeColor = 0x44ff0000; //opaque red fill

                                int floorIndex = buildingFloorNames.indexOf(buildingFloor);
                                if (floorIndex != -1) { //TODO: test
                                    friendsMarkerOptions[floorIndex].add
                                            (new
                                                    MarkerOptions().position
                                                    (position).icon(BitmapDescriptorFactory.defaultMarker
                                                    (BitmapDescriptorFactory.HUE_BLUE)).title(name));
                                    if (accuracyIndicator == 1) {
                                        friendsMarkersCircleOptions[floorIndex].add
                                                (new
                                                        CircleOptions()
                                                        .center(position).radius(5).fillColor(shadeColor).strokeColor(strokeColor)
                                                        .strokeWidth(8).zIndex(100));
                                    }
                                }

                                // draw markers on selected floor
                                drawFriendsLocations();
                            }


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
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

        VolleyQueueSingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);

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
        drawFriendsLocations();
    }

    private void setCurrentFloorText(int floor) {
        if (floor == 0) {
            currentFloorText.setText("EG");
        } else {
            currentFloorText.setText(floor + "G");
        }
    }

    private void drawFriendsLocations() {

        // delete old markers
        for (Marker marker : friendsMarkers) {
            marker.remove();
        }
        friendsMarkers.clear();
        for (Circle circle : friendsMarkersCircles) {
            circle.remove();
        }
        friendsMarkersCircles.clear();

        // draw new markers
        for (MarkerOptions markerOptions : friendsMarkerOptions[currentFloor]) {
            friendsMarkers.add(mMap.addMarker(markerOptions));
        }
        for (CircleOptions circleOptions : friendsMarkersCircleOptions[currentFloor]) {
            friendsMarkersCircles.add(mMap.addCircle(circleOptions));
        }
    }

}
