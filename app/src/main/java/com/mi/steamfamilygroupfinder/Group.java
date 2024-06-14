package com.mi.steamfamilygroupfinder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Group {
    private static final int MAX_MEMBERS = 6;
    private String gid;
    private String groupName;
    private String groupLeader;
    private List<String> members;

    public Group() {
        this.gid = UUID.randomUUID().toString(); // Generate a unique group ID
        this.members = new ArrayList<>();
    }

    public String getGid() {
        return gid;
    }

    public void setGid(String gid) {
        this.gid = gid;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupLeader() {
        return groupLeader;
    }

    public void setGroupLeader(String groupLeader) {
        this.groupLeader = groupLeader;
    }

    public List<String> getMembers() {
        return new ArrayList<>(members); // Return a copy to prevent modification
    }

    public void setMembers(List<String> members) {
        this.members = new ArrayList<>(members);
    }

    public boolean addMember(String uid) {
        if (members.size() < MAX_MEMBERS) {
            members.add(uid);
            return true;
        }
        return false; // Group is full
    }

    public boolean removeMember(String uid) {
        return members.remove(uid);
    }

    public boolean isMember(String uid) {
        return members.contains(uid);
    }

    public List<String> getMemberIds() {
        return new ArrayList<>(members);
    }
}
