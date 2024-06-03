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

import java.util.ArrayList;

public class RegisterActivity extends AppCompatActivity {

    EditText registerEmail, registerPassword;
    Button registerButton;
    TextView goLogin;
    ProgressBar progressBar;
    FirebaseAuth mAuth;

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
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
        registerButton = findViewById(R.id.btnRegister);
        progressBar = findViewById(R.id.progressBar);
        goLogin = findViewById(R.id.tvRegister);
        goLogin.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        mAuth = FirebaseAuth.getInstance();

        registerButton.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            String email = registerEmail.getText().toString();
            String password = registerPassword.getText().toString();

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(this, R.string.toastPlsEmail, Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(password)) {
                Toast.makeText(this, R.string.toastPlsPassword, Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(RegisterActivity.this, R.string.toastOkAccount,
                                    Toast.LENGTH_SHORT).show();
                            UserProfile user = new UserProfile();
                            user.setEmail(email);
                            String trimmedEmail = email.substring(0, email.indexOf('@'));
                            user.setUsername(trimmedEmail);
                            user.setGamesOwned(new ArrayList<>());
                            user.setGamesInterested(new ArrayList<>());
                            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(RegisterActivity.this, R.string.toastKoAccount,
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        });

    }
}