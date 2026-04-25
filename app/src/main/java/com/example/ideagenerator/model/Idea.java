package com.example.ideagenerator.model;

import com.google.firebase.Timestamp;

public class Idea {

    private String id;
    private String title;
    private String shortDescription;
    private String fullDescription;
    private String category;
    private int difficulty;
    private String features;
    private String technologies;
    private String estimatedTime;
    private boolean isFavorite;
    private Timestamp dateAdded;

    public Idea() { }

    public Idea(String title, String shortDescription, String fullDescription,
                String category, int difficulty, String features,
                String technologies, String estimatedTime) {
        this.title = title;
        this.shortDescription = shortDescription;
        this.fullDescription = fullDescription;
        this.category = category;
        this.difficulty = difficulty;
        this.features = features;
        this.technologies = technologies;
        this.estimatedTime = estimatedTime;
        this.isFavorite = false;
        this.dateAdded = Timestamp.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getShortDescription() { return shortDescription; }
    public void setShortDescription(String sd) { this.shortDescription = sd; }
    public String getFullDescription() { return fullDescription; }
    public void setFullDescription(String fd) { this.fullDescription = fd; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public int getDifficulty() { return difficulty; }
    public void setDifficulty(int difficulty) { this.difficulty = difficulty; }
    public String getFeatures() { return features; }
    public void setFeatures(String features) { this.features = features; }
    public String getTechnologies() { return technologies; }
    public void setTechnologies(String technologies) { this.technologies = technologies; }
    public String getEstimatedTime() { return estimatedTime; }
    public void setEstimatedTime(String et) { this.estimatedTime = et; }
    
    public boolean getIsFavorite() { return isFavorite; }
    public void setIsFavorite(boolean fav) { isFavorite = fav; }
    
    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean fav) { isFavorite = fav; }
    
    public Timestamp getDateAdded() { return dateAdded; }
    public void setDateAdded(Timestamp da) { this.dateAdded = da; }

    public String getDifficultyText() {
        switch (difficulty) {
            case 1:  return "Легко";
            case 2:  return "Средне";
            case 3:  return "Сложно";
            default: return "Неизвестно";
        }
    }
}