package com.example.appdocsachfinal.Activities;

import android.os.Bundle;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFavoriteBookBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        firebaseAuth = FirebaseAuth.getInstance();
        loadFavoriteBook();
        binding.btnback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

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