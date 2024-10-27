package com.example.appdocsachfinal.FragmentUser;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
import com.example.appdocsachfinal.Activities.DashBoardUserActivity;
import com.example.appdocsachfinal.Adapter.AdapterCategoryHome;
import com.example.appdocsachfinal.Adapter.AdapterPdfListUser;
import com.example.appdocsachfinal.Model.ModelCategory;
import com.example.appdocsachfinal.Model.ModelListPdf;
import com.example.appdocsachfinal.databinding.FragmentHomeUserBinding;
import com.google.firebase.database.*;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HomeFragmentUser extends Fragment {
    private static final String TAG = "HomeFragmentUser";
    private static final int MAX_BOOKS_TO_SHOW = 9;
    private static final int MAX_DOWNLOADED_BOOKS = 10;

    private FragmentHomeUserBinding binding;
    private ArrayList<ModelListPdf> allBooksArrayList;
    private ArrayList<ModelListPdf> mostDownloadedArrayList;
    private AdapterPdfListUser adapterPdfListUser;
    private AdapterCategoryHome categoryAdapter;
    private String categoryId, categoryTitle;

    private DatabaseReference booksRef;
    private DatabaseReference categoriesRef;
    private DatabaseReference imagesRef;

    private boolean isDataLoading = false;
    private boolean isAdded = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isAdded = true;
        initializeFirebaseRefs();
    }

    private void initializeFirebaseRefs() {
        try {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            booksRef = database.getReference("Books");
            categoriesRef = database.getReference("Categories");
            imagesRef = database.getReference("image");
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize Firebase references: " + e.getMessage());
            safeShowToast("Không thể kết nối đến cơ sở dữ liệu");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        try {
            binding = FragmentHomeUserBinding.inflate(inflater, container, false);
            return initializeView();
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreateView: " + e.getMessage());
            safeShowToast("Đã xảy ra lỗi khi tải giao diện");
            return null;
        }
    }

    private View initializeView() {
        if (binding == null) {
            Log.e(TAG, "Binding failed to initialize");
            return null;
        }

        initializeArrayLists();
        getIntentData();
        setupClickListeners();
        setupSwipeRefresh();
        initializeRecyclerViews();

        if (!isDataLoading) {
            loadAllData();
        }

        return binding.getRoot();
    }

    private void initializeArrayLists() {
        allBooksArrayList = new ArrayList<>();
        mostDownloadedArrayList = new ArrayList<>();
    }

    private void getIntentData() {
        try {
            if (getActivity() != null && getActivity().getIntent() != null) {
                Intent intent = getActivity().getIntent();
                categoryId = intent.getStringExtra("categoryId");
                categoryTitle = intent.getStringExtra("categoryTitle");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting intent data: " + e.getMessage());
        }
    }

    private void setupClickListeners() {
        if (binding != null) {
            binding.edtmore2.setOnClickListener(v -> safeNavigateToMostDownloaded());
            binding.buttonkhampha.setOnClickListener(v -> safeNavigateToDashboard());
        }
    }

    private void setupSwipeRefresh() {
        if (binding != null && binding.swipeRefreshLayout != null) {
            binding.swipeRefreshLayout.setOnRefreshListener(() -> {
                if (!isDataLoading) {
                    loadAllData();
                }
                binding.swipeRefreshLayout.setRefreshing(false);
            });
        }
    }

    private void initializeRecyclerViews() {
        try {
            if (binding == null || !isAdded() || getContext() == null) return;

            setupBooksRecyclerView();
            setupDownloadsRecyclerView();
            setupCategoriesRecyclerView();

        } catch (Exception e) {
            Log.e(TAG, "Error initializing RecyclerViews: " + e.getMessage());
        }
    }

    private void setupBooksRecyclerView() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 3);
        binding.booksRCV.setLayoutManager(gridLayoutManager);
        binding.booksRCV.setHasFixedSize(true);
    }

    private void setupDownloadsRecyclerView() {
        LinearLayoutManager horizontalLayoutManager = new LinearLayoutManager(getContext(),
                LinearLayoutManager.HORIZONTAL, false);
        binding.downloadRCV.setLayoutManager(horizontalLayoutManager);
        binding.downloadRCV.setHasFixedSize(true);
    }

    private void setupCategoriesRecyclerView() {
        LinearLayoutManager categoryLayoutManager = new LinearLayoutManager(getContext());
        binding.theloaiRCV.setLayoutManager(categoryLayoutManager);
        binding.theloaiRCV.setHasFixedSize(true);
    }

    private void loadAllData() {
        if (!isAdded() || getContext() == null) return;

        isDataLoading = true;
        loadImageSlider();
        loadAllBooks();
        loadMostDownloadedBooks();
        loadCategories();
        isDataLoading = false;
    }

    private void loadImageSlider() {
        if (imagesRef == null || !isAdded()) return;

        final List<SlideModel> imageList = new ArrayList<>();
        imagesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    if (!isAdded() || binding == null) return;

                    imageList.clear();
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        String imageUrl = ds.child("url").getValue(String.class);
                        if (imageUrl != null) {
                            imageList.add(new SlideModel(imageUrl, ScaleTypes.FIT));
                        }
                    }
                    if (!imageList.isEmpty() && binding.imageSlider != null) {
                        binding.imageSlider.setImageList(imageList, ScaleTypes.FIT);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error loading image slider: " + e.getMessage());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                handleDatabaseError(error);
            }
        });
    }

    private void loadAllBooks() {
        if (booksRef == null || !isAdded()) return;

        booksRef.limitToFirst(MAX_BOOKS_TO_SHOW)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        try {
                            if (!isAdded() || binding == null) return;

                            allBooksArrayList.clear();
                            for (DataSnapshot ds : snapshot.getChildren()) {
                                ModelListPdf model = ds.getValue(ModelListPdf.class);
                                if (model != null) {
                                    allBooksArrayList.add(model);
                                }
                            }

                            updateBooksAdapter();
                        } catch (Exception e) {
                            Log.e(TAG, "Error loading books: " + e.getMessage());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        handleDatabaseError(error);
                    }
                });
    }

    private void updateBooksAdapter() {
        if (getContext() == null || binding == null) return;

        adapterPdfListUser = new AdapterPdfListUser(getContext(), allBooksArrayList);
        binding.booksRCV.setAdapter(adapterPdfListUser);
    }

    private void loadMostDownloadedBooks() {
        if (booksRef == null || !isAdded()) return;

        booksRef.orderByChild("downloadsCount")
                .limitToLast(MAX_DOWNLOADED_BOOKS)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        try {
                            if (!isAdded() || binding == null) return;

                            mostDownloadedArrayList.clear();
                            for (DataSnapshot ds : snapshot.getChildren()) {
                                ModelListPdf model = ds.getValue(ModelListPdf.class);
                                if (model != null) {
                                    mostDownloadedArrayList.add(model);
                                }
                            }

                            Collections.reverse(mostDownloadedArrayList);
                            updateDownloadsAdapter();
                        } catch (Exception e) {
                            Log.e(TAG, "Error loading most downloaded books: " + e.getMessage());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        handleDatabaseError(error);
                    }
                });
    }

    private void updateDownloadsAdapter() {
        if (getContext() == null || binding == null) return;

        AdapterPdfListUser downloadsAdapter = new AdapterPdfListUser(getContext(), mostDownloadedArrayList);
        binding.downloadRCV.setAdapter(downloadsAdapter);
    }

    private void loadCategories() {
        if (categoriesRef == null || !isAdded()) return;

        categoriesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    if (!isAdded() || binding == null) return;

                    ArrayList<ModelCategory> categories = new ArrayList<>();
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        ModelCategory model = ds.getValue(ModelCategory.class);
                        if (model != null) {
                            categories.add(model);
                        }
                    }

                    updateCategoriesAdapter(categories);
                } catch (Exception e) {
                    Log.e(TAG, "Error loading categories: " + e.getMessage());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                handleDatabaseError(error);
            }
        });
    }

    private void updateCategoriesAdapter(ArrayList<ModelCategory> categories) {
        if (getContext() == null || binding == null) return;

        categoryAdapter = new AdapterCategoryHome(getContext(), categories);
        binding.theloaiRCV.setAdapter(categoryAdapter);
    }

    private void safeNavigateToMostDownloaded() {
        try {
            if (isAdded() && getActivity() != null) {
                Intent intent = new Intent(getActivity(), DashBoardUserActivity.class);
                intent.putExtra("category", "Đọc nhiều nhất");
                startActivity(intent);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to most downloaded: " + e.getMessage());
            safeShowToast("Không thể chuyển trang");
        }
    }

    private void safeNavigateToDashboard() {
        try {
            if (isAdded() && getActivity() != null) {
                startActivity(new Intent(getActivity(), DashBoardUserActivity.class));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to dashboard: " + e.getMessage());
            safeShowToast("Không thể chuyển trang");
        }
    }

    private void safeShowToast(String message) {
        try {
            if (isAdded() && getContext() != null) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error showing toast: " + e.getMessage());
        }
    }

    private void handleDatabaseError(DatabaseError error) {
        Log.e(TAG, "Database error: " + error.getMessage());
        safeShowToast("Đã xảy ra lỗi khi tải dữ liệu");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        try {
            binding = null;
            allBooksArrayList = null;
            mostDownloadedArrayList = null;
            adapterPdfListUser = null;
            categoryAdapter = null;
        } catch (Exception e) {
            Log.e(TAG, "Error in onDestroyView: " + e.getMessage());
        }
    }
}