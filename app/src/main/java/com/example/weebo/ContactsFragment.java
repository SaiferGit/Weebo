package com.example.weebo;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ContactsFragment extends Fragment {

    private View contactsView;  // view for the fragment
    private RecyclerView myContactList;

    private DatabaseReference contactsRef, usersRef;
    private FirebaseAuth mAuth;

    private String currentUserID;

    public ContactsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        contactsView =  inflater.inflate(R.layout.fragment_contacts, container, false);

        // initializing Recycler View
        myContactList = (RecyclerView) contactsView.findViewById(R.id.contacts_list);
        myContactList.setLayoutManager(new LinearLayoutManager(getContext()));

        // before getting contacts of every user, we need to get the online/ current user id first
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        // now contactsRef is referencing to the online user's node that is in the contacts folder
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserID);
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");


        return contactsView;
    }

    // by firebase recycler adapter, we can retrieve all of our contacts from firebase
    // as soon as the activity starts we need to load all the contacts here


    @Override
    public void onStart() {
        super.onStart();

        // set queryr moddhe database er reference diye dite hobe
        // first e recycler option khuli , then oi option ta recycler adapter er moddh pass kori
        FirebaseRecyclerOptions options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(contactsRef, Contacts.class)
                .build();

        // firebase recycler adapter 2 ta parameter nibe, module class and a static class
        // module class er maddhome ami ei recycler view er shathe firebase er connect koriye data antesi
        // ar static class ta hoitese view Holder class
        FirebaseRecyclerAdapter<Contacts, ContactsViewHolder> adapter
                = new FirebaseRecyclerAdapter<Contacts, ContactsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ContactsViewHolder holder, int position, @NonNull Contacts model) {
                // contact er under e ekjon user er jotogula contact ache, shob gular key niye tader data khuje ber korbo
                // eijonne first e contact er under e key collect korlam by the help of position. then oi key gulo use kore tar data retrieve korbo.
                // user -> id -> image jodi thake taile string variable e oigula get korlam. then holder object diye contacts view holder layout er value gula set korlam
                // for displaying image we used picasso again
                String userIDs = getRef(position).getKey();
                usersRef.child(userIDs).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists())
                        {
                            // user er state dekhanor jonne
                            if(dataSnapshot.child("userState").hasChild("state"))
                            {
                                String state = dataSnapshot.child("userState").child("state").getValue().toString();
                                String date = dataSnapshot.child("userState").child("date").getValue().toString();
                                String time = dataSnapshot.child("userState").child("time").getValue().toString();

                                if(state.equals("online"))
                                {
                                    holder.onlineIcon.setVisibility(View.VISIBLE);
                                }
                                else if(state.equals("offline"))
                                {
                                    holder.onlineIcon.setVisibility(View.INVISIBLE);
                                }
                            }

                            // for those user who didn't update their app
                            else
                            {
                                holder.onlineIcon.setVisibility(View.INVISIBLE);
                            }

                            if(dataSnapshot.hasChild("image"))
                            {
                                String userImage = dataSnapshot.child("image").getValue().toString();
                                String profileName = dataSnapshot.child("name").getValue().toString();
                                String profileStatus = dataSnapshot.child("status").getValue().toString();

                                holder.userName.setText(profileName);
                                holder.userStatus.setText(profileStatus);
                                Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(holder.profileImage);
                            }
                            else
                            {
                                String profileName = dataSnapshot.child("name").getValue().toString();
                                String profileStatus = dataSnapshot.child("status").getValue().toString();

                                holder.userName.setText(profileName);
                                holder.userStatus.setText(profileStatus);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @NonNull
            @Override
            public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                // this viewHolder is basically which layout we are going to display our retrieved data.
                // so first e amra amader oi layout ke access korbo
                // then amra amader view take return korbo
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout, viewGroup, false);
                ContactsViewHolder viewHolder = new ContactsViewHolder(view); // creating obj of our static class everytime
                return  viewHolder; // returning that object

            }
        };

        myContactList.setAdapter(adapter);
        adapter.startListening();

    }


    public static class ContactsViewHolder extends RecyclerView.ViewHolder
    {
        TextView userName, userStatus;
        CircleImageView profileImage;
        ImageView onlineIcon;

        public ContactsViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_status);
            profileImage = itemView.findViewById(R.id.users_profile_image);
            onlineIcon = (ImageView) itemView.findViewById(R.id.user_online_status);

        }
    }
}
