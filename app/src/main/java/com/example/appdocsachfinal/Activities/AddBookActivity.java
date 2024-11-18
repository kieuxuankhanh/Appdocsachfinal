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
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.appdocsachfinal.databinding.ActivityAddBookBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;

public class AddBookActivity extends AppCompatActivity {
    private ActivityAddBookBinding binding;
    private FirebaseAuth firebaseAuth;
    private ArrayList<String> categoryTitleArrayList, categoryIdArrayList;
    private static final int PDF_PICK_CODE = 1000;
    private ProgressDialog progressDialog;
    private static final String TAG = "ADD_PDF_TAG";
    private Uri pdfUri = null;
    private Uri imageUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddBookBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Đợi 1 chút nha!!!");
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth = FirebaseAuth.getInstance();
        loadPdfCategories();
        binding.btnattach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pdfPickIntent();
            }
        });
        binding.edttheloai.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CategoryPickDialog();
            }
        });
        binding.btnsubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
                clear();
            }
        });
        binding.btnback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        binding.CVImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImageGallery();
            }
        });
    }
    private void clear(){
        binding.edttenbook.setText("");
        binding.edtmota.setText("");
        binding.edttheloai.setText("");
        binding.imageThumb.setImageResource(0);
    }
    private String title="",description="";
    private void validateData() {
        Log.d(TAG, "validateData: validating Data");
        title = binding.edttenbook.getText().toString().trim();
        description = binding.edtmota.getText().toString().trim();

        if (TextUtils.isEmpty(title)){
            Toast.makeText(this, "Nhập tên", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(description)) {
            Toast.makeText(this, "Nhập mô tả", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(selectedCategoryTitle)) {
            Toast.makeText(this, "Chọn thể loại", Toast.LENGTH_SHORT).show();
        } else if (pdfUri == null) {
            Toast.makeText(this, "Chọn tệp PDF", Toast.LENGTH_SHORT).show();
        }else if (imageUri == null) {
            Toast.makeText(this, "Chọn ảnh bìa sách", Toast.LENGTH_SHORT).show();
        }
        else {
            uploadFilesToStorage();
        }
    }

    private void uploadFilesToStorage() {
        Log.d(TAG, "uploadFilesToStorage: Đang tải files");
        progressDialog.setMessage("Đang tải dữ liệu...");
        progressDialog.show();

        long timestamp = System.currentTimeMillis();
        //đẩy file lên storage
        // Upload PDF
        String pdfPathAndName = "Books/" + timestamp ;
        StorageReference pdfRef = FirebaseStorage.getInstance().getReference(pdfPathAndName);

        // Tải ảnh
        String imagePathAndName = "BookCovers/" + timestamp + ".jpg";
        StorageReference imageRef = FirebaseStorage.getInstance().getReference(imagePathAndName);

        // Tải ảnh trước
        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    Task<Uri> imageUrlTask = taskSnapshot.getStorage().getDownloadUrl();
                    imageUrlTask.addOnSuccessListener(imageDownloadUri -> {
                        // Sau khi đẩy ảnh lên thì đẩy file Pdf
                        pdfRef.putFile(pdfUri)
                                .addOnSuccessListener(pdfTaskSnapshot -> {
                                    Task<Uri> pdfUrlTask = pdfTaskSnapshot.getStorage().getDownloadUrl();
                                    pdfUrlTask.addOnSuccessListener(pdfDownloadUri -> {
                                        // tải ảnh lên database
                                        uploadToDatabase(pdfDownloadUri.toString(), imageDownloadUri.toString(), timestamp);
                                    });
                                })
                                .addOnFailureListener(e -> {
                                    progressDialog.dismiss();
                                    Log.d(TAG, "Thất bại: Tải PDF không thành công " + e.getMessage());
                                    Toast.makeText(AddBookActivity.this, "Tải PDF không thành công " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    });
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Log.d(TAG, "Thất bại: Tải ảnh không thành công " + e.getMessage());
                    Toast.makeText(AddBookActivity.this, "Tải ảnh không thành công " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    private void uploadToDatabase(String pdfUrl, String imageUrl, long timestamp) {
        Log.d(TAG, "uploadToDatabase: Đang lưu thông tin vào database");
        progressDialog.setMessage("Đang lưu thông tin sách");
        //đẩy thông tin sách lên realtime database
        String uid = firebaseAuth.getUid();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid", "" + uid);
        hashMap.put("id", "" + timestamp);
        hashMap.put("title", "" + title);
        hashMap.put("description", "" + description);
        hashMap.put("categoryId", "" + selectedCategoryID);
        hashMap.put("url", "" + pdfUrl);
        hashMap.put("timestamp", timestamp);
        hashMap.put("viewsCount", 0);
        hashMap.put("downloadsCount", 0);
        hashMap.put("imageThumb", "" + imageUrl);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child("" + timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(unused -> {
                    createNoti(timestamp);
                    progressDialog.dismiss();
                    Log.d(TAG, "OnSuccess: tải lên db thành công");
                    Toast.makeText(AddBookActivity.this, "Tải lên thành công", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Log.d(TAG, "OnFailure: Lỗi tải lên db " + e.getMessage());
                    Toast.makeText(AddBookActivity.this, "Lỗi tải lên database", Toast.LENGTH_SHORT).show();
                });
    }

    private void createNoti(long timestamp) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users");
        userRef.child(firebaseAuth.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String userName = ""+snapshot.child("name").getValue();

                        // Tạo thông báo mỗi khi đăng truyện
                        HashMap<String, Object> notificationMap = new HashMap<>();
                        notificationMap.put("id", ""+timestamp);
                        notificationMap.put("timestamp", timestamp);
                        notificationMap.put("title", "Sách mới: " + title);
                        notificationMap.put("message", userName + " đã đăng tải sách mới: " + title);
                        notificationMap.put("bookId", ""+timestamp);
                        notificationMap.put("uid", firebaseAuth.getUid());

                        // Add to Firebase
                        DatabaseReference notifRef = FirebaseDatabase.getInstance().getReference("Notifications");
                        notifRef.child(""+timestamp)
                                .setValue(notificationMap)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Log.d(TAG, "Notification created successfully");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(TAG, "Failed to create notification: " + e.getMessage());
                                    }
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }
    //Tải thể loại
    private void loadPdfCategories() {
        Log.d(TAG,"LoadPdfCategories:  Dang tai the loai pdf...");
        categoryTitleArrayList = new ArrayList<>();
        categoryIdArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryTitleArrayList.clear();
                categoryIdArrayList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                   String categoryId = ""+ds.child("id").getValue();
                   String categoryTitle = ""+ds.child("category").getValue();

                   categoryTitleArrayList.add(categoryTitle);
                   categoryIdArrayList.add(categoryId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    //chọn thể loại
    private String selectedCategoryID,selectedCategoryTitle;
    private void CategoryPickDialog() {
        Log.d(TAG, "categoryPickDialog: Hiển thị thể loại");

        String[] categoriesArray = new String[categoryTitleArrayList.size()];
        for (int i = 0; i< categoryTitleArrayList.size(); i++){
            categoriesArray[i] = categoryTitleArrayList.get(i);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn thể loại")
                .setItems(categoriesArray, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedCategoryTitle = categoryTitleArrayList.get(which);
                        selectedCategoryID = categoryIdArrayList.get(which);
                        binding.edttheloai.setText(selectedCategoryTitle);
                        Log.d(TAG,"onClick: Chọn thể loại: "+categoryTitleArrayList);
                    }
                })
                .show();
    }
    //chọn ảnh bìa truyện
    private void pickImageGallery() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        galleryActivityResultLauncher.launch(intent);
    }
    //lấy ảnh từ thư viện
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
                                Glide.with(AddBookActivity.this).load(imageUri).into(binding.imageThumb);
                            }
                        }
                    }
                }
            }
    );
    //lấy pdf từ thư mục của máy
    private void pdfPickIntent() {
        Log.d(TAG,"pdfPickIntent: starting pdf pick intent");

        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select PDF"),PDF_PICK_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK){
            if(requestCode == PDF_PICK_CODE){
                Log.d(TAG, "onACtivityResult: PDF picked");
                pdfUri = data.getData();
                Log.d(TAG,"onActivityResult: URI: "+pdfUri);
            }
        }
        else {
            Log.d(TAG, "onActivityResult: cancelled picking pdf");
            Toast.makeText(this, "cancelled picking pdf", Toast.LENGTH_SHORT).show();
        }
    }
}