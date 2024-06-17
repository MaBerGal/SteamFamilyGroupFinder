package com.mi.steamfamilygroupfinder.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mi.steamfamilygroupfinder.R;
import com.mi.steamfamilygroupfinder.models.User;

import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder> {

    private List<User> userList;
    private UserSelectionListener selectionListener;

    public UsersAdapter(List<User> userList) {
        this.userList = userList;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public void updateUserList(List<User> newList) {
        userList.clear();
        userList.addAll(newList);
        notifyDataSetChanged();
    }

    public void setUserSelectionListener(UserSelectionListener listener) {
        this.selectionListener = listener;
    }

    public class UserViewHolder extends RecyclerView.ViewHolder {

        private TextView textViewUsername;
        private ImageView imageViewProfile;
        private CheckBox checkBox;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewUsername = itemView.findViewById(R.id.textViewUsername);
            imageViewProfile = itemView.findViewById(R.id.imageViewProfile);
            checkBox = itemView.findViewById(R.id.checkBoxSelectUser);

            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                User user = userList.get(getAdapterPosition());
                if (selectionListener != null) {
                    selectionListener.onUserSelectionChanged(user, isChecked);
                }
            });
        }

        public void bind(User user) {
            textViewUsername.setText(user.getUsername());
            loadProfilePicture(user, imageViewProfile);
        }

        private void loadProfilePicture(User user, ImageView imageViewProfile) {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users").child(user.getUid()).child("profilePicture");
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String base64String = dataSnapshot.getValue(String.class);
                    if (base64String != null && !base64String.isEmpty()) {
                        byte[] imageBytes = Base64.decode(base64String, Base64.DEFAULT);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                        Bitmap circularBitmap = getCircleBitmap(bitmap);
                        imageViewProfile.setImageBitmap(circularBitmap);
                    } else {
                        imageViewProfile.setImageResource(R.drawable.ic_profile_placeholder);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    imageViewProfile.setImageResource(R.drawable.ic_profile_placeholder);
                }
            });
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

    }

    public interface UserSelectionListener {
        void onUserSelectionChanged(User user, boolean isSelected);
    }
}
