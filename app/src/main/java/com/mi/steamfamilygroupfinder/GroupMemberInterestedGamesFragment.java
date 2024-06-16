package com.mi.steamfamilygroupfinder;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log; // Import the Log class
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GroupMemberInterestedGamesFragment extends Fragment {

    private static final String TAG = "GroupMemberInterestedGamesFragment"; // Define a TAG for logging
    private RecyclerView recyclerView;
    private GamesAdapter adapter;
    private List<Game> gamesList;
    private Set<Integer> gameSids; // Store game sids
    private List<Game> filteredGamesList;
    private DatabaseReference gamesReference;
    private SearchView searchView;
    private String[] memberIds;
    private String memberId;
    private String currentSearchQuery = "";
    private int totalGamesToFetch = 0; // Counter for total games to fetch
    private int gamesFetched = 0; // Counter for games fetched

    public GroupMemberInterestedGamesFragment() {
        // Required empty public constructor
    }

    public static GroupMemberInterestedGamesFragment newInstance(String[] memberIds) {
        GroupMemberInterestedGamesFragment fragment = new GroupMemberInterestedGamesFragment();
        Bundle args = new Bundle();
        args.putStringArray("memberIds", memberIds);
        fragment.setArguments(args);
        return fragment;
    }

    public static GroupMemberInterestedGamesFragment newInstance(String memberId) {
        GroupMemberInterestedGamesFragment fragment = new GroupMemberInterestedGamesFragment();
        Bundle args = new Bundle();
        args.putString("memberId", memberId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_group_member_interested_games, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewGames);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        gamesList = new ArrayList<>();
        gameSids = new HashSet<>(); // Use a set to ensure unique sids
        filteredGamesList = new ArrayList<>();
        adapter = new GamesAdapter(gamesList);
        recyclerView.setAdapter(adapter);
        searchView = view.findViewById(R.id.searchView);

        // Retrieve memberIds or memberId from arguments
        Bundle args = getArguments();
        if (args != null) {
            memberIds = args.getStringArray("memberIds");
            if (memberIds == null) {
                memberId = args.getString("memberId");
            }
        }

        gamesReference = FirebaseDatabase.getInstance("https://steamfamilygroupfinder-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("games");

        if (memberIds != null && memberIds.length > 0) {
            loadGamesFromMultipleMembers();
        } else if (memberId != null) {
            loadGamesFromSingleMember();
        }

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

        return view;
    }

    private void loadGamesFromMultipleMembers() {
        Log.d(TAG, "loadGamesFromMultipleMembers called");
        gamesList.clear();
        gameSids.clear();
        totalGamesToFetch = 0;
        gamesFetched = 0;

        for (String memberId : memberIds) {
            Log.d(TAG, "Processing member ID: " + memberId);
            DatabaseReference userGamesRef = FirebaseDatabase.getInstance("https://steamfamilygroupfinder-default-rtdb.europe-west1.firebasedatabase.app/")
                    .getReference("users").child(memberId).child("gamesInterested");

            userGamesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        for (DataSnapshot sidSnapshot : snapshot.getChildren()) {
                            Integer sid = sidSnapshot.getValue(Integer.class);
                            if (sid != null && gameSids.add(sid)) { // Add sid to set and check if it was added
                                Log.d(TAG, "Adding game sid: " + sid + " for member ID: " + memberId);
                                totalGamesToFetch++;
                                fetchGameDetails(sid);
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getContext(), "Failed to load game sids.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void loadGamesFromSingleMember() {
        Log.d(TAG, "loadGamesFromSingleMember called for member ID: " + memberId);
        gamesList.clear();
        gameSids.clear();
        totalGamesToFetch = 0;
        gamesFetched = 0;

        DatabaseReference userGamesRef = FirebaseDatabase.getInstance("https://steamfamilygroupfinder-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("users").child(memberId).child("gamesInterested");

        userGamesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot sidSnapshot : snapshot.getChildren()) {
                        Integer sid = sidSnapshot.getValue(Integer.class);
                        if (sid != null && gameSids.add(sid)) { // Add sid to set and check if it was added
                            Log.d(TAG, "Adding game sid: " + sid + " for member ID: " + memberId);
                            totalGamesToFetch++;
                            fetchGameDetails(sid);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load game sids.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchGameDetails(Integer sid) {
        Log.d(TAG, "Fetching details for game sid: " + sid);

        gamesReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot gameSnapshot : snapshot.getChildren()) {
                    Game game = gameSnapshot.getValue(Game.class);
                    if (game.getSid() == sid) {
                        Log.d(TAG, "Game details fetched: " + game.getName() + " || SID: " + game.getSid());
                        gamesList.add(game);
                        break;
                    }
                }

                gamesFetched++;
                Log.d(TAG, "Games fetched: " + gamesFetched + " out of " + totalGamesToFetch);
                if (gamesFetched == totalGamesToFetch) {
                    updateAdapterWithFetchedGames();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle database error
                Toast.makeText(getContext(), "Failed to fetch games.", Toast.LENGTH_SHORT).show();
                gamesFetched++;
                Log.d(TAG, "Failed to fetch games. Games fetched: " + gamesFetched + " out of " + totalGamesToFetch);
                if (gamesFetched == totalGamesToFetch) {
                    updateAdapterWithFetchedGames();
                }
            }
        });
    }

    private void updateAdapterWithFetchedGames() {
        Log.d(TAG, "Updating adapter with fetched games");
        filter(currentSearchQuery);  // Reapply current search query after loading games
        adapter.notifyDataSetChanged();
    }

    private void filter(String query) {
        Log.d(TAG, "Filtering games with query: " + query);
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
        adapter.updateGamesList(filteredGamesList);
    }
}
