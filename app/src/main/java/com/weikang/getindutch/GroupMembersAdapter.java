package com.weikang.getindutch;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupMembersAdapter extends RecyclerView.Adapter<GroupMembersAdapter.ViewHolder> {

    private FirebaseDatabase mFirebaseDatabase;

    private ArrayList<Users> mMembers = new ArrayList<>();
    private String mGroupName;
    //private ArrayList<String> mImages = new ArrayList<>();
    private Context mContext;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        //each dataItem is only just a string, we should create all the views needed here
        //declare all the views needed
        TextView mUsername;
        TextView membersDebt;
        CircleImageView friendsProfilePic;
        RelativeLayout mParentLayout;

        public ViewHolder(View itemView){
            super(itemView);
            mUsername = itemView.findViewById(R.id.member_name);
            friendsProfilePic = itemView.findViewById(R.id.member_profilePic);
            membersDebt = itemView.findViewById(R.id.member_balance);
            mParentLayout = itemView.findViewById(R.id.group_layout);
        }
    }

    //provide a suitable constructor based on type of dataset
    //constructor will get the data we need
    public GroupMembersAdapter(ArrayList<Users> members, String groupName, Context context){
        //mImageNames = imageNames;
        //mImages = images;
        mGroupName = groupName;
        mMembers = members;
        mContext = context;
        mFirebaseDatabase = FirebaseDatabase.getInstance();
    }

    //create new views (invoked by layout manager)
    @Override
    public GroupMembersAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        //create new view //potential bug
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.items_groupmembers,
                parent, false);
        GroupMembersAdapter.ViewHolder vh = new GroupMembersAdapter.ViewHolder(view);
        return vh;
    }


    //replace the contents of a view(invoked by layout manager)
    @Override
    public void onBindViewHolder(final GroupMembersAdapter.ViewHolder holder, final int position){
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
                holder.membersDebt.setText("owes you $" + dataSnapshot.getValue().toString().substring(1));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //sets what happens when u click the object
        holder.mParentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
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
    public int getItemCount(){ return mMembers.size(); }

}
