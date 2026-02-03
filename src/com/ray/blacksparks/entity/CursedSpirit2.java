package com.ray.blacksparks.entity;

import com.ray.blacksparks.main.GamePanel;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

public class CursedSpirit2 extends Enemy {

    private final int RUN_FRAME_COUNT = 8;
    private final int ATTACK_FRAME_COUNT = 11;
    private BufferedImage[] runSprites;
    private BufferedImage[] attackSprites;

    public CursedSpirit2(GamePanel gp, int worldX, int worldY) {
        super(gp);
        this.worldX = worldX;
        this.worldY = worldY;

        name = "Cursed Spirit (Ranged)";
        speed = 2;
        maxLife = 30;
        life = maxLife;
        attackPower = 10;
        expReward = 80;

        // same Hitbox
        solidArea = new Rectangle(12, 12, 40, 52);

        getImage();
    }

    @Override
    public void getImage() {
        runSprites = setupSprites("/enemy/curse/run.png", RUN_FRAME_COUNT, 2);
        attackSprites = setupSprites("/enemy/curse/attack2.png", ATTACK_FRAME_COUNT, 2);

        this.drawOffsetX = solidArea.x + (solidArea.width - spriteWidth) / 2;
        //drawOffsetX = 40;
        drawOffsetY = -130;
    }

    @Override
    public void setAction() {
        if(attacking) return;

        int dist = Math.abs(gp.player.worldX - worldX);
        int distY = Math.abs(gp.player.worldY - worldY);

        if(dist < gp.tileSize * 8) { //too high will bug

            if(gp.player.worldX < worldX) direction = "left";
            else direction = "right";

            actionLockCounter++;

            if(actionLockCounter > 90) {
                attacking = true;
                spriteCounter = 0;
                spriteNum = 0;
                actionLockCounter = 0;
            }
        }
        else {
            lockCounter++;
            if(lockCounter == 120) {
                if(direction.equals("left")) direction = "right";
                else direction = "left";
                lockCounter = 0;
            }
        }
    }

    @Override
    public void updateAnimation() {
        spriteCounter++;
        if(attacking) {
            spriteCounter++;
            if(spriteCounter > 5) {
                spriteNum++;
                spriteCounter = 0;

                if(spriteNum == 8) {
                    shootProjectile();
                }

                if(spriteNum >= ATTACK_FRAME_COUNT) {
                    attacking = false;
                    spriteNum = 0;
                }
            }
            if(attackSprites != null) image = attackSprites[spriteNum];
        }
        else {
            int frameSpeed = 10;
            if(spriteCounter > frameSpeed) {
                spriteNum++;
                if(spriteNum >= RUN_FRAME_COUNT) spriteNum = 0;
                spriteCounter = 0;
            }
            if(runSprites != null) {
                image = runSprites[spriteNum];
            }
        }
    }

    public void shootProjectile() {
        CursedOrb orb = new CursedOrb(gp, 4);

        //use center of enemy as firing point
        int enemyCenterX = worldX + solidArea.x + (solidArea.width / 2);
        int enemyCenterY = worldY + solidArea.y + (solidArea.height / 2);

        //high enough to not hit the floor
        int startX = enemyCenterX - 16;
        int startY = enemyCenterY - 16 - 15;

        int targetX = gp.player.worldX + gp.player.solidArea.x + gp.player.solidArea.width / 2;
        int targetY = gp.player.worldY + gp.player.solidArea.y + gp.player.solidArea.height / 2;

        orb.setVector(startX, startY, targetX, targetY);
        orb.damage = 15;
        orb.owner = this;

        gp.projectileList.add(orb);
    }
}