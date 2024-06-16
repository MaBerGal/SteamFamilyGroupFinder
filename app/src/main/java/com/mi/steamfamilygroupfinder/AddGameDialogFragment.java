package com.mi.steamfamilygroupfinder;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
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

public class AddGameDialogFragment extends DialogFragment {

    private RecyclerView recyclerView;
    private GameSelectionAdapter adapter;
    private List<Game> gamesList;
    private List<Integer> selectedGames;
    private List<Integer> gamesOwnedSids; // List to store games owned by the user
    private List<Integer> interestedGamesSids; // List to store games user is interested in

    private DatabaseReference databaseReference;

    public AddGameDialogFragment() {
        // Required empty public constructor
    }

    public static AddGameDialogFragment newInstance() {
        return new AddGameDialogFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_add_game, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewGamesList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        gamesList = new ArrayList<>();
        selectedGames = new ArrayList<>();
        adapter = new GameSelectionAdapter(gamesList);
        recyclerView.setAdapter(adapter);

        // Retrieve user's gamesOwned and interestedGames
        retrieveUserGames();

        Button buttonConfirm = view.findViewById(R.id.buttonConfirm);
        buttonConfirm.setOnClickListener(v -> {
            for (Game game : gamesList) {
                if (game.isSelected()) {
                    selectedGames.add(game.getSid());
                }
            }

            // Determine where the fragment originated from
            if (getTargetFragment() instanceof OwnedGamesFragment) {
                // Add selected games to OwnedGamesFragment
                ((OwnedGamesFragment) getTargetFragment()).addGames(selectedGames);
            } else if (getTargetFragment() instanceof InterestedGamesFragment) {
                // Add selected games to InterestedGamesFragment
                ((InterestedGamesFragment) getTargetFragment()).addGames(selectedGames);
            }

            dismiss();
        });

        return view;
    }

    private void retrieveUserGames() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference usersReference = FirebaseDatabase.getInstance("https://steamfamilygroupfinder-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("users").child(userId);

        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    UserProfile userProfile = snapshot.getValue(UserProfile.class);
                    if (userProfile != null) {
                        gamesOwnedSids = userProfile.getGamesOwned(); // Get the list of games owned by the user
                        interestedGamesSids = userProfile.getGamesInterested(); // Get the list of games user is interested in

                        DatabaseReference gamesReference = FirebaseDatabase.getInstance("https://steamfamilygroupfinder-default-rtdb.europe-west1.firebasedatabase.app/")
                                .getReference("games");

                        gamesReference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                gamesList.clear();
                                for (DataSnapshot gameSnapshot : snapshot.getChildren()) {
                                    Game game = gameSnapshot.getValue(Game.class);
                                    if (game != null) {
                                        boolean canSelect = true;

                                        // Check if coming from InterestedGamesFragment and game is already owned
                                        if (getTargetFragment() instanceof InterestedGamesFragment) {
                                            if (gamesOwnedSids != null && gamesOwnedSids.contains(game.getSid())) {
                                                canSelect = false;
                                            }
                                            // Check if game is in interestedGames, don't add to list
                                            if (interestedGamesSids != null && interestedGamesSids.contains(game.getSid())) {
                                                canSelect = false;
                                            }
                                        }

                                        // Always allow selection if coming from OwnedGamesFragment
                                        // This ensures games from InterestedGamesFragment can be added
                                        else if (getTargetFragment() instanceof OwnedGamesFragment) {
                                            // Check if game is in interestedGames, but skip the check
                                            // to allow selection from InterestedGamesFragment
                                        }

                                        if (canSelect) {
                                            gamesList.add(game);
                                        }
                                    }
                                }
                                adapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.e("AddGameDialogFragment", "Failed to load games", error.toException());
                            }
                        });
                    } else {
                        // User profile is null
                        Toast.makeText(getContext(), "Failed to load user profile.", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load user profile.", Toast.LENGTH_SHORT).show();
            }
        };

        usersReference.addListenerForSingleValueEvent(valueEventListener);
    }
}

