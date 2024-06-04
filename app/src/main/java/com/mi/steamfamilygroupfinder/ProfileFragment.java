package com.mi.steamfamilygroupfinder;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileFragment extends Fragment {

    private TextView welcomeMessage;
    private TextView groupStatusMessage;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        welcomeMessage = view.findViewById(R.id.tvWelcome);
        groupStatusMessage = view.findViewById(R.id.tvGroupStatus);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            databaseReference = FirebaseDatabase.getInstance("https://steamfamilygroupfinder-default-rtdb.europe-west1.firebasedatabase.app/").getReference("users").child(uid);

            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        UserProfile userProfile = snapshot.getValue(UserProfile.class);
                        if (userProfile != null) {
                            String welcomeText = getString(R.string.tvWelcome, userProfile.getUsername());
                            welcomeMessage.setText(welcomeText);
                            if (userProfile.getGroup() != null) {
                                groupStatusMessage.setText(R.string.tvGroupStatusOk);
                            } else {
                                groupStatusMessage.setText(R.string.tvGroupStatusKo);
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Handle possible errors.
                }
            });
        }

        return view;
    }
}
