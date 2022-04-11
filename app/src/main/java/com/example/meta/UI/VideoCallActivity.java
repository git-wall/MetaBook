package com.example.meta.UI;

import static com.example.meta.Other.StringUtil.API_KEY;
import static com.example.meta.Other.StringUtil.OtherUserID;
import static com.example.meta.Other.StringUtil.RC_VIDEO_APP_PERM;
import static com.example.meta.Other.StringUtil.SESSION_ID;
import static com.example.meta.Other.StringUtil.TOKEN;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;

import com.airbnb.lottie.LottieAnimationView;

import com.example.meta.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class VideoCallActivity extends AppCompatActivity implements Session.SessionListener, PublisherKit.PublisherListener{
    public static final String LOG_TAG = VideoCallActivity.class.getSimpleName();

    LottieAnimationView closeVideoChatBtn;
    DatabaseReference mRef;
    private String userID = "";
    private FrameLayout mPubViewController;
    private FrameLayout mSubViewController;
    private Session mSession;
    private Publisher mPublisher;
    private Subscriber mSubscriber;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_call);
        closeVideoChatBtn = findViewById(R.id.close_video_chat_btn);
        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mRef = FirebaseDatabase.getInstance().getReference().child("Users");
        mPubViewController = findViewById(R.id.pub_container);
        mSubViewController = findViewById(R.id.sub_container);
        // tắt activity video call trở về chat activity
        closeVideoChatBtn.setOnClickListener(view -> {
            mRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.child(userID).hasChild("Ringing")) {
                        mRef.child(userID).child("Ringing").removeValue();
                        if (mPublisher != null) {
                            mPublisher.destroy();
                        }
                        if (mSubscriber != null) {
                            mSubscriber.destroy();
                        }
                        if (snapshot.child(OtherUserID).hasChild("Calling")) {
                            mRef.child(OtherUserID).child("Calling").removeValue();
                        }
                        Intent intent = new Intent(VideoCallActivity.this, ChatActivity.class);
                        intent.putExtra("hisUid", OtherUserID);
                        startActivity(intent);
                        finish();
                    }
                    if (snapshot.child(OtherUserID).hasChild("Ringing")) {
                        mRef.child(OtherUserID).child("Ringing").removeValue();
                        if (mPublisher != null) {
                            mPublisher.destroy();
                        }
                        if (mSubscriber != null) {
                            mSubscriber.destroy();
                        }
                        if (snapshot.child(userID).hasChild("Calling")) {
                            mRef.child(userID).child("Calling").removeValue();
                        }
                        Intent intent = new Intent(VideoCallActivity.this, ChatActivity.class);
                        intent.putExtra("hisUid", OtherUserID);
                        startActivity(intent);
                        finish();
                    } else {
                        Intent intent = new Intent(VideoCallActivity.this, ChatActivity.class);
                        intent.putExtra("hisUid", OtherUserID);
                        startActivity(intent);
                        finish();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        });
        requestPermission();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, VideoCallActivity.this);
    }

    // check quyền
    @AfterPermissionGranted(RC_VIDEO_APP_PERM)
    private void requestPermission() {
        String[] perm = {Manifest.permission.INTERNET, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
        if (EasyPermissions.hasPermissions(this, perm)) {
            mSession = new Session.Builder(this, API_KEY, SESSION_ID).build();
            mSession.setSessionListener((Session.SessionListener) VideoCallActivity.this);
            mSession.connect(TOKEN);
        } else {
            EasyPermissions.requestPermissions(this, "Need Camera and Mic Permission...", RC_VIDEO_APP_PERM, perm);
        }
    }

    @Override
    public void onStreamCreated(PublisherKit publisherKit, Stream stream) {

    }

    @Override
    public void onStreamDestroyed(PublisherKit publisherKit, Stream stream) {

    }

    @Override
    public void onError(PublisherKit publisherKit, OpentokError opentokError) {

    }

    @Override
    public void onConnected(Session session) {
        Log.i(LOG_TAG, "Session Connected");
        mPublisher = new Publisher.Builder(this).build();
        mPublisher.setPublisherListener((PublisherKit.PublisherListener) VideoCallActivity.this);
        mPubViewController.addView(mPublisher.getView());
        if (mPublisher.getView() instanceof GLSurfaceView) {
            ((GLSurfaceView) mPublisher.getView()).setZOrderOnTop(true);
        }
        mSession.publish(mPublisher);
    }

    @Override
    public void onDisconnected(Session session) {
        Log.i(LOG_TAG, "Stream Disconnected");
    }

    @Override
    public void onStreamReceived(Session session, Stream stream) {
        Log.i(LOG_TAG, "Stream Receiver");
        if (mSubscriber == null) {
            mSubscriber = new Subscriber.Builder(this, stream).build();
            mSession.subscribe(mSubscriber);
            mSubViewController.addView(mSubscriber.getView());
        }
    }

    @Override
    public void onStreamDropped(Session session, Stream stream) {
        Log.i(LOG_TAG, "Stream Dropped");
        if (mSubscriber != null) {
            mSubscriber = null;
            mSubViewController.removeAllViews();
        }
    }

    @Override
    public void onError(Session session, OpentokError opentokError) {
        Log.i(LOG_TAG, "Stream Error");
    }

    @Override
    protected void onStart() {


        super.onStart();
    }

    @Override
    public void onStop() {

        super.onStop();
    }
}