package com.example.dailyboss.domain.model;

import java.util.Date;
import java.util.Calendar;

public class UserMissionProgress {

    private static final int MAX_BUY_IN_SHOP = 5;
    private static final int MAX_REGULAR_BOSS_HIT = 10;
    private static final int MAX_EASY_NORMAL_IMPORTANT_TASK = 10;
    private static final int MAX_OTHER_TASKS = 6;
    private static final int HP_PER_MESSAGE_DAY = 4; // HP za svaki dan kada je poruka poslata
    private static final int HP_NO_UNRESOLVED_TASKS = 10; // HP za bez nerešenih zadataka

    private String id;
    private String specialMissionId;
    private String userId;
    private String username;
    private int buyInShopCount;
    private int regularBossHitCount;
    private int easyNormalImportantTaskCount;
    private int otherTasksCount;
    private boolean noUnresolvedTasksCompleted;
    private Date lastMessageSentDate;
    private int messageSentDaysCount; // NOVO: Broj dana kada je poruka poslata

    public UserMissionProgress() {}

    public UserMissionProgress(String id, String specialMissionId, String userId, String username) {
        this.id = id;
        this.specialMissionId = specialMissionId;
        this.userId = userId;
        this.username = username;

        this.buyInShopCount = 0;
        this.regularBossHitCount = 0;
        this.easyNormalImportantTaskCount = 0;
        this.otherTasksCount = 0;
        this.noUnresolvedTasksCompleted = false;
        this.lastMessageSentDate = null;
        this.messageSentDaysCount = 0; // Inicijalizacija novog polja
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSpecialMissionId() { return specialMissionId; }
    public void setSpecialMissionId(String specialMissionId) { this.specialMissionId = specialMissionId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public int getBuyInShopCount() { return buyInShopCount; }
    public void setBuyInShopCount(int buyInShopCount) { this.buyInShopCount = buyInShopCount; }

    public int getRegularBossHitCount() { return regularBossHitCount; }
    public void setRegularBossHitCount(int regularBossHitCount) { this.regularBossHitCount = regularBossHitCount; }

    public int getEasyNormalImportantTaskCount() { return easyNormalImportantTaskCount; }
    public void setEasyNormalImportantTaskCount(int easyNormalImportantTaskCount) { this.easyNormalImportantTaskCount = easyNormalImportantTaskCount; }

    public int getOtherTasksCount() { return otherTasksCount; }
    public void setOtherTasksCount(int otherTasksCount) { this.otherTasksCount = otherTasksCount; }

    public boolean isNoUnresolvedTasksCompleted() { return noUnresolvedTasksCompleted; }
    public void setNoUnresolvedTasksCompleted(boolean noUnresolvedTasksCompleted) { this.noUnresolvedTasksCompleted = noUnresolvedTasksCompleted; }

    public Date getLastMessageSentDate() { return lastMessageSentDate; }
    public void setLastMessageSentDate(Date lastMessageSentDate) { this.lastMessageSentDate = lastMessageSentDate; }

    // NOVO: Getter i Setter za messageSentDaysCount
    public int getMessageSentDaysCount() { return messageSentDaysCount; }
    public void setMessageSentDaysCount(int messageSentDaysCount) { this.messageSentDaysCount = messageSentDaysCount; }

    public boolean incrementBuyInShopCount() {
        if (buyInShopCount < MAX_BUY_IN_SHOP) {
            buyInShopCount++;
            return true;
        }
        return false;
    }

    public boolean incrementRegularBossHitCount() {
        if (regularBossHitCount < MAX_REGULAR_BOSS_HIT) {
            regularBossHitCount++;
            return true;
        }
        return false;
    }

    public boolean incrementEasyNormalImportantTaskCount(int weight) {
        int actualWeight = Math.max(1, weight);

        if (easyNormalImportantTaskCount + actualWeight <= MAX_EASY_NORMAL_IMPORTANT_TASK) {
            easyNormalImportantTaskCount += actualWeight;
            return true;
        }
        if (easyNormalImportantTaskCount < MAX_EASY_NORMAL_IMPORTANT_TASK) {
            easyNormalImportantTaskCount = MAX_EASY_NORMAL_IMPORTANT_TASK;
            return true;
        }
        return false;
    }

    public boolean incrementOtherTasksCount() {
        if (otherTasksCount < MAX_OTHER_TASKS) {
            otherTasksCount++;
            return true;
        }
        return false;
    }

    /**
     * Povećava brojač poslatih poruka za misiju ako poruka nije već poslata danas.
     *
     * @return true ako je poruka uspešno zabeležena za današnji dan, false ako je već poslata danas.
     */
    public boolean incrementDailyMessage() {
        Date now = new Date();

        // Proveravamo da li je poruka već poslata danas
        if (lastMessageSentDate != null && isSameDay(lastMessageSentDate, now)) {
            return false; // Poruka je već poslata danas
        }

        // Zabeleži da je poruka poslata danas i poveća brojač dana
        lastMessageSentDate = now;
        messageSentDaysCount++; // Povećava brojač dana kada je poruka poslata
        return true;
    }

    private boolean isSameDay(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);

        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    public int calculateTotalDamageDealt() {
        int damage = 0;
        damage += getBuyInShopCount() * 2;
        damage += getRegularBossHitCount() * 2;
        damage += getEasyNormalImportantTaskCount() * 1;
        damage += getOtherTasksCount() * 4;

        if (isNoUnresolvedTasksCompleted()) {
            damage += HP_NO_UNRESOLVED_TASKS;
        }

        // NOVO: Izračunaj HP od poruka na osnovu broja dana kada je poruka poslata
        damage += getMessageSentDaysCount() * HP_PER_MESSAGE_DAY;

        return damage;
    }

    public int calculateTotalTasksCompleted() {
        int tasks = 0;
        tasks += getBuyInShopCount();
        tasks += getRegularBossHitCount();
        tasks += (getEasyNormalImportantTaskCount() / 1);
        tasks += getOtherTasksCount();
        if (isNoUnresolvedTasksCompleted()) {
            tasks += 1;
        }
        // Dodajemo dane sa poslatim porukama kao doprinos zadacima
        tasks += getMessageSentDaysCount();
        return tasks;
    }
}