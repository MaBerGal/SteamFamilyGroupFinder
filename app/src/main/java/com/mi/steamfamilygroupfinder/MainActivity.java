package com.mi.steamfamilygroupfinder;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    Button logoutButton;
    TextView tvUser;
    FirebaseAuth auth;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        tvUser = findViewById(R.id.tvUser);
        user = auth.getCurrentUser();

        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);

        if (user == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        } else {
            tvUser.setText(user.getEmail());
        }

        BottomNavigationView bottomNav = findViewById(R.id.bottom_fragments);
        bottomNav.setSelectedItemId(R.id.bottom_menu_profile);
        bottomNav.setOnItemSelectedListener(item -> {
            // Using IF instead of Switch to avoid non-final ID issues
            if (item.getItemId() == R.id.bottom_menu_profile) {
                getSupportFragmentManager().beginTransaction().replace(R.id.bottomFragmentContainer, new ProfileFragment()).commit();
                overridePendingTransition(0, 0);
                return true;
            } else if (item.getItemId() == R.id.bottom_menu_groups) {
                getSupportFragmentManager().beginTransaction().replace(R.id.bottomFragmentContainer, new GroupsFragment()).commit();
                overridePendingTransition(0, 0);
                return true;
            } else if (item.getItemId() == R.id.bottom_menu_inbox) {
                getSupportFragmentManager().beginTransaction().replace(R.id.bottomFragmentContainer, new InboxFragment()).commit();
                overridePendingTransition(0, 0);
                return true;
            } else {
                return false;
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


}