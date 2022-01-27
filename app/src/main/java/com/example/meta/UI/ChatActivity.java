package com.example.meta.UI;

import static androidx.core.content.res.ResourcesCompat.getFont;
import static com.example.meta.Other.StringUtil.FB_URL;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.meta.Adapter.AdapterChat;
import com.example.meta.Model.ModelChat;
import com.example.meta.Model.ModelUser;
import com.example.meta.Other.ToastV;
import com.example.meta.R;
import com.example.meta.notifications.Data;
import com.example.meta.notifications.Sender;
import com.example.meta.notifications.Token;
import com.github.drjacky.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import www.sanju.motiontoast.MotionToast;

public class ChatActivity extends AppCompatActivity {
    Toolbar toolbar;
    RecyclerView recyclerView;
    CircleImageView profileTv;
    TextView nameTv, userStatusTv, his_typing;
    EditText messageEt;
    ImageButton sendBtn, attachBtn, micBtn;
    ImageView blockIv;

    String hisUid;
    String myUid;
    String hisImage;
    boolean isBlocked = false;

    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference userDbRef;
    ValueEventListener seenListener;
    DatabaseReference userRefForSeen;

    List<ModelChat> chatList;
    AdapterChat adapterChat;

    ToastV v;

    //volley request queue for notification
    private RequestQueue requestQueue;
    private boolean notify = false;
    private final int RQ_CODE_SPEECH_INPUT = 1234;
    Uri imgUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        toolbar = findViewById(R.id.toolbar1);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");
        recyclerView = findViewById(R.id.chat_recyclerView);
        profileTv = findViewById(R.id.profileTv);
        nameTv = findViewById(R.id.nameTv);
        userStatusTv = findViewById(R.id.userStatusTv);
        messageEt = findViewById(R.id.messageEt);
        sendBtn = findViewById(R.id.sendBtn);
        his_typing = findViewById(R.id.his_typing);
        attachBtn = findViewById(R.id.attachBtn);
        blockIv = findViewById(R.id.blockIv);
        micBtn = findViewById(R.id.mic);
        requestQueue = Volley.newRequestQueue(getApplicationContext());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        // create api service
//        apiService = Client.getRetrofit("https://fcm.googleapis.com/").create(APIService.class);
        Intent intent = getIntent();
        hisUid = intent.getStringExtra("hisUid");
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance(FB_URL);
        userDbRef = firebaseDatabase.getReference("Users");
        Query query = userDbRef.orderByChild("uid").equalTo(hisUid);
        query.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String name = "" + ds.child("name").getValue();
                    hisImage = "" + ds.child("image").getValue();
                    String typingStatus = "" + ds.child("typingTo").getValue();
                    //check typing
                    if (typingStatus.equals(myUid)) {
                        his_typing.setVisibility(View.VISIBLE);
                        his_typing.setText(name + " typing...");
                    } else if (typingStatus.equals("noOne")) {
                        his_typing.setVisibility(View.GONE);
                    }
                    //get value online
                    String onlineStatus = "" + ds.child("onlineStatus").getValue();
                    if (onlineStatus.equals("online")) {
                        userStatusTv.setText(onlineStatus);
                    } else {
                        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
                        cal.setTimeInMillis(Long.parseLong(onlineStatus));
                        String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa", cal).toString();
                        userStatusTv.setText("Last seen at: " + dateTime);
                    }
                    // set data
                    nameTv.setText(name);
                    try {
                        Picasso.get().load(hisImage).placeholder(R.drawable.sin).into(profileTv);
                    } catch (Exception e) {
                        Picasso.get().load(R.drawable.sin).into(profileTv);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        attachBtn.setOnClickListener(view -> {
            getImage();
        });
        micBtn.setOnClickListener(view -> {
            SpeedToText();
        });
        sendBtn.setOnClickListener(view -> {
            notify = true;
            String message = messageEt.getText().toString().trim();
            if (TextUtils.isEmpty(message)) {
                MotionToast.Companion.darkToast(ChatActivity.this,
                        "Message",
                        "Can not send empty",
                        MotionToast.TOAST_WARNING,
                        MotionToast.GRAVITY_CENTER,
                        MotionToast.SHORT_DURATION,
                        getFont(ChatActivity.this, R.font.helvetica_regular));
            } else {
                sendMessage(message);
            }
            messageEt.setText("");
        });
        messageEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() == 0) {
                    checkTypingStatus("noOne");
                    sendBtn.setEnabled(false);
                    sendBtn.setImageResource(R.drawable.ic_send_black_24dp);
                } else {
                    checkTypingStatus(hisUid);
                    sendBtn.setEnabled(true);
                    sendBtn.setImageResource(R.drawable.ic_send_blue_24dp);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                checkTypingStatus("noOne");
            }
        });
        blockIv.setOnClickListener(view -> {
            if (isBlocked) {
                unBlockUser();
            } else {
                blockUser();
            }
        });
        checkIsBlocked();
        readMessages();
        seenMessages();
    }

    private void SpeedToText() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Listening /(^0^)/");
        try {
            startActivityForResult(intent, RQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException e) {
            v.Warning(ChatActivity.this, "Warning", "Sorry! Your device doesn't support speech input");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RQ_CODE_SPEECH_INPUT:
                if (requestCode == RESULT_OK && data != null) {
                    List<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String text = result.get(0);
                    messageEt.setText(text);
                }
                break;
        }
    }

    private void checkIsBlocked() {
        DatabaseReference ref = FirebaseDatabase.getInstance(FB_URL).getReference("Users");
        ref.child(firebaseAuth.getUid()).child("BlockedUsers").orderByChild("uid").equalTo(hisUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            if (ds.exists()) {
                                blockIv.setImageResource(R.drawable.ic_baseline_block_24);
                                isBlocked = true;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void blockUser() {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("uid", hisUid);
        DatabaseReference ref = FirebaseDatabase.getInstance(FB_URL).getReference("Users");
        ref.child(myUid).child("BlockedUsers").child(hisUid).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                MotionToast.Companion.darkToast(ChatActivity.this,
                        "Block user",
                        "Block success !!!",
                        MotionToast.TOAST_SUCCESS,
                        MotionToast.GRAVITY_CENTER,
                        MotionToast.SHORT_DURATION,
                        getFont(ChatActivity.this, R.font.helvetica_regular));
                blockIv.setImageResource(R.drawable.ic_baseline_block_24);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                MotionToast.Companion.darkToast(ChatActivity.this,
                        "Block user",
                        "Block failed !!!",
                        MotionToast.TOAST_ERROR,
                        MotionToast.GRAVITY_CENTER,
                        MotionToast.SHORT_DURATION,
                        getFont(ChatActivity.this, R.font.helvetica_regular));
            }
        });
    }

    private void unBlockUser() {
        DatabaseReference ref = FirebaseDatabase.getInstance(FB_URL).getReference("Users");
        ref.child(myUid).child("BlockedUsers").orderByChild("uid").equalTo(hisUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            if (ds.exists()) {
                                ds.getRef().removeValue()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                MotionToast.Companion.darkToast(ChatActivity.this,
                                                        "Block user",
                                                        "Unblock success !!!",
                                                        MotionToast.TOAST_SUCCESS,
                                                        MotionToast.GRAVITY_CENTER,
                                                        MotionToast.SHORT_DURATION,
                                                        getFont(ChatActivity.this, R.font.helvetica_regular));
                                                blockIv.setImageResource(R.drawable.ic_check_green);
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        MotionToast.Companion.darkToast(ChatActivity.this,
                                                "Block user",
                                                "Unblock failed !!!",
                                                MotionToast.TOAST_ERROR,
                                                MotionToast.GRAVITY_CENTER,
                                                MotionToast.SHORT_DURATION,
                                                getFont(ChatActivity.this, R.font.helvetica_regular));
                                    }
                                });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void getImage() {
        ImagePicker.Companion.with(this)
                .crop()
                .cropOval()
                .maxResultSize(512, 512, true)
                .createIntentFromDialog(new Function1() {
                    public Object invoke(Object var1) {
                        this.invoke((Intent) var1);
                        return Unit.INSTANCE;
                    }

                    public final void invoke(@NotNull Intent it) {
                        Intrinsics.checkNotNullParameter(it, "it");
                        launcher.launch(it);
                    }
                });
    }

    ActivityResultLauncher<Intent> launcher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), (ActivityResult result) -> {
                if (result.getResultCode() == RESULT_OK) {
                    assert result.getData() != null;
                    imgUri = result.getData().getData();
                    if (imgUri == null) {
                        return;
                    } else {
                        try {
                            sendImageMessage(imgUri);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else if (result.getResultCode() == ImagePicker.RESULT_ERROR) {
                    ImagePicker.Companion.getError(result.getData());
                }
            });

    private void sendImageMessage(Uri imgUri) throws IOException {
        notify = true;
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Sending image...");
        progressDialog.show();
        String timeStamp = "" + System.currentTimeMillis();
        String fileNameAndPath = "ChatImages/" + "post_" + timeStamp;
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imgUri);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();
        StorageReference ref = FirebaseStorage.getInstance().getReference().child(fileNameAndPath);
        ref.putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                progressDialog.dismiss();
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful()) ;
                String downloadUri = uriTask.getResult().toString();
                if (uriTask.isSuccessful()) {
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance(FB_URL).getReference();
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("sender", myUid);
                    hashMap.put("receiver", hisUid);
                    hashMap.put("message", downloadUri);
                    hashMap.put("timestamp", timeStamp);
                    hashMap.put("type", "image");
                    hashMap.put("isSeen", false);
                    databaseReference.child("Chats").push().setValue(hashMap);
                    DatabaseReference database = FirebaseDatabase.getInstance(FB_URL).getReference("Users").child(myUid);
                    database.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            ModelUser user = snapshot.getValue(ModelUser.class);
                            if (notify) {
                                sendNotification(hisUid, user.getName(), "Sent you a photo");
                            }
                            notify = false;
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                    DatabaseReference chatRef1 = FirebaseDatabase.getInstance(FB_URL).getReference("Chatlist")
                            .child(myUid)
                            .child(hisUid);
                    chatRef1.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (!snapshot.exists()) {
                                chatRef1.child("id").setValue(hisUid);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                    DatabaseReference chatRef2 = FirebaseDatabase.getInstance(FB_URL).getReference("Chatlist")
                            .child(hisUid)
                            .child(myUid);
                    chatRef2.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (!snapshot.exists()) {
                                chatRef2.child("id").setValue(hisUid);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
            }
        });

    }

    private void seenMessages() {
        userRefForSeen = FirebaseDatabase.getInstance(FB_URL).getReference("Chats");
        seenListener = userRefForSeen.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ModelChat chat = ds.getValue(ModelChat.class);
                    if (chat.getReceiver().equals(myUid) && chat.getSender().equals(hisUid)) {
                        HashMap<String, Object> hasSeenHashMap = new HashMap<>();
                        hasSeenHashMap.put("isSeen", true);
                        ds.getRef().updateChildren(hasSeenHashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void readMessages() {
        chatList = new ArrayList<>();
        DatabaseReference dbRef = FirebaseDatabase.getInstance(FB_URL).getReference("Chats");
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ModelChat chat = ds.getValue(ModelChat.class);
                    if (chat.getReceiver().equals(myUid) && chat.getSender().equals(hisUid)
                            || chat.getReceiver().equals(hisUid) && chat.getSender().equals(myUid)) {
                        chatList.add(chat);
                    }
                    adapterChat = new AdapterChat(ChatActivity.this, chatList, hisImage);
                    adapterChat.notifyDataSetChanged();
                    recyclerView.setAdapter(adapterChat);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendMessage(String message) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance(FB_URL).getReference();
        String timestamp = String.valueOf(System.currentTimeMillis());
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", myUid);
        hashMap.put("receiver", hisUid);
        hashMap.put("message", message);
        hashMap.put("timestamp", timestamp);
        hashMap.put("isSeen", false);
        hashMap.put("type", "text");
        databaseReference.child("Chats").push().setValue(hashMap);
        final DatabaseReference database = FirebaseDatabase.getInstance(FB_URL).getReference("Users").child(myUid);
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ModelUser user = snapshot.getValue(ModelUser.class);
                if (notify) {
                    sendNotification(hisUid, user.getName(), message);
                }
                notify = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        DatabaseReference chatRef1 = FirebaseDatabase.getInstance(FB_URL).getReference("Chatlist")
                .child(myUid)
                .child(hisUid);
        chatRef1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    chatRef1.child("id").setValue(hisUid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        DatabaseReference chatRef2 = FirebaseDatabase.getInstance(FB_URL).getReference("Chatlist")
                .child(hisUid)
                .child(myUid);
        chatRef2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    chatRef2.child("id").setValue(hisUid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendNotification(final String hisUid, final String name, final String message) {
        DatabaseReference allTokens = FirebaseDatabase.getInstance(FB_URL).getReference("Tokens");
        Query query = allTokens.orderByKey().equalTo(hisUid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Token token = ds.getValue(Token.class);
                    Data data = new Data("" + myUid,
                            name + ": " + message,
                            "New Message",
                            "" + hisUid,
                            "ChatNotification",
                            R.mipmap.ic_launcher);
                    Sender sender = new Sender(data, token.getToken());
                    //fcm json object request
                    try {
                        JSONObject senderJsonObj = new JSONObject(new Gson().toJson(sender));
                        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", senderJsonObj, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Log.d("JSON_RESPONSE", "onResponse:" + response.toString());
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.d("JSON_RESPONSE", "onResponse:" + error.toString());
                            }
                        }) {
                            @Override
                            public Map<String, String> getHeaders() throws AuthFailureError {
                                Map<String, String> headers = new HashMap<>();
                                headers.put("Content-Type", "application/json");
                                headers.put("Authorization", "key=AAAASBORu3s:APA91bFF8WdfKHNcEPAZ9ADWlPBnZvm1FycqhvOw-Oij1mYCpj2xxyLcOKxQBvwQ9TLTB_tNJfqtDLV7TOjZFZEv1u2d5CKDI_TDt_9oQSTmihknE7OU-8dvzyt_YoBF_G8dpPXMy7eH");

                                return headers;
                            }
                        };
                        requestQueue.add(jsonObjectRequest);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
//                    apiService.sendNotification(sender)
//                            .enqueue(new Callback<Response>() {
//                                @Override
//                                public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
//                                    Toast.makeText(ChatActivity.this, "" + response.message(), Toast.LENGTH_SHORT).show();
//                                }
//
//                                @Override
//                                public void onFailure(Call<Response> call, Throwable t) {
//
//                                }
//                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onStart() {
        checkUserStatus();
        checkOnlineStatus("online");
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        String timestamp = String.valueOf(System.currentTimeMillis());
        checkOnlineStatus(timestamp);
        checkTypingStatus("noOne");
        userRefForSeen.removeEventListener(seenListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        String timestamp = String.valueOf(System.currentTimeMillis());
        checkOnlineStatus(timestamp);
        checkTypingStatus("noOne");
        userRefForSeen.removeEventListener(seenListener);
    }

    @Override
    protected void onResume() {
        checkOnlineStatus("online");
        super.onResume();
    }

    private void checkUserStatus() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            myUid = user.getUid();
        } else {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    private void checkOnlineStatus(String status) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance(FB_URL).getReference("Users").child(myUid);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("onlineStatus", status);
        dbRef.updateChildren(hashMap);
    }

    private void checkTypingStatus(String typing) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance(FB_URL).getReference("Users").child(myUid);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("typingTo", typing);
        dbRef.updateChildren(hashMap);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.action_search).setVisible(false);
        menu.findItem(R.id.action_add_post).setVisible(false);
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
}