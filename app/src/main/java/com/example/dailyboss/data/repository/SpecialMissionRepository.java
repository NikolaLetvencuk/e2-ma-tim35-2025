package com.example.dailyboss.data.repository;

import android.content.Context;
import android.util.Log;

import com.example.dailyboss.data.dao.MissionActivityLogDao;
import com.example.dailyboss.data.dao.SpecialMissionDao;
import com.example.dailyboss.data.dao.UserMissionProgressDao;
import com.example.dailyboss.domain.model.MissionActivityLog;
import com.example.dailyboss.domain.model.SpecialMission;
import com.example.dailyboss.domain.model.User;
import com.example.dailyboss.domain.model.UserMissionProgress;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.Transaction; // Import Transaction

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SpecialMissionRepository {

    private static final String TAG = "SpecialMissionRepo";
    private final FirebaseFirestore db;
    private final CollectionReference specialMissionsCollection;
    private final CollectionReference userMissionProgressCollection;
    private final CollectionReference missionActivityLogsCollection;
    private final SpecialMissionDao specialMissionDao;
    private final UserMissionProgressDao userMissionProgressDao;
    private final MissionActivityLogDao missionActivityLogDao;
    private final ExecutorService executorService;

    public SpecialMissionRepository(Context context) {
        db = FirebaseFirestore.getInstance();
        specialMissionsCollection = db.collection("specialMissions");
        userMissionProgressCollection = db.collection("userMissionProgress");
        missionActivityLogsCollection = db.collection("missionActivityLogs");
        this.specialMissionDao = new SpecialMissionDao(context);
        this.userMissionProgressDao = new UserMissionProgressDao(context);
        this.missionActivityLogDao = new MissionActivityLogDao(context);
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public Task<SpecialMission> startSpecialMission(String allianceId, int numberOfMembers, List<User> members) {
        return Tasks.call(executorService, () -> {
            SpecialMission existingActiveMission = specialMissionDao.getActiveSpecialMissionForAlliance(allianceId);
            if (existingActiveMission != null && existingActiveMission.isActive()) {
                throw new Exception("Savez već ima aktivnu specijalnu misiju (lokalno).");
            }
            return null;
        }).continueWithTask(localCheckTask -> {
            if (localCheckTask.isSuccessful() && localCheckTask.getResult() != null) {
                throw new Exception("Savez već ima aktivnu specijalnu misiju.");
            }
            return getActiveSpecialMissionForAllianceFirestore(allianceId).continueWithTask(firestoreCheckTask -> {
                if (firestoreCheckTask.isSuccessful() && firestoreCheckTask.getResult() != null && firestoreCheckTask.getResult().isActive()) {
                    throw new Exception("Savez već ima aktivnu specijalnu misiju (Firestore).");
                }

                String missionId = UUID.randomUUID().toString();
                Date startTime = new Date();
                Date endTime = new Date(startTime.getTime() + TimeUnit.DAYS.toMillis(14));
                SpecialMission newMission = new SpecialMission(missionId, allianceId, numberOfMembers, startTime, false);
                newMission.setEndTime(endTime);
                newMission.setActive(true);
                newMission.setTotalBossHp(100);
                newMission.setCurrentBossHp(100);
                newMission.setCompletedSuccessfully(false);
                newMission.setRewardsAwarded(false);

                List<Task<Void>> progressTasks = new ArrayList<>();
                for (User member : members) {
                    UserMissionProgress progress = new UserMissionProgress(
                            UUID.randomUUID().toString(), missionId, member.getId(), member.getUsername());
                    progressTasks.add(userMissionProgressCollection.document(progress.getId()).set(progress));
                    executorService.execute(() -> userMissionProgressDao.insert(progress)); // Async local insert
                }

                return Tasks.whenAll(progressTasks)
                        .continueWithTask(innerTask -> {
                            if (!innerTask.isSuccessful()) {
                                throw innerTask.getException();
                            }
                            return specialMissionsCollection.document(missionId).set(newMission)
                                    .continueWithTask(missionSetTask -> {
                                        if (!missionSetTask.isSuccessful()) {
                                            throw missionSetTask.getException();
                                        }
                                        return Tasks.call(executorService, () -> {
                                            specialMissionDao.insert(newMission);
                                            Log.d(TAG, "New SpecialMission inserted into local DB: " + newMission.getId());
                                            return newMission;
                                        });
                                    });
                        });
            });
        });
    }

    private Task<SpecialMission> getActiveSpecialMissionForAllianceFirestore(String allianceId) {
        return specialMissionsCollection
                .whereEqualTo("allianceId", allianceId)
                .whereEqualTo("isActive", true)
                .get()
                .continueWith(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        SpecialMission mission = task.getResult().getDocuments().get(0).toObject(SpecialMission.class);
                        if (mission != null) {
                            executorService.execute(() -> {
                                if (specialMissionDao.getSpecialMissionById(mission.getId()) == null) {
                                    specialMissionDao.insert(mission);
                                } else {
                                    specialMissionDao.update(mission);
                                }
                            });
                        }
                        return mission;
                    }
                    return null;
                });
    }

    public Task<SpecialMission> getSpecialMissionById(String missionId) {
        return Tasks.call(executorService, () -> {
            SpecialMission localMission = specialMissionDao.getSpecialMissionById(missionId);
            if (localMission != null) {
                Log.d(TAG, "SpecialMission found in local DB: " + missionId);
                return localMission;
            }
            Log.d(TAG, "SpecialMission NOT found in local DB, fetching from Firestore: " + missionId);
            return null;
        }).continueWithTask(localTask -> {
            SpecialMission localResult = localTask.getResult();
            if (localResult != null) {
                return Tasks.forResult(localResult);
            }
            return specialMissionsCollection.document(missionId).get()
                    .continueWithTask(firestoreTask -> {
                        if (firestoreTask.isSuccessful() && firestoreTask.getResult().exists()) {
                            SpecialMission firestoreMission = firestoreTask.getResult().toObject(SpecialMission.class);
                            if (firestoreMission != null) {
                                return Tasks.call(executorService, () -> {
                                    if (specialMissionDao.getSpecialMissionById(firestoreMission.getId()) == null) {
                                        specialMissionDao.insert(firestoreMission);
                                    } else {
                                        specialMissionDao.update(firestoreMission);
                                    }
                                    Log.d(TAG, "SpecialMission fetched from Firestore and cached locally: " + missionId);
                                    return firestoreMission;
                                });
                            }
                        }
                        return Tasks.forResult(null);
                    });
        });
    }

    public Task<List<UserMissionProgress>> getAllUserProgressForMission(String specialMissionId) {
        return Tasks.call(executorService, () -> {
            List<UserMissionProgress> localProgresses = userMissionProgressDao.getAllUserProgressForMission(specialMissionId);
            if (!localProgresses.isEmpty()) {
                Log.d(TAG, "UserMissionProgress list found in local DB for mission: " + specialMissionId);
                return localProgresses;
            }
            Log.d(TAG, "UserMissionProgress list NOT found locally, fetching from Firestore for mission: " + specialMissionId);
            return null;
        }).continueWithTask(localTask -> {
            List<UserMissionProgress> localResult = localTask.getResult();
            if (localResult != null) {
                return Tasks.forResult(localResult);
            }
            return userMissionProgressCollection
                    .whereEqualTo("specialMissionId", specialMissionId)
                    .get()
                    .continueWithTask(firestoreTask -> {
                        List<UserMissionProgress> progressList = new ArrayList<>();
                        if (firestoreTask.isSuccessful()) {
                            for (QueryDocumentSnapshot document : firestoreTask.getResult()) {
                                UserMissionProgress progress = document.toObject(UserMissionProgress.class);
                                progressList.add(progress);
                                executorService.execute(() -> {
                                    if (userMissionProgressDao.getUserMissionProgressById(progress.getId()) == null) {
                                        userMissionProgressDao.insert(progress);
                                    } else {
                                        userMissionProgressDao.update(progress);
                                    }
                                });
                            }
                            Log.d(TAG, "UserMissionProgress list fetched from Firestore and cached locally for mission: " + specialMissionId);
                        } else {
                            Log.e(TAG, "Error getting user mission progress from Firestore: " + firestoreTask.getException());
                        }
                        return Tasks.forResult(progressList);
                    });
        });
    }

    public Task<UserMissionProgress> getUserProgressForMission(String specialMissionId, String userId) {
        return Tasks.call(executorService, () -> {
            UserMissionProgress localProgress = userMissionProgressDao.getUserMissionProgressForUserAndMission(userId, specialMissionId);
            if (localProgress != null) {
                Log.d(TAG, "UserMissionProgress found in local DB for user " + userId + " and mission " + specialMissionId);
                return localProgress;
            }
            Log.d(TAG, "UserMissionProgress NOT found locally, fetching from Firestore for user " + userId + " and mission " + specialMissionId);
            return null;
        }).continueWithTask(localTask -> {
            UserMissionProgress localResult = localTask.getResult();
            if (localResult != null) {
                return Tasks.forResult(localResult);
            }
            return userMissionProgressCollection
                    .whereEqualTo("specialMissionId", specialMissionId)
                    .whereEqualTo("userId", userId)
                    .get()
                    .continueWithTask(firestoreTask -> {
                        if (firestoreTask.isSuccessful() && !firestoreTask.getResult().isEmpty()) {
                            UserMissionProgress firestoreProgress = firestoreTask.getResult().getDocuments().get(0).toObject(UserMissionProgress.class);
                            if (firestoreProgress != null) {
                                return Tasks.call(executorService, () -> {
                                    if (userMissionProgressDao.getUserMissionProgressById(firestoreProgress.getId()) == null) {
                                        userMissionProgressDao.insert(firestoreProgress);
                                    } else {
                                        userMissionProgressDao.update(firestoreProgress);
                                    }
                                    Log.d(TAG, "UserMissionProgress fetched from Firestore and cached locally for user " + userId);
                                    return firestoreProgress;
                                });
                            }
                        }
                        return Tasks.forResult(null);
                    });
        });
    }

    public Task<Void> updateMissionProgress(String specialMissionId, String userId, String username, String activityType, int taskValue) {
        return getUserProgressForMission(specialMissionId, userId)
                .continueWithTask(getUserProgressTask -> {
                    if (!getUserProgressTask.isSuccessful() || getUserProgressTask.getResult() == null) {
                        throw new Exception("User mission progress not found or failed to load.");
                    }
                    UserMissionProgress userProgress = getUserProgressTask.getResult();

                    int damageDealt = 0;
                    boolean updated = false;

                    switch (activityType) {
                        case "buyInShop":
                            if (userProgress.getBuyInShopCount() < 5) {
                                userProgress.setBuyInShopCount(userProgress.getBuyInShopCount() + 1);
                                damageDealt = 2;
                                updated = true;
                            }
                            break;
                        case "regularBossHit":
                            if (userProgress.getRegularBossHitCount() < 10) {
                                userProgress.setRegularBossHitCount(userProgress.getRegularBossHitCount() + 1);
                                damageDealt = 2;
                                updated = true;
                            }
                            break;
                        case "easyNormalImportantTask":
                            if (userProgress.getEasyNormalImportantTaskCount() < 10) {
                                userProgress.setEasyNormalImportantTaskCount(userProgress.getEasyNormalImportantTaskCount() + taskValue);
                                damageDealt = taskValue;
                                updated = true;
                            }
                            break;
                        case "otherTasks":
                            if (userProgress.getOtherTasksCount() < 6) {
                                userProgress.setOtherTasksCount(userProgress.getOtherTasksCount() + 1);
                                damageDealt = 4;
                                updated = true;
                            }
                            break;
                        case "noUnresolvedTasks":
                            if (!userProgress.isNoUnresolvedTasksCompleted()) {
                                userProgress.setNoUnresolvedTasksCompleted(true);
                                damageDealt = 10;
                                updated = true;
                            }
                            break;
                        case "allianceMessage":
                            Date today = new Date();
                            if (userProgress.getLastMessageSentDate() == null ||
                                    !isSameDay(userProgress.getLastMessageSentDate(), today)) {
                                userProgress.setLastMessageSentDate(today);
                                damageDealt = 4;
                                updated = true;
                            }
                            break;
                    }

                    if (updated && damageDealt > 0) {
                        int finalDamageDealt = damageDealt;
                        return Tasks.whenAll(
                                userMissionProgressCollection.document(userProgress.getId()).set(userProgress),
                                Tasks.call(executorService, () -> {
                                    userMissionProgressDao.update(userProgress);
                                    Log.d(TAG, "UserMissionProgress updated locally: " + userProgress.getId());
                                    return null;
                                })
                        ).continueWithTask(progressUpdateTask -> {
                            if (!progressUpdateTask.isSuccessful()) {
                                throw progressUpdateTask.getException();
                            }
                            return updateSpecialMissionBossHp(specialMissionId, finalDamageDealt, userId, username, activityType);
                        });
                    }
                    return Tasks.forResult(null);
                });
    }

    private boolean isSameDay(Date date1, Date date2) {
        if (date1 == null || date2 == null) return false;
        // Use java.util.Calendar for older Android versions
        java.util.Calendar cal1 = java.util.Calendar.getInstance();
        cal1.setTime(date1);
        java.util.Calendar cal2 = java.util.Calendar.getInstance();
        cal2.setTime(date2);
        return cal1.get(java.util.Calendar.YEAR) == cal2.get(java.util.Calendar.YEAR) &&
                cal1.get(java.util.Calendar.MONTH) == cal2.get(java.util.Calendar.MONTH) &&
                cal1.get(java.util.Calendar.DAY_OF_MONTH) == cal2.get(java.util.Calendar.DAY_OF_MONTH);
    }

    public Task<Void> applyDamageAndLogActivity(final String specialMissionId,
                                                final int damageDealt,
                                                final String userId,
                                                final String username,
                                                final String activityType) {
        if (damageDealt <= 0) {
            return Tasks.forResult(null);
        }
        return updateSpecialMissionBossHp(specialMissionId, damageDealt, userId, username, activityType);
    }

    private Task<Void> updateSpecialMissionBossHp(final String specialMissionId,
                                                  final int damageDealt,
                                                  final String userId,
                                                  final String username,
                                                  final String activityType) {
        return db.runTransaction((Transaction.Function<Void>) transaction -> { // Explicit cast to help compiler
            DocumentReference missionRef = specialMissionsCollection.document(specialMissionId);
            SpecialMission mission = transaction.get(missionRef).toObject(SpecialMission.class);

            if (mission == null) {
                // If not found in Firestore, try local (as a fallback, though transaction implies Firestore existence)
                mission = specialMissionDao.getSpecialMissionById(specialMissionId);
                if (mission == null) {
                    throw new RuntimeException("Special mission not found in Firestore or local DB. ID: " + specialMissionId);
                }
            }

            // Create an effectively final copy of the mission for the lambda,
            // or just use the updated mission directly if it's not reassigned later.
            // Since we're modifying the object and then passing it, the object reference
            // 'mission' is effectively final within the transaction lambda scope.
            // The issue was the 'mission = specialMissionDao.get...' line, which is now gone.

            long newHp = mission.getCurrentBossHp() - damageDealt;
            if (newHp < 0) newHp = 0;
            mission.setCurrentBossHp(newHp);

            if (newHp == 0 && !mission.isCompletedSuccessfully()) {
                mission.setCompletedSuccessfully(true);
                mission.setActive(false);
                Log.d(TAG, "Special mission completed successfully!");
            }

            transaction.set(missionRef, mission);

            // Create activity log in Firestore
            String logId = UUID.randomUUID().toString();
            String activityDescription = mapActivityTypeToDescription(activityType);
            MissionActivityLog log = new MissionActivityLog(
                    logId, specialMissionId, userId, username, activityDescription, damageDealt, new Date());
            transaction.set(missionActivityLogsCollection.document(logId), log);

            // After Firestore transaction is prepared, update local DBs asynchronously
            final SpecialMission finalMission = mission; // Make effectively final for the lambda
            executorService.execute(() -> {
                specialMissionDao.update(finalMission);
                Log.d(TAG, "SpecialMission updated locally after Firestore transaction: " + finalMission.getId());
            });

            final MissionActivityLog finalLog = log; // Make effectively final for the lambda
            executorService.execute(() -> {
                if (missionActivityLogDao.getMissionActivityLogById(finalLog.getId()) == null) {
                    missionActivityLogDao.insert(finalLog);
                } else {
                    missionActivityLogDao.update(finalLog);
                }
                Log.d(TAG, "MissionActivityLog inserted/updated locally: " + finalLog.getId());
            });

            return null; // Transaction success
        });
    }


    private String mapActivityTypeToDescription(String activityType) {
        switch (activityType) {
            case "buyInShop":
                return "je kupio nešto u prodavnici";
            case "regularBossHit":
                return "je uspešno udario regularnog bosa";
            case "easyNormalImportantTask":
                return "je rešio zadatak";
            case "otherTasks":
                return "je rešio ostale zadatke";
            case "noUnresolvedTasks":
                return "nije imao nerešenih zadataka";
            case "allianceMessage":
                return "je poslao poruku u savezu";
            default:
                return "je izvršio akciju";
        }
    }

    public Task<List<MissionActivityLog>> getMissionActivityLogs(String specialMissionId) {
        return Tasks.call(executorService, () -> {
            List<MissionActivityLog> localLogs = missionActivityLogDao.getLogsForSpecialMission(specialMissionId);
            if (!localLogs.isEmpty()) {
                Log.d(TAG, "MissionActivityLogs found in local DB for mission: " + specialMissionId);
                return localLogs;
            }
            Log.d(TAG, "MissionActivityLogs NOT found locally, fetching from Firestore for mission: " + specialMissionId);
            return null;
        }).continueWithTask(localTask -> {
            List<MissionActivityLog> localResult = localTask.getResult();
            if (localResult != null) {
                return Tasks.forResult(localResult);
            }
            return missionActivityLogsCollection
                    .whereEqualTo("specialMissionId", specialMissionId)
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .get()
                    .continueWithTask(firestoreTask -> {
                        List<MissionActivityLog> logs = new ArrayList<>();
                        if (firestoreTask.isSuccessful()) {
                            for (QueryDocumentSnapshot document : firestoreTask.getResult()) {
                                MissionActivityLog log = document.toObject(MissionActivityLog.class);
                                logs.add(log);
                                executorService.execute(() -> {
                                    if (missionActivityLogDao.getMissionActivityLogById(log.getId()) == null) {
                                        missionActivityLogDao.insert(log);
                                    } else {
                                        missionActivityLogDao.update(log);
                                    }
                                });
                            }
                            Log.d(TAG, "MissionActivityLogs fetched from Firestore and cached locally for mission: " + specialMissionId);
                        } else {
                            Log.e(TAG, "Error getting mission activity logs from Firestore: " + firestoreTask.getException());
                        }
                        return Tasks.forResult(logs);
                    });
        });
    }

    public Task<Void> awardSpecialMissionCompletion(String specialMissionId, List<UserMissionProgress> allUserProgress, long nextRegularBossRewardCoins) {
        return specialMissionsCollection.document(specialMissionId).get().continueWithTask(task -> {
            if (!task.isSuccessful() || !task.getResult().exists()) {
                throw new Exception("Special mission not found.");
            }
            SpecialMission mission = task.getResult().toObject(SpecialMission.class);

            if (mission == null || !mission.isCompletedSuccessfully()) {
                throw new Exception("Mission not successfully completed or not found.");
            }

            List<Task<Void>> awardTasks = new ArrayList<>();
            for (UserMissionProgress userProgress : allUserProgress) {
                int totalTasksCompleted = userProgress.calculateTotalTasksCompleted();
                String badgeImageUrl = generateBadgeImage(totalTasksCompleted);

                Map<String, Object> updates = new HashMap<>();
                updates.put("potions", com.google.firebase.firestore.FieldValue.increment(1));
                updates.put("coins", com.google.firebase.firestore.FieldValue.increment(nextRegularBossRewardCoins / 2));
                updates.put("badges." + specialMissionId, badgeImageUrl);

                awardTasks.add(db.collection("users").document(userProgress.getUserId()).update(updates));
            }

            Map<String, Object> missionUpdates = new HashMap<>();
            missionUpdates.put("rewardsAwarded", true);
            awardTasks.add(specialMissionsCollection.document(specialMissionId).update(missionUpdates));

            return Tasks.whenAll(awardTasks).continueWithTask(allAwardsTask -> {
                if (!allAwardsTask.isSuccessful()) {
                    throw allAwardsTask.getException();
                }
                return Tasks.call(executorService, () -> {
                    mission.setRewardsAwarded(true);
                    specialMissionDao.update(mission);
                    Log.d(TAG, "SpecialMission local DB updated with rewardsAwarded: " + specialMissionId);
                    return null;
                });
            });
        });
    }

    private String generateBadgeImage(int totalTasksCompleted) {
        if (totalTasksCompleted >= 25) {
            return "https://example.com/badges/gold_badge.png";
        } else if (totalTasksCompleted >= 15) {
            return "https://example.com/badges/silver_badge.png";
        } else if (totalTasksCompleted >= 5) {
            return "https://example.com/badges/bronze_badge.png";
        } else {
            return "https://example.com/badges/participation_badge.png";
        }
    }

    public Task<List<SpecialMission>> getAllPastSpecialMissionsForAlliance(String allianceId) {
        return specialMissionsCollection
                .whereEqualTo("allianceId", allianceId)
                .whereEqualTo("isActive", false)
                .orderBy("startTime", Query.Direction.DESCENDING)
                .get()
                .continueWith(task -> {
                    List<SpecialMission> missions = new ArrayList<>();
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            missions.add(document.toObject(SpecialMission.class));
                        }
                    }
                    return missions;
                });
    }
}