package de.tu_berlin.indoornavigation;

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Created by Jan on 19. 01. 2016.
 */
public class LocationSharingSingleton {

    private static final String TAG = LocationSharingSingleton.class.toString();
    private static LocationSharingSingleton mInstance;

    private LinkedList<Beacon> beaconsInHotspots;
    private LinkedList<Beacon> detectedBeacons;
    private LinkedList<Beacon> detectedNearables;
    private String MSIBuildingName;
    private String MSIFloor;

    public LocationSharingSingleton() {

        // initialize lists
        this.beaconsInHotspots = new LinkedList<>();
        this.detectedBeacons = new LinkedList<>();
        this.detectedNearables = new LinkedList<>();

        // query info about beacons in hotspots
        refreshBeaconsInHotspots();

    }

    public static synchronized LocationSharingSingleton getInstance() {
        if (mInstance == null) {
            mInstance = new LocationSharingSingleton();
        }
        return mInstance;
    }

    public void refreshBeaconsInHotspots() {

        this.beaconsInHotspots.clear();

        // get hotspots
        String url = PropertiesSingleton.getInstance().getBackendServerUrl() + "/hotspots";

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String responseStr) {
                        Log.d(TAG, "Response is: " + responseStr);

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

        VolleyQueueSingleton.getInstance(IndoorNavigation.getContext()).addToRequestQueue(stringRequest);

    }

    public void addDetectedBeacon(Beacon beacon) {

        int index = this.beaconsInHotspots.indexOf(beacon);

        if (index != -1) {
            beacon.setName(this.beaconsInHotspots.get(index).getName());
            this.detectedBeacons.add(beacon);
        }
    }

    public void addDetectedNearable(Beacon beacon) {

        int index = this.beaconsInHotspots.indexOf(beacon);

        if (index != -1) {
            beacon.setName(this.beaconsInHotspots.get(index).getName());
            this.detectedNearables.add(beacon);
        }
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

    public LinkedList<Beacon> getDetectedNearablesAndBeacons() {
        LinkedList<Beacon> allBeaconsAndNearables = new LinkedList<>(detectedBeacons);
        allBeaconsAndNearables.addAll(detectedNearables);
        return allBeaconsAndNearables;
    }

    /**
     * Find closest nearable or beacon based on signal strength
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

    public void shareBeaconLocation() {

        Beacon closestBeaconOrNearable = getClosestNearableOrBeacon();

        if (closestBeaconOrNearable != null) {


            String url = PropertiesSingleton.getInstance().getBackendServerUrl() +
                    "/users/me/location/";

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("userMajor", closestBeaconOrNearable.getMajor());
                jsonObject.put("userMinor", closestBeaconOrNearable.getMinor());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // Request a string response from the provided URL.
            JsonObjectRequest putRequest = new JsonObjectRequest(Request.Method.PUT, url, jsonObject,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.d(TAG, "Beacon based location shared. Response is: " + response);
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(TAG, "Beacon based location sharing: That didn't work!" + error.toString());
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
    }

    public void shareMSILocation() {

        String msiBuildingName = getMSIBuildingName();
        String msiFloor = getMSIFloor();

        if (msiBuildingName != null && msiFloor != null) {

            String url = PropertiesSingleton.getInstance().getBackendServerUrl() +
                    "/users/me/location/";

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("userBuilding", msiBuildingName);
                jsonObject.put("userFloor", msiFloor);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // Request a string response from the provided URL.
            JsonObjectRequest putRequest = new JsonObjectRequest(Request.Method.PUT, url, jsonObject,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.d(TAG, "MSI based location shared. Response is: " + response);
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(TAG, "MSI based location sharing: That didn't work!" + error.toString());
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
    }

}
