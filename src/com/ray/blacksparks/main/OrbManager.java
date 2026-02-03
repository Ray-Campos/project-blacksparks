package com.ray.blacksparks.main;

import com.ray.blacksparks.entity.CursedOrb;
import com.ray.blacksparks.entity.Player;
import com.ray.blacksparks.entity.Projectile;
import java.awt.Color;
import java.awt.Graphics2D;

public class OrbManager {

    GamePanel gp;
    Player player;
    ChantLogic chantLogic;

    //0=Empty, 1=Blue, 2=RCT, 3=Blue+Blue, 4=RCT+RCT, 5=RED, 6=PURPLE
    public int orbState = 0;
    private int inputDelay = 0;
    private int bobCounter = 0;

    public OrbManager(GamePanel gp, Player player) {
        this.gp = gp;
        this.player = player;
        this.chantLogic = new ChantLogic(gp, player);
    }

    public void update() {
        if(gp.keyH.keyV && orbState != 0) {
            player.chanting = true;
            chantLogic.update(orbState);
        }
        else {
            player.chanting = false;
            chantLogic.reset();
        }

        if(inputDelay > 0) inputDelay--;
        if(inputDelay == 0) {

            if(gp.keyH.key1) {
                tryAddBlue();
                inputDelay = 15;
            }

            if(gp.keyH.key2) {
                tryAddRCT();
                inputDelay = 15;
            }

            if(gp.keyH.key3) {
                if(orbState != 6) {
                    orbState = 0;
                } else {
                }
                inputDelay = 15;
            }

            if(gp.keyH.keyC) {
                fireTechnique();
                inputDelay = 30;
            }
        }
        bobCounter++;
    }

    private void tryAddBlue() {
        int cost = CombatMath.getGenerationCost(1);

        if(player.stats.consumeCE(cost)) {
            switch(orbState) {
                case 0: orbState = 1; break; //Empty -> Blue
                case 1: orbState = 3; break; //Blue -> Max Blue
                case 2: orbState = 5; break; //RCT -> Red
                case 5: orbState = 6; break; //Red -> Purple
            }
            //gp.playSE(9);
        } else {
        }
    }

    private void tryAddRCT() {
        int cost = CombatMath.getGenerationCost(2);

        if(player.stats.consumeCE(cost)) {
            switch(orbState) {
                case 0: orbState = 2; break; //Empty -> RCT
                case 1: orbState = 5; break; //Blue -> Red
                case 2: orbState = 4; break; //RCT -> Max RCT
                //case 5: orbState = 0; break;
            }
            //gp.playSE(9);
        } else {
        }
    }

    private void fireTechnique() {
        if(orbState == 0) return;
        int type = 1;//Blue
        if(orbState == 5) type = 2; //Red
        if(orbState == 6) type = 3; //Purple

        if(orbState == 2 || orbState == 4) {
            int healAmount = CombatMath.calcHealAmount(player.stats, orbState);
            player.heal(healAmount);

        } else {
            CursedOrb p = new CursedOrb(gp, type);
            int spawnY = player.worldY - 16;
            p.owner = player;

            int damage = CombatMath.calcOrbDamage(player.stats, orbState);
            int knockback = 0;
            if(chantLogic.chantComplete) {
                damage = (int)(damage * 2.0);
                p.solidArea.width += 30;
            }

            switch(orbState) {
                case 1: //Blue
                    knockback = 2;
                    p.color = Color.BLUE;
                    p.speed = 12;
                    break;
                case 3: //Max Blue
                    knockback = 4;
                    p.color = Color.BLUE;
                    p.speed = 16;
                    break;
                case 5: //Red
                    knockback = 40;
                    p.color = Color.RED;
                    p.speed = 8;
                    break;
                case 6: //Purple
                    knockback = 0;
                    p.color = new Color(128, 0, 128);
                    p.speed = 6;
                    p.penetrates = true;
                    break;
            }
            p.set(player.worldX, spawnY, player.direction, true, damage, knockback);
            gp.projectileList.add(p);
            //gp.playSE(2);
        }
        orbState = 0;
        chantLogic.reset();
        player.chanting = false;
    }

    public void draw(Graphics2D g2) {
        if(orbState == 0) return;

        int bobY = (int)(Math.sin(bobCounter * 0.1) * 5); //5px float range

        int playerScreenX = player.worldX - gp.cameraX;
        int playerScreenY = player.worldY - gp.cameraY;

        int centerX = playerScreenX + (player.solidArea.x + player.solidArea.width/2);
        int centerY = playerScreenY;

        int leftShoulderX = centerX - 30;
        int rightShoulderX = centerX + 16;

        switch(orbState) {
            case 1: drawOrb(g2, Color.BLUE, leftShoulderX, centerY + bobY); break;
            case 2: drawOrb(g2, Color.WHITE, leftShoulderX, centerY + bobY); break;
            case 3: //2 Blues
                drawOrb(g2, Color.BLUE, leftShoulderX, centerY + bobY);
                drawOrb(g2, Color.BLUE, rightShoulderX, centerY - bobY);
                break;
            case 4: //2 RCTs
                drawOrb(g2, Color.WHITE, leftShoulderX, centerY + bobY);
                drawOrb(g2, Color.WHITE, rightShoulderX, centerY - bobY);
                break;
            case 5: //RED
                int shakeX = (int)(Math.random()*4 - 2);
                int shakeY = (int)(Math.random()*4 - 2);
                drawOrb(g2, Color.RED, leftShoulderX + shakeX, centerY + shakeY);
                break;
            case 6: //PURPLE
                shakeX = (int)(Math.random()*8 - 4);
                shakeY = (int)(Math.random()*8 - 4);
                drawOrb(g2, new Color(128, 0, 128), leftShoulderX + shakeX, centerY + shakeY);
                break;
        }
    }

    private void drawOrb(Graphics2D g2, Color c, int x, int y) {
        g2.setColor(c);
        g2.fillOval(x, y, 12, 12); //12px Orb
        g2.setColor(Color.WHITE);
        g2.drawOval(x, y, 12, 12); //outlin
    }

    public void reset() { orbState = 0; }
}
