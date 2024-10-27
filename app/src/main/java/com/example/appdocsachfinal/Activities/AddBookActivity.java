package com.example.appdocsachfinal.Activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

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
import com.google.firebase.storage.UploadTask;

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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddBookBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Doi ty");
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

            }
        });
        binding.btnback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

    }
    private String title="",description="";
    private void validateData() {
        Log.d(TAG, "validateData: validating Data");
        title = binding.edttenbook.getText().toString().trim();
        description = binding.edtmota.getText().toString().trim();

        if (TextUtils.isEmpty(title)){
            Toast.makeText(this, "Nhap ten", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(description)) {
            Toast.makeText(this, "Nhap mo ta", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(selectedCategoryTitle)) {
            Toast.makeText(this, "Chon the loai", Toast.LENGTH_SHORT).show();
        } else if (pdfUri == null) {
            Toast.makeText(this, "Chon pdf", Toast.LENGTH_SHORT).show();
        }else {
            upLoadPdfToStorage();
        }
    }

    private void upLoadPdfToStorage() {
        Log.d(TAG,"upLoadPdfToStorage : Dang tai len");

        progressDialog.setMessage("Dang tai Pdf");
        progressDialog.show();

        long timestamp = System.currentTimeMillis();
        String filePathAndName = "Books/"+timestamp;
        StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
        storageReference.putFile(pdfUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.d(TAG, "onSuccess: Da tai pdf len");
                        Log.d(TAG, "onSuccess: lay pdf url");

                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        String uploadedPdfUrl = ""+uriTask.getResult();
                        
                        upLoadPdfIntoDb(uploadedPdfUrl, timestamp);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Log.d(TAG, "That bai: Tai file PDf khong duoc"+e.getMessage());
                        Toast.makeText(AddBookActivity.this, " Tai file PDf khong duoc"+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void upLoadPdfIntoDb(String uploadedPdfUrl, long timestamp) {
        Log.d(TAG,"upLoadPdfToStorage : Dang tai pdf vao firebase db");

        progressDialog.setMessage("Dang tai noi dung pdf");
        String uid = firebaseAuth.getUid();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid",""+uid);
        hashMap.put("id", ""+timestamp);
        hashMap.put("title", ""+title);
        hashMap.put("description", ""+description);
        hashMap.put("categoryId", ""+selectedCategoryID);
        hashMap.put("url", ""+uploadedPdfUrl);
        hashMap.put("timestamp", timestamp);
        hashMap.put("viewsCount", 0);
        hashMap.put("downloadsCount", 0);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(""+timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        Log.d(TAG,"OnSuccess: tai len db");
                        Toast.makeText(AddBookActivity.this, "tai len db", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Log.d(TAG,"OnFailure: Loi tai len db"+e.getMessage());
                        Toast.makeText(AddBookActivity.this, "Loi tai len db", Toast.LENGTH_SHORT).show();
                    }
                });
    }

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
    private String selectedCategoryID,selectedCategoryTitle;
    private void CategoryPickDialog() {
        Log.d(TAG, "categoryPickDialog: hien thi the loai");

        String[] categoriesArray = new String[categoryTitleArrayList.size()];
        for (int i = 0; i< categoryTitleArrayList.size(); i++){
            categoriesArray[i] = categoryTitleArrayList.get(i);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Category")
                .setItems(categoriesArray, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedCategoryTitle = categoryTitleArrayList.get(which);
                        selectedCategoryID = categoryIdArrayList.get(which);
                        binding.edttheloai.setText(selectedCategoryTitle);

                        Log.d(TAG,"onClick: Chon the loai: "+categoryTitleArrayList);

                    }
                })
                .show();

    }

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