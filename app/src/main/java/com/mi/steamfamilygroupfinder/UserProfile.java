package com.mi.steamfamilygroupfinder;

import java.util.List;

public class UserProfile {
    private String uid; // Add this line
    private String username;
    private String email;
    private List<String> gamesOwned;
    private List<String> gamesInterested;

    public UserProfile() {
    }

    public UserProfile(String uid, String email, List<String> gamesOwned, List<String> gamesInterested) {
        this.uid = uid;
        this.email = email;
        this.gamesOwned = gamesOwned;
        this.gamesInterested = gamesInterested;
    }

    // Getter and setter methods

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setGamesOwned(List<String> gamesOwned) {
        this.gamesOwned = gamesOwned;
    }

    public List<String> getGamesOwned() {
        return this.gamesOwned;
    }

    public void setGamesInterested(List<String> gamesInterested) {
        this.gamesInterested = gamesInterested;
    }

    public List<String> getGamesInterested() {
        return this.gamesInterested;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return this.email;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return this.username;
    }
}
