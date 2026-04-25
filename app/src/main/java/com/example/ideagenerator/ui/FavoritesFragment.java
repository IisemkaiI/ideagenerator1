package com.example.ideagenerator.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ideagenerator.R;
import com.example.ideagenerator.adapter.IdeaAdapter;
import com.example.ideagenerator.model.Idea;
import com.example.ideagenerator.viewmodel.IdeaViewModel;

import java.util.ArrayList;
import java.util.List;

public class FavoritesFragment extends Fragment {

    private IdeaViewModel viewModel;
    private IdeaAdapter adapter;
    private TextView tvEmpty;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favorites, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(IdeaViewModel.class);
        tvEmpty = view.findViewById(R.id.tvEmptyFav);

        RecyclerView rv = view.findViewById(R.id.rvFavorites);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new IdeaAdapter();
        rv.setAdapter(adapter);

        adapter.setOnIdeaClickListener(idea -> {
            viewModel.setSelectedIdeaId(idea.getId());
            Bundle args = new Bundle();
            args.putString("ideaId", idea.getId());
            Navigation.findNavController(view)
                    .navigate(R.id.action_favorites_to_detail, args);
        });

        adapter.setOnFavoriteClickListener(idea ->
                viewModel.toggleFavorite(idea.getId(), idea.isFavorite()));

        viewModel.getAllIdeas().observe(getViewLifecycleOwner(), ideas -> {
            List<Idea> favorites = new ArrayList<>();
            for (Idea idea : ideas) {
                if (idea.isFavorite()) favorites.add(idea);
            }
            adapter.setIdeas(favorites);
            tvEmpty.setVisibility(favorites.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }
}
