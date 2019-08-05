package com.example.weebo;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

public class GroupChatActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ImageButton sendMessageButton;
    private EditText userMessageInput;
    private ScrollView mScrollView;
    private TextView displayTextMessages;

    private String currentGroupName, currentUserID, currentUserName, currentDate, currentTime;

    // firebase
    private FirebaseAuth mAuth;
    private DatabaseReference userRef, groupNameRef, groupMessageKeyRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        currentGroupName = getIntent().getExtras().get("groupName").toString(); // getting the group name from groupFragment
        Toast.makeText(GroupChatActivity.this, currentGroupName, Toast.LENGTH_LONG).show();


        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        groupNameRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupName);


        initializeFields();

        getUserInfo();

        // if a user clicks on send message button, the messege will be sent to db first
        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveMessageToDB();

                userMessageInput.setText(null); // after sending the msg the edit text is set to null
                mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    // whenever the activity starts or a user click on the activity
    @Override
    protected void onStart() {
        super.onStart();

        groupNameRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                // if the group exists
                if(dataSnapshot.exists())
                {
                    // showing previous msgs
                    displayMessages(dataSnapshot);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                // if the group exists
                if(dataSnapshot.exists())
                {
                    // showing previous msgs
                    displayMessages(dataSnapshot);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void initializeFields() {
        mToolbar = (Toolbar) findViewById(R.id.group_chat_bar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(currentGroupName);

        sendMessageButton = (ImageButton) findViewById(R.id.send_message_button);
        userMessageInput = (EditText) findViewById(R.id.input_group_message);
        displayTextMessages = (TextView) findViewById(R.id.group_chat_text_display);
        mScrollView = (ScrollView) findViewById(R.id.my_scroll_view);

    }

    private void getUserInfo()
    {
        // currentUserID = user who is online and wanna send msgs.
        userRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // if currentUserID exists
                if(dataSnapshot.exists())
                {
                    currentUserName = dataSnapshot.child("name").getValue().toString(); // retrieving current user name from firebase database

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void saveMessageToDB()
    {
        String msg = userMessageInput.getText().toString(); // getting the typed msg
        String msgKey = groupNameRef.push().getKey(); // unique key created for each msg
        // if the fieldis empty
        if(TextUtils.isEmpty(msg) ){
            userMessageInput.requestFocus();
            userMessageInput.setError("Please type something to send first..");
            return;
        }
        else
        {
            Calendar calForDate = Calendar.getInstance(); // getting current date
            SimpleDateFormat currentDateFormat = new SimpleDateFormat("MMM dd, yyyy");
            // stored the date into currentDate variable
            currentDate = currentDateFormat.format(calForDate.getTime());

            Calendar calForTime = Calendar.getInstance(); // getting current time
            SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
            // stored the time into currentDate variable
            currentTime = currentTimeFormat.format(calForTime.getTime());

            // storing into db
            HashMap<String, Object> groupMessageKey = new HashMap<>();
            groupNameRef.updateChildren(groupMessageKey); // amra jei group e click korlam, groupNameRef variable ke oi group name diye update korlam jate jei group er msg oi group e store korte pari


            groupMessageKeyRef = groupNameRef.child(msgKey);  // msg key ta unique, je msg korlo oi particular group er moddhe tar msg ta oi group er unique msg key er moddhe store korbo, ei msg key ta oi group
            // er shathe child hishebe jog kore dilam. Groups -> Group Name -> unique msg key(groupMessageKeyRef eitake indicate kortese)

            HashMap<String, Object> messageInfoMap = new HashMap<>();
                messageInfoMap.put("name", currentUserName);
                messageInfoMap.put("message", msg);
                messageInfoMap.put("date", currentDate);
                messageInfoMap.put("time", currentTime);
            //updating the children
            groupMessageKeyRef.updateChildren(messageInfoMap); // oi msg key er under e ei child gula update korlam

        }
    }

    private void displayMessages(DataSnapshot dataSnapshot)
    {
        Iterator iterator = dataSnapshot.getChildren().iterator(); // for reading line by line and getting all the msges

        while(iterator.hasNext())
        {
            String chatDate = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatMessage = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatName = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatTime = (String) ((DataSnapshot)iterator.next()).getValue();

            displayTextMessages.append(chatName + " :\n" + chatMessage + " :\n" + chatTime + "      " + chatDate + " :\n\n\n");

            mScrollView.fullScroll(ScrollView.FOCUS_DOWN); // for seeing last or new msg first
        }
    }

}
