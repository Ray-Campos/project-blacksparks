package com.ray.blacksparks.main;

import com.ray.blacksparks.entity.*;
import com.ray.blacksparks.input.KeyHandler;
import com.ray.blacksparks.tile.BackgroundManager;
import com.ray.blacksparks.tile.TileManager;
import com.ray.blacksparks.data.SaveLoad;

import javax.swing.JPanel;
import java.awt.*;
import java.util.ArrayList;

public class GamePanel extends JPanel implements Runnable {

    final int originalTileSize = 32; //32x32 tile
    final int scale = 2; //96x96 tile on screen

    public final int tileSize = originalTileSize * scale;
    public final int maxScreenCol = 20;//old
    public final int maxScreenRow = 12;//old
    public final int screenWidth = tileSize * maxScreenCol;
    public final int screenHeight = tileSize * maxScreenRow;

    public int maxWorldCol;
    public int maxWorldRow;
    public int worldWidth;
    public int worldHeight;

    public int cameraX = 0;
    public int cameraY = 0;

    public int gameState;
    public final int titleState = 0;
    public final int playState = 1;
    public final int pauseState = 2;
    public final int gameOverState = 3;
    public final int transitionState = 4;
    public final int victoryState = 5;

    public String currentMapPath;
    public int nextX, nextY;

    public UI ui = new UI(this);
    public EventHandler eHandler = new EventHandler(this);
    public SaveLoad saveLoad = new SaveLoad(this);

    Sound music = new Sound();
    Sound se = new Sound();
    Sound uiSe = new Sound();

    int currentMenuTrack = 0;
    boolean musicStarted = false;

    public int killCount = 0;
    public int killsToWin = 8; //goal
    public boolean levelComplete = false;
    public boolean demonGateOpen = false;


    int FPS = 60;

    Thread gameThread;

    public ArrayList<Enemy> enemyList = new ArrayList<>();
    public ArrayList<Projectile> projectileList = new ArrayList<>();
    public ArrayList<Entity> particleList = new ArrayList<>();
    public ArrayList<FloatingText> textList = new ArrayList<>();

    public BackgroundManager bgM = new BackgroundManager(this);
    public TileManager tileM = new TileManager(this);
    KeyHandler keyH = new KeyHandler(this);
    public CollisionChecker cChecker = new CollisionChecker(this);

    public Player player = new Player(this, keyH);

    public boolean bossDefeated = false;

    public GamePanel() {
        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setBackground(Color.gray);
        //this.setBackground(java.awt.Color.decode("#333333"));
        this.setDoubleBuffered(true);
        this.addKeyListener(keyH);
        this.setFocusable(true);

        maxWorldCol = 50;
        maxWorldRow = 14;

        worldWidth = tileSize * maxWorldCol;
        worldHeight = tileSize * maxWorldRow;

        gameState = titleState;
        currentMenuTrack = (int)(Math.random() * 3);
        playMusic(currentMenuTrack);
        musicStarted = true;
    }

    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        double drawInterval = 1000000000 / FPS; //0.01666 seconds per frame
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;

