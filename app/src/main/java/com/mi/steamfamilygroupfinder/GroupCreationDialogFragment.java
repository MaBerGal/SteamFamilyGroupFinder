package com.mi.steamfamilygroupfinder;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class GroupCreationDialogFragment extends DialogFragment implements UsersAdapter.UserSelectionListener {

    private EditText editTextGroupName;
    private SearchView searchView;
    private RecyclerView recyclerViewUsers;
    private UsersAdapter usersAdapter;
    private List<UserProfile> allUsers = new ArrayList<>();
    private List<UserProfile> selectedUsers = new ArrayList<>();
    private static final String TAG = "GroupCreationDialog";

    private GroupsFragment groupsFragment; // Store reference to GroupsFragment

    public static GroupCreationDialogFragment newInstance(GroupsFragment groupsFragment) {
        GroupCreationDialogFragment fragment = new GroupCreationDialogFragment();
        fragment.groupsFragment = groupsFragment; // Store reference to GroupsFragment
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_group_creation, null);

        editTextGroupName = view.findViewById(R.id.editTextGroupName);
        searchView = view.findViewById(R.id.searchViewUsers);
        recyclerViewUsers = view.findViewById(R.id.recyclerViewUsers);
        recyclerViewUsers.setLayoutManager(new LinearLayoutManager(getContext()));
        usersAdapter = new UsersAdapter(new ArrayList<>());
        recyclerViewUsers.setAdapter(usersAdapter);

        usersAdapter.setUserSelectionListener(this);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterUsers(newText);
                return true;
            }
        });

        loadAllUsers();

        builder.setView(view)
                .setTitle("Create Group")
                .setPositiveButton("Create", (dialog, which) -> createGroup())
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        return builder.create();
    }

    private void loadAllUsers() {
        DatabaseReference usersReference = FirebaseDatabase.getInstance().getReference("users");
        usersReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allUsers.clear();
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    UserProfile userProfile = userSnapshot.getValue(UserProfile.class);
                    if (userProfile != null && !userProfile.getUid().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                        allUsers.add(userProfile);
                    }
                }
                usersAdapter.updateUserList(allUsers);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Context context = getContext();
                if (context != null) {
                    Toast.makeText(context, "Failed to load users.", Toast.LENGTH_SHORT).show();
                }
                Log.e(TAG, "Failed to load users: " + error.getMessage());
            }
        });
    }

    private void filterUsers(String query) {
        List<UserProfile> filteredUsers = new ArrayList<>();
        for (UserProfile user : allUsers) {
            if (user.getUsername().toLowerCase().contains(query.toLowerCase())) {
                filteredUsers.add(user);
            }
        }
        usersAdapter.updateUserList(filteredUsers);
    }

    private void createGroup() {
        String groupName = editTextGroupName.getText().toString().trim();
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference groupsReference = FirebaseDatabase.getInstance().getReference("groups");
        DatabaseReference usersReference = FirebaseDatabase.getInstance().getReference("users");

        String newGroupId = groupsReference.push().getKey();
        if (newGroupId == null) {
            Context context = getContext();
            if (context != null) {
                Toast.makeText(context, "Failed to create group.", Toast.LENGTH_SHORT).show();
            }
            Log.e(TAG, "Failed to create group ID.");
            return;
        }

        Group newGroup = new Group();
        newGroup.setGid(newGroupId);

        // Set group name
        if (TextUtils.isEmpty(groupName)) {
            String username = allUsers.stream()
                    .filter(user -> user.getUid().equals(currentUserId))
                    .findFirst()
                    .map(UserProfile::getUsername)
                    .orElse("Unnamed");
            groupName = username + "'s Group";
        }
        newGroup.setGroupName(groupName);

        newGroup.setGroupLeader(currentUserId);
        List<String> memberIds = new ArrayList<>();
        memberIds.add(currentUserId); // Add only the current user to the group
        newGroup.setMembers(memberIds);

        groupsReference.child(newGroupId).setValue(newGroup).addOnCompleteListener(task -> {
            Context context = getContext();
            if (task.isSuccessful()) {
                Log.d(TAG, "Group created successfully with ID: " + newGroupId);

                // Update group ID and leadership status for current user
                usersReference.child(currentUserId).child("gid").setValue(newGroupId).addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        // Notify GroupsFragment to refresh group list
                        if (groupsFragment != null) {
                            groupsFragment.fetchGroups();
                        }
                        usersReference.child(currentUserId).child("isGroupLeader").setValue(true);
                        Log.d(TAG, "Group ID updated for current user.");
                    } else {
                        Log.e(TAG, "Failed to update Group ID for current user.");
                    }
                });

                // Send requests to selected users
                for (UserProfile user : selectedUsers) {
                    sendGroupRequest(newGroupId, currentUserId, user.getUid(), true);
                }

                if (context != null) {
                    Toast.makeText(context, "Group created successfully.", Toast.LENGTH_SHORT).show();
                }
            } else {
                if (context != null) {
                    Toast.makeText(context, "Failed to create group.", Toast.LENGTH_SHORT).show();
                }
                Log.e(TAG, "Failed to create group in database: " + task.getException().getMessage());
            }
        });
    }



    private void sendGroupRequest(String groupId, String requesterId, String receiverId, boolean isInvite) {
        // Create a Request object
        Request request = new Request(requesterId, receiverId, groupId, isInvite);
        DatabaseReference requestsReference = FirebaseDatabase.getInstance().getReference("requests").child(request.getRequestId());

        // Save request in the database
        requestsReference.setValue(request)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Request sent successfully to user: " + receiverId);
                    } else {
                        Log.e(TAG, "Failed to send request to user: " + receiverId);
                    }
                });
    }

    private List<String> getSelectedUserIds() {
        List<String> userIds = new ArrayList<>();
        for (UserProfile user : selectedUsers) {
            userIds.add(user.getUid());
        }
        return userIds;
    }

    // Implement method from UserSelectionListener interface
    @Override
    public void onUserSelectionChanged(UserProfile user, boolean isSelected) {
        if (isSelected) {
            if (!selectedUsers.contains(user)) {
                selectedUsers.add(user);
            }
        } else {
            selectedUsers.remove(user);
        }
    }
}
