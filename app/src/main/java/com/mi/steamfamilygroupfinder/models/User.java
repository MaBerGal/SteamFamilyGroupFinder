package com.mi.steamfamilygroupfinder.models;

import java.util.List;

public class User {
    private String uid;
    private String username;
    private String email;
    private String profilePicture;
    private List<Integer> gamesOwned;
    private List<Integer> gamesInterested;
    private String gid; // Reference the group ID
    private boolean isGroupLeader;
    private List<String> chatIdentifiers; // List to store chat identifiers

    public User() {
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

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public List<Integer> getGamesOwned() {
        return gamesOwned;
    }

    public void setGamesOwned(List<Integer> gamesOwned) {
        this.gamesOwned = gamesOwned;
    }

    public List<Integer> getGamesInterested() {
        return gamesInterested;
    }

    public void setGamesInterested(List<Integer> gamesInterested) {
        this.gamesInterested = gamesInterested;
    }

    public String getGid() {
        return gid;
    }

    public void setGid(String gid) {
        this.gid = gid;
    }

    public boolean isGroupLeader() {
        return isGroupLeader;
    }
    public void setGroupLeader(boolean groupLeader) {
        isGroupLeader = groupLeader;
    }

    public void setIsGroupLeader(boolean groupLeader) {
        isGroupLeader = groupLeader;
    }

    public boolean isInGroup() {
        return this.gid != null;
    }
    public List<String> getChatIdentifiers() {
        return chatIdentifiers;
    }

    public void setChatIdentifiers(List<String> chatIdentifiers) {
        this.chatIdentifiers = chatIdentifiers;
    }

    public int getGamesOwnedCount() {
        if (gamesOwned != null) {
            return gamesOwned.size();
        } else {
            return 0;
        }
    }
}
