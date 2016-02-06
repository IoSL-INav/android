package de.tu_berlin.indoornavigation.singletons;

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import de.tu_berlin.indoornavigation.IndoorNavigation;
import de.tu_berlin.indoornavigation.entities.Beacon;
import de.tu_berlin.indoornavigation.utils.AuthUtils;

/**
 * Created by Jan on 19. 01. 2016.
 * <p/>
 * Singleton is used for storing pinpointed location, beacon detected location and MSI API detected
 * location. Singleton also stores data about all beacons that are part of hotspots defined in
 * backend.
 */
public class LocationSharingSingleton {

    private static final String TAG = LocationSharingSingleton.class.toString();
    private static LocationSharingSingleton mInstance;

    private LinkedList<Beacon> beaconsInHotspots;
    private LinkedList<Beacon> detectedBeacons;
    private LinkedList<Beacon> detectedNearables;
    private String MSIBuildingName;
    private String MSIFloor;
    private LatLng pinpointedCoordinates;
    private String pinpointedBuildingName;
    private String pinpointedFloor;

    public LocationSharingSingleton() {

        // initialize lists
        this.beaconsInHotspots = new LinkedList<>();
        this.detectedBeacons = new LinkedList<>();
        this.detectedNearables = new LinkedList<>();

        // query info about beacons in hotspots
        refreshBeaconsInHotspots();

    }

    /**
     * Get singleton instance.
     *
     * @return
     */
    public static synchronized LocationSharingSingleton getInstance() {
        if (mInstance == null) {
            mInstance = new LocationSharingSingleton();
        }
        return mInstance;
    }

    /**
     * Query backend for information about beacons in hotspots.
     */
    public void refreshBeaconsInHotspots() {

        this.beaconsInHotspots.clear();

        // get hotspots
        String url = PropertiesSingleton.getInstance().getBackendServerUrl() + "/hotspots";

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String responseStr) {
                        Log.d(TAG, "Querying hotspots. Response is: " + responseStr);

                        try {
                            JSONArray hotspots = new JSONArray(responseStr);

                            for (int i = 0; i < hotspots.length(); i++) {

                                JSONObject hotspot = hotspots.getJSONObject(i);
                                JSONArray beaconsList = hotspot.getJSONArray("beacons");

                                for (int j = 0; j < beaconsList.length(); j++) {

                                    JSONObject beacon = beaconsList.getJSONObject(j);

                                    beaconsInHotspots.add(new Beacon(beacon.getString("name"),
                                            beacon.getString("companyUUID"), beacon.getInt("major"),
                                            beacon.getInt("minor")));

                                }

                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Querying hotspots. That didn't work!" + error.toString());
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> params = new HashMap<String, String>();
                params.put("Cookie", "connect.sid=" + AuthUtils.token);

                return params;
            }
        };

        VolleyQueueSingleton.getInstance(IndoorNavigation.getContext()).addToRequestQueue(stringRequest);

    }

    /**
     * If estimote Beacon is part of hotspot, add it in list of detected Beacons.
     *
     * @param beacon
     */
    public void addDetectedBeacon(Beacon beacon) {

        int index = this.beaconsInHotspots.indexOf(beacon);

        if (index != -1) {
            beacon.setName(this.beaconsInHotspots.get(index).getName());
            this.detectedBeacons.add(beacon);
        }
    }

    /**
     * If estimote Nearable is part of hotspot, add it in list of detected Beacons.
     *
     * @param beacon
     */
    public void addDetectedNearable(Beacon beacon) {

        int index = this.beaconsInHotspots.indexOf(beacon);

        if (index != -1) {
            beacon.setName(this.beaconsInHotspots.get(index).getName());
            this.detectedNearables.add(beacon);
        }
    }

    /**
     * Returns list of all detected estimote Beacons and estimote Nearables.
     *
     * @return
     */
    public LinkedList<Beacon> getDetectedNearablesAndBeacons() {
        LinkedList<Beacon> allBeaconsAndNearables = new LinkedList<>(detectedBeacons);
        allBeaconsAndNearables.addAll(detectedNearables);
        return allBeaconsAndNearables;
    }

