package com.mi.steamfamilygroupfinder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class GameSelectionAdapter extends RecyclerView.Adapter<GameSelectionAdapter.GameViewHolder> {

    private List<Game> gamesList;

    public GameSelectionAdapter(List<Game> gamesList) {
        this.gamesList = gamesList;
    }

    @NonNull
    @Override
    public GameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_game_selection, parent, false);
        return new GameViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GameViewHolder holder, int position) {
        Game game = gamesList.get(position);
        holder.bind(game);
    }

    @Override
    public int getItemCount() {
        return gamesList.size();
    }

    public class GameViewHolder extends RecyclerView.ViewHolder {

        private TextView textViewGameName;
        private CheckBox checkBoxSelected;

        public GameViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewGameName = itemView.findViewById(R.id.textViewGameName);
            checkBoxSelected = itemView.findViewById(R.id.checkBoxSelected);

            checkBoxSelected.setOnCheckedChangeListener((buttonView, isChecked) -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Game game = gamesList.get(position);
                    game.setSelected(isChecked);
                }
            });
        }

        public void bind(Game game) {
            textViewGameName.setText(game.getName());
            checkBoxSelected.setChecked(game.isSelected());
        }
    }
}
