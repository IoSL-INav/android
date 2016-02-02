package de.tu_berlin.indoornavigation;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.estimote.sdk.SystemRequirementsChecker;

import org.json.JSONException;
import org.json.JSONObject;

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

        // initialize hotspots
        LocationSharingSingleton.getInstance().refreshBeaconsInHotspots();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // request location and bluetooth access
        SystemRequirementsChecker.checkWithDefaultDialogs(this);

        // refresh companion requests and groups
        UsersGroupsDataSingleton.getInstance().refreshCompanionRequestsInfo();
        UsersGroupsDataSingleton.getInstance().refreshGroupsInfo();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_menu, menu);
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
     * Starts groups activity
     */
    public void showGroupsLibrary(View view) {

        Intent intent = new Intent(this, GroupsActivity.class);
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

    public void showAddFriendDialog(MenuItem item) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Title");

        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String email = input.getText().toString();

                Log.d(TAG, email);

                String url = PropertiesSingleton.getInstance().getBackendServerUrl() +
                        "/companionrequests/";

                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("userEmail", email); // TODO: remove
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // Request a string response from the provided URL.
                JsonObjectRequest putRequest = new JsonObjectRequest(Request.Method.POST, url,
                        jsonObject,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Log.d(TAG, "Friend request send. Response is: " + response);
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Friend request send: That didn't work!" + error.toString());
                    }
                }) {
                    @Override
                    public Map<String, String> getHeaders() {
                        HashMap<String, String> params = new HashMap<String, String>();
                        params.put("Cookie", "connect.sid=" + AuthUtils.token);

                        return params;
                    }
                };

                VolleyQueueSingleton.getInstance(getApplicationContext()).addToRequestQueue(putRequest);

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();

    }

    public void showCompanionRequestsActivity(MenuItem menuItem) {
        Intent intent = new Intent(this, CompanionRequestsActivity.class);
        startActivity(intent);
    }

}
