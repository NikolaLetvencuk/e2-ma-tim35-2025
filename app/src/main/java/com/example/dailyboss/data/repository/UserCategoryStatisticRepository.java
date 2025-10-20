package com.example.dailyboss.data.repository;

import android.content.Context;
import android.util.Log;

import com.example.dailyboss.data.dao.UserCategoryStatisticDao;
import com.example.dailyboss.domain.model.UserCategoryStatistic;

import java.util.List;

public class UserCategoryStatisticRepository {

    private final UserCategoryStatisticDao dao;

    public UserCategoryStatisticRepository(Context context) {
        dao = new UserCategoryStatisticDao(context);
    }

    public interface UserCategoryStatisticListener {
        void onSuccess(UserCategoryStatistic statistic);
        void onFailure(Exception e);
    }

    public interface UserCategoryStatisticListListener {
        void onSuccess(List<UserCategoryStatistic> statistics);
        void onFailure(Exception e);
    }

    // Dohvatanje specifične statistike kategorije korisnika
    public void getCategoryStatistic(String userStatisticId, String categoryId, UserCategoryStatisticListener listener) {
        UserCategoryStatistic stat = dao.getCategoryStatistic(userStatisticId, categoryId);
        if (stat != null) {
            listener.onSuccess(stat);
        } else {
            listener.onFailure(new Exception("Statistika kategorije korisnika nije pronađena za UserStatisticId: " + userStatisticId + ", CategoryId: " + categoryId));
        }
    }

    public void getAllCategoryStatisticsForUser(String userStatisticId, UserCategoryStatisticListListener listener) {
        List<UserCategoryStatistic> stats = dao.getAllCategoryStatisticsForUser(userStatisticId);
        if (stats != null) {
            listener.onSuccess(stats);
        } else {
            listener.onFailure(new Exception("Statistike kategorija nisu pronađene za UserStatisticId: " + userStatisticId));
        }
    }

    public boolean saveOrUpdate(UserCategoryStatistic stat) {
        return dao.upsert(stat);
    }

    public boolean deleteCategoryStatisticsForUser(String userStatisticId) {
        return dao.deleteCategoryStatisticsForUser(userStatisticId);
    }
}