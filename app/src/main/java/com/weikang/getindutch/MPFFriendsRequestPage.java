package com.weikang.getindutch;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MPFFriendsRequestPage extends AppCompatActivity {

    //Firebase user
    private FirebaseAuth mAuth;
    private String currUserUid;

    //Adapter
    private RecyclerView mFriendRequests;
    private ArrayList<MPFFriendsUsersClass> mFriendReqList;
    private MPFFriendRequestAdapter mAdapter;
    private ChildEventListener mChildEventListener;

    //Database
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mFriendReqDB;
    private DatabaseReference mUsers;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mpf_activity_friend_request_page);

        //User info
        mAuth = FirebaseAuth.getInstance();
        currUserUid = mAuth.getCurrentUser().getUid();

        //Initiate databases
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFriendReqDB = mFirebaseDatabase.getReference().child("friendReq").child(currUserUid);
        mUsers = mFirebaseDatabase.getReference().child("users");

        //Set up adapter
        mFriendRequests = (RecyclerView) findViewById(R.id.friend_requests_list);
        mFriendReqList = new ArrayList<>();
        mAdapter = new MPFFriendRequestAdapter(mFriendReqList,this);
        mFriendRequests.setAdapter(mAdapter);
        mFriendRequests.setLayoutManager(new LinearLayoutManager(this));

        if (mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(final DataSnapshot dataSnapshot, String s) {
                    mAdapter.clear();
                    for (DataSnapshot requests : dataSnapshot.getChildren()){
                        if(!requests.getValue().toString().equals("true")){
                            mUsers.child(dataSnapshot.getKey()).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot1) {
                                    String name = dataSnapshot1.getValue().toString();
                                    MPFFriendsUsersClass friend = new MPFFriendsUsersClass(dataSnapshot.getKey(),name);
                                    mFriendReqList.add(friend);
                                    mAdapter.notifyItemInserted(mFriendReqList.size() - 1);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                        }
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
            //add a child event listener. SO the reference (mGroupDatabaseRef) defines which part of
            //database to listen to, mchildEventlistener defines what to do
            mFriendReqDB.child("received").addChildEventListener(mChildEventListener);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        mAdapter.clear();
        mChildEventListener = null;
    }
}
