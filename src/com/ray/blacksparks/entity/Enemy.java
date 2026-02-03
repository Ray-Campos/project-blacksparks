package com.ray.blacksparks.entity;

import com.ray.blacksparks.main.GamePanel;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.Objects;

public abstract class Enemy extends Entity {

    public int attackPower = 0;
    public int expReward = 0;

    public int lockCounter = 0;
    public int actionLockCounter = 0;

    public Rectangle debugAttackBox = null;
    public boolean attacking = false;

    public int solidAreaDefaultX, solidAreaDefaultY;
    public String name;

    public int stunTimer = 0;
    public boolean knockback = false;

    public Enemy(GamePanel gp) {
        this.gp = gp;

        direction = "down";
        speed = 1;
        solidArea = new Rectangle(8, 8, 32, 32);
        solidAreaDefaultX = solidArea.x;
        solidAreaDefaultY = solidArea.y;
    }

    public abstract void getImage();
    public abstract void setAction();
    public abstract void updateAnimation();

    @Override
    public void update() {

        if(stunTimer > 0) {
            stunTimer--;
            velocityY += gravity;
            if(velocityY > 15) velocityY = 15;

            return;
        }
        setAction();

        velocityY += gravity;
        if(velocityY > 15) velocityY = 15;

        collisionOn = false;
        String currentDirection = direction;
        direction = "down";

        int oldSpeed = speed;
        speed = velocityY;

        gp.cChecker.checkTile(this);

        if (collisionOn) {
            onGround = true;
            velocityY = 0;
        } else {
            onGround = false;
            worldY += velocityY;
        }
        speed = oldSpeed;
        direction = currentDirection;

        collisionOn = false;
        gp.cChecker.checkTile(this);

        if(!collisionOn && !attacking) {
            if(direction.equals("left")) worldX -= speed;
            if(direction.equals("right")) worldX += speed;
        }
        else if(collisionOn) {
            if(direction.equals("left")) direction = "right";
            else direction = "left";
        }

        updateAnimation();
        checkPlayerCollision();
    }

    public void checkPlayerCollision() {
        if(alive && !gp.player.invincible) {
            Rectangle myRect = new Rectangle(worldX + solidArea.x, worldY + solidArea.y, solidArea.width, solidArea.height);
            Rectangle playerRect = new Rectangle(gp.player.worldX + gp.player.solidArea.x, gp.player.worldY + gp.player.solidArea.y, gp.player.solidArea.width, gp.player.solidArea.height);

            if(myRect.intersects(playerRect)) {
                gp.player.takeDamage(attackPower, false);
                if(gp.player.worldX < worldX) gp.player.worldX -= 3;
                else gp.player.worldX += 3;
            }
        }
    }


    public void alignSpriteToHitbox() {
        //this is bugged tweak manually
        this.drawOffsetX = solidArea.x + (solidArea.width - spriteWidth) / 2;
        this.drawOffsetY = solidArea.y + solidArea.height - spriteHeight;
    }

    @Override
    public void draw(Graphics2D g2) {
        if(worldX + gp.tileSize*2 > gp.cameraX &&
                worldX - gp.tileSize*2 < gp.cameraX + gp.screenWidth &&
                worldY + gp.tileSize*2 > gp.cameraY &&
                worldY - gp.tileSize*2 < gp.cameraY + gp.screenHeight) {

            int screenX = worldX - gp.cameraX;
            int screenY = worldY - gp.cameraY;

            if (image != null) {
                int finalDrawX = screenX + drawOffsetX;
                int finalDrawY = screenY + drawOffsetY;

                if (direction.equals("left")) {
                    g2.drawImage(image, finalDrawX + spriteWidth, finalDrawY, -spriteWidth, spriteHeight, null);
                } else {
                    g2.drawImage(image, finalDrawX, finalDrawY, spriteWidth, spriteHeight, null);
                }
            }
            //g2.setColor(Color.RED);
            //g2.drawRect(screenX + solidArea.x, screenY + solidArea.y, solidArea.width, solidArea.height);

            /*
            if(debugAttackBox != null) {
                int screenAttackX = debugAttackBox.x - gp.cameraX;
                int screenAttackY = debugAttackBox.y - gp.cameraY;

                //g2.setColor(new Color(255, 255, 0, 150));
                //g2.fillRect(screenAttackX, screenAttackY, debugAttackBox.width, debugAttackBox.height);

                debugAttackBox = null;
            }

            if(this instanceof CursedSpirit) { //aggro range
                g2.setColor(Color.YELLOW);
                g2.drawOval(screenX - (gp.tileSize*3), screenY - (gp.tileSize*3), gp.tileSize*7, gp.tileSize*7);
            }*/
        }
    }

    public void takeDamage(int damage) {
        life -= damage;
        gp.addFloatingText("-" + damage, worldX, worldY, Color.BLUE);
        if(direction.equals("left")) worldX += 5;
        else worldX -= 5;

        if(life <= 0) {
            alive = false;
        }
    }

    public void applyKnockback(String sourceDir, int power, boolean isStun) {
        if(this.attacking) {
            this.attacking = false;
            this.spriteNum = 0;
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
}
