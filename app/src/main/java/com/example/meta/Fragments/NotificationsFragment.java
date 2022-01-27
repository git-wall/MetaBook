package com.example.meta.Fragments;

import static com.example.meta.Other.StringUtil.FB_URL;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.meta.Adapter.AdapterNotification;
import com.example.meta.Model.ModelNotification;
import com.example.meta.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class NotificationsFragment extends Fragment {
    RecyclerView notificationRv;
    private FirebaseAuth firebaseAuth;
    private ArrayList<ModelNotification> notificationsList;
    private AdapterNotification adapterNotification;

    public NotificationsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);
        notificationRv = view.findViewById(R.id.notificationRv);
        firebaseAuth = FirebaseAuth.getInstance();
        getAllNotification();
        return view;
    }

    private void getAllNotification() {
        notificationsList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance(FB_URL).getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Notifications").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                notificationsList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ModelNotification model = ds.getValue(ModelNotification.class);
                    notificationsList.add(model);
                }
                adapterNotification = new AdapterNotification(getActivity(), notificationsList);
                notificationRv.setAdapter(adapterNotification);
                adapterNotification.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}