package de.tu_berlin.indoornavigation.singletons;

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

import de.tu_berlin.indoornavigation.utils.AuthUtils;
import de.tu_berlin.indoornavigation.IndoorNavigation;
import de.tu_berlin.indoornavigation.entities.CompanionRequest;
import de.tu_berlin.indoornavigation.entities.Group;
import de.tu_berlin.indoornavigation.entities.User;

/**
 * Created by Jan on 28. 01. 2016.
 */
public class UsersGroupsDataSingleton {

    private static final String TAG = UsersGroupsDataSingleton.class.toString();
    private static UsersGroupsDataSingleton mInstance;
    private LinkedList<Group> groups;
    private LinkedList<CompanionRequest> companionRequests;

    public UsersGroupsDataSingleton() {
        this.groups = new LinkedList<>();
        refreshGroupsInfo();
    }

    public static synchronized UsersGroupsDataSingleton getInstance() {
        if (mInstance == null) {
            mInstance = new UsersGroupsDataSingleton();
        }
        return mInstance;
    }

    public LinkedList<Group> getGroups() {
        return groups;
    }

    public void setGroups(LinkedList<Group> groups) {
        this.groups = groups;
    }

    public LinkedList<CompanionRequest> getCompanionRequests() {
        return companionRequests;
    }

    public void setCompanionRequests(LinkedList<CompanionRequest> companionRequests) {
        this.companionRequests = companionRequests;
    }

    /**
     * Query data about groups
     */
    public void refreshGroupsInfo() {
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

                            LinkedList<Group> groups = new LinkedList<>();
                            String id = "";
                            String name = "";
                            LinkedList<User> members = new LinkedList<>();
                            boolean autoPing = false;
                            for (int i = 0; i < responseArr.length(); i++) {
                                JSONObject obj = responseArr.getJSONObject(i);
                                id = obj.getString("_id");
                                name = obj.getString("name");
                                autoPing = false;
                                JSONArray membersJson = obj.getJSONArray("members");
                                for (int j = 0; j < membersJson.length(); j++) {
                                    String userId = membersJson.getJSONObject(j).getString("_id");
                                    String userUsername = membersJson.getJSONObject(j).getString
                                            ("name");
                                    members.add(new User(userId, userUsername, null, null, null));
                                }
                                groups.add(new Group(id, name, autoPing, members));
                            }
                            setGroups(groups);
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

    public void refreshCompanionRequestsInfo() {

        // get companion requests
        String url = PropertiesSingleton.getInstance().getBackendServerUrl() + "/companionrequests";

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String responseStr) {
                        Log.d(TAG, "Companion requests. Response is: " + responseStr);

                        try {
                            JSONArray responseArr = new JSONArray(responseStr);

                            LinkedList<CompanionRequest> companionRequests = new LinkedList<>();
                            for (int i = 0; i < responseArr.length(); i++) {
                                JSONObject obj = responseArr.getJSONObject(i);
                                String id = obj.getString("_id");
                                JSONObject from = obj.getJSONObject("from");

                                String fromId = from.getString("_id");
                                String fromUsername = from.getString("name");

                                companionRequests.add(new CompanionRequest(id, new User(fromId,
                                        fromUsername, null, null, null)));
                            }
                            setCompanionRequests(companionRequests);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Companion requests. That didn't work!" + error.toString());
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
