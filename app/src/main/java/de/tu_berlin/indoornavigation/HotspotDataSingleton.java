package de.tu_berlin.indoornavigation;

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
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
public class HotspotDataSingleton {

    private static final String TAG = HotspotDataSingleton.class.toString();
    private static HotspotDataSingleton mInstance;

    private LinkedList<Beacon> beacons;

    public HotspotDataSingleton() {

        this.beacons = new LinkedList<>();

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

                                    beacons.add(new Beacon(beacon.getString("companyUUID"),
                                            beacon.getInt("major"), beacon.getInt("minor")));

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

    public static synchronized HotspotDataSingleton getInstance() {
        if (mInstance == null) {
            mInstance = new HotspotDataSingleton();
        }
        return mInstance;
    }

    public LinkedList<Beacon> getBeacons() {
        return beacons;
    }
}
