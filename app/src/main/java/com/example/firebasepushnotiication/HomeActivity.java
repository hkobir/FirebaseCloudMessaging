package com.example.firebasepushnotiication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Switch;
import android.widget.Toast;

import com.example.firebasepushnotiication.databinding.ActivityHomeBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {
    private ActivityHomeBinding binding;
    private List<String> nameList;
    private List<String> uIdList;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home);
        getSupportActionBar().setTitle(FirebaseAuth.getInstance().getCurrentUser().getEmail());

        nameList = new ArrayList<>();
        uIdList = new ArrayList<>();
        userId = FirebaseAuth.getInstance().getUid();
        getUser();

        binding.userLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                binding.progress.setVisibility(View.VISIBLE);

                Map<String, Object> notificationMap = new HashMap<>();
                notificationMap.put("message", nameList.get(i)+" send you a request");
                notificationMap.put("senderId", userId);

                FirebaseFirestore.getInstance().
                        collection("members").
                        document(uIdList.get(i)).
                        collection("notifications").
                        add(notificationMap).
                        addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentReference> task) {
                                if (task.isSuccessful()) {
                                    binding.progress.setVisibility(View.GONE);
                                    Toast.makeText(HomeActivity.this, "Notification sent", Toast.LENGTH_SHORT).show();

                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(HomeActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        binding.progress.setVisibility(View.GONE);
                    }
                });
            }
        });
    }

    private void getUser() {
        binding.progress.setVisibility(View.VISIBLE);
        FirebaseDatabase.getInstance().getReference().child("members").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    nameList.clear();
                    uIdList.clear();
                    for (DataSnapshot data : snapshot.getChildren()) {
                        String name = data.child("name").getValue().toString();
                        String uId = data.getKey();
                        nameList.add(name);
                        uIdList.add(uId);
                    }
                    binding.progress.setVisibility(View.GONE);
                    initListView();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void initListView() {
        ArrayAdapter adapter = new ArrayAdapter(HomeActivity.this, android.R.layout.simple_expandable_list_item_1, nameList);
        binding.userLV.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.logout) {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(HomeActivity.this, MainActivity.class));
            finish();

        }
        return true;
    }
}