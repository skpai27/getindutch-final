package com.weikang.getindutch;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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

public class GroupsPage extends AppCompatActivity {

    private Button mAddButton;
    private RecyclerView mRecyclerView;
    private FirebaseAuth mAuth;
    private TextView groupNameText;
    private TextView myName;
    private ImageView myProfilePic;
    private TextView myBal;
    private ImageButton returnBtn;

    //variables for recyclerview Adapter
    private GroupMembersAdapter mAdapter;

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


        //Initialise Adapter and recyclerview etc
        mRecyclerView = (RecyclerView) findViewById(R.id.membersOfGroup);
        //use getActivity() instead of (this) for context cos this is a fragment
        mAdapter = new GroupMembersAdapter(mMembers, groupName, this);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        //initialise Firebase variables
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mMembersDatabaseReference = mFirebaseDatabase.getReference().child("groups").child(groupName).child("members");
        mMembersDatabaseReference.child(mAuth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String text = "You're owed $".concat(dataSnapshot.getValue().toString());
                myBal.setText(text);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

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
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    String membersUid = dataSnapshot.getKey();
                    if (!membersUid.equals(mAuth.getUid())) {
                        mFirebaseDatabase.getReference().child("users").child(membersUid).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot1) {
                                mMembers.add(friend);
                                mAdapter.notifyItemInserted(mMembers.size() - 1);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
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
}
