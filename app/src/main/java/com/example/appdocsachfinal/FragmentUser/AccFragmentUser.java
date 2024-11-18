package com.example.appdocsachfinal.FragmentUser;

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

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.appdocsachfinal.Activities.FavoriteBookActivity;
import com.example.appdocsachfinal.Activities.ForgotPassActivity;
import com.example.appdocsachfinal.Activities.LoginActivity;
import com.example.appdocsachfinal.Activities.Profile_Activity;
import com.example.appdocsachfinal.MyApplication;
import com.example.appdocsachfinal.R;
import com.example.appdocsachfinal.databinding.FragmentAccUserBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


public class AccFragmentUser extends Fragment {
    private FragmentAccUserBinding binding;
    private FirebaseAuth firebaseAuth;
    private static final String TAG = "PROFILE_TAG";
    public SharedPreferences sharedPreferences;
    public SharedPreferences.Editor editor;
    boolean nightMode;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAccUserBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();

        // Check if context is available
        if (getContext() == null) {
            return;
        }

        // Initialize SharedPreferences
        try {
            sharedPreferences = getContext().getSharedPreferences("MODE", Context.MODE_PRIVATE);
            nightMode = sharedPreferences.getBoolean("nightMode", false);
            if (nightMode) {
                binding.switchmode.setChecked(true);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error initializing SharedPreferences: " + e.getMessage());
        }

        initializeClickListeners();
        loadUserInfo();
        checkUser();
    }

