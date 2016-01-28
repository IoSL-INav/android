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
 * Created by Jan on 28. 01. 2016.
 */
public class UsersGroupsDataSingleton {

    private static final String TAG = UsersGroupsDataSingleton.class.toString();
    private static UsersGroupsDataSingleton mInstance;
    private static LinkedList<Group> groups;

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

    public static LinkedList<Group> getGroups() {
        return groups;
    }

    public void setGroups(LinkedList<Group> groups) {
        UsersGroupsDataSingleton.groups = groups;
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
                            LinkedList<String> members = new LinkedList<>();
                            boolean autoPing = false;
                            for (int i = 0; i < responseArr.length(); i++) {
                                JSONObject obj = responseArr.getJSONObject(i);
                                id = obj.getString("_id");
                                name = obj.getString("name");
                                autoPing = false;
                                JSONArray membersJson = obj.getJSONArray("members");
                                for (int j=0; j<membersJson.length(); j++){
                                    members.add(membersJson.getJSONObject(j).getString("name"));
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
}
