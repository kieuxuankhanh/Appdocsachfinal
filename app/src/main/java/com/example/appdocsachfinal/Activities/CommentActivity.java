package com.example.appdocsachfinal.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.appdocsachfinal.Adapter.AdapterComment;
import com.example.appdocsachfinal.Adapter.AdapterCommentDetails;
import com.example.appdocsachfinal.Model.ModelComment;
import com.example.appdocsachfinal.databinding.ActivityCommentBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class CommentActivity extends AppCompatActivity {
    private ActivityCommentBinding binding;
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;
    private String bookId,commentId;
    private static final String BOOKS_REF = "Books";
    private static final String COMMENTS_REF = "Comments";
    private ArrayList<ModelComment> commentArrayList;
    private AdapterComment adapterComment;
    private AdapterCommentDetails adapterCommentDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCommentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initializeComponents();
        setupClickListeners();
        loadComments();
    }
    //tải bình luận và hiển thị
    private void loadComments() {
        commentArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId).child("Comments")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        commentArrayList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()){
                            ModelComment modelComment = ds.getValue(ModelComment.class);
                            commentArrayList.add(modelComment);
                        }
                        adapterComment = new AdapterComment(CommentActivity.this, commentArrayList);
                        binding.Rcvcomment.setAdapter(adapterComment);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


    private void initializeComponents() {
        firebaseAuth = FirebaseAuth.getInstance();
        Intent intent = getIntent();
        bookId = intent.getStringExtra("bookId");
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Đợi 1 chút nha!!!");
        progressDialog.setMessage("Thêm bình luận...");
        progressDialog.setCanceledOnTouchOutside(false);
    }

    private void setupClickListeners() {
        binding.btnback.setOnClickListener(v -> onBackPressed());

        binding.btncmt.setOnClickListener(v -> {
            String comment = binding.edtcmt.getText().toString().trim();
            if (TextUtils.isEmpty(comment)) {
                binding.edtcmt.setError("Vui lòng nhập bình luận");
                binding.edtcmt.requestFocus();
            } else {
                addComment(comment);
            }
        });
    }
    //đẩy bình luận lên firebase
    private void addComment(String comment) {
        progressDialog.show();

        String timestamp = String.valueOf(System.currentTimeMillis());
        String uid = firebaseAuth.getUid();

        HashMap<String, Object> commentData = new HashMap<>();
        commentData.put("id", timestamp);
        commentData.put("bookId", bookId);
        commentData.put("timestamp", timestamp);
        commentData.put("comment", comment);
        commentData.put("uid", uid);

        DatabaseReference reference = FirebaseDatabase.getInstance()
                .getReference(BOOKS_REF)
                .child(bookId)
                .child(COMMENTS_REF)
                .child(timestamp);

        reference.setValue(commentData)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(CommentActivity.this, "Bình luận đăng thành công", Toast.LENGTH_SHORT).show();
                    binding.edtcmt.setText("");
                    progressDialog.dismiss();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(CommentActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}