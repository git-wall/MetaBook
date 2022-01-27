package com.example.meta.Adapter;

import android.app.FragmentTransaction;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import androidx.fragment.app.FragmentStatePagerAdapter;

import com.example.meta.Fragments.ChatListFragment;
import com.example.meta.Fragments.HomeFragment;
import com.example.meta.Fragments.NotificationsFragment;
import com.example.meta.Fragments.ProfileFragment;
import com.example.meta.Fragments.UsersFragment;
import com.example.meta.R;

public class ViewPagerAdapter extends FragmentStatePagerAdapter {

    public ViewPagerAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return new HomeFragment();
            case 1:
                return new ProfileFragment();
            case 2:
                return new ChatListFragment();
            case 3:
                return new UsersFragment();
            case 4:
                return new NotificationsFragment();
            default:
                return new HomeFragment();
        }
    }

    @Override
    public int getCount() {
        return 5;
    }
}
