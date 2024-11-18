package com.example.appdocsachfinal.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appdocsachfinal.Activities.DashBoardUserActivity;
import com.example.appdocsachfinal.Activities.PdfListUserActivity;
import com.example.appdocsachfinal.Model.ModelCategory;
import com.example.appdocsachfinal.Model.ModelListPdf;
import com.example.appdocsachfinal.R;
import com.example.appdocsachfinal.databinding.ItemListHomeBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AdapterCategoryHome extends RecyclerView.Adapter<AdapterCategoryHome.ViewHolder> {

    private Context context;
    private ArrayList<ModelCategory> categoryArrayList;

    public AdapterCategoryHome(Context context, ArrayList<ModelCategory> categoryArrayList) {
        this.context = context;
        this.categoryArrayList = categoryArrayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_list_home, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ModelCategory modelCategory = categoryArrayList.get(position);
        String id = modelCategory.getId();
        String category = modelCategory.getCategory();
        String uid = modelCategory.getUid();
        long timestamp = modelCategory.getTimestamp();


        holder.binding.txttheloai.setText(modelCategory.getCategory());

        holder.binding.txtmore.setOnClickListener(v -> {
            Intent intent = new Intent(context, PdfListUserActivity.class);
            intent.putExtra("categoryId",id);
            intent.putExtra("categoryTitle",category);
            context.startActivity(intent);
        });

        loadBooksInCategory(modelCategory.getId(), holder.binding.theloaiRCV);
    }

    private void loadBooksInCategory(String categoryId, RecyclerView recyclerView) {
        DatabaseReference booksRef = FirebaseDatabase.getInstance().getReference("Books");
        booksRef.orderByChild("categoryId").equalTo(categoryId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ArrayList<ModelListPdf> booksList = new ArrayList<>();

                        for (DataSnapshot ds : snapshot.getChildren()) {
                            ModelListPdf book = ds.getValue(ModelListPdf.class);
                            if (book != null) {
                                booksList.add(book);
                            }
                        }
                        AdapterPdfListUser bookAdapter = new AdapterPdfListUser(context, booksList);
                        recyclerView.setLayoutManager(new GridLayoutManager(context, 3));
                        recyclerView.setAdapter(bookAdapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    @Override
    public int getItemCount() {
        return categoryArrayList.size();
    }

    // Thêm phương thức updateList
    public void updateList(ArrayList<ModelCategory> newList) {
        categoryArrayList.clear();
        categoryArrayList.addAll(newList);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ItemListHomeBinding binding;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemListHomeBinding.bind(itemView);
        }
    }
}