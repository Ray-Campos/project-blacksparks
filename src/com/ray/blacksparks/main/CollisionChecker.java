package com.ray.blacksparks.main;

import com.ray.blacksparks.entity.Entity;

public class CollisionChecker {

    GamePanel gp;

    public CollisionChecker(GamePanel gp) {
        this.gp = gp;
    }

    //you're on your own now, this can break everything
    public void checkTile(Entity entity) {

        // Get the entity's solid area position in the world
        int entityLeftWorldX = entity.worldX + entity.solidArea.x;
        int entityRightWorldX = entity.worldX + entity.solidArea.x + entity.solidArea.width - 1;
        int entityTopWorldY = entity.worldY + entity.solidArea.y;
        int entityBottomWorldY = entity.worldY + entity.solidArea.y + entity.solidArea.height - 1;

        int entityLeftCol = entityLeftWorldX / gp.tileSize;
        int entityRightCol = entityRightWorldX / gp.tileSize;
        int entityTopRow = entityTopWorldY / gp.tileSize;
        int entityBottomRow = entityBottomWorldY / gp.tileSize;

        if(entityLeftCol < 0 || entityRightCol >= gp.maxWorldCol ||
                entityTopRow < 0 || entityBottomRow >= gp.maxWorldRow) {

            entity.collisionOn = true;
            return;
        }
        int tileNum1, tileNum2;

        switch(entity.direction) {
            case "up":
                entityTopRow = (entityTopWorldY - entity.speed) / gp.tileSize;
                tileNum1 = gp.tileM.mapTileNum[entityLeftCol][entityTopRow];
                tileNum2 = gp.tileM.mapTileNum[entityRightCol][entityTopRow];
                if(gp.tileM.tile[tileNum1] != null && gp.tileM.tile[tileNum1].collision ||
                        gp.tileM.tile[tileNum2] != null && gp.tileM.tile[tileNum2].collision) {
                    entity.collisionOn = true;
                }
                break;
            case "down":
                entityBottomRow = (entityBottomWorldY + entity.speed) / gp.tileSize;
                tileNum1 = gp.tileM.mapTileNum[entityLeftCol][entityBottomRow];
                tileNum2 = gp.tileM.mapTileNum[entityRightCol][entityBottomRow];
                if((gp.tileM.tile[tileNum1] != null && gp.tileM.tile[tileNum1].collision) ||
                        (gp.tileM.tile[tileNum2] != null && gp.tileM.tile[tileNum2].collision)) {
                    entity.collisionOn = true;
                }
                break;
            case "left":
                entityLeftCol = (entityLeftWorldX - entity.speed) / gp.tileSize;
                tileNum1 = gp.tileM.mapTileNum[entityLeftCol][entityTopRow];
                tileNum2 = gp.tileM.mapTileNum[entityLeftCol][entityBottomRow];
                if((gp.tileM.tile[tileNum1] != null && gp.tileM.tile[tileNum1].collision) ||
                        (gp.tileM.tile[tileNum2] != null && gp.tileM.tile[tileNum2].collision)) {
                    entity.collisionOn = true;
                }
                break;
            case "right":
                entityRightCol = (entityRightWorldX + entity.speed) / gp.tileSize;
                tileNum1 = gp.tileM.mapTileNum[entityRightCol][entityTopRow];
                tileNum2 = gp.tileM.mapTileNum[entityRightCol][entityBottomRow];
                if((gp.tileM.tile[tileNum1] != null && gp.tileM.tile[tileNum1].collision) ||
                        (gp.tileM.tile[tileNum2] != null && gp.tileM.tile[tileNum2].collision)) {
                    entity.collisionOn = true;
                }
                break;
        }
    }
}
