package com.mi.steamfamilygroupfinder;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class RegisterActivity extends AppCompatActivity {

    EditText registerEmail, registerPassword, registerUsername;
    Button registerButton;
    TextView goLogin;
    ProgressBar progressBar;
    FirebaseAuth mAuth;
    DatabaseReference databaseReference;

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        registerEmail = findViewById(R.id.edEmail);
        registerPassword = findViewById(R.id.edPassword);
        registerUsername = findViewById(R.id.edUsername);
        registerButton = findViewById(R.id.btnRegister);
        progressBar = findViewById(R.id.progressBar);
        goLogin = findViewById(R.id.tvRegister);
        goLogin.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance("https://steamfamilygroupfinder-default-rtdb.europe-west1.firebasedatabase.app/").getReference("users");

        registerButton.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            String email = registerEmail.getText().toString();
            String password = registerPassword.getText().toString();
            String username = registerUsername.getText().toString();

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(this, R.string.toastPlsEmail, Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            } else if (TextUtils.isEmpty(password)) {
                Toast.makeText(this, R.string.toastPlsPassword, Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            } else if (TextUtils.isEmpty(username)) {
                Toast.makeText(this, R.string.toastPlsUsername, Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            } else {

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            progressBar.setVisibility(View.GONE);
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            String uid = firebaseUser.getUid();
                            UserProfile user = new UserProfile();
                            user.setUid(uid);
                            user.setEmail(email);
                            user.setUsername(username);
                            user.setGamesOwned(new ArrayList<>());
                            user.setGamesInterested(new ArrayList<>());

                            databaseReference.child(uid).setValue(user)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(RegisterActivity.this, R.string.toastOkAccount,
                                                Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(RegisterActivity.this, R.string.toastKoAccount,
                                                Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(RegisterActivity.this, R.string.toastKoAccount,
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
            }
        });
    }
}
