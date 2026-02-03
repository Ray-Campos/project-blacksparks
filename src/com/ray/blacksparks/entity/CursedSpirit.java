package com.ray.blacksparks.entity;

import com.ray.blacksparks.main.GamePanel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Objects;

public class CursedSpirit extends Enemy {

    private final int RUN_FRAME_COUNT = 8;
    private final int ATTACK_FRAME_COUNT = 8;
    private BufferedImage[] runSprites;
    private BufferedImage[] attackSprites;

    public int attackCooldown = 0;

    public CursedSpirit(GamePanel gp, int worldX, int worldY) {
        super(gp);
        this.worldX = worldX;
        this.worldY = worldY;

        name = "Cursed Spirit";
        speed = 2;
        maxLife = 50;
        life = maxLife;
        attackPower = 20;
        expReward = 50;

        solidArea = new Rectangle(12, 12, 40, 52);

        spriteWidth = 96;
        spriteHeight = 84;

        getImage();
    }

    public void getImage() {
        runSprites = setupSprites("/enemy/curse/run.png", RUN_FRAME_COUNT, 2);
        attackSprites = setupSprites("/enemy/curse/attack.png", ATTACK_FRAME_COUNT, 2);

        this.drawOffsetX = solidArea.x + (solidArea.width - spriteWidth) / 2;
        //drawOffsetX = 40;
        drawOffsetY = -130;
    }

    @Override
    public void setAction() {
        if(attacking) return;

        int dist = Math.abs(gp.player.worldX - worldX);
        int distY = Math.abs(gp.player.worldY - worldY);
        int aggroRange = gp.tileSize * 6;
        int attackRange = gp.tileSize * 1;

        if(dist < attackRange && distY < gp.tileSize) {
            if(attackCooldown == 0) {
                attacking = true;
                spriteCounter = 0;
                spriteNum = 0;
            }
        }
        else if(dist < aggroRange) {
            if(gp.player.worldX < worldX) direction = "left";
            else direction = "right";
        }
        else {
            //may clip on the ground when changing direction
            lockCounter++;
            if(lockCounter > 60) {
                if(direction.equals("left")) direction = "right";
                else direction = "left";
                lockCounter = 0;
            }
        }

        //cooldown Tick
        if(attackCooldown > 0) attackCooldown--;
    }

    @Override
    public void updateAnimation() {
        spriteCounter++;

        if(attacking) {
            int frameSpeed = 5;
            if(spriteCounter > frameSpeed) {
                spriteNum++;
                spriteCounter = 0;

                if(spriteNum == 7 || spriteNum == 8) {
                    checkAttackCollision();
                }

                if(spriteNum >= ATTACK_FRAME_COUNT) {
                    attacking = false;
                    spriteNum = 0;
                    attackCooldown = 60;
                }
            }
            if(attackSprites != null && spriteNum < ATTACK_FRAME_COUNT) {
                image = attackSprites[spriteNum];
            }
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

    @Override
    public void checkPlayerCollision() {}

    public void checkAttackCollision() {
        int attackX = (direction.equals("left")) ? worldX - 38 : worldX + 58;
        Rectangle attackBox = new Rectangle(attackX, worldY, 60, 80);

        //this.debugAttackBox = attackBox;N

        Rectangle playerBox = new Rectangle(gp.player.worldX + gp.player.solidArea.x,
                gp.player.worldY + gp.player.solidArea.y,
                gp.player.solidArea.width,
                gp.player.solidArea.height);

        if(attackBox.intersects(playerBox)) {
            gp.player.takeDamage(attackPower, false);
            //if(gp.player.worldX < worldX) gp.player.worldX -= 1;
            //else gp.player.worldX += 1;
        }
    }
}