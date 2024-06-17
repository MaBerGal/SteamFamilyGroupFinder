package com.mi.steamfamilygroupfinder.utility;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseRefs {

    private static final String DATABASE_URL = "https://steamfamilygroupfinder-default-rtdb.europe-west1.firebasedatabase.app/";

    private static DatabaseReference usersReference;
    private static DatabaseReference requestsReference;
    private static DatabaseReference chatsReference;
    private static DatabaseReference groupsReference;
    private static DatabaseReference gamesReference;
    private static FirebaseUser currentUser;

    // Initialize the references
    public static void initialize() {
        usersReference = FirebaseDatabase.getInstance(DATABASE_URL)
                .getReference("users");
        requestsReference = FirebaseDatabase.getInstance(DATABASE_URL)
                .getReference("requests");
        chatsReference = FirebaseDatabase.getInstance(DATABASE_URL)
                .getReference("chats");
        groupsReference = FirebaseDatabase.getInstance(DATABASE_URL)
                .getReference("groups");
        gamesReference = FirebaseDatabase.getInstance(DATABASE_URL)
                .getReference("games");
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    // Getters for the references
    public static DatabaseReference getUsersReference() {
        return usersReference;
    }

    public static DatabaseReference getRequestsReference() {
        return requestsReference;
    }

    public static DatabaseReference getChatsReference() {
        return chatsReference;
    }

    public static DatabaseReference getGroupsReference() {
        return groupsReference;
    }

    public static DatabaseReference getGamesReference() {
        return gamesReference;
    }

    public static FirebaseUser getCurrentUser() {
        return currentUser;
    }
}

