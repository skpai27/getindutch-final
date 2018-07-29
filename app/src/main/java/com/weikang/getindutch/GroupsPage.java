package com.weikang.getindutch;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class GroupsPage extends AppCompatActivity {

    private Button mAddButton;
    private RecyclerView mRecyclerView;
    private FirebaseAuth mAuth;
    private TextView groupNameText;
    private TextView myName;
    private ImageView myProfilePic;
    private TextView myBal;
    private ImageButton returnBtn;
    private float myBalInFloat;

    //variables for recyclerview Adapter
    private ArrayList<MPFFriendsUsersClass> mMembers = new ArrayList<>();
    private ArrayList<MPFFriendsUsersClass> mMembersAlgo = new ArrayList<>();
    private GroupMembersAdapter mAdapter;
    private MPFFriendsUsersClass myself;

    //Firebase variables
    //Database
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mMembersDatabaseReference;
    private ChildEventListener mChildEventListener;

    private Dialog mAddMembers;
    private String groupName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups_page);
        Bundle b = getIntent().getExtras();
        groupName = b.getString("groupName");

        groupNameText = findViewById(R.id.groupName);
        groupNameText.setText(groupName);

        mAddMembers = new Dialog(this);

        returnBtn = (ImageButton) findViewById(R.id.returnButton);
        mAddButton = (Button) findViewById(R.id.addMembers);
        myBal = (TextView) findViewById(R.id.my_balance);
        myName = (TextView) findViewById(R.id.myname);
        myProfilePic = (ImageView) findViewById(R.id.myprofilePic);
        mAuth = FirebaseAuth.getInstance();

        myName.setText(mAuth.getCurrentUser().getDisplayName());
        myself = new MPFFriendsUsersClass(mAuth.getUid(),mAuth.getCurrentUser().getDisplayName());
        mMembersAlgo.add(myself);


        //Initialise Adapter and recyclerview etc
        mRecyclerView = (RecyclerView) findViewById(R.id.membersOfGroup);
        //use getActivity() instead of (this) for context cos this is a fragment
        mAdapter = new GroupMembersAdapter(mMembers, groupName, this);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        //initialise Firebase variables
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mMembersDatabaseReference = mFirebaseDatabase.getReference().child("groups").child(groupName).child("members");
        startView();

        returnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GroupsPage.this, MainPage.class);
                startActivity(intent);
            }
        });

        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddMemberDialog();
            }
        });

    }

    private void showAddMemberDialog() {
        final DatabaseReference mUsersDatabaseReference = FirebaseDatabase.getInstance().getReference().child("users");
        final DatabaseReference mGroupsDatabaseReference = FirebaseDatabase.getInstance().getReference().child("groups").child(groupName);
        TextView cancelBtn;
        TextView nextBtn;
        final EditText userName;
        mAddMembers.setContentView(R.layout.add_group_members_dialog);
        cancelBtn = (TextView) mAddMembers.findViewById(R.id.cancelBtn3);
        nextBtn = (TextView) mAddMembers.findViewById(R.id.nextBtn3);
        userName = (EditText) mAddMembers.findViewById(R.id.userName2);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAddMembers.dismiss();
            }
        });
        mAddMembers.show();
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String currUserName = userName.getText().toString();
                if (currUserName.isEmpty()) {
                    Toast.makeText(GroupsPage.this, "Please enter a username!", Toast.LENGTH_SHORT).show();
                } else {
                    ValueEventListener eventListener = new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (!dataSnapshot.exists()) {
                                //create new user
                                Toast.makeText(GroupsPage.this, currUserName + " is not found! Please try again.", Toast.LENGTH_SHORT).show();
                            } else {
                                // THE CODE BELOW IS UNDER THE ASSUMPTION THAT THERE'S ONLY ONE USER WITH THIS USERNAME.
                                for (DataSnapshot friend : dataSnapshot.getChildren()) {
                                    final String friendUID = friend.getKey().toString();
                                    final DatabaseReference userRef = mGroupsDatabaseReference.child("members").child(friendUID);
                                    ValueEventListener eventListener2 = new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot1) {
                                            if (!dataSnapshot1.exists()) {
                                                //create new user
                                                Toast.makeText(GroupsPage.this, currUserName + " has been added.", Toast.LENGTH_SHORT).show();
                                                mGroupsDatabaseReference.child("members").child(friendUID).setValue(new Float(0));
                                                mUsersDatabaseReference.child(friendUID).child("groups").child(groupName).setValue(true);
                                                mChildEventListener = null;
                                                mAddMembers.dismiss();
                                            } else {
                                                Toast.makeText(GroupsPage.this, currUserName + " is already your friend!", Toast.LENGTH_SHORT).show();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                        }
                                    };
                                    userRef.addListenerForSingleValueEvent(eventListener2);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    };
                    mUsersDatabaseReference.orderByChild("name").equalTo(currUserName).addListenerForSingleValueEvent(eventListener);

                }
            }
        });
    }

    public void startView() {
        if (mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {
                //datasnapshot contains data at that location when listener is triggered
                //first adding group object into the arraylist, then use adpater.notifyiteminserted

                @Override
                public void onChildAdded(final DataSnapshot dataSnapshot, String s) {
                    String membersUid = dataSnapshot.getKey();
                    if (!membersUid.equals(mAuth.getUid())) {
                        mFirebaseDatabase.getReference().child("users").child(membersUid).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot1) {
                                MPFFriendsUsersClass friend = new MPFFriendsUsersClass(dataSnapshot1.child("uid").getValue().toString(), dataSnapshot1.child("name").getValue().toString());
                                float value = Float.parseFloat(dataSnapshot.getValue().toString());
                                value = Math.round(value * 100) / (float) 100.0;
                                friend.setBal(value);
                                mMembers.add(friend);
                                mMembersAlgo.add(friend);
                                mAdapter.notifyItemInserted(mMembers.size() - 1);
                                processBalance();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    } else {
                        myBalInFloat = Math.round(Float.parseFloat(dataSnapshot.getValue().toString()) * 100) / (float) 100.0;
                        myself.setBal(myBalInFloat);
                        processBalance();
                    }

                }

                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                }

                public void onChildRemoved(DataSnapshot dataSnapshot) {
                }

                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                }

                public void onCancelled(DatabaseError databaseError) {
                }
            };
            mMembersDatabaseReference.addChildEventListener(mChildEventListener);
        }
    }

    public void processBalance(){
        Collections.sort(mMembersAlgo);
        int front = 0;
        int back = mMembersAlgo.size() - 1;
        if (back == -1){
            return;
        }
        float high = mMembersAlgo.get(back).getBal();
        float low = mMembersAlgo.get(front).getBal();
        while (front < back){
            if (high + low < 0){
                Log.d("debug", mMembersAlgo.get(back).getName() + " set " + mMembersAlgo.get(front).getName() + " as payee.");
                low = low + high;
                mMembersAlgo.get(back--).setPayee(mMembersAlgo.get(front).getName());
                high = mMembersAlgo.get(front).getBal();
            } else if (high + low > 0) {
                Log.d("debug", mMembersAlgo.get(front).getName() + " set " + mMembersAlgo.get(back).getName() + " as payee.");
                high = high + low;
                mMembersAlgo.get(front++).setPayee(mMembersAlgo.get(back).getName());
                low = mMembersAlgo.get(back).getBal();
            } else {
                Log.d("debug", mMembersAlgo.get(front).getName() + " set " + mMembersAlgo.get(back).getName() + " as payee.");
                mMembersAlgo.get(front++).setPayee(mMembersAlgo.get(back--).getName());
                low = mMembersAlgo.get(front).getBal();
                high = mMembersAlgo.get(back).getBal();
            }
        }
        Log.d("debug",mMembersAlgo.toString());
        if(myBalInFloat<0){
            String payee = "";
            for (MPFFriendsUsersClass member : mMembersAlgo){
                if (member.getName().equals(mAuth.getCurrentUser().getDisplayName())){
                    payee = member.getPayee();
                    myBal.setText("You owe " + payee + " $" + -member.getBal());
                    myBal.setTextColor(Color.RED);
                    break;
                }
            }
        } else {
            myBal.setText("You are owed $" + myBalInFloat);
            myBal.setTextColor(getResources().getColor(R.color.green));
        }
    }
}
