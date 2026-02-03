package com.ray.blacksparks.entity;

import com.ray.blacksparks.main.GamePanel;
import javax.imageio.ImageIO;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Objects;
import java.util.Random;

public class Slime extends Enemy {

    private final int WALK_FRAME_COUNT = 8;
    private BufferedImage[] walkSprites;

    public Slime(GamePanel gp, int worldX, int worldY) {
        super(gp);
        this.worldX = worldX;
        this.worldY = worldY;

        name = "Slime";
        speed = 1;
        maxLife = 6;
        life = maxLife;
        attackPower = 2;
        expReward = 50;

        solidArea = new Rectangle(8, 16, 32, 32);

        getImage();
    }

    public void getImage() {
        walkSprites = setupSprites("/enemy/slime/walk.png", WALK_FRAME_COUNT, 2);

        this.drawOffsetX = solidArea.x + (solidArea.width - spriteWidth) / 2;
        drawOffsetY = -70;
    }

    @Override
    public void setAction() {
        lockCounter++;
        if(lockCounter == 120) {
            Random random = new Random();
            int i = random.nextInt(100) + 1;

            if(i <= 25) direction = "left";
            if(i > 25 && i <= 50) direction = "right";

            lockCounter = 0;
        }
    }

    @Override
    public void updateAnimation() {
        spriteCounter++;
        int frameSpeed = 10;
        if(spriteCounter > frameSpeed) {
            spriteNum++;
            if(spriteNum >= WALK_FRAME_COUNT) spriteNum = 0;
            spriteCounter = 0;
        }
        if(walkSprites != null) {
            image = walkSprites[spriteNum];
        }
    }
}