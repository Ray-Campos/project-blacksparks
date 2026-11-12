package com.ray.blacksparks.entity;

import com.ray.blacksparks.main.CombatMath;
import com.ray.blacksparks.main.GamePanel;
import com.ray.blacksparks.input.KeyHandler;
import com.ray.blacksparks.main.OrbManager;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Objects;

public class Player extends Entity {

    KeyHandler keyH;

    public final int screenX;
    public final int screenY;

    private final int IDLE_FRAME_COUNT = 8;
    private final int RUN_FRAME_COUNT = 10;
    private final int ATTACK_FRAME_COUNT = 7;

    private final int DRAW_OFFSET_X = 270;//128
    private final int DRAW_OFFSET_Y = 204; //80

    private BufferedImage[] idleSprites;
    private BufferedImage[] runSprites;
    private BufferedImage[] attackSprites;
    private BufferedImage[] blockSprites;

    public boolean attacking = false;
    public boolean attackChecked = false;
    public Rectangle attackArea = new Rectangle(0, 0, 80, 36); // The "Phantom Hitbox"
    public int attackCooldown = 0; //may be useless

    public PlayerStats stats;
    public OrbManager orbM;

    public int speedBuffTimer = 0;
    public int speedBuffValue = 0;
    public boolean dashing = false;
    public int defaultSpeed;

    public boolean invincible = false;
    public int invincibleTimer = 0;
    public boolean blocking = false;
    public boolean chanting = false;

    public ArrayList<Ghost> ghostTrail = new ArrayList<>();
    public int ghostTimer = 0;

    public Player(GamePanel gp, KeyHandler keyH) {
        this.gp = gp;
        this.keyH = keyH;
        stats = new PlayerStats();
        this.orbM = new OrbManager(gp, this);
        setDefaultValues();
        defaultSpeed = speed;

        screenX = gp.screenWidth / 2 - (gp.tileSize / 2);
        screenY = gp.screenHeight / 2 - (gp.tileSize / 2);

        solidArea = new Rectangle();
        solidArea.x = 16;
        solidArea.y = 16;
        solidArea.width = 32;
        solidArea.height = 48;
        particleColor = Color.BLUE;

        setDefaultValues();
        getPlayerImage();
    }

    public void setDefaultValues() {
        worldX = gp.tileSize * 10;//2
        worldY = gp.tileSize * 2;//8
        speed = 5;
        direction = "right";
        jumpStrength = 22;
        gravity = 1;

        level = 1;
        maxLife = 10;
        life = maxLife;
        exp = 0;
        nextLevelExp = 5;
    }

