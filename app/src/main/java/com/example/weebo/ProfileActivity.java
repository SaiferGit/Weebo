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

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private String receiverUserID, senderUserID,  current_state;

    private CircleImageView userProfileImage;
    private TextView userProfileName, userProfileStatus;
    private Button sendMessageRequestButton;

    private DatabaseReference userRef, chatRequestRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        chatRequestRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");

        receiverUserID = getIntent().getExtras().get("visit_user_id").toString();
        senderUserID = mAuth.getCurrentUser().getUid(); // sneder user id is the current user id who is online

        Toast.makeText(this, "User ID: " +receiverUserID, Toast.LENGTH_SHORT).show();


        userProfileImage = (CircleImageView) findViewById(R.id.visit_profile_image);
        userProfileName = (TextView) findViewById(R.id.visit_user_name);
        userProfileStatus = (TextView) findViewById(R.id.visit_profile_status);
        sendMessageRequestButton = (Button) findViewById(R.id.send_message_request_button);
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
        // taile current state change kore diye button er naam change kore dilam
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
                }
            });
        }
        // if sender id = reciever id, we won't display the send message button
        else
        {
            sendMessageRequestButton.setVisibility(View.INVISIBLE);
        }
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
