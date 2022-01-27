//package com.example.meta.notifications;
//
//import static android.content.ContentValues.TAG;
//import static com.example.meta.Other.StringUtil.FB_URL;
//
//import android.util.Log;
//
//import androidx.annotation.NonNull;
//
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseUser;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.messaging.FirebaseMessagingService;
//
//public class FirebaseService extends FirebaseMessagingService {
//
//    @Override
//    public void onNewToken(@NonNull String s) {
//        super.onNewToken(s);
//        Log.i(TAG,s);
//        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//        if (user != null){
//            updateToken(s);
//        }
//    }
//
//    private void updateToken(String token) {
//        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//        DatabaseReference ref = FirebaseDatabase.getInstance(FB_URL).getReference("Tokens");
//        Token token1 = new Token(token);
//        ref.child(user.getUid()).setValue(token1);
//    }
//}
