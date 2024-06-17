package com.mi.steamfamilygroupfinder;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mi.steamfamilygroupfinder.utility.FirebaseRefs;

public class MyAccountFragment extends Fragment {

    private EditText editTextUsername;
    private Button buttonChangeUsername;
    private Button buttonDeactivateAccount;
    private FirebaseAuth auth;
    private DatabaseReference userRef;

    public MyAccountFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_my_account, container, false);
        editTextUsername = view.findViewById(R.id.editTextUsername);
        buttonChangeUsername = view.findViewById(R.id.buttonChangeUsername);
        buttonDeactivateAccount = view.findViewById(R.id.buttonDeactivateAccount);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser().getUid();
        userRef = FirebaseRefs.getUsersReference().child(userId);

        // Load current username
        userRef.child("username").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String currentUsername = snapshot.getValue(String.class);
                    editTextUsername.setText(currentUsername);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), R.string.errorLoadUsername, Toast.LENGTH_SHORT).show();
            }
        });

        // Handle change username button click
        buttonChangeUsername.setOnClickListener(v -> {
            String newUsername = editTextUsername.getText().toString().trim();
            if (!newUsername.isEmpty()) {
                changeUsername(newUsername);
            } else {
                editTextUsername.setError(String.valueOf(R.string.warningNewUsername));
            }
        });

        // Handle deactivate account button click
        buttonDeactivateAccount.setOnClickListener(v -> showDeactivateConfirmationDialog());
    }

    private void changeUsername(String newUsername) {
        userRef.child("username").setValue(newUsername)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), R.string.updateUsernameOk, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), R.string.errorUpdateUsername, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showDeactivateConfirmationDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.titleDialogDeleteAccount)
                .setMessage(R.string.confirmationDialogDeleteAccount)
                .setPositiveButton(R.string.deleteDialogOption, (dialog, which) -> deactivateAccount())
                .setNegativeButton(R.string.cancelDialogOption, null)
                .show();
    }

    private void deactivateAccount() {
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();

            // Delete requests related to the user
            deleteRequests();

            // Delete chats where the user is involved
            DatabaseReference chatsRef = FirebaseRefs.getChatsReference();
            chatsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot chatSnapshot : dataSnapshot.getChildren()) {
                        String chatId = chatSnapshot.getKey();
                        if (chatId != null && chatId.contains("_")) {
                            String[] ids = chatId.split("_");
                            if (ids.length == 2 && (ids[0].equals(userId) || ids[1].equals(userId))) {
                                // Determine the other user's ID
                                String otherUserId = (ids[0].equals(userId)) ? ids[1] : ids[0];

                                // Delete the chat from chats node
                                chatSnapshot.getRef().removeValue();
                                // Delete the chat identifier from the other user's chatIdentifiers node
                                deleteChatIdentifierFromOtherUser(userId, otherUserId);
                            }
                        }
                    }

                    // After deleting chats, proceed to remove user from groups
                    removeUserFromGroups(userId);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(getContext(), R.string.errorRemoveChats, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void deleteChatIdentifierFromOtherUser(String currentUser, String otherUser) {
        DatabaseReference otherUserRef = FirebaseRefs.getUsersReference()
                .child(otherUser)
                .child("chatIdentifiers");

        otherUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot identifierSnapshot : dataSnapshot.getChildren()) {
                    String chatIdentifier = identifierSnapshot.getValue(String.class);
                    if (chatIdentifier != null && chatIdentifier.contains(otherUser)) {
                        // Remove the chat identifier for the current user from other user's chatIdentifiers
                        identifierSnapshot.getRef().removeValue();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), R.string.errorRemoveChatOtherUser, Toast.LENGTH_SHORT).show();
                // Toast.makeText(getContext(), "Failed to remove chat identifier from other user.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteRequests() {
        String userId = auth.getCurrentUser().getUid();

        DatabaseReference requestsRef = FirebaseRefs.getRequestsReference();
        DatabaseReference requestsRef2 = FirebaseRefs.getRequestsReference();
        requestsRef.orderByChild("requesterId").equalTo(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    snapshot.getRef().removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), R.string.errorRemoveRequests, Toast.LENGTH_SHORT).show();
            }
        });

        requestsRef2.orderByChild("receiverId").equalTo(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    snapshot.getRef().removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), R.string.errorRemoveRequests, Toast.LENGTH_SHORT).show();
            }
        });

        // After deleting requests, proceed to delete groups
        removeUserFromGroups(userId);
    }

    private void removeUserFromGroups(String userId) {
        //Log.d("MyAccountFragment", "UserId: " + userId);
        DatabaseReference groupsRef = FirebaseRefs.getGroupsReference();
        groupsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot groupSnapshot : dataSnapshot.getChildren()) {
                    String groupId = groupSnapshot.getKey();
                    String groupLeader = groupSnapshot.child("groupLeader").getValue(String.class);

                    //Log.d("MyAccountFragment", "GroupId: " + groupId);
                    //Log.d("MyAccountFragment", "GroupLeader: " + groupLeader);

                    if (groupLeader != null && groupLeader.equals(userId)) {
                        // User is the leader, delete the entire group
                        deleteGroupAndUpdateMembers(groupId);
                    } else {
                        // User is not the leader, just remove the user from the group
                        groupSnapshot.getRef().child("memberIds").child(userId).removeValue();
                        for (DataSnapshot memberSnapshot : groupSnapshot.child("members").getChildren()) {
                            if (memberSnapshot.getValue(String.class).equals(userId)) {
                                memberSnapshot.getRef().removeValue();
                                break;
                            }
                        }

                        // Update user's inGroup status and remove gid
                        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
                        userRef.child("inGroup").setValue(false);
                        userRef.child("gid").removeValue();
                    }
                }

                // After removing user from groups, proceed to delete user data
                deleteUserData();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), R.string.errorRemoveUsersGroup, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteGroupAndUpdateMembers(String groupId) {
        DatabaseReference groupRef = FirebaseRefs.getGroupsReference().child(groupId);
        groupRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot memberSnapshot : dataSnapshot.child("members").getChildren()) {
                    String memberId = memberSnapshot.getValue(String.class);
                    DatabaseReference memberRef = FirebaseDatabase.getInstance().getReference("users").child(memberId);
                    memberRef.child("inGroup").setValue(false);
                    memberRef.child("gid").removeValue();
                }

                // Delete the group
                groupRef.removeValue()
                        .addOnSuccessListener(aVoid -> {
                            //Log.d("MyAccountFragment", "Group " + groupId + " deleted successfully.");
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(getContext(), R.string.errorRemoveGroup, Toast.LENGTH_SHORT).show();
                        });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), R.string.errorUpdateUsersDeletion, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteUserData() {
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference userRef = FirebaseRefs.getUsersReference().child(userId);

            // Remove user data
            userRef.removeValue()
                    .addOnSuccessListener(aVoid -> {
                        // After deleting user data, delete user from Firebase Authentication
                        deleteUserFromAuth();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), R.string.errorRemoveUserData, Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void deleteUserFromAuth() {
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser != null) {
            currentUser.delete()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Sign out and navigate to login screen
                            auth.signOut();
                            Toast.makeText(getContext(), R.string.userDeleteAccountOk, Toast.LENGTH_SHORT).show();
                            // Redirect to LoginActivity
                            Intent intent = new Intent(requireContext(), LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        } else {
                            Toast.makeText(getContext(), R.string.errorUserDeleteAccount, Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

}
