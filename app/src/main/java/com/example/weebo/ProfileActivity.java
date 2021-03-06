package com.example.weebo;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private String receiverUserID, senderUserID,  current_state;

    private CircleImageView userProfileImage;
    private TextView userProfileName, userProfileStatus;
    private Button sendMessageRequestButton, declineMessageRequestButton;

    private DatabaseReference userRef, chatRequestRef , contactRef, notificationRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        chatRequestRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        contactRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        notificationRef = FirebaseDatabase.getInstance().getReference().child("Notifications");

        receiverUserID = getIntent().getExtras().get("visit_user_id").toString();
        senderUserID = mAuth.getCurrentUser().getUid(); // sneder user id is the current user id who is online

        Toast.makeText(this, "User ID: " +receiverUserID, Toast.LENGTH_SHORT).show();


        userProfileImage = (CircleImageView) findViewById(R.id.visit_profile_image);
        userProfileName = (TextView) findViewById(R.id.visit_user_name);
        userProfileStatus = (TextView) findViewById(R.id.visit_profile_status);
        sendMessageRequestButton = (Button) findViewById(R.id.send_message_request_button);
        declineMessageRequestButton = (Button) findViewById(R.id.decline_message_request_button);
        current_state = "new";

        retrieveUserInfo();
    }

    private void retrieveUserInfo() {
        userRef.child(receiverUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if((dataSnapshot.exists()) && (dataSnapshot.hasChild("image")))
                {
                    String userimage = dataSnapshot.child("image").getValue().toString();
                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();

                    Picasso.get().load(userimage).placeholder(R.drawable.profile_image).into(userProfileImage);
                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);

                    // if anyone want's to send a msg, he will first request to the user
                    manageChatRequest();
                }
                else
                {
                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();

                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);

                    manageChatRequest();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void manageChatRequest() {

        // chatRequestRef = rootRef/Chat Requests
        // chat Requests er jodi child e jodi sender theke thake ebong oi sender er jodi
        // receiver theke thake taile check the request type. jodi request type sent hoi
        // taile current state change kore diye button er naam change kore cancel chat request dilam, as sender can send/cancel his request
        chatRequestRef.child(senderUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild(receiverUserID))
                        {
                            String request_type = dataSnapshot.child(receiverUserID).child("request_type").getValue().toString();

                            if(request_type.equals("sent"))
                            {
                                current_state = "request_sent";
                                sendMessageRequestButton.setText("Cancel Chat Request");
                            }

                            // receiver portion: request_type jodi received hoi, maane receiver er phone e receiver er jonne eta received hobe.
                            // taile current_state request received kore dilam and receiver send msg button take Accept chat request hishebe dekhbe.
                            // moreover decline message request button ta invisible kora ache by default, etake visible kore dilam. ar otate click korle request cancel hoye jabe.
                            // request cancel hoye gele amra abar button take invisible kore dibo receiver er jonne, so eita korechi cancelChatRequest() method er moddhe.

                            else if(request_type.equals("received"))
                            {
                                current_state = "request_received";
                                sendMessageRequestButton.setText("Accept ChatRequest");

                                declineMessageRequestButton.setVisibility(View.VISIBLE);
                                declineMessageRequestButton.setEnabled(true);
                                declineMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        cancelChatRequest();
                                    }
                                });
                            }
                        }
                        else
                        {
                            // jodi sender er under e receiver er child thake taile tara friends and button text remove this contact set korbo

                            contactRef.child(senderUserID)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if(dataSnapshot.hasChild(receiverUserID))
                                            {
                                                current_state = "friends";
                                                sendMessageRequestButton.setText("Remove this Contact");
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


        if(!senderUserID.equals(receiverUserID))
        {
            // we will show the send msg button and the sender can request for sending message
            sendMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendMessageRequestButton.setEnabled(false); // send message button take invisible kore dilam

                    if(current_state.equals("new")) // current state diye check kortesi sender ta receiver er kache new kina. jodi new hoi taile
                    {
                        sendChatRequest(); // chat request send korte parbe
                    }
                    // if the request is already sent, the sender can cancel the chat request
                    if(current_state.equals("request_sent"))
                    {
                        cancelChatRequest();
                    }
                    // the receiver received any request from a user
                    if(current_state.equals("request_received"))
                    {
                        acceptChatRequest(); // he can accept it
                    }
                    // if 2 contacts are already friends to each other then they can remove themselves from that
                    if(current_state.equals("friends"))
                    {
                        removeSpecificContact();
                    }
                }
            });
        }
        // if sender id = receiver id, we won't display the send message button
        else
        {
            sendMessageRequestButton.setVisibility(View.INVISIBLE);
        }
    }

    private void removeSpecificContact()
    {
        // remove korbo both user and reciever ID both
        contactRef.child(senderUserID).child(receiverUserID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            contactRef.child(receiverUserID).child(senderUserID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful())
                                            {
                                                sendMessageRequestButton.setEnabled(true);
                                                current_state = "new";
                                                sendMessageRequestButton.setText("Send Message");

                                                // receiver chat decline kore dile cancel msg button ta invisible kore dibo.
                                                declineMessageRequestButton.setVisibility(View.INVISIBLE);
                                                declineMessageRequestButton.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void acceptChatRequest() {
        // contactRef = rootRef/ contacts
        // contacts -> senderID -> receiverID -> contacts folder e jeye save hobe
        // same kaaj ta receiver er contacts eo jeye save hobe, shei khetre age receiver thakbe
        // then users then contact folder for each then saved
        contactRef.child(senderUserID).child(receiverUserID)
                .child("Contacts").setValue("Saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            contactRef.child(receiverUserID).child(senderUserID)
                                    .child("Contacts").setValue("Saved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful())
                                            {
                                                // jodi contact saved hoi, taile oi chat request 2 ta delete kore dibo
                                                chatRequestRef.child(senderUserID).child(receiverUserID)
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if(task.isSuccessful())
                                                                {
                                                                    chatRequestRef.child(receiverUserID).child(senderUserID)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if(task.isSuccessful())
                                                                                    {
                                                                                        // jodi successfully delete korte pari taile tader moddhe current state hocche friends and tara chaile
                                                                                        // eke onnoke remove kore dite parbe, so button text change kore Remove this contact diye dibo.
                                                                                        sendMessageRequestButton.setEnabled(true);
                                                                                        current_state= "friends";
                                                                                        sendMessageRequestButton.setText("Remove this Contact");

                                                                                        declineMessageRequestButton.setVisibility(View.INVISIBLE);
                                                                                        declineMessageRequestButton.setEnabled(false);
                                                                                    }
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });

                                            }
                                        }
                                    });

                        }
                    }
                });
    }

    private void cancelChatRequest() {
        // cancel korbo user and reciever ID both
        chatRequestRef.child(senderUserID).child(receiverUserID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            chatRequestRef.child(receiverUserID).child(senderUserID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful())
                                            {
                                                sendMessageRequestButton.setEnabled(true);
                                                current_state = "new";
                                                sendMessageRequestButton.setText("Send Message");

                                                // receiver chat decline kore dile cancel msg button ta invisible kore dibo.
                                                declineMessageRequestButton.setVisibility(View.INVISIBLE);
                                                declineMessageRequestButton.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void sendChatRequest() {
        // chatReqRef = rootRef/Chat Requests
        // then store senderID -> receiverID -> make a directory name "request_type" -> sent
        // read it as senderID, makes a request sent to receiverID
        // if this task is successful then
        chatRequestRef.child(senderUserID).child(receiverUserID)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            // make another child node under Chat Requests
                            // then store receiverID -> senderID -> make a directory name "request_type" -> received
                            // if the task is successful then
                            chatRequestRef.child(receiverUserID).child(senderUserID)
                                    .child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful())
                                            {
                                                // for firebase push notification we used this map
                                                // we basically created a new node for handling the notification
                                                // using hashmap we stored the notification information
                                                // obj: sender request pathale receiver ta notification er maddhome dekhte parbe
                                                // ekhane amra notification store kortesi db te
                                                HashMap<String, String> chatNotificationMap = new HashMap<>(); // hashmap created named chatNotificationMap
                                                chatNotificationMap.put("from", senderUserID); // stored info
                                                chatNotificationMap.put("type", "request"); // what type of notification

                                                // push(): gave a random key for each notification so that no notification can never be replaced by each other
                                                // Notification -> receiverID -> er under e Map e jei value gula store korsi ogula set kore dibo
                                                notificationRef.child(receiverUserID).push()
                                                        .setValue(chatNotificationMap)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if(task.isSuccessful())
                                                                {
                                                                    // if task is successful then
                                                                    // Send Message button visible kore naam set korlam Cancel Chat Request
                                                                    // and current state change kore request sent kore dilam
                                                                    sendMessageRequestButton.setEnabled(true);
                                                                    current_state = "request_sent";
                                                                    sendMessageRequestButton.setText("Cancel Chat Request");

                                                                }
                                                            }
                                                        });


                                            }
                                        }
                                    });
                        }
                    }
                });
    }
}
