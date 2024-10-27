package com.example.appdocsachfinal.Activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.appdocsachfinal.databinding.ActivityPdfEditBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class PdfEditActivity extends AppCompatActivity {

    private ActivityPdfEditBinding binding;
    private String bookId;
    private ProgressDialog progressDialog;
    private ArrayList<String> categoryTitleArraylist,categoryIdArraylist;
    private static final String TAG = "BOOK_EDIT_TAG";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        bookId = getIntent().getStringExtra("bookId");

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Doi ty");
        progressDialog.setCanceledOnTouchOutside(false);
        loadCategories();
        loadBookInfo();

        binding.btnback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        binding.btnupdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });
        binding.edttheloai.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                categoryDialog();
            }
        });
    }

    private String title="",description="";
    private void validateData() {
        title = binding.edttenbook.getText().toString().trim();
        description = binding.edtmota.getText().toString().trim();
        if (TextUtils.isEmpty(title)){
            Toast.makeText(this, "Nhap ten sach ", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(description)) {
            Toast.makeText(this, "Nhap mo ta ", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(selectedCategoryId)) {
            Toast.makeText(this, "Chon the loai", Toast.LENGTH_SHORT).show();
        }else {
            updatePdf();
        }
    }

    private void updatePdf() {
        Log.d(TAG, "updatePdf: Starting updating pdf info to db");
        progressDialog.setMessage("Dang tai thong tin sach");
        progressDialog.show();
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("title", ""+title);
        hashMap.put("description", ""+description);
        hashMap.put("categoryId", ""+selectedCategoryId);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId)
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG,"onSuccess: Cap nhat sach");
                        progressDialog.dismiss();
                        Toast.makeText(PdfEditActivity.this, "Cap nhat thong tin sach thanh cong", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG,"onFailure: Cap nhat sach that bai"+e.getMessage());
                        progressDialog.dismiss();
                        Toast.makeText(PdfEditActivity.this,""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadBookInfo() {
        Log.d(TAG, "loadBookInfo: Dang tai thong tin truyen");

        DatabaseReference refBooks = FirebaseDatabase.getInstance().getReference("Books");
        refBooks.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        selectedCategoryId = ""+snapshot.child("categoryId").getValue();
                        String description = ""+snapshot.child("description").getValue();
                        String title = ""+snapshot.child("title").getValue();

                        binding.edttenbook.setText(title);
                        binding.edtmota.setText(description);
                        Log.d(TAG,"onDataChange: Dang tai thong tin the loai sach");
                        DatabaseReference refBookCategory = FirebaseDatabase.getInstance().getReference("Categories");
                        refBookCategory.child(selectedCategoryId)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        String category = ""+snapshot.child("category").getValue();
                                        binding.edttheloai.setText(category);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private String selectedCategoryId="", selectedCategoryTitle = "";

    private void categoryDialog(){
        String[] categoriesArray = new String[categoryTitleArraylist.size()];
        for (int i=0;i<categoryTitleArraylist.size();i++){
            categoriesArray[i] = categoryTitleArraylist.get(i);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Category")
                .setItems(categoriesArray, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedCategoryId = categoryIdArraylist.get(which);
                        binding.edttheloai.setText(categoriesArray[which]);
                    }
                })
                .show();
    }
    private void loadCategories() {
        Log.d(TAG, "loadCategories: Dang tai the loai");
        categoryIdArraylist = new ArrayList<>();
        categoryTitleArraylist = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryIdArraylist.clear();
                categoryTitleArraylist.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    String id = ""+ds.child("id").getValue();
                    String category = ""+ds.child("category").getValue();
                    categoryIdArraylist.add(id);
                    categoryTitleArraylist.add(category);
                    Log.d(TAG, "onDataChange: ID: "+id);
                    Log.d(TAG, "onDataChange: Category: "+category);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}