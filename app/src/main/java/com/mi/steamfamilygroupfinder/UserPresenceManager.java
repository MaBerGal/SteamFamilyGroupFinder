package com.mi.steamfamilygroupfinder;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import android.os.Handler;

public class UserPresenceManager {
    private static final long PRESENCE_UPDATE_INTERVAL = 60 * 1000; // 1 minute
    private Handler handler = new Handler();
    private String userId;
    private DocumentReference userDocRef;

    public UserPresenceManager(String userId) {
        this.userId = userId;
        userDocRef = FirebaseFirestore.getInstance().collection("users").document(userId);
    }

    public void start() {
        updatePresence();
        handler.postDelayed(presenceUpdater, PRESENCE_UPDATE_INTERVAL);
    }

    public void stop() {
        handler.removeCallbacks(presenceUpdater);
    }

    private void updatePresence() {
        userDocRef.update("lastActive", FieldValue.serverTimestamp());
    }

    private Runnable presenceUpdater = new Runnable() {
        @Override
        public void run() {
            updatePresence();
            handler.postDelayed(this, PRESENCE_UPDATE_INTERVAL);
        }
    };
}
