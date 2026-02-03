package com.ray.blacksparks.entity;

import com.ray.blacksparks.main.GamePanel;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;

public class FlyingEye extends Enemy {

    private final int FLIGHT_FRAMES = 8;
    private final int ATTACK_FRAMES = 8;
    private BufferedImage[] flightSprites;
    private BufferedImage[] attackSprites;

    public FlyingEye(GamePanel gp, int worldX, int worldY) {
        super(gp);
        this.worldX = worldX;
        this.worldY = worldY;

        name = "Flying Eye";
        speed = 3;
        maxLife = 20;
        life = maxLife;
        attackPower = 15;
        expReward = 50;

        gravity = 0;//to fly

        solidArea = new Rectangle(4, 4, 32, 32);
        solidAreaDefaultX = solidArea.x;
        solidAreaDefaultY = solidArea.y;

        getImage();
    }

    @Override
    public void getImage() {
        flightSprites = setupSprites("/enemy/eye/flight.png", FLIGHT_FRAMES, 2);
        attackSprites = setupSprites("/enemy/eye/attack.png", ATTACK_FRAMES, 2);

        drawOffsetX = -135;
        drawOffsetY = -135;
    }

    @Override
    public void setAction() {
        if(attacking) return;

        int dist = Math.abs(gp.player.worldX - worldX);
        int distY = Math.abs(gp.player.worldY - worldY);

        if(dist < gp.tileSize * 10) {

            if(gp.player.worldX < worldX) direction = "left";
            else direction = "right";

            if(distY > 10) {
                if(gp.player.worldY < worldY) velocityY = -2;
                else velocityY = 2;
            } else {
                velocityY = 0;
            }

            if(dist < gp.tileSize * 1.0) {
                attacking = true;
                spriteCounter = 0;
                spriteNum = 0;
            }

        } else {
            velocityY = 0;

            lockCounter++;
            if(lockCounter == 120) { //2 seconds
                if(direction.equals("left")) direction = "right";
                else direction = "left";
                lockCounter = 0;
            }
        }
    }

    @Override
    public void updateAnimation() {
        if(attacking) {
            spriteCounter++;
            if(spriteCounter > 5) {
                spriteNum++;
                spriteCounter = 0;

                if(spriteNum == 7 || spriteNum == 8) {
                    checkAttackCollision();
                }

                if(spriteNum >= ATTACK_FRAMES) {
                    attacking = false;
                    spriteNum = 0;
                }
            }
            if(attackSprites != null) image = attackSprites[spriteNum];
        }
        else {
            spriteCounter++;
            if(spriteCounter > 6) {
                spriteNum++;
                if(spriteNum >= FLIGHT_FRAMES) spriteNum = 0;
                spriteCounter = 0;
            }
            if(flightSprites != null) image = flightSprites[spriteNum];
        }
    }

    public void checkAttackCollision() {
        int attackRange = 40;
        int attackX = (direction.equals("left")) ? worldX - attackRange : worldX + solidArea.width;
        int attackY = worldY;

        Rectangle attackBox = new Rectangle(attackX, attackY, attackRange, solidArea.height);
        this.debugAttackBox = attackBox;

        Rectangle playerBox = new Rectangle(gp.player.worldX + gp.player.solidArea.x,
                gp.player.worldY + gp.player.solidArea.y,
                gp.player.solidArea.width,
                gp.player.solidArea.height);

        if(attackBox.intersects(playerBox)) {
            gp.player.takeDamage(attackPower, false);
        }
    }
}