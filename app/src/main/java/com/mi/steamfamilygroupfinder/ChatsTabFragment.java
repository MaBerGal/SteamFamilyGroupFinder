package com.mi.steamfamilygroupfinder;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ChatsTabFragment extends Fragment implements ChatListAdapter.OnChatItemClickListener {

    private RecyclerView recyclerViewChats;
    private ChatListAdapter chatListAdapter;
    private List<DataSnapshot> chatSnapshots;
    private DatabaseReference userChatsRef;
    private FirebaseUser currentUser;

    public ChatsTabFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        chatSnapshots = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chats, container, false);

        recyclerViewChats = view.findViewById(R.id.recyclerViewChats);
        recyclerViewChats.setLayoutManager(new LinearLayoutManager(getContext()));

        chatListAdapter = new ChatListAdapter(getContext(), chatSnapshots, this);
        recyclerViewChats.setAdapter(chatListAdapter);

        userChatsRef = FirebaseDatabase.getInstance().getReference("users")
                .child(currentUser.getUid()).child("chatIdentifiers");

        loadChats();

        return view;
    }

    private void loadChats() {
        DatabaseReference chatIdentifiersRef = FirebaseDatabase.getInstance().getReference("users")
                .child(currentUser.getUid()).child("chatIdentifiers");

        chatIdentifiersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatSnapshots.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String chatIdentifier = dataSnapshot.getValue(String.class);
                    if (chatIdentifier != null) {
                        Log.d("ChatsTabFragment", "Chat Identifier: " + chatIdentifier);
                        DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("chats").child(chatIdentifier);
                        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                chatSnapshots.add(dataSnapshot);
                                chatListAdapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Log.e("ChatsTabFragment", "Failed to load chat: " + databaseError.getMessage());
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load chat identifiers: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onChatItemClick(String chatId) {
        String memberId = getMemberIdFromChatId(chatId);
        ChatFragment chatFragment = ChatFragment.newInstance(memberId, chatId);
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.bottomFragmentContainer, chatFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private String getMemberIdFromChatId(String chatId) {
        String[] userIds = chatId.split("_");

        if (userIds.length != 2) {
            Log.e("ChatFragment", "Invalid chatId format: " + chatId);
            return null; // Handle invalid chatId format gracefully
        }

        String userId1 = userIds[0];
        String userId2 = userIds[1];
        String currentUserId = currentUser.getUid();

        // Determine which userId is the memberId
        if (userId1.equals(currentUserId)) {
            return userId2;
        } else {
            return userId1;
        }
    }

}