    /**
     * Find closest nearable or beacon based on signal strength.
     *
     * @param
     */
    public Beacon getClosestNearableOrBeacon() {

        Beacon closestNearableOrBeacon = null;

        if (!this.getDetectedNearablesAndBeacons().isEmpty()) {
            closestNearableOrBeacon = this.getDetectedNearablesAndBeacons().getFirst();
        }
        for (Beacon beaconOrNearable : this.getDetectedNearablesAndBeacons()) {
            if (beaconOrNearable.getRssi() > closestNearableOrBeacon.getRssi()) {
                closestNearableOrBeacon = beaconOrNearable;
            }
        }

        return closestNearableOrBeacon;
    }

    /**
     * Updates location based on most accurate location info available.
     */
    public void updateLocation() {

        Beacon closestBeaconOrNearable = getClosestNearableOrBeacon();

        Log.d(TAG, "Updating location");
        Log.d(TAG, "Pinpointed coordinates: " + pinpointedCoordinates + " building: " +
                pinpointedBuildingName + " floor: " + pinpointedFloor);
        if (closestBeaconOrNearable != null) {
            Log.d(TAG, "Closest beacon: " + closestBeaconOrNearable.getName());
        } else {
            Log.d(TAG, "Closest beacon: null");
        }
        Log.d(TAG, "MSI building: " + MSIBuildingName + " floor: " + MSIFloor);

        // json object with location
        JSONObject jsonObject = new JSONObject();

        // add the most accurate location available to json object
        if (pinpointedCoordinates != null && pinpointedBuildingName != null && pinpointedFloor !=
                null) { // pinpointed location available
            try {
                jsonObject.put("userLon", pinpointedCoordinates.longitude);
                jsonObject.put("userLat", pinpointedCoordinates.latitude);
                jsonObject.put("userBuilding", pinpointedBuildingName);
                jsonObject.put("userFloor", pinpointedFloor);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (closestBeaconOrNearable != null) { // detected beacon information available
            try {
                jsonObject.put("userMajor", closestBeaconOrNearable.getMajor());
                jsonObject.put("userMinor", closestBeaconOrNearable.getMinor());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (getMSIBuildingName() != null && getMSIFloor() != null) { // MSI location available
            try {
                jsonObject.put("userBuilding", getMSIBuildingName());
                jsonObject.put("userFloor", getMSIFloor());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        // define url
        String url = PropertiesSingleton.getInstance().getBackendServerUrl() +
                "/users/me/location/";

        // Request a string response from the provided URL.
        JsonObjectRequest putRequest = new JsonObjectRequest(Request.Method.PUT, url, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "Location shared. Response is: " + response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Location sharing: That didn't work!" + error.toString());
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> params = new HashMap<String, String>();
                params.put("Cookie", "connect.sid=" + AuthUtils.token);

                return params;
            }
        };

        VolleyQueueSingleton.getInstance(IndoorNavigation.getContext()).addToRequestQueue(putRequest);

    }

    // Getters and setters
    public String getPinpointedFloor() {
        return pinpointedFloor;
    }

    public void setPinpointedFloor(String pinpointedFloor) {
        this.pinpointedFloor = pinpointedFloor;
    }

    public String getPinpointedBuildingName() {
        return pinpointedBuildingName;
    }

    public void setPinpointedBuildingName(String pinpointedBuildingName) {
        this.pinpointedBuildingName = pinpointedBuildingName;
    }

    public LinkedList<Beacon> getBeaconsInHotspots() {
        return beaconsInHotspots;
    }

    public LinkedList<Beacon> getDetectedBeacons() {
        return detectedBeacons;
    }

    public LinkedList<Beacon> getDetectedNearables() {
        return detectedNearables;
    }

    public String getMSIBuildingName() {
        return MSIBuildingName;
    }

    public void setMSIBuildingName(String MSIBuildingName) {
        this.MSIBuildingName = MSIBuildingName;
    }

    public String getMSIFloor() {
        return MSIFloor;
    }

    public void setMSIFloor(String MSIFloor) {
        this.MSIFloor = MSIFloor;
    }

    public LatLng getPinpointedCoordinates() {
        return pinpointedCoordinates;
    }

    public void setPinpointedCoordinates(LatLng pinpointedCoordinates) {
        this.pinpointedCoordinates = pinpointedCoordinates;
    }
}