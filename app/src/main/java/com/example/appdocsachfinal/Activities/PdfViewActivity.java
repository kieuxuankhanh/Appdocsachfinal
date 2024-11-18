package com.example.appdocsachfinal.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.appdocsachfinal.databinding.ActivityPdfViewBinding;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.github.barteksc.pdfviewer.util.FitPolicy;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;

public class PdfViewActivity extends AppCompatActivity {

    private ActivityPdfViewBinding binding;
    private String bookId;
    private static final String TAG = "PDF_VIEW_TAG";
    private DatabaseReference ref;
    private int lastReadPage = 0;
    private PDFView pdfView;
    private String pdfUrl;
    private File pdfFile;
//    private SwipeRefreshLayout swipeRefreshLayout;

    private static final int CHUNK_SIZE = 1024 * 1024; // 1MB chunks
    private static final int MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB limit
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private int currentRetryAttempt = 0;
    private static final long CACHE_EXPIRATION_TIME = 7 * 24 * 60 * 60 * 1000; // 7 days

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize views
        pdfView = binding.pdfView;
        binding.txtnamebook.setSelected(true);
//        swipeRefreshLayout = binding.swipeRefresh;

        // Restore state if available
        if (savedInstanceState != null) {
            lastReadPage = savedInstanceState.getInt("lastReadPage", 0);
            bookId = savedInstanceState.getString("bookId");
            pdfUrl = savedInstanceState.getString("pdfUrl");
        } else {
            Intent intent = getIntent();
            bookId = intent.getStringExtra("bookId");
        }

        ref = FirebaseDatabase.getInstance().getReference("Books");

        // Configure PDF view for configuration changes
        configureViewer();

        if (isNetworkAvailable()) {
            loadBookDetails();
        } else {
            loadFromCache();
        }

