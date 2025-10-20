package com.example.dailyboss.service;

import android.content.Context;
import android.util.Log;

import com.example.dailyboss.data.repository.BadgeRepository;
import com.example.dailyboss.data.repository.UserBadgeRepository;
import com.example.dailyboss.domain.model.Badge;
import com.example.dailyboss.domain.model.UserBadge;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BadgeService {

    private static final String TAG = "BadgeService";

    private final BadgeRepository badgeRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final Context context;

    public BadgeService(Context context) {
        this.context = context.getApplicationContext();
        this.badgeRepository = new BadgeRepository(context);
        this.userBadgeRepository = new UserBadgeRepository(context);
        initializeDefaultBadges();
    }


    public void initializeDefaultBadges() {
        List<Badge> defaultBadges = createDefaultBadges();

        for (Badge badge : defaultBadges) {
            Badge existingBadge = badgeRepository.getBadge(badge.getId());
            if (existingBadge == null) {
                badgeRepository.addBadge(badge);
                Log.d(TAG, "Default badge added: " + badge.getName());
            } else {
                Log.d(TAG, "Default badge already exists: " + badge.getName());
            }
        }
    }


    public List<Badge> createDefaultBadges() {
        List<Badge> badges = new ArrayList<>();

        badges.add(new Badge(
                "badge_alliance",
                "Savezni Borac",
                "Pridružio si se savezu!",
                "badge_alliance",
                0
        ));

        badges.add(new Badge(
                "badge_level_1",
                "Početnik",
                "Dostigao si nivo 1!",
                "badge_level_1",
                0
        ));

        badges.add(new Badge(
                "badge_ten",
                "Deset Zadataka",
                "Uspešno si rešio 10 zadataka!",
                "badge_ten", // Pretpostavljena ikonica
                10 // Zahteva 10 završenih zadataka
        ));

        badges.add(new Badge(
                "badge_boss",
                "Boss Borac",
                "Pobedio si prvog Bossa!",
                "badge_boss", // Pretpostavljena ikonica
                0 // Nije bazirano na Completion
        ));

        badges.add(new Badge(
                "badge_special",
                "Specijalna misija",
                "Uspešno si rešio specijalnu misiju!", // Ovaj opis je OK
                "badge_special", // Pretpostavljena ikonica
                0 // Nije bazirano na Completion
        ));

        badges.add(new Badge(
                "badge_5",
                "5 Specijalnih Zadataka",
                "Rešio si 5 specijalnih zadataka!",
                "badge_5", // Pretpostavljena ikonica
                0 // Nije bazirano na Completion
        ));

        badges.add(new Badge(
                "badge_10",
                "10 Specijalnih Zadataka",
                "Rešio si 10 specijalnih zadataka!",
                "badge_10", // Pretpostavljena ikonica
                0 // Nije bazirano na Completion
        ));

        badges.add(new Badge(
                "badge_20",
                "20 Specijalnih Zadataka",
                "Rešio si 20 specijalnih zadataka!",
                "badge_20", // Pretpostavljena ikonica
                0 // Nije bazirano na Completion
        ));

        badges.add(new Badge(
                "badge_30",
                "30 Specijalnih Zadataka",
                "Rešio si 30 specijalnih zadataka!",
                "badge_30", // Pretpostavljena ikonica
                0 // Nije bazirano na Completion
        ));

        badges.add(new Badge(
                "badge_40",
                "40 Specijalnih Zadataka",
                "Rešio si 40 specijalnih zadataka!",
                "badge_40", // Pretpostavljena ikonica
                0 // Nije bazirano na Completion
        ));

        badges.add(new Badge(
                "badge_all",
                "Sve Specijalne Zadatke",
                "Rešio si SVE specijalne zadatke!",
                "badge_all", // Pretpostavljena ikonica
                0 // Nije bazirano na Completion
        ));
        return badges;
    }


    public boolean awardBadgeToUser(String userId, String badgeId) {
        // Proveri da li korisnik već ima ovaj bedž
        List<UserBadge> userBadges = userBadgeRepository.getUserBadges(userId);
        for (UserBadge ub : userBadges) {
            if (ub.getBadgeId().equals(badgeId)) {
                Log.d(TAG, "Korisnik " + userId + " već ima bedž " + badgeId);
                return true; // Već ima, smatraj uspehom
            }
        }

        // Ako nema, dodaj bedž
        UserBadge newUserBadge = new UserBadge(
                UUID.randomUUID().toString(),
                userId,
                badgeId,
                System.currentTimeMillis()
        );
        boolean success = userBadgeRepository.addUserBadge(newUserBadge);
        if (success) {
            Log.d(TAG, "Bedž " + badgeId + " dodeljen korisniku " + userId);
        } else {
            Log.e(TAG, "Neuspešno dodeljivanje bedža " + badgeId + " korisniku " + userId);
        }
        return success;
    }

    public void awardSpecialTaskBadges(String userId, int solvedSpecialTasks) {
        // Logika za dodelu bedževa na osnovu broja rešenih zadataka
        // Prvo dodelimo "badge_special" ako je barem jedan zadatak rešen
        if (solvedSpecialTasks > 0) {
            awardBadgeToUser(userId, "badge_special");
        }

        if (solvedSpecialTasks >= 5 && solvedSpecialTasks < 10) {
            awardBadgeToUser(userId, "badge_5");
        } else if (solvedSpecialTasks >= 10 && solvedSpecialTasks < 20) {
            awardBadgeToUser(userId, "badge_10");
        } else if (solvedSpecialTasks >= 20 && solvedSpecialTasks < 30) {
            awardBadgeToUser(userId, "badge_20");
        } else if (solvedSpecialTasks >= 30 && solvedSpecialTasks < 40) { // Ispravljeno sa 45 na 40 za ovaj prag
            awardBadgeToUser(userId, "badge_30");
        } else if (solvedSpecialTasks >= 40 && solvedSpecialTasks < 46) { // Dodat prag za 40+
            awardBadgeToUser(userId, "badge_40");
        } else if (solvedSpecialTasks >= 46) { // "all" prag
            awardBadgeToUser(userId, "badge_all");
        }
        // Možeš dodati i logiku za uklanjanje nižih bedževa ako želiš da korisnik ima samo najviši dostignut.
        // Trenutno, ova implementacija samo dodaje bedževe, ne uklanja prethodne.
    }


    public List<Badge> getUserBadges(String userId) {
        return userBadgeRepository.getBadgesForUser(userId);
    }
}