        while (gameThread != null) {
            currentTime = System.nanoTime();
            delta += (currentTime - lastTime) / drawInterval;
            lastTime = currentTime;

            if (delta >= 1) {
                update();
                repaint();
                delta--;
            }
        }
    }

    public void retry() {
        player.setDefaultValues();
        player.orbM.reset();

        killCount = 0;
        levelComplete = false;
        bossDefeated = false;
        player.stats.attributePoints = 0;
        player.stats.setDefaultValues();

        enemyList.clear();
        projectileList.clear();
        tileM.loadMap("/maps/map01.txt");
    }

    public void update() {
        if (gameState == titleState) {
            if (!music.isPlaying()) {
                currentMenuTrack++;
                if(currentMenuTrack > 2) {
                    currentMenuTrack = 0;
                }
                playMusic(currentMenuTrack);
            }
        }

        if (gameState == playState) {
            player.checkLevelUp();

            player.update();
            updateCamera();

            for (int i = 0; i < enemyList.size(); i++) {
                Enemy e = enemyList.get(i);
                if (e.alive) {
                    e.update();
                } else {
                    processEnemyDeath(e);

                    enemyList.remove(i);
                }
            }
            eHandler.checkEvent();

            for (int i = 0; i < projectileList.size(); i++) {
                Projectile p = projectileList.get(i);
                if (p.alive) {
                    p.update();

                    Rectangle bulletRect = new Rectangle(p.worldX + p.solidArea.x, p.worldY + p.solidArea.y, p.solidArea.width, p.solidArea.height);

                    if(p.owner != player) {
                        Rectangle playerRect = new Rectangle(player.worldX + player.solidArea.x, player.worldY + player.solidArea.y, player.solidArea.width, player.solidArea.height);

                        if(bulletRect.intersects(playerRect)) {
                            if(!player.invincible) {
                                player.takeDamage(p.damage, false);
                                p.alive = false;
                            }
                        }
                    }

                    for (Enemy e : enemyList) {
                        if (e.alive) {
                            if(p.owner == e) continue;
                            Rectangle enemyRect = new Rectangle(e.worldX + e.solidArea.x, e.worldY + e.solidArea.y, e.solidArea.width, e.solidArea.height);

                            if (bulletRect.intersects(enemyRect)) {
                                if(p.hitList.contains(e)) {
                                    continue;
                                }
                                int visualType = (((CursedOrb) p).type);
                                Rectangle overlap = bulletRect.intersection(enemyRect);

                                int impactX = overlap.x + overlap.width / 2;
                                int impactY = overlap.y + overlap.height / 2;
                                particleList.add(new ImpactEffect(this, impactX, impactY, visualType));

                                p.generateParticle(e, e);
                                int rawDamage = p.damage;
                                applyDamageToEnemy(e, p.damage, true);
                                boolean doStun = (visualType == 1);
                                e.applyKnockback(p.direction, p.knockbackPower, doStun);

                                this.playSE(5);
                                if(p.penetrates) {
                                    p.hitList.add(e);
                                } else {
                                    p.alive = false;
                                }
                                if(!p.penetrates) break;
                            }
                        }
                    }
                }
                if (!p.alive) projectileList.remove(i);
            }
            for(int i = 0; i < particleList.size(); i++) {
                Entity p = particleList.get(i);
                if(p.alive) {
                    p.update();
                } else {
                    particleList.remove(i);
                }
            }
        }
        for(int i = 0; i < textList.size(); i++) {
            FloatingText ft = textList.get(i);
            if(ft.alive) {
                ft.update();
            } else {
                textList.remove(i);
            }
        }

        if (gameState == pauseState) {
        }
    }

    public void updateCamera() {
        cameraX = player.worldX - player.screenX;
        cameraY = player.worldY - player.screenY;
        if(cameraX < 0) {
            cameraX = 0;
        }
        else if(cameraX > worldWidth - screenWidth) {
            cameraX = worldWidth - screenWidth;
        }

        if(cameraY < 0) {
            cameraY = 0;
        }
        else if(cameraY > worldHeight - screenHeight) {
            cameraY = worldHeight - screenHeight;
        }
    }

    public void playMusic(int i) {
        music.stop();

        music.setFile(i);
        music.play();
    }

    public void playMusicLoop(int i) {
        music.stop();
        music.setFile(i);
        music.loop();
    }

    public void stopMusic() {
        music.stop();
    }

    public void playSE(int i) {
        if (i == 7) {
            uiSe.setFile(i);
            uiSe.play();
        }
        else {
            se.setFile(i);
            se.play();
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        if (gameState == titleState) {
            ui.draw(g2);
        }
        else {
            bgM.draw(g2);
            tileM.draw(g2);

            for (Enemy e : enemyList) {
                e.draw(g2);
            }

            player.draw(g2);

            for (int i = 0; i < projectileList.size(); i++) {
                if (projectileList.get(i) != null) {
                    projectileList.get(i).draw(g2);
                }
            }

            /*if (levelComplete) {
                g2.setColor(Color.BLUE);
                g2.setFont(new Font("Arial", Font.BOLD, 40));
                g2.drawString("LEVEL COMPLETED", 300, 300);
            }*/
            for(int i = 0; i < particleList.size(); i++) {
                particleList.get(i).draw(g2);
            }
            for(int i = 0; i < textList.size(); i++) {
                textList.get(i).draw(g2);
            }

            ui.draw(g2);
        }

        g2.dispose();
    }

    public void addFloatingText(String text, int x, int y, Color c) {
        textList.add(new FloatingText(this, text, x, y, c));
    }

    private void processEnemyDeath(Enemy e) {
        player.exp += e.expReward;
        player.checkLevelUp();
        //System.out.println("Kill: " + (killCount + 1) + " | XP: " + player.exp);
        killCount++;

        if(killCount >= killsToWin && !levelComplete) {
            levelComplete = true;
            tileM.openGates();
            playSE(6); // Unlock Sound
            ui.showMessage("SEAL BROKEN!");
        }

        if(e instanceof com.ray.blacksparks.entity.Boss) {
            bossDefeated = true;
            gameState = victoryState;
            ui.counter = 0;
            stopMusic();
            playMusic(8);
        }
    }

    public void applyDamageToEnemy(Enemy e, int damage, boolean isProjectile) {

        if(e instanceof com.ray.blacksparks.entity.Boss) {
            com.ray.blacksparks.entity.Boss boss = (com.ray.blacksparks.entity.Boss)e;

            if(boss.daActive && isProjectile) {
                //playSE(10);
                addFloatingText("NEUTRALIZED", e.worldX, e.worldY - 30, Color.GRAY);
                return;
            }
        }

        boolean blackFlash = CombatMath.rollBlackFlash(player.stats);

        if(blackFlash) {
            damage = (int)(damage * 2.5); // 2.5x Multiplier

            //for(int k=0; k<10; k++) {
                // generateParticle(e, Color.BLACK);
           //}
            //playSE(9);
            if(player.stats.blackFlashCount == 0 && !player.stats.firstBlackFlash) {
                player.stats.firstBlackFlash = true;
                player.stats.attributePoints += 3;
                player.stats.upgrade(0);
                player.stats.upgrade(1);
                player.stats.upgrade(2);
                ui.showMessage("POTENTIAL UNLOCKED!");
            }
            player.stats.blackFlashCount++;

            int newTier = (player.stats.inZone) ? 2 : 1;
            player.stats.activateZone(newTier);

            addFloatingText("BLACK FLASH!", e.worldX, e.worldY - 20, Color.BLACK);
        }
        e.takeDamage(damage);
    }

    public TileManager getTileM() {
        return tileM;
    }
}