        binding.btnback.setOnClickListener(v -> {
            saveLastReadPage();
            onBackPressed();
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("lastReadPage", pdfView.getCurrentPage());
        outState.putString("bookId", bookId);
        outState.putString("pdfUrl", pdfUrl);
    }

    private void configureViewer() {
        // Lock to portrait mode to prevent issues during rotation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
            return capabilities != null && (
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
        }
        return false;
    }

    private void loadFromCache() {
        File cacheFile = new File(getCacheDir(), "pdf_" + bookId + ".pdf");
        if (cacheFile.exists() && !isCacheExpired(cacheFile)) {
            loadPdfFromFile(cacheFile);
        } else {
            showNoConnectionError();
        }
    }

    private boolean isCacheExpired(File cacheFile) {
        long lastModified = cacheFile.lastModified();
        return System.currentTimeMillis() - lastModified > CACHE_EXPIRATION_TIME;
    }

    private void showNoConnectionError() {
        binding.progressBar.setVisibility(View.GONE);
//        swipeRefreshLayout.setRefreshing(false);
        Toast.makeText(this, "Không có kết nối mạng", Toast.LENGTH_SHORT).show();
    }

    private void loadBookDetails() {
        Log.d(TAG, "LoadBookDetails: Getting PDF URL");
        binding.progressBar.setVisibility(View.VISIBLE);

        ref.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        try {
                            String title = snapshot.child("title").getValue(String.class);
                            pdfUrl = snapshot.child("url").getValue(String.class);
                            Long fileSize = snapshot.child("fileSize").getValue(Long.class);

                            if (snapshot.hasChild("lastReadPage")) {
                                lastReadPage = Integer.parseInt(String.valueOf(snapshot.child("lastReadPage").getValue()));
                            }

                            if (title != null) {
                                binding.txtnamebook.setText(title);
                            }

                            // Check file size
                            if (fileSize != null && fileSize > MAX_FILE_SIZE) {
                                Toast.makeText(PdfViewActivity.this, "Tệp quá lớn", Toast.LENGTH_LONG).show();
                                return;
                            }

                            if (pdfUrl != null) {
                                // Check cache first
                                File cacheFile = new File(getCacheDir(), "pdf_" + bookId + ".pdf");
                                if (cacheFile.exists() && !isCacheExpired(cacheFile)) {
                                    loadPdfFromFile(cacheFile);
                                } else {
                                    loadBookFromUrl();
                                }
                            }
                        } catch (Exception e) {
                            handleError("Lỗi tải: " + e.getMessage());
                        } finally {
//                            swipeRefreshLayout.setRefreshing(false);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        handleError("Lỗi dữ liệu: " + error.getMessage());
//                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
    }

    private void loadBookFromUrl() {
        if (!isNetworkAvailable()) {
            showNoConnectionError();
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);

        StorageReference reference = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);

        // Create cache file
        File cacheFile = new File(getCacheDir(), "pdf_" + bookId + ".pdf");

        reference.getFile(cacheFile)
                .addOnSuccessListener(taskSnapshot -> {
                    loadPdfFromFile(cacheFile);
                })
                .addOnFailureListener(e -> {
                    if (currentRetryAttempt < MAX_RETRY_ATTEMPTS) {
                        currentRetryAttempt++;
                        new Handler().postDelayed(this::loadBookFromUrl, 1000 * currentRetryAttempt);
                    } else {
                        handleError("lỗi tải tệp PDF: " + e.getMessage());
                    }
                })
                .addOnProgressListener(snapshot -> {
                    double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                    binding.progressBar.setProgress((int) progress);
                });
    }
private void loadPdfFromFile(File pdfFile) {
    try {
        pdfView.fromFile(pdfFile)
                .defaultPage(lastReadPage)
                .enableSwipe(true)
                .swipeHorizontal(false)
                .enableDoubletap(true)
                .enableAnnotationRendering(false)
                .scrollHandle(new DefaultScrollHandle(this))
                .spacing(0) // Giảm spacing
                .autoSpacing(false)
                // Cài đặt fit khác
                .pageFitPolicy(FitPolicy.WIDTH) // Fit theo width trước
                .fitEachPage(true)
                .pageSnap(false)
                .nightMode(false)
                .enableAntialiasing(true)
                .pageFling(true)
                .onLoad(nbPages -> {
                    // Tự động điều chỉnh zoom sau khi load
                    float width = pdfView.getWidth();
                    float height = pdfView.getHeight();
                    float ratio = width / height;
                    pdfView.setMinZoom(ratio * 0.5f);
                    pdfView.setMidZoom(ratio);
                    pdfView.setMaxZoom(ratio * 3.0f);
                })
                .onPageChange((page, pageCount) -> {
                    int currentPage = page + 1;
                    binding.txtpage.setText(currentPage + "/" + pageCount);
                })
                .onError(t -> handleError("Lỗi tải tệp PDF: " + t.getMessage()))
                .onPageError((page, t) -> handleError("Lỗi ở trang " + page + ": " + t.getMessage()))
                .load();

        binding.progressBar.setVisibility(View.GONE);
    } catch (Exception e) {
        handleError("Lỗi tải tệp: " + e.getMessage());
    }
}

    private void handleError(String errorMessage) {
        binding.progressBar.setVisibility(View.GONE);
//        swipeRefreshLayout.setRefreshing(false);
        Log.e(TAG, errorMessage);
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
    }

    private void cleanupOldCache() {
        File[] cacheFiles = getCacheDir().listFiles((dir, name) -> name.startsWith("pdf_"));
        if (cacheFiles != null) {
            for (File file : cacheFiles) {
                if (isCacheExpired(file)) {
                    file.delete();
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveLastReadPage();
    }

    private void saveLastReadPage() {
        if (pdfView != null && pdfView.getCurrentPage() >= 0) {
            ref.child(bookId).child("lastReadPage").setValue(pdfView.getCurrentPage());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cleanupOldCache();
    }
}