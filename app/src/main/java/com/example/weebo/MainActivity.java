package com.example.weebo;

import android.content.DialogInterface;
import android.content.Intent;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private Toolbar mToolbar; // for adding the toolbar
    private ViewPager myViewPager; // for displaying fragments of the tabs
    private TabLayout myTabLayout;
    private TabsAccessorAdapter myTabsAccessorAdapter; // accessing the class "TabAccessorAdapter" from MainActivity

    // defining firebase services
    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initializing firebase services
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser(); // getting the current user.
        rootRef = FirebaseDatabase.getInstance().getReference();

        // referencing variables with layout
        mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);

        // adding the toolbar
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Weebo"); // giving the title


        myViewPager = (ViewPager) findViewById(R.id.main_tabs_pager);
        myTabsAccessorAdapter = new TabsAccessorAdapter(getSupportFragmentManager());
        myViewPager.setAdapter(myTabsAccessorAdapter);

        //for accessing the tabs layout
        myTabLayout = (TabLayout) findViewById(R.id.main_tabs);
        myTabLayout.setupWithViewPager(myViewPager);

    }

    @Override
    protected void onStart() {
        super.onStart();

        // if the user is not authenticated, send user to login activity
        if(currentUser == null)
        {
            sendUserToLoginActivity();
        }
        // if the user is authenticated, we will verify the user existence
        else
        {
            verifyUserExistence();
        }
    }

    private void verifyUserExistence() {
        // getting the current user ID
        String currentUserID = mAuth.getCurrentUser().getUid();

        // referencing the current user ID node then
        rootRef.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // if the "name" node exist ie the user is not a new user
                // here dataSnapshot = rootRef/Users/UID
                if((dataSnapshot.child("name").exists()))
                {
                    // for old user no need to force for updating settings
                    // so we told welcome
                    Toast.makeText(MainActivity.this, "Welcome!", Toast.LENGTH_LONG).show();
                }
                else
                {
                    // send user to settings activity for setting up his name.
                    // dp is optional but profile name is mandatory
                    // that's why we checked only for existing of name
                    sendUserToSettingsActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    // by this method we can access the options inside the menu folder
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.options_menu, menu); // linking up with menu option resource file
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
         super.onOptionsItemSelected(item);

         if(item.getItemId() == R.id.main_logout_option)
         {
             mAuth.signOut();
             sendUserToLoginActivity();

         }

        if(item.getItemId() == R.id.main_settings_option)
        {
            sendUserToSettingsActivity();
        }

        if(item.getItemId() == R.id.main_find_friends_option)
        {
            sendUserToFindFriendsActivity();
        }

        if(item.getItemId() == R.id.main_create_group_option)
        {
            requestNewGroup();
        }

        return true;
    }

    private void requestNewGroup() {
        // we will ask the user to enter the group name using alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.AlertDialog);
        builder.setTitle("Enter Group Name :");

        final EditText groupNameField = new EditText(MainActivity.this); // getting the group name from user
        groupNameField.setHint("e.g Murkir Bilas"); // showing hint
        builder.setView(groupNameField); // setting the view for the edit text

        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String groupName = groupNameField.getText().toString();

                // if the fields are empty
                if(TextUtils.isEmpty(groupName) )
                {
                    groupNameField.requestFocus();
                    groupNameField.setError("Please write a valid Group Name");
                    return;
                }
                else
                {
                    createNewGroup(groupName);
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show(); // showing dialog box for both options

    }

    private void createNewGroup(final String groupName) {
        rootRef.child("Groups").child(groupName).setValue("")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(MainActivity.this, groupName + " group is created successfully..", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void sendUserToLoginActivity() {

        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        // user cant' go back to login activity once he reaches to main activity
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(loginIntent);
        finish();
    }

    private void sendUserToFindFriendsActivity() {

        Intent findFriendsIntent = new Intent(MainActivity.this, FindFriendsActivity.class);
        startActivity(findFriendsIntent);
        finish();
    }

    private void sendUserToSettingsActivity() {

        Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
        // user cant' go back to main activity once he reaches to settings activity for not choosing his name
        settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(settingsIntent);
        finish();
    }
}
