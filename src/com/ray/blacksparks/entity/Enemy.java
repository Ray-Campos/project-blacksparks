package com.ray.blacksparks.entity;

import com.ray.blacksparks.main.GamePanel;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.Objects;

public abstract class Enemy extends Entity {

    // COMMON COMBAT STATS
    public int attackPower = 0;
    public int expReward = 0;

    // AI VARIABLES
    public int lockCounter = 0;
    public int actionLockCounter = 0; // For attack delays

    public Rectangle debugAttackBox = null;
    public boolean attacking = false;

    public int solidAreaDefaultX, solidAreaDefaultY;
    public String name;

    public int stunTimer = 0; // If > 0
    public boolean knockback = false;

    public Enemy(GamePanel gp) {
        this.gp = gp;
        // Default hitbox (Standard 32x32)
        // Children can override this in their constructor
        direction = "down";
        speed = 1;
        solidArea = new Rectangle(8, 8, 32, 32);
        solidAreaDefaultX = solidArea.x;
        solidAreaDefaultY = solidArea.y;
    }

    // ABSTRACT
    public abstract void getImage();
    public abstract void setAction(); // The Brain
    public abstract void updateAnimation(); // Specific animation logic

    @Override
    public void update() {

        if(stunTimer > 0) {
            stunTimer--;
            // While stunned just apply gravity but NO movement/AI
            velocityY += gravity;
            if(velocityY > 15) velocityY = 15;

            return; // SKIP AI AND MOVEMENT
        }
        // THINK (AI)
        setAction();

        // GRAVITY & PHYSICS
        // Apply Gravity
        velocityY += gravity;
        if(velocityY > 15) velocityY = 15;

        // Check Floor
        collisionOn = false;
        String currentDirection = direction; // Save real direction
        direction = "down"; // Trick checker

        int oldSpeed = speed;
        speed = velocityY;

        gp.cChecker.checkTile(this);

        if (collisionOn) {
            onGround = true;

            // SNAP TO GRID
            // Calculate the Y of the tile we hit (The floor)
            // (Current Y + Hitbox Offset + Hitbox Height + Speed) / 64
            int tileY = (worldY + solidArea.y + solidArea.height + velocityY) / gp.tileSize;

            // Set worldY so feet sit EXACTLY on top of that tile
            worldY = tileY * gp.tileSize - solidArea.height - solidArea.y;

            velocityY = 0;
        } else {
            onGround = false;
            worldY += velocityY;
        }
        speed = oldSpeed; // Reset
        direction = currentDirection;

        // HORIZONTAL MOVEMENT
        // Restore facing direction from AI
        // Check Wall Collision
        collisionOn = false;
        // setAction() should have set 'direction' already
        gp.cChecker.checkTile(this);

        if(!collisionOn && !attacking) {
            if(direction.equals("left")) worldX -= speed;
            if(direction.equals("right")) worldX += speed;
        }
        // HIT A WALL? THEN FLIP
        else if(collisionOn) {
            if(direction.equals("left")) direction = "right";
            else direction = "left";
        }

        // ANIMATION (Delegated to Child)
        updateAnimation();

        // CHECK PLAYER COLLISION (Touch Damage)
        checkPlayerCollision();
    }

    public void checkPlayerCollision() {
        if(alive && !gp.player.invincible) {
            // Simple intersection check
            Rectangle myRect = new Rectangle(worldX + solidArea.x, worldY + solidArea.y, solidArea.width, solidArea.height);
            Rectangle playerRect = new Rectangle(gp.player.worldX + gp.player.solidArea.x, gp.player.worldY + gp.player.solidArea.y, gp.player.solidArea.width, gp.player.solidArea.height);

            if(myRect.intersects(playerRect)) {
                gp.player.takeDamage(attackPower, false);
                if(gp.player.worldX < worldX) gp.player.worldX -= 3;
                else gp.player.worldX += 3;
            }
        }
    }

    // Call this AFTER loading sprites to auto-center them
    public void alignSpriteToHitbox() {
        // Formula: (HitboxX - SpriteX) + (HitboxWidth/2 - SpriteWidth/2)
        // Since both are relative to 0,0 locally:

        // CENTER HORIZONTALLY
        // solidArea.x is the gap between entity-left and hitbox-left
        this.drawOffsetX = solidArea.x + (solidArea.width - spriteWidth) / 2;

        // ALIGN FEET (Vertically)
        // you want feet to match, not the center of the head
        // Formula: TopOfHitbox + HitboxHeight - SpriteHeight
        this.drawOffsetY = solidArea.y + solidArea.height - spriteHeight;
    }

    @Override
    public void draw(Graphics2D g2) {
        // CULLING
        if(worldX + gp.tileSize*2 > gp.cameraX &&
                worldX - gp.tileSize*2 < gp.cameraX + gp.screenWidth &&
                worldY + gp.tileSize*2 > gp.cameraY &&
                worldY - gp.tileSize*2 < gp.cameraY + gp.screenHeight) {

            int screenX = worldX - gp.cameraX;
            int screenY = worldY - gp.cameraY;

            if (image != null) {
                // WHERE TO DRAW (Top-Left of the Image container)
                int finalDrawX = screenX + drawOffsetX;
                int finalDrawY = screenY + drawOffsetY;

                if (direction.equals("left")) {
                    // FLIP LOGIC
                    // draw at X + Width, and draw "backwards" by -Width
                    g2.drawImage(image, finalDrawX + spriteWidth, finalDrawY, -spriteWidth, spriteHeight, null);
                } else {
                    // NORMAL LOGIC
                    g2.drawImage(image, finalDrawX, finalDrawY, spriteWidth, spriteHeight, null);
                }
            }

            // DEBUG: BODY HITBOX (Red Outline)
            g2.setColor(Color.RED);
            g2.drawRect(screenX + solidArea.x, screenY + solidArea.y, solidArea.width, solidArea.height);

            // DEBUG: ATTACK HITBOX (Filled Yellow)
            if(debugAttackBox != null) {
                // Convert World Attack Box to Screen Coords
                // Use cameraX/Y, NOT player.screenX/Y
                int screenAttackX = debugAttackBox.x - gp.cameraX;
                int screenAttackY = debugAttackBox.y - gp.cameraY;

                g2.setColor(new Color(255, 255, 0, 150));
                g2.fillRect(screenAttackX, screenAttackY, debugAttackBox.width, debugAttackBox.height);

                debugAttackBox = null;
            }

            // DEBUG: AGGRO RANGE (Yellow Circle for CursedSpirit)
            if(this instanceof CursedSpirit) {
                g2.setColor(Color.YELLOW);
                g2.drawOval(screenX - (gp.tileSize*3), screenY - (gp.tileSize*3), gp.tileSize*7, gp.tileSize*7);
            }
        }
    }

    // SHARED COMBAT METHODS
    public void takeDamage(int damage) {
        life -= damage;
        gp.addFloatingText("-" + damage, worldX, worldY, Color.BLUE);
        // Simple visual feedback (Knockback)
        if(direction.equals("left")) worldX += 5;
        else worldX -= 5;

        if(life <= 0) {
            alive = false;
        }
    }

    public void applyKnockback(String sourceDir, int power, boolean isStun) {
        if(this.attacking) {
            this.attacking = false;
            this.spriteNum = 0; // Reset animation
            //this.attackCooldown = 20; // Small delay before they can try again
        }

        // APPLY STUN
        if(isStun) {
            this.stunTimer = 60; // 1 Second Stun
        }
        if(power > 0) {
            this.knockback = true;
            // Push away from source
            if(sourceDir.equals("left")) worldX -= power;
            else worldX += power;
        }
    }
}