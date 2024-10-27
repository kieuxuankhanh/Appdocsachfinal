package com.example.appdocsachfinal.Model;

import android.widget.Filter;

import com.example.appdocsachfinal.Adapter.AdapterCategory;

import java.util.ArrayList;

public class FilterCategory extends Filter {
    ArrayList<ModelCategory> filterList;
    AdapterCategory adapterCategory;

    public FilterCategory(ArrayList<ModelCategory> filterList, AdapterCategory adapterCategory) {
        this.filterList = filterList;
        this.adapterCategory = adapterCategory;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();
        if (constraint != null && constraint.length() > 0){
            constraint = constraint.toString().toUpperCase();
            ArrayList<ModelCategory> filterdModels = new ArrayList<>();

            for (int i=0;i<filterList.size();i++){
                if (filterList.get(i).getCategory().toUpperCase().contains(constraint)){
                    filterdModels.add(filterList.get(i));
                }
            }
            results.count = filterdModels.size();
            results.values = filterdModels;
        }
        else {
            results.count = filterList.size();
            results.values = filterList;
        }
        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        adapterCategory.catagoryArrayList = (ArrayList<ModelCategory>)results.values;
        adapterCategory.notifyDataSetChanged();

    }
}
