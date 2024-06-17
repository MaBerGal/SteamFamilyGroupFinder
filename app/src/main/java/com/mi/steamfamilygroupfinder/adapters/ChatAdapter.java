package com.mi.steamfamilygroupfinder.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.mi.steamfamilygroupfinder.R;
import com.mi.steamfamilygroupfinder.models.ChatMessage;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;

    private Context context;
    private List<ChatMessage> messageList;
    private String currentUserId;

    public ChatAdapter(Context context, List<ChatMessage> messageList, String currentUserId) {
        this.context = context;
        this.messageList = messageList;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_MESSAGE_SENT) {
            view = LayoutInflater.from(context).inflate(R.layout.item_message_sent, parent, false);
        } else { // VIEW_TYPE_MESSAGE_RECEIVED
            view = LayoutInflater.from(context).inflate(R.layout.item_message_received, parent, false);
        }
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatMessage message = messageList.get(position);
        holder.bind(message);
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage message = messageList.get(position);
        if (message.getSenderId().equals(currentUserId)) {
            return VIEW_TYPE_MESSAGE_SENT;
        } else {
            return VIEW_TYPE_MESSAGE_RECEIVED;
        }
    }

    public class ChatViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewMessage;
        private TextView textViewTimestamp;
        private CardView cardViewMessage; // Added for card background customization

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewMessage = itemView.findViewById(R.id.textViewMessage);
            textViewTimestamp = itemView.findViewById(R.id.textViewTimestamp);
            cardViewMessage = itemView.findViewById(R.id.cardViewMessage);
        }

        public void bind(ChatMessage message) {
            textViewMessage.setText(message.getMessage());

            // Format timestamp to display in the user's timezone
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
            String formattedDate = dateFormat.format(message.getTimestamp());
            textViewTimestamp.setText(formattedDate);

            // Determine if message is sent or received
            if (message.getSenderId().equals(currentUserId)) {
                // Message sent by logged-in user
                cardViewMessage.setCardBackgroundColor(ContextCompat.getColor(context, R.color.colorSentMessage));
                cardViewMessage.setCardElevation(0);
            } else {
                // Message received from another user
                cardViewMessage.setCardBackgroundColor(ContextCompat.getColor(context, R.color.colorReceivedMessage));
                cardViewMessage.setCardElevation(0);
            }
        }
    }
}

