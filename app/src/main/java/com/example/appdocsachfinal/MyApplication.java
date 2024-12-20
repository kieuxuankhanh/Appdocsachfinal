package com.example.appdocsachfinal;
import static com.example.appdocsachfinal.Constants.MAX_BYTES_PDF;

import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;


import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class MyApplication  extends Application {
    private static final String TAG_DOWNLOAD = "DOWNLOAD_TAG";
    @Override
    public void onCreate() {
        super.onCreate();
    }
    public static String formatTimestamp(long timestamp){
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(timestamp);
        String date = DateFormat.format("dd/MM/yyyy",cal).toString();
        return date;
    }

    public static void deleteBook(Context context, String bookId, String bookUrl, String bookTitle) {
        String TAG = "DELETE_BOOK_TAG";
        Log.d(TAG, "deleteBook: Đang xóa...");
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Đợi một chút nha!!!");
        progressDialog.show();

        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl);
        storageReference.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "OnSuccess: Xóa khỏi bộ nhớ ");
                        Log.d(TAG, "OnSuccess: Xóa khỏi dữ liệu ");

                        // Xóa từ bảng Books
                        DatabaseReference booksRef = FirebaseDatabase.getInstance().getReference("Books");
                        booksRef.child(bookId).removeValue()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        // Sau khi xóa sách thành công, xóa khỏi tất cả Favorites
                                        DatabaseReference favRef = FirebaseDatabase.getInstance().getReference("Users");
                                        favRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                                                    // Kiểm tra và xóa sách khỏi Favorites của mỗi user
                                                    if (userSnapshot.child("Favorites").exists()) {
                                                        DatabaseReference userFavRef = favRef.child(userSnapshot.getKey()).child("Favorites").child(bookId);
                                                        userFavRef.removeValue();
                                                    }
                                                }
                                                progressDialog.dismiss();
                                                Toast.makeText(context, "Xóa sách thành công", Toast.LENGTH_SHORT).show();
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {
                                                progressDialog.dismiss();
                                                Toast.makeText(context, "" + error.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(TAG, "onFailure: Xóa sách thất bại " + e.getMessage());
                                        progressDialog.dismiss();
                                        Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: Xóa sách bị lỗi " + e.getMessage());
                        progressDialog.dismiss();
                        Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    public static void LoadPdfSize(String pdfUrl, String pdfTitle, TextView txtsize) {
        String TAG = "PDF_SIZE_TAG";
        StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        ref.getMetadata()
                .addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                    @Override
                    public void onSuccess(StorageMetadata storageMetadata) {
                        double bytes = storageMetadata.getSizeBytes();
                        Log.d(TAG,"onSuccess: "+pdfTitle+" "+bytes);
                        double kb = bytes/1024;
                        double mb = kb/1024;

                        if (mb>=1){
                            txtsize.setText(String.format("%.2f",mb)+" MB");
                        } else if (kb>=1) {
                            txtsize.setText(String.format("%.2f",kb)+" KB");
                        }
                        else {
                            txtsize.setText(String.format("%.2f",bytes)+" bytes");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG,"onFailure: "+e.getMessage());
                    }
                });
    }
    public static void loadPdfFromUrlSinglePage(String pdfUrl, String pdfTitle, PDFView pdfView, ProgressBar progressBar,TextView txtpage) {
        String TAG = "PDF_LOAD_SINGLE_TAG";
        StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        ref.getBytes(MAX_BYTES_PDF)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Log.d(TAG,"onSuccess: "+pdfTitle+"Da lay duoc file");

                        pdfView.fromBytes(bytes)
                                .pages(0)
                                .spacing(0)
                                .swipeHorizontal(false)
                                .enableSwipe(false)
                                .onError(new OnErrorListener() {
                                    @Override
                                    public void onError(Throwable t) {
                                        progressBar.setVisibility(View.INVISIBLE);
                                        Log.d(TAG, "onError: "+t.getMessage());
                                    }
                                })
                                .onPageError(new OnPageErrorListener() {
                                    @Override
                                    public void onPageError(int page, Throwable t) {
                                        progressBar.setVisibility(View.INVISIBLE);
                                        Log.d(TAG,"onPageError: "+t.getMessage());
                                    }
                                })
                                .onLoad(new OnLoadCompleteListener() {
                                    @Override
                                    public void loadComplete(int nbPages) {
                                        progressBar.setVisibility(View.INVISIBLE);
                                        Log.d(TAG,"LoadComplete: pdf loaded");
                                        if (txtpage != null){
                                            txtpage.setText(""+nbPages);
                                        }
                                    }
                                })
                                .load();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG,"onFailure: Tải file thất bại"+e.getMessage());
                    }
                });
    }
    public static void loadImageFromUrl(String bookId, ImageView imageView, ProgressBar progressBar) {
        String TAG = "IMAGE_LOAD_TAG";
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");

        ref.child(bookId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String imageThumbUrl = snapshot.child("imageThumb").getValue(String.class);

                if (imageThumbUrl == null || imageThumbUrl.isEmpty()) {
                    Log.e(TAG, "Image URL is null or empty");
                    progressBar.setVisibility(View.INVISIBLE);
                    imageView.setImageResource(R.drawable.skeleton);
                    return;
                }

                try {
                    if (imageView.getContext() == null) {
                        Log.e(TAG, "Context is null");
                        return;
                    }

                    Glide.with(imageView.getContext())
                            .load(imageThumbUrl)
                            .listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                    progressBar.setVisibility(View.INVISIBLE);
                                    Log.e(TAG, "Glide load failed: " + (e != null ? e.getMessage() : "unknown error"));
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                    progressBar.setVisibility(View.INVISIBLE);
                                    Log.d(TAG, "Hiển thị ảnh thành công");
                                    return false;
                                }
                            })
                            .placeholder(R.drawable.skeleton)
                            .error(R.drawable.warning)
                            .into(imageView);

                } catch (Exception e) {
                    progressBar.setVisibility(View.INVISIBLE);
                    Log.e(TAG, "Exception in loadImageFromUrl: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.INVISIBLE);
                Log.e(TAG, "Database error: " + error.getMessage());
            }
        });
    }

    public static void loadCategory(String categoryId, TextView txttheloai) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.child(categoryId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String category = ""+snapshot.child("category").getValue();
                        txttheloai.setText(category);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
    public static void incrementBookViewCount(String bookId){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String viewsCount = ""+snapshot.child("viewsCount").getValue();
                        if (viewsCount.equals("") || viewsCount.equals("null")){
                            viewsCount = "0";
                        }
                        long newviewsCount = Long.parseLong(viewsCount) + 1;
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("viewsCount", newviewsCount);
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Books");
                        reference.child(bookId)
                                .updateChildren(hashMap);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
    public static void downloadBook(Context context,String bookId, String bookTitle, String bookUrl){
        Log.d(TAG_DOWNLOAD, "downloadBook: Đang tải sách");
        String nameWithExtension = bookTitle + ".pdf";
        Log.d(TAG_DOWNLOAD,"downloadBook: NAME: "+nameWithExtension);
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Doi ty");
        progressDialog.setMessage("Dang tai "+nameWithExtension+"...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl);
        storageReference.getBytes(MAX_BYTES_PDF)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Log.d(TAG_DOWNLOAD,"onSuccess: Tai sach thanh cong");
                        Log.d(TAG_DOWNLOAD,"onSuccess: Dang luu sach");
                        saveDownloadedBook(context,progressDialog,bytes,nameWithExtension,bookId);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG_DOWNLOAD,"onFailure: Tải xuống thất bại" +e.getMessage());
                        progressDialog.dismiss();
                        Toast.makeText(context, "Tải xuống thất bại"+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private static void saveDownloadedBook(Context context, ProgressDialog progressDialog, byte[] bytes, String nameWithExtension, String bookId) {
        Log.d(TAG_DOWNLOAD, "saveDownloadedBook: Đã lưu sách tải xuống");
        try{
            File downloadFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            downloadFolder.mkdir();
            String filePath = downloadFolder.getPath()+"/"+nameWithExtension;
            FileOutputStream out = new FileOutputStream(filePath);
            out.write(bytes);
            out.close();
            Toast.makeText(context, "Tải về máy thành công", Toast.LENGTH_SHORT).show();
            Log.d(TAG_DOWNLOAD,"saveDownloadedBook: Tải về máy thành công");
            progressDialog.dismiss();
            incrementBookDownloadCount(bookId);
            
        }catch (Exception e){
            Log.d(TAG_DOWNLOAD, "saveDownloadedBook: Thất bại"+e.getMessage());
            Toast.makeText(context, "Thất bại"+e.getMessage(), Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
        }
    }
    private static void incrementBookDownloadCount(String bookId) {
        Log.d(TAG_DOWNLOAD, "incrementBookDownloadCount: Đếm số lượt tải");
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String downloadsCount = ""+snapshot.child("downloadsCount").getValue();
                        Log.d(TAG_DOWNLOAD,"onDataChange: Số lượt tải: "+downloadsCount);
                        if (downloadsCount.equals("")||downloadsCount.equals("null")){
                            downloadsCount = "0";
                        }
                        long newDownloadsCount = Long.parseLong(downloadsCount)+1;
                        Log.d(TAG_DOWNLOAD,"onDataChange: Lượt tải về mới: "+newDownloadsCount);

                        HashMap<String,Object> hashMap = new HashMap<>();
                        hashMap.put("downloadsCount", newDownloadsCount);
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Books");
                        reference.child(bookId).updateChildren(hashMap)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Log.d(TAG_DOWNLOAD,"onSuccess: Cập nhật lượt tải xuống");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(TAG_DOWNLOAD,"onFailure: Cập nhật lượt tải xuống thất bại"+e.getMessage());
                                    }
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
    public static void addToFavorite(Context context,String bookId){
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        long timestamp = System.currentTimeMillis();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("bookId",""+bookId);
        hashMap.put("timestamp",""+timestamp);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Favorites").child(bookId)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(context, "Yêu Thích", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    public static void removeFromFavorite(Context context,String bookId){
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Favorites").child(bookId)
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(context, "Xóa yêu thích", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
