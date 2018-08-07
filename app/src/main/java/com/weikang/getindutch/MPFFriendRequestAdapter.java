package com.weikang.getindutch;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;



public class MPFFriendRequestAdapter extends RecyclerView.Adapter<MPFFriendRequestAdapter.ViewHolder> {


    private ArrayList<MPFFriendsUsersClass> mFriends = new ArrayList<>();
    //private ArrayList<String> mImages = new ArrayList<>();
    private Context mContext;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        //each dataItem is only just a string, we should create all the views needed here
        //declare all the views needed
        TextView mUsername;
        CircleImageView friendsProfilePic;
        RelativeLayout mParentLayout;
        ImageView ignore;
        ImageView accept;

        public ViewHolder(View itemView){
            super(itemView);
            mUsername = itemView.findViewById(R.id.userName1);
            friendsProfilePic = itemView.findViewById(R.id.profilePic1);
            mParentLayout = itemView.findViewById(R.id.items_friend_requests);
            accept = itemView.findViewById(R.id.accept);
            ignore = itemView.findViewById(R.id.ignore);
        }
    }

    //provide a suitable constructor based on type of dataset
    //constructor will get the data we need
    public MPFFriendRequestAdapter(ArrayList<MPFFriendsUsersClass> friends, Context context){
        //mImageNames = imageNames;
        //mImages = images;
        mFriends = friends;
        mContext = context;
    }

    //create new views (invoked by layout manager)
    @Override
    public MPFFriendRequestAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        //create new view //potential bug
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.mpf_friend_request,
                parent, false);
        ViewHolder vh = new ViewHolder(view);
        return vh;
    }

    //replace the contents of a view(invoked by layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, final int position){
        //get images
        Glide.with(mContext)
                .asBitmap()
                .load(mFriends.get(position).getPhotoUrl())
                .into(holder.friendsProfilePic);

        holder.mUsername.setText(mFriends.get(position).getName());

        //sets what happens when u click the object
        holder.mParentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                Toast.makeText(mContext, mFriends.get(position).getName(), Toast.LENGTH_SHORT).show();
            }
        });

        holder.accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String friendUid = mFriends.get(position).getUid();
                String myUid = FirebaseAuth.getInstance().getUid();
                FirebaseDatabase.getInstance().getReference().child("users").child(friendUid).child("friends").child(myUid).setValue(0f);
                FirebaseDatabase.getInstance().getReference().child("users").child(myUid).child("friends").child(friendUid).setValue(0f);
                FirebaseDatabase.getInstance().getReference().child("friendReq").child(myUid).child("received").child(friendUid).removeValue();
                Toast.makeText(mContext, "You have accepted " + mFriends.get(position).getName() + " as your friend!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(mContext, MPFFriendsRequestPage.class);
                mContext.startActivity(intent);
            }
        });

        holder.ignore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String friendUid = mFriends.get(position).getUid();
                String myUid = FirebaseAuth.getInstance().getUid();
                FirebaseDatabase.getInstance().getReference().child("friendReq").child(myUid).child("received").child(friendUid).child("ignored").setValue(true);
                Toast.makeText(mContext, "You have ignored " + mFriends.get(position).getName() + "'s friend request.", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(mContext, MPFFriendsRequestPage.class);
                mContext.startActivity(intent);
            }
        });


    }

    public void clear() {
        final int size = mFriends.size();
        if (size > 0) {
            for (int i = 0; i < size; i++) {
                mFriends.remove(0);
            }

            notifyItemRangeRemoved(0, size);
        }
    }

    //return size of your dataset (invoked by layout manager)
    @Override
    public int getItemCount(){ return mFriends.size(); }
}

