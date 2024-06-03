package com.mi.steamfamilygroupfinder;

import java.util.List;

public class UserProfile {
    public String username;
    public String email;
    public List<String> gamesOwned;
    public List<String> gamesInterested;

    public UserProfile() {
    }

    public UserProfile(String email, List<String> gamesOwned, List<String> gamesInterested) {
        this.email = email;
        this.gamesOwned = gamesOwned;
        this.gamesInterested = gamesInterested;
    }

    // Getter and setter methods
    // Important for retrieving the user's profile data from activities

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

