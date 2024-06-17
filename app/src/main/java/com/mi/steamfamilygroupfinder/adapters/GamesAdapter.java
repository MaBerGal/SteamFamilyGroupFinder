package com.mi.steamfamilygroupfinder.adapters;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.mi.steamfamilygroupfinder.R;
import com.mi.steamfamilygroupfinder.models.Game;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GamesAdapter extends RecyclerView.Adapter<GamesAdapter.GameViewHolder> {

    private final List<Game> gamesList;

    public GamesAdapter(List<Game> gamesList) {
        this.gamesList = new ArrayList<>(gamesList);
    }

    @NonNull
    @Override
    public GameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_game, parent, false);
        return new GameViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GameViewHolder holder, int position) {
        Game game = gamesList.get(position);
        holder.bind(game);

        holder.itemView.setOnLongClickListener(v -> {
            ClipData.Item item = new ClipData.Item(String.valueOf(game.getSid()));
            ClipData dragData = new ClipData(String.valueOf(game.getSid()), new String[]{ClipDescription.MIMETYPE_TEXT_PLAIN}, item);
            View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
            v.startDragAndDrop(dragData, shadowBuilder, v, 0);
            return true;
        });

        holder.itemView.setOnClickListener(v -> {
            showGameDetailsDialog(holder.itemView.getContext(), game);
        });
    }

    @Override
    public int getItemCount() {
        return gamesList.size();
    }

    public void updateGamesList(List<Game> newGamesList) {
        gamesList.clear();
        gamesList.addAll(newGamesList);
        notifyDataSetChanged();
    }

    public void onItemMove(int fromPosition, int toPosition) {
        Collections.swap(gamesList, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
    }

    public static class GameViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;

        public GameViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageViewGame);
        }

        public void bind(Game game) {
            Picasso.get().load(game.getImage()).into(imageView);
        }
    }

    private void showGameDetailsDialog(Context context, Game game) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_game_details, null);
        builder.setView(view);

        // String storeUrl2 = game.getStore_url();
        //Log.d("GameDetails", "Store URL: " + storeUrl2);
        //Log.d("GameDetails", "Game name " + game.getName());
        ImageView imageViewGameDetail = view.findViewById(R.id.imageViewGameDetail);
        TextView textViewGameTitle = view.findViewById(R.id.textViewGameTitle);
        TextView textViewGameDescription = view.findViewById(R.id.textViewGameDescription);
        TextView textViewGameGenres = view.findViewById(R.id.textViewGameGenres);
        TextView textViewGameDevelopers = view.findViewById(R.id.textViewGameDevelopers);
        TextView textViewGameLanguages = view.findViewById(R.id.textViewGameLanguages);
        Button buttonViewInSteam = view.findViewById(R.id.buttonViewInSteam);

        Picasso.get().load(game.getImage()).into(imageViewGameDetail);
        textViewGameTitle.setText(game.getName());
        textViewGameDescription.setText(game.getDescription());
        textViewGameGenres.setText(game.getGenres());
        textViewGameDevelopers.setText(game.getDevelopers());
        textViewGameLanguages.setText(game.getLanguages());

        buttonViewInSteam.setOnClickListener(v -> {
            String storeUrl = game.getStore_url();
            if (storeUrl != null && !storeUrl.isEmpty()) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(storeUrl));
                context.startActivity(browserIntent);
            } else {
                Toast.makeText(context, "Store URL is not available", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setPositiveButton(android.R.string.ok, (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

}

