package com.mi.steamfamilygroupfinder;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.BitmapShader;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatViewHolder> {

    private Context context;
    private List<DataSnapshot> chatSnapshots;
    private FirebaseUser currentUser;
    private OnChatItemClickListener onChatItemClickListener;

    public ChatListAdapter(Context context, List<DataSnapshot> chatSnapshots, OnChatItemClickListener onChatItemClickListener) {
        this.context = context;
        this.chatSnapshots = chatSnapshots;
        this.currentUser = FirebaseAuth.getInstance().getCurrentUser();
        this.onChatItemClickListener = onChatItemClickListener;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chat, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        DataSnapshot dataSnapshot = chatSnapshots.get(position);
        holder.bind(dataSnapshot);
    }

    @Override
    public int getItemCount() {
        return chatSnapshots.size();
    }

    public class ChatViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView textViewUsername;
        private TextView textViewLastMessage;
        private TextView textViewTimestamp;
        private ImageView imageViewProfile;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewUsername = itemView.findViewById(R.id.textViewUsername);
            textViewLastMessage = itemView.findViewById(R.id.textViewLastMessage);
            textViewTimestamp = itemView.findViewById(R.id.textViewTimestamp);
            imageViewProfile = itemView.findViewById(R.id.imageViewProfile);

            itemView.setOnClickListener(this);
        }

        public void bind(DataSnapshot dataSnapshot) {
            String chatId = dataSnapshot.getKey();
            if (chatId != null) {
                // Extract userIds from chatId
                String[] userIds = chatId.split("_");
                if (userIds.length == 2) {
                    String userId1 = userIds[0];
                    String userId2 = userIds[1];
                    String otherUserId = (userId1.equals(currentUser.getUid())) ? userId2 : userId1;

                    DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("chats")
                            .child(chatId)
                            .child("messages");

                    chatRef.orderByChild("timestamp").limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                for (DataSnapshot messageSnapshot : snapshot.getChildren()) {
                                    ChatMessage message = messageSnapshot.getValue(ChatMessage.class);
                                    if (message != null) {
                                        fetchOtherUserInfo(message, otherUserId, chatId);
                                    }
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e("eeee", "Failed to fetch last message: " + error.getMessage());
                        }
                    });
                }
            }
        }

        private void fetchOtherUserInfo(ChatMessage message, String otherUserId, String chatId) {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users")
                    .child(otherUserId);

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String username = snapshot.child("username").getValue(String.class);
                        String profileImageUrl = snapshot.child("profilePicture").getValue(String.class);
                        long timestamp = message.getTimestamp();

                        textViewUsername.setText(username);
                        textViewLastMessage.setText(message.getSenderId().equals(currentUser.getUid()) ? "You: " + message.getMessage() : username + ": " + message.getMessage());
                        textViewTimestamp.setText(formatTimestamp(timestamp));
                        loadProfileImage(profileImageUrl);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("eee", "Failed to fetch user info: " + error.getMessage());
                }
            });
        }

        private void loadProfileImage(String base64String) {
            if (base64String != null && !base64String.isEmpty()) {
                byte[] imageBytes = Base64.decode(base64String, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                imageViewProfile.setImageBitmap(getCircleBitmap(bitmap));
            }
        }

        private Bitmap getCircleBitmap(Bitmap bitmap) {
            int size = Math.min(bitmap.getWidth(), bitmap.getHeight());
            Bitmap output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(output);

            final int color = 0xff424242;
            final Paint paint = new Paint();
            final float radius = size / 2f;

            paint.setAntiAlias(true);
            canvas.drawARGB(0, 0, 0, 0);
            paint.setColor(color);
            canvas.drawCircle(radius, radius, radius, paint);

            paint.setXfermode(new android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(bitmap, (size - bitmap.getWidth()) / 2f, (size - bitmap.getHeight()) / 2f, paint);

            return output;
        }

        private String formatTimestamp(long timestamp) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
            return dateFormat.format(new Date(timestamp));
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                DataSnapshot dataSnapshot = chatSnapshots.get(position);
                onChatItemClickListener.onChatItemClick(dataSnapshot.getKey());
            }
        }
    }

    public interface OnChatItemClickListener {
        void onChatItemClick(String chatId);
    }
}
