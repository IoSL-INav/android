package de.tu_berlin.indoornavigation;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class GroupsActivity extends AppCompatActivity implements GroupFragment.OnListFragmentInteractionListener {

    private static final String TAG = GroupsActivity.class.toString();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups);
    }

    public void onListFragmentInteraction(Group group) {
        Log.d(TAG, group.getName());

        Intent intent = new Intent(this, UsersActivity.class);
        intent.putExtra("members", group.getMembers());
        intent.putExtra("groupId", group.getId());
        startActivity(intent);
    }

}
