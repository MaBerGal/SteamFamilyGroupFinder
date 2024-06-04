package com.mi.steamfamilygroupfinder;

import java.util.ArrayList;
import java.util.List;

public class Group {
    private static final int MAX_MEMBERS = 6;
    private List<UserProfile> members;

    public Group() {
        this.members = new ArrayList<>();
    }

    public boolean addMember(UserProfile user) {
        if (members.size() < MAX_MEMBERS) {
            members.add(user);
            return true;
        }
        return false; // Group is full
    }

    public boolean removeMember(UserProfile user) {
        return members.remove(user);
    }

    public boolean isMember(UserProfile user) {
        return members.contains(user);
    }

    public List<UserProfile> getMembers() {
        return new ArrayList<>(members); // Return a copy to prevent modification
    }

    public List<String> getMemberUsernames() {
        List<String> usernames = new ArrayList<>();
        for (UserProfile member : members) {
            usernames.add(member.getUsername());
        }
        return usernames;
    }

    public List<String> getAllGamesOwned() {
        List<String> allGames = new ArrayList<>();
        for (UserProfile member : members) {
            allGames.addAll(member.getGamesOwned());
        }
        return allGames;
    }
}
