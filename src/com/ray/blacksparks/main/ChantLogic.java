package com.ray.blacksparks.main;

import com.ray.blacksparks.entity.Player;
import java.awt.Color;

public class ChantLogic {

    GamePanel gp;
    Player player;

    private final String[] BLUE_CHANT = { "Phase...", "Twilight...", "Eyes of Wisdom" };
    private final String[] RED_CHANT = { "Phase...", "Paramita...", "Pillars of Light" };
    private final String[] PURPLE_CHANT = { "Nine Ropes...", "Polarized Light...", "Crow and Declaration...", "Between Front and Back" };

    public boolean isChanting = false;
    public boolean chantComplete = false;

    private int timer = 0;
    private int lineIndex = 0;
    private int currentOrbState = 0;

    private final int TEXT_DELAY = 25;

    public ChantLogic(GamePanel gp, Player player) {
        this.gp = gp;
        this.player = player;
    }

    public void update(int orbState) {
        if(!isChanting || currentOrbState != orbState) {
            isChanting = true;
            chantComplete = false;
            currentOrbState = orbState;
            timer = 0;
            lineIndex = 0;
        }
        if(chantComplete) {
            return;
        }
        timer++;

        if(timer >= TEXT_DELAY) {
            timer = 0;
            spawnNextLine(orbState);

            lineIndex++;
            if(isChantingFinished(orbState)) {
                chantComplete = true;
                gp.playSE(6);
                gp.ui.showMessage("OUTPUT: 200%");
            }
        }
    }

    public void reset() {
        isChanting = false;
        chantComplete = false;
        timer = 0;
        lineIndex = 0;
    }

    private void spawnNextLine(int orbState) {
        String text = "";
        Color c = Color.white;
        String[] currentArr = null;

        if(orbState == 1 || orbState == 3) {
            currentArr = BLUE_CHANT;
            c = Color.CYAN;
        }
        else if(orbState == 5) {
            currentArr = RED_CHANT;
            c = Color.RED;
        }
        else if(orbState == 6) {
            currentArr = PURPLE_CHANT;
            c = new Color(180, 0, 255);
        }

        if(currentArr != null && lineIndex < currentArr.length) {
            text = currentArr[lineIndex];
            gp.addFloatingText(text, player.worldX + 20, player.worldY - 20 - (lineIndex * 15), c);
            //gp.playSE(9);
        }
    }

    private boolean isChantingFinished(int orbState) {
        if(orbState == 1 || orbState == 3) return lineIndex >= BLUE_CHANT.length;
        if(orbState == 5) return lineIndex >= RED_CHANT.length;
        if(orbState == 6) return lineIndex >= PURPLE_CHANT.length;
        return true;
    }
}
