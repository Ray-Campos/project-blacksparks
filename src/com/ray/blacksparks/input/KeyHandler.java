package com.ray.blacksparks.input;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;

import com.ray.blacksparks.main.GamePanel;

public class KeyHandler implements KeyListener {

    GamePanel gp;
    public KeyHandler(GamePanel gp) {
        this.gp = gp;
    }

    public boolean upPressed, downPressed, leftPressed, rightPressed;
    public boolean key1, key2, key3, keyC;
    public boolean spacePressed;
    public boolean keySHIFT;
    public boolean keyCONTROL;
    public boolean keyV;

    @Override
    public void keyTyped(KeyEvent e) {
        //not used, too buggy
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();

        if (gp.gameState == gp.titleState) {
            if(code == KeyEvent.VK_W) {
                gp.ui.commandNum--;
                if(gp.ui.commandNum == 1 && !new File("save.dat").exists()) {
                    gp.ui.commandNum--;
                }
                if(gp.ui.commandNum < 0) gp.ui.commandNum = 2;
            }
            if(code == KeyEvent.VK_S) {
                gp.ui.commandNum++;
                if(gp.ui.commandNum == 1 && !new File("save.dat").exists()) {
                    gp.ui.commandNum++;
                }
                if(gp.ui.commandNum > 2) gp.ui.commandNum = 0;
            }
            if (code == KeyEvent.VK_ENTER) {
                if(gp.ui.commandNum == 0) {
                    gp.retry();
                    gp.player.stats.deactivateZone();
                    gp.gameState = gp.playState;
                    gp.stopMusic();
                    gp.playMusicLoop(3);
                }
                if(gp.ui.commandNum == 1) {
                    gp.saveLoad.load();
                    gp.gameState = gp.playState;
                    gp.player.orbM.reset();
                    gp.player.stats.deactivateZone();
                    gp.stopMusic();
                    gp.playMusicLoop(3);
                }
                if(gp.ui.commandNum == 2) {
                    System.exit(0);
                }
            }
        }

        else if (gp.gameState == gp.gameOverState) {
            if(code == KeyEvent.VK_SPACE) {
                gp.retry();
                gp.gameState = gp.titleState;
                gp.ui.counter = 0;
                gp.ui.subState = 0;
                gp.playMusic(0);
            }
        }
        else if (gp.gameState == gp.victoryState) {
            if(code == KeyEvent.VK_ESCAPE) {
                gp.gameState = gp.titleState;
                gp.ui.counter = 0;
                gp.ui.subState = 0;
                gp.playMusic(0);
            }
        }

        else if (gp.gameState == gp.playState) {
            if (code == KeyEvent.VK_W) upPressed = true;
            if (code == KeyEvent.VK_S) downPressed = true;
            if (code == KeyEvent.VK_A) leftPressed = true;
            if (code == KeyEvent.VK_D) rightPressed = true;
            if (code == KeyEvent.VK_1) key1 = true;
            if (code == KeyEvent.VK_2) key2 = true;
            if (code == KeyEvent.VK_3) key3 = true;
            if (code == KeyEvent.VK_C) keyC = true;
            if (code == KeyEvent.VK_SPACE) spacePressed = true;
            if (code == KeyEvent.VK_SHIFT) keySHIFT = true;
            if (code == KeyEvent.VK_S) keyCONTROL = true;
            if (code == KeyEvent.VK_V) keyV = true;
            if (code == KeyEvent.VK_E) {
                gp.player.interact();
            }
            if (code == KeyEvent.VK_ESCAPE) {
                gp.gameState = gp.pauseState;
            }
        }

        else if (gp.gameState == gp.pauseState) {
            if (code == KeyEvent.VK_ESCAPE) {
                gp.gameState = gp.playState;
            }

            if (code == KeyEvent.VK_W) {
                gp.ui.currentTab--;
                if(gp.ui.currentTab < 0) gp.ui.currentTab = 2;
            }
            if (code == KeyEvent.VK_S) {
                gp.ui.currentTab++;
                if(gp.ui.currentTab > 2) gp.ui.currentTab = 0;
            }
            if(code == KeyEvent.VK_A) {
                if(gp.ui.currentTab == gp.ui.tabHelp) {
                    gp.ui.helpPage--;
                    if(gp.ui.helpPage < 0) gp.ui.helpPage = 3;
                }
            }
            if(code == KeyEvent.VK_D) {
                if(gp.ui.currentTab == gp.ui.tabHelp) {
                    gp.ui.helpPage++;
                    if(gp.ui.helpPage > 3) gp.ui.helpPage = 0;
                }
            }

            if (gp.ui.currentTab == gp.ui.tabCharacter) {
                if(gp.player.stats.attributePoints > 0) {
                    if (code == KeyEvent.VK_1) {
                        gp.player.stats.upgrade(0);
                        gp.playSE(7);
                    }
                    if (code == KeyEvent.VK_2) {
                        gp.player.stats.upgrade(1);
                        gp.playSE(7);
                    }
                    if (code == KeyEvent.VK_3) {
                        gp.player.stats.upgrade(2);
                        gp.playSE(7);
                    }
                }
            }
            if (code == KeyEvent.VK_Q) {
                if(gp.ui.currentTab == gp.ui.tabSystem) {
                    gp.stopMusic();
                    gp.gameState = gp.titleState;
                    gp.playMusic(0);
                    gp.ui.currentTab = 0;
                    gp.ui.commandNum = 0;
                }
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();

        if (code == KeyEvent.VK_W) {
            upPressed = false;
        }
        if (code == KeyEvent.VK_S) {
            downPressed = false;
        }
        if (code == KeyEvent.VK_A) {
            leftPressed = false;
        }
        if (code == KeyEvent.VK_D) {
            rightPressed = false;
        }
        if (code == KeyEvent.VK_1) key1 = false;
        if (code == KeyEvent.VK_2) key2 = false;
        if (code == KeyEvent.VK_3) key3 = false;
        if (code == KeyEvent.VK_C) keyC = false;
        if (code == KeyEvent.VK_SPACE) spacePressed = false;
        if (code == KeyEvent.VK_SHIFT) keySHIFT = false;
        if (code == KeyEvent.VK_S) keyCONTROL = false;
        if (code == KeyEvent.VK_V) keyV = false;
    }
}
