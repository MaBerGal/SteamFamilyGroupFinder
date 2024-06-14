package com.mi.steamfamilygroupfinder;

import android.content.ClipData;
import android.content.ClipDescription;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
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
}
