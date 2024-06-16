package com.mi.steamfamilygroupfinder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class RequestsAdapter extends RecyclerView.Adapter<RequestsAdapter.RequestViewHolder> {

    private Context context;
    private List<Request> requests;
    private RequestActionListener actionListener;

    public RequestsAdapter(Context context, List<Request> requests, RequestActionListener actionListener) {
        this.context = context;
        this.requests = requests;
        this.actionListener = actionListener;
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_request, parent, false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        Request request = requests.get(position);
        holder.bind(request);
    }

    public Request getRequestAtPosition(int position) {
        return requests.get(position);
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    public interface RequestActionListener {
        void onAccept(Request request);
        void onReject(Request request);
    }

    public class RequestViewHolder extends RecyclerView.ViewHolder {

        private TextView textViewRequesterName;
        private ImageButton imageButtonAccept;
        private ImageButton imageButtonReject;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewRequesterName = itemView.findViewById(R.id.textViewRequesterName);
            imageButtonAccept = itemView.findViewById(R.id.imageViewAccept);
            imageButtonReject = itemView.findViewById(R.id.imageViewReject);
        }

        public void bind(Request request) {
            // Determine the request type and fetch necessary data
            if (request.getIsInvite()) {
                // Fetch group name from the database
                DatabaseReference groupRef = FirebaseDatabase.getInstance().getReference("groups").child(request.getGroupId());
                groupRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String groupName = dataSnapshot.child("groupName").getValue(String.class);
                        textViewRequesterName.setText("Invited to group: " + groupName);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        textViewRequesterName.setText("Invited to group: [Error fetching group name]");
                    }
                });
            } else {
                // Fetch username from the database
                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(request.getRequesterId());
                userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String username = dataSnapshot.child("username").getValue(String.class);
                        textViewRequesterName.setText(username + " requests to join your group");
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        textViewRequesterName.setText("[Error fetching username] requests to join your group");
                    }
                });
            }

            imageButtonAccept.setOnClickListener(v -> {
                if (actionListener != null) {
                    actionListener.onAccept(request);
                }
            });

            imageButtonReject.setOnClickListener(v -> {
                if (actionListener != null) {
                    actionListener.onReject(request);
                }
            });


        }
    }

}
