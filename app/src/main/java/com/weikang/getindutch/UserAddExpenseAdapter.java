package com.weikang.getindutch;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAddExpenseAdapter extends RecyclerView.Adapter<UserAddExpenseAdapter.MyHolder> {
    Context c;
    ArrayList<UserAddExpense> users = new ArrayList<>();
    ArrayList<UserAddExpense> checkedUsers = new ArrayList<>();

    public UserAddExpenseAdapter(Context c, ArrayList<UserAddExpense> users){
        this.c = c;
        this.users = users;
    }

    //initialise viewholder
    @Override
    public MyHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.items_groupmembers_splitbill, null);
        MyHolder holder = new MyHolder(v);
        return holder;
    }

    //data bound to views
    @Override
    public void onBindViewHolder(MyHolder holder, int position){
        final UserAddExpense user = users.get(position);

        //get images
        Glide.with(c)
                .asBitmap()
                .load(user.getPhotoUrl())
                .into(holder.mUserImage);

        holder.mUserName.setText(user.getName());
        holder.mCheckBox.setChecked(true);

        holder.mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    checkedUsers.add(user);// update your model (or other business logic) based on isChecked
                }else{
                    checkedUsers.remove(user);
                }
            }
        });

        /*holder.setItemClickListener(new MyHolder.ItemClickListener(){
            @Override
            public void onItemClick(View v, int pos){
                CheckBox myCheckBox = (CheckBox) v;
                UserAddExpense currentUser = users.get(pos);

                if(myCheckBox.isChecked()){
                    currentUser.setSelected(true);
                    checkedUsers.add(currentUser);
                }
                else if(!myCheckBox.isChecked()){
                    currentUser.setSelected(false);
                    checkedUsers.remove(currentUser);
                }
            }
        });*/
    }
    @Override
    public int getItemCount(){
        return users.size();
    }

    static class MyHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        CircleImageView mUserImage;
        TextView mUserName;
        CheckBox mCheckBox;

        ItemClickListener itemClickListener;

        public MyHolder(View itemView){
            super(itemView);
            mUserImage = itemView.findViewById(R.id.member_profilePic);
            mUserName = itemView.findViewById(R.id.member_name);
            mCheckBox = itemView.findViewById(R.id.checkbox);
        }

        public void setItemClickListener(ItemClickListener ic){
            this.itemClickListener = ic;
        }
        @Override
        public void onClick(View v){
            this.itemClickListener.onItemClick(v, getLayoutPosition());
        }
        interface ItemClickListener{
            void onItemClick(View v, int pos);
        }
    }

    public void clear() {
        final int size = users.size();
        if (size > 0) {
            for (int i = 0; i < size; i++) {
                users.remove(0);
            }

            notifyItemRangeRemoved(0, size);
        }
    }

}
