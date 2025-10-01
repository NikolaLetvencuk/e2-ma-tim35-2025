package com.example.dailyboss.domain.enums;

public enum TaskDifficulty {
    VERY_EASY(1),
    EASY(3),
    HARD(7),
    EXTREME(20);

    private final int xpValue;

    TaskDifficulty(int xpValue) {
        this.xpValue = xpValue;
    }

    public int getXpValue() {
        return xpValue;
    }
}