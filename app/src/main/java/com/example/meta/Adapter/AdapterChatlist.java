package com.example.meta.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meta.UI.ChatActivity;
import com.example.meta.Model.ModelUser;
import com.example.meta.R;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterChatlist extends RecyclerView.Adapter<AdapterChatlist.MyHolder> {
    Context context;
    List<ModelUser> userList;
    private HashMap<String, String> lastMessageMap;

    public AdapterChatlist(Context context, List<ModelUser> userList) {
        this.context = context;
        this.userList = userList;
        lastMessageMap = new HashMap<>();
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_chatlist, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        String hisUid = userList.get(position).getUid();
        String username = userList.get(position).getName();
        String userImage = userList.get(position).getImage();
        String lastMessage = lastMessageMap.get(hisUid);
        holder.nameTv.setText(username);
        if (lastMessage == null || lastMessage.equals("default")) {
            holder.lastMessageTv.setVisibility(View.GONE);
        } else {
            holder.lastMessageTv.setVisibility(View.VISIBLE);
            holder.lastMessageTv.setText(lastMessage);
        }
        try {
            Picasso.get().load(userImage).placeholder(R.drawable.sin).into(holder.profileTv);
        } catch (Exception e) {
            Picasso.get().load(R.drawable.sin).into(holder.profileTv);
        }
        if (userList.get(position).getOnlineStatus().equals("online")) {
            holder.onlineStatusIv.setImageResource(R.drawable.circle_online);
        } else {
            holder.onlineStatusIv.setImageResource(R.drawable.circle_offline);
        }
        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("hisUid", hisUid);
            context.startActivity(intent);
        });
    }
    public void setLastMessageMap(String userId, String lastMessage) {
        lastMessageMap.put(userId, lastMessage);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder {
        CircleImageView profileTv;
        ImageView  onlineStatusIv;
        TextView nameTv, lastMessageTv;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            profileTv = itemView.findViewById(R.id.profileTv);
            onlineStatusIv = itemView.findViewById(R.id.onlineStatusIv);
            nameTv = itemView.findViewById(R.id.nameTv);
            lastMessageTv = itemView.findViewById(R.id.lastMessageTv);
        }
    }
}
