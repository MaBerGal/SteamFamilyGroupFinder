package com.mi.steamfamilygroupfinder;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    EditText loginEmail, loginPassword;
    Button loginButton;
    CheckBox cbRemember;
    TextView goRegister;
    ProgressBar progressBar;
    FirebaseAuth mAuth;
    SharedPreferences sharedPreferences;

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginEmail = findViewById(R.id.edEmail);
        loginPassword = findViewById(R.id.edPassword);
        loginButton = findViewById(R.id.btnLogin);
        cbRemember = findViewById(R.id.cbRemember);
        progressBar = findViewById(R.id.progressBar);
        goRegister = findViewById(R.id.tvRegister);

        mAuth = FirebaseAuth.getInstance();
        sharedPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);

        loadPreferences();

        goRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
            finish();
        });

        loginButton.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            String email = loginEmail.getText().toString();
            String password = loginPassword.getText().toString();

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(this, R.string.toastPlsEmail, Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                return;
            }

            if (TextUtils.isEmpty(password)) {
                Toast.makeText(this, R.string.toastPlsPassword, Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            if (cbRemember.isChecked()) {
                                savePreferences(email, password);
                            } else {
                                clearPreferences();
                            }
                            Toast.makeText(LoginActivity.this, R.string.toastOkLogin,
                                    Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(LoginActivity.this, R.string.toastKoLogin,
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    private void savePreferences(String email, String password) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("email", email);
        editor.putString("password", password);
        editor.putBoolean("remember", true);
        editor.apply();
    }

    private void loadPreferences() {
        boolean remember = sharedPreferences.getBoolean("remember", false);
        if (remember) {
            String email = sharedPreferences.getString("email", "");
            String password = sharedPreferences.getString("password", "");
            loginEmail.setText(email);
            loginPassword.setText(password);
            cbRemember.setChecked(true);
        }
    }

    private void clearPreferences() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }
}
