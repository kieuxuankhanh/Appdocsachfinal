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
import com.denzcoskun.imageslider.interfaces.ItemClickListener;
import com.denzcoskun.imageslider.models.SlideModel;
import com.example.appdocsachfinal.Activities.DashBoardUserActivity;
import com.example.appdocsachfinal.Activities.PdfDetailActivity;
import com.example.appdocsachfinal.Adapter.AdapterCategoryHome;
import com.example.appdocsachfinal.Adapter.AdapterPdfListUser;
import com.example.appdocsachfinal.Model.ModelCategory;
import com.example.appdocsachfinal.Model.ModelListPdf;
import com.example.appdocsachfinal.databinding.FragmentHomeUserBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

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

        private FirebaseAuth firebaseAuth;

        private boolean isDataLoading = false;
        private boolean isAdded = false;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            try {
                super.onCreate(savedInstanceState);
                isAdded = true;
                initializeFirebaseRefs();
            } catch (Exception e) {
                Log.e(TAG, "Error in onCreate: " + e.getMessage());
            }
        }

        private void initializeFirebaseRefs() {
            try {
                if (getActivity() == null) return;

                FirebaseDatabase database = FirebaseDatabase.getInstance();
                if (database != null) {
                    booksRef = database.getReference("Books");
                    categoriesRef = database.getReference("Categories");
                    imagesRef = database.getReference("Books");
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to initialize Firebase references: " + e.getMessage());
                safeShowToast("Không thể kết nối đến cơ sở dữ liệu");
            }
        }

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            try {
                if (getActivity() == null) return null;

                firebaseAuth = FirebaseAuth.getInstance();
                binding = FragmentHomeUserBinding.inflate(inflater, container, false);

                if (binding != null) {
                    return initializeView();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error in onCreateView: " + e.getMessage());
            }
            return null;
        }

        private View initializeView() {
            try {
                if (binding == null || getActivity() == null) return null;

                initializeArrayLists();
                getIntentData();
                setupClickListeners();
                setupSwipeRefresh();
                initializeRecyclerViews();

                if (!isDataLoading) {
                    loadAllData();
                }

                return binding.getRoot();
            } catch (Exception e) {
                Log.e(TAG, "Error in initializeView: " + e.getMessage());
                return null;
            }
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
            try {
                if (binding == null) return;

                View.OnClickListener dashboardClickListener = v -> safeNavigateToDashboard();
                View.OnClickListener mostDownloadedClickListener = v -> safeNavigateToMostDownloaded();

                binding.edtdecu.setOnClickListener(mostDownloadedClickListener);
                binding.edtdocnhieu.setOnClickListener(mostDownloadedClickListener);
                binding.buttonkhampha.setOnClickListener(dashboardClickListener);
                binding.imageSlider.setOnClickListener(dashboardClickListener);
            } catch (Exception e) {
                Log.e(TAG, "Error setting up click listeners: " + e.getMessage());
            }
        }

        private void setupSwipeRefresh() {
            try {
                if (binding != null && binding.swipeRefreshLayout != null) {
                    binding.swipeRefreshLayout.setOnRefreshListener(() -> {
                        if (!isDataLoading) {
                            loadAllData();
                        }
                        binding.swipeRefreshLayout.setRefreshing(false);
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "Error setting up swipe refresh: " + e.getMessage());
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
            try {
                if (getContext() == null || binding == null) return;

                GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 3);
                binding.booksRCV.setLayoutManager(gridLayoutManager);
                binding.booksRCV.setHasFixedSize(true);
            } catch (Exception e) {
                Log.e(TAG, "Error setting up books RecyclerView: " + e.getMessage());
            }
        }

        private void setupDownloadsRecyclerView() {
            try {
                if (getContext() == null || binding == null) return;

                LinearLayoutManager horizontalLayoutManager = new LinearLayoutManager(
                        getContext(), LinearLayoutManager.HORIZONTAL, false
                );
                binding.downloadRCV.setLayoutManager(horizontalLayoutManager);
                binding.downloadRCV.setHasFixedSize(true);
            } catch (Exception e) {
                Log.e(TAG, "Error setting up downloads RecyclerView: " + e.getMessage());
            }
        }

        private void setupCategoriesRecyclerView() {
            try {
                if (getContext() == null || binding == null) return;

                LinearLayoutManager categoryLayoutManager = new LinearLayoutManager(getContext());
                binding.theloaiRCV.setLayoutManager(categoryLayoutManager);
                binding.theloaiRCV.setHasFixedSize(true);
            } catch (Exception e) {
                Log.e(TAG, "Error setting up categories RecyclerView: " + e.getMessage());
            }
        }

        private void loadAllData() {
            try {
                if (!isAdded() || getContext() == null) return;

                isDataLoading = true;
                loadImageSlider();
                loadAllBooks();
                loadMostDownloadedBooks();
                loadCategories();
                loadUserInfo();
                isDataLoading = false;
            } catch (Exception e) {
                Log.e(TAG, "Error loading all data: " + e.getMessage());
                isDataLoading = false;
            }
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
            try {
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
                                    Log.e(TAG, "Error processing books data: " + e.getMessage());
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                handleDatabaseError(error);
                            }
                        });
            } catch (Exception e) {
                Log.e(TAG, "Error loading books: " + e.getMessage());
            }
        }

        private void loadMostDownloadedBooks() {
            try {
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
                                    Log.e(TAG, "Error processing downloaded books data: " + e.getMessage());
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                handleDatabaseError(error);
                            }
                        });
            } catch (Exception e) {
                Log.e(TAG, "Error loading most downloaded books: " + e.getMessage());
            }
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
                    // Thêm category "Khác" vào cuối
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



        private void loadUserInfo() {
            try {
                if (firebaseAuth == null || firebaseAuth.getUid() == null || !isAdded()) return;

                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
                reference.child(firebaseAuth.getUid())
                        .addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                try {
                                    if (!isAdded() || binding == null) return;

                                    if (snapshot.exists()) {
                                        String name = snapshot.child("name").exists() ?
                                                String.valueOf(snapshot.child("name").getValue()) : "";

                                        if (!name.isEmpty()) {
                                            binding.txtname.setText(name);
                                            binding.txtname.setVisibility(View.VISIBLE);
                                        } else {
                                            binding.txtname.setVisibility(View.GONE);
                                        }
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "Error processing user data: " + e.getMessage());
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                handleDatabaseError(error);
                            }
                        });
            } catch (Exception e) {
                Log.e(TAG, "Error loading user info: " + e.getMessage());
            }
        }

        private void updateBooksAdapter() {
            try {
                if (getContext() == null || binding == null || !isAdded()) return;

                adapterPdfListUser = new AdapterPdfListUser(getContext(), allBooksArrayList);
                binding.booksRCV.setAdapter(adapterPdfListUser);
            } catch (Exception e) {
                Log.e(TAG, "Error updating books adapter: " + e.getMessage());
            }
        }

        private void updateDownloadsAdapter() {
            try {
                if (getContext() == null || binding == null || !isAdded()) return;

                AdapterPdfListUser downloadsAdapter = new AdapterPdfListUser(getContext(), mostDownloadedArrayList);
                binding.downloadRCV.setAdapter(downloadsAdapter);
            } catch (Exception e) {
                Log.e(TAG, "Error updating downloads adapter: " + e.getMessage());
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
            try {
                super.onDestroyView();
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