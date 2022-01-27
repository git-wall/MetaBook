package com.example.meta.Adapter;

import static com.example.meta.Other.StringUtil.FB_URL;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meta.UI.AddPostActivity;
import com.example.meta.Model.ModelPost;
import com.example.meta.UI.PostDetailActivity;
import com.example.meta.UI.PostLikedByActivity;
import com.example.meta.R;
import com.example.meta.UI.ThereProfileActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterPosts extends RecyclerView.Adapter<AdapterPosts.myHolder> {
    Context context;
    List<ModelPost> postList;
    String myUid;
    private final DatabaseReference likeRef;
    private final DatabaseReference postRef;
    boolean mProcessLike = false;

    public AdapterPosts(Context context, List<ModelPost> postList) {
        this.context = context;
        this.postList = postList;
        myUid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        likeRef = FirebaseDatabase.getInstance(FB_URL).getReference().child("Likes");
        postRef = FirebaseDatabase.getInstance(FB_URL).getReference().child("Posts");
    }

    @NonNull
    @Override
    public myHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_posts, parent, false);
        return new myHolder(view);
    }

    @SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
    @Override
    public void onBindViewHolder(@NonNull myHolder holder, int position) {
        String uid = postList.get(position).getUid();
        String uName = postList.get(position).getuName();
        String uEmail = postList.get(position).getuEmail();
        String uDp = postList.get(position).getuDp();
        final String pId = postList.get(position).getpId();
        String pTitle = postList.get(position).getpTitle();
        String pDescription = postList.get(position).getpDescr();
        final String pImage = postList.get(position).getpImage();
        String pTimeStamp = postList.get(position).getpTime();
        String pLikes = postList.get(position).getpLikes();
        String pComments = postList.get(position).getpComments();
        //convert timestamp to under time
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(pTimeStamp));
        String pTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();
        holder.uNameTv.setText(uName);
        holder.pTimeTv.setText(pTime);
        holder.pTitleTv.setText(pTitle);
        holder.pDescriptionTv.setText(pDescription);
        holder.pLikeTv.setText(pLikes + "Likes");
        holder.pCommentsTv.setText(pComments + "Comments");
        setLikes(holder, pId);
        // set user dp
        try {
            Picasso.get().load(uDp).placeholder(R.drawable.sin).into(holder.uPictureTv);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (pImage.equals("noImage")) {
            holder.pImageIv.setVisibility(View.GONE);
        } else {
            holder.pImageIv.setVisibility(View.VISIBLE);
            try {
                Picasso.get().load(pImage).placeholder(R.drawable.sin).into(holder.pImageIv);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //handler button
        holder.moreBtn.setOnClickListener(view -> showMoreOptions(holder.moreBtn, uid, myUid, pId, pImage));
        holder.likeBtn.setOnClickListener(view -> {
            int pLike = Integer.parseInt(postList.get(position).getpLikes());
            mProcessLike = true;
            String postIde = postList.get(position).getpId();
            likeRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (mProcessLike) {
                        if (snapshot.child(postIde).hasChild(myUid)) {
                            postRef.child(postIde).child("pLikes").setValue("" + (pLike - 1));
                            likeRef.child(postIde).child(myUid).removeValue();
                            mProcessLike = false;
                        } else {
                            postRef.child(postIde).child("pLikes").setValue("" + (pLike + 1));
                            likeRef.child(postIde).child(myUid).setValue("Liked");
                            mProcessLike = false;
                            addToHisNotification(""+uid,""+pId,"Liked your post");
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        });
        holder.commentBtn.setOnClickListener(view -> {
            Intent intent = new Intent(context, PostDetailActivity.class);
            intent.putExtra("postId", pId);
            context.startActivity(intent);
        });
        holder.shareBtn.setOnClickListener(view -> {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) holder.pImageIv.getDrawable();
            if (bitmapDrawable == null) {
                shareTextOnly(pTitle, pDescription);
            } else {
                Bitmap bitmap = bitmapDrawable.getBitmap();
                shareImageAndText(pTitle, pDescription, bitmap);
            }
        });
        holder.profileLayout.setOnClickListener(view -> {
            Intent intent = new Intent(context, ThereProfileActivity.class);
            intent.putExtra("uid", uid);
            context.startActivity(intent);
        });
        holder.pLikeTv.setOnClickListener(view -> {
            Intent intent = new Intent(context, PostLikedByActivity.class);
            intent.putExtra("postId",pId);
            context.startActivity(intent);
        });
    }

    private void addToHisNotification(String hisUid, String pId, String notification) {
        String timestamp = "" + System.currentTimeMillis();
        HashMap<Object, String> hashMap = new HashMap<>();
        hashMap.put("pId", pId);
        hashMap.put("timestamp", timestamp);
        hashMap.put("pUid", hisUid);
        hashMap.put("notification", notification);
        hashMap.put("sUid", myUid);
        DatabaseReference ref = FirebaseDatabase.getInstance(FB_URL).getReference("Users");
        ref.child(hisUid).child("Notifications").child(timestamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    private void shareTextOnly(String pTitle, String pDescription) {
        String shareBody = pTitle + "\n" + pDescription;
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Subject Here");
        intent.putExtra(Intent.EXTRA_TEXT, shareBody);
        context.startActivity(Intent.createChooser(intent, "Share Via"));
    }

    private void shareImageAndText(String pTitle, String pDescription, Bitmap bitmap) {
        String shareBody = pTitle + "\n" + pDescription;
        //save image in cache
        Uri uri = saveImageToShare(bitmap);
        Intent sIntent = new Intent(Intent.ACTION_SEND);
        sIntent.putExtra(Intent.EXTRA_STREAM, uri);
        sIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
        sIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject Here");
        sIntent.setType("image/png");
        context.startActivity(Intent.createChooser(sIntent, "Share Via"));
    }

    private Uri saveImageToShare(Bitmap bitmap) {
        File imageFolder = new File(context.getCacheDir(), "images");
        Uri uri = null;
        try {
            imageFolder.mkdir();//create if not exits
            File file = new File(imageFolder, "share_image.png");
            FileOutputStream stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream);
            stream.flush();
            stream.close();
            uri = FileProvider.getUriForFile(context, "com.blogspot.atifsoftwares.firebaseapp.fileprovider", file);
        } catch (Exception e) {
            Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return uri;
    }

    private void setLikes(myHolder holder, String postKey) {
        likeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(postKey).hasChild(myUid)) {
                    holder.likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.like_blue, 0, 0, 0);
                    holder.likeBtn.setText("Liked");
                } else {
                    holder.likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.like24, 0, 0, 0);
                    holder.likeBtn.setText("Like");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void showMoreOptions(ImageButton moreBtn, String uid, String myUid, String pId, String pImage) {
        PopupMenu popupMenu = new PopupMenu(context, moreBtn, Gravity.END);
        if (uid.equals(myUid)) {
            popupMenu.getMenu().add(Menu.NONE, 0, 0, "Edit");
            popupMenu.getMenu().add(Menu.NONE, 1, 0, "Delete");
        }
        popupMenu.getMenu().add(Menu.NONE, 2, 0, "View Detail");
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int id = menuItem.getItemId();
                if (id == 1) {
                    beginDelete(pId, pImage);
                } else if (id == 0) {
                    Intent intent = new Intent(context, AddPostActivity.class);
                    intent.putExtra("key", "editPost");
                    intent.putExtra("editPostId", pId);
                    context.startActivity(intent);
                } else if (id == 2) {
                    Intent intent = new Intent(context, PostDetailActivity.class);
                    intent.putExtra("postId", pId);
                    context.startActivity(intent);
                }
                return false;
            }
        });
        popupMenu.show();
    }

    private void beginDelete(String pId, String pImage) {
        if (pImage.equals("noImage")) {
            deleteWithoutImage(pId, pImage);
        } else {
            deleteWithImage(pId, pImage);
        }
    }

    private void deleteWithImage(String pId, String pImage) {
        final ProgressDialog pd = new ProgressDialog(context);
        pd.setMessage("Deleting...");
        StorageReference picRef = FirebaseStorage.getInstance().getReferenceFromUrl(pImage);
        picRef.delete().addOnSuccessListener(unused -> {
            Query query = FirebaseDatabase.getInstance(FB_URL).getReference("Posts").orderByChild("pId").equalTo(pId);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        ds.getRef().removeValue();
                        pd.dismiss();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }).addOnFailureListener(e -> {
            pd.dismiss();
            Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void deleteWithoutImage(String pId, String pImage) {
        final ProgressDialog pd = new ProgressDialog(context);
        pd.setMessage("Deleting...");
        Query query = FirebaseDatabase.getInstance(FB_URL).getReference("Posts").orderByChild("pId").equalTo(pId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ds.getRef().removeValue();
                    pd.dismiss();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    class myHolder extends RecyclerView.ViewHolder {
        CircleImageView uPictureTv;
        ImageView pImageIv;
        TextView uNameTv, pTimeTv, pTitleTv, pDescriptionTv, pLikeTv, pCommentsTv;
        ImageButton moreBtn;
        Button likeBtn, commentBtn, shareBtn;
        LinearLayout profileLayout;

        public myHolder(@NonNull View itemView) {
            super(itemView);
            uPictureTv = itemView.findViewById(R.id.uPictureTv);
            pImageIv = itemView.findViewById(R.id.pImageIv);
            uNameTv = itemView.findViewById(R.id.uNameTv);
            pTimeTv = itemView.findViewById(R.id.pTimeTv);
            pTitleTv = itemView.findViewById(R.id.pTitleTv);
            pDescriptionTv = itemView.findViewById(R.id.pDescriptionTv);
            pLikeTv = itemView.findViewById(R.id.pLikeTv);
            pCommentsTv = itemView.findViewById(R.id.pCommentsTv);
            moreBtn = itemView.findViewById(R.id.moreBtn);
            likeBtn = itemView.findViewById(R.id.likeBtn);
            commentBtn = itemView.findViewById(R.id.commentBtn);
            shareBtn = itemView.findViewById(R.id.shareBtn);
            profileLayout = itemView.findViewById(R.id.profileLayout);

        }
    }
}
