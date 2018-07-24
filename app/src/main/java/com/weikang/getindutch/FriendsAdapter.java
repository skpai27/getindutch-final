package com.weikang.getindutch;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;



public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.ViewHolder> {


    private ArrayList<Users> mFriends = new ArrayList<>();
    //private ArrayList<String> mImages = new ArrayList<>();
    private Context mContext;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        //each dataItem is only just a string, we should create all the views needed here
        //declare all the views needed
        TextView mUsername;
        CircleImageView friendsProfilePic;
        RelativeLayout mParentLayout;

        public ViewHolder(View itemView){
            super(itemView);
            mUsername = itemView.findViewById(R.id.userName);
            friendsProfilePic = itemView.findViewById(R.id.profilePic);
            mParentLayout = itemView.findViewById(R.id.items_friends);
        }
    }

    //provide a suitable constructor based on type of dataset
    //constructor will get the data we need
    public FriendsAdapter(ArrayList<Users> friends, Context context){
        //mImageNames = imageNames;
        //mImages = images;
        mFriends = friends;
        mContext = context;
    }

    //create new views (invoked by layout manager)
    @Override
    public FriendsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        //create new view //potential bug
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.items_friends,
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

