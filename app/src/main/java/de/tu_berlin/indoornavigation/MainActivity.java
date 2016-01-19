package de.tu_berlin.indoornavigation;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.toString();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // access authentication token from Shared Preferences
        SharedPreferences sharedPreferences = getSharedPreferences(AuthUtils.PREFS_NAME, 0);
        AuthUtils.token = sharedPreferences.getString("authToken", null);

        // check if token is still valid
        String url = PropertiesSingleton.getInstance().getBackendServerUrl() + "/login/";

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String responseStr) {
                        Log.d(TAG, "Response is: " + responseStr);
                        try {
                            JSONObject response = new JSONObject(responseStr);
                            if (response.getString("status").equals("success")) {
                                Log.d(TAG, "Token is still valid. WIll redirect to menu view.");
                                queryGroups();
                                showMenuActivity();
                            } else {
                                Log.d(TAG, "Token expired. Will redirect to login view.");
                                showLoginActivity();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Log.e(TAG, "That didn't work!" + error.toString());
                Log.d(TAG, "Token expired. Will redirect to login view.");
                showLoginActivity();
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

    /**
     * Starts login (web view) activity
     */
    public void showLoginActivity() {

        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();

    }

    /**
     * Starts menu activity
     */
    public void showMenuActivity() {

        Intent intent = new Intent(this, MenuActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * Gets data about groups and saves it to IndoorNavigation class instance
     */
    private void queryGroups() {
        // get groups
        String url = PropertiesSingleton.getInstance().getBackendServerUrl() + "/users/me/groups";

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String responseStr) {
                        Log.d(TAG, "Response is: " + responseStr);

                        try {
                            JSONArray responseArr = new JSONArray(responseStr);

                            ArrayList<Group> groups = new ArrayList<>();
                            String id = "";
                            String name = "";
                            boolean autoPing = false;
                            for (int i = 0; i < responseArr.length(); i++) {
                                JSONObject obj = responseArr.getJSONObject(i);
                                id = obj.getString("_id");
                                name = obj.getString("name");
                                autoPing = false;
                                groups.add(new Group(id, name, autoPing));
                            }
                            IndoorNavigation.setGroups(groups);
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
}
