package com.example.dailyboss.service;

import android.content.Context;
import android.util.Log;

import com.example.dailyboss.data.dao.MissionActivityLogDao;
import com.example.dailyboss.data.dao.SpecialMissionDao;
import com.example.dailyboss.data.dao.UserEquipmentDao;
import com.example.dailyboss.data.dao.UserMissionProgressDao;
import com.example.dailyboss.data.dao.UserProfileDao;
import com.example.dailyboss.data.repository.AllianceRepository;
import com.example.dailyboss.data.repository.EquipmentRepository;
import com.example.dailyboss.data.repository.SpecialMissionRepository;
import com.example.dailyboss.data.repository.UserRepository;
import com.example.dailyboss.data.repository.UserStatisticRepository;
import com.example.dailyboss.domain.enums.EquipmentBonusType;
import com.example.dailyboss.domain.enums.EquipmentType;
import com.example.dailyboss.domain.model.Alliance;
import com.example.dailyboss.domain.model.Equipment;
import com.example.dailyboss.domain.model.MissionActivityLog;
import com.example.dailyboss.domain.model.SpecialMission;
import com.example.dailyboss.domain.model.User;
import com.example.dailyboss.domain.model.UserEquipment;
import com.example.dailyboss.domain.model.UserMissionProgress;
import com.example.dailyboss.domain.model.UserStatistic;
import com.google.android.gms.tasks.Tasks;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SpecialMissionService {

    private static final String TAG = "SpecialMissionService";
    private final Context context;
    private final SpecialMissionRepository specialMissionRepository;
    private final AllianceRepository allianceRepository;
    private final UserRepository userRepository;
    private final UserMissionProgressDao userMissionProgressDao;
    private final MissionActivityLogDao missionActivityLogDao;
    private final UserStatisticRepository userStatisticRepository;
    private final EquipmentService equipmentService; // Inject EquipmentService
    private final UserEquipmentDao userEquipmentDao; // To add equipment to user
    private final SpecialMissionDao specialMissionDao;
    private final BattleService battleService;
    private final BadgeService badgeService;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public SpecialMissionService(Context context) {
        this.context = context.getApplicationContext();
        this.specialMissionRepository = new SpecialMissionRepository(context);
        this.specialMissionDao = new SpecialMissionDao(context);
        this.allianceRepository = new AllianceRepository(context);
        this.userRepository = new UserRepository(context);
        this.userMissionProgressDao = new UserMissionProgressDao(context);
        this.missionActivityLogDao = new MissionActivityLogDao(context);
        this.userStatisticRepository = new UserStatisticRepository(context);
        this.equipmentService = new EquipmentService(context);
        this.battleService = new BattleService(context);
        this.userEquipmentDao = new UserEquipmentDao(context);
        this.badgeService = new BadgeService(context);
    }

    /**
     * Checks if a special mission has ended and, if successful, awards rewards to all participating members.
     * This method should be called periodically or when a mission is known to have ended.
     *
     * @param missionId The ID of the special mission to check.
     */
    public void checkAndAwardMissionCompletion(String missionId) {
        executorService.execute(() -> {
            try {
                SpecialMission mission = Tasks.await(specialMissionRepository.getSpecialMissionById(missionId));

                if (mission == null) {
                    Log.e(TAG, "Mission not found: " + missionId);
                    return;
                }
                mission.setRewardsAwarded(false);
                // Check if mission has already been marked as completed and rewards awarded
                if (mission.isRewardsAwarded()) {
                    Log.d(TAG, "Rewards already awarded for mission: " + missionId);
                    return;
                }

                Date now = new Date();
                boolean missionEnded = now.after(mission.getEndTime());

                if (!missionEnded && !mission.isCompletedSuccessfully()) {
                    Log.d(TAG, "Mission " + missionId + " ended successfully. Awarding rewards...");
                    awardMissionRewards(mission);

                    // Mark mission as rewards awarded to prevent re-awarding
                    mission.setRewardsAwarded(true);
                    specialMissionDao.update(mission);
                } else if (!missionEnded && mission.isCompletedSuccessfully()) {
                    Log.d(TAG, "Mission " + missionId + " ended unsuccessfully. No rewards.");
                    // Optionally, update mission status for unsuccessful completion
                    mission.setRewardsAwarded(true); // Still mark as handled
                    specialMissionDao.update(mission);
                } else {
                    Log.d(TAG, "Mission " + missionId + " is still active or not yet completed.");
                }

            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Error checking or awarding mission completion for " + missionId + ": " + e.getMessage());
            }
        });
    }

    private void awardMissionRewards(SpecialMission mission) {
        try {
            Alliance alliance = Tasks.await(allianceRepository.getAllianceById(mission.getAllianceId()));
            if (alliance == null) {
                Log.e(TAG, "Alliance not found for mission: " + mission.getId());
                return;
            }

            List<String> memberUserIds = new ArrayList<>();
            List<User> users = new ArrayList<>(userRepository.getLocalUsersByAllianceId(alliance.getId()));
            for (User user : users) {
                memberUserIds.add(user.getId());
            }
            if (alliance.getLeaderId() != null && !memberUserIds.contains(alliance.getLeaderId())) {
                memberUserIds.add(alliance.getLeaderId());
            }

            for (String userId : memberUserIds) {
                User user = userRepository.getLocalUser(userId);
                if (user == null) {
                    Log.e(TAG, "User not found with ID: " + userId);
                    continue;
                }

                UserMissionProgress userProgress = userMissionProgressDao.getUserMissionProgressForUserAndMission(userId, mission.getId());

                // Only award if user participated in the mission (has progress entry)
                if (userProgress != null) {
                    awardIndividualUserRewards(user, mission, userProgress);
                } else {
                    Log.d(TAG, "User " + user.getUsername() + " did not participate in mission " + mission.getId() + ". No rewards.");
                }
            }
        } catch (ExecutionException | InterruptedException e) {
            Log.e(TAG, "Error awarding mission rewards for mission " + mission.getId() + ": " + e.getMessage());
        }
    }

    private void awardIndividualUserRewards(User user, SpecialMission mission, UserMissionProgress userProgress) {
        String userId = user.getId();
        String username = user.getUsername();

        Log.d(TAG, "Awarding rewards for user: " + username + " in mission: " + mission.getId());

        // 1. Award one random potion and one random piece of apparel
        List<Equipment> randomRewards = equipmentService.getRandomMissionRewards();
        for (Equipment reward : randomRewards) {
            if (reward != null) {
                UserEquipment existingUserEquipment = userEquipmentDao.getUserSpecificEquipment(userId, reward.getId());
                if (existingUserEquipment != null) {
                    // If user already has this equipment, increase quantity
                    existingUserEquipment.setQuantity(existingUserEquipment.getQuantity() + 1);
                    userEquipmentDao.upsert(existingUserEquipment);
                    Log.d(TAG, "Increased quantity of " + reward.getName() + " for " + username);
                } else {
                    // Create new UserEquipment entry
                    UserEquipment newUserEquipment = new UserEquipment(
                            UUID.randomUUID().toString(),
                            userId,
                            reward.getId(),
                            1, // quantity
                            false, // not active yet
                            reward.getDurationBattles(), // duration from default equipment
                            System.currentTimeMillis(), // activation timestamp
                            reward.getBonusValue() // current bonus value
                    );
                    userEquipmentDao.upsert(newUserEquipment);
                    Log.d(TAG, "Awarded new equipment: " + reward.getName() + " to " + username);
                }
            }
        }

        // 2. Award 50% of the coins from the next regular boss's victory reward
        UserStatistic userStatistic = userStatisticRepository.getUserStatistic(userId);
        if (userStatistic != null) {
            int currentLevel = userStatistic.getLevel();
            int nextBossReward = battleService.calculateCoinsForBoss(currentLevel + 1, userId); // Reward for the next level boss
            int missionCoinReward = (int) Math.round(nextBossReward * 0.50);

            userStatistic.setCoins(userStatistic.getCoins() + missionCoinReward);
            userStatisticRepository.saveOrUpdate(userStatistic);

            // Log coin reward
            String logId = UUID.randomUUID().toString();
            String description = String.format(Locale.getDefault(), "Dobijena nagrada (novčići): %d", missionCoinReward);
            MissionActivityLog activityLog = new MissionActivityLog(
                    logId,
                    mission.getId(),
                    userId,
                    username,
                    description,
                    0, // No HP change
                    new Date()
            );
            missionActivityLogDao.insert(activityLog);
            Log.d(TAG, "Awarded " + missionCoinReward + " coins to " + username);
        } else {
            Log.e(TAG, "User statistics not found for user: " + userId + ". Cannot award coins.");
        }

        executorService.execute(() -> {
            badgeService.awardSpecialTaskBadges(userId, userProgress.calculateTotalTasksCompleted());
        });

        // TODO: Implement badge awarding once badge system is defined
        // userProgress.setMissionBadgesAwarded(true); // Example flag
        // userMissionProgressDao.update(userProgress);
    }
}