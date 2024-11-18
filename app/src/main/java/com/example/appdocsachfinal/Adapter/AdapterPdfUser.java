package com.example.appdocsachfinal.Adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.appdocsachfinal.Filter.FilterPdfUser;
import com.example.appdocsachfinal.Model.ModelPdf;
import com.example.appdocsachfinal.Activities.PdfDetailActivity;
import com.example.appdocsachfinal.MyApplication;
import com.example.appdocsachfinal.databinding.RowPdfUserBinding;
import com.github.barteksc.pdfviewer.PDFView;

import java.util.ArrayList;

public class AdapterPdfUser extends RecyclerView.Adapter<AdapterPdfUser.HolderPdfUser> implements Filterable {

    private Context context;
    public ArrayList<ModelPdf> pdfArrayList, filterList;
    private RowPdfUserBinding binding;
    private static final String TAG = "PDF_ADAPTER_TAG";
    private ProgressDialog progressDialog;

    private FilterPdfUser filterPdfUser;
    public AdapterPdfUser(Context context, ArrayList<ModelPdf> pdfArrayList) {
        this.context = context;
        this.pdfArrayList = pdfArrayList;
        this.filterList = pdfArrayList;
        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Doi ty");
        progressDialog.setCanceledOnTouchOutside(false);
    }

    @NonNull
    @Override
    public HolderPdfUser onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = RowPdfUserBinding.inflate(LayoutInflater.from(context),parent,false);
        return new HolderPdfUser(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderPdfUser holder, int position) {
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
//        MyApplication.loadPdfFromUrlSinglePage(
//                ""+pdfUrl,
//                ""+title,
//                holder.pdfView,
//                holder.progressBar
//                ,null
//        );
        MyApplication.loadImageFromUrl(""+pdfId,holder.imageThumb,holder.progressBar);
        MyApplication.LoadPdfSize(
                ""+pdfUrl,
                ""+title,
                holder.txtsize);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PdfDetailActivity.class);
                intent.putExtra("bookId",pdfId);
                context.startActivity(intent);
            }
        });
    }


    @Override
    public int getItemCount() {
        return pdfArrayList.size();
    }

    @Override
    public Filter getFilter() {
        if (filterPdfUser == null){
            filterPdfUser = new FilterPdfUser(filterList,this);
        }
        return filterPdfUser;
    }

    class HolderPdfUser extends RecyclerView.ViewHolder{
//        PDFView pdfView;
        ImageView imageThumb;
        ProgressBar progressBar;
        TextView txttitle,txtdes,txttheloai,txtsize,txtdate;

        public HolderPdfUser(@NonNull View itemView) {
            super(itemView);

//            pdfView =  binding.pdfView;
            imageThumb = binding.ImageThumb;
            progressBar = binding.progressBar;
            txttitle = binding.txttitle;
            txtdes = binding.txtdes;
            txttheloai = binding.txttheloai;
            txtsize = binding.txtsize;
            txtdate = binding.txtdate;
        }
    }
}
