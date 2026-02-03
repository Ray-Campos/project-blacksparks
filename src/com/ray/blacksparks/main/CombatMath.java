package com.ray.blacksparks.main;

import com.ray.blacksparks.entity.PlayerStats;

public class CombatMath {

    public static boolean rollBlackFlash(PlayerStats p) {
        double chance = 0.025;//0.025

        if(p.zoneTier == 1) chance = 0.05;//0.05

        if(p.zoneTier == 2) chance = 0.075;//0.075

        return Math.random() < chance;
    }

    public static int calcLightAttackDamage(PlayerStats p) {
        //formula: 1x(Melee) + 0.5x(CE Output)
        return (int)(p.getStrength() * 1.0 + p.getCeOutput() * 0.5);
    }

    public static int calcStrongAttackDamage(PlayerStats p) {
        //formula: 2x(Melee) + 0.5x(CE Current) + 0.5x(CE Output)
        return (int)(p.getStrength() * 2.0 + p.ce * 0.1 + p.getCeOutput() * 0.5);
    }

    public static int calcDefense(PlayerStats p, int incomingDamage) {
        //formula: 1x(Res) + 0.25x(CE Current) + 0.25x(CE Output)
        int mitigation = (int)(p.getResilience() * 1.0 + p.ce * 0.25 + p.getCeOutput() * 0.25);

        int finalDamage = incomingDamage - mitigation;
        if(finalDamage < 0) finalDamage = 0;
        return finalDamage;
    }

    public static int getLightAttackCost() {
        return 10;
    }

    public static int getStrongAttackCost() {
        return 25;
    }

    public static int getDashCost(PlayerStats p) {
        return (int)(p.staminaMax * 0.20);
    }

    public static int getBlockCostPerFrame(PlayerStats p) {
        int cost = (int)(p.ceMax * 0.02);
        if(cost < 1) cost = 1;
        return cost;
    }

    public static int getGenerationCost(int type) {
        switch(type) {
            case 1: return 30;
            case 2: return 50;
            default: return 0;
        }
    }

    public static int calcOrbDamage(PlayerStats p, int orbState) {
        double output = p.ceOutput;

        switch(orbState) {
            case 1: return (int)(output * 1.0); //blue: 1x
            case 3: return (int)(output * 1.5); //max blue: 1.5x
            case 5: return (int)(output * 3.0); //red: 3x
            case 6: return (int)(output * 6.0); //purple: 9x
            default: return 0;
        }
    }

    public static int calcHealAmount(PlayerStats p, int orbState) {
        double output = p.ceOutput;

        switch(orbState) {
            case 2: return (int)(output * 1.0); //RCT: 1x
            case 4: return (int)(output * 1.5); //max RCT: 1.5x
            default: return 0;
        }
    }
}
