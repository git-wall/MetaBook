package com.example.meta.UI;

import static com.example.meta.Other.StringUtil.FB_URL;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;

import com.example.meta.Adapter.AdapterUsers;
import com.example.meta.Model.ModelUser;
import com.example.meta.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class PostLikedByActivity extends AppCompatActivity {
    String postId;
    private RecyclerView recyclerView;
    private List<ModelUser> userList;
    private AdapterUsers adapterUsers;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_liked_by);
        recyclerView = findViewById(R.id.recyclerView);
        //action bar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Post Liked By");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        firebaseAuth = FirebaseAuth.getInstance();
        actionBar.setSubtitle(firebaseAuth.getCurrentUser().getEmail());
        //get postID
        Intent intent = getIntent();
        postId = intent.getStringExtra("postId");
        userList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance(FB_URL).getReference("Likes");
        ref.child(postId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String hisUid = "" + ds.getRef().getKey();
                    getUsers(hisUid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getUsers(String hisUid) {
        DatabaseReference ref= FirebaseDatabase.getInstance(FB_URL).getReference("Users");
        ref.orderByChild("uid").equalTo(hisUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds:snapshot.getChildren()){
                    ModelUser modelUser = ds.getValue(ModelUser.class);
                    userList.add(modelUser);
                }
                adapterUsers = new AdapterUsers(PostLikedByActivity.this,userList);
                recyclerView.setAdapter(adapterUsers);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}