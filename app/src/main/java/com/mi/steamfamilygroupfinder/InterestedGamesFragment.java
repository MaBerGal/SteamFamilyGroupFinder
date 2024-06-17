package com.mi.steamfamilygroupfinder;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.DragEvent;
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
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.mi.steamfamilygroupfinder.adapters.GamesAdapter;
import com.mi.steamfamilygroupfinder.dialogs.AddGameDialogFragment;
import com.mi.steamfamilygroupfinder.models.Game;
import com.mi.steamfamilygroupfinder.models.User;
import com.mi.steamfamilygroupfinder.utility.FirebaseRefs;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class InterestedGamesFragment extends Fragment {

    private RecyclerView recyclerView;
    private GamesAdapter adapter;
    private List<Game> gamesList;
    private List<Game> filteredGamesList;
    private List<Integer> gamesOwnedSids; // List to store games owned by the user
    private DatabaseReference databaseReference;
    private FloatingActionButton fabDeleteGame;
    private FloatingActionButton fabAddGame;
    private Button buttonBack;
    private SearchView searchView;
    private String currentSearchQuery = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_interested_games, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewGames);
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL, false));
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        }
        gamesList = new ArrayList<>();
        filteredGamesList = new ArrayList<>();
        adapter = new GamesAdapter(gamesList);
        recyclerView.setAdapter(adapter);
        searchView = view.findViewById(R.id.searchView);

        fabAddGame = view.findViewById(R.id.fabAddGame);
        fabAddGame.setOnClickListener(v -> {
            AddGameDialogFragment dialogFragment = AddGameDialogFragment.newInstance();
            dialogFragment.setTargetFragment(InterestedGamesFragment.this, 0);
            dialogFragment.show(getFragmentManager(), "AddGameDialogFragment");
        });

        fabDeleteGame = view.findViewById(R.id.fabDeleteGame);

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        databaseReference = FirebaseRefs.getUsersReference().child(userId).child("gamesInterested");


        DatabaseReference gamesOwnedRef = FirebaseRefs.getUsersReference().child(userId).child("gamesOwned");
        gamesOwnedRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    gamesOwnedSids = new ArrayList<>();
                    for (DataSnapshot gameSnapshot : snapshot.getChildren()) {
                        gamesOwnedSids.add(gameSnapshot.getValue(Integer.class));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), R.string.errorLoadOwnedGames, Toast.LENGTH_SHORT).show();
            }
        });

        loadGames();

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

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
                return makeMovementFlags(dragFlags, 0);
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                adapter.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            }

            @Override
            public void onSelectedChanged(@Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
                super.onSelectedChanged(viewHolder, actionState);
                if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
                    fabDeleteGame.setVisibility(View.VISIBLE);
                    Log.d("DragEvent", "visible");
                } else if (actionState == ItemTouchHelper.ACTION_STATE_IDLE) {
                    fabDeleteGame.setVisibility(View.GONE);
                    Log.d("DragEvent", "gone");
                }
            }

        });

        itemTouchHelper.attachToRecyclerView(recyclerView);

        fabDeleteGame.setOnDragListener((v, event) -> {
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    Log.d("DragEvent", "ACTION_DRAG_STARTED");
                    if (event.getClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
                        v.setVisibility(View.VISIBLE);
                        return true;
                    }
                    return false;
                case DragEvent.ACTION_DRAG_ENTERED:
                    Log.d("DragEvent", "ACTION_DRAG_ENTERED");
                    v.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
                    return true;
                case DragEvent.ACTION_DRAG_EXITED:
                    Log.d("DragEvent", "ACTION_DRAG_EXITED");
                    v.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
                    return true;
                case DragEvent.ACTION_DROP:
                    Log.d("DragEvent", "ACTION_DROP");
                    ClipData.Item item = event.getClipData().getItemAt(0);
                    String gameId = item.getText().toString();
                    return true;
                case DragEvent.ACTION_DRAG_ENDED:
                    Log.d("DragEvent", "ACTION_DRAG_ENDED");
                    v.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
                    return true;
                default:
                    Log.d("DragEvent", "xd");
                    break;
            }
            return false;
        });


        buttonBack = view.findViewById(R.id.buttonBack);
        buttonBack.setOnClickListener(v -> getActivity().onBackPressed());

        return view;
    }

    private void loadGames() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference usersReference = FirebaseRefs.getUsersReference().child(userId);

        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        List<Integer> gamesInterestedSids = user.getGamesInterested();
                        DatabaseReference gamesReference = FirebaseRefs.getGamesReference();

                        gamesReference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                gamesList.clear();
                                if (gamesInterestedSids != null && !gamesInterestedSids.isEmpty()) {
                                    for (DataSnapshot gameSnapshot : snapshot.getChildren()) {
                                        Game game = gameSnapshot.getValue(Game.class);
                                        if (game != null && gamesInterestedSids.contains(game.getSid())) {
                                            gamesList.add(game);
                                        }
                                    }
                                }
                                filteredGamesList.clear();
                                filteredGamesList.addAll(gamesList);
                                filter(currentSearchQuery);  // Reapply current search query after loading games
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(getContext(), R.string.errorLoadOwnedGames, Toast.LENGTH_SHORT).show();
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

    private void deleteGame(int gameSid) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userGamesRef = FirebaseRefs.getUsersReference().child(userId).child("gamesInterested");

        userGamesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot gameSnapshot : dataSnapshot.getChildren()) {
                        int sid = gameSnapshot.getValue(Integer.class);
                        if (sid == gameSid) {
                            gameSnapshot.getRef().removeValue()
                                    .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), R.string.removeGameOk, Toast.LENGTH_SHORT).show())
                                    .addOnFailureListener(e -> Toast.makeText(getContext(), R.string.errorRemoveGame, Toast.LENGTH_SHORT).show());
                            loadGames();
                            return; // Exit the loop once the game is found and removed
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), R.string.errorRemoveGame, Toast.LENGTH_SHORT).show();
            }
        });
    }


    public void addGames(List<Integer> selectedGameSids) {
        if (gamesOwnedSids != null) {
            // Filter out games that are already owned
            selectedGameSids.removeIf(gamesOwnedSids::contains);
        }

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Integer> currentGames = new ArrayList<>();
                if (snapshot.exists()) {
                    for (DataSnapshot gameSnapshot : snapshot.getChildren()) {
                        currentGames.add(gameSnapshot.getValue(Integer.class));
                    }
                }
                currentGames.addAll(selectedGameSids);
                Set<Integer> uniqueGames = new HashSet<>(currentGames); // To avoid duplicates
                databaseReference.setValue(new ArrayList<>(uniqueGames))
                        .addOnSuccessListener(aVoid -> {
                            if (selectedGameSids.size() == 1) {
                                Toast.makeText(getContext(), R.string.addGameOk, Toast.LENGTH_SHORT).show();
                            } else if (selectedGameSids.size() > 1) {
                                Toast.makeText(getContext(), R.string.addGamesOk, Toast.LENGTH_SHORT).show();
                            }
                            loadGames();  // Reload games after addition
                        })
                        .addOnFailureListener(e -> Toast.makeText(getContext(), R.string.errorAddGames, Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), R.string.errorAddGames, Toast.LENGTH_SHORT).show();
                //Toast.makeText(getContext(), "Failed to load current games.", Toast.LENGTH_SHORT).show();
            }
        });
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
        adapter.updateGamesList(filteredGamesList);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Call loadGames method when the fragment is resumed (i.e., when its tab is selected)
        loadGames();
    }
}
