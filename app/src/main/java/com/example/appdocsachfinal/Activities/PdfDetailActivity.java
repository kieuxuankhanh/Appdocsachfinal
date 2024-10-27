package com.example.appdocsachfinal.Activities;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.appdocsachfinal.MyApplication;
import com.example.appdocsachfinal.R;
import com.example.appdocsachfinal.databinding.ActivityPdfDetailBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class PdfDetailActivity extends AppCompatActivity {
    private static final String TAG_DOWNLOAD = "DOWNLOAD_TAG";
    private ActivityPdfDetailBinding binding;
    String bookId,bookTitle,bookUrl;
    boolean isInMyFavorite = false;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        bookId = intent.getStringExtra("bookId");
        binding.btnsave.setVisibility(View.GONE);
        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null){
            checkIsFavorite();
        }
        loadBookDetails();
        MyApplication.incrementBookViewCount(bookId);

        binding.btnback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        binding.btnread.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(PdfDetailActivity.this, PdfViewActivity.class);
                intent1.putExtra("bookId",bookId);
                startActivity(intent1);
            }
        });
        binding.btnsave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG_DOWNLOAD,"onClick:Checking permission");
                if (ContextCompat.checkSelfPermission(PdfDetailActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                    Log.d(TAG_DOWNLOAD,"onClick: Permission already granted, can download book");
                    MyApplication.downloadBook(PdfDetailActivity.this,""+bookId,""+bookTitle,""+bookUrl);
                }
                else {
                    Log.d(TAG_DOWNLOAD,"onClick: Permission was not granted, request permission");
                    requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                }
            }
        });
        binding.btnfavor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isInMyFavorite){
                    MyApplication.removeFromFavorite(PdfDetailActivity.this, bookId);
                }
                else {
                    MyApplication.addToFavorite(PdfDetailActivity.this,bookId);
                }
            }
        });
    }

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted){
                    Log.d(TAG_DOWNLOAD,"Permission Granted");
                    MyApplication.downloadBook(this, ""+bookId,""+bookTitle,""+bookUrl);
                }else {
                    Log.d(TAG_DOWNLOAD,"Permission tu choi");
                    Toast.makeText(this, "Permission tu choi", Toast.LENGTH_SHORT).show();
                }
            });

    private void loadBookDetails() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        bookTitle = ""+snapshot.child("title").getValue();
                        String description = ""+snapshot.child("description").getValue();
                        String categoryId = ""+snapshot.child("categoryId").getValue();
                        String viewsCount = ""+snapshot.child("viewsCount").getValue();
                        String downloadsCount = ""+snapshot.child("downloadsCount").getValue();
                        bookUrl = ""+snapshot.child("url").getValue();
                        String timestamp = ""+snapshot.child("timestamp").getValue();
                        binding.btnsave.setVisibility(View.VISIBLE);

                        String date = MyApplication.formatTimestamp(Long.parseLong(timestamp));
                        MyApplication.loadCategory(
                                ""+categoryId,
                                binding.txtcategory
                        );
                        MyApplication.loadPdfFromUrlSinglePage(
                                ""+bookUrl,
                                ""+bookTitle,
                                binding.pdfView,
                                binding.progressBar,
                                binding.txtpage
                        );
                        MyApplication.LoadPdfSize(
                                ""+bookUrl,
                                ""+bookTitle,
                                binding.txtsize
                        );
                        binding.txttitle.setText(bookTitle);
                        binding.txtmota.setText(description);
                        binding.txtviews.setText(viewsCount.replace("null","N/A"));
                        binding.txtdownload.setText(downloadsCount.replace("null","N/A"));
                        binding.txtdate.setText(date);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
    private void checkIsFavorite(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Favorites").child(bookId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        isInMyFavorite = snapshot.exists();
                        if (isInMyFavorite){
                            binding.btnfavor.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.baseline_favorite_white,0,0);
                        }else {
                            binding.btnfavor.setCompoundDrawablesRelativeWithIntrinsicBounds(0,R.drawable.baseline_favorite_border,0,0);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}