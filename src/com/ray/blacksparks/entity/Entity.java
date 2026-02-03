package com.ray.blacksparks.entity;

import com.ray.blacksparks.main.GamePanel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.awt.Color;
import java.util.Objects;

public class Entity {

    public GamePanel gp;

    public int worldX, worldY;
    public int speed;
    public String direction = "down";

    public BufferedImage image;
    public int spriteCounter = 0;
    public int spriteNum = 0;

    public int drawOffsetX = 0;
    public int drawOffsetY = 0;
    public int spriteWidth = 0;
    public int spriteHeight = 0;

    public boolean alive = true;
    public boolean collisionOn = false;
    public Rectangle solidArea = new Rectangle(0, 0, 48, 48); // Default
    public int solidAreaDefaultX, solidAreaDefaultY;

    public int velocityY = 0;
    public int gravity = 1;
    public int jumpStrength = 16;
    public boolean onGround = false;

    public int maxLife;
    public int life;
    public int level;
    public int exp;
    public int nextLevelExp;

    public boolean knockback = false;
    public int stunTimer = 0;
    public Color particleColor;

    public Entity(GamePanel gp) {
        this.gp = gp;
    }

    public Entity() {}

    public BufferedImage[] setupSprites(String path, int frameCount, int scaleMultiplier) {
        BufferedImage[] frames = new BufferedImage[frameCount];
        try {
            BufferedImage sheet = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream(path)));

            int frameW = sheet.getWidth() / frameCount;
            int frameH = sheet.getHeight();

            this.spriteWidth = frameW * scaleMultiplier;
            this.spriteHeight = frameH * scaleMultiplier;

            for(int i = 0; i < frameCount; i++) {
                frames[i] = sheet.getSubimage(i * frameW, 0, frameW, frameH);
            }

        } catch (Exception e) {
            System.out.println("Error loading: " + path);
            e.printStackTrace();
        }
        return frames;
    }

    public BufferedImage[] setupSprites(String folderPath, String fileNamePrefix, int frameCount, int scaleMultiplier) {
        BufferedImage[] frames = new BufferedImage[frameCount];

        try {
            for(int i = 0; i < frameCount; i++) {
                String fullPath = folderPath + fileNamePrefix + i + ".png";
                BufferedImage rawImage = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream(fullPath)));

                int newWidth = rawImage.getWidth() * scaleMultiplier;
                int newHeight = rawImage.getHeight() * scaleMultiplier;

                BufferedImage scaledImage = new BufferedImage(newWidth, newHeight, rawImage.getType());
                Graphics2D g2 = scaledImage.createGraphics();
                g2.drawImage(rawImage, 0, 0, newWidth, newHeight, null);
                g2.dispose();

                frames[i] = scaledImage;

                if(i == 0) {
                    this.spriteWidth = newWidth;
                    this.spriteHeight = newHeight;
                }
            }
        } catch (Exception e) {
            System.out.println("ERROR LOADING: " + folderPath);
            e.printStackTrace();
        }
        return frames;
    }

    public void generateParticle(Entity generator, Entity target) {

        Color color = generator.particleColor;
        int size = 6;
        int speed = 1;
        int maxLife = 20;

        //might be worth adjusting these values per generator type later
        Particle p1 = new Particle(gp, target, color, size, speed, maxLife, -2, -2);
        Particle p2 = new Particle(gp, target, color, size, speed, maxLife, 2, -2);
        Particle p3 = new Particle(gp, target, color, size, speed, maxLife, -2, 2);
        Particle p4 = new Particle(gp, target, color, size, speed, maxLife, 2, 2);

        gp.particleList.add(p1);
        gp.particleList.add(p2);
        gp.particleList.add(p3);
        gp.particleList.add(p4);
    }
    public void update() {}
    public void draw(Graphics2D g2) {}
}
