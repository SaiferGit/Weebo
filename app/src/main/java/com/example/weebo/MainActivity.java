package com.example.weebo;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private Toolbar mToolbar; // for adding the toolbar
    private ViewPager myViewPager; // for displaying fragments of the tabs
    private TabLayout myTabLayout;
    private TabsAccessorAdapter myTabsAccessorAdapter; // accessing the class "TabAccessorAdapter" from MainActivity

    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
    }

    private void sendUserToLoginActivity() {

        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(loginIntent);
    }
}
