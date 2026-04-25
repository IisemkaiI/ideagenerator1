package com.example.ideagenerator.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ideagenerator.R;
import com.example.ideagenerator.adapter.IdeaAdapter;
import com.example.ideagenerator.viewmodel.IdeaViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class CatalogFragment extends Fragment {

    private IdeaViewModel viewModel;
    private IdeaAdapter adapter;
    private TextView tvEmpty;
    private View rootView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_catalog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rootView = view;
        viewModel = new ViewModelProvider(requireActivity()).get(IdeaViewModel.class);
        tvEmpty = view.findViewById(R.id.tvEmpty);

        RecyclerView rv = view.findViewById(R.id.rvIdeas);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new IdeaAdapter();
        rv.setAdapter(adapter);

        adapter.setOnIdeaClickListener(idea -> {
            viewModel.setSelectedIdeaId(idea.getId());
            Bundle args = new Bundle();
            args.putString("ideaId", idea.getId());
            Navigation.findNavController(view)
                    .navigate(R.id.action_catalog_to_detail, args);
        });

        adapter.setOnFavoriteClickListener(idea ->
                viewModel.toggleFavorite(idea.getId(), idea.isFavorite()));

        viewModel.getAllIdeas().observe(getViewLifecycleOwner(), ideas -> {
            adapter.setIdeas(ideas);
            tvEmpty.setVisibility(ideas.isEmpty() ? View.VISIBLE : View.GONE);
        });

        MaterialButton btnDeleteAll = view.findViewById(R.id.btnDeleteAll);
        btnDeleteAll.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Удаление всех идей")
                    .setMessage("Вы уверены, что хотите удалить все идеи?")
                    .setPositiveButton("Удалить всё", (dialog, which) -> {
                        viewModel.deleteAllIdeas();
                        Toast.makeText(requireContext(), "Все идеи удалены", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Отмена", null)
                    .show();
        });
    }
}
