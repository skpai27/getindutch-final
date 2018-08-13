package com.weikang.getindutch;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class PGEditProfilePage extends AppCompatActivity {

    //Attributes
    private final int CHANGE_PROFILE_PIC = 0;
    private Uri photoUri = null;

    //Widgets
    private TextView changePhoto;
    private TextView doneBtn;
    private CircleImageView myProfilePic;
    private ProgressBar myProgBar;

    //Firebase
    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDb;
    private DatabaseReference mUserRef;
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mUserProfilePhotoRef;
    private String originalPhotoUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pg_activity_edit_profile_page);

        //initialising widgets
        changePhoto = findViewById(R.id.changePhoto);
        doneBtn = findViewById(R.id.doneBtn);
        myProfilePic = findViewById(R.id.myProfilePic);
        myProgBar = findViewById(R.id.progressBar);

        //Set prog bar visibility
        myProgBar.setVisibility(ProgressBar.VISIBLE);

        //Firebase
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDb = FirebaseDatabase.getInstance();
        mUserRef = mFirebaseDb.getReference().child("users").child(mAuth.getUid());
        mFirebaseStorage = FirebaseStorage.getInstance();
        mUserProfilePhotoRef = mFirebaseStorage.getReference().child(mAuth.getUid()).child("profilePic");

        //Original photo url
        mUserRef.child("photoUrl").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                originalPhotoUrl = dataSnapshot.getValue().toString();
                Glide.with(PGEditProfilePage.this).load(originalPhotoUrl).listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        myProgBar.setVisibility(ProgressBar.INVISIBLE);
                        myProfilePic.setVisibility(View.VISIBLE);
                        return false;
                    }
                }).into(myProfilePic);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        changePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeProfilePic();
            }
        });

        //onClick listener for done btn: update database and return to main page
        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateDb();
                Intent intent = new Intent(PGEditProfilePage.this,MainPage.class);
                startActivity(intent);
            }
        });
    }

    public void changeProfilePic(){
        startActivityForResult(getPickImageIntent(PGEditProfilePage.this),CHANGE_PROFILE_PIC);
    }

    public void updateDb(){
        mUserProfilePhotoRef.putFile(photoUri);
    }

    //Method found online to choose intent for camera or gallery to upload profile photo
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }
    public static Intent getPickImageIntent(Context context) {
        Intent chooserIntent = null;

        List<Intent> intentList = new ArrayList<>();

        Intent pickIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intentList = addIntentsToList(context, intentList, pickIntent);
        intentList = addIntentsToList(context, intentList, takePhotoIntent);

        if (intentList.size() > 0) {
            chooserIntent = Intent.createChooser(intentList.remove(intentList.size() - 1),
                    "Select Image");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentList.toArray(new Parcelable[]{}));
        }

        return chooserIntent;
    }
    private static List<Intent> addIntentsToList(Context context, List<Intent> list, Intent intent) {
        List<ResolveInfo> resInfo = context.getPackageManager().queryIntentActivities(intent, 0);
        for (ResolveInfo resolveInfo : resInfo) {
            String packageName = resolveInfo.activityInfo.packageName;
            Intent targetedIntent = new Intent(intent);
            targetedIntent.setPackage(packageName);
            list.add(targetedIntent);
        }
        return list;
    }
    private static File getTempFile(Context context) {
        File imageFile = new File(context.getExternalCacheDir(), "tempImage");
        imageFile.getParentFile().mkdirs();
        return imageFile;
    }

    //Process image
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CHANGE_PROFILE_PIC){
            if (resultCode == RESULT_OK){
                myProfilePic.setVisibility(View.INVISIBLE);
                myProgBar.setVisibility(ProgressBar.VISIBLE);
                Uri selectedImage = data.getData();
                myProfilePic.setImageURI(selectedImage);
                photoUri = selectedImage;
                myProgBar.setVisibility(ProgressBar.INVISIBLE);
                myProfilePic.setVisibility(View.VISIBLE);
            }
        }
    }
}