    private void initializeClickListeners() {
        binding.switchmode.setOnClickListener(v -> {
            try {
                if (nightMode) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    editor = sharedPreferences.edit();
                    editor.putBoolean("nightMode", false);
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    editor = sharedPreferences.edit();
                    editor.putBoolean("nightMode", true);
                }
                editor.apply();
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

//    private void deleteAccount() {
//        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
//        builder.setTitle("Xóa tài khoản")
//                .setMessage("Bạn có chắc chắn muốn xóa tài khoản? Hành động này không thể hoàn tác.")
//                .setPositiveButton("Xóa", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        // Kiểm tra context có tồn tại không
//                        if (getActivity() == null) return;
//
//                        // Hiển thị dialog loading
//                        ProgressDialog progressDialog = new ProgressDialog(requireActivity());
//                        progressDialog.setMessage("Đang xóa tài khoản...");
//                        progressDialog.setCancelable(false); // Ngăn người dùng tắt dialog
//                        progressDialog.show();
//
//                        // Lấy user hiện tại
//                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//                        if (user == null) {
//                            progressDialog.dismiss();
//                            Toast.makeText(requireContext(), "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
//                            return;
//                        }
//
//                        String uid = user.getUid();
//
//                        // 1. Xóa thông tin user từ Realtime Database
//                        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);
//                        userRef.removeValue()
//                                .addOnSuccessListener(new OnSuccessListener<Void>() {
//                                    @Override
//                                    public void onSuccess(Void unused) {
//                                        // 2. Xóa ảnh đại diện từ Storage (nếu có)
//                                        if (user.getPhotoUrl() != null) {
//                                            StorageReference photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(user.getPhotoUrl().toString());
//                                            photoRef.delete()
//                                                    .addOnFailureListener(new OnFailureListener() {
//                                                        @Override
//                                                        public void onFailure(@NonNull Exception e) {
//                                                            Log.e("DELETE_ACCOUNT", "Không thể xóa ảnh đại diện: " + e.getMessage());
//                                                        }
//                                                    });
//                                        }
//
//                                        // 3. Xóa favorites của user (nếu có)
//                                        DatabaseReference favRef = FirebaseDatabase.getInstance().getReference("Users")
//                                                .child(uid).child("Favorites");
//                                        favRef.removeValue();
//
//                                        // 4. Xóa tài khoản Authentication
//                                        user.delete()
//                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
//                                                    @Override
//                                                    public void onSuccess(Void unused) {
//                                                        if (getActivity() == null) return;
//
//                                                        progressDialog.dismiss();
//                                                        Toast.makeText(requireContext(), "Tài khoản đã được xóa", Toast.LENGTH_SHORT).show();
//
//                                                        // Đăng xuất khỏi Firebase
//                                                        FirebaseAuth.getInstance().signOut();
//
//                                                        // Chuyển về màn hình đăng nhập
//                                                        Intent intent = new Intent(requireActivity(), LoginActivity.class);
//                                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                                                        startActivity(intent);
////                                                        requireActivity().finish();
//                                                    }
//                                                })
//                                                .addOnFailureListener(new OnFailureListener() {
//                                                    @Override
//                                                    public void onFailure(@NonNull Exception e) {
//                                                        if (getActivity() == null) return;
//
//                                                        progressDialog.dismiss();
//                                                        String errorMessage = "";
//                                                        if (e instanceof FirebaseAuthRecentLoginRequiredException) {
//                                                            errorMessage = "Vui lòng đăng nhập lại để xóa tài khoản";
//                                                            // Có thể thêm code để yêu cầu người dùng xác thực lại ở đây
//                                                            reAuthenticateUser();
//                                                        } else {
//                                                            errorMessage = "Lỗi: " + e.getMessage();
//                                                        }
//                                                        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
//                                                    }
//                                                });
//                                    }
//                                })
//                                .addOnFailureListener(new OnFailureListener() {
//                                    @Override
//                                    public void onFailure(@NonNull Exception e) {
//                                        if (getActivity() == null) return;
//
//                                        progressDialog.dismiss();
//                                        Toast.makeText(requireContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                                    }
//                                });
//                    }
//                })
//                .setNegativeButton("Hủy", null)
//                .show();
//    }
//
//    // Thêm phương thức để xác thực lại người dùng nếu cần
//    private void reAuthenticateUser() {
//        // Hiển thị dialog yêu cầu nhập lại mật khẩu
//        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
//        View view = getLayoutInflater().inflate(R.layout.dialog_reauthenticate, null);
//        EditText passwordEt = view.findViewById(R.id.passwordEt);
//
//        builder.setView(view)
//                .setTitle("Xác thực lại")
//                .setMessage("Vui lòng nhập lại mật khẩu của bạn")
//                .setPositiveButton("Xác nhận", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        String password = passwordEt.getText().toString().trim();
//                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//                        if (user != null && !TextUtils.isEmpty(password)) {
//                            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), password);
//                            user.reauthenticate(credential)
//                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
//                                        @Override
//                                        public void onSuccess(Void unused) {
//                                            // Sau khi xác thực lại thành công, thực hiện xóa tài khoản
//                                            deleteAccount();
//                                        }
//                                    })
//                                    .addOnFailureListener(new OnFailureListener() {
//                                        @Override
//                                        public void onFailure(@NonNull Exception e) {
//                                            Toast.makeText(requireContext(), "Xác thực thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                                        }
//                                    });
//                        }
//                    }
//                })
//                .setNegativeButton("Hủy", null)
//                .show();
//    }

    private void deleteAccount() {
        // Hiển thị dialog yêu cầu nhập lại mật khẩu trước
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        View view = getLayoutInflater().inflate(R.layout.dialog_reauthenticate, null);
        EditText passwordEt = view.findViewById(R.id.edtpass);

        builder.setView(view)
                .setTitle("Xác thực")
                .setMessage("Vui lòng nhập mật khẩu để xác nhận xóa tài khoản")
                .setPositiveButton("Xác nhận", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String password = passwordEt.getText().toString().trim();
                        if (TextUtils.isEmpty(password)) {
                            Toast.makeText(requireContext(), "Vui lòng nhập mật khẩu", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if (user != null) {
                            // Xác thực lại với mật khẩu vừa nhập
                            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), password);
                            user.reauthenticate(credential)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            // Sau khi xác thực thành công, hiển thị dialog xác nhận xóa
                                            showDeleteConfirmationDialog();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(requireContext(), "Mật khẩu không đúng", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setTitle("Xóa tài khoản")
                .setMessage("Bạn có chắc chắn muốn xóa tài khoản? Hành động này không thể hoàn tác.")
                .setPositiveButton("Xóa", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        proceedWithAccountDeletion();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void proceedWithAccountDeletion() {
        // Kiểm tra context có tồn tại không
        if (getActivity() == null) return;

        // Hiển thị dialog loading
        ProgressDialog progressDialog = new ProgressDialog(requireActivity());
        progressDialog.setMessage("Đang xóa tài khoản...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Lấy user hiện tại
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            progressDialog.dismiss();
            Toast.makeText(requireContext(), "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = user.getUid();

        // 1. Xóa thông tin user từ Realtime Database
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);
        userRef.removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        // 2. Xóa ảnh đại diện từ Storage (nếu có)
                        if (user.getPhotoUrl() != null) {
                            StorageReference photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(user.getPhotoUrl().toString());
                            photoRef.delete()
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.e("DELETE_ACCOUNT", "Không thể xóa ảnh đại diện: " + e.getMessage());
                                        }
                                    });
                        }

                        // 3. Xóa favorites của user (nếu có)
                        DatabaseReference favRef = FirebaseDatabase.getInstance().getReference("Users")
                                .child(uid).child("Favorites");
                        favRef.removeValue();

                        // 4. Xóa tài khoản Authentication
                        user.delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        if (getActivity() == null) return;

                                        progressDialog.dismiss();
                                        Toast.makeText(requireContext(), "Tài khoản đã được xóa", Toast.LENGTH_SHORT).show();

                                        // Đăng xuất khỏi Firebase
                                        FirebaseAuth.getInstance().signOut();

                                        // Chuyển về màn hình đăng nhập
                                        Intent intent = new Intent(requireActivity(), LoginActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        if (getActivity() == null) return;

                                        progressDialog.dismiss();
                                        Toast.makeText(requireContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (getActivity() == null) return;

                        progressDialog.dismiss();
                        Toast.makeText(requireContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
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
                                    // Get user data directly from snapshot
                                    String email = snapshot.child("email").exists() ?
                                            snapshot.child("email").getValue().toString() : "";
                                    String name = snapshot.child("name").exists() ?
                                            snapshot.child("name").getValue().toString() : "";
                                    String profileImage = snapshot.child("profileImage").exists() ?
                                            snapshot.child("profileImage").getValue().toString() : "";
                                    String timestamp = snapshot.child("timestamp").exists() ?
                                            snapshot.child("timestamp").getValue().toString() : "";

                                    // Update UI with user data
                                    if (!email.isEmpty()) {
                                        binding.txtEmail.setText(email);
                                    }

                                    if (!name.isEmpty()) {
                                        binding.txtName.setText(name);
                                        binding.txtName.setVisibility(View.VISIBLE);
                                    } else {
                                        binding.txtName.setVisibility(View.GONE);
                                    }

                                    // Format and set timestamp
                                    if (!timestamp.isEmpty()) {
                                        try {
                                            long timestampLong = Long.parseLong(timestamp);
                                            String formattedDate = MyApplication.formatTimestamp(timestampLong);
                                            binding.txtDate.setText(formattedDate);
                                        } catch (NumberFormatException e) {
                                            Log.e(TAG, "Error parsing timestamp: " + e.getMessage());
                                        }
                                    }

                                    // Load profile image
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
                                } else {
                                    Log.e(TAG, "User data does not exist in database");
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