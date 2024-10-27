package com.example.appdocsachfinal.FragmentUser;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.example.appdocsachfinal.Activities.FavoriteBookActivity;
import com.example.appdocsachfinal.Activities.LoginActivity;
import com.example.appdocsachfinal.Activities.Profile_Activity;
import com.example.appdocsachfinal.MyApplication;
import com.example.appdocsachfinal.R;
import com.example.appdocsachfinal.databinding.FragmentAccUserBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AccFragmentUser extends Fragment {
    private FragmentAccUserBinding binding;
    private FirebaseAuth firebaseAuth;
    private static final String TAG = "PROFILE_TAG";
    public AccFragmentUser() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding =FragmentAccUserBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        firebaseAuth = FirebaseAuth.getInstance();

        binding.btnout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent= new Intent(requireActivity(), LoginActivity.class);
                startActivity(intent);
                requireActivity().finish();
            }
        });
        binding.userProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(requireActivity(), Profile_Activity.class);
                startActivity(intent);
            }
        });
        binding.btnlikebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentfavor = new Intent(requireActivity(), FavoriteBookActivity.class);
                startActivity(intentfavor);
            }
        });
        loadUserInfo();
        checkUser();
    }
    private void loadUserInfo() {
        Log.d(TAG, "LoadUserInfo: dang tai..."+firebaseAuth.getUid());
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String email = ""+snapshot.child("email").getValue();
                        String name = ""+snapshot.child("name").getValue();
                        String profileImage = ""+snapshot.child("profileImage").getValue();
                        String timestamp = ""+snapshot.child("timestamp").getValue();
                        String uid = ""+snapshot.child("uid").getValue();
                        String userType = ""+snapshot.child("userType").getValue();

                        String formattedDate = MyApplication.formatTimestamp(Long.parseLong(timestamp));

                        binding.txtEmail.setText(email);
                        binding.txtName.setText(name);
                        binding.txtDate.setText(formattedDate);

                        Glide.with(AccFragmentUser.this)
                                .load(profileImage)
                                .placeholder(R.drawable.avatar)
                                .into(binding.imgAvatar);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void checkUser() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user==null){
            return;
        }

        String name = user.getDisplayName();
        String email = user.getEmail();
        Uri photoUrl = user.getPhotoUrl();

        if (name == null){
            binding.txtName.setVisibility(View.GONE);
        }
        else {
            binding.txtName.setVisibility(View.VISIBLE);
            binding.txtName.setText(name);
        }
        binding.txtName.setText(name);
        binding.txtEmail.setText(email);
        Glide.with(this).load(photoUrl).error(R.drawable.avatar).into(binding.imgAvatar);
    }
}