package com.example.appdocsachfinal.Activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.appdocsachfinal.databinding.ActivityAddCategoryBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class AddCategoryActivity extends AppCompatActivity {
    private ActivityAddCategoryBinding binding;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddCategoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        initProgressDialog();
        setButtonListeners();
    }

    private void initProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Đợi chút");
        progressDialog.setCanceledOnTouchOutside(false);
    }

    private void setButtonListeners() {
        binding.btnsubmit.setOnClickListener(v -> validateData());
        binding.btnback.setOnClickListener(v -> finish());
    }

    private void validateData() {
        String category = binding.edttheloai.getText().toString().trim();
        if (TextUtils.isEmpty(category)) {
            showToast("Nhập thể loại");
        } else {
            addCategoryToFirebase(category);
        }
    }

    private void addCategoryToFirebase(String category) {
        progressDialog.setMessage("Đang thêm thể loại");
        progressDialog.show();

        long timestamp = System.currentTimeMillis();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("id", String.valueOf(timestamp));
        hashMap.put("category", category);
        hashMap.put("timestamp", timestamp);
        hashMap.put("uid", firebaseAuth.getUid());

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.child(String.valueOf(timestamp))
                .setValue(hashMap)
                .addOnSuccessListener(unused -> {
                    progressDialog.dismiss();
                    showToast("Thêm thể loại thành công");
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    showToast(e.getMessage());
                });
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
