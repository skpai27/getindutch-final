package com.weikang.getindutch;

import android.content.Context;
import android.graphics.Color;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;


public class MPFFriendsAdapter extends RecyclerView.Adapter<MPFFriendsAdapter.ViewHolder> {


    private ArrayList<MPFFriendsUsersClass> mFriends = new ArrayList<>();
    //private ArrayList<String> mImages = new ArrayList<>();
    private Context mContext;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        //each dataItem is only just a string, we should create all the views needed here
        //declare all the views needed
        TextView mUsername;
        TextView mUserBal;
        CircleImageView friendsProfilePic;
        RelativeLayout mParentLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            mUsername = itemView.findViewById(R.id.userName);
            mUserBal = itemView.findViewById(R.id.user_balance2);
            friendsProfilePic = itemView.findViewById(R.id.profilePic);
            mParentLayout = itemView.findViewById(R.id.items_friends);
        }
    }

    //provide a suitable constructor based on type of dataset
    //constructor will get the data we need
    public MPFFriendsAdapter(ArrayList<MPFFriendsUsersClass> friends, Context context) {
        //mImageNames = imageNames;
        //mImages = images;
        mFriends = friends;
        mContext = context;
    }

    //create new views (invoked by layout manager)
    @Override
    public MPFFriendsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //create new view //potential bug
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.mpf_items_friends,
                parent, false);
        ViewHolder vh = new ViewHolder(view);
        return vh;
    }

    //replace the contents of a view(invoked by layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        //get images
        Glide.with(mContext)
                .asBitmap()
                .load(mFriends.get(position).getPhotoUrl())
                .into(holder.friendsProfilePic);

        holder.mUsername.setText(mFriends.get(position).getName());

        setUserBal(holder, position);

        //sets what happens when u click the object
        holder.mParentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(mContext, mFriends.get(position).getName(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setUserBal(final ViewHolder holder, final int position) {
        String userUid = FirebaseAuth.getInstance().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(userUid).child("friends");
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot friends : dataSnapshot.getChildren()) {
                    String friendUid = mFriends.get(position).getUid();
                    String friendName = mFriends.get(position).getName();
                    Log.d("debug", friends.getKey());
                    if (friends.getKey().equals(friendUid)) {
                        float value = Float.parseFloat(friends.getValue().toString());
                        if (value == 0f) {
                            holder.mUserBal.setVisibility(View.INVISIBLE);
                        } else if (value > 0) {
                            holder.mUserBal.setVisibility(View.VISIBLE);
                            holder.mUserBal.setText(friendName + " owes you $" + Float.toString(value));
                            holder.mUserBal.setTextColor(Color.GREEN);
                        } else {
                            holder.mUserBal.setVisibility(View.VISIBLE);
                            holder.mUserBal.setText("You owe " + friendName + " $" + Float.toString(-value));
                            holder.mUserBal.setTextColor(mContext.getResources().getColor(R.color.green));
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

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
    public int getItemCount() {
        return mFriends.size();
    }
}

