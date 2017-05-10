package tcd.android.com.howaboutthere;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int RC_SIGN_IN = 0;
    private static final int RC_PLACE_PICKER = 1;
    private static final int RC_GET_BACK_FRIEND_LIST = 2;
    private static final String EXTRA_FRIEND_LIST = "extraFriendList";
    private static final int RC_GET_USER_DECISION = 3;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mUserDatabaseReference;
    private DatabaseReference mFriendDatabaseReference;
    private DatabaseReference mInviteDatabaseReference;
    private DatabaseReference mGroupDatabaseReference;
    private ChildEventListener mChildEventListener;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseStorage mFirebaseStorage;

    private FloatingActionButton fabButton;

    private ListView lvPlan;
    private PlanListAdapter planListAdapter;
    private ArrayList<PlanDetail> planDetails;

    private String ownerId;
    private ArrayList<String> ownerFriendIdsList;
    private ArrayList<String> ownerFriendNameList;
    private ArrayList<String> idsListAccordingToFriendNameList;

    private Place place;
    private String dateSelected;
    private String timeSelected;

    private ArrayList<String> ownerGroupIds;
    private int currentChosenGroupPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // require internet connection
        checkNetworkState();

        // initialize database components
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseStorage = FirebaseStorage.getInstance();
        mUserDatabaseReference = mFirebaseDatabase.getReference().child("users");
        mFriendDatabaseReference = mFirebaseDatabase.getReference().child("friends");
        mInviteDatabaseReference = mFirebaseDatabase.getReference().child("invites");
        mGroupDatabaseReference = mFirebaseDatabase.getReference().child("groups");

        // Floating action button
        fabButton = (FloatingActionButton) findViewById(R.id.btnFab);
        fabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // picking a place (using google Places API)
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                try {
                    startActivityForResult(builder.build(MainActivity.this), RC_PLACE_PICKER);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });

        // listview of plans
        lvPlan = (ListView) findViewById(R.id.lvPlan);
        planListAdapter = new PlanListAdapter(this);
        lvPlan.setAdapter(planListAdapter);
        lvPlan.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                currentChosenGroupPosition = position;

                // show plan's extra infomation
                final Intent showPlanDetailIntent = new Intent(MainActivity.this, PlaceDetailsActivity.class);
                showPlanDetailIntent.putExtra("Place name", planDetails.get(position).getPlaceName());
                showPlanDetailIntent.putExtra("Place address", planDetails.get(position).getPlaceAddress());
                showPlanDetailIntent.putExtra("Date and time", planDetails.get(position).getDateTime());
                showPlanDetailIntent.putExtra("LatLng", planDetails.get(position).getPlaceLatLng());

                // retrieve friend names from their ids
                final HashMap<String, Integer> mapGroupStatuses = new HashMap<String, Integer>();
                mUserDatabaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        ArrayList<User> users = new ArrayList<User>();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            User user = snapshot.getValue(User.class);
                            if (planDetails.get(position).getMembers().containsKey(snapshot.getKey())) {
                                mapGroupStatuses.put(user.getName(),
                                        planDetails.get(position).getMembers().get(snapshot.getKey()));
                            }
                        }

                        showPlanDetailIntent.putExtra("Member statuses", mapGroupStatuses);
                        startActivityForResult(showPlanDetailIntent, RC_GET_USER_DECISION);
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {}
                });
            }
        });

        planDetails = new ArrayList<>();
        ownerGroupIds = new ArrayList<>();

        // firebase authentication
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                // user signs in successfully
                if (firebaseUser != null) {
                    ownerId = firebaseUser.getUid();
                    // write user to database
                    mUserDatabaseReference.child(firebaseUser.getUid())
                            .setValue(new User(firebaseUser.getDisplayName(), firebaseUser.getEmail()))
                            .addOnCompleteListener(MainActivity.this, new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Log.d(TAG, "onComplete: Add user info to database successfully");
                                    }
                                }
                            });
                    // get owner friends list (their ids)
                    mFriendDatabaseReference.child(ownerId)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    GenericTypeIndicator<ArrayList<String>> t = new GenericTypeIndicator<ArrayList<String>>() {};
                                    ownerFriendIdsList = dataSnapshot.getValue(t);
                                    if (ownerFriendIdsList == null || ownerFriendIdsList.isEmpty()) {
                                        ownerFriendIdsList = new ArrayList<String>();
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                    onSignedInInitialize();
                } else {                        // user signs out or need to be authenticated
                    onSignedOutCleanUp();
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setProviders(Arrays.asList(
                                            new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                                            new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build(),
                                            new AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER).build()))
                                    .build(),
                            RC_SIGN_IN);
                }
            }
        };
    }

    private void checkNetworkState() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetworkInfo == null || !activeNetworkInfo.isConnected()) {
            AlertDialog.Builder checkNetworkDialog = new AlertDialog.Builder(this);
            checkNetworkDialog.setTitle(getResources().getString(R.string.error_no_connection));
            checkNetworkDialog.setMessage(getResources().getString(R.string.check_network_connection));
            checkNetworkDialog.setCancelable(false);
            // add cancel button
            checkNetworkDialog.setNegativeButton(getResources().getString(R.string.quit),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            finish();
                        }
                    });
            // add try again button
            checkNetworkDialog.setPositiveButton(getResources().getString(R.string.try_again),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            checkNetworkState();
                        }
                    });
            checkNetworkDialog.show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            // sign in successfully
            case RC_SIGN_IN:
                if (resultCode == RESULT_OK) {
                    Toast.makeText(MainActivity.this,
                            getResources().getString(R.string.welcome_message),
                            Toast.LENGTH_SHORT).show();
                } else if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(MainActivity.this,
                            getResources().getString(R.string.cancel_sign_in_message),
                            Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            // pick a place from Place Picker
            case RC_PLACE_PICKER:
                if (resultCode == RESULT_OK) {
                    ownerFriendNameList = new ArrayList<>();
                    idsListAccordingToFriendNameList = new ArrayList<>();
                    // get owner's friend names (and ids)
                    mUserDatabaseReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            // get friend names (and ids)
                            ArrayList<User> users = new ArrayList<User>();
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                User user = snapshot.getValue(User.class);
                                if (ownerFriendIdsList.contains(snapshot.getKey())) {
                                    ownerFriendNameList.add(user.getName());
                                    idsListAccordingToFriendNameList.add(snapshot.getKey());
                                }
                            }

                            DatePickerDialog datePickerDialog = new DatePickerDialog(MainActivity.this,
                                    new DatePickerDialog.OnDateSetListener() {
                                        @Override
                                        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                            dateSelected = String.valueOf(dayOfMonth) + "/"
                                                    + String.valueOf(month + 1) + "/" + String.valueOf(year);

                                            final CharSequence[] timeList = {
                                                    getResources().getString(R.string.morning),
                                                    getResources().getString(R.string.afternoon),
                                                    getResources().getString(R.string.evening),
                                                    getResources().getString(R.string.night)
                                            };
                                            AlertDialog timePicker = new AlertDialog.Builder(MainActivity.this)
                                                    .setTitle(getResources().getString(R.string.set_your_time))
                                                    .setSingleChoiceItems(timeList, 0,
                                                            new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialog, int which) {
                                                                    timeSelected = (String) timeList[which];
                                                                }
                                                            }).setPositiveButton(getResources().getString(R.string.OK),
                                                            new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialog, int id) {
                                                                    // open invite friends activity
                                                                    Intent inviteFriendsIntent = new Intent(MainActivity.this, InviteFriendsActivity.class);
                                                                    inviteFriendsIntent.putStringArrayListExtra(EXTRA_FRIEND_LIST, ownerFriendNameList);
                                                                    startActivityForResult(inviteFriendsIntent, RC_GET_BACK_FRIEND_LIST);
                                                                }
                                                            }).setNegativeButton(getResources().getString(R.string.cancel),
                                                            new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialog, int id) {
                                                                    //  Your code when user clicked on Cancel
                                                                }
                                                            }).create();
                                            timePicker.show();
                                        }
                                    },
                                    Calendar.getInstance().get(Calendar.YEAR),
                                    Calendar.getInstance().get(Calendar.MONTH),
                                    Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
                            datePickerDialog.show();

                            // save the place and display its name in Toast
                            place = PlacePicker.getPlace(data, MainActivity.this);
                            String toastMsg = String.format("%s", place.getName());
                            Toast.makeText(MainActivity.this, toastMsg, Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {}
                    });
                }
                break;
            // get results (friends list) from the invite friends activity
            case RC_GET_BACK_FRIEND_LIST:
                if (resultCode == Activity.RESULT_OK) {
                    ArrayList<Integer> selectedFriends = new ArrayList<>();
                    if (data.hasExtra(EXTRA_FRIEND_LIST)) {
                        selectedFriends = data.getIntegerArrayListExtra(EXTRA_FRIEND_LIST);

                        // create new group id
                        final String groupId = mGroupDatabaseReference.push().getKey();

                        // construct the chosen friends list
                        ArrayList<String> membersInGroup = new ArrayList<>();
                        for (int index : selectedFriends) {
                            membersInGroup.add(idsListAccordingToFriendNameList.get(index));
                        }

                        // add chosen friends to group
                        HashMap<String, Integer> mapGroupStatuses = new HashMap<>();
                        for (String friendId : membersInGroup) {
                            mapGroupStatuses.put(friendId, -1);
                        }
                        mapGroupStatuses.put(ownerId, 1);
                        mGroupDatabaseReference.child(groupId).child("persons").setValue(mapGroupStatuses);

                        // add the chosen place to group
                        HashMap<String, String> mapPlaceInfo = new HashMap<>();
                        mapPlaceInfo.put("Place name", place.getName().toString());
                        mapPlaceInfo.put("Place address", place.getAddress().toString());
                        mapPlaceInfo.put("LatLng", String.valueOf(place.getLatLng().latitude) + ","
                                + String.valueOf(place.getLatLng().longitude));
                        mapPlaceInfo.put("Date and time", timeSelected + ", " + dateSelected);
                        mGroupDatabaseReference.child(groupId).child("place").setValue(mapPlaceInfo);

                        // add group's id to each member
                        membersInGroup.add(ownerId);
                        for (final String member : membersInGroup) {
                            // get the member's groups list first
                            mInviteDatabaseReference.child(member)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            GenericTypeIndicator<ArrayList<String>> t = new GenericTypeIndicator<ArrayList<String>>() {};
                                            ArrayList<String> groupsContainMember = dataSnapshot.getValue(t);
                                            if (groupsContainMember == null) {
                                                groupsContainMember = new ArrayList<String>();
                                            }
                                            // then add the chosen group's id
                                            groupsContainMember.add(groupId);
                                            // and overwrite the database
                                            mInviteDatabaseReference.child(member).setValue(groupsContainMember);
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {}
                                    });
                        }
                    }
                    Log.d(TAG, "onActivityResult: " + selectedFriends.toString());
                }
                break;
            // get user decision (yes or no) from the place details activity
            case RC_GET_USER_DECISION:
                final int userDecision = data.getIntExtra("User decision", 0);
                // not choosing neither options
                if (userDecision == -1) {
                    break;
                }
                // update decision in the database
                mGroupDatabaseReference.child(ownerGroupIds.get(currentChosenGroupPosition))
                        .child("persons").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // get the group member decisions
                        GenericTypeIndicator<HashMap<String, Integer>> t =
                                new GenericTypeIndicator<HashMap<String, Integer>>() {};
                        HashMap<String, Integer> updatedGroupStatuses = dataSnapshot.getValue(t);
                        // then update it with the new decision
                        updatedGroupStatuses.put(ownerId, userDecision);
                        // and overwrite it
                        mGroupDatabaseReference.child(ownerGroupIds.get(currentChosenGroupPosition))
                                .child("persons").setValue(updatedGroupStatuses);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {}
                });
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // sign out options
            case R.id.sign_out_menu:
                AuthUI.getInstance().signOut(this);
                return true;
            // show owner's id
            case R.id.my_id_menu:
                AlertDialog.Builder myIdDialog = new AlertDialog.Builder(this);
                myIdDialog.setTitle(getResources().getString(R.string.your_id));
                myIdDialog.setMessage(ownerId);
                myIdDialog.show();
                return true;
            // add friend option
            case R.id.add_friend_menu:
                // display a dialog with EditText
                AlertDialog.Builder addFriendDialog = new AlertDialog.Builder(this);
                final EditText edittext = new EditText(MainActivity.this);
                addFriendDialog.setTitle(getResources().getString(R.string.enter_friend_id));
                addFriendDialog.setView(edittext);
                // add cancel button
                addFriendDialog.setNegativeButton(getResources().getString(R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                            }
                        });
                // and OK button
                addFriendDialog.setPositiveButton(getResources().getString(R.string.OK),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                final String friendId = edittext.getText().toString();
                                // check in the database
                                mUserDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        ArrayList<User> users = new ArrayList<User>();
                                        // whether the entered id exists or not
                                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                            User user = snapshot.getValue(User.class);
                                            // if it does exist
                                            if (friendId.equals(snapshot.getKey())) {
                                                // overwrite the friends list with the new friend added
                                                ownerFriendIdsList.add(friendId);
                                                mFriendDatabaseReference.child(ownerId).setValue(ownerFriendIdsList);
                                                return;
                                            }
                                        }
                                        // or if it does not
                                        Toast.makeText(MainActivity.this,
                                                getResources().getString(R.string.error_user_not_exist),
                                                Toast.LENGTH_SHORT).show();
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                    }
                                });
                            }
                        });
                addFriendDialog.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
    }

    private void onSignedInInitialize() {
        attachDatabaseReadListener();
    }

    private void onSignedOutCleanUp() {
        detachDatabaseReadListener();
        planListAdapter.clear();                // clear the plans list
    }

    private void attachDatabaseReadListener() {
        if (mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    createPlanList(dataSnapshot);
                }
                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    ownerGroupIds.clear();
                    planListAdapter.clear();
                    planDetails.clear();
                    createPlanList(dataSnapshot);
                }
                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {}
                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
                @Override
                public void onCancelled(DatabaseError databaseError) {}
            };
            mInviteDatabaseReference.addChildEventListener(mChildEventListener);
        }
    }

    private void createPlanList(DataSnapshot dataSnapshot) {
        // check if it belongs to the owner
        if (!dataSnapshot.getKey().equals(ownerId)) {
            return;
        }

        // get the owner's groups id
        GenericTypeIndicator<ArrayList<String>> t = new GenericTypeIndicator<ArrayList<String>>() {};
        final ArrayList<String> groupIds = dataSnapshot.getValue(t);

        // for each group...
        for (final String groupId : groupIds) {
            ownerGroupIds.add(groupId);
            mGroupDatabaseReference.child(groupId).child("persons")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            // get the statuses (decisions) of the members
                            GenericTypeIndicator<HashMap<String, Integer>> t =
                                    new GenericTypeIndicator<HashMap<String, Integer>>() {};
                            final HashMap<String, Integer> mapGroupStatuses = dataSnapshot.getValue(t);
                            mGroupDatabaseReference.child(groupId).child("place")
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            // and the place's info
                                            GenericTypeIndicator<HashMap<String, String>> t1 =
                                                    new GenericTypeIndicator<HashMap<String, String>>() {};
                                            HashMap<String, String> mapPlaceInfo = dataSnapshot.getValue(t1);
                                            // including number of goings, and busy ones
                                            int going = 0, busy = 0;
                                            for (Map.Entry<String, Integer> entry : mapGroupStatuses.entrySet()) {
                                                if (entry.getValue() == 1)
                                                    going++;
                                                else if (entry.getValue() == 0)
                                                    busy++;
                                            }

                                            Plan plan = new Plan(
                                                    mapPlaceInfo.get("Place name"),
                                                    mapPlaceInfo.get("Place address"),
                                                    mapPlaceInfo.get("Date and time"),
                                                    mapGroupStatuses.size(), going, busy);

                                            // add to the list view
                                            planListAdapter.add(plan);

                                            // and the auxiliary list
                                            PlanDetail planDetail = new PlanDetail(
                                                    mapPlaceInfo.get("Place name"),
                                                    mapPlaceInfo.get("Place address"),
                                                    mapPlaceInfo.get("Date and time"),
                                                    mapPlaceInfo.get("LatLng"),
                                                    mapGroupStatuses
                                            );
                                            planDetails.add(planDetail);
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {}
                                    });
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {}
                    });
        }
    }

    private void detachDatabaseReadListener() {
        if (mChildEventListener != null) {
            mInviteDatabaseReference.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
    }
}