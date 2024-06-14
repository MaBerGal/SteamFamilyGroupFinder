package com.mi.steamfamilygroupfinder;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class GroupsFragment extends Fragment {

    private FloatingActionButton fabCreateGroup;
    private RecyclerView recyclerViewGroups;
    private GroupsAdapter groupsAdapter;
    private List<Group> groupList = new ArrayList<>();
    private DatabaseReference databaseReference;
    private String userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_groups, container, false);

        fabCreateGroup = view.findViewById(R.id.fabCreateGroup);
        recyclerViewGroups = view.findViewById(R.id.recyclerViewGroups);
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference("groups");

        recyclerViewGroups.setLayoutManager(new LinearLayoutManager(getContext()));
        groupsAdapter = new GroupsAdapter(getContext(), groupList, userId);
        recyclerViewGroups.setAdapter(groupsAdapter);

        fetchGroups();

        fabCreateGroup.setOnClickListener(v -> {
            GroupCreationDialogFragment dialogFragment = GroupCreationDialogFragment.newInstance(this);
            dialogFragment.show(getChildFragmentManager(), "GroupCreationDialogFragment");
        });

        return view;
    }

    void fetchGroups() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                groupList.clear();
                boolean userInGroup = false;
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Group group = dataSnapshot.getValue(Group.class);
                    if (group != null) {
                        groupList.add(group);
                        // Check if the user is already in any group
                        if (group.getMembers().contains(userId)) {
                            userInGroup = true;
                        }
                    }
                }

                // Sort the groupList to show the logged-in user's group first
                groupList.sort((group1, group2) -> {
                    // Check if group1 is led by the current user
                    if (group1.getGroupLeader().equals(userId)) {
                        return -1; // group1 should come before group2
                    } else if (group2.getGroupLeader().equals(userId)) {
                        return 1; // group2 should come before group1
                    } else {
                        return 0; // maintain the current order
                    }
                });

                groupsAdapter.notifyDataSetChanged();

                // Disable FAB if the user is already in a group
                if (userInGroup) {
                    fabCreateGroup.setEnabled(false);
                    fabCreateGroup.setVisibility(View.GONE); // Optionally hide the FAB
                } else {
                    fabCreateGroup.setEnabled(true);
                    fabCreateGroup.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load groups.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
