package com.ray.blacksparks.entity;

import com.ray.blacksparks.main.GamePanel;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

public class FloatingText {

    GamePanel gp;
    public String text;
    public int worldX, worldY;
    public Color color;
    public boolean alive = true;

    public int motionY = 0;
    public int lifeTimer = 0;
    public int maxLife = 60; // 1 second

    public FloatingText(GamePanel gp, String text, int worldX, int worldY, Color color) {
        this.gp = gp;
        this.text = text;
        this.worldX = worldX;
        this.worldY = worldY;
        this.color = color;
    }

    public void update() {
        lifeTimer++;

        if(lifeTimer < 10) worldY -= 2;
        else if(lifeTimer < 40) worldY -= 1;

        if(lifeTimer > maxLife) {
            alive = false;
        }
    }

    public void draw(Graphics2D g2) {
        int screenX = worldX - gp.cameraX;
        int screenY = worldY - gp.cameraY;

        g2.setFont(new Font("Arial", Font.BOLD, 20));

        g2.setColor(Color.black);
        g2.drawString(text, screenX + 2, screenY + 2);

        g2.setColor(color);
        g2.drawString(text, screenX, screenY);
    }
}
