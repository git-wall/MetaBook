package com.example.meta.UI;

import static com.example.meta.Other.StringUtil.FB_URL;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;

import com.example.meta.Adapter.ViewPagerAdapter;
import com.example.meta.R;
import com.example.meta.notifications.Token;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Objects;

public class DashBoardActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    ActionBar actionBar;
    BottomNavigationView navigationView;
    String mUID;
    ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dash_board);
        actionBar = getSupportActionBar();
        firebaseAuth = FirebaseAuth.getInstance();
        viewPager = findViewById(R.id.viewPager);
        setupViewPager();
        navigationView = findViewById(R.id.navigation);
        navigationView.setOnNavigationItemSelectedListener(selectedListener);
        navigationView.setItemHorizontalTranslationEnabled(true);
        checkUserStatus();
    }


    @Override
    protected void onResume() {
        checkUserStatus();
        super.onResume();
    }

    public void updateToken() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                return;
            } else {
                String token = Objects.requireNonNull(task.getResult());
                DatabaseReference ref = FirebaseDatabase.getInstance(FB_URL).getReference("Tokens");
                Token token1 = new Token(token);
                ref.child(mUID).setValue(token1);
            }
        });
    }

    private void setupViewPager() {
        ViewPagerAdapter view = new ViewPagerAdapter(getSupportFragmentManager(), FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        viewPager.setAdapter(view);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        navigationView.getMenu().findItem(R.id.nav_home).setChecked(true);
                        actionBar.setTitle("Home");
                        break;
                    case 1:
                        navigationView.getMenu().findItem(R.id.nav_profile).setChecked(true);
                        actionBar.setTitle("Profile");
                        break;
                    case 2:
                        navigationView.getMenu().findItem(R.id.nav_chat).setChecked(true);
                        actionBar.setTitle("Chats");
                        break;
                    case 3:
                        navigationView.getMenu().findItem(R.id.nav_users).setChecked(true);
                        actionBar.setTitle("Friends");
                        break;
                    case 4:
                        navigationView.getMenu().findItem(R.id.nav_more).setChecked(true);
                        actionBar.setTitle("Notification");
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private final BottomNavigationView.OnNavigationItemSelectedListener selectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @SuppressLint("NonConstantResourceId")
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.nav_home:
                    viewPager.setCurrentItem(0);
                    break;
                case R.id.nav_profile:
                    viewPager.setCurrentItem(1);
                    break;
                case R.id.nav_chat:
                    viewPager.setCurrentItem(2);
                    break;
                case R.id.nav_users:
                    viewPager.setCurrentItem(3);
                    break;
                case R.id.nav_more:
                    viewPager.setCurrentItem(4);
                    break;
            }
            return true;
        }
    };

    private void checkUserStatus() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            mUID = user.getUid();
            SharedPreferences sp = getSharedPreferences("SP_USER", MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("Current_USERID", mUID);
            editor.apply();
            DatabaseReference dbRef = FirebaseDatabase.getInstance(FB_URL).getReference("Users").child(mUID);
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("onlineStatus", "online");
            dbRef.updateChildren(hashMap);
            updateToken();
        } else {
            startActivity(new Intent(DashBoardActivity.this, MainActivity.class));
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onStart() {
        checkUserStatus();
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        checkOnlineStatus(timestamp);
        super.onDestroy();
    }

    private void checkOnlineStatus(String status) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance(FB_URL).getReference("Users").child(mUID);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("onlineStatus", status);
        dbRef.updateChildren(hashMap);
    }
}