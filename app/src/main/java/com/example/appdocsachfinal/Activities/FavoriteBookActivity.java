package com.example.appdocsachfinal.Activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.appdocsachfinal.Adapter.AdapterFavoriteBook;
import com.example.appdocsachfinal.Model.ModelPdf;
import com.example.appdocsachfinal.databinding.ActivityFavoriteBookBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FavoriteBookActivity extends AppCompatActivity {
    private ActivityFavoriteBookBinding binding;
    private ArrayList<ModelPdf> pdfArrayList;
    private AdapterFavoriteBook adapterFavoriteBook;
    private FirebaseAuth firebaseAuth;
    private boolean isDataLoading = false;
    private static final String TAG = "FAVORITE_LIST_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFavoriteBookBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        firebaseAuth = FirebaseAuth.getInstance();
        loadFavoriteBook();
        setupSwipeRefresh();
        binding.btnback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        binding.edtsearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    adapterFavoriteBook.getFilter().filter(s);
                }catch (Exception e){
                    Log.d(TAG,"onTextChanged: "+e.getMessage());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }
    private void setupSwipeRefresh() {
        if (binding != null && binding.swipeRefreshLayout != null) {
            binding.swipeRefreshLayout.setOnRefreshListener(() -> {
                if (!isDataLoading) {
                    loadFavoriteBook();
                }
                binding.swipeRefreshLayout.setRefreshing(false);
            });
        }
    }
    //Đẩy id của sách yêu thích lên bảng người dùng
    private void loadFavoriteBook() {
        pdfArrayList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Favorites")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        pdfArrayList.clear();
                        for (DataSnapshot ds: snapshot.getChildren()){
                            String bookId = ""+ds.child("bookId").getValue();
                            ModelPdf modelPdf = new ModelPdf();
                            modelPdf.setId(bookId);
                            pdfArrayList.add(modelPdf);
                        }
                        binding.txtpage.setText(""+pdfArrayList.size());
                        adapterFavoriteBook = new AdapterFavoriteBook(FavoriteBookActivity.this, pdfArrayList);
                        binding.Rcvbook.setAdapter(adapterFavoriteBook);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}