package com.ray.blacksparks.data;

import java.io.Serializable;

public class DataStorage implements Serializable {

    public int level;
    public int maxLife;
    public int life;
    public int exp;
    public int nextLevelExp;

    public int strength;
    public int resilience;
    public int ceOutput;

    public int staminaMax;
    public double staminaRegen;
    public double currentStamina;

    public int ceMax;
    public double ceRegen;
    public double currentCE;

    public int attributePoints;
    public int blackFlashCount;
    public boolean firstBlackFlash;

    public String mapFilePath;
    public int worldX;
    public int worldY;
    public String direction;

    //world events
    public boolean bossDefeated;
    public boolean gateOpen;
    public int killCount;
    public boolean demonGateOpen;
}
