package com.example.appdocsachfinal.Activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.appdocsachfinal.R;
import com.example.appdocsachfinal.databinding.ActivityPdfEditBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;

public class PdfEditActivity extends AppCompatActivity {
    private ActivityPdfEditBinding binding;
    private String bookId;
    private ProgressDialog progressDialog;
    private ArrayList<String> categoryTitleArraylist, categoryIdArraylist;
    private static final String TAG = "BOOK_EDIT_TAG";
    private Uri imageUri; // Uri cho ảnh được chọn
    private String selectedCategoryId = "", selectedCategoryTitle = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        bookId = getIntent().getStringExtra("bookId");

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Đợi 1 chút nha!!!");
        progressDialog.setCanceledOnTouchOutside(false);

        loadCategories();
        loadBookInfo();

        binding.btnback.setOnClickListener(v -> finish());
        binding.btnupdate.setOnClickListener(v -> validateData());
        binding.edttheloai.setOnClickListener(v -> categoryDialog());
        binding.CVImage.setOnClickListener(v -> chooseImage());
    }

    private void categoryDialog() {
        String[] categoriesArray = new String[categoryTitleArraylist.size()];
        categoryTitleArraylist.toArray(categoriesArray);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn thể loại")
                .setItems(categoriesArray, (dialog, which) -> {
                    selectedCategoryId = categoryIdArraylist.get(which);
                    selectedCategoryTitle = categoryTitleArraylist.get(which);
                    binding.edttheloai.setText(selectedCategoryTitle);
                })
                .show();
    }

    private void loadCategories() {
        Log.d(TAG, "loadCategories: Đang tải thể loại");
        categoryIdArraylist = new ArrayList<>();
        categoryTitleArraylist = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryIdArraylist.clear();
                categoryTitleArraylist.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String id = "" + ds.child("id").getValue();
                    String category = "" + ds.child("category").getValue();
                    categoryIdArraylist.add(id);
                    categoryTitleArraylist.add(category);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PdfEditActivity.this, "8", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void validateData() {
        String title = binding.edttenbook.getText().toString().trim();
        String description = binding.edtmota.getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            Toast.makeText(this, "Vui lòng nhập tên sách", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(description)) {
            Toast.makeText(this, "Vui lòng nhập mô tả", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(selectedCategoryId)) {
            Toast.makeText(this, "Vui lòng chọn thể loại", Toast.LENGTH_SHORT).show();
        } else {
            if (imageUri != null) {
                uploadImageAndUpdateBook(title, description);
            } else {
                updateBook(title, description, null);
            }
        }
    }

    private void uploadImageAndUpdateBook(String title, String description) {
        progressDialog.setMessage("Đang tải ảnh lên...");
        progressDialog.show();

        String filePathAndName = "BookImages/" + bookId;
        StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);

        storageReference.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    uriTask.addOnSuccessListener(uri -> {
                        String uploadedImageUrl = uri.toString();
                        updateBook(title, description, uploadedImageUrl);
                    });
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(PdfEditActivity.this, "Lỗi tải ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateBook(String title, String description, String imageUrl) {
        progressDialog.setMessage("Đang cập nhật thông tin sách...");

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("title", title);
        hashMap.put("description", description);
        hashMap.put("categoryId", selectedCategoryId);
        if (imageUrl != null) {
            hashMap.put("imageThumb", imageUrl);
        }

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId)
                .updateChildren(hashMap)
                .addOnSuccessListener(unused -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadBookInfo() {
        DatabaseReference refBooks = FirebaseDatabase.getInstance().getReference("Books");
        refBooks.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        selectedCategoryId = "" + snapshot.child("categoryId").getValue();
                        String description = "" + snapshot.child("description").getValue();
                        String title = "" + snapshot.child("title").getValue();
                        String imageThumb = "" + snapshot.child("imageThumb").getValue();

                        binding.edttenbook.setText(title);
                        binding.edtmota.setText(description);

                        if (!TextUtils.isEmpty(imageThumb) && !imageThumb.equals("null")) {
                            try {
                                Glide.with(PdfEditActivity.this)
                                        .load(imageThumb)
                                        .placeholder(R.drawable.avatar)
                                        .error(R.drawable.avatar)
                                        .into(binding.imageThumb);
                            } catch (Exception e) {
                                Log.e(TAG, "Lỗi load ảnh: ", e);
                            }
                        }

                        loadCategoryInfo(selectedCategoryId);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(PdfEditActivity.this, "Lỗi tải thông tin sách", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadCategoryInfo(String categoryId) {
        DatabaseReference refBookCategory = FirebaseDatabase.getInstance().getReference("Categories");
        refBookCategory.child(categoryId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String category = "" + snapshot.child("category").getValue();
                        binding.edttheloai.setText(category);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(PdfEditActivity.this, "7", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void chooseImage() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        galleryActivityResultLauncher.launch(intent);
    }

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
                                // Lấy quyền truy cập lâu dài
                                getContentResolver().takePersistableUriPermission(imageUri,
                                        Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                Log.d(TAG, "onActivityResult: lấy ảnh từ thư viện " + imageUri);
                                Glide.with(PdfEditActivity.this).load(imageUri).into(binding.imageThumb);
                            }
                        }
                    }
                }
            }
    );
}