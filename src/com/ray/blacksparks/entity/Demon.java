package com.ray.blacksparks.entity;

import com.ray.blacksparks.main.GamePanel;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Demon extends Enemy {

    private final int FRAME_WIDTH = 80;
    private final int FRAME_HEIGHT = 80;

    private final int IDLE_FRAMES = 6;
    private final int RUN_FRAMES = 10;
    private final int ATTACK_FRAMES = 11;
    private final int DEATH_FRAMES = 9; // Assuming long death

    private BufferedImage[] idleSprites;
    private BufferedImage[] runSprites;
    private BufferedImage[] attackSprites;
    private BufferedImage[] deathSprites;

    public boolean dying = false;
    public int attackCooldown = 0;

    public Demon(GamePanel gp, int worldX, int worldY) {
        super(gp);
        this.worldX = worldX;
        this.worldY = worldY;

        name = "Demon";

        maxLife = 400;
        life = maxLife;
        attackPower = 30;
        expReward = 500;
        speed = 2;

        solidArea = new Rectangle(16, 16, 48, 96);
        //solidArea = new Rectangle(16, 16, 48, 48);

        getImage();
    }

    public void getImage() {
        int scale = 2;

        idleSprites = setupBossSprites("/enemy/demon/idle.png", 0, IDLE_FRAMES, scale);
        runSprites = setupBossSprites("/enemy/demon/run.png", 1, RUN_FRAMES, scale);
        attackSprites = setupBossSprites("/enemy/demon/attack.png", 2, ATTACK_FRAMES, scale);
        deathSprites = setupBossSprites("/enemy/demon/death.png", 4, DEATH_FRAMES, scale);

        this.drawOffsetX = solidArea.x + (solidArea.width - spriteWidth) / 2;
        //drawOffsetX = -120;
        drawOffsetY = -200;
    }

    private BufferedImage[] setupBossSprites(String path, int row, int frameCount, int scale) {
        return super.setupSprites(path, frameCount, scale);
    }

    @Override
    public void setAction() {
        if(dying) return;
        if(attacking) return;

        int dist = Math.abs(gp.player.worldX - worldX);

        if(gp.player.worldX < worldX) direction = "left";
        else direction = "right";

        int range = gp.tileSize * 2;
        if(dist < range) {
            if(attackCooldown == 0) {
                attacking = true;
                spriteCounter = 0;
                spriteNum = 0;
            }
        }
        if(attackCooldown > 0) attackCooldown--;
    }

    @Override
    public void updateAnimation() {
        spriteCounter++;
        int frameSpeed = 6;// lower is faster

        if(dying) {
            if(spriteCounter > frameSpeed) {
                spriteNum++;
                spriteCounter = 0;

                if(spriteNum >= DEATH_FRAMES) {
                    alive = false; // FINALLY DEAD
                    gp.ui.showMessage("SPECIAL GRADE EXORCISED");
                }
            }
            if(deathSprites != null && spriteNum < DEATH_FRAMES) image = deathSprites[spriteNum];
        }
        else if(attacking) {
            if(spriteCounter > frameSpeed) {
                spriteNum++;
                spriteCounter = 0;

                if(spriteNum == 8 || spriteNum == 9) {
                    checkAttackCollision();
                }
                if(spriteNum >= ATTACK_FRAMES) {
                    attacking = false;
                    spriteNum = 0;
                    attackCooldown = 60;
                }
            }
            if(attackSprites != null && spriteNum < ATTACK_FRAMES) image = attackSprites[spriteNum];
        }
        else {
            if(spriteCounter > frameSpeed) {
                spriteNum++;
                if(spriteNum >= RUN_FRAMES) spriteNum = 0;
                spriteCounter = 0;
            }
            if(runSprites != null) image = runSprites[spriteNum];
        }
    }

    @Override
    public void draw(Graphics2D g2) {
        int screenX = worldX - gp.cameraX;
        int screenY = worldY - gp.cameraY;

        if(worldX + gp.tileSize*4 > gp.cameraX &&
                worldX - gp.tileSize*4 < gp.cameraX + gp.screenWidth &&
                worldY + gp.tileSize*4 > gp.cameraY &&
                worldY - gp.tileSize*4 < gp.cameraY + gp.screenHeight) {

            if (image != null) {
                int finalDrawX = screenX + drawOffsetX;
                int finalDrawY = screenY + drawOffsetY;

                if (direction.equals("right")) {
                    g2.drawImage(image, finalDrawX + spriteWidth, finalDrawY, -spriteWidth, spriteHeight, null);
                } else {
                    g2.drawImage(image, finalDrawX, finalDrawY, spriteWidth, spriteHeight, null);
                }
            }
            // should use parents one
            //g2.setColor(Color.RED);
            //g2.drawRect(screenX + solidArea.x, screenY + solidArea.y, solidArea.width, solidArea.height);

            /* attack box
            if(debugAttackBox != null) {
                int screenAttackX = debugAttackBox.x - gp.cameraX;
                int screenAttackY = debugAttackBox.y - gp.cameraY;
                g2.setColor(new Color(255, 255, 0, 150));
                g2.fillRect(screenAttackX, screenAttackY, debugAttackBox.width, debugAttackBox.height);
                debugAttackBox = null;
            }*/
        }
    }

    @Override
    public void checkPlayerCollision() {}

    public void checkAttackCollision() {
        // HUGE SWORD HITBOX
        int attackW = 140;
        int attackH = 150;

        int attackX;

        if(direction.equals("left")) {
            attackX = worldX - attackW + 32;
        } else {
            int bodyRightEdge = worldX + solidArea.x + solidArea.width;

            attackX = bodyRightEdge - 20;
        }

        Rectangle attackBox = new Rectangle(attackX, worldY - 40, attackW, attackH);
        this.debugAttackBox = attackBox;

        if(attackBox.intersects(getPlayerRect())) {
            gp.player.takeDamage(attackPower, false);

            if(gp.player.worldX < worldX) gp.player.worldX -= 10;
            else gp.player.worldX += 10;
        }
    }

    @Override
    public void takeDamage(int damage) {
        if(dying) return;

        super.takeDamage(damage);

        if(life <= 0) {
            life = 0;
            speed = 0;
            dying = true;
            alive = true;
            collisionOn = false;
            gp.tileM.openDemonGate();
            gp.demonGateOpen = true;
            gp.ui.showMessage("THE PATH IS OPEN");
        }
    }

    private Rectangle getPlayerRect() {
        return new Rectangle(gp.player.worldX + gp.player.solidArea.x,
                gp.player.worldY + gp.player.solidArea.y,
                gp.player.solidArea.width,
                gp.player.solidArea.height);
    }
}