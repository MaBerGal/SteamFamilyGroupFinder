package com.mi.steamfamilygroupfinder.models;

import java.util.UUID;

public class Request {
    private String requestId;
    private String requesterId;
    private String receiverId;
    private String groupId;
    private boolean isInvite; // This indicates if it's an invite or request to join

    public Request() {
        // Default constructor required for Firebase
    }

    public Request(String requesterId, String receiverId, String groupId, boolean isInvite) {
        this.requestId = UUID.randomUUID().toString();
        this.requesterId = requesterId;
        this.receiverId = receiverId;
        this.groupId = groupId;
        this.isInvite = isInvite;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getRequesterId() {
        return requesterId;
    }

    public void setRequesterId(String requesterId) {
        this.requesterId = requesterId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public boolean getIsInvite() {
        return isInvite;
    }

    public void setIsInvite(boolean invite) {
        isInvite = invite;
    }
}
