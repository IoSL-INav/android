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

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.tu_berlin.indoornavigation.entities.User;
import de.tu_berlin.indoornavigation.singletons.PropertiesSingleton;
import de.tu_berlin.indoornavigation.singletons.UsersGroupsDataSingleton;
import de.tu_berlin.indoornavigation.singletons.VolleyQueueSingleton;
import de.tu_berlin.indoornavigation.utils.AuthUtils;

public class UsersActivity extends AppCompatActivity implements UserFragment.OnListFragmentInteractionListener {

    private static final String TAG = UsersActivity.class.toString();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);
    }

    public void onListFragmentInteraction(final String groupId, final User user) {
        Log.d(TAG, groupId + " " + user.getUsername());

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete user?");

        // Set up the buttons
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String url = PropertiesSingleton.getInstance().getBackendServerUrl() +
                        "/users/me/groups/" + groupId + "/users/" + user.getId();

                JSONObject jsonObject = new JSONObject();

                // Request a string response from the provided URL.
                JsonObjectRequest putRequest = new JsonObjectRequest(Request.Method.DELETE, url,
                        jsonObject,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Log.d(TAG, "Remove friend. Response is: " + response);
                                UsersGroupsDataSingleton.getInstance().refreshGroupsInfo();
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Remove friend. That didn't work!" + error.toString());
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

    public ArrayList<User> getMembers() {
        return (ArrayList<User>) getIntent().getExtras().get("members");
    }

    public String getGroupId() {
        return getIntent().getStringExtra("groupId");
    }
}
