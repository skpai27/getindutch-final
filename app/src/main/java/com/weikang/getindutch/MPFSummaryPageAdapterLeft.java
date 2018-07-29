package com.weikang.getindutch;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class MPFSummaryPageAdapterLeft extends RecyclerView.Adapter<MPFSummaryPageAdapterLeft.ViewHolder> {


    private ArrayList<MPFSummaryPageExpensesClass> mExpenseRecords = new ArrayList<>();
    //private ArrayList<String> mImages = new ArrayList<>();
    private Context mContext;

    //Firebase uid details
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser mUser = mAuth.getCurrentUser();
    String mUserId;

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
    public MPFSummaryPageAdapterLeft(ArrayList<MPFSummaryPageExpensesClass> expenseRecords, Context context){
        //mImageNames = imageNames;
        //mImages = images;
        mExpenseRecords = expenseRecords;
        mContext = context;
        mUserId = mAuth.getUid();
    }

    //create new views (invoked by layout manager)
    @Override
    public MPFSummaryPageAdapterLeft.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        //create new view //potential bug
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.summary_page_items,
                parent, false);
        ViewHolder vh = new ViewHolder(view);
        return vh;
    }

    //replace the contents of a view(invoked by layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, final int position){
        MPFSummaryPageExpensesClass expenseRecord = mExpenseRecords.get(position);
        //get images
        Glide.with(mContext)
                .asBitmap()
                .load(expenseRecord.getExpenseIconUrl())
                .into(holder.mExpenseIcon);

        holder.mDescription.setText(expenseRecord.getDescription());
        holder.mGroupNameorPayeeName.setText(expenseRecord.getGroupNameOrPayeeName());
        holder.mLineOne.setText("You paid "+ expenseRecord.getAmountPaid());
        holder.mLineTwo.setText("You are owed "+ (expenseRecord.getAmountPaid() - expenseRecord.getAmountOwedPerPax()));
        holder.mSummaryAmount.setText("+" + (expenseRecord.getAmountPaid() - expenseRecord.getAmountOwedPerPax()));

        if(position %2 == 1)
        {
            holder.itemView.setBackgroundColor(Color.parseColor("#ADFF2F"));
        }
        else
        {
            holder.itemView.setBackgroundColor(Color.parseColor("#9ACD32"));
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