package com.ray.blacksparks.entity;

import com.ray.blacksparks.main.GamePanel;
import java.awt.Graphics2D;
import java.awt.Color; // Using Java drawing for now (easy to replace with sprite later)
import java.util.ArrayList;

public class Projectile extends Entity {

    public Color color = Color.CYAN;
    public int damage;
    public int knockbackPower; // 0-10
    public boolean penetrates = false;

    public double velX, velY;
    public boolean vectorMovement = false;
    public Entity owner;

    public ArrayList<Entity> hitList = new ArrayList<>();

    public Projectile(GamePanel gp) {
        this.gp = gp;

        speed = 10;
        alive = true;
        particleColor = Color.CYAN;

        solidArea = new java.awt.Rectangle(8, 8, 16, 16);
    }

    public void set(int worldX, int worldY, String direction, boolean onGround, int damage, int knockback) {
        this.worldX = worldX;
        this.worldY = worldY;
        this.direction = direction;
        this.onGround = onGround;
        this.damage = damage;
        this.knockbackPower = knockback;
        this.alive = true;

        hitList.clear();
    }

    public void update() {
        if(vectorMovement) {
            worldX += velX;
            worldY += velY;

            collisionOn = false;
            gp.cChecker.checkTile(this);
            if(collisionOn && !penetrates) alive = false;

        } else {

            if (direction.equals("left")) worldX -= speed;
            if (direction.equals("right")) worldX += speed;

            collisionOn = false;
            gp.cChecker.checkTile(this);
        }
        if(collisionOn) {
            alive = false;
        }

        if(worldX < 0 || worldX > gp.worldWidth) {
            alive = false;
        }

        //spriteCounter++;
    }

    public void draw(Graphics2D g2) {
        int screenX = worldX - gp.cameraX;
        int screenY = worldY - gp.cameraY;

        if(worldX + gp.tileSize > gp.cameraX &&
                worldX - gp.tileSize < gp.cameraX + gp.screenWidth &&
                worldY + gp.tileSize > gp.cameraY &&
                worldY - gp.tileSize < gp.cameraY + gp.screenHeight) {

            g2.setColor(this.color);
            g2.fillOval(screenX, screenY, 32, 32);

            g2.setColor(Color.white);
            g2.drawOval(screenX, screenY, 32, 32);
        }
    }

    public void setVector(int startX, int startY, int targetX, int targetY) {
        this.worldX = startX;
        this.worldY = startY;

        double distX = targetX - startX;
        double distY = targetY - startY;
        double dist = Math.sqrt(distX*distX + distY*distY);

        this.velX = (distX / dist) * speed;
        this.velY = (distY / dist) * speed;

        this.vectorMovement = true;
        this.alive = true;
        this.hitList.clear();
    }
}
