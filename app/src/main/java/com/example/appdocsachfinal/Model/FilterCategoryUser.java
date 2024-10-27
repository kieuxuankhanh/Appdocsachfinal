package com.example.appdocsachfinal.Model;

import android.widget.Filter;

import com.example.appdocsachfinal.Adapter.AdapterCategory;
import com.example.appdocsachfinal.Adapter.AdapterCategoryUser;

import java.util.ArrayList;

public class FilterCategoryUser extends Filter {
    ArrayList<ModelCategory> filterList;
    AdapterCategoryUser adapterCategoryUser;

    public FilterCategoryUser(ArrayList<ModelCategory> filterList, AdapterCategoryUser adapterCategoryUser) {
        this.filterList = filterList;
        this.adapterCategoryUser = adapterCategoryUser;
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
        adapterCategoryUser.catagoryArrayList = (ArrayList<ModelCategory>)results.values;
        adapterCategoryUser.notifyDataSetChanged();

    }
}
