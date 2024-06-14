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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class RequestsTabFragment extends Fragment {

    private RecyclerView recyclerViewRequests;
    private RequestsAdapter requestsAdapter;
    private List<Request> requestsList = new ArrayList<>();
    private DatabaseReference requestsReference;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_requests_tab, container, false);

        recyclerViewRequests = view.findViewById(R.id.recyclerViewRequests);
        recyclerViewRequests.setLayoutManager(new LinearLayoutManager(getContext()));

        requestsAdapter = new RequestsAdapter(requireContext(), requestsList, new RequestsAdapter.RequestActionListener() {
            @Override
            public void onAccept(Request request) {
                handleAccept(request);
            }

            @Override
            public void onReject(Request request) {
                handleReject(request);
            }
        });
        recyclerViewRequests.setAdapter(requestsAdapter);

        loadRequests();

        return view;
    }

    private void loadRequests() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        requestsReference = FirebaseDatabase.getInstance().getReference("requests");
        requestsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                requestsList.clear();
                for (DataSnapshot requestSnapshot : snapshot.getChildren()) {
                    Request request = requestSnapshot.getValue(Request.class);
                    if (request != null && currentUserId.equals(request.getReceiverId())) {
                        requestsList.add(request);
                    }
                }
                requestsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireContext(), "Failed to load requests.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleAccept(Request request) {
        if (request.getIsInvite()) {
            addUserToGroup(request, request.getReceiverId());
        } else {
            addUserToGroup(request, request.getRequesterId());
        }

        removeRequest(request);
    }

    private void handleReject(Request request) {
        removeRequest(request);
    }

    private void addUserToGroup(Request request, String userId) {
        DatabaseReference groupRef = FirebaseDatabase.getInstance().getReference("groups").child(request.getGroupId()).child("members");
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("gid");

        groupRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> members = (List<String>) dataSnapshot.getValue();
                if (members == null) {
                    members = new ArrayList<>();
                }
                if (!members.contains(userId)) {
                    members.add(userId);
                    groupRef.setValue(members).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            userRef.setValue(request.getGroupId()).addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    Toast.makeText(requireContext(), "User added to group successfully", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(requireContext(), "Failed to update user's group ID.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Toast.makeText(requireContext(), "Failed to add user to group.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(requireContext(), "Failed to add user to group.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void removeRequest(Request request) {
        DatabaseReference requestRef = FirebaseDatabase.getInstance().getReference("requests").child(request.getRequestId());
        requestRef.removeValue().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Toast.makeText(requireContext(), "Failed to remove request.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
