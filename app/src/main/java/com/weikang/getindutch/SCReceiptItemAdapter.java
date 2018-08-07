package com.weikang.getindutch;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;


public class SCReceiptItemAdapter extends RecyclerView.Adapter<SCReceiptItemAdapter.ViewHolder> {


    private ArrayList<SCReceiptItem> mTexts = new ArrayList<>();
    private Context mContext;
    private String targetGroup;
    private ArrayList<SCCheckboxSpinner> mMembers = new ArrayList<>();
    private SCCheckboxSpinnerAdapter mSpinnerAdapter;
    private ArrayList<SCCheckboxSpinnerAdapter> mAdapters = new ArrayList<>();
    private int mMembersSize;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        //each dataItem is only just a string, we should create all the views needed here
        //declare all the views needed
        TextView mItemDescription;
        TextView mItemPrice;
        Spinner mFriendsSpinner;

        public ViewHolder(View itemView) {
            super(itemView);
            mItemDescription = itemView.findViewById(R.id.itemDescription);
            mItemPrice = itemView.findViewById(R.id.Price);
            mFriendsSpinner = itemView.findViewById(R.id.membersSpinner);

        }
    }

    //provide a suitable constructor based on type of dataset
    //constructor will get the data we need
    public SCReceiptItemAdapter(ArrayList<SCReceiptItem> texts, final Context context) {
        mTexts = texts;
        mContext = context;
        targetGroup = texts.get(0).getTargetGroup();
        final FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseDatabase.getReference().child("groups").child(targetGroup).child("members").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (final DataSnapshot ds : dataSnapshot.getChildren()) {
                    mFirebaseDatabase.getReference().child("users").child(ds.getKey()).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot2) {
                            Log.d("debug", ds.getKey());
                            mMembers.add(new SCCheckboxSpinner(dataSnapshot2.getValue().toString(), ds.getKey()));
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //create new views (invoked by layout manager)
    @Override
    public SCReceiptItemAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //create new view //potential bug
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sc_receipt_items,
                parent, false);
        ViewHolder vh = new ViewHolder(view);
        return vh;
    }

    //replace the contents of a view(invoked by layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        //get images
        //Toast.makeText(mContext, "size: " + mMembers.size(), Toast.LENGTH_SHORT).show();
        readData(new MyCallback() {
            @Override
            public void onCallback(int value) {
                //Log.d("debug", Integer.toString(mMembersSize));
                holder.mItemDescription.setText(mTexts.get(position).getItemDescriptions());
                holder.mItemPrice.setText("$" + String.format("%.2f", mTexts.get(position).getItemPrice()));
                mSpinnerAdapter = new SCCheckboxSpinnerAdapter(mContext, 0, mMembers, mMembersSize, mTexts.get(position).getItemPrice(), targetGroup);
                mAdapters.add(mSpinnerAdapter);
                mSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                holder.mFriendsSpinner.setAdapter(mSpinnerAdapter);
                holder.mFriendsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
            }
        });
    }

    public void clear() {
        final int size = mTexts.size();
        if (size > 0) {
            for (int i = 0; i < size; i++) {
                mTexts.remove(0);
            }

            notifyItemRangeRemoved(0, size);
        }
    }

    //return size of your dataset (invoked by layout manager)
    @Override
    public int getItemCount() {
        return mTexts.size();
    }

    public interface MyCallback {
        void onCallback(int value);
    }

    public void readData(final MyCallback myCallback) {
        final FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseDatabase.getReference().child("groups").child(targetGroup).child("members").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mMembersSize = 0;
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    mMembersSize++;
                }
                myCallback.onCallback(mMembersSize);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public void submit(String description) {
        final float[] addTo = new float[mMembersSize];
        //Log.d("debug", "member size = " + mMembersSize);
        float expense = 0;
        float amountOwedPerPax = 0;

        //iterate through items to get total amount paid by current user
        for (SCReceiptItem items : mTexts){
            expense += items.getItemPrice();
        }
        expense = Math.round(expense * 100) / (float) 100.0;
        for (SCCheckboxSpinnerAdapter adapters : mAdapters) {
            float[] adapterAddTo = adapters.submit();


            for (int i = 0; i < addTo.length; i++) {
                Log.d("debug", i + " " + adapterAddTo[i]);
                addTo[i] += adapterAddTo[i];
            }
        }

        //Create hashmap for database
        HashMap<String,String> peopleSharingExpense = new HashMap<>();
        FirebaseDatabase mFirebaseDb = FirebaseDatabase.getInstance();
        final DatabaseReference membersRef = mFirebaseDb.getReference().child("groups").child(targetGroup).child("members");
        DatabaseReference expenseRef = mFirebaseDb.getReference().child("expenseRecords");
        for (int i = 0; i < addTo.length; i++) {
            if(!mMembers.get(i).getMemberUid().equals(FirebaseAuth.getInstance().getUid())){
                //if not current user, add to hashmap
                peopleSharingExpense.put(mMembers.get(i).getMemberUid(),mMembers.get(i).getMemberName());
            } else {
                //Derived from the math of MPFSummaryPageExpensesCLass
                amountOwedPerPax = expense - Math.round(addTo[i] * 100) / (float) 100.0;
            }
            final int finalI = i;
            membersRef.child(mMembers.get(i).getMemberUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot2) {
                    float value = Float.parseFloat(dataSnapshot2.getValue().toString()) + Math.round(addTo[finalI] * 100) / (float) 100.0;
                    Log.d("debug", mMembers.get(finalI).getMemberName() + " adds " + value);
                    membersRef.child(mMembers.get(finalI).getMemberUid()).setValue(value);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        String expenseIconUrl = "https://www.shareicon.net/download/2016/08/18/809809_cab_512x512.png";
        MPFSummaryPageExpensesClass expenseRecord = new MPFSummaryPageExpensesClass(FirebaseAuth.getInstance().getUid(),
                targetGroup, description , expense, amountOwedPerPax,
                peopleSharingExpense, expenseIconUrl );
        expenseRef.push().setValue(expenseRecord);
    }
}
