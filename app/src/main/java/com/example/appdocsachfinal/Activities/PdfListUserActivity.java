package com.example.appdocsachfinal.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.appdocsachfinal.Adapter.AdapterPdfUser;
import com.example.appdocsachfinal.Model.ModelPdf;
import com.example.appdocsachfinal.databinding.ActivityPdfListUserBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class PdfListUserActivity extends AppCompatActivity {
    
    private ActivityPdfListUserBinding binding;
    private ArrayList<ModelPdf> pdfArrayList;
    private AdapterPdfUser adapterPdfUser;

    private static final String TAG = "PDF_LIST_TAG";

    private String categoryId,categoryTitle;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfListUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        categoryId = intent.getStringExtra("categoryId");
        categoryTitle = intent.getStringExtra("categoryTitle");

        binding.txtsubTitle.setText(categoryTitle);
        loadPdfList();

        binding.edtsearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    adapterPdfUser.getFilter().filter(s);
                }catch (Exception e){
                    Log.d(TAG,"onTextChanged: "+e.getMessage());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        binding.btnback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

    }

    private void loadPdfList() {
        pdfArrayList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.orderByChild("categoryId").equalTo(categoryId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        pdfArrayList.clear();
                        for (DataSnapshot ds: snapshot.getChildren()){
                            ModelPdf modelPdf = ds.getValue(ModelPdf.class);
                            pdfArrayList.add(modelPdf);
                            Log.d(TAG,"onDataChange: "+modelPdf.getId()+" "+modelPdf.getTitle());
                        }
                        adapterPdfUser = new AdapterPdfUser(PdfListUserActivity.this,pdfArrayList);
                        binding.Rcvbook.setAdapter(adapterPdfUser);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}