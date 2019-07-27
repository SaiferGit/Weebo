package com.example.weebo;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class TabsAccessorAdapter extends FragmentPagerAdapter {

    //class to access the tabs

    public TabsAccessorAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        //switch for getting the position of our fragments
        switch (i) //here i is the position
        {
            case 0:
                ChatsFragment chatsFragment = new ChatsFragment();
                return chatsFragment;

            case 1:
                GroupsFragment groupsFragment = new GroupsFragment();
                return groupsFragment;

            case 2:
                ContactsFragment contactsFragment  = new ContactsFragment();
                return contactsFragment;

            default:
                return null;
        }

    }

    @Override
    public int getCount() {
        return 3; // since we have 3 fragments
    }

    // methods for setting up title for that 3 fragments


    @Nullable
    @Override
    public CharSequence getPageTitle(int position)
    {
        //setting up the title
        switch (position) //here i is the position
        {
            case 0:
                return "Chats";

            case 1:
                return "Groups";

            case 2:
                return "Contacts";

            default:
                return null;
        }
    }
}
