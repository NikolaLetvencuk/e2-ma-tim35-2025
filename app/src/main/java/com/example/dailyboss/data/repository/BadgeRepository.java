package com.example.dailyboss.data.repository;

import android.content.Context;

import com.example.dailyboss.data.dao.BadgeDao;
import com.example.dailyboss.domain.model.Badge;

import java.util.List;

public class BadgeRepository {

    private final BadgeDao badgeDao;

    public BadgeRepository(Context context) {
        badgeDao = new BadgeDao(context);
    }

    public boolean addBadge(Badge badge) {
        return badgeDao.insert(badge);
    }

    public boolean updateBadge(Badge badge) {
        return badgeDao.update(badge);
    }

    public Badge getBadge(String id) {
        return badgeDao.getBadge(id);
    }

    public List<Badge> getAllBadges() {
        return badgeDao.getAllBadges();
    }

    public boolean deleteBadge(String id) {
        return badgeDao.delete(id);
    }
}