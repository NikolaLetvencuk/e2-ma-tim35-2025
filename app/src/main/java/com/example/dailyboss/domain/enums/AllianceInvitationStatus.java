package com.example.dailyboss.domain.model;

public enum AllianceInvitationStatus {
    PENDING("Pending"),   // Zahtev je poslat i čeka se odgovor
    ACCEPTED("Accepted"), // Poziv je prihvaćen
    REJECTED("Rejected"); // Poziv je odbijen

    private final String displayValue;

    AllianceInvitationStatus(String displayValue) {
        this.displayValue = displayValue;
    }

    // Možete koristiti ovu metodu za konverziju u String ako baza zahteva
    public String getDisplayValue() {
        return displayValue;
    }
}