package com.example.meta.UI;

import static com.example.meta.Other.StringUtil.OtherUserID;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.airbnb.lottie.LottieAnimationView;
import com.example.meta.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Objects;


public class IncomingCallActivity extends AppCompatActivity {
    LottieAnimationView accept_call, cancel_call;

    ImageView profile_image_calling;
    DatabaseReference mRef;
    String ProfileImageLink, Username;
    String myProfileImageLink, myUsername, myID, checker = "";
    private String callingID = "", ringingID = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_call);
        cancel_call = findViewById(R.id.cancel_call);
        accept_call = findViewById(R.id.make_call);
        myID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        profile_image_calling = findViewById(R.id.profile_image_calling);
        mRef = FirebaseDatabase.getInstance().getReference().child("Users");
        cancel_call.setOnClickListener(view -> {
            checker = "clicked";
        });
        // chấp nhận cuộc gọi chuyển wa activity phát video call
        accept_call.setOnClickListener(view -> {
            final HashMap<String, Object> callingPickUpMap = new HashMap<>();
            callingPickUpMap.put("picked", "picked");
            mRef.child(myID).child("Ringing")
                    .updateChildren(callingPickUpMap)
                    .addOnCompleteListener(task -> {
                        Intent intent = new Intent(IncomingCallActivity.this, VideoCallActivity.class);
                        startActivity(intent);
                    });
        });
        LoadOtherUser();
    }

    //Load ảnh người được gọi trên màn
    private void LoadOtherUser() {
        mRef.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(OtherUserID).exists()) {
                    Username = snapshot.child(OtherUserID).child("name").getValue().toString();
                    ProfileImageLink = snapshot.child(OtherUserID).child("image").getValue().toString();
                    Picasso.get().load(ProfileImageLink).into(profile_image_calling);
                }
                if (snapshot.child(myID).exists()) {
                    myProfileImageLink = snapshot.child(myID).child("image").getValue().toString();
                    myUsername = snapshot.child(myID).child("name").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // khi bắt đầu trước khi được khởi tạo
    @Override
    protected void onStart() {
        super.onStart();


        mRef.child(OtherUserID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!checker.equals("clicked") && !snapshot.hasChild("Calling") && !snapshot.hasChild("Ringing")) {
                    final HashMap<String, Object> call = new HashMap<>();
                    call.put("calling", OtherUserID);
                    mRef.child(myID).child("Calling").updateChildren(call).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            final HashMap<String, Object> ring = new HashMap<>();
                            ring.put("ringing", myID);
                            mRef.child(OtherUserID).child("Ringing").updateChildren(ring);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(myID).hasChild("Ringing") && !snapshot.child(myID).hasChild("Calling")) {
                    accept_call.setVisibility(View.VISIBLE);
                }
                if (snapshot.child(OtherUserID).child("Ringing").hasChild("picked")) {
                    Intent intent = new Intent(IncomingCallActivity.this, VideoCallActivity.class);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onStop() {

        super.onStop();
    }
}