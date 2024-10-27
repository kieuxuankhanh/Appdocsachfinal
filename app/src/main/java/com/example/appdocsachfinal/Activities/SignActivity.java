package com.example.appdocsachfinal.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.appdocsachfinal.databinding.ActivitySignBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class SignActivity extends AppCompatActivity {

    private ActivitySignBinding binding;

    private FirebaseAuth firebaseAuth;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Doi ty");
        progressDialog.setCanceledOnTouchOutside(false);

        binding.btnsign1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });
        binding.btnlogin1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
    private  String name="",email="",password="";

    private void validateData() {

        name = binding.edtname.getText().toString().trim();
        email = binding.edtuser.getText().toString().trim();
        password = binding.edtpass.getText().toString().trim();
        String cPassword = binding.edtpass2.getText().toString().trim();
        if (TextUtils.isEmpty(name)){
            Toast.makeText(this, "Nhap ten?", Toast.LENGTH_SHORT).show();

        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Nhap lai email", Toast.LENGTH_SHORT).show();
            
        } else if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Nhap ten?", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(cPassword)) {
            Toast.makeText(this, "Nhap ten?", Toast.LENGTH_SHORT).show();
        } else if (!password.equals(cPassword)) {
            Toast.makeText(this, "Nhap ten?", Toast.LENGTH_SHORT).show();
        } else {
                createUserAccount();
        }
    }

    private void createUserAccount() {
        progressDialog.setMessage("Dang tao ");
        progressDialog.show();

        firebaseAuth.createUserWithEmailAndPassword(email,password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        progressDialog.dismiss();
                        updateUserInfo();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(SignActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateUserInfo() {
        progressDialog.setMessage("Dang luu");

        long timestamp = System.currentTimeMillis();
        String uid = firebaseAuth.getUid();
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("uid", uid);
        hashMap.put("email", email);
        hashMap.put("name", name);
        hashMap.put("profileImage", "");
        hashMap.put("userType", "user");
        hashMap.put("timestamp", timestamp);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(uid)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        Toast.makeText(SignActivity.this, "dang tao", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(SignActivity.this, MainUserActivity.class));
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(SignActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }
}