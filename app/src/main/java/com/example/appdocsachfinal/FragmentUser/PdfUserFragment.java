package com.example.appdocsachfinal.FragmentUser;

import static android.content.Intent.getIntent;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.appdocsachfinal.Adapter.AdapterPdfUser;
import com.example.appdocsachfinal.Model.ModelPdf;
import com.example.appdocsachfinal.R;
import com.example.appdocsachfinal.databinding.FragmentPdfUserBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PdfUserFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PdfUserFragment extends Fragment {

    private String categoryId;
    private String category;
    private String uid;
    private ArrayList<ModelPdf> pdfArrayList;
    private AdapterPdfUser adapterPdfUser;
    private FragmentPdfUserBinding binding;
    private static final String TAG ="BOOK_USER_TAG";
    public PdfUserFragment() {
        // Required empty public constructor
    }
    // TODO: Rename and change types and number of parameters
    public static PdfUserFragment newInstance(String categoryId, String category,String uid) {
        PdfUserFragment fragment = new PdfUserFragment();
        Bundle args = new Bundle();
        args.putString("categoryId", categoryId);
        args.putString("category", category);
        args.putString("uid", uid);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            categoryId = getArguments().getString("categoryId");
            category = getArguments().getString("category");
            uid = getArguments().getString("uid");
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPdfUserBinding.inflate(LayoutInflater.from(getContext()),container,false);
        String category = getArguments().getString("category");
        Log.d(TAG,"onCreateView: Thể loại:"+category);
        if (category.equals("Tất cả")){
            loadAllBooks();
        } else if (category.equals("Tải nhiều nhất")) {
            loadMostViewedDownloadedBooks("downloadsCount");
        } else if (category.equals("Đọc nhiều nhất")) {
            loadMostViewedDownloadedBooks("viewsCount");
        } else {
            loadCategorizedBooks();
        }
        binding.searchEt.addTextChangedListener(new TextWatcher() {
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

        return binding.getRoot();
    }

    private void loadMostViewedDownloadedBooks(String orderBy) {
        pdfArrayList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.orderByChild(orderBy).limitToLast(10)
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                pdfArrayList.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    ModelPdf modelPdf = ds.getValue(ModelPdf.class);
                    pdfArrayList.add(modelPdf);
                }
                adapterPdfUser = new AdapterPdfUser(getContext(), pdfArrayList);
                binding.booksRCV.setAdapter(adapterPdfUser);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadCategorizedBooks() {
        pdfArrayList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.orderByChild("categoryId").
                equalTo(categoryId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        pdfArrayList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()){
                            ModelPdf modelPdf = ds.getValue(ModelPdf.class);
                            pdfArrayList.add(modelPdf);
                        }
                        adapterPdfUser = new AdapterPdfUser(getContext(), pdfArrayList);
                        binding.booksRCV.setAdapter(adapterPdfUser);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadAllBooks() {
        pdfArrayList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                pdfArrayList.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    ModelPdf modelPdf = ds.getValue(ModelPdf.class);
                    pdfArrayList.add(modelPdf);
                }
                adapterPdfUser = new AdapterPdfUser(getContext(), pdfArrayList);
                binding.booksRCV.setAdapter(adapterPdfUser);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


}