package tcd.android.com.howaboutthere;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.IntegerRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;

import java.util.ArrayList;

public class InviteFriendsActivity extends AppCompatActivity {

    private static final String TAG = "Invite Friends Activity";

    private ListView lvFriendList;
    private FriendListAdapter adapter;
    private ArrayList<String> nameList;
    private ArrayList<Integer> selectedFriends;

    private static final String EXTRA_FRIEND_LIST = "extraFriendList";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_friends);

        lvFriendList = (ListView) findViewById(R.id.lvFriendList);
        adapter = new FriendListAdapter(this);
        nameList = new ArrayList<>();
        selectedFriends = new ArrayList<>();
        lvFriendList.setAdapter(adapter);

        // retrieve friends list from previous activity's intent
        Intent friendListIntent = getIntent();
        if (friendListIntent.hasExtra(EXTRA_FRIEND_LIST)) {
            nameList = friendListIntent.getStringArrayListExtra(EXTRA_FRIEND_LIST);
            adapter.addAll(nameList);
        }

        // trigger checkbox
        lvFriendList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CheckBox cb = (CheckBox) view.findViewById(R.id.cbFriend);
                cb.setChecked(!cb.isChecked());
                if (cb.isChecked()) {
                    selectedFriends.add(position);
                } else {
                    selectedFriends.remove(selectedFriends.indexOf(position));
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.invite_friends_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // get back to main activity and return selected friends
            case R.id.next_menu:
                if (selectedFriends.size() == 0)
                {
                    Toast.makeText(this, "Please choose at least one friend", Toast.LENGTH_SHORT).show();
                    return true;
                }
                Intent resultIntent = new Intent();
                resultIntent.putIntegerArrayListExtra(EXTRA_FRIEND_LIST, selectedFriends);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
