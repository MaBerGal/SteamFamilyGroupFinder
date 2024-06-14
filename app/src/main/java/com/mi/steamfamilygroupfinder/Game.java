package com.mi.steamfamilygroupfinder;

import java.util.List;

public class Game {
    private int sid;
    private String name;
    private String description;
    private String image;
    private int store_uscore;
    private String store_url;
    private String published_store;
    private String genres;
    private String developers;
    private String tags; // Changed from List<String> to String
    private String languages; // Changed from List<String> to String
    private boolean isSelected; // New attribute for selection status

    // Empty constructor
    public Game() {
    }

    // Getters and setters
    public int getSid() {
        return sid;
    }

    public void setSid(int sid) {
        this.sid = sid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getStoreUscore() {
        return store_uscore;
    }

    public void setStoreUscore(int store_uscore) {
        this.store_uscore = store_uscore;
    }

    public String getStoreUrl() {
        return store_url;
    }

    public void setStoreUrl(String store_url) {
        this.store_url = store_url;
    }

    public String getPublishedStore() {
        return published_store;
    }

    public void setPublishedStore(String published_store) {
        this.published_store = published_store;
    }

    public String getGenres() {
        return genres;
    }

    public void setGenres(String genres) {
        this.genres = genres;
    }

    public String getDevelopers() {
        return developers;
    }

    public void setDevelopers(String developers) {
        this.developers = developers;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getLanguages() {
        return languages;
    }

    public void setLanguages(String languages) {
        this.languages = languages;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
