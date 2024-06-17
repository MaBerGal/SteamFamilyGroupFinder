package com.mi.steamfamilygroupfinder;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mi.steamfamilygroupfinder.adapters.RequestsAdapter;
import com.mi.steamfamilygroupfinder.models.Request;
import com.mi.steamfamilygroupfinder.utility.FirebaseRefs;

import java.util.ArrayList;
import java.util.List;

public class RequestsTabFragment extends Fragment {

    private RecyclerView recyclerViewRequests;
    private RequestsAdapter requestsAdapter;
    private List<Request> requestsList = new ArrayList<>();
    private DatabaseReference requestsReference;
    private MediaPlayer mediaPlayer;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_requests_tab, container, false);

        recyclerViewRequests = view.findViewById(R.id.recyclerViewRequests);
        recyclerViewRequests.setLayoutManager(new LinearLayoutManager(getContext()));

        mediaPlayer = MediaPlayer.create(getContext(), R.raw.yay);

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

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                return makeMovementFlags(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Request request = requestsAdapter.getRequestAtPosition(position);
                handleReject(request);
            }
        });

        itemTouchHelper.attachToRecyclerView(recyclerViewRequests);

        return view;
    }

    private void loadRequests() {
        String currentUserId = FirebaseRefs.getCurrentUser().getUid();
        if (currentUserId != null) {
            requestsReference = FirebaseRefs.getRequestsReference();
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
                    Toast.makeText(requireContext(), R.string.errorLoadRequests, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void handleAccept(Request request) {
        if (request.getIsInvite()) {
            addUserToGroup(request, request.getReceiverId());
        } else {
            addUserToGroup(request, request.getRequesterId());
        }

        removeRequestsForUser(request.getRequesterId());
        removeRequestsForUser(request.getReceiverId());

        playYaySound();
    }

    private void playYaySound() {
        if (mediaPlayer != null) {
            mediaPlayer.start();
        }
    }


    private void removeRequestsForUser(String userId) {
        DatabaseReference requestsRef = FirebaseRefs.getRequestsReference();
        requestsRef.orderByChild("requesterId").equalTo(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot requestSnapshot : dataSnapshot.getChildren()) {
                    Request request = requestSnapshot.getValue(Request.class);
                    if (request != null) {
                        requestsRef.child(request.getRequestId()).removeValue();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(requireContext(), "Failed to remove requests for user.", Toast.LENGTH_SHORT).show();
            }
        });

        requestsRef.orderByChild("receiverId").equalTo(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot requestSnapshot : dataSnapshot.getChildren()) {
                    Request request = requestSnapshot.getValue(Request.class);
                    if (request != null) {
                        requestsRef.child(request.getRequestId()).removeValue();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(requireContext(), "Failed to remove requests for user.", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void handleReject(Request request) {
        removeRequest(request);
    }

    private void addUserToGroup(Request request, String userId) {
        DatabaseReference groupRef = FirebaseRefs.getGroupsReference().child(request.getGroupId()).child("members");
        DatabaseReference userRef = FirebaseRefs.getUsersReference().child(userId).child("gid");

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
                                    Toast.makeText(requireContext(), R.string.addUserToGroupOk, Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(requireContext(), R.string.errorAddUserToGroup, Toast.LENGTH_SHORT).show();
                                    // Toast.makeText(requireContext(), "Failed to update user's group ID.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Toast.makeText(requireContext(), R.string.errorAddUserToGroup, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(requireContext(), R.string.errorAddUserToGroup, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void removeRequest(Request request) {
        DatabaseReference requestRef = FirebaseDatabase.getInstance().getReference("requests").child(request.getRequestId());
        requestRef.removeValue().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Toast.makeText(requireContext(), "Failed to delete request.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

}
