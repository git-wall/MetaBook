package com.example.meta.UI;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CompoundButton;

import com.example.meta.Other.ToastV;
import com.example.meta.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

public class SettingsActivity extends AppCompatActivity {
    SwitchCompat postSwitch;
    SharedPreferences sp;
    SharedPreferences.Editor editor;
    private static final String TOPIC_POST_NOTIFICATION = "POST";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Settings");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        postSwitch = findViewById(R.id.postSwitch);
        sp = getSharedPreferences("Notification_SP", MODE_PRIVATE);
        boolean isPostEnabled = sp.getBoolean("" + TOPIC_POST_NOTIFICATION, false);
        if(isPostEnabled){
            postSwitch.setChecked(true);
        }else {
            postSwitch.setChecked(false);
        }
        postSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                editor = sp.edit();
                editor.putBoolean(""+TOPIC_POST_NOTIFICATION,b);
                if (b) {
                    subscribePostNotification();
                } else {
                    unsubscribePostNotification();
                }
            }
        });
    }

    private void unsubscribePostNotification() {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(""+TOPIC_POST_NOTIFICATION)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg ="You will not receive post notification";
                        if(!task.isSuccessful()){
                            msg ="UnSubscription failed";
                        }
                        ToastV toastV = new ToastV();
                        toastV.Info(SettingsActivity.this,"Notification",msg);
                    }
                });
    }

    private void subscribePostNotification() {
        FirebaseMessaging.getInstance().subscribeToTopic(""+TOPIC_POST_NOTIFICATION)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg ="You will receive post notification";
                        if(!task.isSuccessful()){
                            msg ="Subscription failed";
                        }
                        ToastV toastV = new ToastV();
                        toastV.Info(SettingsActivity.this,"Notification",msg);
                    }
                });
    }

    @Override
    public boolean onNavigateUp() {
        onBackPressed();
        return super.onNavigateUp();
    }
}