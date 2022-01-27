package com.example.meta.UI;

import static com.example.meta.Other.StringUtil.FB_URL;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.TextView;

import com.example.meta.Other.ToastV;
import com.example.meta.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {
    EditText mEmailEt, mPasswordEt;
    AppCompatButton mRegisterBtn;
    private FirebaseAuth mAuth;
    ProgressDialog progressDialog;
    TextView mHaveAccountTv;
    private FirebaseUser mUser;
    ToastV toastV = new ToastV();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Create Account");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        mEmailEt = findViewById(R.id.emailEt);
        mPasswordEt = findViewById(R.id.passwordEt);
        mRegisterBtn = findViewById(R.id.registerBtn);
        mHaveAccountTv = findViewById(R.id.have_accountTv);
        progressDialog = new ProgressDialog(this);
        mAuth = FirebaseAuth.getInstance();
        progressDialog.setMessage("Registering User...");
        mRegisterBtn.setOnClickListener(view -> {
            String email = mEmailEt.getText().toString().trim();
            String password = mPasswordEt.getText().toString().trim();
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                mEmailEt.setError("Invalid Email");
                mEmailEt.setFocusable(true);
            } else if (password.length() < 6) {
                mPasswordEt.setError("Password length at least 6 characters");
                mPasswordEt.setFocusable(true);
            } else {
                registerUser(email, password);
            }
        });
        mHaveAccountTv.setOnClickListener(view -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });

    }

    private void registerUser(String email, String password) {
        progressDialog.show();
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                progressDialog.dismiss();
                FirebaseUser user = mAuth.getCurrentUser();
                String email1 = user.getEmail();
                String uid = user.getUid();
                HashMap<Object,String> hashMap = new HashMap<>();
                hashMap.put("email",email1);
                hashMap.put("uid",uid);
                hashMap.put("name","");
                hashMap.put("phone","");
                hashMap.put("image","");
                hashMap.put("cover","");
                hashMap.put("gender","");
                hashMap.put("birthday","");
                hashMap.put("live","");
                hashMap.put("relationship","");
                hashMap.put("onlineStatus","online");
                hashMap.put("typingTo","noOne");
                FirebaseDatabase database = FirebaseDatabase.getInstance(FB_URL);
                DatabaseReference reference = database.getReference("Users");
                reference.child(uid).setValue(hashMap);
                toastV.Success(RegisterActivity.this,"Register","Success");
                Intent intent = new Intent(RegisterActivity.this, DashBoardActivity.class);
                startActivity(intent);
                finish();
            } else {
                progressDialog.dismiss();
            }
        }).addOnFailureListener(e -> progressDialog.dismiss());
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}