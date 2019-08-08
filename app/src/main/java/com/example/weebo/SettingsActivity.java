package com.example.weebo;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private Button updateAccountSettings;
    private EditText userName, userStatus;
    private CircleImageView userProfileImage;
    private ProgressDialog loadingBar;

    private Toolbar mToolbar;

    private String currentUserID;

    // defining firebase services
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;
    private StorageReference userProfileImageRef; // in userProfileImageRef folder, we will store the profile image only

    private StorageReference storageReference;
    private static final int IMAGE_REQUEST = 1;
    private Uri imageUri;
    private StorageTask uploadTask;

    private static final int galleryPick = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // initializing firebase services
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();
        userProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");
        storageReference = FirebaseStorage.getInstance().getReference().child("Profile Images");


        // defining variables with layouts
        InitializeFields();

        //userName.setVisibility(View.INVISIBLE);

        updateAccountSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateSettings();
            }
        });

        retrieveUserInfo();

        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*"); // type of file that they should choose
                startActivityForResult(galleryIntent, galleryPick);
            };
        });
    }


    private void InitializeFields() {
        updateAccountSettings = (Button) findViewById(R.id.settings_update_button);
        userName = (EditText) findViewById(R.id.set_user_name);
        userStatus = (EditText) findViewById(R.id.set_profile_status);
        userProfileImage = (CircleImageView) findViewById(R.id.set_profile_image);
        loadingBar = new ProgressDialog(this);

        mToolbar = (Toolbar) findViewById(R.id.settings_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("Account Settings");

    }

    // this method will get the result of the image that is selelcted
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == galleryPick && resultCode == RESULT_OK && data != null)
        {
            Uri imageUri = data.getData();

            // start picker to get image for cropping and then use the image in cropping activity
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK)
            {
                loadingBar.setTitle("Set Profile Image");
                loadingBar.setMessage("Please wait, your profile image is updating");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                Uri resultUri = result.getUri();

                // storing the image into firebase storage
                StorageReference filePath = userProfileImageRef.child(currentUserID + ".jpg");

                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful())
                        {
                            Toast.makeText(SettingsActivity.this, "Profile Image Uploaded Successfully", Toast.LENGTH_SHORT).show();

                            final String downloadedUrl = task.getResult().getMetadata().getReference().getDownloadUrl().toString(); // getting the link of the profile image from firebase storage
                            // reference to our app is rootRef
                            rootRef.child("Users").child(currentUserID).child("image")
                                    .setValue(downloadedUrl)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                           if(task.isSuccessful())
                                           {
                                               Toast.makeText(SettingsActivity.this, "Image Stored into DB successfully", Toast.LENGTH_SHORT).show();
                                                loadingBar.dismiss();
                                           }
                                           else
                                           {
                                               String msg = task.getException().toString();
                                               Toast.makeText(SettingsActivity.this, "Error: " +msg, Toast.LENGTH_SHORT).show();
                                                loadingBar.dismiss();
                                           }
                                        }
                                    });
                        }
                        else
                        {
                            String msg = task.getException().toString();
                            Toast.makeText(SettingsActivity.this, "Error: " + msg, Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        }
                    }
                });
            }
            else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE)
            {
                Exception error = result.getError();
            }
        }

    }

    private void updateSettings() {
        String setUserName = userName.getText().toString();
        String setStatus = userStatus.getText().toString();

        // if the fields are empty
        if(TextUtils.isEmpty(setUserName) )
        {
            userName.requestFocus();
            userName.setError("This Field is required to sign into your account!");
            return;
        }
        // if all fields are okay then
        else
        {
            // storing data into firebase using hash map
            HashMap<String, Object> profileMap = new HashMap<>();
                profileMap.put("uid", currentUserID);
                profileMap.put("name", setUserName);
                profileMap.put("status", setStatus);
            rootRef.child("Users").child(currentUserID).updateChildren(profileMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                sendUserToMainActivity();
                                Toast.makeText(SettingsActivity.this, "Profile Updated Successfully", Toast.LENGTH_LONG).show();
                            }
                            else
                            {
                                // getting the error type
                                String msg = task.getException().toString();
                                Toast.makeText(SettingsActivity.this, "Error: "+ msg, Toast.LENGTH_LONG).show();
                            }
                        }
                    });

        }


        }

    private void retrieveUserInfo() {
        rootRef.child("Users").child(currentUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        // if the user has created his profile then
                        if((dataSnapshot.exists()) && (dataSnapshot.hasChild("name") && (dataSnapshot.hasChild(("image")))))
                        {
                            String retrieveUserName = dataSnapshot.child("name").getValue().toString();
                            String retrieveUserStatus = dataSnapshot.child("status").getValue().toString();
                            String retrieveProfileImage = dataSnapshot.child("image").getValue().toString();

                            userName.setText(retrieveUserName);
                            userStatus.setText(retrieveUserStatus);
                            // using picasso library we will display the profile image
                            //Picasso.get().load(retrieveProfileImage).placeholder(R.drawable.profile_image).into(userProfileImage);
                            Glide.with(getApplicationContext())
                                    .load(retrieveProfileImage)
                                    .into(userProfileImage);

                        }

                        // user jodi dp na diye thake taile
                        else if((dataSnapshot.exists()) && (dataSnapshot.hasChild("name")))
                        {
                            String retrieveUserName = dataSnapshot.child("name").getValue().toString();
                            String retrieveUserStatus = dataSnapshot.child("status").getValue().toString();

                            userName.setText(retrieveUserName);
                            userStatus.setText(retrieveUserStatus);
                        }
                        else
                        {
                            //userName.setVisibility(View.VISIBLE);
                            Toast.makeText(SettingsActivity.this, "Please set and update your profile information...", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }



    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(SettingsActivity.this, MainActivity.class);
        // user cant' go back to main activity once he reaches to settings activity for not choosing his name
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }


}
