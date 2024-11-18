package com.example.appdocsachfinal.Filter;

import android.widget.Filter;

import com.example.appdocsachfinal.Adapter.AdapterFavoriteBook;
import com.example.appdocsachfinal.Model.ModelPdf;

import java.util.ArrayList;

public class FilterFavorite extends Filter {
    ArrayList<ModelPdf> filterList;
    AdapterFavoriteBook adapterFavoriteBook;

    public FilterFavorite(ArrayList<ModelPdf> filterList, AdapterFavoriteBook adapterFavoriteBook) {
        this.filterList = filterList;
        this.adapterFavoriteBook = adapterFavoriteBook;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();
        if (constraint!=null || constraint.length() > 0){
            constraint = constraint.toString().toUpperCase();
            ArrayList<ModelPdf> filterModels = new ArrayList<>();
            for (int i=0;i< filterList.size();i++){
                if (filterList.get(i).getTitle().toUpperCase().contains(constraint)){
                    filterModels.add(filterList.get(i));
                }
            }
            results.count = filterModels.size();
            results.values = filterModels;
        }
        else {
            results.count = filterList.size();
            results.values = filterList;
        }
        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        adapterFavoriteBook.pdfArrayList = (ArrayList<ModelPdf>)results.values;
        adapterFavoriteBook.notifyDataSetChanged();
    }
}