    public void getPlayerImage() {
        int scale = 2;
        try {
            //BufferedImage idleStrip = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/player/idle.png")));
            idleSprites = setupSprites("/player/idle/", "idle", IDLE_FRAME_COUNT, scale);
            runSprites = setupSprites("/player/run/", "run", RUN_FRAME_COUNT, scale);
            attackSprites = setupSprites("/player/attack/", "attack", ATTACK_FRAME_COUNT, scale);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private BufferedImage[] loadSpriteStrip(BufferedImage strip, int frameCount) {
        int frameWidth = strip.getWidth() / frameCount;
        int frameHeight = strip.getHeight();
        BufferedImage[] sprites = new BufferedImage[frameCount];
        for(int i = 0; i < frameCount; i++) {
            sprites[i] = strip.getSubimage(i * frameWidth, 0, frameWidth, frameHeight);
        }
        return sprites;
    }

    public void update() {
        //stats.update();
        stats.regen();
        orbM.update();

        if(speedBuffTimer > 0) {
            speedBuffTimer--;
            speed = defaultSpeed + speedBuffValue;

            //ghost trail
            if(dashing) {
                ghostTimer++;
                if(ghostTimer > 2) {
                    ghostTrail.add(new Ghost(worldX, worldY, direction, image));
                    if(ghostTrail.size() > 4) ghostTrail.remove(0);
                    ghostTimer = 0;
                }
            }
        } else {
            speed = defaultSpeed;
            if(dashing) {
                dashing = false;
                if(!blocking && invincibleTimer == 0) invincible = false;
                ghostTrail.clear();
            }
        }

        if(invincibleTimer > 0) {
            invincibleTimer--;
            if(invincibleTimer == 0 && !dashing && !blocking) {
                invincible = false;
            }
        }if(keyH.keySHIFT && !dashing && !blocking && stats.consumeStamina(CombatMath.getDashCost(stats))) {
            applySpeedBuff(6, 4, true);
            //gp.playSE(2);
        }

        if(keyH.keyCONTROL) {
            int cost = CombatMath.getBlockCostPerFrame(stats);
            if(stats.consumeCE(cost)) {
                blocking = true;
                invincible = true;
                //speed = 2;
            } else {
                blocking = false;
                //speed = defaultSpeed;
            }
        } else {
            if(blocking) {
                blocking = false;
                if(!dashing && invincibleTimer == 0) invincible = false;
                //speed = defaultSpeed;
            }
        }

        if(attackCooldown > 0) attackCooldown--;

        if(!attacking && attackCooldown == 0 && keyH.spacePressed) {
            performLightAttack();
        }

        boolean isMoving = false;
        if(attacking) {
            attackUpdate();
        }
        else if(chanting) {
            dashing = false;
            blocking = false;
            isMoving = false;
        }
        else {
            if (keyH.leftPressed) {
                direction = "left";
                isMoving = true;
            }
            if (keyH.rightPressed) {
                direction = "right";
                isMoving = true;
            }

            collisionOn = false;
            gp.cChecker.checkTile(this);

            if (!collisionOn && isMoving) {
                if (direction.equals("left")) worldX -= speed;
                if (direction.equals("right")) worldX += speed;
            }

            velocityY += gravity;

            if (velocityY > 15) velocityY = 15;

            String currentFacing = direction;

            direction = "down";
            int oldSpeed = speed;
            speed = velocityY;

            collisionOn = false;
            gp.cChecker.checkTile(this);

            if (collisionOn) {
                onGround = true;
                velocityY = 0;
                //y = (y / gp.tileSize) * gp.tileSize;
            } else {
                onGround = false;
                worldY += velocityY;
            }
            speed = oldSpeed;
            direction = currentFacing;

            if (keyH.upPressed && onGround) {
                velocityY = -jumpStrength;
                onGround = false;
                gp.playSE(4);
            }

            if(!attacking) {
                spriteCounter++;
                if (spriteCounter > 6) {
                    spriteNum++;
                    int maxFrames = isMoving ? runSprites.length : idleSprites.length;
                    if (spriteNum >= maxFrames) spriteNum = 0;
                    spriteCounter = 0;
                }
            }

            if (isMoving) {
                if (runSprites != null && spriteNum < runSprites.length) image = runSprites[spriteNum];
            } else {
                if (idleSprites != null && spriteNum < idleSprites.length) image = idleSprites[spriteNum];
            }
        }
    }

    public void performLightAttack() {
        int cost = CombatMath.getLightAttackCost();

        if(stats.consumeStamina(cost)) {
            attacking = true;
            spriteCounter = 0;
            gp.playSE(5);
        }
    }

    public void attackUpdate() {
        spriteCounter++;
        int frameSpeed = 5;
        int currentFrame = spriteCounter / frameSpeed;

        if(currentFrame >= attackSprites.length) {
            currentFrame = attackSprites.length - 1;
        }

        image = attackSprites[currentFrame];

        if(currentFrame == 5 && !attackChecked ) {
            checkAttackCollision();
            attackChecked = true;
        }

        if(spriteCounter >= (attackSprites.length * frameSpeed)) {
            attacking = false;
            spriteCounter = 0;
            attackChecked = false;
            attackCooldown = 10;
        }
    }

    public void checkAttackCollision() {
        int currentWorldX = worldX;
        int currentWorldY = worldY;

        int adjustX = 0;
        int adjustY = 0;

        if(direction.equals("left")) {
            adjustX = solidArea.x - attackArea.width;
        }
        if(direction.equals("right")) {
            adjustX = solidArea.x + solidArea.width;
        };

        Rectangle hitBox = new Rectangle(
                currentWorldX + adjustX,
                currentWorldY + solidArea.y,
                attackArea.width,
                attackArea.height
        );

        for(Enemy e : gp.enemyList) {
            if(e.alive) {
                Rectangle enemyRect = new Rectangle(
                        e.worldX + e.solidArea.x,
                        e.worldY + e.solidArea.y,
                        e.solidArea.width,
                        e.solidArea.height
                );

                if(hitBox.intersects(enemyRect)) {
                    int damage = CombatMath.calcLightAttackDamage(stats);

                    gp.applyDamageToEnemy(e, damage, false);
                    generateParticle(e, e);
                    gp.playSE(5);
                    //System.out.println("HIT! Dealt " + damage + " dmg.");
                }
            }
        }
    }

    public void performHeavyAttack() {
        int cost = CombatMath.getStrongAttackCost();
        if(stats.consumeStamina(cost)) {
            int damage = CombatMath.calcStrongAttackDamage(stats);
            //System.out.println("HEAVY Attack! Dmg: " + damage);
        }
    }

    public void dash() {
        int cost = CombatMath.getDashCost(stats);
        if(stats.consumeStamina(cost)) {
            // speed += 10;
            //System.out.println("Dash! Cost: " + cost);
        }
    }

    public void block() {
        int cost = CombatMath.getBlockCostPerFrame(stats);
        if(stats.consumeCE(cost)) {
            //invincible = true;
        } else {
            //invincible = false;
        }
    }

    public void draw(Graphics2D g2) {

        int tempScreenX = worldX - gp.cameraX;
        int tempScreenY = worldY - gp.cameraY;

        if(dashing && !ghostTrail.isEmpty()) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));

            for(Ghost g : ghostTrail) {
                int gScreenX = g.x - gp.cameraX;
                int gScreenY = g.y - gp.cameraY;

                int drawX = gScreenX - DRAW_OFFSET_X;
                int drawY = gScreenY - DRAW_OFFSET_Y;

                int drawWidth = g.image.getWidth();
                int drawHeight = g.image.getHeight();

                if(g.direction.equals("left")) {
                    g2.drawImage(g.image, drawX + drawWidth, drawY, -drawWidth, drawHeight, null);
                } else {
                    g2.drawImage(g.image, drawX, drawY, drawWidth, drawHeight, null);
                }
            }
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        }

