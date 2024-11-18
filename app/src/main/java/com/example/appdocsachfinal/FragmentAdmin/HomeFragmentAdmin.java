package com.example.appdocsachfinal.FragmentAdmin;

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
import com.denzcoskun.imageslider.interfaces.ItemChangeListener;
import com.denzcoskun.imageslider.interfaces.ItemClickListener;
import com.denzcoskun.imageslider.models.SlideModel;
import com.example.appdocsachfinal.Activities.DashBoardUserActivity;
import com.example.appdocsachfinal.Activities.PdfDetailActivity;
import com.example.appdocsachfinal.Adapter.AdapterCategoryHome;
import com.example.appdocsachfinal.Adapter.AdapterPdfListUser;
import com.example.appdocsachfinal.Model.ModelCategory;
import com.example.appdocsachfinal.Model.ModelListPdf;
import com.example.appdocsachfinal.databinding.FragmentHomeAdminBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HomeFragmentAdmin extends Fragment {
    private static final String TAG = "HomeFragmentAdmin";
    private static final int MAX_BOOKS_TO_SHOW = 9;
    private static final int MAX_DOWNLOADED_BOOKS = 10;

    private FragmentHomeAdminBinding binding;
    private ArrayList<ModelListPdf> allBooksArrayList;
    private ArrayList<ModelListPdf> mostDownloadedArrayList;
    private AdapterPdfListUser adapterPdfListUser;
    private AdapterCategoryHome categoryAdapter;
    private String categoryId, categoryTitle;

    private DatabaseReference booksRef;
    private DatabaseReference categoriesRef;
    private DatabaseReference imagesRef;

    private FirebaseAuth firebaseAuth;

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
            imagesRef = database.getReference("Books");
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize Firebase references: " + e.getMessage());
            safeShowToast("Không thể kết nối đến cơ sở dữ liệu");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        try {
            firebaseAuth = FirebaseAuth.getInstance();
            binding = FragmentHomeAdminBinding.inflate(inflater, container, false);
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
        loadUserInfo();

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
            binding.edtdecu.setOnClickListener(v -> safeNavigateToMostDownloaded());
            binding.edtdocnhieu.setOnClickListener(v -> safeNavigateToMostDownloaded());
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
        try {
            if (imagesRef == null || !isAdded() || binding == null) return;

            final List<SlideModel> imageList = new ArrayList<>();
            final List<String> bookIdList = new ArrayList<>();

            imagesRef.limitToFirst(8).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    try {
                        if (!isAdded() || binding == null) return;

                        imageList.clear();
                        bookIdList.clear();
                        int count = 0;
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            if (count >= 5) break;

                            String imageUrl = ds.child("imageThumb").getValue(String.class);
                            String bookId = ds.child("id").getValue(String.class);

                            if (imageUrl != null && bookId != null) {
                                imageList.add(new SlideModel(imageUrl, ScaleTypes.FIT));
                                bookIdList.add(bookId);
                                count++;
                            }
                        }

                        if (!imageList.isEmpty() && binding.imageSlider != null) {
                            binding.imageSlider.setImageList(imageList, ScaleTypes.FIT);

                            // Xử lý click với interface đầy đủ
                            binding.imageSlider.setItemClickListener(new ItemClickListener() {
                                @Override
                                public void doubleClick(int i) {

                                }

                                @Override
                                public void onItemSelected(int position) {
                                    if (position < bookIdList.size()) {
                                        String bookId = bookIdList.get(position);
                                        Intent intent = new Intent(requireActivity(), PdfDetailActivity.class);
                                        intent.putExtra("bookId", bookId);
                                        startActivity(intent);
                                    }
                                }
                            });
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing image slider data: " + e.getMessage());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    handleDatabaseError(error);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error loading image slider: " + e.getMessage());
        }
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

        booksRef.orderByChild("viewsCount")
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
        try {
            if (categoriesRef == null || !isAdded()) return;

            categoriesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    try {
                        if (!isAdded() || binding == null) return;

                        ArrayList<ModelCategory> categories = new ArrayList<>();
                        ModelCategory khacCategory = null;

                        for (DataSnapshot ds : snapshot.getChildren()) {
                            ModelCategory model = ds.getValue(ModelCategory.class);
                            if (model != null) {
                                if (model.getCategory().equals("Khác")) {
                                    khacCategory = model;
                                } else {
                                    categories.add(model);
                                }
                            }
                        }
                        if (khacCategory != null) {
                            categories.add(khacCategory);
                        }

                        updateCategoriesAdapter(categories);
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing categories data: " + e.getMessage());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    handleDatabaseError(error);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error loading categories: " + e.getMessage());
        }
    }

    private void updateCategoriesAdapter(ArrayList<ModelCategory> categories) {
        try {
            if (getContext() == null || binding == null || !isAdded()) return;

            categoryAdapter = new AdapterCategoryHome(getContext(), categories);
            binding.theloaiRCV.setAdapter(categoryAdapter);
        } catch (Exception e) {
            Log.e(TAG, "Error updating categories adapter: " + e.getMessage());
        }
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
    private void loadUserInfo() {
        if (firebaseAuth.getUid() == null) {
            Log.e(TAG, "User ID is null");
            return;
        }

        try {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
            reference.child(firebaseAuth.getUid())
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            try {
                                if (snapshot.exists()) {
                                    String name = snapshot.child("name").exists() ?
                                            snapshot.child("name").getValue().toString() : "";
                                    if (!name.isEmpty()) {
                                        binding.txtname.setText(name);
                                        binding.txtname.setVisibility(View.VISIBLE);
                                    } else {
                                        binding.txtname.setVisibility(View.GONE);
                                    }

                                } else {
                                    Log.e(TAG, "User data does not exist in database");
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error processing user data: " + e.getMessage());
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e(TAG, "Database error: " + error.getMessage());
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error loading user info: " + e.getMessage());
        }
    }
}