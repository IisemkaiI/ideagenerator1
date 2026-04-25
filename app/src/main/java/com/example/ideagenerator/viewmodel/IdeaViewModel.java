package com.example.ideagenerator.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.ideagenerator.model.Idea;
import com.example.ideagenerator.repository.IdeaRepository;

import java.util.List;

public class IdeaViewModel extends ViewModel {

    private final IdeaRepository repository;
    private String selectedIdeaId;

    public IdeaViewModel() {
        repository = new IdeaRepository();
    }

    public LiveData<List<Idea>> getAllIdeas()   { return repository.getAllIdeas(); }
    public LiveData<Idea> getGeneratedIdea()    { return repository.getGeneratedIdea(); }
    public LiveData<Boolean> isLoading()         { return repository.getLoading(); }
    public LiveData<String> getError()           { return repository.getError(); }

    public void generateIdea(String category, int difficulty) {
        repository.generateIdea(category, difficulty);
    }

    public void toggleFavorite(String ideaId, boolean currentStatus) {
        repository.toggleFavorite(ideaId, currentStatus);
    }

    public void deleteIdea(String ideaId) {
        repository.deleteIdea(ideaId);
    }

    public void deleteAllIdeas() {
        repository.deleteAllIdeas();
    }

    public void setSelectedIdeaId(String id) { this.selectedIdeaId = id; }
    public String getSelectedIdeaId()         { return selectedIdeaId; }
}
