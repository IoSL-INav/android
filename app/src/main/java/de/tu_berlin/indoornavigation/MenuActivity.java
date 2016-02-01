package de.tu_berlin.indoornavigation;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class MenuActivity extends AppCompatActivity {

    private static final String TAG = MenuActivity.class.toString();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
    }

    /**
     * Starts maps activity zoomed in mensa
     */
    public void showMapsActivityMensa(View view) {

        Intent intent = new Intent(this, MapsActivity.class);
        intent.putExtra("id", "mensa");
        startActivity(intent);
    }

    /**
     * Starts maps activity zoomed in library
     */
    public void showMapsActivityLibrary(View view) {

        Intent intent = new Intent(this, MapsActivity.class);
        intent.putExtra("id", "library");
        startActivity(intent);
    }

    /**
     * Sends /login request. Deletes token from Shared Preferences and AuthUtils. Deletes cookies.
     * Exits application.
     */
    public void logout(View view) {

        // send /login request
        String url = PropertiesSingleton.getInstance().getBackendServerUrl() + "/logout";

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
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


        VolleyQueueSingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);

        // delete token from shared preferences
        SharedPreferences sharedPreferences = getSharedPreferences(AuthUtils.PREFS_NAME, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("authToken");
        editor.commit();

        // remove token from AuthUtils
        AuthUtils.token = null;

        finish();
        System.exit(0);
    }

    /**
     * Exit application.
     */
    public void exit(View view) {

        finish();
        System.exit(0);
    }

}