        if(invincible && !dashing && !blocking) {
            if(invincibleTimer % 20 > 10) {
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
            }
        }
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));

        if(blocking) {
            g2.setColor(new Color(0, 200, 255, 100));

            int centerX = tempScreenX + solidArea.x + (solidArea.width / 2);
            int centerY = tempScreenY + solidArea.y + (solidArea.height / 2);

            int radius = 40;
            g2.fillOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
            g2.setColor(Color.white);
            g2.setStroke(new BasicStroke(2));
            g2.drawOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
        }

        if (image != null) {
            int drawX = tempScreenX - DRAW_OFFSET_X;
            int drawY = tempScreenY - DRAW_OFFSET_Y;

            int drawWidth = image.getWidth();
            int drawHeight = image.getHeight();

            if(direction.equals("left")) {
                g2.drawImage(image, drawX + drawWidth, drawY, -drawWidth, drawHeight, null);
            } else {
                g2.drawImage(image, drawX, drawY, drawWidth, drawHeight, null);
            }

            //debug red box
            g2.setColor(java.awt.Color.RED);
            g2.drawRect(tempScreenX + solidArea.x, tempScreenY + solidArea.y, solidArea.width, solidArea.height);
        }

        //debug attack box
        if(attacking) {
            g2.setColor(new Color(255, 0, 0, 100));

            int adjustX = 0;
            int adjustY = solidArea.y;

            if(direction.equals("left")) adjustX = solidArea.x - attackArea.width;
            if(direction.equals("right")) adjustX = solidArea.x + solidArea.width;

            int screenXbox = tempScreenX + adjustX;
            int screenYbox = tempScreenY + adjustY;

            g2.fillRect(screenXbox, screenYbox, attackArea.width, attackArea.height);
        }

        orbM.draw(g2);
    }

    public void checkLevelUp() {
        if(exp >= nextLevelExp) {
            level++;
            nextLevelExp = nextLevelExp * 2;
            maxLife += 10;
            life += 10;
            stats.attributePoints++;

            gp.playSE(7);
            gp.ui.showMessage("LEVEL UP! Points Available: " + stats.attributePoints);
        }
    }

    public void heal(int amount) {
        life += amount;
        if(life > maxLife) {
            life = maxLife;
        }

        gp.addFloatingText("+" + amount, worldX, worldY, Color.GREEN);
        //gp.playSE(2);
    }

    public void interact() {
        int currentCol = (worldX + gp.tileSize / 2) / gp.tileSize;
        int currentRow = (worldY + gp.tileSize / 2) / gp.tileSize;

        if(isShrine(currentCol, currentRow)) {
            triggerSave();
            return;
        }

        int interactCol = currentCol;
        int interactRow = currentRow;

        if(direction.equals("up"))    interactRow--;
        if(direction.equals("down"))  interactRow++;
        if(direction.equals("left"))  interactCol--;
        if(direction.equals("right")) interactCol++;

        if(isShrine(interactCol, interactRow)) {
            triggerSave();
        }
    }

    public boolean isShrine(int col, int row) {
        if(col >= 0 && col < gp.maxWorldCol && row >= 0 && row < gp.maxWorldRow) {
            return gp.tileM.mapTileNum[col][row] == 3;
        }
        return false;
    }

    public void triggerSave() {
        gp.saveLoad.save();
        gp.ui.showMessage("GAME SAVED");
        gp.playSE(6);
    }

    public void applySpeedBuff(int duration, int amount, boolean isDash) {
        speedBuffTimer = duration;
        speedBuffValue = amount;
        dashing = isDash;

        if(isDash) {
            invincible = true;
            ghostTrail.add(new Ghost(worldX, worldY, direction, image));
        }
    }

    public void takeDamage(int damage, boolean ignoreInvincibility) {
        if(invincible && !ignoreInvincibility) return;

        int actualDamage = CombatMath.calcDefense(stats, damage);
        int minDamage = (int)(damage * 0.20);

        if(actualDamage < minDamage) {
            actualDamage = minDamage;
        }
        life -= actualDamage;

        if(life <= 0) {
            life = 0;
            gp.gameState = gp.gameOverState;
            gp.playSE(9);
            gp.stopMusic();
        }

        invincible = true;
        invincibleTimer = 120;

        gp.playSE(5);
        gp.addFloatingText("-" + actualDamage, worldX, worldY, Color.RED);
        //no knockback for now
    }

    class Ghost {
        int x, y;
        String direction;
        BufferedImage image;

        public Ghost(int x, int y, String direction, BufferedImage image) {
            this.x = x;
            this.y = y;
            this.direction = direction;
            this.image = image;
        }
    }
}