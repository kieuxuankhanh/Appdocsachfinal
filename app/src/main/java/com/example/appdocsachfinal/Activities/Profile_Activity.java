package com.example.appdocsachfinal.Activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.bumptech.glide.Glide;
import com.example.appdocsachfinal.R;
import com.example.appdocsachfinal.databinding.ActivityProfileBinding;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;

public class Profile_Activity extends AppCompatActivity {
    private ActivityProfileBinding binding;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private static final String TAG = "PROFILE_EDIT_TAG";
    private Uri imageUri;
    private String name = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Đợi một chút...");
        progressDialog.setCanceledOnTouchOutside(false);
        firebaseAuth = FirebaseAuth.getInstance();
        loadUserInfo();

        binding.btnback.setOnClickListener(v -> onBackPressed());
        binding.imgAvatar.setOnClickListener(v -> showImageAttachMenu());
        binding.btnedit.setOnClickListener(v -> validateData());
    }

    private void validateData() {
        name = binding.txtName.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Nhập tên", Toast.LENGTH_SHORT).show();
        } else {
            if (imageUri == null) {
                updateProfile("");
            } else {
                uploadImage();
            }
        }
    }

    private void updateProfile(String imageUrl) {
        Log.d(TAG, "updateProfile: đang cập nhật thông tin người dùng ");
        progressDialog.setMessage("Cập nhật thông tin người dùng");
        progressDialog.show();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("name", name);
        if (!TextUtils.isEmpty(imageUrl)) {
            hashMap.put("profileImage", imageUrl);
        }
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.child(firebaseAuth.getUid())
                .updateChildren(hashMap)
                .addOnSuccessListener(unused -> {
                    Log.d(TAG, "onSuccess: cập nhật thông tin thành công");
                    progressDialog.dismiss();
                    Toast.makeText(Profile_Activity.this, "Thông tin đã được cập nhật", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.d(TAG, "onFailure: lỗi " + e.getMessage());
                    progressDialog.dismiss();
                    Toast.makeText(Profile_Activity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void uploadImage() {
        Log.d(TAG, "uploadImage: đang cập nhật ảnh ");
        progressDialog.setMessage("Đang tải ảnh");
        progressDialog.show();
        String filePathAndName = "ProfileImages/" + firebaseAuth.getUid();
        StorageReference reference = FirebaseStorage.getInstance().getReference(filePathAndName);

        if (imageUri != null) {
            reference.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        Log.d(TAG, "onSuccess: cập nhật ảnh thành công");
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        uriTask.addOnSuccessListener(uri -> {
                            String uploadedImageUrl = uri.toString();
                            Log.d(TAG, "onSuccess: đang cập nhật url " + uploadedImageUrl);
                            updateProfile(uploadedImageUrl);

                        });
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Log.d(TAG, "onFailure: cập nhật ảnh lỗi " + e.getMessage());
                        progressDialog.dismiss();
                        Toast.makeText(Profile_Activity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(this, "Ảnh không hợp lệ", Toast.LENGTH_SHORT).show();
        }
    }

    private void showImageAttachMenu() {
        PopupMenu popupMenu = new PopupMenu(this, binding.imgAvatar);
        popupMenu.getMenu().add(Menu.NONE, 0, 0, "Camera");
        popupMenu.getMenu().add(Menu.NONE, 1, 1, "Gallery");
        popupMenu.show();
        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == 0) {
                pickImageCamera();
            } else if (item.getItemId() == 1) {
                pickImageGallery();
            }
            return false;
        });
    }

    private void pickImageGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        galleryActivityResultLauncher.launch(intent);
    }

    private void pickImageCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Ảnh mới");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Mô tả");
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        cameraActivityResultLauncher.launch(intent);
    }

    private ActivityResultLauncher<Intent> cameraActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Log.d(TAG, "onActivity: Chọn từ Camera " + imageUri);
                        if (imageUri != null) {
                            Glide.with(Profile_Activity.this).load(imageUri).into(binding.imgAvatar);
                        }
                    } else {
                        Toast.makeText(Profile_Activity.this, "Cancelled", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private ActivityResultLauncher<Intent> galleryActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            imageUri = data.getData();
                            if (imageUri != null) {
                                Log.d(TAG, "onActivityResult: lấy ảnh từ thư viện " + imageUri);
                                Glide.with(Profile_Activity.this).load(imageUri).into(binding.imgAvatar);
                            }
                        }
                    }
                }
            }
    );

    private void loadUserInfo() {
        Log.d(TAG, "LoadUserInfo: đang tải..." + firebaseAuth.getUid());
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String email = "" + snapshot.child("email").getValue();
                        String name = "" + snapshot.child("name").getValue();
                        String profileImage = "" + snapshot.child("profileImage").getValue();

                        binding.txtEmail.setText(email);
                        binding.txtName.setText(name);

                        Glide.with(Profile_Activity.this)
                                .load(profileImage)
                                .placeholder(R.drawable.avatar)
                                .into(binding.imgAvatar);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(Profile_Activity.this, "Lỗi tải thông tin: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}