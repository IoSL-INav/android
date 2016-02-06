package de.tu_berlin.indoornavigation;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import de.tu_berlin.indoornavigation.entities.CompanionRequest;
import de.tu_berlin.indoornavigation.singletons.PropertiesSingleton;
import de.tu_berlin.indoornavigation.singletons.UsersGroupsDataSingleton;
import de.tu_berlin.indoornavigation.singletons.VolleyQueueSingleton;
import de.tu_berlin.indoornavigation.utils.AuthUtils;

public class CompanionRequestsActivity extends AppCompatActivity implements
        CompanionRequestFragment.OnListFragmentInteractionListener {

    private static final String TAG = CompanionRequestsActivity.class.toString();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_companion_requests);
    }

    /**
     * Open dialog for accepting companion requests.
     *
     * @param companionRequest
     */
    public void onListFragmentInteraction(final CompanionRequest companionRequest) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Accept companion request?");

        // Set up the buttons
        builder.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String url = PropertiesSingleton.getInstance().getBackendServerUrl() +
                        "/companionrequests/" + companionRequest.getId();

                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("accept", true);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // Request a string response from the provided URL.
                JsonObjectRequest putRequest = new JsonObjectRequest(Request.Method.PUT, url,
                        jsonObject,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Log.d(TAG, "Companion request accepted. Response is: " + response);
                                UsersGroupsDataSingleton.getInstance().refreshCompanionRequestsInfo();

                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Companion request accepted. That didn't work!" + error
                                .toString());
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
        builder.setNegativeButton("Reject", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String url = PropertiesSingleton.getInstance().getBackendServerUrl() +
                        "/companionrequests/" + companionRequest.getId();

                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("deny", true);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // Request a string response from the provided URL.
                JsonObjectRequest putRequest = new JsonObjectRequest(Request.Method.PUT, url,
                        jsonObject,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Log.d(TAG, "Companion request canceled. Response is: " + response);
                                UsersGroupsDataSingleton.getInstance().refreshCompanionRequestsInfo();
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Companion request canceled. That didn't work!" + error
                                .toString());
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

                dialog.cancel();
            }
        });

        builder.show();

    }
}
