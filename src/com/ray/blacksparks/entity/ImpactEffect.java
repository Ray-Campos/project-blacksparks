package com.ray.blacksparks.entity;

import com.ray.blacksparks.main.GamePanel;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class ImpactEffect extends Entity {

    private BufferedImage[] sprites;
    private int frameCount;
    private int frameSpeed;

    public ImpactEffect(GamePanel gp, int x, int y, int type) {
        super(gp);
        this.worldX = x;
        this.worldY = y;

        getImage(type);
    }

    public void getImage(int type) {
        String path = "/particle/hit_blue.png";
        frameCount = 4;
        frameSpeed = 5;

        if(type == 2) {
            path = "/particle/hit_red.png";
            frameCount = 4;
        }
        else if(type == 3) {
            path = "/particle/hit_purple.png";
            frameCount = 4;
        }

        try {
            int scale = (type == 3) ? 2 : 1;
            sprites = setupSprites(path, frameCount, scale);

            spriteWidth = gp.tileSize * scale;
            spriteHeight = gp.tileSize * scale;

            worldX -= spriteWidth / 2;
            worldY -= spriteHeight / 2;

        } catch (Exception e) {
            alive = false;
        }
    }

    @Override
    public void update() {
        spriteCounter++;
        if(spriteCounter > frameSpeed) {
            spriteNum++;
            spriteCounter = 0;
            if(spriteNum >= frameCount) {
                alive = false;
            }
        }
    }

    @Override
    public void draw(Graphics2D g2) {
        int screenX = worldX - gp.cameraX;
        int screenY = worldY - gp.cameraY;

        if(sprites != null && spriteNum < sprites.length) {
            g2.drawImage(sprites[spriteNum], screenX, screenY, spriteWidth, spriteHeight, null);
        }
    }
}