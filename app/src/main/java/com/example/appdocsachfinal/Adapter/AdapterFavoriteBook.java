package com.example.appdocsachfinal.Adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appdocsachfinal.Activities.PdfDetailActivity;
import com.example.appdocsachfinal.Model.ModelPdf;
import com.example.appdocsachfinal.MyApplication;
import com.example.appdocsachfinal.databinding.ActivityFavoriteBookBinding;
import com.example.appdocsachfinal.databinding.RowPdfFavoriteBinding;
import com.github.barteksc.pdfviewer.PDFView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AdapterFavoriteBook extends RecyclerView.Adapter<AdapterFavoriteBook.HolderFavoriteBook>{

    private Context context;
    private ArrayList<ModelPdf> pdfArrayList;
    private RowPdfFavoriteBinding binding;
    private static final String TAG = "FAV_BOOK_TAG";

    public AdapterFavoriteBook(Context context, ArrayList<ModelPdf> pdfArrayList) {
        this.context = context;
        this.pdfArrayList = pdfArrayList;
    }

    @NonNull
    @Override
    public HolderFavoriteBook onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = RowPdfFavoriteBinding.inflate(LayoutInflater.from(context),parent,false);
        return new HolderFavoriteBook(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderFavoriteBook holder, int position) {
        ModelPdf modelPdf = pdfArrayList.get(position);
        loadBookDetails(modelPdf,holder);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PdfDetailActivity.class);
                intent.putExtra("bookId", modelPdf.getId());
                context.startActivity(intent);
            }
        });
        holder.btnfavor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyApplication.removeFromFavorite(context, modelPdf.getId());
            }
        });
    }

    private void loadBookDetails(ModelPdf modelPdf, HolderFavoriteBook holder) {
        String bookId = modelPdf.getId();
        Log.d(TAG, "LoadBookDetails: Chi tiet: "+bookId);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String bookTitle = ""+snapshot.child("title").getValue();
                        String description = ""+snapshot.child("description").getValue();
                        String categoryId = ""+snapshot.child("categoryId").getValue();
                        String bookUrl = ""+snapshot.child("url").getValue();
                        String timestamp = ""+snapshot.child("timestamp").getValue();
                        String uid = ""+snapshot.child("uid").getValue();
                        String viewsCount = ""+snapshot.child("viewsCount").getValue();
                        String downloadsCount = ""+snapshot.child("downloadsCount").getValue();

                        modelPdf.setFavorite(true);
                        modelPdf.setTitle(bookTitle);
                        modelPdf.setDescription(description);
                        modelPdf.setTimestamp(Long.parseLong(timestamp));
                        modelPdf.setCategoryId(categoryId);
                        modelPdf.setUid(uid);
                        modelPdf.setUrl(bookUrl);

                        String date = MyApplication.formatTimestamp(Long.parseLong(timestamp));

                        MyApplication.loadCategory(categoryId,holder.txttheloai);
                        MyApplication.loadPdfFromUrlSinglePage(""+bookUrl,""+bookTitle,holder.pdfView,holder.progressBar,null);
                        MyApplication.LoadPdfSize(""+bookUrl,""+bookTitle,holder.txtsize);

                        holder.txttitle.setText(bookTitle);
                        holder.txtdes.setText(description);
                        holder.txtdate.setText(date);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return pdfArrayList.size();
    }

    class HolderFavoriteBook extends RecyclerView.ViewHolder{

        PDFView pdfView;
        ProgressBar progressBar;
        TextView txttitle, txtdes, txttheloai, txtsize,txtdate;
        ImageButton btnfavor;

        public HolderFavoriteBook(@NonNull View itemView) {
            super(itemView);

            pdfView = binding.pdfView;
            progressBar = binding.progressBar;
            txttitle = binding.txttitle;
            txtdes = binding.txtdes;
            txttheloai = binding.txttheloai;
            txtsize = binding.txtsize;
            txtdate = binding.txtdate;
            btnfavor = binding.btnfavor;
        }
    }
}