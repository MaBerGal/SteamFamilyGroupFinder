package com.mi.steamfamilygroupfinder;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.mi.steamfamilygroupfinder.adapters.ChatAdapter;
import com.mi.steamfamilygroupfinder.models.ChatMessage;
import com.mi.steamfamilygroupfinder.utility.FirebaseRefs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChatFragment extends Fragment {

    private RecyclerView recyclerViewChat;
    private EditText editTextMessage;
    private ImageButton buttonSend;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessages;
    private DatabaseReference chatRef;
    private DatabaseReference userChatIdentifiersRef1;
    private DatabaseReference userChatIdentifiersRef2;
    private FirebaseUser currentUser;
    private String memberId;
    private String chatId;
    private MediaPlayer mediaPlayer;


    public ChatFragment() {
        // Required empty public constructor
    }

    public static ChatFragment newInstance(String memberId, String chatId) {
        ChatFragment fragment = new ChatFragment();
        Bundle args = new Bundle();
        args.putString("memberId", memberId);
        args.putString("chatId", chatId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            memberId = getArguments().getString("memberId");
            chatId = getArguments().getString("chatId");
        }
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        chatMessages = new ArrayList<>();

        mediaPlayer = MediaPlayer.create(requireContext(), R.raw.message);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        recyclerViewChat = view.findViewById(R.id.recyclerViewChat);
        editTextMessage = view.findViewById(R.id.editTextMessage);
        buttonSend = view.findViewById(R.id.buttonSend);

        chatAdapter = new ChatAdapter(getContext(), chatMessages, currentUser.getUid());
        recyclerViewChat.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewChat.setAdapter(chatAdapter);

        // If memberId is provided, generate chatId if not already set
        if (memberId != null && chatId == null) {
            chatId = generateChatId(currentUser.getUid(), memberId);
        }

        // Initialize chatRef and load messages
        chatRef = FirebaseRefs.getChatsReference()
                .child(chatId)
                .child("messages");

        // Reference to the user's chatIdentifiers node
        userChatIdentifiersRef1 = FirebaseRefs.getUsersReference()
                .child(currentUser.getUid())
                .child("chatIdentifiers");

        // Reference to the other user's chatIdentifiers node
        userChatIdentifiersRef2 = FirebaseRefs.getUsersReference()
                .child(memberId)
                .child("chatIdentifiers");

        loadMessages();

        // Add text change listener and send button click listener
        editTextMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                buttonSend.setEnabled(s.length() > 0);
                buttonSend.setColorFilter(getResources().getColor(s.length() > 0 ? android.R.color.holo_blue_light : android.R.color.darker_gray));
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        buttonSend.setOnClickListener(v -> sendMessage());

        return view;
    }

    private void loadMessages() {
        chatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatMessages.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    ChatMessage message = dataSnapshot.getValue(ChatMessage.class);
                    chatMessages.add(message);
                }
                chatAdapter.notifyDataSetChanged();
                recyclerViewChat.scrollToPosition(chatMessages.size() - 1);
                playMessageSound();
                //Log.d("ChatFragment", "Loaded messages: " + chatMessages.size());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //Log.e("ChatFragment", "Failed to load messages", error.toException());
            }
        });
    }

    private void playMessageSound() {
        if (mediaPlayer != null) {
            mediaPlayer.start();
        }
    }


    private void sendMessage() {
        String messageText = editTextMessage.getText().toString().trim();
        if (messageText.isEmpty()) {
            return;
        }

        long timestamp = System.currentTimeMillis();
        ChatMessage message = new ChatMessage(messageText, currentUser.getUid(), timestamp);

        // Push the new message to the chatRef
        chatRef.push().setValue(message).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                editTextMessage.setText("");
                recyclerViewChat.scrollToPosition(chatMessages.size() - 1);

                // Add chatId to user's chatIdentifiers list
                addChatIdentifierToUser();
            } else {
                Toast.makeText(getContext(), R.string.errorSendMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Helper method to generate a chatId based on two user IDs
    private String generateChatId(String userId1, String userId2) {
        List<String> userIds = new ArrayList<>();
        userIds.add(userId1);
        userIds.add(userId2);
        Collections.sort(userIds); // Sort to maintain consistency
        return userIds.get(0) + "_" + userIds.get(1); // Example format: userId1_userId2
    }

    // Method to add chatId to user's chatIdentifiers list for two different references
    private void addChatIdentifierToUser() {
        // Listener for userChatIdentifiersRef1
        userChatIdentifiersRef1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> chatIdentifiers = new ArrayList<>();
                if (snapshot.exists()) {
                    chatIdentifiers = (List<String>) snapshot.getValue();
                }
                if (!chatIdentifiers.contains(chatId)) {
                    chatIdentifiers.add(chatId);
                    userChatIdentifiersRef1.setValue(chatIdentifiers)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    //Log.d("ChatFragment", "ChatId added to user's chatIdentifiers (Ref1)");
                                } else {
                                    //Log.e("ChatFragment", "Failed to add chatId to user's chatIdentifiers (Ref1)", task.getException());
                                }
                            });
                } else {
                   //Log.d("ChatFragment", "ChatId already exists in user's chatIdentifiers (Ref1)");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //Log.e("ChatFragment", "Database error: " + error.getMessage(), error.toException());
            }
        });

        // Listener for userChatIdentifiersRef2
        userChatIdentifiersRef2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> chatIdentifiers = new ArrayList<>();
                if (snapshot.exists()) {
                    chatIdentifiers = (List<String>) snapshot.getValue();
                }
                if (!chatIdentifiers.contains(chatId)) {
                    chatIdentifiers.add(chatId);
                    userChatIdentifiersRef2.setValue(chatIdentifiers)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    //Log.d("ChatFragment", "ChatId added to user's chatIdentifiers (Ref2)");
                                } else {
                                    //Log.e("ChatFragment", "Failed to add chatId to user's chatIdentifiers (Ref2)", task.getException());
                                }
                            });
                } else {
                    //Log.d("ChatFragment", "ChatId already exists in user's chatIdentifiers (Ref2)");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //Log.e("ChatFragment", "Database error: " + error.getMessage(), error.toException());
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
