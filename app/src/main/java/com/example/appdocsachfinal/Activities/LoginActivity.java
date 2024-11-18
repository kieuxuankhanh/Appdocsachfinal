package com.example.appdocsachfinal.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.appdocsachfinal.R;
import com.example.appdocsachfinal.databinding.ActivityLoginBinding;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.Login;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;

    private FirebaseAuth firebaseAuth;
    FirebaseDatabase database;
    GoogleSignInClient mGoogleSignInClient;
    CallbackManager callbackManager;
    int RC_SIGN_IN=20;

    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        firebaseAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail().build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Đợi 1 chút nha!!!");
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
            }
        });
        binding.txtforgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, ForgotPassActivity.class));
            }
        });
        binding.btngg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                
                googleSignIn();

            }
        });

        callbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        handleFacebookAccessToken(loginResult.getAccessToken());
                    }

                    @Override
                    public void onCancel() {
                        Toast.makeText(LoginActivity.this,
                                "Đăng nhập Facebook đã bị hủy", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(FacebookException error) {
                        Toast.makeText(LoginActivity.this,
                                "Đăng nhập Facebook thất bại: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
        binding.btnfb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginManager.getInstance().logInWithReadPermissions(LoginActivity.this, Arrays.asList("email", "public_profile"));
            }
        });
    }

    private void handleFacebookAccessToken(AccessToken token) {
        progressDialog.setMessage("Đang đăng nhập với Facebook...");
        progressDialog.show();

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            // Save user data to Firebase Database
                            long timestamp = System.currentTimeMillis();
                            HashMap<String, Object> map = new HashMap<>();
                            map.put("uid", user.getUid());
                            map.put("email", user.getEmail());
                            map.put("name", user.getDisplayName());
                            map.put("profileImage", user.getPhotoUrl() != null ?
                                    user.getPhotoUrl().toString() : "");
                            map.put("userType", "user");
                            map.put("timestamp", timestamp);

                            DatabaseReference ref = database.getReference("Users");
                            ref.child(user.getUid())
                                    .setValue(map)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            progressDialog.dismiss();
                                            startActivity(new Intent(LoginActivity.this,
                                                    MainUserActivity.class));
                                            finish();
                                        }
                                    });
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(LoginActivity.this,
                                    "Đăng nhập Facebook thất bại: " +
                                            task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void googleSignIn() {
        Intent intent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(intent,RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
        if (requestCode==RC_SIGN_IN){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuth(account.getIdToken());
            }
            catch (Exception e){
                Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void firebaseAuth(String idToken){
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            long timestamp = System.currentTimeMillis();
                            HashMap<String, Object> map = new HashMap<>();
                            map.put("uid", user.getUid());
                            map.put("email", user.getEmail());
                            map.put("name", user.getDisplayName());
                            map.put("profileImage", user.getPhotoUrl().toString());
                            map.put("userType", "user");
                            map.put("timestamp", timestamp);

                            database.getReference().child("Users").child(user.getUid()).setValue(map);
                            Intent intent = new Intent(LoginActivity.this, MainUserActivity.class);
                            startActivity(intent);
                        }
                        else {
                            Toast.makeText(LoginActivity.this, "Loi", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private String email ="",password="";

    private void validateData() {
        email = binding.edtuser.getText().toString().trim();
        password = binding.edtpass.getText().toString().trim();

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Nhập Email!!!", Toast.LENGTH_SHORT).show();

        } else if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Nhập mật khẩu!!!", Toast.LENGTH_SHORT).show();
        }
        else {
            loginUser();
        }
    }

    private void loginUser() {
        progressDialog.setMessage("Đang đăng nhập");
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
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        String errorMessage;

                        if (e instanceof com.google.firebase.auth.FirebaseAuthInvalidCredentialsException) {
                            errorMessage = "Email hoặc mật khẩu không đúng!";
                        }
                        else if (e instanceof com.google.firebase.auth.FirebaseAuthInvalidUserException) {
                            String errorCode = ((com.google.firebase.auth.FirebaseAuthInvalidUserException) e).getErrorCode();
                            if (errorCode.equals("ERROR_USER_NOT_FOUND")) {
                                errorMessage = "Tài khoản không tồn tại!";
                            } else {
                                errorMessage = "Tài khoản đã bị vô hiệu hóa!";
                            }
                        }
                        else {
                            errorMessage = "Đăng nhập thất bại, vui lòng thử lại!";
                        }

                        Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                        binding.edtpass.setText(""); // Xóa mật khẩu đã nhập
                    }
                });
    }

    private void checkUser() {
        progressDialog.setMessage("Đang kiểm tra");
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