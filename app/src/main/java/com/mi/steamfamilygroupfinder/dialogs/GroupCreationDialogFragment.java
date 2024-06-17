package com.mi.steamfamilygroupfinder.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.google.firebase.database.ValueEventListener;
import com.mi.steamfamilygroupfinder.GroupsFragment;
import com.mi.steamfamilygroupfinder.R;
import com.mi.steamfamilygroupfinder.adapters.UsersAdapter;
import com.mi.steamfamilygroupfinder.models.Group;
import com.mi.steamfamilygroupfinder.models.Request;
import com.mi.steamfamilygroupfinder.models.User;
import com.mi.steamfamilygroupfinder.utility.FirebaseRefs;

import java.util.ArrayList;
import java.util.List;

public class GroupCreationDialogFragment extends DialogFragment implements UsersAdapter.UserSelectionListener {

    private EditText editTextGroupName;
    private SearchView searchView;
    private RecyclerView recyclerViewUsers;
    private UsersAdapter usersAdapter;
    private List<User> allUsers = new ArrayList<>();
    private List<User> selectedUsers = new ArrayList<>();

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
                .setTitle(R.string.titleDialogCreateGroup)
                .setPositiveButton(R.string.createDialogOption, (dialog, which) -> createGroup())
                .setNegativeButton(R.string.cancelDialogOption, (dialog, which) -> dialog.dismiss());

        return builder.create();
    }

    private void loadAllUsers() {
        DatabaseReference usersReference = FirebaseRefs.getUsersReference();
        usersReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allUsers.clear();
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    User user = userSnapshot.getValue(User.class);
                    if (user != null && !user.getUid().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                        allUsers.add(user);
                    }
                }
                usersAdapter.updateUserList(allUsers);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Context context = getContext();
                if (context != null) {
                    Toast.makeText(context, R.string.errorLoadUsers, Toast.LENGTH_SHORT).show();
                }
                //Log.e("GroupCreationDialog", "Failed to load users: " + error.getMessage());
            }
        });
    }

    private void filterUsers(String query) {
        List<User> filteredUsers = new ArrayList<>();
        for (User user : allUsers) {
            if (user.getUsername().toLowerCase().contains(query.toLowerCase())) {
                filteredUsers.add(user);
            }
        }
        usersAdapter.updateUserList(filteredUsers);
    }

    private void createGroup() {
        String groupName = editTextGroupName.getText().toString().trim();
        String currentUserId = FirebaseRefs.getCurrentUser().getUid();
        DatabaseReference groupsReference = FirebaseRefs.getGroupsReference();
        DatabaseReference usersReference = FirebaseRefs.getUsersReference();

        String newGroupId = groupsReference.push().getKey();
        if (newGroupId == null) {
            Context context = getContext();
            if (context != null) {
                Toast.makeText(context, R.string.errorCreateGroup, Toast.LENGTH_SHORT).show();
            }
            //Log.e("GroupCreationDialog", "Failed to create group ID.");
            return;
        }

        Group newGroup = new Group();
        newGroup.setGid(newGroupId);

        if (TextUtils.isEmpty(groupName)) {
            newGroup.setGroupName(getContext().getResources().getString(R.string.unnamedGroup));
            // Retrieve the current user's username directly from Firebase
            usersReference.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User user = snapshot.getValue(User.class);
                    String username = user != null ? user.getUsername() : getContext().getResources().getString(R.string.unnamedGroup);
                    newGroup.setGroupName(username);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    //Log.e("GroupCreationDialog", "Failed to retrieve current user's username: " + error.getMessage());
                    newGroup.setGroupName(getContext().getResources().getString(R.string.unnamedGroup));
                }
            });
        } else {
            newGroup.setGroupName(editTextGroupName.getText().toString().trim());
        }

        newGroup.setGroupLeader(currentUserId);
        List<String> memberIds = new ArrayList<>();
        memberIds.add(currentUserId); // Add only the current user to the group
        newGroup.setMembers(memberIds);

        groupsReference.child(newGroupId).setValue(newGroup).addOnCompleteListener(task -> {
            Context context = getContext();
            if (task.isSuccessful()) {
                //Log.d("GroupCreationDialog", "Group created successfully with ID: " + newGroupId);

                // Update group ID and leadership status for current user
                usersReference.child(currentUserId).child("gid").setValue(newGroupId).addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        // Notify GroupsFragment to refresh group list
                        if (groupsFragment != null) {
                            groupsFragment.fetchGroups();
                        }
                        usersReference.child(currentUserId).child("isGroupLeader").setValue(true);
                        //Log.d("GroupCreationDialog", "Group ID updated for current user.");
                    } else {
                        //Log.e("GroupCreationDialog", "Failed to update Group ID for current user.");
                    }
                });

                // Send requests to selected users
                for (User user : selectedUsers) {
                    sendGroupRequest(newGroupId, currentUserId, user.getUid(), true);
                }

                if (context != null) {
                    Toast.makeText(context, R.string.createGroupOk, Toast.LENGTH_SHORT).show();
                }
            } else {
                if (context != null) {
                    Toast.makeText(context, R.string.errorCreateGroup, Toast.LENGTH_SHORT).show();
                }
                //Log.e("GroupCreationDialog", "Failed to create group in database: " + task.getException().getMessage());
            }
        });
    }



    private void sendGroupRequest(String groupId, String requesterId, String receiverId, boolean isInvite) {
        // Create a Request object
        Request request = new Request(requesterId, receiverId, groupId, isInvite);
        DatabaseReference requestsReference = FirebaseRefs.getRequestsReference().child(request.getRequestId());

        // Save request in the database
        requestsReference.setValue(request)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        //Log.d("GroupCreationDialog", "Request sent successfully to user: " + receiverId);
                    } else {
                        //Log.e("GroupCreationDialog", "Failed to send request to user: " + receiverId);
                    }
                });
    }

    private List<String> getSelectedUserIds() {
        List<String> userIds = new ArrayList<>();
        for (User user : selectedUsers) {
            userIds.add(user.getUid());
        }
        return userIds;
    }

    // Implement method from UserSelectionListener interface
    @Override
    public void onUserSelectionChanged(User user, boolean isSelected) {
        if (isSelected) {
            if (!selectedUsers.contains(user)) {
                selectedUsers.add(user);
            }
        } else {
            selectedUsers.remove(user);
        }
    }
}
