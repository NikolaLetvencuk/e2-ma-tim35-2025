package com.example.dailyboss.presentation.fragments;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.animation.Transformation;
import android.widget.Button; // Ostaje ako se negde koristi, ali je Attack dugme ImageButton
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import com.example.dailyboss.R;
import com.example.dailyboss.data.SharedPreferencesHelper;
import com.example.dailyboss.data.dao.MissionActivityLogDao;
import com.example.dailyboss.data.dao.UserMissionProgressDao;
import com.example.dailyboss.data.repository.AllianceRepository;
import com.example.dailyboss.data.repository.SpecialMissionRepository;
import com.example.dailyboss.data.repository.UserRepository;
import com.example.dailyboss.data.repository.UserStatisticRepository;
import com.example.dailyboss.domain.model.Alliance;
import com.example.dailyboss.domain.model.MissionActivityLog;
import com.example.dailyboss.domain.model.User;
import com.example.dailyboss.domain.model.UserEquipment;
import com.example.dailyboss.domain.model.UserMissionProgress;
import com.example.dailyboss.service.BattleService;
import com.example.dailyboss.domain.model.UserStatistic;
import com.google.android.gms.tasks.Tasks;
// Nema potrebe za Equipment i UserEquipment ako se ne koriste direktno

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Executors;


public class BattleFragment extends Fragment implements SensorEventListener {

    // Ažurirane deklaracije: ivEquipment UKLONJEN, ivBoss ZAMENJEN sa ivBossAnimation
    private LottieAnimationView ivBossAnimation;
    private ImageView ivCoinIconResultPanel, ivChest, ivUserAvatar, ivAttackIcon;
    private ProgressBar progressBossHp, progressUserPp;
    private TextView tvAttacksLeft, tvHitChance, tvCoinsWon, tvEquipmentWon, tvShakeInstruction, tvAttackResult;
    private TextView tvBossHpValue, tvUserPpValue, tvBossName;
    private ImageButton btnAttack;
    private ImageView ivPotentialRewardChest, ivPotentialCoinIcon;
    private TextView tvPotentialCoins;
    private ImageView rewardFrame;
    private View resultPanel; // Glavni uzrok prethodnog NullPointer-a
    private LinearLayout llActiveEquipment;
    private int bossMaxHp = 200;
    private int bossHp;
    private int userPp = 50;
    private int attacksLeft = 5;
    private int bossIndex = 1;
    private int successPercent = 95;
    private boolean chestOpened = false;
    private String selectedEquipmentId = null;
    private String selectedEquipmentName = null;
    private String equipmentRewardType = null; // Dodata varijabla za tip opreme koju smo osvojili

    private Random rng = new Random();

