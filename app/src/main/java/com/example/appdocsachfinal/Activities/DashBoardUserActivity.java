package com.example.appdocsachfinal.Activities;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.example.appdocsachfinal.Adapter.AdapterPdfUser;
import com.example.appdocsachfinal.FragmentUser.PdfUserFragment;
import com.example.appdocsachfinal.Model.ModelCategory;
import com.example.appdocsachfinal.databinding.ActivityDashBoardUserBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class DashBoardUserActivity extends AppCompatActivity {
    public ArrayList<ModelCategory> categoryArrayList;
    public ViewPagerAdapter viewPagerAdapter;
    private ActivityDashBoardUserBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashBoardUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        String category = getIntent().getStringExtra("category");

        setupViewPagerAdapter(binding.viewPager);
        binding.tabLayout.setupWithViewPager(binding.viewPager);

        PdfUserFragment pdfUserFragment = PdfUserFragment.newInstance(null, category, null);

    }
    private void setupViewPagerAdapter(ViewPager viewPager){
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT,this);
        categoryArrayList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryArrayList.clear();
                ModelCategory modelAll = new ModelCategory("01","Tất cả","",1);
                ModelCategory modelMostViewed = new ModelCategory("02","Đọc nhiều nhất","",1);
                ModelCategory modelMostDownloaded = new ModelCategory("03", "Tải nhiều nhất","",1);

                categoryArrayList.add(modelAll);
                categoryArrayList.add(modelMostViewed);
                categoryArrayList.add(modelMostDownloaded);
                viewPagerAdapter.addFragment(PdfUserFragment.newInstance(
                        ""+modelAll.getId(),
                        ""+modelAll.getCategory(),
                        ""+modelAll.getUid()
                ),modelAll.getCategory());
                viewPagerAdapter.addFragment(PdfUserFragment.newInstance(
                        ""+modelMostViewed.getId(),
                        ""+modelMostViewed.getCategory(),
                        ""+modelMostViewed.getUid()
                ),modelMostViewed.getCategory());
                viewPagerAdapter.addFragment(PdfUserFragment.newInstance(
                        ""+modelMostDownloaded.getId(),
                        ""+modelMostDownloaded.getCategory(),
                        ""+modelMostDownloaded.getUid()
                ),modelMostDownloaded.getCategory());
                viewPagerAdapter.notifyDataSetChanged();
                for (DataSnapshot ds: snapshot.getChildren()){
                    ModelCategory modelCategory = ds.getValue(ModelCategory.class);
                    categoryArrayList.add(modelCategory);
                    viewPagerAdapter.addFragment(PdfUserFragment.newInstance(
                            ""+modelCategory.getId(),
                            ""+modelCategory.getCategory(),
                            ""+modelCategory.getUid()),modelCategory.getCategory());
                    viewPagerAdapter.notifyDataSetChanged();
                }
                binding.btnback.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onBackPressed();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        viewPager.setAdapter(viewPagerAdapter);
    }
    public class ViewPagerAdapter extends FragmentPagerAdapter{
        private ArrayList<PdfUserFragment> fragmentList = new ArrayList<>();
        private ArrayList<String> fragmentTitleList = new ArrayList<>();
        private Context context;

        public ViewPagerAdapter(@NonNull FragmentManager fm, int behavior,Context context) {
            super(fm, behavior);
            this.context = context;
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }
        private void addFragment(PdfUserFragment fragment, String title){
            fragmentList.add(fragment);
            fragmentTitleList.add(title);
        }
        //Note
        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return fragmentTitleList.get(position);
        }
    }
}