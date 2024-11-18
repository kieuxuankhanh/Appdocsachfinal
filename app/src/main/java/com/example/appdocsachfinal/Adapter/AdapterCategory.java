//package com.example.appdocsachfinal.Adapter;
//
//import android.app.AlertDialog;
//import android.content.Context;
//import android.content.DialogInterface;
//import android.content.Intent;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Filter;
//import android.widget.Filterable;
//import android.widget.ImageButton;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.example.appdocsachfinal.Filter.FilterCategory;
//import com.example.appdocsachfinal.Model.ModelCategory;
//import com.example.appdocsachfinal.Activities.PdfListAdminActivity;
//import com.example.appdocsachfinal.databinding.RowCategoryBinding;
//import com.google.android.gms.tasks.OnFailureListener;
//import com.google.android.gms.tasks.OnSuccessListener;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//
//import java.util.ArrayList;
//
//public class AdapterCategory extends RecyclerView.Adapter<AdapterCategory.HolderCategory> implements Filterable {
//
//    private Context context;
//    public ArrayList<ModelCategory> catagoryArrayList , filterList;
//    private RowCategoryBinding binding;
//
//    private FilterCategory filterCategory;
//
//    public AdapterCategory(Context context, ArrayList<ModelCategory> catagoryArrayList) {
//        this.context = context;
//        this.catagoryArrayList = catagoryArrayList;
//        this.filterList = catagoryArrayList;
//    }
//
//    @NonNull
//    @Override
//    public HolderCategory onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        binding = RowCategoryBinding.inflate(LayoutInflater.from(context),parent,false);
//
//        return new HolderCategory(binding.getRoot());
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull HolderCategory holder, int position) {
//        ModelCategory modelCatagory = catagoryArrayList.get(position);
//        String id = modelCatagory.getId();
//        String category = modelCatagory.getCategory();
//        String uid = modelCatagory.getUid();
//        long timestamp = modelCatagory.getTimestamp();
//
//        holder.edttentl.setText(category);
//
//        holder.btnxoatl.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                AlertDialog.Builder builder =  new AlertDialog.Builder(context);
//                builder.setTitle("Xóa")
//                        .setMessage("Bạn có chắc chắn muốn xóa")
//                        .setPositiveButton("Đồng ý", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                Toast.makeText(context, "Đang xóa", Toast.LENGTH_SHORT).show();
//                                deleteCategory(modelCatagory,holder);
//                            }
//                        })
//                        .setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                dialog.dismiss();
//                            }
//                        })
//                        .show();
//            }
//        });
//
//        holder.itemView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(context, PdfListAdminActivity.class);
//                intent.putExtra("categoryId",id);
//                intent.putExtra("categoryTitle",category);
//                context.startActivity(intent);
//            }
//        });
//    }
//
//    private void deleteCategory(ModelCategory modelCatagory, HolderCategory holder) {
//        String id = modelCatagory.getId();
//        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
//        ref.child(id)
//                .removeValue()
//                .addOnSuccessListener(new OnSuccessListener<Void>() {
//                    @Override
//                    public void onSuccess(Void unused) {
//                        Toast.makeText(context, "Xóa thành công", Toast.LENGTH_SHORT).show();
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
//                    }
//                });
//    }
//
//    @Override
//    public int getItemCount() {
//        return catagoryArrayList.size();
//    }
//
//    @Override
//    public Filter getFilter() {
//        if (filterCategory == null){
//            filterCategory = new FilterCategory(filterList,this);
//        }
//        return filterCategory;
//    }
//
//    class HolderCategory extends RecyclerView.ViewHolder{
//
//        TextView edttentl;
//        ImageButton btnxoatl;
//        public HolderCategory(@NonNull View itemView) {
//            super(itemView);
//
//            edttentl = binding.edttentl;
//            btnxoatl = binding.btnxoatl;
//        }
//    }
//}

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

import com.example.appdocsachfinal.Filter.FilterCategory;
import com.example.appdocsachfinal.Model.ModelCategory;
import com.example.appdocsachfinal.Activities.PdfListAdminActivity;
import com.example.appdocsachfinal.databinding.RowCategoryBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class AdapterCategory extends RecyclerView.Adapter<AdapterCategory.HolderCategory> implements Filterable {

    private Context context;
    public ArrayList<ModelCategory> catagoryArrayList, filterList;
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
        binding = RowCategoryBinding.inflate(LayoutInflater.from(context), parent, false);
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
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Xóa thể loại")
                        .setMessage("Tất cả sách trong thể loại này sẽ được chuyển sang thể loại 'Khác'. Bạn có chắc chắn muốn xóa?")
                        .setPositiveButton("Đồng ý", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteCategory(modelCatagory, holder);
                            }
                        })
                        .setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
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
                intent.putExtra("categoryId", id);
                intent.putExtra("categoryTitle", category);
                context.startActivity(intent);
            }
        });
    }

    private void deleteCategory(ModelCategory modelCategory, HolderCategory holder) {
        String categoryId = modelCategory.getId();
        String category = modelCategory.getCategory();

        // Không cho phép xóa thể loại "Khác"
        if (category.equals("Khác")) {
            Toast.makeText(context, "Không thể xóa thể loại 'Khác'", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference categoriesRef = FirebaseDatabase.getInstance().getReference("Categories");
        DatabaseReference booksRef = FirebaseDatabase.getInstance().getReference("Books");

        // Tìm categoryId của thể loại "Khác"
        categoriesRef.orderByChild("category").equalTo("Khác")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String otherId = "";
                        // Tìm id của category "Khác"
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            otherId = ds.child("id").getValue(String.class);
                            break;
                        }

                        // Nếu chưa có thể loại "Khác", tạo mới
                        if (otherId.isEmpty()) {
                            otherId = "" + System.currentTimeMillis();
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("id", otherId);
                            hashMap.put("category", "Khác");
                            hashMap.put("timestamp", System.currentTimeMillis());
                            hashMap.put("uid", modelCategory.getUid());

                            categoriesRef.child(otherId).setValue(hashMap);
                        }

                        // Lấy tất cả sách thuộc thể loại cũ
                        final String finalOtherId = otherId;
                        booksRef.orderByChild("categoryId").equalTo(categoryId)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        for (DataSnapshot ds : snapshot.getChildren()) {
                                            // Cập nhật categoryId của từng cuốn sách
                                            String bookId = ds.getKey();
                                            booksRef.child(bookId).child("categoryId").setValue(finalOtherId);
                                        }

                                        // Sau khi chuyển xong tất cả sách, xóa thể loại cũ
                                        categoriesRef.child(categoryId).removeValue()
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {
                                                        Toast.makeText(context,
                                                                "Đã xóa thể loại và chuyển sách sang thể loại 'Khác'",
                                                                Toast.LENGTH_SHORT).show();
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(context,
                                                                "" + e.getMessage(),
                                                                Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Toast.makeText(context,
                                                "" + error.getMessage(),
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(context,
                                "" + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public int getItemCount() {
        return catagoryArrayList.size();
    }

    @Override
    public Filter getFilter() {
        if (filterCategory == null) {
            filterCategory = new FilterCategory(filterList, this);
        }
        return filterCategory;
    }

    class HolderCategory extends RecyclerView.ViewHolder {
        TextView edttentl;
        ImageButton btnxoatl;

        public HolderCategory(@NonNull View itemView) {
            super(itemView);
            edttentl = binding.edttentl;
            btnxoatl = binding.btnxoatl;
        }
    }
}
