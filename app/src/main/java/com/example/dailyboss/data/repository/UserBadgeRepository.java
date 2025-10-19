package com.example.dailyboss.data.repository;

import android.content.Context;

import com.example.dailyboss.data.dao.UserBadgeDao;
import com.example.dailyboss.data.dao.BadgeDao;
import com.example.dailyboss.domain.model.Badge;
import com.example.dailyboss.domain.model.UserBadge;

import java.util.ArrayList;
import java.util.List;

public class UserBadgeRepository {

    private final UserBadgeDao userBadgeDao;
    private final BadgeDao badgeDao; // Dodaj dao za Badge

    public UserBadgeRepository(Context context) {
        userBadgeDao = new UserBadgeDao(context);
        badgeDao = new BadgeDao(context); // inicijalizacija
    }

    public boolean addUserBadge(UserBadge userBadge) {
        return userBadgeDao.insert(userBadge);
    }

    public List<UserBadge> getUserBadges(String userId) {
        return userBadgeDao.getUserBadges(userId);
    }

    public boolean removeUserBadge(String userBadgeId) {
        return userBadgeDao.delete(userBadgeId);
    }

    // ✨ NOVO: Dohvatanje svih Badge objekata za korisnika
    public List<Badge> getBadgesForUser(String userId) {
        List<UserBadge> userBadges = userBadgeDao.getUserBadges(userId);
        List<Badge> badges = new ArrayList<>();

        for (UserBadge ub : userBadges) {
            Badge badge = badgeDao.getBadge(ub.getBadgeId());
            if (badge != null) {
                badges.add(badge);
            }
        }
        return badges;
    }
}