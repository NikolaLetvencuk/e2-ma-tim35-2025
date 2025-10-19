package com.example.dailyboss.data.repository;

import android.content.Context;
import android.util.Log;

import com.example.dailyboss.data.dao.UserEquipmentDao;
import com.example.dailyboss.domain.model.UserEquipment;

import java.util.List;

public class UserEquipmentRepository {

    private final UserEquipmentDao userEquipmentDao;
    private static final String TAG = "UserEquipmentRepo";

    public UserEquipmentRepository(Context context) {
        this.userEquipmentDao = new UserEquipmentDao(context);
    }

    // Interfejs callback-a za operacije nad UserEquipment
    public interface EquipmentDataListener {
        void onSuccess(UserEquipment userEquipment);
        void onSuccess(List<UserEquipment> userEquipmentList);
        void onFailure(Exception e);
    }

    /**
     * Dodavanje ili ažuriranje korisničke opreme
     */
    public void upsertUserEquipment(UserEquipment userEquipment, EquipmentDataListener listener) {
        try {
            boolean success = userEquipmentDao.upsert(userEquipment);
            if (success) {
                listener.onSuccess(userEquipment);
            } else {
                listener.onFailure(new Exception("Upsert user equipment nije uspeo"));
            }
        } catch (Exception e) {
            Log.e(TAG, "Greška pri upsert-u: " + e.getMessage());
            listener.onFailure(e);
        }
    }

    /**
     * Dohvatanje pojedinačne opreme po ID-u
     */
    public void getUserEquipmentById(String userEquipmentId, EquipmentDataListener listener) {
        try {
            UserEquipment userEquipment = userEquipmentDao.getUserEquipment(userEquipmentId);
            if (userEquipment != null) {
                listener.onSuccess(userEquipment);
            } else {
                listener.onFailure(new Exception("UserEquipment nije pronađen"));
            }
        } catch (Exception e) {
            Log.e(TAG, "Greška pri dohvatanju user equipment: " + e.getMessage());
            listener.onFailure(e);
        }
    }

    /**
     * Dohvatanje sve opreme za jednog korisnika
     */
    public void getAllUserEquipmentForUser(String userId, EquipmentDataListener listener) {
        try {
            List<UserEquipment> equipmentList = userEquipmentDao.getUserEquipmentForUser(userId);
            listener.onSuccess(equipmentList);
        } catch (Exception e) {
            Log.e(TAG, "Greška pri dohvatanju opreme korisnika: " + e.getMessage());
            listener.onFailure(e);
        }
    }

    /**
     * Brisanje korisničke opreme po ID-u
     */
    public void deleteUserEquipment(String userEquipmentId, EquipmentDataListener listener) {
        try {
            boolean deleted = userEquipmentDao.deleteUserEquipment(userEquipmentId);
            if (deleted) {
                listener.onSuccess((UserEquipment) null); // Nema objekta za vraćanje
            } else {
                listener.onFailure(new Exception("Brisanje user equipment nije uspelo"));
            }
        } catch (Exception e) {
            Log.e(TAG, "Greška pri brisanju user equipment: " + e.getMessage());
            listener.onFailure(e);
        }
    }

    /**
     * Dohvatanje specifične opreme korisnika (ako postoji samo jedna instanca)
     */
    public void getUserSpecificEquipment(String userId, String equipmentId, EquipmentDataListener listener) {
        try {
            UserEquipment userEquipment = userEquipmentDao.getUserSpecificEquipment(userId, equipmentId);
            if (userEquipment != null) {
                listener.onSuccess(userEquipment);
            } else {
                listener.onFailure(new Exception("Specifična oprema korisnika nije pronađena"));
            }
        } catch (Exception e) {
            Log.e(TAG, "Greška pri dohvatanju specifične opreme: " + e.getMessage());
            listener.onFailure(e);
        }
    }
}