package com.mi.steamfamilygroupfinder;

import java.util.List;

public class UserProfile {
    private String uid;
    private String username;
    private String email;
    private List<String> gamesOwned;
    private List<String> gamesInterested;
    private Group group; // Add this line

    public UserProfile() {
    }

    public UserProfile(String uid, String username, String email, List<String> gamesOwned, List<String> gamesInterested) {
        this.uid = uid;
        this.username = username;
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<String> getGamesOwned() {
        return gamesOwned;
    }

    public void setGamesOwned(List<String> gamesOwned) {
        this.gamesOwned = gamesOwned;
    }

    public List<String> getGamesInterested() {
        return gamesInterested;
    }

    public void setGamesInterested(List<String> gamesInterested) {
        this.gamesInterested = gamesInterested;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public boolean isInGroup() {
        return this.group != null;
    }
}
