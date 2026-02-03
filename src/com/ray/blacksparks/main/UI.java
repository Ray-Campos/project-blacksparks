package com.ray.blacksparks.main;

import java.awt.*;

import com.ray.blacksparks.entity.Enemy;
import com.ray.blacksparks.entity.PlayerStats;
import com.ray.blacksparks.entity.Boss;
import java.io.File;

public class UI {

    GamePanel gp;
    Font arial_40, arial_80B;

    public String message = "";
    public int messageCounter = 0;

    public int commandNum = 0;
    public int subState = 0;
    public int counter = 0;

    public int currentTab = 0;
    public final int tabCharacter = 0;
    public final int tabSystem = 1;
    public final int tabHelp = 2; // New Tab ID
    public int helpPage = 0; // 0-3

    public UI(GamePanel gp) {
        this.gp = gp;
        arial_40 = new Font("Arial", Font.PLAIN, 40);
        arial_80B = new Font("Arial", Font.BOLD, 80);
    }

    public void showMessage(String text) {
        message = text;
        messageCounter = 120;
    }

    public void draw(Graphics2D g2) {

        if (gp.gameState == gp.titleState) {
            drawTitleScreen(g2);
        }

        if (gp.gameState == gp.playState) {
            drawPlayerLife(g2);
            drawOrbHUD(g2);
            drawBossHP(g2);
        }

        if (gp.gameState == gp.victoryState) {
            drawVictoryScreen(g2);
        }

        if (gp.gameState == gp.pauseState) {
            drawPlayerLife(g2);
            drawOrbHUD(g2);
            drawPauseScreen(g2);
            drawBossHP(g2);
        }
        if(gp.gameState == gp.gameOverState) {
            drawGameOver(g2);
        }

        if (gp.gameState == gp.transitionState) {
            drawTransition(g2);
        }

        if(messageCounter > 0) {
            g2.setFont(g2.getFont().deriveFont(30F));
            g2.setColor(Color.white);
            g2.drawString(message, gp.tileSize, gp.tileSize * 5);
            messageCounter--;
        }
    }

    public void drawGameOver(Graphics2D g2) {

        counter++;
        float alpha = counter * 0.01f;
        if(alpha > 1f) {
            alpha = 1f;
            subState = 1;
        }

        g2.setColor(Color.black);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);

