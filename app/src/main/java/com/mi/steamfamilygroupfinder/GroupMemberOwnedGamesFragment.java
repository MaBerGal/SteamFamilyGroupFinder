package com.mi.steamfamilygroupfinder;

import android.content.res.Configuration;
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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.mi.steamfamilygroupfinder.adapters.GamesAdapter;
import com.mi.steamfamilygroupfinder.models.Game;
import com.mi.steamfamilygroupfinder.utility.FirebaseRefs;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GroupMemberOwnedGamesFragment extends Fragment {
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
    private Button buttonBack;
    private int totalGamesToFetch = 0; // Counter for total games to fetch
    private int gamesFetched = 0; // Counter for games fetched


    public GroupMemberOwnedGamesFragment() {
        // Required empty public constructor
    }

    public static GroupMemberOwnedGamesFragment newInstance(String[] memberIds) {
        GroupMemberOwnedGamesFragment fragment = new GroupMemberOwnedGamesFragment();
        Bundle args = new Bundle();
        args.putStringArray("memberIds", memberIds);
        fragment.setArguments(args);
        return fragment;
    }

    public static GroupMemberOwnedGamesFragment newInstance(String memberId) {
        GroupMemberOwnedGamesFragment fragment = new GroupMemberOwnedGamesFragment();
        Bundle args = new Bundle();
        args.putString("memberId", memberId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_group_member_owned_games, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewGames);
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL, false));
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        }
        gamesList = new ArrayList<>();
        gameSids = new HashSet<>(); // Use a set to ensure unique sids
        filteredGamesList = new ArrayList<>();
        adapter = new GamesAdapter(gamesList);
        recyclerView.setAdapter(adapter);
        searchView = view.findViewById(R.id.searchView);
        buttonBack = view.findViewById(R.id.buttonBack);

        // Retrieve memberIds or memberId from arguments
        Bundle args = getArguments();
        if (args != null) {
            memberIds = args.getStringArray("memberIds");
            if (memberIds == null) {
                memberId = args.getString("memberId");
            }
        }

        gamesReference = FirebaseRefs.getGamesReference();

        if (memberIds != null && memberIds.length > 0) {
            loadGamesFromMultipleMembers();
        } else if (memberId != null) {
            loadGamesFromSingleMember();
        }

        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

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
        //Log.d("GroupMemberOwnedGames", "loadGamesFromMultipleMembers called");
        gamesList.clear();
        gameSids.clear();
        totalGamesToFetch = 0;
        gamesFetched = 0;

        for (String memberId : memberIds) {
            //Log.d("GroupMemberOwnedGames", "Processing member ID: " + memberId);
            DatabaseReference userGamesRef = FirebaseRefs.getUsersReference().child(memberId).child("gamesOwned");

            userGamesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        for (DataSnapshot sidSnapshot : snapshot.getChildren()) {
                            Integer sid = sidSnapshot.getValue(Integer.class);
                            if (sid != null && gameSids.add(sid)) { // Add sid to set and check if it was added
                                //Log.d("GroupMemberOwnedGames", "Adding game sid: " + sid + " for member ID: " + memberId);
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
        //Log.d("GroupMemberOwnedGames", "loadGamesFromSingleMember called for member ID: " + memberId);
        gamesList.clear();
        gameSids.clear();
        totalGamesToFetch = 0;
        gamesFetched = 0;

        DatabaseReference userGamesRef = FirebaseRefs.getUsersReference().child(memberId).child("gamesOwned");

        userGamesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot sidSnapshot : snapshot.getChildren()) {
                        Integer sid = sidSnapshot.getValue(Integer.class);
                        if (sid != null && gameSids.add(sid)) { // Add sid to set and check if it was added
                            //Log.d("GroupMemberOwnedGames", "Adding game sid: " + sid + " for member ID: " + memberId);
                            totalGamesToFetch++;
                            fetchGameDetails(sid);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), R.string.errorLoadGames, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchGameDetails(Integer sid) {
        // Log.d("GroupMemberOwnedGames", "Fetching details for game sid: " + sid);
        gamesReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot gameSnapshot : snapshot.getChildren()) {
                    Game game = gameSnapshot.getValue(Game.class);
                    if (game.getSid() == sid) {
                        //Log.d("GroupMemberOwnedGames", "Game details fetched: " + game.getName() + " || SID: " + game.getSid());
                        gamesList.add(game);
                        break;
                    }
                }

                gamesFetched++;
                //Log.d("GroupMemberOwnedGames", "Games fetched: " + gamesFetched + " out of " + totalGamesToFetch);
                if (gamesFetched == totalGamesToFetch) {
                    updateAdapterWithFetchedGames();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle database error
                Toast.makeText(getContext(), R.string.errorLoadGames, Toast.LENGTH_SHORT).show();
                gamesFetched++;
                //Log.d("GroupMemberOwnedGames", "Failed to fetch games. Games fetched: " + gamesFetched + " out of " + totalGamesToFetch);
                if (gamesFetched == totalGamesToFetch) {
                    updateAdapterWithFetchedGames();
                }
            }
        });
    }

    private void updateAdapterWithFetchedGames() {
        //Log.d("GroupMemberOwnedGames", "Updating adapter with fetched games");
        filter(currentSearchQuery);  // Reapply current search query after loading games
        adapter.notifyDataSetChanged();
    }

    private void filter(String query) {
        //Log.d("GroupMemberOwnedGames", "Filtering games with query: " + query);
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
