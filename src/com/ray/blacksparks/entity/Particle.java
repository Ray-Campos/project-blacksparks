package com.ray.blacksparks.entity;

import com.ray.blacksparks.main.GamePanel;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class Particle extends Entity {

    Entity generator;
    Color color;
    int size;
    int xd, yd;

    private BufferedImage[] sprites;

    public Particle(GamePanel gp, Entity generator, Color color, int size, int speed, int maxLife, int xd, int yd) {
        this.gp = gp;

        this.generator = generator;
        this.color = color;
        this.size = size;
        this.speed = speed;
        this.maxLife = maxLife;
        this.xd = xd;
        this.yd = yd;

        this.life = maxLife;

        int offset = (gp.tileSize/2) - (size/2);
        worldX = generator.worldX + offset;
        worldY = generator.worldY + offset;

        getImage();
    }

    public void getImage() {
        sprites = setupSprites("/particle/pop.png", 4, 1);
    }

    public void update() {
        life--;
        if(life < 0) alive = false;

        worldX += xd * speed;
        worldY += yd * speed;
        if(life < maxLife / 2) worldY++;

        spriteCounter++;
        if(spriteCounter > 5) {
            spriteNum++;
            if(spriteNum >= 4) spriteNum = 0;
            spriteCounter = 0;
        }
    }

    public void draw(Graphics2D g2) {
        int screenX = worldX - gp.cameraX;
        int screenY = worldY - gp.cameraY;

        if(sprites != null && sprites[spriteNum] != null) {
            g2.drawImage(sprites[spriteNum], screenX, screenY, size*2, size*2, null);
        } else {
            g2.setColor(color);
            g2.fillRect(screenX, screenY, size, size);
        }
    }
}