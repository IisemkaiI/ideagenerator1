package com.example.ideagenerator.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.ideagenerator.R;
import com.example.ideagenerator.model.Idea;
import com.example.ideagenerator.viewmodel.IdeaViewModel;

public class GeneratorFragment extends Fragment {

    private IdeaViewModel viewModel;
    private Spinner spinnerCategory, spinnerDifficulty;
    private Button btnGenerate;
    private ProgressBar progressBar;
    private TextView tvError;
    private CardView cardResult;
    private TextView tvResultTitle, tvResultShort, tvResultCategory, tvResultTime;
    private Button btnDetails, btnCopyResult;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_generator, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(IdeaViewModel.class);
        initViews(view);
        btnGenerate.setOnClickListener(v -> onGenerateClick());
        observeViewModel();
    }

    private void initViews(View view) {
        spinnerCategory   = view.findViewById(R.id.spinnerCategory);
        spinnerDifficulty = view.findViewById(R.id.spinnerDifficulty);
        btnGenerate       = view.findViewById(R.id.btnGenerate);
        progressBar       = view.findViewById(R.id.progressBar);
        tvError           = view.findViewById(R.id.tvError);
        cardResult        = view.findViewById(R.id.cardResult);
        tvResultTitle     = view.findViewById(R.id.tvResultTitle);
        tvResultShort     = view.findViewById(R.id.tvResultShort);
        tvResultCategory  = view.findViewById(R.id.tvResultCategory);
        tvResultTime      = view.findViewById(R.id.tvResultTime);
        btnDetails        = view.findViewById(R.id.btnDetails);
        btnCopyResult     = view.findViewById(R.id.btnCopyResult);
    }

    private void onGenerateClick() {
        String category = spinnerCategory.getSelectedItem().toString();
        int difficulty = spinnerDifficulty.getSelectedItemPosition() + 1;
        cardResult.setVisibility(View.GONE);
        viewModel.generateIdea(category, difficulty);
    }

    private void observeViewModel() {
        viewModel.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            btnGenerate.setEnabled(!isLoading);
        });

        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                tvError.setText(error);
                tvError.setVisibility(View.VISIBLE);
            } else {
                tvError.setVisibility(View.GONE);
            }
        });

        viewModel.getGeneratedIdea().observe(getViewLifecycleOwner(), idea -> {
            if (idea != null) showResult(idea);
        });
    }

    private void showResult(Idea idea) {
        tvResultTitle.setText(idea.getTitle());
        tvResultShort.setText(idea.getShortDescription());
        tvResultCategory.setText(idea.getCategory() + "  \u2022  " + idea.getDifficultyText());
        tvResultTime.setText("\u23F1 " + idea.getEstimatedTime());

        cardResult.setAlpha(0f);
        cardResult.setVisibility(View.VISIBLE);
        cardResult.animate().alpha(1f).setDuration(400).start();

        btnDetails.setOnClickListener(v -> {
            viewModel.setSelectedIdeaId(idea.getId());
            Bundle args = new Bundle();
            args.putString("ideaId", idea.getId());
            Navigation.findNavController(v)
                    .navigate(R.id.action_generator_to_detail, args);
        });

        btnCopyResult.setOnClickListener(v ->
                copyToClipboard(idea.getTitle() + "\n" + idea.getShortDescription()));
    }

    private void copyToClipboard(String text) {
        ClipboardManager cb = (ClipboardManager)
                requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
        cb.setPrimaryClip(ClipData.newPlainText("Идея", text));
        Toast.makeText(requireContext(), "Скопировано!", Toast.LENGTH_SHORT).show();
    }
}
