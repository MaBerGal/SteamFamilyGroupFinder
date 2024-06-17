package com.mi.steamfamilygroupfinder.dialogs;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.mi.steamfamilygroupfinder.InterestedGamesFragment;
import com.mi.steamfamilygroupfinder.OwnedGamesFragment;
import com.mi.steamfamilygroupfinder.R;
import com.mi.steamfamilygroupfinder.adapters.GameSelectionAdapter;
import com.mi.steamfamilygroupfinder.models.Game;
import com.mi.steamfamilygroupfinder.models.User;
import com.mi.steamfamilygroupfinder.utility.FirebaseRefs;

import java.util.ArrayList;
import java.util.List;

public class AddGameDialogFragment extends DialogFragment {

    private RecyclerView recyclerView;
    private GameSelectionAdapter adapter;
    private List<Game> gamesList;
    private List<Game> filteredGamesList;
    private List<Integer> selectedGames;
    private List<Integer> gamesOwnedSids;
    private List<Integer> interestedGamesSids;
    private DatabaseReference databaseReference;
    private SearchView searchView;
    private String currentSearchQuery = "";

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
        searchView = view.findViewById(R.id.searchView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        gamesList = new ArrayList<>();
        filteredGamesList = new ArrayList<>();
        selectedGames = new ArrayList<>();
        adapter = new GameSelectionAdapter(filteredGamesList);
        recyclerView.setAdapter(adapter);

        // Retrieve user's gamesOwned and interestedGames
        retrieveUserGames();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                currentSearchQuery = newText;
                filter(newText);
                return true;
            }
        });

        Button buttonConfirm = view.findViewById(R.id.buttonConfirm);
        buttonConfirm.setOnClickListener(v -> {
            for (Game game : gamesList) {
                if (game.isSelected()) {
                    selectedGames.add(game.getSid());
                }
            }

            // Determine where the fragment originated from
            if (getTargetFragment() instanceof OwnedGamesFragment) {
                ((OwnedGamesFragment) getTargetFragment()).addGames(selectedGames);
            } else if (getTargetFragment() instanceof InterestedGamesFragment) {
                ((InterestedGamesFragment) getTargetFragment()).addGames(selectedGames);
            }

            dismiss();
        });

        return view;
    }

    private void retrieveUserGames() {
        String userId = FirebaseRefs.getCurrentUser().getUid();
        DatabaseReference usersReference = FirebaseRefs.getUsersReference().child(userId);

        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        gamesOwnedSids = user.getGamesOwned();
                        interestedGamesSids = user.getGamesInterested();

                        DatabaseReference gamesReference = FirebaseRefs.getGamesReference();

                        gamesReference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                gamesList.clear();
                                for (DataSnapshot gameSnapshot : snapshot.getChildren()) {
                                    Game game = gameSnapshot.getValue(Game.class);
                                    if (game != null) {
                                        boolean canSelect = true;

                                        if (getTargetFragment() instanceof InterestedGamesFragment) {
                                            if (gamesOwnedSids != null && gamesOwnedSids.contains(game.getSid())) {
                                                canSelect = false;
                                            }
                                            if (interestedGamesSids != null && interestedGamesSids.contains(game.getSid())) {
                                                canSelect = false;
                                            }
                                        } else if (getTargetFragment() instanceof OwnedGamesFragment) {
                                            if (gamesOwnedSids != null && gamesOwnedSids.contains(game.getSid())) {
                                                canSelect = false;
                                            }
                                            if (interestedGamesSids != null && interestedGamesSids.contains(game.getSid())) {
                                                canSelect = true;
                                            }
                                        }

                                        if (canSelect) {
                                            gamesList.add(game);
                                        }
                                    }
                                }
                                filter(currentSearchQuery);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                //Log.e("AddGameDialogFragment", "Failed to load games", error.toException());
                            }
                        });
                    } else {
                        // User profile is null
                        Toast.makeText(getContext(), R.string.errorLoadUserProfile, Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), R.string.errorLoadUserProfile, Toast.LENGTH_SHORT).show();
            }
        };

        usersReference.addListenerForSingleValueEvent(valueEventListener);
    }

    private void filter(String query) {
        filteredGamesList.clear();
        if (TextUtils.isEmpty(query)) {
            filteredGamesList.addAll(gamesList);
        } else {
            String userInput = query.toLowerCase().trim();
            for (Game game : gamesList) {
                if (game.getName().toLowerCase().contains(userInput)) {
                    filteredGamesList.add(game);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }
}
