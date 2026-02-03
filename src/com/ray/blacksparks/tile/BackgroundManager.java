package com.ray.blacksparks.tile;

import com.ray.blacksparks.main.GamePanel;
import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Objects;

public class BackgroundManager {

    GamePanel gp;
    public BufferedImage bgImage;
    public BufferedImage domainImage;

    private boolean isDomainActive = false;
    private double parallaxScale = 0.5;

    public BackgroundManager(GamePanel gp) {
        this.gp = gp;
        loadBackgrounds();
    }

    public void loadBackgrounds() {
        try {
            domainImage = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/backgrounds/domain.png")));
            bgImage = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/backgrounds/sky.png")));
        } catch (Exception e) {
            System.out.println("no background");
            bgImage = new BufferedImage(gp.screenWidth, gp.screenHeight, BufferedImage.TYPE_INT_RGB);
        }
    }

    public void setMapBackground(String mapPath) {
        isDomainActive = false;

        try {
            String path = "/backgrounds/sky.png";

            if(mapPath.contains("map02")) {
                path = "/backgrounds/cave.png";
            }

            bgImage = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream(path)));

        } catch(Exception e) { e.printStackTrace(); }
    }

    public void setDomainState(boolean active) {
        this.isDomainActive = active;
    }

    public void draw(Graphics2D g2) {
        BufferedImage current = isDomainActive ? domainImage : bgImage;
        if(current == null) return;

        int screenW = gp.screenWidth;
        int screenH = gp.screenHeight;

        if(isDomainActive) {
            g2.drawImage(current, 0, 0, screenW, screenH, null);
        }
        else {
            double ratio = (double) screenH / current.getHeight();
            int drawW = (int) (current.getWidth() * ratio);
            int drawH = screenH;

            long totalScroll = (long) (gp.cameraX * parallaxScale);

            int offset = (int) (totalScroll % drawW);

            g2.drawImage(current, -offset, 0, drawW, drawH, null);

            g2.drawImage(current, -offset + drawW, 0, drawW, drawH, null);

            if((-offset + drawW) < screenW) {
                g2.drawImage(current, -offset + (drawW * 2), 0, drawW, drawH, null);
            }
        }
    }
}
