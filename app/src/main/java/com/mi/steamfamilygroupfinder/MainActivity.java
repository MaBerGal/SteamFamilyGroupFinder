package com.mi.steamfamilygroupfinder;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.mi.steamfamilygroupfinder.dialogs.AboutDialogFragment;
import com.mi.steamfamilygroupfinder.models.User;
import com.mi.steamfamilygroupfinder.utility.FirebaseRefs;
import com.mi.steamfamilygroupfinder.utility.Utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private FirebaseUser user;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private ImageView imageViewProfile;
    private DatabaseReference currentUserReference;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        currentUserReference = FirebaseRefs.getUsersReference().child(user.getUid());

        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.my_drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.navOpen, R.string.navClose);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        NavigationView navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        imageViewProfile = headerView.findViewById(R.id.ivProfilePicture);
        TextView tvHeaderUsername = headerView.findViewById(R.id.tvHeaderUsername);
        TextView tvHeaderEmail = headerView.findViewById(R.id.tvHeaderEmail);

        // Initialize MediaPlayer
        mediaPlayer = MediaPlayer.create(this, R.raw.switch_sound);

        if (user == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        } else {
            currentUserReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        User user = snapshot.getValue(User.class);
                        if (user != null) {
                            tvHeaderUsername.setText(user.getUsername());
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Toast.makeText(MainActivity.this, R.string.toastKoDatabase,
                            Toast.LENGTH_SHORT).show();
                }
            });

            tvHeaderEmail.setText(user.getEmail());
            loadProfilePicture();
        }

        imageViewProfile.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });

        navigationView.setNavigationItemSelectedListener(menuItem -> {
            int id = menuItem.getItemId();
            if (id == R.id.nav_library) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.bottomFragmentContainer, new LibraryFragment())
                        .addToBackStack(null)
                        .commit();
            } else if (id == R.id.nav_myaccount) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.bottomFragmentContainer, new MyAccountFragment())
                        .addToBackStack(null)
                        .commit();
            } else if (id == R.id.nav_logout) {
                auth.signOut();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        BottomNavigationView bottomNav = findViewById(R.id.bottom_fragments);
        bottomNav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.bottom_menu_profile) {
                playSwitchSound();
                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right)
                        .replace(R.id.bottomFragmentContainer, new ProfileFragment())
                        .commit();
                return true;
            } else if (item.getItemId() == R.id.bottom_menu_groups) {
                playSwitchSound();
                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right)
                        .replace(R.id.bottomFragmentContainer, new GroupsFragment())
                        .commit();
                return true;
            } else if (item.getItemId() == R.id.bottom_menu_inbox) {
                playSwitchSound();
                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right)
                        .replace(R.id.bottomFragmentContainer, new InboxFragment())
                        .commit();
                return true;
            } else {
                return false;
            }
        });

    }

    private void playSwitchSound() {
        if (mediaPlayer != null) {
            mediaPlayer.start();
        }
    }


    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                            String base64String = encodeImageToBase64(bitmap);
                            saveProfilePicture(base64String);
                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(this, R.string.errorLoadImage, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
    );

    private String encodeImageToBase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    private void saveProfilePicture(String base64String) {
        String userId = user.getUid();
        currentUserReference.child("profilePicture").setValue(base64String)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        loadProfilePicture();
                        Toast.makeText(MainActivity.this, R.string.saveImageOk, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, R.string.errorSaveImage, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadProfilePicture() {
        String userId = user.getUid();
        currentUserReference.child("profilePicture").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String base64String = dataSnapshot.getValue(String.class);
                if (base64String != null && !base64String.isEmpty()) {
                    byte[] imageBytes = Base64.decode(base64String, Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                    Bitmap circularBitmap = Utils.getCircleBitmap(bitmap);
                    imageViewProfile.setImageBitmap(circularBitmap);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, R.string.errorLoadImage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.top_right_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        int id = item.getItemId();
        if (id == R.id.top_right_menu_about) {
            showAboutDialog();
            return true;
        } else if (id == R.id.top_right_menu_logout) {
            auth.signOut();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showAboutDialog() {
        AboutDialogFragment dialogFragment = new AboutDialogFragment();
        dialogFragment.show(getSupportFragmentManager(), String.valueOf(R.string.menuAbout));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }


}
