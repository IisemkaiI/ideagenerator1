package com.example.ideagenerator.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ideagenerator.R;
import com.example.ideagenerator.model.Idea;

import java.util.ArrayList;
import java.util.List;

public class IdeaAdapter extends RecyclerView.Adapter<IdeaAdapter.IdeaViewHolder> {

    private List<Idea> ideas = new ArrayList<>();
    private OnIdeaClickListener clickListener;
    private OnFavoriteClickListener favoriteListener;

    public interface OnIdeaClickListener { void onIdeaClick(Idea idea); }
    public interface OnFavoriteClickListener { void onFavoriteClick(Idea idea); }

    public void setOnIdeaClickListener(OnIdeaClickListener l) { this.clickListener = l; }
    public void setOnFavoriteClickListener(OnFavoriteClickListener l) { this.favoriteListener = l; }

    public void setIdeas(List<Idea> newIdeas) {
        this.ideas = newIdeas;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public IdeaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_idea, parent, false);
        return new IdeaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IdeaViewHolder holder, int position) {
        holder.bind(ideas.get(position));
    }

    @Override
    public int getItemCount() { return ideas.size(); }

    class IdeaViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTitle, tvShort, tvCategory, tvDifficulty, tvTime;
        private final ImageView ivFavorite;

        IdeaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle      = itemView.findViewById(R.id.tvIdeaTitle);
            tvShort      = itemView.findViewById(R.id.tvIdeaShort);
            tvCategory   = itemView.findViewById(R.id.tvIdeaCategory);
            tvDifficulty = itemView.findViewById(R.id.tvIdeaDifficulty);
            tvTime       = itemView.findViewById(R.id.tvIdeaTime);
            ivFavorite   = itemView.findViewById(R.id.ivFavorite);
        }

        void bind(Idea idea) {
            tvTitle.setText(idea.getTitle());
            tvShort.setText(idea.getShortDescription());
            tvCategory.setText(idea.getCategory());
            tvDifficulty.setText(idea.getDifficultyText());
            tvTime.setText(idea.getEstimatedTime());

            ivFavorite.setImageResource(idea.isFavorite()
                    ? android.R.drawable.btn_star_big_on
                    : android.R.drawable.btn_star_big_off);

            itemView.setOnClickListener(v -> {
                if (clickListener != null) clickListener.onIdeaClick(idea);
            });
            ivFavorite.setOnClickListener(v -> {
                if (favoriteListener != null) favoriteListener.onFavoriteClick(idea);
            });
        }
    }
}
