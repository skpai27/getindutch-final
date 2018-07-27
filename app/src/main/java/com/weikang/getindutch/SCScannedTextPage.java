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
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SCScannedTextPage extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private SCReceiptItemAdapter mAdapter = null;
    private Button mConfirm;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mGroupDatabaseRef;
    private List<String> mGroups;
    private ArrayAdapter<String> mSpinnerAdapter;
    private Spinner groupSelectorSpinner;
    private String targetGroupName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanned_text_page);

        //initialising widgets
        mRecyclerView = findViewById(R.id.receiptItems);
        groupSelectorSpinner = findViewById(R.id.groupSelector);
        mConfirm = (Button) findViewById(R.id.confirmBtn);

        //initialising auth and database
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mGroupDatabaseRef = mFirebaseDatabase.getReference().child("groups");

        //Set up adapter for group selector spinner
        mGroups = new ArrayList<>();
        mFirebaseDatabase.getReference().child("users").child(mAuth.getUid()).child("groups").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()){
                    mGroups.add(ds.getKey());
                }
                mSpinnerAdapter = new ArrayAdapter<>(SCScannedTextPage.this, android.R.layout.simple_spinner_item, mGroups);
                mSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                groupSelectorSpinner.setAdapter(mSpinnerAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        groupSelectorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                targetGroupName = parent.getItemAtPosition(position).toString();
                //retrieve text from ocr
                Bundle bundle = getIntent().getExtras();
                ArrayList<SCReceiptItem> srcText = bundle.getParcelableArrayList("srcText");
                srcText.get(0).setTargetGroup(targetGroupName);


                //initialise adapter view
                if (mRecyclerView.getAdapter() != null){
                    mRecyclerView.setAdapter(null);
                }
                mAdapter = new SCReceiptItemAdapter(srcText,SCScannedTextPage.this);
                mRecyclerView.setAdapter(mAdapter);
                mRecyclerView.setLayoutManager(new LinearLayoutManager(SCScannedTextPage.this));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        //confirm button on click
        mConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    mAdapter.submit();
                    Toast.makeText(SCScannedTextPage.this, "These costs have been added.", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(SCScannedTextPage.this,MainPage.class);
                    startActivity(intent);
                } catch (ArithmeticException e){
                    Toast.makeText(SCScannedTextPage.this,"Please make sure at least one check box is ticked for each item.", Toast.LENGTH_LONG ).show();
                }
            }
        });
    }
}
