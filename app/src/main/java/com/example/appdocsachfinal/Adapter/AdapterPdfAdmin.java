package com.example.appdocsachfinal.Adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appdocsachfinal.Filter.FilterPdfAdmin;
import com.example.appdocsachfinal.Model.ModelPdf;
import com.example.appdocsachfinal.Activities.PdfDetailActivity;
import com.example.appdocsachfinal.Activities.PdfEditActivity;
import com.example.appdocsachfinal.MyApplication;
import com.example.appdocsachfinal.databinding.RowPdfAdminBinding;
import com.github.barteksc.pdfviewer.PDFView;

import java.util.ArrayList;

public class AdapterPdfAdmin extends RecyclerView.Adapter<AdapterPdfAdmin.HolderPdfAdmin> implements Filterable {

    private Context context;
    public ArrayList<ModelPdf> pdfArrayList, filterList;
    private RowPdfAdminBinding binding;
    private static final String TAG = "PDF_ADAPTER_TAG";
    private ProgressDialog progressDialog;

    private FilterPdfAdmin filterPdfAdmin;
    public AdapterPdfAdmin(Context context, ArrayList<ModelPdf> pdfArrayList) {
        this.context = context;
        this.pdfArrayList = pdfArrayList;
        this.filterList = pdfArrayList;
        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Đợi một chút nha!!!");
        progressDialog.setCanceledOnTouchOutside(false);
    }

    @NonNull
    @Override
    public HolderPdfAdmin onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = RowPdfAdminBinding.inflate(LayoutInflater.from(context),parent,false);
        return new HolderPdfAdmin(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderPdfAdmin holder, int position) {
        ModelPdf modelPdf = pdfArrayList.get(position);
        String pdfId = modelPdf.getId();
        String categoryId = modelPdf.getCategoryId();
        String title = modelPdf.getTitle();
        String description = modelPdf.getDescription();
        String pdfUrl = modelPdf.getUrl();
        long timestamp = modelPdf.getTimestamp();

        String formattedDate = MyApplication.formatTimestamp(timestamp);
        holder.txttitle.setText(title);
        holder.txtdes.setText(description);
        holder.txtdate.setText(formattedDate);
        MyApplication.loadCategory(
                ""+categoryId,
                holder.txttheloai
        );
        MyApplication.loadImageFromUrl(""+pdfId,holder.imageThumb,holder.progressBar);
        MyApplication.LoadPdfSize(
                ""+pdfUrl,
                ""+title,
                holder.txtsize);

        holder.btnmore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moreOptionDialog(modelPdf,holder);
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PdfDetailActivity.class);
                intent.putExtra("bookId",pdfId);
                context.startActivity(intent);
            }
        });
    }

    private void moreOptionDialog(ModelPdf modelPdf, HolderPdfAdmin holder) {
        String bookId = modelPdf.getId();
        String bookUrl = modelPdf.getUrl();
        String bookTitle = modelPdf.getTitle();

        String[] options = {"Sửa","Xóa"};
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Chọn tùy chọn")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0){
                            Intent intent = new Intent(context, PdfEditActivity.class);
                            intent.putExtra("bookId",bookId);
                            context.startActivity(intent);
                        } else if (which == 1) {
                           MyApplication.deleteBook(context,
                                   ""+bookId,
                                   ""+bookUrl,
                                   ""+bookTitle
                           );
                           //deleteBook(model,holder);
                        }
                    }
                })
                .show();
    }

    @Override
    public int getItemCount() {
        return pdfArrayList.size();
    }

    @Override
    public Filter getFilter() {
        if (filterPdfAdmin == null){
            filterPdfAdmin = new FilterPdfAdmin(filterList,this);
        }
        return filterPdfAdmin;
    }

    class HolderPdfAdmin extends RecyclerView.ViewHolder{
//        PDFView pdfView;
        ImageView imageThumb;
        ProgressBar progressBar;
        TextView txttitle,txtdes,txttheloai,txtsize,txtdate;
        ImageButton btnmore;
        public HolderPdfAdmin(@NonNull View itemView) {
            super(itemView);

//            pdfView =  binding.pdfView;
            imageThumb = binding.ImageThumb;
            progressBar = binding.progressBar;
            txttitle = binding.txttitle;
            txtdes = binding.txtdes;
            txttheloai = binding.txttheloai;
            txtsize = binding.txtsize;
            txtdate = binding.txtdate;
            btnmore = binding.btnmore;
        }
    }
}
