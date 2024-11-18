package com.example.appdocsachfinal.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.appdocsachfinal.FragmentAdmin.AccFragmentAdmin;
import com.example.appdocsachfinal.FragmentAdmin.BookFragmentAdmin;
import com.example.appdocsachfinal.FragmentAdmin.HomeFragmentAdmin;
import com.example.appdocsachfinal.FragmentAdmin.NotiFragmentAdmin;
import com.example.appdocsachfinal.FragmentUser.HomeFragmentUser;
import com.example.appdocsachfinal.R;
import com.example.appdocsachfinal.databinding.ActivityMainAdminBinding;
import com.google.firebase.auth.FirebaseAuth;

public class MainAdminActivity extends AppCompatActivity {
    private ActivityMainAdminBinding binding;
    private FirebaseAuth firebaseAuth;
    private Fragment currentFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initializeFirebase();
        setupBottomNavigation();
        setupFabButton();

        if (savedInstanceState == null) {
            loadFragment(new HomeFragmentAdmin(), true);
        }
    }

    private void initializeFirebase() {
        firebaseAuth = FirebaseAuth.getInstance();
    }

    private void setupFabButton() {
        binding.btnfab.setOnClickListener(v -> {
            Intent intent = new Intent(MainAdminActivity.this, AddBookActivity.class);
            startActivity(intent);
        });
    }

    private void setupBottomNavigation() {
        binding.BottomNvi.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Fragment fragment = null;

            if (itemId == R.id.btnhome) {
                fragment = new HomeFragmentAdmin();
            } else if (itemId == R.id.btnbook) {
                fragment = new BookFragmentAdmin();
            } else if (itemId == R.id.btnnoti) {
                fragment = new NotiFragmentAdmin();
            } else if (itemId == R.id.btnperson) {
                fragment = new AccFragmentAdmin();
            } else if (itemId == R.id.btnbimat) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://quatvn.fit/"));
                startActivity(browserIntent);
            }

            if (fragment != null) {
                loadFragment(fragment, false);
                return true;
            }
            return false;
        });
    }

    private void loadFragment(Fragment fragment, boolean isAppInitialized) {
        try {
            if (fragment != null) {
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();

                transaction.setCustomAnimations(
                        R.anim.fade_in,
                        R.anim.fade_out,
                        R.anim.fade_in,
                        R.anim.fade_out
                );

                if (isAppInitialized) {
                    transaction.add(R.id.framelayout, fragment);
                } else {
                    transaction.replace(R.id.framelayout, fragment);
                    // Add to backstack only for non-home fragments
                    if (!(fragment instanceof HomeFragmentUser)) {
                        transaction.addToBackStack(null);
                    }
                }

                transaction.commit();
                currentFragment = fragment;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}