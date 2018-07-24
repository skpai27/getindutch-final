package com.weikang.getindutch;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;



public class ReceiptItemAdapter extends RecyclerView.Adapter<ReceiptItemAdapter.ViewHolder> {


    private ArrayList<ReceiptItem> mTexts = new ArrayList<>();
    private Context mContext;
    private String targetGroup;
    private ArrayList<SCCheckboxSpinner> mMembers;
    private SCCheckboxSpinnerAdapter mSpinnerAdapter;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        //each dataItem is only just a string, we should create all the views needed here
        //declare all the views needed
        TextView mItemDescription;
        TextView mItemPrice;
        Spinner mFriendsSpinner;

        public ViewHolder(View itemView){
            super(itemView);
            mItemDescription = itemView.findViewById(R.id.itemDescription);
            mItemPrice = itemView.findViewById(R.id.Price);
            mFriendsSpinner = itemView.findViewById(R.id.membersSpinner);
        }
    }

    //provide a suitable constructor based on type of dataset
    //constructor will get the data we need
    public ReceiptItemAdapter(ArrayList<ReceiptItem> texts, Context context){
        mTexts = texts;
        mContext = context;
        targetGroup = texts.get(0).getTargetGroup();
        mMembers = new ArrayList<>();
        final FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseDatabase.getReference().child("groups").child(targetGroup).child("members").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()){
                    mFirebaseDatabase.getReference().child("users").child(ds.getKey()).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot2) {
                            mMembers.add(new SCCheckboxSpinner(dataSnapshot2.getValue().toString()));
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
    public ReceiptItemAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        //create new view //potential bug
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.receipt_items,
                parent, false);
        ViewHolder vh = new ViewHolder(view);
        return vh;
    }

    //replace the contents of a view(invoked by layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, final int position){
        //get images

        holder.mItemDescription.setText(mTexts.get(position).getItemDescriptions());
        holder.mItemPrice.setText("$"+String.format("%.2f",mTexts.get(position).getItemPrice()));
        mSpinnerAdapter = new SCCheckboxSpinnerAdapter(mContext,0,mMembers);
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
    public int getItemCount(){ return mTexts.size(); }
}

