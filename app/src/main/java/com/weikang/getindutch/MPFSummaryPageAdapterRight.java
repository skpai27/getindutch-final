package com.weikang.getindutch;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class MPFSummaryPageAdapterRight extends RecyclerView.Adapter<MPFSummaryPageAdapterRight.ViewHolder> {


    private ArrayList<MPFSummaryPageExpensesClass> mExpenseRecords = new ArrayList<>();
    //private ArrayList<String> mImages = new ArrayList<>();
    private Context mContext;

    //Firebase uid details
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser mUser = mAuth.getCurrentUser();
    String mUserId;

    //Firebase Database variables
    FirebaseDatabase mFirebaseDatabase;
    DatabaseReference mUserDatabaseReference;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        //each dataItem is only just a string, we should create all the views needed here
        //declare all the views needed
        TextView mDescription;
        TextView mGroupNameorPayeeName;
        TextView mLineOne;
        TextView mLineTwo;
        TextView mSummaryAmount;
        CircleImageView mExpenseIcon;
        RelativeLayout mParentLayout;

        public ViewHolder(View itemView){
            super(itemView);
            mDescription = itemView.findViewById(R.id.description);
            mGroupNameorPayeeName = itemView.findViewById(R.id.group_name_or_indiv_name);
            mLineOne = itemView.findViewById(R.id.sub_text_first_line);
            mLineTwo = itemView.findViewById(R.id.sub_text_second_line);
            mSummaryAmount = itemView.findViewById(R.id.summary_amount);
            mExpenseIcon = itemView.findViewById(R.id.expense_icon);
            mParentLayout = itemView.findViewById(R.id.parent_layout);
        }
    }

    //provide a suitable constructor based on type of dataset
    //constructor will get the data we need
    public MPFSummaryPageAdapterRight(ArrayList<MPFSummaryPageExpensesClass> expenseRecords, Context context){
        //mImageNames = imageNames;
        //mImages = images;
        mExpenseRecords = expenseRecords;
        mContext = context;
        mUserId = mAuth.getUid();
    }

    //create new views (invoked by layout manager)
    @Override
    public MPFSummaryPageAdapterRight.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        //create new view //potential bug
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.mpf_summary_page_items,
                parent, false);
        ViewHolder vh = new ViewHolder(view);
        return vh;
    }

    //replace the contents of a view(invoked by layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position){
        final MPFSummaryPageExpensesClass expenseRecord = mExpenseRecords.get(position);
        mUserDatabaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(expenseRecord.getPayerUid());
        mUserDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                holder.mLineOne.setText(name + " paid " + expenseRecord.getAmountPaid());
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
        //get images
        Glide.with(mContext)
                .asBitmap()
                .load(expenseRecord.getExpenseIconUrl())
                .into(holder.mExpenseIcon);

        holder.mDescription.setText(expenseRecord.getDescription());
        holder.mGroupNameorPayeeName.setText(expenseRecord.getGroupNameOrPayeeName());
        holder.mLineTwo.setText("You owe "+ (expenseRecord.getAmountOwedPerPax()));
        holder.mSummaryAmount.setText("-" + (expenseRecord.getAmountOwedPerPax()));

        if(position %2 == 1)
        {
            holder.itemView.setBackgroundColor(Color.parseColor("#E44600"));
        }
        else
        {
            holder.itemView.setBackgroundColor(Color.parseColor("#FF7102"));
        }

        //sets what happens when u click the object
        /*holder.mParentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                Toast.makeText(mContext, mGroups.get(position).getName(), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(mContext,GroupsPage.class);
                Bundle b = new Bundle();
                b.putString("groupName",mGroups.get(position).getName());
                intent.putExtras(b);
                mContext.startActivity(intent);
            }
        });*/
    }

    public void clear() {
        final int size = mExpenseRecords.size();
        if (size > 0) {
            for (int i = 0; i < size; i++) {
                mExpenseRecords.remove(0);
            }

            notifyItemRangeRemoved(0, size);
        }
    }

    //return size of your dataset (invoked by layout manager)
    @Override
    public int getItemCount(){
        return mExpenseRecords.size();
    }
}