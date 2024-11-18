package com.example.appdocsachfinal.FragmentUser;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appdocsachfinal.Adapter.AdapterNoti;
import com.example.appdocsachfinal.Model.ModelNoti;
import com.example.appdocsachfinal.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class NotiFragmentUser extends Fragment {

    private RecyclerView notificationsRv;
    private ArrayList<ModelNoti> notificationsList;
    private AdapterNoti adapterNotification;
    private FirebaseAuth firebaseAuth;

    public NotiFragmentUser() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_noti_user, container, false);

        firebaseAuth = FirebaseAuth.getInstance();

        notificationsRv = view.findViewById(R.id.notificationsRv);
        notificationsRv.setLayoutManager(new LinearLayoutManager(getContext()));

        loadNotifications();

        return view;
    }

    private void loadNotifications() {
        notificationsList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Notifications");
        ref.orderByChild("timestamp")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        notificationsList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            ModelNoti model = ds.getValue(ModelNoti.class);
                            notificationsList.add(0, model);
                        }
                        adapterNotification = new AdapterNoti(getContext(), notificationsList);
                        notificationsRv.setAdapter(adapterNotification);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}