package com.example.meta.UI;

import static com.example.meta.Other.StringUtil.FB_URL;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.meta.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class InformationThereUserActivity extends AppCompatActivity {
    String uid, name, email, phone, gender, birthday, live, relationship;
    TextView liveTv, emailTv, phoneTv, GenderTv, birthdayTv, RelationshipTv;
    ActionBar actionBar;
    ImageView contactEmail,callPhone;
    private static final int MY_PERMISSION_REQUEST_CODE_CALL_PHONE = 555;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information_there_user);
        actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        Mirror();
        Intent intent = getIntent();
        uid = intent.getStringExtra("uid");
        Query query = FirebaseDatabase.getInstance(FB_URL).getReference("Users").orderByChild("uid").equalTo(uid);
        query.addValueEventListener(new ValueEventListener() {
            @SuppressLint({"SetTextI18n", "ResourceType"})
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    name = "" + ds.child("name").getValue();
                    email = "" + ds.child("email").getValue();
                    phone = "" + ds.child("phone").getValue();
                    gender = "" + ds.child("gender").getValue();
                    birthday = "" + ds.child("birthday").getValue();
                    live = "" + ds.child("live").getValue();
                    relationship = "" + ds.child("relationship").getValue();
                    liveTv.setText(live);
                    emailTv.setText(email);
                    phoneTv.setText(phone);
                    birthdayTv.setText(birthday);
                    RelationshipTv.setText(relationship);
                    GenderTv.setText(gender);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        callPhone.setOnClickListener(view -> {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) { // 23
                int sendSmsPermisson = ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.CALL_PHONE);
                if (sendSmsPermisson != PackageManager.PERMISSION_GRANTED) {
                    this.requestPermissions(
                            new String[]{Manifest.permission.CALL_PHONE},
                            MY_PERMISSION_REQUEST_CODE_CALL_PHONE
                    );
                    return;
                }
            }
            callNow();
        });
        contactEmail.setOnClickListener(view -> {
            Intent intent1 = new Intent(Intent.ACTION_SENDTO);
            String uriText = "mailto:" + Uri.encode("louyi696@gmail.com") + "?subject="
                    + Uri.encode("Feedback") + Uri.encode("");
            Uri uri = Uri.parse(uriText);
            intent1.setData(uri);
            startActivity(Intent.createChooser(intent1, "Send mail"));
        });
    }

    private void callNow() {
        Intent call = new Intent(Intent.ACTION_CALL);
        call.setData(Uri.parse("tel:" + phoneTv.getText().toString().trim()));
        startActivity(call);
    }

    private void Mirror() {
        liveTv = findViewById(R.id.liveTv);
        emailTv = findViewById(R.id.emailTv);
        phoneTv = findViewById(R.id.phoneTv);
        GenderTv = findViewById(R.id.GenderTv);
        birthdayTv = findViewById(R.id.birthdayTv);
        RelationshipTv = findViewById(R.id.RelationshipTv);
        contactEmail = findViewById(R.id.contactEmail);
        callPhone = findViewById(R.id.callPhone);
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == MY_PERMISSION_REQUEST_CODE_CALL_PHONE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted!", Toast.LENGTH_LONG).show();
                callNow();
            } else {
                Toast.makeText(this, "Permission denied!", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == MY_PERMISSION_REQUEST_CODE_CALL_PHONE) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Action OK", Toast.LENGTH_LONG).show();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Action Cancelled", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Action Failed", Toast.LENGTH_LONG).show();
            }
        }
    }
}