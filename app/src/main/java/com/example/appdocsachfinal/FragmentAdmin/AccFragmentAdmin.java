//package com.example.appdocsachfinal.FragmentAdmin;
//
//import android.content.Context;
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.net.Uri;
//import android.os.Bundle;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.appcompat.app.AppCompatDelegate;
//import androidx.appcompat.widget.AppCompatButton;
//import androidx.fragment.app.Fragment;
//
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//
//import com.bumptech.glide.Glide;
//import com.example.appdocsachfinal.Activities.FavoriteBookActivity;
//import com.example.appdocsachfinal.Activities.LoginActivity;
//import com.example.appdocsachfinal.Activities.Profile_Activity;
//import com.example.appdocsachfinal.MyApplication;
//import com.example.appdocsachfinal.R;
//import com.example.appdocsachfinal.databinding.FragmentAccAdminBinding;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseUser;
//import com.google.firebase.database.DataSnapshot;
//import com.google.firebase.database.DatabaseError;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.database.ValueEventListener;
//
//
//public class AccFragmentAdmin extends Fragment {
//    private FragmentAccAdminBinding binding;
//    private FirebaseAuth firebaseAuth;
//    private static final String TAG = "PROFILE_TAG";
//    public SharedPreferences sharedPreferences;
//    public SharedPreferences.Editor editor;
//    boolean nightMode;
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        binding = FragmentAccAdminBinding.inflate(getLayoutInflater());
//        return binding.getRoot();
//    }
//
//    @Override
//    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//
//        // Initialize Firebase Auth
//        firebaseAuth = FirebaseAuth.getInstance();
//
//        // Check if context is available
//        if (getContext() == null) {
//            return;
//        }
//
//        // Initialize SharedPreferences
//        try {
//            sharedPreferences = getContext().getSharedPreferences("MODE", Context.MODE_PRIVATE);
//            nightMode = sharedPreferences.getBoolean("nightMode", false);
//
//            // Thiết lập trạng thái ban đầu cho switch
//            binding.switchmode.setChecked(nightMode);
//
//            // Thiết lập theme ban đầu
//            if (nightMode) {
//                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
//            } else {
//                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
//            }
//        } catch (Exception e) {
//            Log.e(TAG, "Error initializing SharedPreferences: " + e.getMessage());
//        }
//
//        initializeClickListeners();
//        loadUserInfo();
//        checkUser();
//    }
//
//    private void initializeClickListeners() {
//        binding.switchmode.setOnCheckedChangeListener((buttonView, isChecked) -> {
//            try {
//                editor = sharedPreferences.edit();
//                if (isChecked) {
//                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
//                    editor.putBoolean("nightMode", true);
//                    nightMode = true;
//                } else {
//                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
//                    editor.putBoolean("nightMode", false);
//                    nightMode = false;
//                }
//                editor.commit(); // Sử dụng commit() thay vì apply() để đảm bảo thay đổi ngay lập tức
//
//                // Tạo lại activity để áp dụng theme mới
//                if (getActivity() != null) {
//                    getActivity().recreate();
//                }
//            } catch (Exception e) {
//                Log.e(TAG, "Error handling night mode: " + e.getMessage());
//            }
//        });
//
//        binding.btnout.setOnClickListener(v -> {
//            try {
//                FirebaseAuth.getInstance().signOut();
//                if (getActivity() != null) {
//                    Intent intent = new Intent(requireActivity(), LoginActivity.class);
//                    startActivity(intent);
//                    requireActivity().finish();
//                }
//            } catch (Exception e) {
//                Log.e(TAG, "Error signing out: " + e.getMessage());
//            }
//        });
//
//        binding.userProfile.setOnClickListener(v -> {
//            try {
//                if (getActivity() != null) {
//                    Intent intent = new Intent(requireActivity(), Profile_Activity.class);
//                    startActivity(intent);
//                }
//            } catch (Exception e) {
//                Log.e(TAG, "Error opening profile: " + e.getMessage());
//            }
//        });
//
//        binding.btnlikebook.setOnClickListener(v -> {
//            try {
//                if (getActivity() != null) {
//                    Intent intent = new Intent(requireActivity(), FavoriteBookActivity.class);
//                    startActivity(intent);
//                }
//            } catch (Exception e) {
//                Log.e(TAG, "Error opening favorite books: " + e.getMessage());
//            }
//        });
//    }
//    private void loadUserInfo() {
//        if (firebaseAuth.getUid() == null) {
//            Log.e(TAG, "User ID is null");
//            return;
//        }
//
//        try {
//            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
//            reference.child(firebaseAuth.getUid())
//                    .addValueEventListener(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(@NonNull DataSnapshot snapshot) {
//                            try {
//                                if (snapshot.exists()) {
//                                    // Get user data directly from snapshot
//                                    String email = snapshot.child("email").exists() ?
//                                            snapshot.child("email").getValue().toString() : "";
//                                    String name = snapshot.child("name").exists() ?
//                                            snapshot.child("name").getValue().toString() : "";
//                                    String profileImage = snapshot.child("profileImage").exists() ?
//                                            snapshot.child("profileImage").getValue().toString() : "";
//                                    String timestamp = snapshot.child("timestamp").exists() ?
//                                            snapshot.child("timestamp").getValue().toString() : "";
//
//                                    // Update UI with user data
//                                    if (!email.isEmpty()) {
//                                        binding.txtEmail.setText(email);
//                                    }
//
//                                    if (!name.isEmpty()) {
//                                        binding.txtName.setText(name);
//                                        binding.txtName.setVisibility(View.VISIBLE);
//                                    } else {
//                                        binding.txtName.setVisibility(View.GONE);
//                                    }
//
//                                    // Format and set timestamp
//                                    if (!timestamp.isEmpty()) {
//                                        try {
//                                            long timestampLong = Long.parseLong(timestamp);
//                                            String formattedDate = MyApplication.formatTimestamp(timestampLong);
//                                            binding.txtDate.setText(formattedDate);
//                                        } catch (NumberFormatException e) {
//                                            Log.e(TAG, "Error parsing timestamp: " + e.getMessage());
//                                        }
//                                    }
//
//                                    // Load profile image
//                                    if (getContext() != null) {
//                                        if (!profileImage.isEmpty()) {
//                                            Glide.with(requireContext())
//                                                    .load(profileImage)
//                                                    .placeholder(R.drawable.avatar)
//                                                    .error(R.drawable.avatar)
//                                                    .into(binding.imgAvatar);
//                                        } else {
//                                            Glide.with(requireContext())
//                                                    .load(R.drawable.avatar)
//                                                    .into(binding.imgAvatar);
//                                        }
//                                    }
//                                } else {
//                                    Log.e(TAG, "User data does not exist in database");
//                                }
//                            } catch (Exception e) {
//                                Log.e(TAG, "Error processing user data: " + e.getMessage());
//                            }
//                        }
//
//                        @Override
//                        public void onCancelled(@NonNull DatabaseError error) {
//                            Log.e(TAG, "Database error: " + error.getMessage());
//                        }
//                    });
//        } catch (Exception e) {
//            Log.e(TAG, "Error loading user info: " + e.getMessage());
//        }
//    }
//    private void checkUser() {
//        try {
//            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//            if (user == null) {
//                return;
//            }
//
//            String name = user.getDisplayName();
//            String email = user.getEmail();
//            Uri photoUrl = user.getPhotoUrl();
//
//            if (name == null || name.isEmpty()) {
//                binding.txtName.setVisibility(View.GONE);
//            } else {
//                binding.txtName.setVisibility(View.VISIBLE);
//                binding.txtName.setText(name);
//            }
//
//            if (email != null) {
//                binding.txtEmail.setText(email);
//            }
//
//            if (getContext() != null) {
//                Glide.with(requireContext())
//                        .load(photoUrl)
//                        .error(R.drawable.avatar)
//                        .into(binding.imgAvatar);
//            }
//        } catch (Exception e) {
//            Log.e(TAG, "Error checking user: " + e.getMessage());
//        }
//    }
//
//    @Override
//    public void onDestroyView() {
//        super.onDestroyView();
//        binding = null;
//    }
//}

