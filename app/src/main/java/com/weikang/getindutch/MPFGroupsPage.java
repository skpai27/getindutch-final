package com.weikang.getindutch;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.googlecode.tesseract.android.ResultIterator;
import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

public class MPFGroupsPage extends Fragment {
    private static final String TAG = "AllPageFragment";
    private static final int CAMERA_PIC_REQUEST = 2;
    private static final int MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE = 3;

    private Dialog mDialogAddpopup;
    private Dialog mDialogCreateGroup;

    private Spinner mSortDropdown;
    private FloatingActionButton mAddButton;
    private RecyclerView mRecyclerView;

    //variables for recyclerview Adapter
    private ArrayList<MPFGroupsClass> mGroups = new ArrayList<>();
    private MPFGroupsPageAdapter mAdapter;

    //Firebase variables
    //Database
    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mGroupsDatabaseReference;
    private ChildEventListener mChildEventListener;
    private DatabaseReference mGroupDatabaseReference;
    private DatabaseReference mUsersDatabaseReference;

    //TessOCR
    private TessOCR mTessOCR;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.all_page,container,false);

        mAddButton = (FloatingActionButton) view.findViewById(R.id.addBtn);
        mSortDropdown = (Spinner) view.findViewById(R.id.dropDown);
        mDialogAddpopup = new Dialog(getActivity());
        mDialogCreateGroup = new Dialog(getActivity());

        //Initialise Adapter and recyclerview etc
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        //use getActivity() instead of (this) for context cos this is a fragment
        mAdapter = new MPFGroupsPageAdapter(mGroups, getActivity());
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        //initialise Firebase variables
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mGroupsDatabaseReference = mFirebaseDatabase.getReference().child("users").child(mAuth.getUid()).child("groups");
        //change the names in future to avoid confusion
        mGroupDatabaseReference = mFirebaseDatabase.getReference().child("groups");
        mUsersDatabaseReference = mFirebaseDatabase.getReference().child("users");

        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddPopup(v);
            }
        });

        //database child event listener
        startView();
        return view;
    }

    public void startView(){
        if (mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {
                //datasnapshot contains data at that location when listener is triggered
                //first adding group object into the arraylist, then use adpater.notifyiteminserted

                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    final String groupName = dataSnapshot.getKey();
                    mAdapter.clear();
                    mFirebaseDatabase.getReference().child("groups").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot1) {
                            for (DataSnapshot datas : dataSnapshot1.getChildren()){
                                if(datas.getKey().equals(groupName)) {
                                    MPFGroupsClass group = datas.getValue(MPFGroupsClass.class);
                                    mGroups.add(group);
                                    mAdapter.notifyItemInserted(mGroups.size() - 1);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }

                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                }

                public void onChildRemoved(DataSnapshot dataSnapshot) {
                }

                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                }

                public void onCancelled(DatabaseError databaseError) {
                }
            };
            //add a child event listener. SO the reference (mGroupDatabaseRef) defines which part of
            //database to listen to, mchildEventlistener defines what to do
            mGroupsDatabaseReference.addChildEventListener(mChildEventListener);
        }
    }

    public void switchActivity(){
        mAdapter.clear();
        mChildEventListener = null;
    }

    public void showAddPopup(View view){
        mDialogAddpopup.setContentView(R.layout.add_pop_up);
        TextView textclose = (TextView) mDialogAddpopup.findViewById(R.id.text_close);
        Button manualAdd = (Button) mDialogAddpopup.findViewById(R.id.manual_add);
        Button receiptScan = (Button) mDialogAddpopup.findViewById(R.id.scanner_receipt);
        Button createGroup = (Button) mDialogAddpopup.findViewById(R.id.create_group);
        mDialogAddpopup.show();
        mDialogAddpopup.setCancelable(true);
        textclose.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                mDialogAddpopup.dismiss();
            }
        });
        manualAdd.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                switchActivity();
                Intent intent = new Intent(getActivity(), AEAddExpensesMain.class);
                startActivity(intent);
            }
        });
        createGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCreateGroup(v);
            }
        });
        receiptScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startReceiptScan(v);
            }
        });
    }

    public void showCreateGroup(View view){
        TextView cancelBtn;
        TextView nextBtn;
        final EditText groupName;
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        mDialogCreateGroup.setContentView(R.layout.create_group_popup);
        cancelBtn = (TextView) mDialogCreateGroup.findViewById(R.id.cancelBtn);
        nextBtn = (TextView) mDialogCreateGroup.findViewById(R.id.nextBtn);
        groupName = (EditText) mDialogCreateGroup.findViewById(R.id.groupName);
        cancelBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                mDialogCreateGroup.dismiss();
            }
        });
        mDialogCreateGroup.show();
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String currGroupName = groupName.getText().toString();
                if (currGroupName.isEmpty()){
                    Toast.makeText(getActivity(),"Please enter a group name!", Toast.LENGTH_SHORT).show();
                } else {
                    final MPFGroupsClass newGroup = new MPFGroupsClass(currGroupName, new HashMap<String, Float>()); //not really sure why it has to be final
                    final DatabaseReference groupRef = mGroupDatabaseReference.child(currGroupName);
                    ValueEventListener eventListener = new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(!dataSnapshot.exists()) {
                                //create new user
                                mDialogCreateGroup.dismiss();
                                mDialogAddpopup.dismiss();
                                mUsersDatabaseReference.child(user.getUid()).child("groups").child(currGroupName).setValue(true);
                                newGroup.addMembers(user.getUid());
                                groupRef.setValue(newGroup);
                                Toast.makeText(getActivity(),currGroupName + " has been successfully created!", Toast.LENGTH_SHORT).show();
                                mChildEventListener = null;

                            } else {
                                Toast.makeText(getActivity(), "Name of group has been taken. Please try again", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {}
                    };
                    groupRef.addListenerForSingleValueEvent(eventListener);
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        switchActivity();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_PIC_REQUEST && isStoragePermissionGranted()) {
            if (resultCode == RESULT_OK) {
                Uri targetUri = data.getData();
                Bitmap bitmap;
                try {
                    assert targetUri != null;
                    bitmap = BitmapFactory.decodeStream(getContext().getContentResolver().openInputStream(targetUri));
                    bitmap = rotateImageIfRequired(bitmap,getContext(),targetUri);
                    AssetManager assetManager = getContext().getAssets();
                    mTessOCR = new TessOCR(assetManager, "eng");
                    mDialogAddpopup.dismiss();
                    // Here, thisActivity is the current activity
                    doOCR(bitmap);
                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    Toast.makeText(getContext(), "Unable to open image", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(getContext(), "Scan cancelled", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void doOCR (final Bitmap bitmap) {
        new Thread(new Runnable() {
            public void run() {
                final String srcText = mTessOCR.getOCRResult(bitmap);
                if (srcText != null && !srcText.equals("")) {
                    Bundle bundle = new Bundle();
                    ArrayList<ReceiptItem> lines = processReceipt(mTessOCR);
                    bundle.putParcelableArrayList("srcText",lines);
                    /*bundle.putString("srcText",srcText);*/
                    Intent intent = new Intent(getContext(), ScannedTextPage.class);
                    intent.putExtras(bundle);
                    startActivity(intent);
                    Log.d(TAG, "bitch " + lines.toString() + "?");
                }
                mTessOCR.onDestroy();
            }
        }).start();
    }

    private ArrayList<ReceiptItem> processReceipt(TessOCR mTessOCR) {
        TessBaseAPI tessBaseAPI = mTessOCR.getmTess();
        ResultIterator iterator = tessBaseAPI.getResultIterator();
        ArrayList<ReceiptItem> processedArray = new ArrayList<>();
        boolean isItem = false;
        while (iterator.next(TessBaseAPI.PageIteratorLevel.RIL_TEXTLINE)){

            String currentLine = iterator.getUTF8Text(TessBaseAPI.PageIteratorLevel.RIL_TEXTLINE);
            Log.d(TAG,"|" + currentLine + "|");
            if ((currentLine.length() > 7 && currentLine.substring(0,8).equals("SUBTOTAL")) || (currentLine.length() > 3 && currentLine.substring(0,4).equals("LINK"))){
                isItem = false;
            }
            if (isItem && currentLine.charAt(1) != ('X')){
                String itemDescription = "";
                Float itemPrice = 0f;
                String[] stringArray = currentLine.split("\\s+");
                for (String word : stringArray){
                    if (word.contains(".")) {
                        try {
                            itemPrice = Float.valueOf(word);
                        } catch (NumberFormatException e) {
                            itemDescription = itemDescription.concat(word + " ");
                        }
                    } else {
                        itemDescription = itemDescription.concat(word + " ");
                    }
                    Log.d(TAG, "?" + word + "?");
                }
                ReceiptItem currentItem = new ReceiptItem(itemDescription,itemPrice);
                processedArray.add(currentItem);
            }
            if (currentLine.equals("SGD\n")){
                isItem = true;
            }
        }
        return processedArray;
    }

    public static Bitmap rotateImageIfRequired(Bitmap img, Context context, Uri selectedImage) throws IOException {

        if (selectedImage.getScheme().equals("content")) {
            String[] projection = { MediaStore.Images.ImageColumns.ORIENTATION };
            Cursor c = context.getContentResolver().query(selectedImage, projection, null, null, null);
            if (c.moveToFirst()) {
                final int rotation = c.getInt(0);
                c.close();
                return rotateImage(img, rotation);

            }
            return img;
        } else {
            ExifInterface ei = new ExifInterface(selectedImage.getPath());
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    return rotateImage(img, 90);
                case ExifInterface.ORIENTATION_ROTATE_180:
                    return rotateImage(img, 180);
                case ExifInterface.ORIENTATION_ROTATE_270:
                    return rotateImage(img, 270);
                default:
                    return img;
            }
        }
    }

    private static Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        return Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
    }

    public void startReceiptScan(View view){
        Intent cameraIntent = new Intent(Intent.ACTION_PICK);
        File pictureDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        Uri data = Uri.parse(pictureDirectory.getPath());
        cameraIntent.setDataAndType(data, "image/*");
        startActivityForResult(cameraIntent, CAMERA_PIC_REQUEST);
    }

    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(getContext(),android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"Permission is granted");
                return true;
            } else {

                Log.v(TAG,"Permission is revoked");
                ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Permission is granted");
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
            Log.v(TAG,"Permission: "+permissions[0]+ "was "+grantResults[0]);
            //resume tasks needing this permission
        }
    }

    //TODO: onCreate method to save states between fragments, clear viewpager if logout.
}