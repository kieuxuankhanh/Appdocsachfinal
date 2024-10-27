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

import com.example.appdocsachfinal.databinding.ActivityLoginBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;

    private FirebaseAuth firebaseAuth;

    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        firebaseAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Doi ty");
        progressDialog.setCanceledOnTouchOutside(false);

        binding.btnlog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               validateData();
            }
        });
        binding.btnsign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, SignActivity.class));
                finish();
            }
        });

    }
    private String email ="",password="";

    private void validateData() {
        email = binding.edtuser.getText().toString().trim();
        password = binding.edtpass.getText().toString().trim();

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Nhap lai email", Toast.LENGTH_SHORT).show();

        } else if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Nhap ten?", Toast.LENGTH_SHORT).show();
        }
        else {
            loginUser();
        }
    }

    private void loginUser() {
        progressDialog.setMessage("Dang dang nhap");
        progressDialog.show();

        firebaseAuth.signInWithEmailAndPassword(email,password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        checkUser();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(LoginActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkUser() {
        progressDialog.setMessage("Dang kiem tra");
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        progressDialog.dismiss();
                        String userType = "" + snapshot.child("userType").getValue();

                        if (userType.equals("user")){
                            startActivity(new Intent(LoginActivity.this, MainUserActivity.class));
                            finish();
                        }else if (userType.equals("admin")){
                            startActivity(new Intent(LoginActivity.this, MainAdminActivity.class));
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}