package com.example.appdocsachfinal.FragmentAdmin;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.appdocsachfinal.Activities.FavoriteBookActivity;
import com.example.appdocsachfinal.Activities.ForgotPassActivity;
import com.example.appdocsachfinal.Activities.LoginActivity;
import com.example.appdocsachfinal.Activities.Profile_Activity;
import com.example.appdocsachfinal.MyApplication;
import com.example.appdocsachfinal.R;
import com.example.appdocsachfinal.databinding.FragmentAccAdminBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class AccFragmentAdmin extends Fragment {
    private FragmentAccAdminBinding binding;
    private FirebaseAuth firebaseAuth;
    private static final String TAG = "PROFILE_TAG";
    public static final String PREFS_NAME = "AppSettings";
    public static final String KEY_NIGHT_MODE = "NightMode";
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private boolean nightMode;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAccAdminBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();

        if (getContext() == null) {
            return;
        }

        try {
            // Khởi tạo SharedPreferences với tên riêng cho app
            sharedPreferences = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            editor = sharedPreferences.edit();

            // Đọc trạng thái night mode đã lưu
            nightMode = sharedPreferences.getBoolean(KEY_NIGHT_MODE, false);

            // Cập nhật switch và theme mà không recreate activity
            binding.switchmode.setChecked(nightMode);
            updateTheme(nightMode);

            initializeClickListeners();
            loadUserInfo();
            checkUser();
        } catch (Exception e) {
            Log.e(TAG, "Error in onViewCreated: " + e.getMessage());
        }
    }

    private void updateTheme(boolean isNight) {
        if (getActivity() != null) {
            try {
                AppCompatDelegate.setDefaultNightMode(
                        isNight ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
                );
            } catch (Exception e) {
                Log.e(TAG, "Error updating theme: " + e.getMessage());
            }
        }
    }

    private void initializeClickListeners() {
        binding.switchmode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            try {
                // Lưu trạng thái mới
                editor.putBoolean(KEY_NIGHT_MODE, isChecked);
                editor.apply();
                nightMode = isChecked;

                // Cập nhật theme
                updateTheme(isChecked);

                // Reload fragment
                if (getActivity() != null && getFragmentManager() != null) {
                    Fragment currentFragment = getFragmentManager().findFragmentById(R.id.framelayout);
                    if (currentFragment != null) {
                        getFragmentManager()
                                .beginTransaction()
                                .detach(currentFragment)
                                .attach(currentFragment)
                                .commit();
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error handling night mode: " + e.getMessage());
            }
        });

        binding.btnout.setOnClickListener(v -> {
            try {
                FirebaseAuth.getInstance().signOut();
                if (getActivity() != null) {
                    Intent intent = new Intent(requireActivity(), LoginActivity.class);
                    startActivity(intent);
                    requireActivity().finish();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error signing out: " + e.getMessage());
            }
        });

        binding.userProfile.setOnClickListener(v -> {
            try {
                if (getActivity() != null) {
                    Intent intent = new Intent(requireActivity(), Profile_Activity.class);
                    startActivity(intent);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error opening profile: " + e.getMessage());
            }
        });

        binding.btnlikebook.setOnClickListener(v -> {
            try {
                if (getActivity() != null) {
                    Intent intent = new Intent(requireActivity(), FavoriteBookActivity.class);
                    startActivity(intent);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error opening favorite books: " + e.getMessage());
            }
        });
        binding.btnchangepass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(requireActivity(), ForgotPassActivity.class);
                startActivity(intent);
            }
        });
        binding.btndeltk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteAccount();
            }
        });
    }

    private void deleteAccount() {
        Toast.makeText(requireContext(), "Đéo ai cho xóa mà xóa, Admin xóa ăn lồn à!!!", Toast.LENGTH_SHORT).show();
    }


    private void loadUserInfo() {
        if (firebaseAuth.getUid() == null) {
            Log.e(TAG, "User ID is null");
            return;
        }

        try {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
            reference.child(firebaseAuth.getUid())
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            try {
                                if (snapshot.exists()) {
                                    String email = snapshot.child("email").exists() ?
                                            snapshot.child("email").getValue().toString() : "";
                                    String name = snapshot.child("name").exists() ?
                                            snapshot.child("name").getValue().toString() : "";
                                    String profileImage = snapshot.child("profileImage").exists() ?
                                            snapshot.child("profileImage").getValue().toString() : "";
                                    String timestamp = snapshot.child("timestamp").exists() ?
                                            snapshot.child("timestamp").getValue().toString() : "";

                                    if (!email.isEmpty()) {
                                        binding.txtEmail.setText(email);
                                    }

                                    if (!name.isEmpty()) {
                                        binding.txtName.setText(name);
                                        binding.txtName.setVisibility(View.VISIBLE);
                                    } else {
                                        binding.txtName.setVisibility(View.GONE);
                                    }

                                    if (!timestamp.isEmpty()) {
                                        try {
                                            long timestampLong = Long.parseLong(timestamp);
                                            String formattedDate = MyApplication.formatTimestamp(timestampLong);
                                            binding.txtDate.setText(formattedDate);
                                        } catch (NumberFormatException e) {
                                            Log.e(TAG, "Error parsing timestamp: " + e.getMessage());
                                        }
                                    }

                                    if (getContext() != null) {
                                        if (!profileImage.isEmpty()) {
                                            Glide.with(requireContext())
                                                    .load(profileImage)
                                                    .placeholder(R.drawable.avatar)
                                                    .error(R.drawable.avatar)
                                                    .into(binding.imgAvatar);
                                        } else {
                                            Glide.with(requireContext())
                                                    .load(R.drawable.avatar)
                                                    .into(binding.imgAvatar);
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error processing user data: " + e.getMessage());
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e(TAG, "Database error: " + error.getMessage());
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error loading user info: " + e.getMessage());
        }
    }

    private void checkUser() {
        try {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) {
                return;
            }

            String name = user.getDisplayName();
            String email = user.getEmail();
            Uri photoUrl = user.getPhotoUrl();

            if (name == null || name.isEmpty()) {
                binding.txtName.setVisibility(View.GONE);
            } else {
                binding.txtName.setVisibility(View.VISIBLE);
                binding.txtName.setText(name);
            }

            if (email != null) {
                binding.txtEmail.setText(email);
            }

            if (getContext() != null) {
                Glide.with(requireContext())
                        .load(photoUrl)
                        .error(R.drawable.avatar)
                        .into(binding.imgAvatar);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking user: " + e.getMessage());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}