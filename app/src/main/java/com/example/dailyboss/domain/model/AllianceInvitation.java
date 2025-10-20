package com.example.dailyboss.domain.model;

import java.util.Date;
import com.example.dailyboss.domain.model.AllianceInvitationStatus;

public class AllianceInvitation {
    private String id;
    private String allianceId;
    private String allianceName; // Dodato za lakši prikaz u notifikaciji
    private String senderId; // ID vođe koji je poslao poziv
    private String receiverId; // ID korisnika koji je pozvan
    private Date sentAt;
    private AllianceInvitationStatus status; // "Pending", "Accepted", "Rejected"

    // Prazan konstruktor potreban za Firebase
    public AllianceInvitation() {}

    public AllianceInvitation(String id, String allianceId, String allianceName, String senderId, String receiverId, Date sentAt, AllianceInvitationStatus status) {
        this.id = id;
        this.allianceId = allianceId;
        this.allianceName = allianceName;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.sentAt = sentAt;
        this.status = status;
    }

    // Getteri i Setteri
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getAllianceId() { return allianceId; }
    public void setAllianceId(String allianceId) { this.allianceId = allianceId; }

    public String getAllianceName() { return allianceName; }
    public void setAllianceName(String allianceName) { this.allianceName = allianceName; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getReceiverId() { return receiverId; }
    public void setReceiverId(String receiverId) { this.receiverId = receiverId; }

    public Date getSentAt() { return sentAt; }
    public void setSentAt(Date sentAt) { this.sentAt = sentAt; }

    public AllianceInvitationStatus getStatus() { return status; }
    public void setStatus(AllianceInvitationStatus status) { this.status = status; }
}