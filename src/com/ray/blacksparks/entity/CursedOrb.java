package com.ray.blacksparks.entity;

import com.ray.blacksparks.main.GamePanel;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.image.BufferedImage;

public class CursedOrb extends Projectile {

    private BufferedImage[] sprites;
    private int FRAME_COUNT = 8;
    public String name;
    public int type;

    public CursedOrb(GamePanel gp, int type) {
        super(gp);
        this.type = type;

        name = "Cursed Orb";
        speed = 10;
        maxLife = 80;
        life = maxLife;
        damage = 2;
        knockbackPower = 5;
        alive = true;

        solidArea = new java.awt.Rectangle(8, 8, 32, 32);

        getImage();
    }

    public void getImage() {
        //tweak
        this.drawOffsetX = 0;
        this.drawOffsetY = 0;

        String path = "/projectile/orb_blue.png";

        switch(type) {
            case 1:
                path = "/projectile/orb_blue.png";
                drawOffsetY = 5;
                break;
            case 2:
                path = "/projectile/orb_red.png";
                speed = 8;
                break;
            case 3:
                path = "/projectile/orb_purple.png";
                speed = 6;
                drawOffsetY = -10;
                break;
            case 4:
                path = "/projectile/orb_enemy.png";
                speed = 7;
                FRAME_COUNT = 4;
                drawOffsetY = 0;
                break;
            case 5:
                path = "/projectile/orb_boss.png";
                speed = 8;
                FRAME_COUNT = 4;
                drawOffsetY = 0;
                break;
            case 6:
                path = "/projectile/orb_boss2.png";
                speed = 5;
                FRAME_COUNT = 4;
                drawOffsetY = 0;
                break;
        }

        try {
            sprites = setupSprites(path, FRAME_COUNT, 1);
        } catch(Exception e) {
            //sprites = setupSprites("/projectile/orb_blue.png", FRAME_COUNT, 1);
        }
    }

    @Override
    public void update() {
        super.update();

        spriteCounter++;
        if(spriteCounter > 5) {
            spriteNum++;
            if(spriteNum >= FRAME_COUNT) spriteNum = 0;
            spriteCounter = 0;
        }
    }

    @Override
    public void draw(Graphics2D g2) {
        int screenX = worldX - gp.cameraX;
        int screenY = worldY - gp.cameraY;

        if(worldX + gp.tileSize > gp.cameraX &&
                worldX - gp.tileSize < gp.cameraX + gp.screenWidth) {

            if(sprites != null) {
                g2.drawImage(sprites[spriteNum], screenX, screenY, gp.tileSize, gp.tileSize, null);
            } else {
                if(type == 2) g2.setColor(Color.RED);
                else if(type == 3) g2.setColor(new Color(128, 0, 128));
                else g2.setColor(Color.BLUE);

                g2.fillOval(screenX, screenY, 24, 24);
            }
        }
    }
}