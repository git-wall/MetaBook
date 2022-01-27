package com.example.meta.UI;

import static androidx.core.content.res.ResourcesCompat.getFont;
import static com.example.meta.Other.StringUtil.FB_URL;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.meta.Other.ToastV;
import com.example.meta.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.HashMap;

import www.sanju.motiontoast.MotionToast;

public class InformationUserActivity extends AppCompatActivity {
    TextView liveTv, emailTv, phoneTv, GenderTv, birthdayTv, RelationshipTv;
    TextView editLive, editRelationShip;
    ImageView editPhone, editGender, editBirthday;
    ActionBar actionBar;
    FirebaseUser user;
    FirebaseAuth firebaseAuth;
    DatabaseReference userDbRef;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    ProgressDialog pd;
    String uid, name, email, phone, gender, birthday, live, relationship;
    public String date;
    DatePickerDialog.OnDateSetListener listener;
    ToastV toastV = new ToastV();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information_user);
        actionBar = getSupportActionBar();
        actionBar.setTitle("Edit profile");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        pd = new ProgressDialog(this);
        firebaseDatabase = FirebaseDatabase.getInstance(FB_URL);
        databaseReference = firebaseDatabase.getReference("Users");
        Mirror();
        firebaseAuth = FirebaseAuth.getInstance();
        checkUserStatus();
        userDbRef = FirebaseDatabase.getInstance(FB_URL).getReference("Users");
        Query query = userDbRef.orderByChild("email").equalTo(email);
        query.addValueEventListener(new ValueEventListener() {
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
        editPhone.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Update phone number");
            LinearLayout linearLayout = new LinearLayout(this);
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            linearLayout.setPadding(10, 10, 10, 10);
            EditText editText = new EditText(this);
            editText.setHint("Enter new phone number");
            linearLayout.addView(editText);
            builder.setView(linearLayout);
            builder.setPositiveButton("Update", (dialogInterface, i) -> {
                String value = editText.getText().toString().trim();
                if (!TextUtils.isEmpty(value)) {
                    pd.show();
                    HashMap<String, Object> result = new HashMap<>();
                    result.put("phone", value);
                    databaseReference.child(uid).updateChildren(result).addOnSuccessListener(unused -> {
                        pd.dismiss();
                        toastV.Success(this,"Phone number","Update success");
                    }).addOnFailureListener(e -> {
                        pd.dismiss();
                        toastV.Failed(this,"Phone number","Update failed");
                    });
                } else {
                    toastV.Warning(this,"Phone number","Is empty");
                }
            }).setNegativeButton("Cancel", (dialogInterface, i) -> {

            });
            builder.create().show();
        });
        editLive.setOnClickListener(view -> {

        });
        editBirthday.setOnClickListener(view -> {
            Calendar calendar = Calendar.getInstance();
            final int year = calendar.get(Calendar.YEAR);
            final int month = calendar.get(Calendar.MONTH);
            final int day = calendar.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog dialog = new DatePickerDialog(this, android.R.style.Theme_Holo_Light_Dialog_MinWidth,listener,year,month,day);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.show();
            listener = (view1, year1, month1, day1) -> {
                month1 += 1;
                date = day1 + "/" + month1 + "/" + year1;
                HashMap<String, Object> result = new HashMap<>();
                result.put("birthday", date);
                databaseReference.child(uid).updateChildren(result).addOnSuccessListener(unused -> {
                    toastV.Success(this,"Birthday","Update success");

                }).addOnFailureListener(e -> {
                    toastV.Failed(this,"Birthday","Update failed");
                });
            };
        });
        editGender.setOnClickListener(view -> {
            String[] data = {"Man","Woman"};
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Update your gender");
            builder.setSingleChoiceItems(data, -1, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    GenderTv.setText(data[i]);
                    HashMap<String, Object> result = new HashMap<>();
                    result.put("gender", GenderTv.getText().toString().trim());
                    dialogInterface.dismiss();
                    databaseReference.child(uid).updateChildren(result).addOnSuccessListener(unused -> {
                        MotionToast.Companion.darkToast(InformationUserActivity.this,
                                "Birthday",
                                "Update success",
                                MotionToast.TOAST_SUCCESS,
                                MotionToast.GRAVITY_CENTER,
                                MotionToast.SHORT_DURATION,
                                getFont(InformationUserActivity.this, R.font.helvetica_regular));
                    }).addOnFailureListener(e -> {
                        MotionToast.Companion.darkToast(InformationUserActivity.this,
                                "Gender",
                                "Update failed",
                                MotionToast.TOAST_ERROR,
                                MotionToast.GRAVITY_CENTER,
                                MotionToast.SHORT_DURATION,
                                getFont(InformationUserActivity.this, R.font.helvetica_regular));
                    });
                }
            }).setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });
            builder.create().show();
        });
        editRelationShip.setOnClickListener(view -> {
            String[] data = {"Single","Dating","Married","Divorced","Relict"};
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Update your relationship");
            builder.setSingleChoiceItems(data, -1, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    RelationshipTv.setText(data[i]);
                    HashMap<String, Object> result = new HashMap<>();
                    result.put("relationship", RelationshipTv.getText().toString().trim());
                    dialogInterface.dismiss();
                    databaseReference.child(uid).updateChildren(result).addOnSuccessListener(unused -> {
                        Toast.makeText(InformationUserActivity.this, "Update success", Toast.LENGTH_SHORT).show();
                    }).addOnFailureListener(e -> {
                        Toast.makeText(InformationUserActivity.this, "Update failed", Toast.LENGTH_SHORT).show();
                    });
                }
            }).setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });
            builder.create().show();
        });
    }

    private void checkUserStatus() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            email = user.getEmail();
            uid = user.getUid();
        } else {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    private void Mirror() {
        liveTv = findViewById(R.id.liveTv);
        emailTv = findViewById(R.id.emailTv);
        phoneTv = findViewById(R.id.phoneTv);
        GenderTv = findViewById(R.id.GenderTv);
        birthdayTv = findViewById(R.id.birthdayTv);
        RelationshipTv = findViewById(R.id.RelationshipTv);
        editLive = findViewById(R.id.editLive);
        editRelationShip = findViewById(R.id.editRelationShip);
        editPhone = findViewById(R.id.editPhone);
        editGender = findViewById(R.id.editGender);
        editBirthday = findViewById(R.id.editBirthday);
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}