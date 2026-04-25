package com.example.ideagenerator.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.ideagenerator.R;
import com.example.ideagenerator.model.Idea;
import com.example.ideagenerator.viewmodel.IdeaViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class DetailFragment extends Fragment {

    private IdeaViewModel viewModel;
    private View rootView;
    private TextView tvTitle, tvCategory, tvDifficulty, tvShort, tvFull;
    private TextView tvFeatures, tvTech, tvTime;
    private Button btnFavorite, btnCopy, btnDelete;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rootView = view;
        viewModel = new ViewModelProvider(requireActivity()).get(IdeaViewModel.class);

        tvTitle      = view.findViewById(R.id.tvDetailTitle);
        tvCategory   = view.findViewById(R.id.tvDetailCategory);
        tvDifficulty = view.findViewById(R.id.tvDetailDifficulty);
        tvShort      = view.findViewById(R.id.tvDetailShort);
        tvFull       = view.findViewById(R.id.tvDetailFull);
        tvFeatures   = view.findViewById(R.id.tvDetailFeatures);
        tvTech       = view.findViewById(R.id.tvDetailTech);
        tvTime       = view.findViewById(R.id.tvDetailTime);
        btnFavorite  = view.findViewById(R.id.btnFavorite);
        btnCopy      = view.findViewById(R.id.btnCopyDetail);
        btnDelete    = view.findViewById(R.id.btnDelete);

        String ideaId = getArguments() != null
                ? getArguments().getString("ideaId") : null;

        String finalId = ideaId;
        viewModel.getAllIdeas().observe(getViewLifecycleOwner(), ideas -> {
            for (Idea idea : ideas) {
                if (idea.getId() != null && idea.getId().equals(finalId)) {
                    displayIdea(idea);
                    break;
                }
            }
        });
    }

    private void displayIdea(Idea idea) {
        tvTitle.setText(idea.getTitle());
        tvCategory.setText(idea.getCategory());
        tvDifficulty.setText(idea.getDifficultyText());
        tvShort.setText(idea.getShortDescription());
        tvFull.setText(idea.getFullDescription());
        tvFeatures.setText(idea.getFeatures());
        tvTech.setText(idea.getTechnologies());
        tvTime.setText(idea.getEstimatedTime());

        btnFavorite.setText(idea.isFavorite() ? "В избранном" : "В избранное");

        btnFavorite.setOnClickListener(v ->
                viewModel.toggleFavorite(idea.getId(), idea.isFavorite()));

        btnCopy.setOnClickListener(v -> {
            String text = idea.getTitle() + "\n\n" +
                    idea.getFullDescription() + "\n\n" +
                    "Фичи: " + idea.getFeatures() + "\n" +
                    "Технологии: " + idea.getTechnologies() + "\n" +
                    "Время: " + idea.getEstimatedTime();
            ClipboardManager clipboard = (ClipboardManager)
                    requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setPrimaryClip(ClipData.newPlainText("Идея", text));
            Toast.makeText(requireContext(), "Скопировано!", Toast.LENGTH_SHORT).show();
        });

        btnDelete.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Удаление")
                    .setMessage("Вы уверены, что хотите удалить эту идею?")
                    .setPositiveButton("Удалить", (dialog, which) -> {
                        viewModel.deleteIdea(idea.getId());
                        Navigation.findNavController(rootView).popBackStack();
                        Toast.makeText(requireContext(), "Идея удалена", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Отмена", null)
                    .show();
        });
    }
}
