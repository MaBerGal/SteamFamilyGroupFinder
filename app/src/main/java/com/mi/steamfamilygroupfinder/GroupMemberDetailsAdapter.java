// GroupMembersModalAdapter.java
package com.mi.steamfamilygroupfinder;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.List;

public class GroupMemberDetailsAdapter extends RecyclerView.Adapter<GroupMemberDetailsAdapter.MemberViewHolder> {

    private Context context;
    private List<String> memberIds;
    private Dialog dialog;

    public GroupMemberDetailsAdapter(Context context, List<String> memberIds, Dialog dialog) {
        this.context = context;
        this.memberIds = memberIds;
        this.dialog = dialog;
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_group_member_details, parent, false);
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
        private TextView textViewUsername;
        private ImageButton imageButtonLibrary;
        private ImageButton imageButtonMessage;
        private ImageView imageViewStar; // Added for leader indicator
        private CardView cardViewDetails;

        public MemberViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewProfile = itemView.findViewById(R.id.imageViewProfile);
            textViewUsername = itemView.findViewById(R.id.textViewUsername);
            imageButtonLibrary = itemView.findViewById(R.id.imageButtonLibrary);
            imageButtonMessage = itemView.findViewById(R.id.imageButtonMessage);
            imageViewStar = itemView.findViewById(R.id.imageViewStar);
            cardViewDetails = itemView.findViewById(R.id.cardViewDetails);
        }

        public void bind(String memberId) {
            loadProfilePicture(memberId, imageViewProfile);
            loadUsername(memberId, textViewUsername);
            checkIfLeader(memberId);

            if (memberId.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                imageButtonLibrary.setVisibility(View.GONE);
                imageButtonMessage.setVisibility(View.GONE);
                cardViewDetails.setBackgroundColor(Color.parseColor("#D1F6A5"));
            } else {
                // Set click listeners for buttons only if not the logged-in user
                imageButtonLibrary.setOnClickListener(v -> {
                    openGroupMemberLibraryFragment(memberId);
                });

                imageButtonMessage.setOnClickListener(v -> openChatFragment(memberId));

            }
        }

        private void loadProfilePicture(String userId, ImageView imageView) {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users")
                    .child(userId)
                    .child("profilePicture");

            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String base64String = dataSnapshot.getValue(String.class);
                    if (base64String != null && !base64String.isEmpty()) {
                        // Convert base64 string to byte array and load using Glide
                        byte[] imageBytes = android.util.Base64.decode(base64String, android.util.Base64.DEFAULT);
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

        private void loadUsername(String userId, TextView textView) {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users")
                    .child(userId)
                    .child("username");

            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String username = dataSnapshot.getValue(String.class);
                    if (username != null && !username.isEmpty()) {
                        textView.setText(username);
                    } else {
                        textView.setText("Unknown");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    textView.setText("Unknown");
                }
            });
        }

        private void checkIfLeader(String userId) {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users")
                    .child(userId)
                    .child("isGroupLeader");

            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Boolean isLeader = dataSnapshot.getValue(Boolean.class);
                    if (isLeader != null && isLeader) {
                        imageViewStar.setVisibility(View.VISIBLE); // Show star if leader
                    } else {
                        imageViewStar.setVisibility(View.GONE); // Hide star if not leader
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    imageViewStar.setVisibility(View.GONE); // Handle error case
                }
            });
        }

        private void openGroupMemberLibraryFragment(String memberId) {
            FragmentManager fragmentManager = ((AppCompatActivity) context).getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            GroupMemberLibraryFragment fragment = new GroupMemberLibraryFragment();
            // Pass memberId or any necessary data to the fragment using arguments
            Bundle args = new Bundle();
            args.putString("memberId", memberId);
            fragment.setArguments(args);
            fragmentTransaction.replace(R.id.bottomFragmentContainer, fragment);
            fragmentTransaction.addToBackStack(null); // Optional, if you want to add to back stack
            fragmentTransaction.commit();
            dialog.dismiss();
        }

        private void openChatFragment(String memberId) {
            FragmentManager fragmentManager = ((AppCompatActivity) context).getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            ChatFragment fragment = ChatFragment.newInstance(memberId, null);
            Bundle args = new Bundle();
            args.putString("memberId", memberId);
            fragment.setArguments(args);
            fragmentTransaction.replace(R.id.bottomFragmentContainer, fragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
            dialog.dismiss();
        }
    }
}
