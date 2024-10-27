package com.example.appdocsachfinal.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appdocsachfinal.Activities.PdfDetailActivity;
import com.example.appdocsachfinal.Model.ModelListPdf;
import com.example.appdocsachfinal.MyApplication;
import com.example.appdocsachfinal.databinding.RowPdfListBinding;
import com.github.barteksc.pdfviewer.PDFView;

import java.util.ArrayList;

public class AdapterPdfListUser extends RecyclerView.Adapter<AdapterPdfListUser.HolderPdfListUser> {
    private Context context;
    private ArrayList<ModelListPdf> pdfArrayListUser;

    public AdapterPdfListUser(Context context, ArrayList<ModelListPdf> pdfArrayListUser) {
        this.context = context;
        this.pdfArrayListUser = pdfArrayListUser;
    }

    @NonNull
    @Override
    public HolderPdfListUser onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RowPdfListBinding binding = RowPdfListBinding.inflate(LayoutInflater.from(context), parent, false);
        return new HolderPdfListUser(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderPdfListUser holder, int position) {
        ModelListPdf modelListPdf = pdfArrayListUser.get(position);
        String bookId = modelListPdf.getId();
        String title = modelListPdf.getTitle();
        String pdfUrl = modelListPdf.getUrl();

        holder.txttitle.setText(title);
        MyApplication.loadPdfFromUrlSinglePage(pdfUrl, title, holder.pdfView, holder.progressBar,null);
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, PdfDetailActivity.class);
            intent.putExtra("bookId", bookId);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return pdfArrayListUser.size();
    }

    class HolderPdfListUser extends RecyclerView.ViewHolder {
        TextView txttitle;
        PDFView pdfView;
        ProgressBar progressBar;

        public HolderPdfListUser(@NonNull RowPdfListBinding binding) {
            super(binding.getRoot());
            txttitle = binding.txttitle;
            pdfView = binding.pdfView;
            progressBar = binding.progressBar;
        }
    }
}