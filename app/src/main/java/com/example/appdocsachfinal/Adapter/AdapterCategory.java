package com.example.appdocsachfinal.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appdocsachfinal.Model.FilterCategory;
import com.example.appdocsachfinal.Model.ModelCategory;
import com.example.appdocsachfinal.Activities.PdfListAdminActivity;
import com.example.appdocsachfinal.databinding.RowCategoryBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class AdapterCategory extends RecyclerView.Adapter<AdapterCategory.HolderCategory> implements Filterable {

    private Context context;
    public ArrayList<ModelCategory> catagoryArrayList , filterList;
    private RowCategoryBinding binding;

    private FilterCategory filterCategory;

    public AdapterCategory(Context context, ArrayList<ModelCategory> catagoryArrayList) {
        this.context = context;
        this.catagoryArrayList = catagoryArrayList;
        this.filterList = catagoryArrayList;
    }

    @NonNull
    @Override
    public HolderCategory onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = RowCategoryBinding.inflate(LayoutInflater.from(context),parent,false);

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

        holder.btnxoatl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder =  new AlertDialog.Builder(context);
                builder.setTitle("Xoa")
                        .setMessage("Ban co chac chan muon xoa")
                        .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(context, "Dang Xoa", Toast.LENGTH_SHORT).show();
                                deleteCategory(modelCatagory,holder);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PdfListAdminActivity.class);
                intent.putExtra("categoryId",id);
                intent.putExtra("categoryTitle",category);
                context.startActivity(intent);
            }
        });
    }

    private void deleteCategory(ModelCategory modelCatagory, HolderCategory holder) {
        String id = modelCatagory.getId();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.child(id)
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(context, "Xoa thanh cong", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public int getItemCount() {
        return catagoryArrayList.size();
    }

    @Override
    public Filter getFilter() {
        if (filterCategory == null){
            filterCategory = new FilterCategory(filterList,this);
        }
        return filterCategory;
    }

    class HolderCategory extends RecyclerView.ViewHolder{

        TextView edttentl;
        ImageButton btnxoatl;
        public HolderCategory(@NonNull View itemView) {
            super(itemView);

            edttentl = binding.edttentl;
            btnxoatl = binding.btnxoatl;
        }
    }
}