    private UserStatisticRepository userStatisticRepository;
    private AllianceRepository allianceRepository;
    private com.example.dailyboss.data.repository.EquipmentRepository equipmentRepository;
    private BattleService battleService;
    private String currentUserId;
    private UserMissionProgressDao progressDao;
    private MissionActivityLogDao activityLogDao;
    private SpecialMissionRepository specialMissionRepository;
    private UserRepository userRepository;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private long lastShakeTime = 0;
    private static final int SHAKE_WAIT_MS = 500;
    private static final float SHAKE_THRESHOLD = 2f;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_battle, container, false);

        // Inicijalizacija UI elemenata
        ivBossAnimation = view.findViewById(R.id.ivBossAnimation); // Za GIF animaciju bossa
        progressBossHp = view.findViewById(R.id.progressBossHp);
        progressUserPp = view.findViewById(R.id.progressUserPp);
        tvAttacksLeft = view.findViewById(R.id.tvAttacksLeft);
        tvHitChance = view.findViewById(R.id.tvHitChance);
        tvBossHpValue = view.findViewById(R.id.tvBossHpValue);
        tvUserPpValue = view.findViewById(R.id.tvUserPpValue); // KORISTIMO TVUSERPPVALUE IZ NOVOG XML-a
        btnAttack = view.findViewById(R.id.btnAttack);
        ivUserAvatar = view.findViewById(R.id.ivUserAvatar);
        tvBossName = view.findViewById(R.id.tvBossName);
        tvAttackResult = view.findViewById(R.id.tvAttackResult); // Inicijalizuj tvAttackResult
        ivAttackIcon = view.findViewById(R.id.ivAttackIcon);     // Inicijalizuj ivAttackIcon
        llActiveEquipment = view.findViewById(R.id.llActiveEquipment); // Dodaj ovo

        // INICIJALIZACIJA REZULTAT PANELA (UZROK PROBLEMA):
        resultPanel = view.findViewById(R.id.resultPanel);
        tvCoinsWon = view.findViewById(R.id.tvCoinsWon);
        tvEquipmentWon = view.findViewById(R.id.tvEquipmentWon);
        ivChest = view.findViewById(R.id.ivChest);
        tvShakeInstruction = view.findViewById(R.id.tvShakeInstruction);
        ivCoinIconResultPanel = view.findViewById(R.id.ivCoinIconResultPanel);
        rewardFrame = view.findViewById(R.id.reward_frame); // Ako želiš da referenciraš ceo frame
        ivPotentialRewardChest = view.findViewById(R.id.ivPotentialRewardChest);
        tvPotentialCoins = view.findViewById(R.id.tvPotentialCoins);
        ivPotentialCoinIcon = view.findViewById(R.id.ivPotentialCoinIcon);
        // KRAJ INICIJALIZACIJE REZULTAT PANELA

        // Učitaj GIF za bossa
        loadBossIdleAnimation();

        // Postavi default avatar ili učitaj iz SharedPreferencesHelper
        ivUserAvatar.setImageResource(R.drawable.avatar_1);

        bossHp = bossMaxHp;
        progressBossHp.setMax(bossMaxHp);
        progressBossHp.setProgress(bossHp);
        progressUserPp.setMax(1000);
        progressUserPp.setProgress(userPp);

        userStatisticRepository = new UserStatisticRepository(getContext());
        equipmentRepository = new com.example.dailyboss.data.repository.EquipmentRepository(getContext());
        battleService = new BattleService(getContext());
        progressDao = new UserMissionProgressDao(getContext());
        activityLogDao = new MissionActivityLogDao(getContext());
        allianceRepository = new AllianceRepository(getContext());
        specialMissionRepository = new SpecialMissionRepository(getContext());
        userRepository = new UserRepository(getContext());
        SharedPreferencesHelper prefs = new SharedPreferencesHelper(getContext());
        currentUserId = prefs.getLoggedInUserId();
        android.util.Log.d("BattleFragment", "onCreateView: currentUserId = " + currentUserId);

        if (currentUserId == null) {
            android.util.Log.e("BattleFragment", "currentUserId is null in onCreateView, trying to get from arguments");
            if (getArguments() != null) {
                currentUserId = getArguments().getString("userId");
                android.util.Log.d("BattleFragment", "Got currentUserId from arguments: " + currentUserId);
            }
        }

        // Sad kad je currentUserId (valjda) postavljen, učitaj podatke
        loadUserData();

        btnAttack.setOnClickListener(v -> performAttack());

        sensorManager = (SensorManager) requireContext().getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

        return view;
    }

    // Metoda za učitavanje GIF-a bossa
    private void loadBossIdleAnimation() {
        if (ivBossAnimation != null) {
            ivBossAnimation.setAnimation("boss_idle.json");
            ivBossAnimation.playAnimation();
            ivBossAnimation.loop(true);
        }
    }

    private void showAttackResult(boolean hit) {
        // Resetuj vidljivost i animacije
        tvAttackResult.setVisibility(View.GONE);
        ivAttackIcon.setVisibility(View.GONE);
        tvAttackResult.clearAnimation();
        ivAttackIcon.clearAnimation();

        // Pripremi animaciju
        Animation fadeOutUp = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                float translateY = -150 * interpolatedTime; // Pomeri gore za 150dp
                float alpha = 1 - interpolatedTime; // Nestaje
                t.getMatrix().setTranslate(0, translateY);
                t.setAlpha(alpha);
            }
        };
        fadeOutUp.setDuration(800); // Traje 0.8 sekundi
        fadeOutUp.setFillAfter(true); // Održi finalno stanje (gone)
        fadeOutUp.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // Nema potrebe za posebnim radnjama na startu
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // Sakrij element nakon završetka animacije
                tvAttackResult.setVisibility(View.GONE);
                ivAttackIcon.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // Nema potrebe za posebnim radnjama na ponavljanju
            }
        });


        if (hit) {
            tvAttackResult.setText("HIT!");
            tvAttackResult.setTextColor(getResources().getColor(android.R.color.holo_orange_light)); // Zlatno-narandžasta za HIT
            tvAttackResult.setVisibility(View.VISIBLE);
            tvAttackResult.startAnimation(fadeOutUp);

            ivAttackIcon.setImageResource(R.drawable.ic_sword_attack); // Koristi postojeću ikonicu za mač
            ivAttackIcon.setImageTintList(AppCompatResources.getColorStateList(requireContext(), android.R.color.holo_orange_light));
            ivAttackIcon.setVisibility(View.VISIBLE);
            ivAttackIcon.startAnimation(fadeOutUp); // Ikonica se animira zajedno sa tekstom

        } else {
            tvAttackResult.setText("MISS!");
            tvAttackResult.setTextColor(getResources().getColor(android.R.color.darker_gray)); // Tamno siva za MISS
            tvAttackResult.setVisibility(View.VISIBLE);
            tvAttackResult.startAnimation(fadeOutUp);

            // Možeš dodati drugu ikonicu za MISS, npr. prekriženi mač ili oblak dima
            // Za sada, neka bude ista ikonica ali tamnija.
            ivAttackIcon.setImageResource(R.drawable.ic_sword_attack);
            ivAttackIcon.setImageTintList(AppCompatResources.getColorStateList(requireContext(), android.R.color.darker_gray));
            ivAttackIcon.setVisibility(View.VISIBLE);
            ivAttackIcon.startAnimation(fadeOutUp);
        }
    }

    private void loadUserData() {
        android.util.Log.d("BattleFragment", "loadUserData: currentUserId = " + currentUserId);

        if (currentUserId == null) {
            android.util.Log.e("BattleFragment", "currentUserId is null in loadUserData");
            return;
        }

        UserStatistic stats = userStatisticRepository.getUserStatistic(currentUserId);
        android.util.Log.d("BattleFragment", "Retrieved UserStatistic for userId: " + currentUserId + ", stats: " + (stats != null ? "found" : "null"));

        if (stats != null) {
            android.util.Log.d("BattleFragment", "UserStatistic details: level=" + stats.getLevel() + ", XP=" + stats.getTotalXPPoints() + ", PP=" + stats.getPowerPoints());
            userPp = stats.getPowerPoints();

            // Apply equipment bonuses (skraćeno radi preglednosti)
            int oldPp = userPp;
            userPp = userPp + battleService.applyEquipmentBonuses(userPp, currentUserId);
            successPercent = battleService.calculateSuccessRateForCurrentStage(currentUserId, bossIndex) + battleService.applyHitChanceBonuses(95, currentUserId);
            attacksLeft = 5 + battleService.applyBootsBonus(5, currentUserId);

            progressUserPp.setMax(userPp);
            progressUserPp.setProgress(userPp);

            bossIndex = stats.getLevel();
            bossMaxHp = battleService.calculateBossHp(bossIndex);
            bossHp = bossMaxHp;


            progressBossHp.setMax(bossMaxHp);
            progressBossHp.setProgress(bossHp);

            chestOpened = false;
            selectedEquipmentId = null;
            selectedEquipmentName = null;
            equipmentRewardType = null; // Resetuj i tip opreme

            // Ažuriraj UI nakon svih kalkulacija
            updateUi();
            displayActiveEquipment(); // POZIV ZA PRIKAZ OPREME
        } else {
            android.util.Log.e("BattleFragment", "UserStatistic not found for userId: " + currentUserId);
            // Postavi neke default vrednosti
            bossMaxHp = 200;
            bossHp = bossMaxHp;
            userPp = 50;
            attacksLeft = 5;
            successPercent = 95;
            progressBossHp.setMax(bossMaxHp);
            progressBossHp.setProgress(bossHp);
            progressUserPp.setMax(1000);
            progressUserPp.setProgress(userPp);
            updateUi();
            // displayActiveEquipment(); // Možda i ovde ako želite prikazati prazan slot
        }
    }

    private void displayActiveEquipment() {
        if (llActiveEquipment == null) {
            android.util.Log.e("BattleFragment", "llActiveEquipment is null, cannot display equipment.");
            return;
        }
        llActiveEquipment.removeAllViews(); // Ukloni sve prethodno dodate ikonice

        List<UserEquipment> activeEquipment = equipmentRepository.getAllUserEquipmentForUser(currentUserId);
        android.util.Log.d("BattleFragment", "Found " + activeEquipment.size() + " equipment items for user: " + currentUserId);

        for (UserEquipment userEq : activeEquipment) {
            if (userEq.isActive()) {
                android.util.Log.d("BattleFragment", "Displaying active equipment: " + userEq.getEquipmentId());
                ImageView equipmentIcon = new ImageView(getContext());
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        (int) getResources().getDimension(R.dimen.equipment_icon_size), // Define in dimens.xml
                        (int) getResources().getDimension(R.dimen.equipment_icon_size)
                );
                params.setMarginEnd((int) getResources().getDimension(R.dimen.equipment_icon_margin)); // Define in dimens.xml
                equipmentIcon.setLayoutParams(params);
                equipmentIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);
                equipmentIcon.setBackgroundResource(R.drawable.equipment_icon_background_fight);

                // Dodatno, možeš postaviti padding unutar ikonice da se ne preklapa sa ivicama pozadine
                int padding = (int) getResources().getDimension(R.dimen.equipment_icon_padding); // Definiši ovu dimenziju
                equipmentIcon.setPadding(padding, padding, padding, padding);
                // Postavi ikonicu na osnovu equipmentId-a
                int iconResId = getEquipmentIconResId(userEq.getEquipmentId());
                if (iconResId != 0) {
                    equipmentIcon.setImageResource(iconResId);
                    llActiveEquipment.addView(equipmentIcon);
                } else {
                    android.util.Log.w("BattleFragment", "No icon found for equipment ID: " + userEq.getEquipmentId());
                }
            }
        }
    }

    private int getEquipmentIconResId(String equipmentId) {
        switch (equipmentId) {
            case "gloves": return R.drawable.ic_gloves; // Kreiraj ove drawable-e
            case "shield": return R.drawable.ic_shield;
            case "boots": return R.drawable.ic_boots;
            case "sword": return R.drawable.ic_sword;
            case "bow": return R.drawable.ic_bow;
            case "potion_20_pp":
            case "potion_40_pp":
            case "potion_60_pp":
            case "potion_80_pp":
            case "potion_5_permanent":
            case "potion_10_permanent":
                return R.drawable.ic_potion1; // Generic potion icon
            default: return 0;
        }
    }

    private void updateUi() {
        // Ažurirano na "NAPADI"
        tvAttacksLeft.setText(String.format(Locale.getDefault(), "NAPADI: %d/5", attacksLeft));
        // Ažurirano na "Šansa za pogodak"
        tvHitChance.setText(String.format(Locale.getDefault(), "Šansa za pogodak: %d%%", successPercent));
        progressBossHp.setProgress(Math.max(0, bossHp));
        progressUserPp.setProgress(userPp);

        // Ažuriraj HP i PP tekstove
        tvBossHpValue.setText(String.format(Locale.getDefault(), "%d/%d", bossHp, bossMaxHp));
        // tvUserPpValue odgovara ID-u tvUserPpValue u XML-u.
        tvUserPpValue.setText(String.format(Locale.getDefault(), "PP %d", userPp));
        int potentialCoins = battleService.calculateCoinsForBoss(bossIndex, currentUserId); // Ili neka druga logika
        tvPotentialCoins.setText(String.valueOf(potentialCoins));
    }

    private void performAttack() {
        if (attacksLeft <= 0) {
            Toast.makeText(getContext(), "Nema više napada", Toast.LENGTH_SHORT).show();
            return;
        }

        if (bossHp <= 0) {
            Toast.makeText(getContext(), "Boss je već poražen!", Toast.LENGTH_SHORT).show();
            return;
        }

        attacksLeft--;
        boolean hit = rng.nextInt(100) < successPercent;
        if (hit) {
            bossHp -= userPp;
            showBossHitAnimation(); // Tvoja postojeća animacija bossa
            showAttackResult(true); // Prikazi "HIT!" tekst i ikonicu
            Executors.newSingleThreadExecutor().execute(() -> {
                        logRegularBossHit(currentUserId);
            });
            Toast.makeText(getContext(), "Pogodak! -" + userPp + " HP", Toast.LENGTH_SHORT).show();
        } else {
            showMissAnimation(); // Tvoja postojeća vibracija za promašaj
            showAttackResult(false); // Prikazi "MISS!" tekst i ikonicu
            Toast.makeText(getContext(), "Promašaj!", Toast.LENGTH_SHORT).show();
        }

        if (bossHp < 0) bossHp = 0;
        updateUi();

        if (bossHp == 0) {
            onBossDefeated();
        } else if (attacksLeft == 0) {
            onAttacksExhausted();
        }
    }

    public void logRegularBossHit(String userId) {
        User user = userRepository.getLocalUser(userId);

        if (user == null || user.getAllianceId() == null) {
            return;
        }

        try {
            Alliance alliance = Tasks.await(allianceRepository.getAllianceById(user.getAllianceId()));

            if (alliance != null && alliance.getActiveSpecialMissionId() != null) {

                UserMissionProgress progress = progressDao.getUserMissionProgressForUserAndMission(userId, alliance.getActiveSpecialMissionId()); // Pretpostavka da BattleService ima progressDao

                if (progress != null) {

                    boolean incremented = progress.incrementRegularBossHitCount();

                    if (incremented) {
                        progressDao.update(progress);

                        String logId = UUID.randomUUID().toString();
                        String description = "Uspešan udarac u regularnoj borbi sa bosom";
                        Date currentTime = new Date();
                        int damage = 2;

                        MissionActivityLog missionActivityLog = new MissionActivityLog(
                                logId,
                                alliance.getActiveSpecialMissionId(),
                                userId,
                                user.getUsername(),
                                description,
                                damage,
                                currentTime
                        );

                        activityLogDao.insert(missionActivityLog);
                        specialMissionRepository.applyDamageAndLogActivity(alliance.getActiveSpecialMissionId(), damage, userId, user.getUsername(), "regularBossHit");
                        Log.d("BattleService", "Mission activity logged for RegularBossHit: " + description);
                    }
                }
            }
        } catch (Exception e) {
            Log.e("BattleService", "Error logging boss hit for mission: " + e.getMessage());
        }
    }

    private void showBossHitAnimation() {
        // Ako želite posebnu "hit" animaciju, možete da učitate drugu Lottie animaciju
        // ili da privremeno zaustavite idle animaciju i prikažete flash ili sl.
        // Za jednostavan flash efekat preko Lottie animacije:
        if (ivBossAnimation != null) {
            ivBossAnimation.pauseAnimation(); // Privremeno zaustavi idle animaciju

            ScaleAnimation anim = new ScaleAnimation(
                    1f, 0.95f, 1f, 0.95f,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f);
            anim.setDuration(120);
            anim.setRepeatCount(1);
            anim.setRepeatMode(Animation.REVERSE);
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    // Možda dodati neki flash drawable ovde
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    ivBossAnimation.playAnimation(); // Nastavi idle animaciju nakon "hita"
                }

                @Override
                public void onAnimationRepeat(Animation animation) {}
            });
            ivBossAnimation.startAnimation(anim);
        }
    }

    private void showMissAnimation() {
        Vibrator v = (Vibrator) requireContext().getSystemService(Context.VIBRATOR_SERVICE);
        if (v != null) {
            if (android.os.Build.VERSION.SDK_INT >= 26) {
                v.vibrate(VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                v.vibrate(80);
            }
        }
    }

    private void onBossDefeated() {
        int coins = battleService.calculateCoinsForBoss(bossIndex, currentUserId);

        // Određivanje tipa opreme sa 95% šanse za odeću, 5% za oružje
        if (rng.nextInt(100) < 20) { // 20% šanse za dobijanje opreme
            equipmentRewardType = (rng.nextInt(100) < 95) ? "CLOTHING" : "WEAPON"; // 95% za odeću, 5% za oružje
            selectedEquipmentId = battleService.getRandomEquipmentId(equipmentRewardType);
            selectedEquipmentName = battleService.getEquipmentNameById(selectedEquipmentId);
        } else {
            equipmentRewardType = null;
            selectedEquipmentId = null;
            selectedEquipmentName = null;
        }

        showResult(coins, equipmentRewardType); // Prosleđujemo tip opreme
        battleService.decrementEquipmentDuration(currentUserId);
    }

    private void onAttacksExhausted() {
        int initialBossHp = bossMaxHp;
        int damageDone = initialBossHp - bossHp;
        boolean halfReduced = damageDone >= (initialBossHp / 2.0);

        int coins = (int) Math.ceil(battleService.calculateCoinsForBoss(bossIndex, currentUserId) / 2.0);

        equipmentRewardType = null; // Resetuj za svaki slučaj
        selectedEquipmentId = null;
        selectedEquipmentName = null;

        if (halfReduced) {
            // Ako je umanjeno 50% HP-a, postoji 20% šanse za opremu (umanjeno upola na 10% u finalnom kodu)
            if (rng.nextInt(100) < 10) { // 10% šanse za dobijanje opreme
                equipmentRewardType = (rng.nextInt(100) < 95) ? "CLOTHING" : "WEAPON"; // Opet, 95% za odeću, 5% za oružje
                selectedEquipmentId = battleService.getRandomEquipmentId(equipmentRewardType);
                selectedEquipmentName = battleService.getEquipmentNameById(selectedEquipmentId);
            }
        }

        showResult(coins, equipmentRewardType); // Prosleđujemo tip opreme
        battleService.decrementEquipmentDuration(currentUserId);
    }


    private void showResult(int coins, String equipmentType) {
        // Postavi tekst, ali ga NE prikazuj (biće prikazan nakon otvaranja kovčega)
        tvCoinsWon.setText(String.format("Novčići: %d", coins));
        String equipmentName = (selectedEquipmentName != null && !selectedEquipmentName.isEmpty()) ? selectedEquipmentName : "-";
        tvEquipmentWon.setText(String.format("Oprema: %s", equipmentName));

        // Sakrij nagrade pre otvaranja
        tvCoinsWon.setVisibility(View.INVISIBLE);
        tvEquipmentWon.setVisibility(View.INVISIBLE);
        ivCoinIconResultPanel.setVisibility(View.INVISIBLE);
        // Prikazi uputstvo za shake
        tvShakeInstruction.setVisibility(View.VISIBLE);

        // Prikaži panel i zatvoreni kovčeg
        resultPanel.setVisibility(View.VISIBLE);
        ivChest.setImageResource(R.drawable.chest_closed);

        // Animacija zatresanja (vizuelna komponenta da se "nešto dešava")
        ScaleAnimation shake = new ScaleAnimation(
                1f, 1.05f, 1f, 0.95f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        shake.setDuration(120);
        shake.setRepeatCount(3);
        shake.setRepeatMode(Animation.REVERSE);
        ivChest.startAnimation(shake);

        // Ukloni onClick slušaoca ako ga je bilo
        ivChest.setOnClickListener(null);
    }


    private void openChest() {
        if (chestOpened) {
            return;
        }

        chestOpened = true;

        // Sakrij uputstvo za shake
        tvShakeInstruction.setVisibility(View.GONE);

        // Prikaži otvoreni kovčeg i opcionalno zvuk/konfete
        ivChest.setImageResource(R.drawable.chest_open);
        // Opcionalno: Pusti zvuk otvaranja kovčega i animiraj konfete

        // Prikaži osvojene nagrade
        tvCoinsWon.setVisibility(View.VISIBLE);
        tvEquipmentWon.setVisibility(View.VISIBLE);
        ivCoinIconResultPanel.setVisibility(View.VISIBLE);


        // Parsiranje vrednosti iz tekstualnih polja
        int coins = 0;
        try {
            // Zamena svih ne-cifara (uključujući "Novčići: ") praznim stringom i parsovanje
            String coinsText = tvCoinsWon.getText().toString().replaceAll("[^\\d]", "");
            if (!coinsText.isEmpty()) {
                coins = Integer.parseInt(coinsText);
            }
        } catch (NumberFormatException e) {
            android.util.Log.e("BattleFragment", "Greška pri parsovanju novčića: " + e.getMessage());
        }

        // Logika za dodavanje novčića i opreme
        if (currentUserId != null) {
            UserStatistic stats = userStatisticRepository.getUserStatistic(currentUserId);
            if (stats != null) {
                stats.setCoins(stats.getCoins() + coins);
                userStatisticRepository.saveOrUpdate(stats);
                android.util.Log.d("BattleFragment", "Added " + coins + " coins. New total: " + stats.getCoins());
            } else {
                android.util.Log.e("BattleFragment", "UserStatistic is null when trying to add coins.");
            }
        }

        if (selectedEquipmentId != null && currentUserId != null) {
            battleService.addSpecificEquipment(selectedEquipmentId, currentUserId);
            android.util.Log.d("BattleFragment", "Added equipment: " + selectedEquipmentName + " for user: " + currentUserId);
        }

        String equipmentMessage = "";
        if (selectedEquipmentName != null && !selectedEquipmentName.equals("-")) {
            equipmentMessage = " i " + selectedEquipmentName;
        }

        Toast.makeText(getContext(), "Osvojili ste " + coins + " novčića" + equipmentMessage, Toast.LENGTH_LONG).show();
    }


    @Override
    public void onResume() {
        super.onResume();
        if (accelerometer != null && sensorManager != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (accelerometer != null && sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER) return;

        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        float gX = x / SensorManager.GRAVITY_EARTH;
        float gY = y / SensorManager.GRAVITY_EARTH;
        float gZ = z / SensorManager.GRAVITY_EARTH;

        float gForce = (float) Math.sqrt(gX * gX + gY * gY + gZ * gZ);

        if (gForce > SHAKE_THRESHOLD) {
            final long now = System.currentTimeMillis();
            if (lastShakeTime + SHAKE_WAIT_MS > now) {
                return;
            }
            lastShakeTime = now;

            if (attacksLeft > 0 && resultPanel.getVisibility() != View.VISIBLE) {
                // Shake za napad (samo ako borba nije završena)
                performAttack();
            } else if (resultPanel != null && resultPanel.getVisibility() == View.VISIBLE && !chestOpened) {
                // Shake za otvaranje kovčega (ako je borba završena i kovčeg nije otvoren)
                openChest();

            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}