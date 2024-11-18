package com.example.appdocsachfinal.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appdocsachfinal.Activities.PdfListUserActivity;
import com.example.appdocsachfinal.Filter.FilterCategoryUser;
import com.example.appdocsachfinal.Model.ModelCategory;
import com.example.appdocsachfinal.databinding.RowCategoryUserBinding;

import java.util.ArrayList;

public class AdapterCategoryUser extends RecyclerView.Adapter<AdapterCategoryUser.HolderCategory> implements Filterable {

    private Context context;
    public ArrayList<ModelCategory> catagoryArrayList , filterList;
    private RowCategoryUserBinding binding;

    private FilterCategoryUser filterCategoryUser;

    public AdapterCategoryUser(Context context, ArrayList<ModelCategory> catagoryArrayList) {
        this.context = context;
        this.catagoryArrayList = catagoryArrayList;
        this.filterList = catagoryArrayList;
    }

    @NonNull
    @Override
    public HolderCategory onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = RowCategoryUserBinding.inflate(LayoutInflater.from(context),parent,false);

        return new HolderCategory(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderCategory holder, int position) {
        ModelCategory modelCatagory = catagoryArrayList.get(position);
        String id = modelCatagory.getId();
        String category = modelCatagory.getCategory();
        String uid = modelCatagory.getUid();
        long timestamp = modelCatagory.getTimestamp();

        holder.edttentl.setText(category);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PdfListUserActivity.class);
                intent.putExtra("categoryId",id);
                intent.putExtra("categoryTitle",category);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return catagoryArrayList.size();
    }

    @Override
    public Filter getFilter() {
        if (filterCategoryUser == null){
            filterCategoryUser = new FilterCategoryUser(filterList,this);
        }
        return filterCategoryUser;
    }

    class HolderCategory extends RecyclerView.ViewHolder{

        TextView edttentl;
        public HolderCategory(@NonNull View itemView) {
            super(itemView);
            edttentl = binding.edttentl;
        }
    }
}
