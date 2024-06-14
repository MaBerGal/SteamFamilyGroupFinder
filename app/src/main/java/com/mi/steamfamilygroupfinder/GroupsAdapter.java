package com.mi.steamfamilygroupfinder;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
        private GroupMembersAdapter membersAdapter;

        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewGroupName = itemView.findViewById(R.id.textViewGroupName);
            recyclerViewGroupMembers = itemView.findViewById(R.id.recyclerViewGroupMembers);
            recyclerViewGroupMembers.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        }

        public void bind(Group group) {
            textViewGroupName.setText(group.getGroupName());

            // Highlight the logged-in user's group
            if (group.getGroupLeader().equals(currentUserId)) {
                // Set a different background color or any other indication
                itemView.setBackgroundColor(Color.parseColor("#FFFF00")); // Yellow background example
            }

            // Assuming group.getMembers() returns List<String> of user IDs
            membersAdapter = new GroupMembersAdapter(context, group.getMembers());
            recyclerViewGroupMembers.setAdapter(membersAdapter);
        }
    }
}
