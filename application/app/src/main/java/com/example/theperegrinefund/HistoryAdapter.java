package com.example.theperegrinefund;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

/**
 * Adaptateur pour afficher la liste d'historique des messages
 */
public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private List<HistoryItemD> historyItems;
    private OnHistoryItemClickListener clickListener;

    /**
     * Interface pour gérer les clics sur les éléments
     */
    public interface OnHistoryItemClickListener {
        void onHistoryItemClick(HistoryItemD item, int position);
    }

    /**
     * Constructeur de l'adaptateur
     * @param historyItems Liste des éléments d'historique
     * @param clickListener Listener pour les clics
     */
    public HistoryAdapter(List<HistoryItemD> historyItems, OnHistoryItemClickListener clickListener) {
        this.historyItems = historyItems;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        HistoryItemD item = historyItems.get(position);
        holder.bind(item, position);
    }

    @Override
    public int getItemCount() {
        return historyItems.size();
    }

    /**
     * ViewHolder pour les éléments d'historique
     */
    public class HistoryViewHolder extends RecyclerView.ViewHolder {
        private TextView textView;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.history_item_text);
        }

        /**
         * Lie les données à la vue
         * @param item L'élément d'historique
         * @param position La position dans la liste
         */
        public void bind(HistoryItemD item, int position) {
            textView.setText(item.getTitle());

            // Configuration du style selon l'état de sélection
            if (item.isSelected()) {
                textView.setBackgroundResource(R.drawable.history_item_selected);
            } else {
                textView.setBackgroundResource(R.drawable.history_item_normal);
            }

            // Configuration du listener de clic
            itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onHistoryItemClick(item, position);
                }
            });
        }
    }
}