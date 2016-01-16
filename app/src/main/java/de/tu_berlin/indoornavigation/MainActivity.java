package de.tu_berlin.indoornavigation;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.estimote.sdk.SystemRequirementsChecker;

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
        ;

        VolleyQueueSingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);

        /*// show or hide texts and buttons, based on token availability
        TextView loginNameText1 = (TextView) findViewById(R.id.loggedin_text1);
        TextView loginNameText2 = (TextView) findViewById(R.id.loggedin_text2);
        Button loginButton = (Button) findViewById(R.id.login_button);
        Button logoutButton = (Button) findViewById(R.id.logout_button);
        if (AuthUtils.token != null) {
            loginButton.setVisibility(View.INVISIBLE);
            logoutButton.setVisibility(View.VISIBLE);
            loginNameText1.setVisibility(View.VISIBLE);
            loginNameText2.setVisibility(View.VISIBLE);
            loginNameText2.setText(AuthUtils.token);
        } else {
            loginButton.setVisibility(View.VISIBLE);
            logoutButton.setVisibility(View.INVISIBLE);
            loginNameText1.setVisibility(View.INVISIBLE);
            loginNameText2.setVisibility(View.INVISIBLE);
        }*/

    }

    @Override
    protected void onResume() {
        super.onResume();

        SystemRequirementsChecker.checkWithDefaultDialogs(this); //TODO: this should be moved to
        // point where we actually need bluetooth
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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
     * Starts maps activity
     */
    public void showMapsActivity() {

        Intent intent = new Intent(this, MapsActivity.class);
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
     * Sends /login request. Deletes token from Shared Preferences and AuthUtils. Deletes cookies.
     * Restarts activity.
     */
    public void logout(View view) {

        // send /login request
        String url = "http://piazza.snet.tu-berlin.de/logout";

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
        startActivity(getIntent());
    }

    /**
     * Exit application.
     */
    public void exit(View view) {

        finish();
        System.exit(0);
    }

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
