package com.mi.steamfamilygroupfinder;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.mi.steamfamilygroupfinder.models.User;
import com.mi.steamfamilygroupfinder.utility.FirebaseRefs;

import java.util.ArrayList;

public class RegisterActivity extends AppCompatActivity {

    EditText registerEmail, registerPassword, registerUsername;
    Button registerButton;
    TextView goLogin;
    ProgressBar progressBar;
    FirebaseAuth mAuth;
    DatabaseReference usersRef;
    private VideoView videoView;

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
        usersRef = FirebaseRefs.getUsersReference();

        videoView = findViewById(R.id.videoView);
        Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.video_background_2);
        videoView.setVideoURI(videoUri);
        videoView.setOnPreparedListener(mp -> {
            mp.setLooping(true);
            videoView.start();
        });

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
                                if (firebaseUser != null) {
                                    String uid = firebaseUser.getUid();
                                    User user = new User();
                                    user.setUid(uid);
                                    user.setEmail(email);
                                    user.setUsername(username);
                                    user.setGamesOwned(new ArrayList<>());
                                    user.setGamesInterested(new ArrayList<>());
                                    user.setGid(null); // Initialize gid as null
                                    user.setIsGroupLeader(false); // Initialize isGroupLeader as false

                                    usersRef.child(uid).setValue(user)
                                            .addOnSuccessListener(aVoid -> {
                                                Toast.makeText(RegisterActivity.this, R.string.toastOkRegister,
                                                        Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                                startActivity(intent);
                                                finish();
                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(RegisterActivity.this, R.string.toastKoRegisterDatabase,
                                                        Toast.LENGTH_SHORT).show();
                                            });
                                }
                            } else {
                                progressBar.setVisibility(View.GONE);
                                // User creation failed
                                if (task.getException() != null) {
                                    String errorCode = ((FirebaseAuthException) task.getException()).getErrorCode();
                                    switch (errorCode) {
                                        case "ERROR_INVALID_EMAIL":
                                            Toast.makeText(RegisterActivity.this, R.string.toastKoRegisterInvalidEmail,
                                                    Toast.LENGTH_SHORT).show();
                                            break;
                                        case "ERROR_WEAK_PASSWORD":
                                            Toast.makeText(RegisterActivity.this, R.string.toastKoRegisterWeakPassword,
                                                    Toast.LENGTH_SHORT).show();
                                            break;
                                        case "ERROR_EMAIL_ALREADY_IN_USE":
                                            Toast.makeText(RegisterActivity.this, R.string.toastKoRegisterEmailInUse,
                                                    Toast.LENGTH_SHORT).show();
                                            break;
                                        default:
                                            Toast.makeText(RegisterActivity.this, R.string.toastKoRegister,
                                                    Toast.LENGTH_SHORT).show();
                                            break;
                                    }
                                } else {
                                    Toast.makeText(RegisterActivity.this, R.string.toastKoRegister,
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
    }
}
