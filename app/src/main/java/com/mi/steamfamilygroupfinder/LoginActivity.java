package com.mi.steamfamilygroupfinder;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.mi.steamfamilygroupfinder.utility.FirebaseRefs;

public class LoginActivity extends AppCompatActivity {

    EditText loginUsername, loginEmail, loginPassword;
    String inputUsername, inputEmail, inputPassword;
    Button loginButton;
    CheckBox cbRemember;
    TextView goRegister;
    ProgressBar progressBar;
    FirebaseAuth mAuth;
    DatabaseReference usersReference;
    SharedPreferences sharedPreferences;
    private VideoView videoView;

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

        videoView = findViewById(R.id.videoView);
        Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.video_background);
        videoView.setVideoURI(videoUri);
        videoView.setOnPreparedListener(mp -> {
            mp.setLooping(true);
            videoView.start();
        });

        loginUsername = findViewById(R.id.edUsername);
        loginEmail = findViewById(R.id.edEmail);
        loginPassword = findViewById(R.id.edPassword);
        loginButton = findViewById(R.id.btnLogin);
        cbRemember = findViewById(R.id.cbRemember);
        progressBar = findViewById(R.id.progressBar);
        goRegister = findViewById(R.id.tvRegister);

        mAuth = FirebaseAuth.getInstance();
        usersReference = FirebaseRefs.getUsersReference();
        sharedPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);

        loadPreferences();

        goRegister.setOnClickListener(v -> {
            if (cbRemember.isChecked()) {
                inputUsername = loginUsername.getText().toString();
                inputEmail = loginEmail.getText().toString();
                inputPassword = loginPassword.getText().toString();
                savePreferences(inputUsername, inputEmail, inputPassword);
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
                finish();
            } else {
                clearPreferences();
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
                finish();
            }
        });

        loginButton.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            String email = loginEmail.getText().toString();
            String password = loginPassword.getText().toString();
            String inputUsername = loginUsername.getText().toString();

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(this, R.string.toastPlsEmail, Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            } else if (TextUtils.isEmpty(password)) {
                Toast.makeText(this, R.string.toastPlsPassword, Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            } else if (TextUtils.isEmpty(inputUsername)) {
                Toast.makeText(this, R.string.toastPlsUsername, Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            } else {
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, task -> {
                            if (task.isSuccessful()) {
                                FirebaseUser firebaseUser = mAuth.getCurrentUser();
                                if (firebaseUser != null) {
                                    String uid = firebaseUser.getUid();
                                    usersReference.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            progressBar.setVisibility(View.GONE);
                                            if (dataSnapshot.exists()) {
                                                String username = dataSnapshot.child("username").getValue(String.class);
                                                if (username != null && username.equals(inputUsername)) {
                                                    if (cbRemember.isChecked()) {
                                                        savePreferences(inputUsername, email, password);
                                                    } else {
                                                        clearPreferences();
                                                    }
                                                    Toast.makeText(LoginActivity.this, R.string.toastOkLogin,
                                                            Toast.LENGTH_SHORT).show();
                                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                                    startActivity(intent);
                                                    finish();
                                                } else {
                                                    // Signing out so that it doesn't go to MainActivity when switching to RegisterActivity
                                                    mAuth.signOut();
                                                    Toast.makeText(LoginActivity.this, R.string.toastKoLoginUsername,
                                                            Toast.LENGTH_SHORT).show();
                                                }
                                            } else {
                                                Toast.makeText(LoginActivity.this, R.string.toastKoLoginDatabase,
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                            progressBar.setVisibility(View.GONE);
                                            Toast.makeText(LoginActivity.this, R.string.toastKoLoginDatabase,
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            } else {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(LoginActivity.this, R.string.toastKoLoginEmailPassword,
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    private void savePreferences(String username, String email, String password) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("username", username);
        editor.putString("email", email);
        editor.putString("password", password);
        editor.putBoolean("remember", true);
        editor.apply();
    }

    private void loadPreferences() {
        boolean remember = sharedPreferences.getBoolean("remember", false);
        if (remember) {
            String username = sharedPreferences.getString("username", "");
            String email = sharedPreferences.getString("email", "");
            String password = sharedPreferences.getString("password", "");
            loginUsername.setText(username);
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
