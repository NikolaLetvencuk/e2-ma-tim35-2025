package com.example.dailyboss.data.repository;

import android.content.Context;

import com.example.dailyboss.data.dao.UserStatisticDao;
import com.example.dailyboss.domain.model.UserStatistic;

public class UserStatisticRepository {

    private final UserStatisticDao dao;

    public UserStatisticRepository(Context context) {
        dao = new UserStatisticDao(context);
    }

    public interface UserStatisticListener {
        void onSuccess(UserStatistic statistic);
        void onFailure(Exception e);
    }

    // Dohvatanje statistike korisnika asinhrono preko listener-a
    public void getUserStatistic(String userId, UserStatisticListener listener) {
        UserStatistic stat = dao.getByUserId(userId);
        if (stat != null) {
            listener.onSuccess(stat);
        } else {
            listener.onFailure(new Exception("Statistika korisnika nije pronađena"));
        }
    }

    // Čuvanje ili ažuriranje statistike
    public boolean saveOrUpdate(UserStatistic stat) {
        return dao.upsert(stat);
    }
}