package com.ray.blacksparks.entity;

public class PlayerStats {
    public int strength;
    public int resilience;
    public int ceOutput;

    public int staminaMax;
    public double staminaRegen;
    public double stamina;

    public int ceMax;
    public double ceRegen;
    public double ce;

    public int attributePoints = 0;
    public boolean firstBlackFlash = false;
    public int blackFlashCount = 0;

    public int buffStrength = 0;
    public int buffResilience = 0;
    public int buffCeOutput = 0;
    public double buffStaminaRegen = 0;
    public double buffCeRegen = 0;

    public boolean inZone = false;
    public int zoneTier = 0; //1 or 2
    public int zoneTimer = 0;

    public PlayerStats() {
        setDefaultValues();
    }

    public void setDefaultValues() {
        strength = 1;
        resilience = 1;
        ceOutput = 8;

        staminaMax = 50;
        staminaRegen = 0.2;
        stamina = staminaMax;

        ceMax = 50;
        ceRegen = 0.1; //6 CE per seconds
        ce = ceMax;
    }

    public int getStrength() { return strength + buffStrength; }
    public int getResilience() { return resilience + buffResilience; }
    public int getCeOutput() { return ceOutput + buffCeOutput; }

    public double getStaminaRegen() { return staminaRegen + buffStaminaRegen; }
    public double getCeRegen() { return ceRegen + buffCeRegen; }

    public void regen() {
        if(inZone) {
            zoneTimer--;
            if(zoneTimer <= 0) {
                deactivateZone();
            }
        }
        if(stamina < staminaMax) {
            stamina += getStaminaRegen();
            if(stamina > staminaMax) stamina = staminaMax;
        }
        if(ce < ceMax) {
            ce += getCeRegen();
            if(ce > ceMax) ce = ceMax;
        }
    }

    public boolean consumeStamina(int amount) {
        if(stamina >= amount) {
            stamina -= amount;
            return true;
        }
        return false;
    }

    public boolean consumeCE(int amount) {
        if(ce >= amount) {
            ce -= amount;
            return true;
        }
        return false;
    }

    public void upgrade(int buildType) {
        if(attributePoints <= 0) return;

        switch(buildType) {
            case 0:
                strength += 2;
                resilience += 2;
                staminaRegen += 0.1;
                staminaMax += 15;
                break;
            case 1:
                ceOutput += 3;
                ceRegen += 0.1;
                break;
            case 2:
                ceMax += 20;
                ceRegen += 0.05;
                ceOutput += 1;
                break;
        }
        attributePoints--;

        //stamina = staminaMax;
        //ce = ceMax;
    }

    public void activateZone(int tier) {
        inZone = true;
        zoneTier = tier;
        zoneTimer = 600;

        if(tier == 1) {
            buffStrength = (int)(strength * 0.10) + 1;
            buffResilience = (int)(resilience * 0.10) + 1;
            buffCeOutput = (int)(ceOutput * 0.10) + 2;

            buffStaminaRegen = 0.3;
            buffCeRegen = 0.1;
        }
        else if(tier == 2) {
            buffStrength = (int)(strength * 0.15) + 2;
            buffResilience = (int)(resilience * 0.15) + 2;
            buffCeOutput = (int)(ceOutput * 0.15) + 5;

            buffStaminaRegen = 0.5;
            buffCeRegen = 0.3;
        }
    }

    public void deactivateZone() {
        inZone = false;
        zoneTier = 0;

        buffStrength = 0;
        buffResilience = 0;
        buffCeOutput = 0;
        buffStaminaRegen = 0;
        buffCeRegen = 0;
    }
}
