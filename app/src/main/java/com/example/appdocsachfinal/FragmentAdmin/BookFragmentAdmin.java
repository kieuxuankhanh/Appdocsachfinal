package com.example.appdocsachfinal.FragmentAdmin;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.appdocsachfinal.Adapter.AdapterCategory;
import com.example.appdocsachfinal.Activities.AddCategoryActivity;
import com.example.appdocsachfinal.Model.ModelCategory;
import com.example.appdocsachfinal.databinding.FragmentBookAdminBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


public class BookFragmentAdmin extends Fragment {

    private FragmentBookAdminBinding binding;
    private FirebaseAuth firebaseAuth;
    private ArrayList<ModelCategory> categoryArrayList;
    private AdapterCategory adapterCategory;
    public BookFragmentAdmin() {
        // Required empty public constructor
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadCategories();
        binding.btnthemtheloai.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(requireActivity(), AddCategoryActivity.class));
            }
        });

        binding.edtsearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    adapterCategory.getFilter().filter(s);
                }catch (Exception e){

                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    private void loadCategories() {
        categoryArrayList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryArrayList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    ModelCategory modelCategory = ds.getValue(ModelCategory.class);
                    categoryArrayList.add(modelCategory);
                }

                // Sắp xếp danh sách theo tên category
                Collections.sort(categoryArrayList, new Comparator<ModelCategory>() {
                    @Override
                    public int compare(ModelCategory c1, ModelCategory c2) {
                        return c1.getCategory().compareToIgnoreCase(c2.getCategory());
                    }
                });

                // Tùy chọn: đưa category "Khác" xuống cuối danh sách
                if (categoryArrayList.size() > 1) {
                    for (int i = 0; i < categoryArrayList.size(); i++) {
                        if (categoryArrayList.get(i).getCategory().equals("Khác")) {
                            ModelCategory other = categoryArrayList.remove(i);
                            categoryArrayList.add(other);
                            break;
                        }
                    }
                }

                adapterCategory = new AdapterCategory(requireActivity(), categoryArrayList);
                binding.catagoryRecycleview.setAdapter(adapterCategory);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireActivity(), "Lỗi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentBookAdminBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }
}