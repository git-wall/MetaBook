package com.example.meta.UI;

import static com.example.meta.Other.StringUtil.FB_URL;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.meta.Adapter.AdapterPosts;
import com.example.meta.Model.ModelPost;
import com.example.meta.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ThereProfileActivity extends AppCompatActivity {
    FirebaseAuth firebaseAuth;
    RecyclerView postsRecycleView;
    List<ModelPost> postList;
    AdapterPosts adapterPosts;
    String uid;
    ImageView imgBg;
    TextView nameTv;
    CircleImageView avatar;
    CardView cardViewInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_there_profile);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Profile");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        postsRecycleView = findViewById(R.id.recycleView_posts);
        avatar = findViewById(R.id.avatar);
        nameTv = findViewById(R.id.nameTv);
        imgBg = findViewById(R.id.imgBg);
        cardViewInfo = findViewById(R.id.cardViewInfo);
        firebaseAuth = FirebaseAuth.getInstance();
        Intent intent = getIntent();
        uid = intent.getStringExtra("uid");
        Query query = FirebaseDatabase.getInstance(FB_URL).getReference("Users").orderByChild("uid").equalTo(uid);
        query.addValueEventListener(new ValueEventListener() {
            @SuppressLint({"SetTextI18n", "ResourceType"})
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String name = "" + ds.child("name").getValue();
                    String cover = "" + ds.child("cover").getValue();
                    String image = "" + ds.child("image").getValue();
                    nameTv.setText(name);
                    try {
                        Picasso.get().load(image).into(avatar);
                    } catch (Exception e) {
                        Picasso.get().load(R.id.sin).into(avatar);
                    }
                    try {
                        Picasso.get().load(cover).into(imgBg);
                    } catch (Exception e) {
                        Picasso.get().load(R.id.sin).into(imgBg);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        cardViewInfo.setOnClickListener(view -> {
            Intent intent1 = new Intent(ThereProfileActivity.this,InformationThereUserActivity.class);
            intent1.putExtra("uid",uid);
            startActivity(intent1);
        });
        postList = new ArrayList<>();
        checkUserStatus();
        loadHitsPost();
    }

    private void loadHitsPost() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        postsRecycleView.setLayoutManager(linearLayoutManager);
        DatabaseReference ref = FirebaseDatabase.getInstance(FB_URL).getReference("Posts");
        Query query = ref.orderByChild("uid").equalTo(uid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ModelPost modelPost = ds.getValue(ModelPost.class);
                    postList.add(modelPost);
                    adapterPosts = new AdapterPosts(ThereProfileActivity.this, postList);
                    postsRecycleView.setAdapter(adapterPosts);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ThereProfileActivity.this, "" + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void searchHistPost(String search){
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        postsRecycleView.setLayoutManager(linearLayoutManager);
        DatabaseReference ref = FirebaseDatabase.getInstance(FB_URL).getReference("Posts");
        Query query = ref.orderByChild("uid").equalTo(uid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ModelPost modelPost = ds.getValue(ModelPost.class);
                    if (modelPost.getpTitle().toLowerCase().contains(search.toLowerCase()) ||
                            modelPost.getpDescr().toLowerCase().contains(search.toLowerCase())) {
                        postList.add(modelPost);
                    }
                    adapterPosts = new AdapterPosts(ThereProfileActivity.this, postList);
                    postsRecycleView.setAdapter(adapterPosts);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ThereProfileActivity.this, "" + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.action_add_post).setVisible(false);
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                if (!TextUtils.isEmpty(s)) {
                    searchHistPost(s);
                } else {
                    loadHitsPost();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (!TextUtils.isEmpty(s)) {
                    searchHistPost(s);
                } else {
                    loadHitsPost();
                }
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            firebaseAuth.signOut();
            checkUserStatus();
        }
        return super.onOptionsItemSelected(item);
    }

    private void checkUserStatus() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {

        } else {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }
}