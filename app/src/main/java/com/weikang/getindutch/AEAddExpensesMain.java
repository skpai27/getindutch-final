package com.weikang.getindutch;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AEAddExpensesMain extends AppCompatActivity {

    private Spinner mPayeeSpinner;
    private Spinner mGroupSpinner;
    private EditText mExpense;
    private EditText mDescription;
    private Button mButtonAdd;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mUserGroupsDatabaseReference;
    private DatabaseReference mGroupDatabaseReference;
    private DatabaseReference mUserReference;
    private DatabaseReference mGroupSizeDatabaseReference;
    private DatabaseReference mMembersDatabaseReference;
    private DatabaseReference mExpenseRecordsReference;
    private ChildEventListener mChildEventListener;

    //RecyclerView for members to split bill with
    private AEUserAddExpenseAdapter mAdapter;
    private RecyclerView mRecyclerView;

    private String selectedGroup;

    private final String TAG = "AddExpensesActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_expenses_manual);

        //Firebase Auth variables
        final FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser mUser = mAuth.getCurrentUser();
        final String mUserId = mUser.getUid();

        //Firebase Database initialisation
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        //Expenses database reference (to put expense information)
        mExpenseRecordsReference = mFirebaseDatabase.getReference().child("expenseRecords");

        mExpense = findViewById(R.id.edittext_Expense);
        mDescription = findViewById(R.id.edittext_description);
        //initialise button
        mButtonAdd = findViewById(R.id.button_add);

        //RecyclerView config
        final ArrayList<AEUserAddExpenseClass> mUsers = new ArrayList<>();
        //Initialise Adapter and recyclerview etc
        mRecyclerView = findViewById(R.id.recyclerView);
        //use getActivity() instead of (this) for context cos this is a fragment
        mAdapter = new AEUserAddExpenseAdapter(this, mUsers);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        //Configuring Payee Spinner
        mPayeeSpinner = findViewById(R.id.spinner_payee);
        //create arrayadapter using String array and a default spinner layout
        ArrayAdapter<CharSequence> payeeSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.payee_array, android.R.layout.simple_spinner_item);
        payeeSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mPayeeSpinner.setAdapter(payeeSpinnerAdapter);

        //Configuring Group Spinner
        mGroupSpinner = findViewById(R.id.spinner_group);
        //create List of groups that user belongs to
        final List<String> groups = new ArrayList<String>();
        mUserGroupsDatabaseReference = mFirebaseDatabase.getReference().child("users").child(mUserId).child("groups");
        mUserGroupsDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot groupSnapshot: dataSnapshot.getChildren()) {
                    String groupName = groupSnapshot.getKey();
                    groups.add(groupName);
                }

                //create arrayadapter using the list above and a default spinner layout
                ArrayAdapter<String> groupSpinnerAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, groups);
                groupSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                mGroupSpinner.setAdapter(groupSpinnerAdapter);

                mGroupSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
                    @Override
                    public void onItemSelected(AdapterView parent, View view, int position, long id){
                        selectedGroup = parent.getItemAtPosition(position).toString();
                        refreshAdapter(mUserId, mUsers);
                        //TODO: execute function to update recyclerview
                    }
                    @Override
                    public void onNothingSelected(AdapterView parent){}
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

        mButtonAdd.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                final Float expense = Float.parseFloat(mExpense.getText().toString());
                String description = mDescription.getText().toString();
                /*Log.i(TAG, "before user.getiud");
                for(AEUserAddExpenseClass user:mAdapter.checkedUsers){
                    Log.i(TAG, user.getName());
                }*/
                //Hashmap to store uid:userName maps for expenseRecord
                HashMap<String, String> peopleSharingExpense = new HashMap<>();
                //to insert all Uids into an arraylist so that we can check if they are selected
                final ArrayList<String> selectedUserUids = new ArrayList<>();
                for(AEUserAddExpenseClass user:mAdapter.checkedUsers){
                    selectedUserUids.add(user.getUid());
                    peopleSharingExpense.put(user.getUid(), user.getName());
                }


                //Firebase Database variables
                //get reference to the group that was selected
                mGroupDatabaseReference = mFirebaseDatabase.getReference().child("groups").child(selectedGroup);
                mGroupSizeDatabaseReference = mGroupDatabaseReference.child("size");
                mMembersDatabaseReference = mGroupDatabaseReference.child("members");

                //TODO: change this url to be variable
                String expenseIconUrl = "https://www.shareicon.net/download/2016/08/18/809809_cab_512x512.png";
                //Creating expenseRecord to store in database
                MPFSummaryPageExpensesClass expenseRecord = new MPFSummaryPageExpensesClass(mUserId,
                        selectedGroup, description, expense, expense/(mAdapter.checkedUsers.size() + 1), peopleSharingExpense, expenseIconUrl );
                mExpenseRecordsReference.push().setValue(expenseRecord);

                mGroupDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        //int sizeX = Integer.valueOf(dataSnapshot.child("size").getValue().toString());
                        //sizeOfGroup[0] = sizeX;

                        //Iterating through each user to add the expense
                        final Float expenseToAdd = expense / (mAdapter.checkedUsers.size() + 1);

                        //iterating through each member in the group using childeventlistener
                        mMembersDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                for (DataSnapshot memberSnapshot : dataSnapshot.getChildren()) {
                                    if (memberSnapshot.getKey().equals(mUserId)) {
                                        float newValue = Float.parseFloat(memberSnapshot.getValue().toString()) + expense - expenseToAdd;

                                        mMembersDatabaseReference.child(memberSnapshot.getKey()).setValue(newValue);
                                    } else {
                                        if(selectedUserUids.contains(memberSnapshot.getKey())) {
                                            float newValue = Float.parseFloat(memberSnapshot.getValue().toString()) - expenseToAdd;
                                            mMembersDatabaseReference.child(memberSnapshot.getKey()).setValue(newValue);
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {}
                });

                Toast.makeText(getApplicationContext(),"Expense added!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(AEAddExpensesMain.this,MainPage.class);
                startActivity(intent);
            }
        });
    }

    //function to change the recyclerview everytime the selection of the group changes
    private void refreshAdapter(final String mUserId, final ArrayList<AEUserAddExpenseClass> mUsers){
        mRecyclerView.setAdapter(null);
        mAdapter.clear();
        mAdapter.checkedUsers.clear();
        mGroupDatabaseReference = mFirebaseDatabase.getReference().child("groups").child(selectedGroup).child("members");
        mGroupDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (final DataSnapshot memberSnapshot : dataSnapshot.getChildren()){
                    if(!memberSnapshot.getKey().equals(mUserId)) {
                        mUserReference = mFirebaseDatabase.getReference().child("users").child(memberSnapshot.getKey());
                        mUserReference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                String userName = dataSnapshot.child("name").getValue().toString();
                                AEUserAddExpenseClass newUser = new AEUserAddExpenseClass(memberSnapshot.getKey(), userName);
                                mUsers.add(newUser);
                                mAdapter.checkedUsers.add(newUser);
                                mAdapter.notifyItemInserted(mUsers.size() - 1);
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {}
                        });

                    }
                }
                mRecyclerView.setAdapter(mAdapter);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

    }

}

