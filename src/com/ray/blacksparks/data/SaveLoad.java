package com.ray.blacksparks.data;

import com.ray.blacksparks.main.GamePanel;
import java.io.*;

public class SaveLoad {

    GamePanel gp;

    public SaveLoad(GamePanel gp) {
        this.gp = gp;
    }

    public void save() {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File("save.dat")));
            DataStorage ds = new DataStorage();

            // 1. COPY PLAYER DATA
            ds.level = gp.player.level;
            ds.maxLife = gp.player.maxLife;
            ds.life = gp.player.life;
            ds.exp = gp.player.exp;
            ds.nextLevelExp = gp.player.nextLevelExp;

            //COPY RPG STATS
            ds.strength = gp.player.stats.strength;
            ds.resilience = gp.player.stats.resilience;
            ds.ceOutput = gp.player.stats.ceOutput;

            ds.staminaMax = gp.player.stats.staminaMax;
            ds.staminaRegen = gp.player.stats.staminaRegen;
            ds.currentStamina = gp.player.stats.stamina;

            ds.ceMax = gp.player.stats.ceMax;
            ds.ceRegen = gp.player.stats.ceRegen;
            ds.currentCE = gp.player.stats.ce;

            ds.attributePoints = gp.player.stats.attributePoints;
            ds.blackFlashCount = gp.player.stats.blackFlashCount;
            ds.firstBlackFlash = gp.player.stats.firstBlackFlash;

            ds.killCount = gp.killCount;
            ds.gateOpen = gp.levelComplete;
            ds.bossDefeated = gp.bossDefeated;
            ds.demonGateOpen = gp.demonGateOpen;
            System.out.println("SAVING MAP PATH: " + gp.currentMapPath);
            ds.mapFilePath = gp.currentMapPath;

            ds.worldX = gp.player.worldX;
            ds.worldY = gp.player.worldY;
            ds.direction = gp.player.direction;

            // 3. WRITE FILE
            oos.writeObject(ds);
            oos.close();

            System.out.println("GAME SAVED!");

        } catch (Exception e) {
            System.out.println("SAVE ERROR!");
            e.printStackTrace();
        }
    }

    public void load() {
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File("save.dat")));
            DataStorage ds = (DataStorage) ois.readObject();

            gp.enemyList.clear();
            gp.projectileList.clear();

            gp.player.level = ds.level;
            gp.player.maxLife = ds.maxLife;
            gp.player.life = ds.life;
            gp.player.exp = ds.exp;
            gp.player.nextLevelExp = ds.nextLevelExp;

            gp.player.stats.strength = ds.strength;
            gp.player.stats.resilience = ds.resilience;
            gp.player.stats.ceOutput = ds.ceOutput;

            gp.player.stats.staminaMax = ds.staminaMax;
            gp.player.stats.staminaRegen = ds.staminaRegen;
            gp.player.stats.stamina = ds.currentStamina;

            gp.player.stats.ceMax = ds.ceMax;
            gp.player.stats.ceRegen = ds.ceRegen;
            gp.player.stats.ce = ds.currentCE;

            gp.player.stats.attributePoints = ds.attributePoints;
            gp.player.stats.blackFlashCount = ds.blackFlashCount;
            gp.player.stats.firstBlackFlash = ds.firstBlackFlash;

            gp.killCount = ds.killCount;
            gp.levelComplete = ds.gateOpen;
            gp.bossDefeated = ds.bossDefeated;
            gp.demonGateOpen = ds.demonGateOpen;
            String map = ds.mapFilePath != null ? ds.mapFilePath : "/maps/map01.txt";
            //System.out.println("path: " + ds.mapFilePath);
            gp.tileM.loadMap(map);

            gp.player.worldX = ds.worldX;
            gp.player.worldY = ds.worldY;
            gp.player.direction = ds.direction;

            if(gp.levelComplete) {
                gp.tileM.openGates();
            }

            if(gp.bossDefeated) {
                for(int i = 0; i < gp.enemyList.size(); i++) {
                    if(gp.enemyList.get(i) instanceof com.ray.blacksparks.entity.Boss) {
                        gp.enemyList.remove(i);
                        break;
                    }
                }
            }

            ois.close();
            //System.out.println("loaded");

        } catch (Exception e) {
            System.out.println("no save file found???");
            // e.printStackTrace(); // stack trace
        }
    }
}
