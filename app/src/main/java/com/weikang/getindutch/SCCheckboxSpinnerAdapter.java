package com.weikang.getindutch;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SCCheckboxSpinnerAdapter extends ArrayAdapter<SCCheckboxSpinner> {
    private Context mContext;
    private ArrayList<SCCheckboxSpinner> listState;
    private SCCheckboxSpinnerAdapter myAdapter;
    private boolean[] checkedItemsList;
    private float[] addTo;
    private float itemCost;
    private boolean isFromView = false;
    private String groupname;
    private int numberSharing;
    private String currentUser;

    public SCCheckboxSpinnerAdapter(Context context, int resource, List<SCCheckboxSpinner> objects, int objectSize, float itemCost, String groupname) {
        super(context, resource, objects);
        this.mContext = context;
        this.listState = (ArrayList<SCCheckboxSpinner>) objects;
        this.myAdapter = this;
        this.checkedItemsList = new boolean[objectSize];
        this.groupname = groupname;
        this.numberSharing = 1;
        this.addTo = new float[objectSize];
        this.itemCost = itemCost;
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getUid()).child("name");
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                currentUser = dataSnapshot.getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        //Log.d("debug", objects.toString());
        //Toast.makeText(context, " object size: " + objectSize, Toast.LENGTH_LONG).show();
    }

    @Override
    public View getDropDownView(int position, View convertView,
                                ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    public View getCustomView(final int position, View convertView,
                              ViewGroup parent) {

        final ViewHolder holder;
        if (convertView == null) {
            LayoutInflater layoutInflator = LayoutInflater.from(mContext);
            convertView = layoutInflator.inflate(R.layout.spinner_item_checkbox, null);
            holder = new ViewHolder();
            holder.mTextView = (TextView) convertView
                    .findViewById(R.id.text);
            holder.mCheckBox = (CheckBox) convertView
                    .findViewById(R.id.checkbox);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.mTextView.setText(listState.get(position).getMemberName());

        isFromView = true;
        holder.mCheckBox.setChecked(checkedItemsList[position]);
        isFromView = false;

        holder.mCheckBox.setTag(position);
        //Log.d("debug", Arrays.toString(checkedItemsList));
        holder.mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                int getPosition = (Integer) buttonView.getTag();
                if (!isFromView) {
                    checkedItemsList[position] = isChecked;
                }


            }
        });

        return convertView;
    }

    private class ViewHolder {
        private TextView mTextView;
        private CheckBox mCheckBox;
    }

    public float[] submit() {
        numberSharing = 0;
        for (int i = 0; i < checkedItemsList.length; i++) {
            if (checkedItemsList[i]) {
                numberSharing++;
            }
        }
        for (int i = 0; i < checkedItemsList.length; i++) {
            if (checkedItemsList[i]) {
                if (listState.get(i).getMemberName().equals(currentUser)) {
                    if (numberSharing != 1) {
                        addTo[i] = itemCost/ (float) numberSharing;
                    }
                    //Log.d("debug", "A = " + addTo[i]);
                } else {
                    addTo[i] = - itemCost / (float) numberSharing;
                    //Log.d("debug", "B = " + addTo[i]);
                }
            } else {
                if (listState.get(i).getMemberName().equals(currentUser)) {
                    addTo[i] = itemCost;
                    //Log.d("debug", "C = " + addTo[i]);
                } else {
                    addTo[i] = (float) 0.0;
                    //Log.d("debug", "D = " + addTo[i]);
                }

            }
        }

        return addTo;
    }
}

