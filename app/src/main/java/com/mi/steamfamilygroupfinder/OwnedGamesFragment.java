package com.mi.steamfamilygroupfinder;

import android.content.ClipData;
import android.content.ClipDescription;
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
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class OwnedGamesFragment extends Fragment {

    private RecyclerView recyclerView;
    private GamesAdapter adapter;
    private List<Game> gamesList;
    private List<Game> filteredGamesList;
    private DatabaseReference databaseReference;
    private FloatingActionButton fabDeleteGame;
    private FloatingActionButton fabAddGame;
    private Button buttonBack;
    private SearchView searchView;
    private String currentSearchQuery = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_owned_games, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewGames);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        gamesList = new ArrayList<>();
        filteredGamesList = new ArrayList<>();
        adapter = new GamesAdapter(gamesList);
        recyclerView.setAdapter(adapter);
        searchView = view.findViewById(R.id.searchView);

        fabAddGame = view.findViewById(R.id.fabAddGame);
        fabAddGame.setOnClickListener(v -> {
            AddGameDialogFragment dialogFragment = AddGameDialogFragment.newInstance();
            dialogFragment.setTargetFragment(OwnedGamesFragment.this, 0);
            dialogFragment.show(getFragmentManager(), "AddGameDialogFragment");
        });

        fabDeleteGame = view.findViewById(R.id.fabDeleteGame);

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance("https://steamfamilygroupfinder-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("users").child(userId).child("gamesOwned");

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
        DatabaseReference usersReference = FirebaseDatabase.getInstance("https://steamfamilygroupfinder-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("users").child(userId);

        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    UserProfile userProfile = snapshot.getValue(UserProfile.class);
                    if (userProfile != null) {
                        List<Integer> gamesOwnedSids = userProfile.getGamesOwned();
                        DatabaseReference gamesReference = FirebaseDatabase.getInstance("https://steamfamilygroupfinder-default-rtdb.europe-west1.firebasedatabase.app/")
                                .getReference("games");

                        gamesReference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                gamesList.clear();
                                if (gamesOwnedSids != null && !gamesOwnedSids.isEmpty()) {
                                    for (DataSnapshot gameSnapshot : snapshot.getChildren()) {
                                        Game game = gameSnapshot.getValue(Game.class);
                                        if (game != null && gamesOwnedSids.contains(game.getSid())) {
                                            gamesList.add(game);
                                        }
                                    }
                                } else {
                                    // No games owned
                                    Toast.makeText(getContext(), "You have no games owned.", Toast.LENGTH_SHORT).show();
                                }
                                filteredGamesList.clear();
                                filteredGamesList.addAll(gamesList);
                                filter(currentSearchQuery);  // Reapply current search query after loading games
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(getContext(), "Failed to load games.", Toast.LENGTH_SHORT).show();
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

    private void deleteGame(int gameSid) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userGamesRef = FirebaseDatabase.getInstance("https://steamfamilygroupfinder-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("users").child(userId).child("gamesOwned");

        userGamesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot gameSnapshot : dataSnapshot.getChildren()) {
                        int sid = gameSnapshot.getValue(Integer.class);
                        if (sid == gameSid) {
                            gameSnapshot.getRef().removeValue()
                                    .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Game removed successfully.", Toast.LENGTH_SHORT).show())
                                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to remove game.", Toast.LENGTH_SHORT).show());
                            loadGames();
                            return; // Exit the loop once the game is found and removed
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to remove game.", Toast.LENGTH_SHORT).show();
            }
        });
    }


    public void addGames(List<Integer> selectedGameSids) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userGamesInterestedRef = FirebaseDatabase.getInstance("https://steamfamilygroupfinder-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("users").child(userId).child("gamesInterested");

        userGamesInterestedRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Remove the selected games from the gamesInterested list
                    for (DataSnapshot gameSnapshot : dataSnapshot.getChildren()) {
                        Integer sid = gameSnapshot.getValue(Integer.class);
                        if (selectedGameSids.contains(sid)) {
                            gameSnapshot.getRef().removeValue();
                        }
                    }
                }

                // Add the selected games to the gamesOwned list
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
                                    if (selectedGameSids.size() > 0) {
                                        Toast.makeText(getContext(), "Games added successfully.", Toast.LENGTH_SHORT).show();
                                    }
                                    loadGames();  // Reload games after addition
                                })
                                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to add games.", Toast.LENGTH_SHORT).show());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), "Failed to load current games.", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to remove game from interested list.", Toast.LENGTH_SHORT).show();
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
}
