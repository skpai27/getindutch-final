package com.weikang.getindutch;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupMembersAdapter extends RecyclerView.Adapter<GroupMembersAdapter.ViewHolder> {

    private FirebaseDatabase mFirebaseDatabase;

    private ArrayList<MPFFriendsUsersClass> mMembers = new ArrayList<>();
    private String mGroupName;
    //private ArrayList<String> mImages = new ArrayList<>();
    private Context mContext;
    private Dialog mPayeeText;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        //each dataItem is only just a string, we should create all the views needed here
        //declare all the views needed
        TextView mUsername;
        TextView membersDebt;
        CircleImageView friendsProfilePic;
        RelativeLayout mParentLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            mUsername = itemView.findViewById(R.id.member_name);
            friendsProfilePic = itemView.findViewById(R.id.member_profilePic);
            membersDebt = itemView.findViewById(R.id.member_balance);
            mParentLayout = itemView.findViewById(R.id.group_layout);
        }
    }

    //provide a suitable constructor based on type of dataset
    //constructor will get the data we need
    public GroupMembersAdapter(ArrayList<MPFFriendsUsersClass> members, String groupName, Context context) {
        //mImageNames = imageNames;
        //mImages = images;
        mGroupName = groupName;
        mMembers = members;
        mContext = context;
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mPayeeText = new Dialog(mContext);
    }

    //create new views (invoked by layout manager)
    @Override
    public GroupMembersAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //create new view //potential bug
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.mpf_items_groupmembers,
                parent, false);
        GroupMembersAdapter.ViewHolder vh = new GroupMembersAdapter.ViewHolder(view);
        return vh;
    }


    //replace the contents of a view(invoked by layout manager)
    @Override
    public void onBindViewHolder(final GroupMembersAdapter.ViewHolder holder, final int position) {
        //get images
        Glide.with(mContext)
                .asBitmap()
                .load(mMembers.get(position).getPhotoUrl())
                .into(holder.friendsProfilePic);

        holder.mUsername.setText(mMembers.get(position).getName());

        //Finding the balance
        mFirebaseDatabase.getReference().child("groups").child(mGroupName).child("members").child(mMembers.get(position).getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                float value = Float.parseFloat(dataSnapshot.getValue().toString());
                value = Math.round(value * 100) / (float) 100.0;
                String text;
                if (value >= 0) {
                    text = mMembers.get(position).getName() + " is owed $" + value;
                } else {
                    final HashMap<String, Float> payee = mMembers.get(position).getPayee();
                    if (payee.size() == 1) {
                        String payeeName = "";
                        for (String name : payee.keySet()) {
                            payeeName = name;
                        }
                        if (payeeName.equals(FirebaseAuth.getInstance().getCurrentUser().getDisplayName())) {
                            payeeName = "you";
                        }
                        text = mMembers.get(position).getName() + " owes " + payeeName + " $" + (-value);
                    } else {
                        text = "Click here to view who " + mMembers.get(position).getName() + " owes";
                        holder.membersDebt.setClickable(true);
                        holder.membersDebt.setTextSize(14);
                        holder.membersDebt.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);
                        holder.membersDebt.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //Toast.makeText(mContext,"Clickable", Toast.LENGTH_SHORT).show();
                                showPayeePopup(mMembers.get(position).getName(),payee);
                            }
                        });
                    }
                }
                holder.membersDebt.setText(text);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //sets what happens when u click the object
        holder.mParentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(mContext, mMembers.get(position).getName(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void clear() {
        final int size = mMembers.size();
        if (size > 0) {
            for (int i = 0; i < size; i++) {
                mMembers.remove(0);
            }
            notifyItemRangeRemoved(0, size);
        }
    }

    //return size of your dataset (invoked by layout manager)
    @Override
    public int getItemCount() {
        return mMembers.size();
    }

    public void showPayeePopup(String name, HashMap<String,Float> payeeList){
        mPayeeText.setContentView(R.layout.mpf_show_payee_dialog);
        TextView payeeDisplay = mPayeeText.findViewById(R.id.payeeText);
        String payeeText = "";
        for (String key : payeeList.keySet()){
            Log.d("debug", " Payee list = " + payeeList.keySet().toString());
            payeeText = payeeText.concat(name + " owes " + key + " $" + (- Math.round(payeeList.get(key) * 100) / (float) 100.0) + "\n");
        }
        payeeDisplay.setText(payeeText);
        mPayeeText.show();
    }

}
