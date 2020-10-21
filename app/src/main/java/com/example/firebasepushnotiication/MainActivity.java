package com.example.firebasepushnotiication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.firebasepushnotiication.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            startActivity(new Intent(MainActivity.this, HomeActivity.class));
            finish();
        }
        binding.signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (binding.nameET.getText().toString().isEmpty() ||
                        binding.emailET.getText().toString().isEmpty() ||
                        binding.passET.getText().toString().isEmpty()) {
                    Toast.makeText(MainActivity.this, "Field must not be empty!", Toast.LENGTH_SHORT).show();
                } else {
                    createUser();
                }

            }
        });
    }

    private void createUser() {
        binding.progress.setVisibility(View.VISIBLE);

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(binding.emailET.getText().toString(),
                binding.passET.getText().toString()).
                addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //save user info into database
                            final String uID = FirebaseAuth.getInstance().getUid();
                            User user = new User(binding.nameET.getText().toString(),
                                    binding.emailET.getText().toString());
                            DatabaseReference userRef = FirebaseDatabase.getInstance().
                                    getReference().child("members").child(uID);

                            userRef.setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                                        @Override
                                        public void onComplete(@NonNull Task<String> task) {
                                            String tokenId = task.getResult();
                                            Log.d("token id: ", tokenId);
                                            Map<String, Object> tokenMap = new HashMap<>();
                                            tokenMap.put("tokenId", tokenId);
                                            FirebaseFirestore.getInstance().
                                                    collection("members").
                                                    document(uID).set(tokenMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        binding.progress.setVisibility(View.GONE);
                                                        Toast.makeText(MainActivity.this, "Success", Toast.LENGTH_SHORT).show();
                                                        startActivity(new Intent(MainActivity.this, HomeActivity.class));
                                                        finish();
                                                    }
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(MainActivity.this,
                                                            "" + e.getMessage(),
                                                            Toast.LENGTH_SHORT).show();
                                                    binding.progress.setVisibility(View.GONE);
                                                }
                                            });
                                        }
                                    });
                                }
                            });
                        }
                    }
                });
    }
}