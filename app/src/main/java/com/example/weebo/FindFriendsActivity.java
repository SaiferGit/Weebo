package com.example.weebo;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriendsActivity extends AppCompatActivity
{
    private Toolbar mToolbar;
    private RecyclerView findFriendsRecyclerList;
    private DatabaseReference usersRef;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);


        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");


        findFriendsRecyclerList = (RecyclerView) findViewById(R.id.find_friends_recycler_list);
        findFriendsRecyclerList.setLayoutManager(new LinearLayoutManager(this));


        mToolbar = (Toolbar) findViewById(R.id.find_friends_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Find Friends");
    }


    @Override
    protected void onStart()
    {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                        .setQuery(usersRef, Contacts.class)
                        .build();
        // retrieving all the users using firebaseRecyclerAdapter
        // it takes 2 parameter, 1 is module class and other is viewHolder class
        // in this case Contacts is a module class, using Contacts we are retrieving name, status and image.
        FirebaseRecyclerAdapter<Contacts, FindFriendViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contacts, FindFriendViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull FindFriendViewHolder holder, final int position, @NonNull Contacts model)
                    {
                        // setting the values using holder object
                        holder.userName.setText(model.getName());
                        holder.userStatus.setText(model.getStatus());
                        Picasso.get().load(model.getImage()).placeholder(R.drawable.profile_image).into(holder.profileImage);

                        // itemview = a object of a user
                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view)
                            {
                                String visit_user_id = getRef(position).getKey(); // if a user click on a object, then it will get the position first and then will get the key of that object
                                

                                Intent profileIntent = new Intent(FindFriendsActivity.this, ProfileActivity.class);
                                profileIntent.putExtra("visit_user_id", visit_user_id);
                                startActivity(profileIntent);
                            }
                        });
                    }


                    @NonNull
                    @Override
                    public FindFriendViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
                    {
                        // using this method we are connecting the users display layout
                        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout, viewGroup, false);
                        FindFriendViewHolder viewHolder = new FindFriendViewHolder(view);
                        return viewHolder;
                        // everytime we are creating a object of viewHolder class to display a user

                    }
                };

        findFriendsRecyclerList.setAdapter(adapter);

        adapter.startListening();
    }


    // this is the viewHolder class
    public static class FindFriendViewHolder extends RecyclerView.ViewHolder
    {
        TextView userName, userStatus;
        CircleImageView profileImage;

        // this is the constructor of the viewHolder Class
        public FindFriendViewHolder(@NonNull View itemView)
        {
            super(itemView);
            // linking the resource file with the viewHolder class
            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_status);
            profileImage = itemView.findViewById(R.id.users_profile_image);
        }
    }
}