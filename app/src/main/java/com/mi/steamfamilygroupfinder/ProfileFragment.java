package com.mi.steamfamilygroupfinder;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mi.steamfamilygroupfinder.models.Group;
import com.mi.steamfamilygroupfinder.models.User;
import com.mi.steamfamilygroupfinder.utility.Utils;

public class ProfileFragment extends Fragment {

    private TextView welcomeMessage;
    private TextView groupStatusMessage;
    private TextView tvGamesOwned;
    private TextView tvGamesInterested;
    private TextView tvGroupName;
    private ImageView profileImageView;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    private DatabaseReference groupsRef;

    public ProfileFragment() {
        // Required empty public constructor
    }

    public static ProfileFragment newInstance() {
        return new ProfileFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        groupsRef = FirebaseDatabase.getInstance().getReference().child("groups");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        welcomeMessage = view.findViewById(R.id.tvWelcomeUsername);
        groupStatusMessage = view.findViewById(R.id.tvGroupStatus);
        tvGamesOwned = view.findViewById(R.id.tvGamesOwned);
        tvGamesInterested = view.findViewById(R.id.tvGamesInterested);
        tvGroupName = view.findViewById(R.id.tvGroupName);
        profileImageView = view.findViewById(R.id.ivProfilePicture);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            userRef = FirebaseDatabase.getInstance().getReference().child("users").child(uid);

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (isAdded() && getContext() != null) {
                        if (snapshot.exists()) {
                            User user = snapshot.getValue(User.class);
                            if (user != null) {
                                String welcomeText = getString(R.string.tvWelcome, user.getUsername());
                                welcomeMessage.setText(welcomeText);

                                if (user.getGid() != null) {
                                    groupStatusMessage.setText(getString(R.string.tvGroupStatusOk));
                                    loadGroupName(user.getGid());
                                } else {
                                    groupStatusMessage.setText(getString(R.string.tvGroupStatusKo));
                                }

                                // Load games owned and interested counts
                                if (snapshot.child("gamesOwned").exists()) {
                                    long gamesOwnedCount = snapshot.child("gamesOwned").getChildrenCount();
                                    tvGamesOwned.setText(getString(R.string.tvGamesOwned, gamesOwnedCount));
                                }

                                if (snapshot.child("gamesInterested").exists()) {
                                    long gamesInterestedCount = snapshot.child("gamesInterested").getChildrenCount();
                                    tvGamesInterested.setText(getString(R.string.tvGamesInterested, gamesInterestedCount));
                                }

                                // Load the profile picture
                                loadProfilePicture(user.getProfilePicture());
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), R.string.toastKoDatabase, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        return view;
    }

    private void loadProfilePicture(String base64String) {
        if (isAdded() && getContext() != null) {
            if (base64String != null && !base64String.isEmpty()) {
                byte[] imageBytes = Base64.decode(base64String, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                Bitmap circularBitmap = Utils.getCircleBitmap(bitmap);
                profileImageView.setImageBitmap(circularBitmap);
            }
        }
    }

    private void loadGroupName(String groupId) {
        groupsRef.child(groupId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Group group = snapshot.getValue(Group.class);
                    if (group != null) {
                        String groupName = group.getGroupName();
                        // Set the group name text
                        String text = getString(R.string.tvGroupName) + "\n" + groupName;
                        tvGroupName.setText(text);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), R.string.toastKoDatabase, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}
