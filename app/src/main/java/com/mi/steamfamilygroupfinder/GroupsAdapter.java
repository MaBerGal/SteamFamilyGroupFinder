// GroupsAdapter.java
package com.mi.steamfamilygroupfinder;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class GroupsAdapter extends RecyclerView.Adapter<GroupsAdapter.GroupViewHolder> {

    private Context context;
    private List<Group> groupList;
    private String currentUserId;

    public GroupsAdapter(Context context, List<Group> groupList, String currentUserId) {
        this.context = context;
        this.groupList = groupList;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_group, parent, false);
        return new GroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        Group group = groupList.get(position);
        holder.bind(group);
    }

    @Override
    public int getItemCount() {
        return groupList.size();
    }

    public class GroupViewHolder extends RecyclerView.ViewHolder {

        private TextView textViewGroupName;
        private RecyclerView recyclerViewGroupMembers;
        private CardView cardViewGroup;

        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewGroupName = itemView.findViewById(R.id.textViewGroupName);
            recyclerViewGroupMembers = itemView.findViewById(R.id.recyclerViewGroupMembers);
            recyclerViewGroupMembers.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
            cardViewGroup = itemView.findViewById(R.id.cardViewGroup);
        }

        public void bind(Group group) {
            textViewGroupName.setText(group.getGroupName());

            // Check if the current user belongs to the group
            boolean isMember = group.getMembers().contains(currentUserId);
            boolean hasLessThanSixMembers = group.getMembers().size() < 6;
            boolean userBelongsToAnotherGroup = false; // Implement your logic to check this

            // Retrieve inGroup attribute for the current user
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUserId).child("inGroup");
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Boolean inGroup = dataSnapshot.getValue(Boolean.class);
                    boolean userBelongsToAnotherGroup = inGroup != null && inGroup;

                    // Setup the members adapter
                    GroupMembersAdapter membersAdapter = new GroupMembersAdapter(context, group.getMembers());
                    recyclerViewGroupMembers.setAdapter(membersAdapter);

                    // Handle click on group item
                    itemView.setOnClickListener(v -> {
                        showGroupDetailsDialog(group, isMember, hasLessThanSixMembers, userBelongsToAnotherGroup);
                    });

                    // Update UI based on membership and inGroup status
                    if (isMember) {
                        cardViewGroup.setBackgroundColor(Color.parseColor("#FFFF00")); // Yellow background example
                    } else {
                        cardViewGroup.setBackgroundColor(Color.TRANSPARENT); // Clear background if not a member
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });


            // Setup the members adapter
            GroupMembersAdapter membersAdapter = new GroupMembersAdapter(context, group.getMembers());
            recyclerViewGroupMembers.setAdapter(membersAdapter);

            // Check if the current user belongs to the group
            if (group.getMembers().contains(currentUserId)) {
                cardViewGroup.setBackgroundColor(Color.parseColor("#FFFF00")); // Yellow background example
            } else {
                cardViewGroup.setBackgroundColor(Color.TRANSPARENT); // Clear background if not a member
            }

            // Handle click on group item
            itemView.setOnClickListener(v -> {
                showGroupDetailsDialog(group, isMember, hasLessThanSixMembers, userBelongsToAnotherGroup);
            });
        }

        private void showGroupDetailsDialog(Group group, boolean isMember, boolean hasLessThanSixMembers, boolean userBelongsToAnotherGroup) {
            // Inflate the dialog layout
            View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_group_details, null);

            // Initialize dialog and its components
            Dialog dialog = new Dialog(context);
            dialog.setContentView(dialogView);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));

            // Initialize views from dialog layout
            TextView textViewGroupNameDialog = dialog.findViewById(R.id.textViewGroupName);
            RecyclerView recyclerViewGroupMembersModal = dialog.findViewById(R.id.recyclerViewGroupMembersModal);
            Button buttonViewGroupLibrary = dialog.findViewById(R.id.buttonViewGroupLibrary);
            Button buttonRequestJoinGroup = dialog.findViewById(R.id.buttonJoinRequest);
            Button buttonLeaveGroup = dialog.findViewById(R.id.buttonLeaveGroup);

            // Set group name
            textViewGroupNameDialog.setText(group.getGroupName());

            // Setup members adapter for the modal dialog
            GroupMemberDetailsAdapter membersModalAdapter = new GroupMemberDetailsAdapter(context, group.getMembers(), dialog);
            recyclerViewGroupMembersModal.setLayoutManager(new LinearLayoutManager(context));
            recyclerViewGroupMembersModal.setAdapter(membersModalAdapter);

            // Show or hide buttons based on conditions
            if (!isMember && hasLessThanSixMembers && !userBelongsToAnotherGroup) {
                buttonRequestJoinGroup.setVisibility(View.VISIBLE);
            } else {
                buttonRequestJoinGroup.setVisibility(View.GONE);
            }

            if (isMember) {
                buttonLeaveGroup.setVisibility(View.VISIBLE);
            } else {
                buttonLeaveGroup.setVisibility(View.GONE);
            }

            // Set onClickListeners
            buttonViewGroupLibrary.setOnClickListener(v -> {
                openGroupMemberLibraryFragment(group.getMembers());
                dialog.dismiss();
            });

            buttonRequestJoinGroup.setOnClickListener(v -> {
                String groupId = group.getGid(); // Replace with your actual method to get groupId
                String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid(); // Get current user ID
                sendGroupRequest(groupId, currentUserId, group.getGroupLeader(), false); // Send join group request
                dialog.dismiss();
            });

            buttonLeaveGroup.setOnClickListener(v -> {
                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUserId);
                DatabaseReference groupRef = FirebaseDatabase.getInstance().getReference("groups").child(group.getGid());

                // Check if the current user is the leader of the group
                groupRef.child("groupLeader").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String groupLeaderId = snapshot.getValue(String.class);
                        if (groupLeaderId != null && groupLeaderId.equals(currentUserId)) {
                            // Current user is the leader, ask for confirmation to delete the group
                            new AlertDialog.Builder(context)
                                    .setTitle("Leave Group")
                                    .setMessage("You are the leader of this group. Leaving the group will delete it. Do you want to proceed?")
                                    .setPositiveButton("Yes", (dialog, which) -> deleteGroup(groupRef, userRef, (Dialog) dialog))
                                    .setNegativeButton("No", null)
                                    .show();
                        } else {
                            // Current user is not the leader, proceed with leaving the group
                            leaveGroup(userRef, groupRef, dialog);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(context, "Failed to check group leader status.", Toast.LENGTH_SHORT).show();
                    }
                });
            });


            // Show the dialog
            dialog.show();
        }

        private void leaveGroup(DatabaseReference userRef, DatabaseReference groupRef, Dialog dialog) {
            userRef.child("gid").removeValue()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            userRef.child("inGroup").setValue(false)
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            groupRef.child("members")
                                                    .orderByValue()
                                                    .equalTo(currentUserId)
                                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                                snapshot.getRef().removeValue()
                                                                        .addOnCompleteListener(task2 -> {
                                                                            if (task2.isSuccessful()) {
                                                                                Toast.makeText(context, "You left the group.", Toast.LENGTH_SHORT).show();
                                                                                dialog.dismiss();
                                                                            } else {
                                                                                Toast.makeText(context, "Failed to remove from group members.", Toast.LENGTH_SHORT).show();
                                                                            }
                                                                        });
                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                                            Toast.makeText(context, "Failed to remove from group members.", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                        } else {
                                            Toast.makeText(context, "Failed to update inGroup status.", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            Toast.makeText(context, "Failed to remove group ID.", Toast.LENGTH_SHORT).show();
                        }
                    });
        }

        private void deleteGroup(DatabaseReference groupRef, DatabaseReference userRef, Dialog dialog) {
            groupRef.removeValue().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    userRef.child("gid").removeValue()
                            .addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    userRef.child("inGroup").setValue(false)
                                            .addOnCompleteListener(task2 -> {
                                                if (task2.isSuccessful()) {
                                                    Toast.makeText(context, "Group deleted successfully.", Toast.LENGTH_SHORT).show();
                                                    dialog.dismiss();
                                                } else {
                                                    Toast.makeText(context, "Failed to update inGroup status.", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                } else {
                                    Toast.makeText(context, "Failed to remove group ID.", Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    Toast.makeText(context, "Failed to delete the group.", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(context, "Request sent!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "Failed to send request", Toast.LENGTH_SHORT).show();
                        }
                    });
        }


        private void openGroupMemberLibraryFragment(List<String> memberIds) {
            // Create a bundle and put memberIds into it
            Bundle args = new Bundle();
            String[] memberIdsArray = memberIds.toArray(new String[0]);
            args.putStringArray("memberIds", memberIdsArray);

            // Iterate through memberIds array and print each member id
            for (String memberId : memberIdsArray) {
                Log.d("MemberId", memberId);
            }

            // Navigate to GroupMemberLibraryFragment and pass the bundle
            GroupMemberLibraryFragment fragment = new GroupMemberLibraryFragment();
            fragment.setArguments(args);

            // Assuming you're using FragmentManager to navigate
            FragmentManager fragmentManager = ((FragmentActivity) context).getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.bottomFragmentContainer, fragment)  // Replace bottomFragmentContainer with your actual container id
                    .addToBackStack(null)
                    .commit();
        }

    }
}
