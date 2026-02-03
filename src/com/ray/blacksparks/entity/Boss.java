package com.ray.blacksparks.entity;

import com.ray.blacksparks.main.GamePanel;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Boss extends Enemy {

    private final int FRAME_WIDTH = 80;
    private final int FRAME_HEIGHT = 80;

    private final int IDLE_FRAMES = 9;
    private final int RUN_FRAMES = 6;
    private final int ATTACK_FRAMES = 12;
    private final int DEATH_FRAMES = 20;

    private BufferedImage[] idleSprites;
    private BufferedImage[] runSprites;
    private BufferedImage[] attackSprites;
    private BufferedImage[] deathSprites;

    public boolean dying = false;
    public boolean enraging = false;
    public int attackCooldown = 0;

    public boolean daActive = false;
    public int daTimer = 0;
    public int daCooldown = 0;

    public Boss(GamePanel gp, int worldX, int worldY) {
        super(gp);
        this.worldX = worldX;
        this.worldY = worldY;

        name = "NightBorne";

        maxLife = 1000;
        life = maxLife;
        attackPower = 50;
        expReward = 1000;

        speed = 3;

        solidArea = new Rectangle(16, 16, 48, 96); //this may cause issues
        //solidArea = new Rectangle(16, 16, 48, 48);

        getImage();
    }

    public void getImage() {
        int scale = 3;

        idleSprites = setupBossSprites("/enemy/boss/idle.png", 0, IDLE_FRAMES, scale);
        runSprites = setupBossSprites("/enemy/boss/run.png", 1, RUN_FRAMES, scale);
        attackSprites = setupBossSprites("/enemy/boss/attack.png", 2, ATTACK_FRAMES, scale);
        deathSprites = setupBossSprites("/enemy/boss/death.png", 4, DEATH_FRAMES, scale);

        //can cause issues with projectiles
        this.drawOffsetX = solidArea.x + (solidArea.width - spriteWidth) / 2;
        drawOffsetY = -70;
    }

    private BufferedImage[] setupBossSprites(String path, int row, int frameCount, int scale) {
        return super.setupSprites(path, frameCount, scale); //watch the scale
    }

    @Override
    public void setAction() {
        if(dying) return; //skip
        if(attacking) return;

        //second phase trigger
        if(!enraging && life < maxLife/2) {
            enraging = true;
            speed += 3;
            attackPower += 20;
            life = 750;
            gp.addFloatingText("DOMAIN EXPANSION!", worldX, worldY - 50, java.awt.Color.RED);
            gp.playSE(9);
        }

        if(enraging) {
            if(daActive) {
                //lasts 5 Seconds
                daTimer--;
                if(daTimer <= 0) {
                    daActive = false;
                    daCooldown = 240; // 4 secs not real life
                    gp.addFloatingText("DA DISSIPATED", worldX, worldY - 20, java.awt.Color.GRAY);
                }
            } else {
                if(daCooldown > 0) {
                    daCooldown--;
                } else {
                    daActive = true;
                    daTimer = 300;
                    gp.bgM.setDomainState(true);
                    //gp.playSE(9);
                    gp.addFloatingText("DOMAIN AMPLIFICATION", worldX, worldY - 80, new java.awt.Color(139, 0, 0));
                }
            }
        }

        int dist = Math.abs(gp.player.worldX - worldX);

        //may be stuck on the ground
        if(gp.player.worldX < worldX) direction = "left";
        else direction = "right";

        int range = gp.tileSize * 3; //need to align on the body
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
        int frameSpeed = 6;
        if(enraging) {
            frameSpeed = 4;
        }

        if(dying) {
            if(spriteCounter > frameSpeed) {
                spriteNum++;
                spriteCounter = 0;

                if(spriteNum == 14 || spriteNum == 15 || spriteNum == 16) {
                    checkExplosionCollision();
                }

                if(spriteNum >= DEATH_FRAMES) {
                    alive = false;
                    gp.ui.showMessage("SPECIAL GRADE EXORCISED");
                }
            }
            if(deathSprites != null && spriteNum < DEATH_FRAMES) image = deathSprites[spriteNum];
        }
        else if(attacking) {
            if(spriteCounter > frameSpeed) {
                spriteNum++;
                spriteCounter = 0;

                if(spriteNum == 10) {
                    checkAttackCollision();
                }

                if(spriteNum >= ATTACK_FRAMES) {
                    attacking = false;
                    spriteNum = 0;
                    attackCooldown = (enraging) ? 40 : 80; //don't touch blind
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

        //kinda bugged
        if(worldX + gp.tileSize*2 > gp.cameraX &&
                worldX - gp.tileSize*2 < gp.cameraX + gp.screenWidth) {

            if(daActive) {
                java.awt.Composite original = g2.getComposite();

                g2.setColor(new java.awt.Color(50, 0, 0));
                g2.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 0.6f));

                //tweak on y
                int auraX = screenX + solidArea.x - 10;
                int auraY = screenY + solidArea.y - 10;
                int auraW = solidArea.width + 20;
                int auraH = solidArea.height + 20;

                g2.fillOval(auraX, auraY, auraW, auraH);

                g2.setComposite(original);
            }
        }
        super.draw(g2);
    }

    @Override
    public void checkPlayerCollision() {}

    public void checkAttackCollision() {
        int attackW = 140;
        int attackH = 150;
        int overlap = gp.tileSize / 2;
        int attackX = (direction.equals("left")) ? worldX - attackW + 32: worldX + solidArea.width - 32;

        Rectangle attackBox = new Rectangle(attackX, worldY - 40, attackW, attackH);
        //this.debugAttackBox = attackBox; //draw it

        if(attackBox.intersects(getPlayerRect())) {
            gp.player.takeDamage(attackPower, enraging);

            if(gp.player.worldX < worldX) gp.player.worldX -= 3;
            else gp.player.worldX += 3;
        }
    }

    public void checkExplosionCollision() {
        int radius = 200;
        Rectangle boomBox = new Rectangle(worldX + solidArea.width/2 - radius/2, worldY + solidArea.height/2 - radius/2, radius, radius);
        this.debugAttackBox = boomBox;

        if(boomBox.intersects(getPlayerRect())){
            gp.player.takeDamage(200, true);
            //gp.addFloatingText("-200 (TRUE DMG)", gp.player.worldX, gp.player.worldY, java.awt.Color.MAGENTA);
            gp.playSE(5);
        }
    }

    @Override
    public void takeDamage(int damage) {
        if(dying) return;

        if(daActive) {
            super.takeDamage((damage/2) + (damage/4)); // 75% DMG in DA
        }
        else {
            super.takeDamage(damage);
        }

        if(life <= 0) {
            life = 0;
            speed = 0;
            dying = true;
            alive = true;
            gp.bgM.setDomainState(false);
            collisionOn = false;
        }
    }

    @Override
    public void applyKnockback(String sourceDir, int power, boolean isStun) {
        if(daActive || enraging) return;
        if(this.attacking) {
            this.attacking = false;
            this.spriteNum = 0;
            //this.attackCooldown = 20;
        }
        if(isStun) {
            this.stunTimer = 60;
        }
        if(power > 0) {
            this.knockback = true;
            if(sourceDir.equals("left")) worldX -= power;
            else worldX += power;
        }
    }

    private Rectangle getPlayerRect() {
        return new Rectangle(gp.player.worldX + gp.player.solidArea.x,
                gp.player.worldY + gp.player.solidArea.y,
                gp.player.solidArea.width,
                gp.player.solidArea.height);
    }
}