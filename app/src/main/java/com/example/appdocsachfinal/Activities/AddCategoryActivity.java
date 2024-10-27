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

        firebaseAuth= FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Doi chut");
        progressDialog.setCanceledOnTouchOutside(false);

        binding.btnsubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });
        binding.btnback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    private String category="";
    private void validateData() {

        category = binding.edttheloai.getText().toString().trim();
        if (TextUtils.isEmpty(category)){
            Toast.makeText(this, "Nhap the loai", Toast.LENGTH_SHORT).show();
        }else {
            addCategoryFireBase();
        }
    }

    private void addCategoryFireBase() {
        progressDialog.setMessage("Dang them the loai");
        progressDialog.show();
        long timestamp = System.currentTimeMillis();

        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("id",""+timestamp);
        hashMap.put("category",""+category);
        hashMap.put("timestamp",timestamp);
        hashMap.put("uid",""+firebaseAuth.getUid());

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.child(""+timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        Toast.makeText(AddCategoryActivity.this, "Them the loai thanh cong", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(AddCategoryActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}