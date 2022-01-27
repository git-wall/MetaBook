package com.example.meta.Adapter;

import static com.example.meta.Other.StringUtil.FB_URL;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meta.Model.ModelComment;
import com.example.meta.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterComments extends RecyclerView.Adapter<AdapterComments.MyHolder> {
    Context context;
    List<ModelComment> commentList;
    String myUid, postId;
    String comment;


    public AdapterComments(Context context, List<ModelComment> commentList, String myUid, String postId) {
        this.context = context;
        this.commentList = commentList;
        this.myUid = myUid;
        this.postId = postId;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_comments, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        String uid = commentList.get(position).getUid();
        String name = commentList.get(position).getuName();
        String email = commentList.get(position).getuEmail();
        String image = commentList.get(position).getuDp();
        String cId = commentList.get(position).getcId();
        comment = commentList.get(position).getComment();
        String timestamp = commentList.get(position).getTimestamp();
        String like = commentList.get(position).getLike();
        //convert time
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(timestamp));
        String pTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();
        //set data
        holder.nameTv.setText(name);
        holder.commentTv.setText(comment);
        holder.timeTv.setText(pTime);
        try {
            Picasso.get().load(image).placeholder(R.drawable.sin).into(holder.avatarTv);
        } catch (Exception e) {
            e.printStackTrace();
        }

        holder.itemView.setOnLongClickListener(view -> {
            if (myUid.equals(uid)) {
                Dialog dialog = new Dialog(view.getRootView().getContext());
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.bottomsheet);
                TextView copy = dialog.findViewById(R.id.copy);
                TextView edit = dialog.findViewById(R.id.edit);
                TextView delete = dialog.findViewById(R.id.delete);
                copy.setOnClickListener(view1 -> {
                    ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("TextView", comment);
                    clipboard.setPrimaryClip(clip);
                    dialog.dismiss();
                    Toast.makeText(context, "Copy successful", Toast.LENGTH_SHORT).show();
                });
                edit.setOnClickListener(view2 -> {
                    dialog.dismiss();
                    AlertDialog.Builder builder = new AlertDialog.Builder(view2.getRootView().getContext());
                    builder.setTitle("Update comment");
                    LinearLayout linearLayout = new LinearLayout(view2.getRootView().getContext());
                    linearLayout.setOrientation(LinearLayout.VERTICAL);
                    linearLayout.setPadding(10, 10, 10, 10);
                    EditText editText = new EditText(view2.getRootView().getContext());
                    editText.setText(comment);
                    linearLayout.addView(editText);
                    builder.setView(linearLayout);
                    builder.setPositiveButton("Update", (dialogInterface, i) -> {
                        String value = editText.getText().toString().trim();
                        if (!TextUtils.isEmpty(value)) {
                            HashMap<String, Object> result = new HashMap<>();
                            result.put("comment", value);
                            final DatabaseReference ref = FirebaseDatabase.getInstance(FB_URL).getReference("Posts").child(postId);
                            ref.child("Comments").child(cId).updateChildren(result).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    dialog.dismiss();
                                    Toast.makeText(context, "Update success", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    dialog.dismiss();
                                }
                            });
                        }
                    });
                    builder.create().show();
                });
                delete.setOnClickListener(view1 -> {
                    deleteComment(cId);
                    dialog.dismiss();
                });
                dialog.show();
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                dialog.getWindow().setGravity(Gravity.BOTTOM);

            } else {

            }
            return true;
        });
    }

    private void deleteComment(String cId) {
        final DatabaseReference ref = FirebaseDatabase.getInstance(FB_URL).getReference("Posts").child(postId);
        ref.child("Comments").child(cId).removeValue();
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String comments = "" + snapshot.child("pComments").getValue();
                int newCommentVal = Integer.parseInt(comments) + 1;
                ref.child("pComments").setValue("" + newCommentVal);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder {
        CircleImageView avatarTv;
        TextView nameTv, commentTv, timeTv;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            avatarTv = itemView.findViewById(R.id.avatarIv);
            nameTv = itemView.findViewById(R.id.nameTv);
            commentTv = itemView.findViewById(R.id.commentTv);
            timeTv = itemView.findViewById(R.id.timeTv);
        }
    }
}
