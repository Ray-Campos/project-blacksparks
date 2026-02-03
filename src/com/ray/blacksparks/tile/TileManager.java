package com.ray.blacksparks.tile;

import com.ray.blacksparks.entity.Boss;
import com.ray.blacksparks.entity.CursedSpirit;
import com.ray.blacksparks.entity.Slime;
import com.ray.blacksparks.main.GamePanel;
import com.ray.blacksparks.entity.Enemy;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;

public class TileManager {

    GamePanel gp;
    public Tile[] tile;
    public int[][] mapTileNum;

    public TileManager(GamePanel gp) {
        this.gp = gp;

        tile = new Tile[10];

        mapTileNum = new int[gp.maxWorldCol][gp.maxWorldRow];

        getTileImage();
        loadMap("/maps/map01.txt");
    }

    public void getTileImage() {
        try {

            tile[1] = new Tile();
            tile[1].image = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/tiles/rock.png")));
            tile[1].collision = true;

            tile[2] = new Tile();
            tile[2].image = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/tiles/gate.png")));
            tile[2].collision = true;

            tile[3] = new Tile();
            tile[3].image = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/tiles/shrine.png")));
            tile[3].collision = false;

            tile[4] = new Tile();
            tile[4].image = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/tiles/brick.png")));
            tile[4].collision = true;

            tile[5] = new Tile();
            tile[5].image = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/tiles/brick2.png")));
            tile[5].collision = true;

            tile[6] = new Tile();
            tile[6].image = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/tiles/brick3.png")));
            tile[6].collision = true;

            tile[7] = new Tile();
            tile[7].image = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/tiles/column.png")));
            tile[7].collision = true;

            tile[8] = new Tile();
            tile[8].image = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/tiles/carpet.png")));
            tile[8].collision = true;

            tile[9] = new Tile();
            tile[9].image = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/tiles/gate2.png")));
            tile[9].collision = true;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadMap(String filePath) {
        try {
            gp.currentMapPath = filePath;
            gp.bgM.setMapBackground(filePath);

            gp.enemyList.clear();
            gp.projectileList.clear();
            gp.particleList.clear();

            InputStream is = getClass().getResourceAsStream(filePath);
            BufferedReader br = new BufferedReader(new InputStreamReader(Objects.requireNonNull(is)));

            String line = br.readLine();
            String[] maxDimensions = line.split(" ");

            gp.maxWorldCol = Integer.parseInt(maxDimensions[0]);
            gp.maxWorldRow = Integer.parseInt(maxDimensions[1]);
            gp.worldWidth = gp.tileSize * gp.maxWorldCol;
            gp.worldHeight = gp.tileSize * gp.maxWorldRow;

            mapTileNum = new int[gp.maxWorldCol][gp.maxWorldRow];

            int col = 0;
            int row = 0;

            while (col < gp.maxWorldCol && row < gp.maxWorldRow) {
                line = br.readLine();

                if(line == null) break;

                String[] tokens = line.split(" ");

                for(String token : tokens) {
                    if(col >= gp.maxWorldCol) break;

                    if (token.equals("#")) {
                        int worldX = col * gp.tileSize;
                        int worldY = row * gp.tileSize;
                        gp.enemyList.add(new Slime(gp, worldX, worldY));
                        mapTileNum[col][row] = 0;
                    }
                    else if (token.equals("G")) {
                        mapTileNum[col][row] = 2;
                    }
                    else if (token.equals("E")) {
                        int worldX = col * gp.tileSize;
                        int worldY = row * gp.tileSize;
                        gp.enemyList.add(new com.ray.blacksparks.entity.FlyingEye(gp, worldX, worldY));
                        mapTileNum[col][row] = 0;
                    }
                    else if (token.equals("D") && !gp.demonGateOpen) {
                        int worldX = col * gp.tileSize;
                        int worldY = row * gp.tileSize;
                        gp.enemyList.add(new com.ray.blacksparks.entity.Demon(gp, worldX, worldY));
                        mapTileNum[col][row] = 0;
                    }
                    else if (token.equals("R")) {
                        int worldX = col * gp.tileSize;
                        int worldY = row * gp.tileSize;
                        gp.enemyList.add(new com.ray.blacksparks.entity.CursedSpirit2(gp, worldX, worldY));
                        mapTileNum[col][row] = 0;
                    }
                    else if (token.equals("B")) {
                        if(!gp.bossDefeated) {
                            int worldX = col * gp.tileSize;
                            int worldY = row * gp.tileSize;
                            gp.enemyList.add(new Boss(gp, worldX, worldY));
                        }
                        mapTileNum[col][row] = 0;
                    }
                    else if (token.equals("S")) {
                        int worldX = col * gp.tileSize;
                        int worldY = row * gp.tileSize;
                        gp.enemyList.add(new CursedSpirit(gp, worldX, worldY));
                        mapTileNum[col][row] = 0;
                    }
                    else {
                        try {
                            int num = Integer.parseInt(token);
                            if (num == 9 && gp.demonGateOpen) {
                                num = 0;
                            }
                            mapTileNum[col][row] = num;
                        } catch(NumberFormatException e) {
                            mapTileNum[col][row] = 0;
                        }
                    }
                    col++;
                }
                col = 0;
                row++;
            }
            br.close();
        } catch (Exception e) {
            System.out.println("error loading map: " + filePath);
            e.printStackTrace();
        }
    }


    //NEW METHOD:
    public void openGates() {
        for(int col = 0; col < gp.maxWorldCol; col++) {
            for(int row = 0; row < gp.maxWorldRow; row++) {
                if(mapTileNum[col][row] == 2) {
                    mapTileNum[col][row] = 0;
                }
            }
        }
    }

    public void openDemonGate() {
        for(int col = 0; col < gp.maxWorldCol; col++) {
            for(int row = 0; row < gp.maxWorldRow; row++) {
                if(mapTileNum[col][row] == 9) {
                    mapTileNum[col][row] = 0;
                }
            }
        }
    }

    public void draw(Graphics2D g2) {
        int worldCol = 0;
        int worldRow = 0;

        while (worldCol < gp.maxWorldCol && worldRow < gp.maxWorldRow) {
            int tileNum = mapTileNum[worldCol][worldRow];

            int worldX = worldCol * gp.tileSize;
            int worldY = worldRow * gp.tileSize;

            int screenX = worldX - gp.cameraX;
            int screenY = worldY - gp.cameraY;

            if(worldX + gp.tileSize > gp.cameraX &&
                    worldX - gp.tileSize < gp.cameraX + gp.screenWidth &&
                    worldY + gp.tileSize > gp.cameraY &&
                    worldY - gp.tileSize < gp.cameraY + gp.screenHeight) {

                if (tileNum != 0 && tile[tileNum] != null) {
                    g2.drawImage(tile[tileNum].image, screenX, screenY, gp.tileSize, gp.tileSize, null);
                }
            }

            worldCol++;
            if (worldCol == gp.maxWorldCol) {
                worldCol = 0;
                worldRow++;
            }
        }
    }
}