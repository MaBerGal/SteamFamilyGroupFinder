package com.mi.steamfamilygroupfinder;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
    private ImageView profileImageView;
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
        welcomeMessage = view.findViewById(R.id.tvWelcomeUsername);
        groupStatusMessage = view.findViewById(R.id.tvGroupStatus);
        profileImageView = view.findViewById(R.id.ivProfilePicture);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            databaseReference = FirebaseDatabase.getInstance("https://steamfamilygroupfinder-default-rtdb.europe-west1.firebasedatabase.app/").getReference("users").child(uid);

            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (isAdded() && getContext() != null) { // Check if fragment is added and has valid context
                        if (snapshot.exists()) {
                            UserProfile userProfile = snapshot.getValue(UserProfile.class);
                            if (userProfile != null) {
                                String welcomeText = getString(R.string.tvWelcome, userProfile.getUsername());
                                welcomeMessage.setText(welcomeText);
                                if (userProfile.getGid() != null) {
                                    groupStatusMessage.setText(R.string.tvGroupStatusOk);
                                } else {
                                    groupStatusMessage.setText(R.string.tvGroupStatusKo);
                                }
                                // Load the profile picture
                                loadProfilePicture();
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    if (getContext() != null) { // Check if context is valid before showing toast
                        Toast.makeText(getContext(), R.string.toastKoDatabase, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        return view;
    }

    private void loadProfilePicture() {
        if (isAdded() && getContext() != null) { // Check if fragment is added and has valid context
            String userId = mAuth.getCurrentUser().getUid();
            databaseReference.child("profilePicture").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (isAdded() && getContext() != null) { // Check if fragment is added and has valid context
                        String base64String = dataSnapshot.getValue(String.class);
                        if (base64String != null && !base64String.isEmpty()) {
                            byte[] imageBytes = Base64.decode(base64String, Base64.DEFAULT);
                            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                            Bitmap circularBitmap = getCircleBitmap(bitmap);
                            profileImageView.setImageBitmap(circularBitmap);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    if (getContext() != null) { // Check if context is valid before showing toast
                        Toast.makeText(getContext(), R.string.errorLoadImage, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private Bitmap getCircleBitmap(Bitmap bitmap) {
        int size = Math.min(bitmap.getWidth(), bitmap.getHeight());
        Bitmap output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final float radius = size / 2f;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawCircle(radius, radius, radius, paint);

        paint.setXfermode(new android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, (size - bitmap.getWidth()) / 2f, (size - bitmap.getHeight()) / 2f, paint);

        return output;
    }

}

