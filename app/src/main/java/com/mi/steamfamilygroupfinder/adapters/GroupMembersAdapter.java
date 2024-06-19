package com.mi.steamfamilygroupfinder.adapters;

import android.content.Context;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.mi.steamfamilygroupfinder.R;
import com.mi.steamfamilygroupfinder.utility.FirebaseRefs;

import java.util.List;

public class GroupMembersAdapter extends RecyclerView.Adapter<GroupMembersAdapter.MemberViewHolder> {

    private Context context;
    private List<String> memberIds; // List of user IDs

    public GroupMembersAdapter(Context context, List<String> memberIds) {
        this.context = context;
        this.memberIds = memberIds;
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_group_member, parent, false);
        return new MemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        String memberId = memberIds.get(position);
        holder.bind(memberId);
    }

    @Override
    public int getItemCount() {
        return memberIds.size();
    }

    public class MemberViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageViewProfile;
        private ImageView imageViewStar;

        public MemberViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewProfile = itemView.findViewById(R.id.imageViewProfile);
            imageViewStar = itemView.findViewById(R.id.imageViewStar);
        }

        public void bind(String memberId) {
            loadProfilePicture(memberId, imageViewProfile);
            checkIfLeader(memberId, imageViewStar);
        }

        private void loadProfilePicture(String userId, ImageView imageView) {
            DatabaseReference databaseReference = FirebaseRefs.getUsersReference()
                    .child(userId)
                    .child("profilePicture");

            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String base64String = dataSnapshot.getValue(String.class);
                    if (base64String != null && !base64String.isEmpty()) {
                        byte[] imageBytes = Base64.decode(base64String, Base64.DEFAULT);
                        Glide.with(context)
                                .load(imageBytes)
                                .circleCrop()
                                .placeholder(R.drawable.ic_profile_placeholder)
                                .into(imageView);
                    } else {
                        imageView.setImageResource(R.drawable.ic_profile_placeholder);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    imageView.setImageResource(R.drawable.ic_profile_placeholder);
                }
            });
        }

        private void checkIfLeader(String userId, ImageView imageViewCrown) {
            DatabaseReference databaseReference = FirebaseRefs.getUsersReference()
                    .child(userId)
                    .child("groupLeader");

            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Boolean isLeader = dataSnapshot.getValue(Boolean.class);
                    if (isLeader != null && isLeader) {
                        imageViewCrown.setVisibility(View.VISIBLE);
                    } else {
                        imageViewCrown.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    imageViewCrown.setVisibility(View.GONE);
                }
            });
        }
    }
}
