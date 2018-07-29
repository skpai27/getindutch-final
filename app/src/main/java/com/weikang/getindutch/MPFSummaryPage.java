package com.weikang.getindutch;

        import android.os.Bundle;
        import android.support.annotation.NonNull;
        import android.support.annotation.Nullable;
        import android.support.v4.app.Fragment;
        import android.support.v7.widget.LinearLayoutManager;
        import android.support.v7.widget.RecyclerView;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;

        import com.google.firebase.auth.FirebaseAuth;
        import com.google.firebase.database.ChildEventListener;
        import com.google.firebase.database.DataSnapshot;
        import com.google.firebase.database.DatabaseError;
        import com.google.firebase.database.DatabaseReference;
        import com.google.firebase.database.FirebaseDatabase;

        import java.util.ArrayList;

public class MPFSummaryPage extends Fragment {
    private static final String TAG = "SummaryPageFragment";

    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mExpenseRecordsDatabaseReference;
    private ChildEventListener mChildEventListener;

    private RecyclerView mRecyclerViewLeft;
    private RecyclerView mRecyclerViewRight;

    private ArrayList<MPFSummaryPageExpensesClass> mExpenseRecordsLeft;
    private MPFSummaryPageAdapterLeft mAdapterLeft;

    private ArrayList<MPFSummaryPageExpensesClass> mExpenseRecordsRight;
    private MPFSummaryPageAdapterRight mAdapterRight;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.summary_page,container,false);

        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mExpenseRecordsDatabaseReference = mFirebaseDatabase.getReference().child("expenseRecords");

        mExpenseRecordsLeft = new ArrayList<>();
        mExpenseRecordsRight = new ArrayList<>();

        mAdapterLeft = new MPFSummaryPageAdapterLeft(mExpenseRecordsLeft, getActivity());
        mAdapterRight = new MPFSummaryPageAdapterRight(mExpenseRecordsRight, getActivity());

        mRecyclerViewLeft = view.findViewById(R.id.recycler_view_green);
        mRecyclerViewLeft.setAdapter(mAdapterLeft);
        mRecyclerViewLeft.setLayoutManager(new LinearLayoutManager(getActivity()));

        mRecyclerViewRight = view.findViewById(R.id.recycler_view_red);
        mRecyclerViewRight.setAdapter(mAdapterRight);
        mRecyclerViewRight.setLayoutManager(new LinearLayoutManager(getActivity()));

        startView();
        return view;
    }

    public void startView(){
        if (mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {
                //datasnapshot contains data at that location when listener is triggered
                //first adding group object into the arraylist, then use adpater.notifyiteminserted

                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    MPFSummaryPageExpensesClass expenseRecord = dataSnapshot.getValue(MPFSummaryPageExpensesClass.class);
                    if(expenseRecord.getPayerUid().equals(mAuth.getUid())) {
                        mExpenseRecordsLeft.add(expenseRecord);
                        mAdapterLeft.notifyItemInserted(mExpenseRecordsLeft.size() - 1);
                    } else if(expenseRecord.getPeopleSharingExpense().containsKey(mAuth.getUid())){
                        mExpenseRecordsRight.add(expenseRecord);
                        mAdapterRight.notifyItemInserted(mExpenseRecordsRight.size() - 1);
                    }
                }
                public void onChildChanged(DataSnapshot dataSnapshot, String s) { }
                public void onChildRemoved(DataSnapshot dataSnapshot) { }
                public void onChildMoved(DataSnapshot dataSnapshot, String s) { }
                public void onCancelled(DatabaseError databaseError) { }
            };
            //add a child event listener. SO the reference (mGroupDatabaseRef) defines which part of
            //database to listen to, mchildEventlistener defines what to do
            mExpenseRecordsDatabaseReference.addChildEventListener(mChildEventListener);

        }
    }

    public void switchActivity(){
        mAdapterLeft.clear();
        mChildEventListener = null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        switchActivity();
    }
}
