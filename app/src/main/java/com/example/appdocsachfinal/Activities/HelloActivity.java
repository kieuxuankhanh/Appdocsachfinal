package com.example.appdocsachfinal.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.appdocsachfinal.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HelloActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private SharedPreferences sharedPreferences;
    private static final int SPLASH_TIME = 2000;
    private boolean dataLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hello);

        // lưu trữ thông tin người dùng
        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);

        // Khởi tạo Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        //lưu trữ dữ liệu để truy cập offline
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        // Bắt đầu load data ngay
        checkUserInBackground();

        // Timer màn hình chào
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                navigateToNextScreen();
            }
        }, SPLASH_TIME);
    }

    private void checkUserInBackground() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users")
                    .child(firebaseUser.getUid());

            // Enable disk persistence
            ref.keepSynced(true);

            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String userType = "" + snapshot.child("userType").getValue();
                    sharedPreferences.edit()
                            .putString("userType", userType)
                            .putString("uid", firebaseUser.getUid())
                            .apply();
                    dataLoaded = true;
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    dataLoaded = true;
                }
            });
        } else {
            dataLoaded = true;
        }
    }

    private void navigateToNextScreen() {
        // Nếu data chưa load xong, đợi thêm 500ms
        if (!dataLoaded) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    navigateToNextScreen();
                }
            }, 500);
            return;
        }

        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser == null) {
            startActivity(new Intent(HelloActivity.this, LoginActivity.class));
            finish();
            return;
        }

        String userType = sharedPreferences.getString("userType", null);
        Intent intent;

        if ("user".equals(userType)) {
            intent = new Intent(HelloActivity.this, MainUserActivity.class);
        } else if ("admin".equals(userType)) {
            intent = new Intent(HelloActivity.this, MainAdminActivity.class);
        } else {
            intent = new Intent(HelloActivity.this, LoginActivity.class);
        }

        startActivity(intent);
        finish();
    }
}