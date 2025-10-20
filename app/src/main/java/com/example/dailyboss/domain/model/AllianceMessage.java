package com.example.dailyboss.domain.model;

import java.util.Date;

public class AllianceMessage {
    private String id;
    private String allianceId;
    private String senderId;
    private String senderUsername; // NEW FIELD
    private String content;
    private Date timestamp;

    public AllianceMessage() {}

    public AllianceMessage(String id, String allianceId, String senderId, String senderUsername, String content, Date timestamp) {
        this.id = id;
        this.allianceId = allianceId;
        this.senderId = senderId;
        this.senderUsername = senderUsername; // Assign in constructor
        this.content = content;
        this.timestamp = timestamp;
    }

    // Getteri i Setteri
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getAllianceId() { return allianceId; }
    public void setAllianceId(String allianceId) { this.allianceId = allianceId; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    // Getter and Setter for senderUsername
    public String getSenderUsername() { return senderUsername; }
    public void setSenderUsername(String senderUsername) { this.senderUsername = senderUsername; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
}