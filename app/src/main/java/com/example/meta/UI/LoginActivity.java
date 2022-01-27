package com.example.meta.UI;

import static com.example.meta.Other.StringUtil.FB_URL;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.meta.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 100;
    private static final String TAG = "LOGIN";
    EditText mEmailEt, mPasswordEt;
    TextView notAccountTv, mRecoverPassTv;
    AppCompatButton mLoginBtn;
    private FirebaseAuth mAuth;
    ProgressDialog pd;
    SignInButton mGoogleLoginBtn;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Login");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        GoogleSignInOptions gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mEmailEt = findViewById(R.id.emailLogin);
        mPasswordEt = findViewById(R.id.passwordLogin);
        mLoginBtn = findViewById(R.id.loginBtn);
        notAccountTv = findViewById(R.id.not_accountTv);
        mRecoverPassTv = findViewById(R.id.recoverPassTv);
        mGoogleLoginBtn = findViewById(R.id.googleLoginBtn);
        mAuth = FirebaseAuth.getInstance();
        pd = new ProgressDialog(this);
        notAccountTv.setOnClickListener(view -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            finish();
        });
        mRecoverPassTv.setOnClickListener(view -> {
            showRecoverPass();
        });
        mLoginBtn.setOnClickListener(view -> {
            String email = mEmailEt.getText().toString().trim();
            String password = mPasswordEt.getText().toString().trim();
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                mEmailEt.setError("Invalid Email");
                mEmailEt.setFocusable(true);
            } else if (email.isEmpty()) {
                mEmailEt.setError("Email is not vacant");
                mEmailEt.setFocusable(true);
            } else if (password.isEmpty()) {
                mEmailEt.setError("Password is not vacant");
                mEmailEt.setFocusable(true);
            } else {
                loginUser(email, password);
            }
        });
        mGoogleLoginBtn.setOnClickListener(view -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });
    }

    private void showRecoverPass() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Recover Password");
        LinearLayout linearLayout = new LinearLayout(this);
        EditText emailEt = new EditText(this);
        emailEt.setHint("Email");
        emailEt.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        emailEt.setMinEms(10);
        linearLayout.addView(emailEt);
        linearLayout.setPadding(10, 10, 10, 10);
        builder.setView(linearLayout);
        builder.setPositiveButton("Recover", (dialogInterface, i) -> {
            String email = emailEt.getText().toString().trim();
            String password = mPasswordEt.getText().toString().trim();
            beginRecover(email, password);
        });
        builder.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss());
        builder.create().show();
    }

    private void beginRecover(String email, String password) {
        pd.setMessage("Sending email...");
        pd.show();
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            pd.dismiss();
            if (task.isSuccessful()) {
                Toast.makeText(LoginActivity.this, "Email sent", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(LoginActivity.this, "Failed...", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            pd.dismiss();
            Toast.makeText(LoginActivity.this, "Failed...", Toast.LENGTH_SHORT).show();
        });
    }

    private void loginUser(String email, String password) {
        pd.setMessage("Logging In...");
        pd.show();
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                pd.dismiss();
                FirebaseUser user = mAuth.getCurrentUser();
                Intent intent = new Intent(LoginActivity.this, DashBoardActivity.class);
                startActivity(intent);
                finish();
            } else {
                pd.dismiss();
                Toast.makeText(LoginActivity.this, "failed", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> pd.dismiss());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if(task.getResult().getAdditionalUserInfo().isNewUser()){
                            String email = user.getEmail();
                            String uid = user.getUid();
                            HashMap<Object,String> hashMap = new HashMap<>();
                            hashMap.put("email",email);
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
                        }
                        Toast.makeText(LoginActivity.this, " " + user.getEmail(), Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "signInWithCredential:success");
                        Intent intent = new Intent(LoginActivity.this, DashBoardActivity.class);
                        startActivity(intent);
                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(LoginActivity.this, "Login failed", Toast.LENGTH_SHORT).show();
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                    }
                }).addOnFailureListener(e -> Toast.makeText(LoginActivity.this, "Alo >>>" + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}