        g2.setColor(Color.RED);
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 60f));
        String text = "CONSUMED BY THE CURSE";
        int x = getXforCenteredText(text, g2);
        int y = gp.screenHeight / 2;

        g2.setColor(Color.BLACK);
        g2.drawString(text, x+4, y+4);

        g2.setColor(Color.RED);
        g2.drawString(text, x, y);

        if(subState == 1) {
            g2.setColor(Color.WHITE);
            g2.setFont(g2.getFont().deriveFont(20f));
            String sub = "Press SPACE to Restart";
            x = getXforCenteredText(sub, g2);
            g2.drawString(sub, x, y + 80);
        }
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
    }

    public void drawTitleScreen(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0));
        g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);

        g2.setFont(arial_80B);
        String text = "BLACK SPARKS";
        int x = getXforCenteredText(text, g2);
        int y = gp.tileSize * 3;

        g2.setColor(Color.gray);
        g2.drawString(text, x+5, y+5);

        g2.setColor(Color.white);
        g2.drawString(text, x, y);

        g2.setFont(arial_40);

        text = "NEW GAME";
        x = getXforCenteredText(text, g2);
        y += gp.tileSize * 4;
        g2.drawString(text, x, y);
        if(commandNum == 0) {
            g2.drawString(">", x-gp.tileSize, y);
        }

        boolean saveExists = new File("save.dat").exists();

        if(saveExists) {
            g2.setColor(Color.white);
        } else {
            g2.setColor(Color.gray);
        }

        text = "LOAD GAME";
        x = getXforCenteredText(text, g2);
        y += gp.tileSize;
        g2.drawString(text, x, y);

        if(commandNum == 1) {
            g2.setColor(Color.white);
            g2.drawString(">", x-gp.tileSize, y);
        }

        g2.setColor(Color.white);
        text = "QUIT";
        x = getXforCenteredText(text, g2);
        y += gp.tileSize;
        g2.drawString(text, x, y);
        if(commandNum == 2) {
            g2.drawString(">", x-gp.tileSize, y);
        }
    }

    public void drawPlayerLife(Graphics2D g2) {
        int x = gp.tileSize / 2;
        int y = gp.tileSize / 2;
        int width = gp.tileSize * 5;
        int height = 20;

        g2.setColor(new Color(35, 35, 35));
        g2.fillRect(x, y, width, height);

        double oneScale = (double)width / gp.player.maxLife;
        double hpBarValue = oneScale * gp.player.life;

        g2.setColor(new Color(255, 0, 30));
        g2.fillRect(x, y, (int)hpBarValue, height);

        g2.setColor(Color.white);
        g2.setStroke(new BasicStroke(2));
        g2.drawRect(x, y, width, height);

        g2.setFont(arial_40);
        g2.drawString("Lv." + gp.player.level, x + width + 10, y + 32);

        y += 35;
        height = 12;

        g2.setColor(new Color(35, 35, 35));
        g2.fillRect(x, y, width, height);

        double staminaScale = (double)width / gp.player.stats.staminaMax;
        double staminaVal = staminaScale * gp.player.stats.stamina;

        g2.setColor(Color.green);
        g2.fillRect(x, y, (int)staminaVal, height);
        g2.setColor(Color.white);
        g2.setStroke(new BasicStroke(1));
        g2.drawRect(x, y, width, height);

        y += 18;

        g2.setColor(new Color(35, 35, 35));
        g2.fillRect(x, y, width, height);

        double ceScale = (double)width / gp.player.stats.ceMax;
        double ceVal = ceScale * gp.player.stats.ce;

        g2.setColor(new Color(0, 100, 255));
        g2.fillRect(x, y, (int)ceVal, height);
        g2.setColor(Color.white);
        g2.drawRect(x, y, width, height);

        g2.setColor(new Color(255, 255, 255, 150));
        g2.setFont(new Font("Arial", Font.PLAIN, 14));
        g2.drawString("[ESC] PAUSE", x, y + 160);

        if(gp.player.stats.inZone) {
            g2.setFont(new Font("Arial", Font.BOLD | Font.ITALIC, 18));

            if(gp.player.stats.zoneTier == 1) {
                g2.setColor(Color.YELLOW);
                g2.drawString("THE ZONE [120%]", x, y + 110);
            } else {
                g2.setColor(Color.RED);
                g2.drawString("THE ZONE [150%] - AWAKENED", x, y + 110);
            }
        }
    }

    public void drawOrbHUD(Graphics2D g2) {
        int x = gp.tileSize / 2;
        int y = gp.tileSize * 2;

        g2.setColor(Color.white);
        g2.setFont(new Font("Arial", Font.PLAIN, 20));
        g2.drawString("TECHNIQUE:", x, y - 10);

        g2.drawRect(x, y, 40, 40);

        g2.drawRect(x + 50, y, 40, 40);

        int state = gp.player.orbM.orbState;

        if(state == 1 || state == 3 || state == 5 || state == 6) {//brp
            g2.setColor(Color.BLUE);
            if(state == 5) g2.setColor(Color.RED);
            if(state == 6) g2.setColor(new Color(128,0,128));
            g2.fillOval(x + 5, y + 5, 30, 30);
        }

        if(state == 2 || state == 3 || state == 4) {
            if(state == 3) {
                g2.setColor(Color.BLUE);
                g2.fillOval(x + 55, y + 5, 30, 30);
            } else {
                g2.setColor(Color.WHITE);
                g2.fillOval(x + 5, y + 5, 30, 30);
                if(state == 4) g2.fillOval(x + 55, y + 5, 30, 30);
            }
        }
    }

    public void drawPauseScreen(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);

        int windowWidth = gp.tileSize * 14;
        int windowHeight = gp.tileSize * 8;
        int windowX = (gp.screenWidth - windowWidth) / 2;
        int windowY = (gp.screenHeight - windowHeight) / 2;

        g2.setColor(new Color(40, 40, 40, 230));
        g2.fillRoundRect(windowX, windowY, windowWidth, windowHeight, 25, 25);

        g2.setColor(Color.white);
        g2.setStroke(new BasicStroke(3));
        g2.drawRoundRect(windowX, windowY, windowWidth, windowHeight, 25, 25);

        int tabColumnWidth = gp.tileSize * 4;
        int textX = windowX + 30;
        int textY = windowY + gp.tileSize * 2;

        g2.setFont(arial_40);

        g2.setColor(currentTab == tabCharacter ? Color.yellow : Color.white);
        g2.drawString("Character", textX, textY);
        if(currentTab == tabCharacter) g2.drawString(">", textX - 25, textY);

        textY += gp.tileSize * 2;

        g2.setColor(currentTab == tabSystem ? Color.yellow : Color.white);
        g2.drawString("System", textX, textY);
        if(currentTab == tabSystem) g2.drawString(">", textX - 25, textY);

        textY += gp.tileSize * 2;

        g2.setColor(currentTab == tabHelp ? Color.yellow : Color.white);
        g2.drawString("Grimoire", textX, textY);
        if(currentTab == tabHelp) g2.drawString(">", textX - 25, textY);

        g2.setColor(Color.white);
        g2.drawLine(windowX + tabColumnWidth, windowY, windowX + tabColumnWidth, windowY + windowHeight);

        int contentX = windowX + tabColumnWidth + 30;
        int contentY = windowY + gp.tileSize; //*2
        g2.setFont(new Font("Arial", Font.PLAIN, 28));
        g2.setColor(Color.white);

        switch(currentTab) {
            case tabCharacter:
                drawCharacterStats(g2, contentX, contentY);
                break;

            case tabSystem:
                contentY += gp.tileSize * 2;
                g2.setFont(new Font("Arial", Font.PLAIN, 28));
                g2.drawString("Save Game (Auto-Save at Shrines)", contentX, contentY);

                contentY += gp.tileSize * 3;
                g2.setColor(Color.red);
                g2.setFont(new Font("Arial", Font.BOLD, 28));
                g2.drawString("[Q] Return to Title Screen", contentX, contentY);
                break;
            case tabHelp: drawHelpContent(g2, contentX, contentY); break;
        }
        g2.setFont(new Font("Arial", Font.ITALIC, 20));
        g2.setColor(Color.lightGray);
        g2.drawString("Press ESC to Resume", windowX + 20, windowY + windowHeight + 30);
    }

    public void drawHelpContent(Graphics2D g2, int x, int y) {
        g2.setColor(Color.cyan);
        g2.setFont(new Font("Arial", Font.BOLD, 20));
        g2.drawString("PAGE " + (helpPage + 1) + "/4  < A | D >", x, y - 20);
        g2.setColor(Color.white);
        g2.setFont(new Font("Arial", Font.PLAIN, 24));

        y += 20;

        if(helpPage == 0) {
            g2.drawString("[W,A,D] Movement", x, y); y+=40;
            g2.drawString("[SPACE] Light Attack", x, y); y+=40;
            g2.drawString("[S] Infinity Block (Costs CE)", x, y); y+=40;
            g2.drawString("[SHIFT] Dash (Costs Stamina)", x, y); y+=40;
            g2.drawString("[E] Interact / Save", x, y); y+=40;
            g2.drawString("[1] Blue technique", x, y); y+=40;
            g2.drawString("[2] Reversal cursed technique", x, y); y+=40;
            g2.drawString("[3] Dissipate all orbs", x, y); y+=40;
            g2.drawString("[C] Release orbs", x, y); y+=40;
            g2.drawString("[V] Hold Chant to amplify certain Orbs", x, y); y+=40;
        }
        else if(helpPage == 1) {
            g2.drawString("Level up by exorcising Curses", x, y); y+=40;
            g2.drawString("Body: Governs Physical prowess.", x, y); y+=40;
            g2.drawString("CE efficiency: Governs Cursed Energy prowess.", x, y); y+=40;
            g2.drawString("CE capacity: Amplifies one reserves.", x, y); y+=40;
            g2.drawString("While in battle, keep the heat on...", x, y); y+=30;
            g2.drawString("and maybe the Black Sparks will bless you...", x, y);
        }
        else if(helpPage == 2) {
            g2.drawString("THE ZONE: Land Black Flashes", x, y); y+=30;
            g2.drawString("to enter a heightened state.", x, y); y+=50;
            g2.drawString("ORBS: Combine the instances of your", x, y); y+=30;
            g2.drawString("technique to achieve a greater power.", x, y); y+=50;
        }
        else if(helpPage == 3) {
            g2.drawString("MISSION: SPECIAL GRADE EXORCISM", x, y); y+=40;
            g2.drawString("A Special Grade Spirit has manifested in the depths.", x, y); y+=30;
            g2.drawString("Continue to the right, and unlock all the seals.", x, y); y+=40;
            g2.drawString("Only then you can face the curse.", x, y); y+=30;
            g2.drawString("May thy curse be stronger...", x, y);
        }
    }

    public void drawCharacterStats(Graphics2D g2, int x, int y) {
        PlayerStats stats = gp.player.stats;

        g2.setFont(new Font("Arial", Font.BOLD, 20));
        g2.setColor(Color.yellow);
        g2.drawString("LEVEL " + gp.player.level + " SORCERER", x, y);
        y += 30;

        g2.setColor(Color.white);
        g2.setFont(new Font("Arial", Font.PLAIN, 18));

        if(gp.player.stats.inZone){
            g2.drawString("Strength: " + stats.strength + " + " + gp.player.stats.buffStrength, x, y);
            y += 25;
            g2.drawString("Resilience: " + stats.resilience + " + " + gp.player.stats.buffResilience, x, y);
            y += 25;
            g2.drawString("Stamina: " + (int) stats.stamina + "/" + stats.staminaMax, x, y);
            y += 25;
            g2.drawString("Stam Regen: " + String.format("%.1f + %.1f", stats.staminaRegen, gp.player.stats.buffStaminaRegen), x, y);
            y += 35;
        }
        else {
            g2.drawString("Strength: " + stats.strength, x, y);
            y += 25;
            g2.drawString("Resilience: " + stats.resilience, x, y);
            y += 25;
            g2.drawString("Stamina: " + (int) stats.stamina + "/" + stats.staminaMax, x, y);
            y += 25;
            g2.drawString("Stam Regen: " + String.format("%.1f", stats.staminaRegen), x, y);
            y += 35;
        }

        int col2X = x + 180;
        int resetY = y - 110;

        if(gp.player.stats.inZone) {
            g2.drawString("CE Output: " + stats.ceOutput + " + " + gp.player.stats.buffCeOutput, col2X, resetY);
            resetY += 25;
            g2.drawString("CE Max: " + stats.ceMax, col2X, resetY);
            resetY += 25;
            g2.drawString("CE Regen: " + String.format("%.1f + %.1f", stats.ceRegen, gp.player.stats.buffCeRegen), col2X, resetY);
        }
        else {
            g2.drawString("CE Output: " + stats.ceOutput, col2X, resetY);
            resetY += 25;
            g2.drawString("CE Max: " + stats.ceMax, col2X, resetY);
            resetY += 25;
            g2.drawString("CE Regen: " + String.format("%.1f", stats.ceRegen), col2X, resetY);
        }
        y += 20;
        g2.setColor(Color.gray);
        g2.drawLine(x, y, x + 400, y);
        y += 30;

        g2.setFont(new Font("Arial", Font.BOLD, 22));
        if(stats.attributePoints > 0) {
            g2.setColor(new Color(0, 255, 0));
            g2.drawString("POINTS AVAILABLE: " + stats.attributePoints, x, y);
            y += 30;

            g2.setFont(new Font("Arial", Font.PLAIN, 16));
            g2.setColor(Color.white);
            g2.drawString("[1] BODY BUILD (Str++, Res++, Stam)", x, y); y += 25;
            g2.drawString("[2] EFFICIENCY (Output++, Regen++)", x, y); y += 25;
            g2.drawString("[3] CAPACITY (Max CE++, Regen+)", x, y);
        } else {
            g2.setColor(Color.gray);
            g2.drawString("Next Point at: " + gp.player.exp +"/" + gp.player.nextLevelExp + " XP", x, y);
        }
    }

    public void drawBossHP(Graphics2D g2) {
        Enemy boss = null;
        for(int i = 0; i < gp.enemyList.size(); i++) {
            Enemy e = gp.enemyList.get(i);
            if(e instanceof com.ray.blacksparks.entity.Boss && e.alive) {
                boss = e;
                break;
            }
        }

        if(boss == null) return;

        int width = gp.screenWidth / 2;
        int height = 20;
        int x = gp.screenWidth / 2 - width / 2;
        int y = gp.screenHeight - 50;

        g2.setColor(new Color(30, 30, 30));
        g2.fillRect(x, y, width, height);

        double scale = (double)width / boss.maxLife;
        double currentWidth = scale * boss.life;

        if(((com.ray.blacksparks.entity.Boss)boss).enraging) {
            g2.setColor(new Color(138, 3, 3));
        } else {
            g2.setColor(new Color(100, 0, 200));
        }

        g2.fillRect(x, y, (int)currentWidth, height);

        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(2));
        g2.drawRect(x, y, width, height);

        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 20f));
        g2.drawString(boss.name, x, y - 10);
    }

    public void drawTransition(Graphics2D g2) {
        counter++;

        float alpha = counter * 0.05f;
        if(alpha > 1f) alpha = 1f;

        g2.setColor(Color.black);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);

        if(counter == 25) {

            gp.tileM.loadMap(gp.currentMapPath);

            gp.player.worldX = gp.nextX;
            gp.player.worldY = gp.nextY;
            gp.updateCamera();

            // gp.playSE(6);
        }
        if(counter > 50) {
            gp.gameState = gp.playState;
            //gp.currentMapPath = null;
            counter = 0;
        }
    }

    public void drawVictoryScreen(Graphics2D g2) {
        counter++;

        float alpha = counter * 0.005f;
        if(alpha > 1f) alpha = 1f;

        g2.setColor(Color.black);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);

        if(alpha > 0.5f) {
            g2.setColor(Color.white);
            g2.setFont(gp.ui.arial_80B); // Reuse font
            String text = "SPECIAL GRADE EXORCISED";
            int x = getXforCenteredText(text, g2);
            int y = gp.screenHeight / 2;

            float textAlpha = (alpha - 0.5f) * 2;
            if(textAlpha > 1f) textAlpha = 1f;
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, textAlpha));

            g2.drawString(text, x, y);

            g2.setFont(gp.ui.arial_40);
            String sub = "The Veil is lifted. For now...";
            x = getXforCenteredText(sub, g2);
            g2.drawString(sub, x, y + 80);

            if(counter > 400) {
                g2.setFont(g2.getFont().deriveFont(20f));
                g2.drawString("Press ESC to Return", x + 50, y + 160);
            }
        }
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
    }

    public int getXforCenteredText(String text, Graphics2D g2) {
        int length = (int)g2.getFontMetrics().getStringBounds(text, g2).getWidth();
        return gp.screenWidth / 2 - length / 2;
    }
}
