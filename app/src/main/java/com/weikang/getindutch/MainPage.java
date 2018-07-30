package com.weikang.getindutch;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;

public class MainPage extends AppCompatActivity {
    public static final int RC_SIGN_IN = 1;
    private static final int CAMERA_PIC_REQUEST = 2;
    public static final String GUEST = "guest";
    public static final String TAG = "MainPage";

    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseUser user;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mGroupDatabaseReference;
    private DatabaseReference mUsersDatabaseReference;
    private ProgressBar mProgressBar;
    private String mUsername;
    private Toolbar mToolbar;
    private ViewPager mViewPager;
    private SCTessOCR mSCTessOCR;
    private MPFFriendsUsersClass currentUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        mUsername = GUEST;
        mFirebaseAuth = FirebaseAuth.getInstance();
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        1);
            }
        }
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                if (user != null){
                    //user is signed in
                    mUsername = user.getDisplayName();
                    onSignedInInitialise(user.getUid());
                    mViewPager = (ViewPager) findViewById(R.id.container);
                    setupViewPage(mViewPager);
                    TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayout);
                    tabLayout.setupWithViewPager(mViewPager);
                } else {
                    //user is signed out
                    //TODO: onSignedOutCleanup method
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setAvailableProviders(Arrays.asList(
                                            new AuthUI.IdpConfig.EmailBuilder().build(),
                                            new AuthUI.IdpConfig.GoogleBuilder().build()))
                                    .build(),
                            RC_SIGN_IN);
                }
            }
        };



        //initialising buttons
        mToolbar = (Toolbar) findViewById(R.id.mainPageToolbar);
        setSupportActionBar(mToolbar);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mGroupDatabaseReference = mFirebaseDatabase.getReference().child("groups");
        mUsersDatabaseReference = mFirebaseDatabase.getReference().child("users");


    }

    public void onSignedInInitialise(String userUid){
        currentUser = new MPFFriendsUsersClass(userUid,mUsername);
        final DatabaseReference currDataRef = mUsersDatabaseReference.child(userUid);
        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()) {
                    //create new user
                    currDataRef.setValue(currentUser);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
        currDataRef.addListenerForSingleValueEvent(eventListener);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Signed in!", Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Sign in cancelled", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainpagemenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.sign_out_btn:
                //sign out
                AuthUI.getInstance().signOut(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mFirebaseAuth != null){
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    private void setupViewPage(ViewPager viewPager){
        SectionsPageAdapter adapter = new SectionsPageAdapter(getSupportFragmentManager());
        adapter.addFragment(new MPFGroupsPage(), "All");
        adapter.addFragment(new MPFSummaryPage(),"Summary");
        adapter.addFragment(new MPFFriendsPage(),"Friends");
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(2);
    }
}
