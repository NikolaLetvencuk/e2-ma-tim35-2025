package com.example.dailyboss.data.repository;

import android.content.Context;

import com.example.dailyboss.data.dao.UserProfileDao;
import com.example.dailyboss.domain.model.UserProfile;

public class UserProfileRepository {

    private final UserProfileDao dao;

    public UserProfileRepository(Context context) {
        dao = new UserProfileDao(context);
    }

    public interface UserStatisticListener {
        void onSuccess(UserProfile statistic);
        void onFailure(Exception e);
    }

    public void getUserStatistic(String userId, UserStatisticListener listener) {
        UserProfile stat = dao.getByUserId(userId);
        if (stat != null) {
            listener.onSuccess(stat);
        } else {
            listener.onFailure(new Exception("Statistika korisnika nije pronađena"));
        }
    }

    public boolean saveOrUpdate(UserProfile stat) {
        return dao.upsert(stat);
    }
}