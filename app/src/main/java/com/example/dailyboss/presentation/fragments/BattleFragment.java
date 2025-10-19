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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.dailyboss.R;

import java.util.Locale;
import java.util.Random;

public class BattleFragment extends Fragment implements SensorEventListener {

    private ImageView ivBoss, ivEquipment, ivChest;
    private ProgressBar progressBossHp, progressUserPp;
    private TextView tvAttacksLeft, tvHitChance, tvCoinsWon, tvEquipmentWon;
    private Button btnAttack;
    private View resultPanel;

    private int bossMaxHp = 200;
    private int bossHp;
    private int userPp = 50;
    private int attacksLeft = 5;
    private int bossIndex = 1;
    private int successPercent = 67;

    private Random rng = new Random();

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private long lastShakeTime = 0;
    private static final int SHAKE_WAIT_MS = 500;
    private static final float SHAKE_THRESHOLD = 12f;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_battle, container, false);

        ivBoss = view.findViewById(R.id.ivBoss);
        ivEquipment = view.findViewById(R.id.ivEquipment);
        ivChest = view.findViewById(R.id.ivChest);
        progressBossHp = view.findViewById(R.id.progressBossHp);
        progressUserPp = view.findViewById(R.id.progressUserPp);
        tvAttacksLeft = view.findViewById(R.id.tvAttacksLeft);
        tvHitChance = view.findViewById(R.id.tvHitChance);
        btnAttack = view.findViewById(R.id.btnAttack);
        resultPanel = view.findViewById(R.id.resultPanel);
        tvCoinsWon = view.findViewById(R.id.tvCoinsWon);
        tvEquipmentWon = view.findViewById(R.id.tvEquipmentWon);

        bossHp = bossMaxHp;
        progressBossHp.setMax(bossMaxHp);
        progressBossHp.setProgress(bossHp);
        progressUserPp.setMax(1000);
        progressUserPp.setProgress(userPp);

        updateUi();

        btnAttack.setOnClickListener(v -> performAttack());

        sensorManager = (SensorManager) requireContext().getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

        return view;
    }

    private void updateUi() {
        tvAttacksLeft.setText(String.format(Locale.getDefault(), "Attacks: %d/5", attacksLeft));
        tvHitChance.setText(String.format(Locale.getDefault(), "Hit chance: %d%%", successPercent));
        progressBossHp.setProgress(Math.max(0, bossHp));
        progressUserPp.setProgress(userPp);
    }

    private void performAttack() {
        if (attacksLeft <= 0) {
            Toast.makeText(getContext(), "No attacks left", Toast.LENGTH_SHORT).show();
            return;
        }

        attacksLeft--;
        boolean hit = rng.nextInt(100) < successPercent;
        if (hit) {
            bossHp -= userPp;
            showBossHitAnimation();
            Toast.makeText(getContext(), "Hit! -" + userPp + " HP", Toast.LENGTH_SHORT).show();
        } else {
            showMissAnimation();
            Toast.makeText(getContext(), "Miss!", Toast.LENGTH_SHORT).show();
        }

        if (bossHp < 0) bossHp = 0;
        updateUi();

        if (bossHp == 0) {
            onBossDefeated();
        } else if (attacksLeft == 0) {
            onAttacksExhausted();
        }
    }

    private void showBossHitAnimation() {
        ivBoss.setImageResource(R.drawable.boss_hit);
        ScaleAnimation anim = new ScaleAnimation(
                1f, 0.95f, 1f, 0.95f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        anim.setDuration(120);
        anim.setRepeatCount(1);
        anim.setRepeatMode(Animation.REVERSE);
        ivBoss.startAnimation(anim);

        new Handler().postDelayed(() -> ivBoss.setImageResource(R.drawable.boss_idle), 250);
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
        int coins = calculateCoinsForBoss(bossIndex);
        boolean equipmentDropped = rng.nextInt(100) < 20;
        String equipmentType = null;
        if (equipmentDropped) {
            equipmentType = (rng.nextInt(100) < 95) ? "Clothing" : "Weapon";
        }

        showResult(coins, equipmentType);
    }

    private void onAttacksExhausted() {
        int initialBossHp = bossMaxHp;
        int damageDone = initialBossHp - bossHp;
        boolean halfReduced = damageDone >= (initialBossHp / 2.0);

        int coins = 0;
        String equipmentType = null;

        if (halfReduced) {
            coins = (int) Math.ceil(calculateCoinsForBoss(bossIndex) / 2.0);
            if (rng.nextInt(100) < 10) {
                equipmentType = (rng.nextInt(100) < 95) ? "Clothing" : "Weapon";
            }
        }

        showResult(coins, equipmentType);
    }

    private int calculateCoinsForBoss(int index) {
        double coins = 200.0;
        for (int i = 1; i < index; i++) {
            coins *= 1.2;
        }
        return (int) Math.round(coins);
    }

    private void showResult(int coins, String equipmentType) {
        resultPanel.setVisibility(View.VISIBLE);
        tvCoinsWon.setText("Coins: " + coins);
        tvEquipmentWon.setText("Equipment: " + (equipmentType == null ? "-" : equipmentType));

        ivChest.setImageResource(R.drawable.chest_closed);
        ScaleAnimation shake = new ScaleAnimation(
                1f, 1.05f, 1f, 0.95f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        shake.setDuration(120);
        shake.setRepeatCount(3);
        shake.setRepeatMode(Animation.REVERSE);
        ivChest.startAnimation(shake);

        ivChest.setOnClickListener(v -> openChest(coins, equipmentType));
    }

    private void openChest(int coins, String equipmentType) {
        ivChest.setImageResource(R.drawable.chest_open);
        Toast.makeText(getContext(), "You won " + coins + " coins" + (equipmentType != null ? " and " + equipmentType : ""), Toast.LENGTH_LONG).show();
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

            if (attacksLeft > 0) {
                performAttack();
            } else if (resultPanel.getVisibility() == View.VISIBLE) {
                openChest(
                        Integer.parseInt(tvCoinsWon.getText().toString().replaceAll("\\D+", "")),
                        tvEquipmentWon.getText().toString().replace("Equipment: ", "").equals("-") ? null : tvEquipmentWon.getText().toString().replace("Equipment: ", "")
                );
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}
