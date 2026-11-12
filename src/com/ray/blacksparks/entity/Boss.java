package com.ray.blacksparks.entity;

import com.ray.blacksparks.main.GamePanel;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Boss extends Enemy {

    // SPRITES
    private final int IDLE_FRAMES = 9;
    private final int RUN_FRAMES = 6;
    private final int ATTACK_FRAMES = 12;
    private final int DEATH_FRAMES = 20;

    private BufferedImage[] idleSprites;
    private BufferedImage[] runSprites;
    private BufferedImage[] attackSprites;
    private BufferedImage[] deathSprites;

    // STATES
    public boolean dying = false;
    public boolean enraging = false;
    public int attackCooldown = 0;

    // DOMAIN AMPLIFICATION
    public boolean daActive = false;
    public int daTimer = 0;
    public int daCooldown = 0;

    // RANGED COMBAT
    public boolean charging = false;
    public int chargeCounter = 0;
    private final int CHARGE_DURATION = 60; // 1 second charge

    // DETERMINISTIC TIMER
    public int rangeAttackTimer = 0;

    public Boss(GamePanel gp, int worldX, int worldY) {
        super(gp);
        this.worldX = worldX;
        this.worldY = worldY - gp.tileSize; // Spawn in air

        name = "NightBorne";

        maxLife = 1000;
        life = maxLife;
        attackPower = 50;
        expReward = 5000;
        speed = 3;

        // Floor friction fix (90 height)
        solidArea = new Rectangle(16, 16, 48, 96);

        solidAreaDefaultX = solidArea.x;
        solidAreaDefaultY = solidArea.y;

        getImage();
    }

    public void getImage() {
        int scale = 3;
        idleSprites = setupBossSprites("/enemy/boss/idle.png", 0, IDLE_FRAMES, scale);
        runSprites = setupBossSprites("/enemy/boss/run.png", 1, RUN_FRAMES, scale);
        attackSprites = setupBossSprites("/enemy/boss/attack.png", 2, ATTACK_FRAMES, scale);
        deathSprites = setupBossSprites("/enemy/boss/death.png", 4, DEATH_FRAMES, scale);

        this.drawOffsetX = solidArea.x + (solidArea.width - spriteWidth) / 2;
        drawOffsetY = -70;
    }

    private BufferedImage[] setupBossSprites(String path, int row, int frameCount, int scale) {
        return super.setupSprites(path, frameCount, scale);
    }

    @Override
    public void update() {
        if (charging) {
            // GRAVITY ONLY
            velocityY += gravity;
            if(velocityY > 15) velocityY = 15;

            collisionOn = false;
            // for vertical check
            int originalSpeed = speed;
            speed = velocityY;
            String originalDir = direction;
            direction = "down";

            gp.cChecker.checkTile(this);

            if(collisionOn) {
                // SNAP TO GRID
                int tileY = (worldY + solidArea.y + solidArea.height + velocityY) / gp.tileSize;
                worldY = tileY * gp.tileSize - solidArea.height - solidArea.y;
                velocityY = 0;
            } else {
                worldY += velocityY;
            }

            speed = originalSpeed;
            direction = originalDir;

            updateAnimation();
            return; // FREEZE X MOVEMENT
        }
        super.update();
    }

    @Override
    public void setAction() {
        if(dying || attacking || charging) return;

        // PHASE 2 TRIGGER (Heal to 750, Speed +3)
        if(!enraging && life < maxLife/2) {
            enraging = true;
            speed += 3;
            attackPower += 20;
            life = 750;
            gp.addFloatingText("DOMAIN EXPANSION!", worldX, worldY - 50, Color.RED);
            gp.playSE(9);
        }

        // DA CYCLE LOGIC
        if(enraging) {
            if(daActive) {
                // Lasts 5 Seconds
                daTimer--;
                if(daTimer <= 0) {
                    daActive = false;
                    daCooldown = 240; // 4 seconds cooldown
                    gp.addFloatingText("DA DISSIPATED", worldX, worldY - 20, Color.GRAY);
                }
            } else {
                if(daCooldown > 0) {
                    daCooldown--;
                } else {
                    daActive = true;
                    daTimer = 300;
                    gp.bgM.setDomainState(true);
                    gp.addFloatingText("DOMAIN AMPLIFICATION", worldX, worldY - 80, new Color(139, 0, 0));
                }
            }
        }

        // DETERMINISTIC RANGED LOGIC
        rangeAttackTimer++;
        int interval = enraging ? 150 : 240;
        int dist = Math.abs(gp.player.worldX - worldX);
        int verticalDist = Math.abs(gp.player.worldY - worldY);

        if (rangeAttackTimer > interval && dist > gp.tileSize * 3) {
            charging = true;
            chargeCounter = 0;
            spriteNum = 0;
            rangeAttackTimer = 0;

            if(gp.player.worldX < worldX) direction = "left";
            else direction = "right";

            return;
        }

        // STANDARD CHASE
        if(gp.player.worldX < worldX) direction = "left";
        else direction = "right";

        // MELEE TRIGGER
        int meleeRange = gp.tileSize * 3;
        if(dist < meleeRange && verticalDist < gp.tileSize * 2) {
            if (attackCooldown == 0) {
                attacking = true;
                spriteCounter = 0;
                spriteNum = 0;
            }
        }

        if(attackCooldown > 0) attackCooldown--;
    }

    @Override
    public void updateAnimation() {
        spriteCounter++;
        int frameSpeed = enraging ? 4 : 6;

        if(dying) {
            if(spriteCounter > frameSpeed) {
                spriteNum++;
                spriteCounter = 0;

                // 3-FRAME LETHAL EXPLOSION
                if(spriteNum == 14 || spriteNum == 15 || spriteNum == 16) {
                    checkExplosionCollision();
                }

                if(spriteNum >= DEATH_FRAMES) {
                    alive = false;
                    gp.ui.showMessage("SPECIAL GRADE EXORCISED");
                    gp.bgM.setDomainState(false);
                }
            }
            if(deathSprites != null && spriteNum < DEATH_FRAMES) image = deathSprites[spriteNum];
        }
        else if(charging) {
            if(spriteCounter > 5) {
                spriteNum++;
                if(spriteNum >= IDLE_FRAMES) spriteNum = 0;
                spriteCounter = 0;
            }
            if(idleSprites != null) image = idleSprites[spriteNum];

            chargeCounter++;
            if(chargeCounter >= CHARGE_DURATION) {
                charging = false;
                chargeCounter = 0;

                if(enraging) performSweepAttack();
                else performRangedAttack();

                attackCooldown = 60;
            }
        }
        else if(attacking) {
            if(spriteCounter > frameSpeed) {
                spriteNum++;
                spriteCounter = 0;

                if(spriteNum == 10) checkAttackCollision();

                if(spriteNum >= ATTACK_FRAMES) {
                    attacking = false;
                    spriteNum = 0;
                    attackCooldown = enraging ? 40 : 80;
                }
            }
            if(attackSprites != null && spriteNum < ATTACK_FRAMES) image = attackSprites[spriteNum];
        }
        else {
            if(spriteCounter > frameSpeed) {
                spriteNum++;
                if(spriteNum >= RUN_FRAMES) spriteNum = 0;
                spriteCounter = 0;
            }
            if(runSprites != null) image = runSprites[spriteNum];
        }
    }

    public void performRangedAttack() {
        CursedOrb orb = new CursedOrb(gp, 5);
        int startX = worldX + solidArea.width/2;
        int startY = worldY + solidArea.y + 20;
        int targetX = gp.player.worldX + gp.player.solidArea.x;
        int targetY = gp.player.worldY + gp.player.solidArea.y;
        orb.setVector(startX, startY, targetX, targetY);
        orb.damage = 45;
        orb.owner = this;
        gp.projectileList.add(orb);
    }

    public void performSweepAttack() {
        int startX = worldX + solidArea.width/2;
        int startY = worldY + solidArea.y + 20;
        int targetX = gp.player.worldX + gp.player.solidArea.x;
        int targetY = gp.player.worldY + gp.player.solidArea.y;
        int spread = 90;
        for(int i = -1; i <= 1; i++) {
            CursedOrb orb = new CursedOrb(gp, 6);
            orb.setVector(startX, startY, targetX, targetY + (i * spread));
            orb.damage = 20;
            orb.owner = this;
            gp.projectileList.add(orb);
        }
    }

    public void checkExplosionCollision() {
        int radius = 200;
        Rectangle boomBox = new Rectangle(worldX + solidArea.width/2 - radius/2, worldY + solidArea.height/2 - radius/2, radius, radius);
        this.debugAttackBox = boomBox;
        if(boomBox.intersects(new Rectangle(gp.player.worldX + gp.player.solidArea.x, gp.player.worldY + gp.player.solidArea.y, gp.player.solidArea.width, gp.player.solidArea.height))){
            gp.player.takeDamage(200, true);
            gp.playSE(5);
        }
    }

    @Override
    public void checkPlayerCollision() {}

    public void checkAttackCollision() {
        int attackW = 140;
        int attackH = 150;
        int attackX = (direction.equals("left")) ? worldX - attackW + 32: worldX + solidArea.width - 32;
        Rectangle attackBox = new Rectangle(attackX, worldY - 40, attackW, attackH);
        this.debugAttackBox = attackBox;
        if(attackBox.intersects(new Rectangle(gp.player.worldX + gp.player.solidArea.x, gp.player.worldY + gp.player.solidArea.y, gp.player.solidArea.width, gp.player.solidArea.height))) {
            gp.player.takeDamage(attackPower, enraging);
            if(gp.player.worldX < worldX) gp.player.worldX -= 3;
            else gp.player.worldX += 3;
        }
    }

    @Override
    public void draw(Graphics2D g2) {
        int screenX = worldX - gp.cameraX;
        int screenY = worldY - gp.cameraY;

        if(worldX + gp.tileSize*2 > gp.cameraX && worldX - gp.tileSize*2 < gp.cameraX + gp.screenWidth) {
            if(daActive) {
                java.awt.Composite original = g2.getComposite();
                g2.setColor(new java.awt.Color(50, 0, 0));
                g2.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 0.6f));
                g2.fillOval(screenX + solidArea.x - 10, screenY + solidArea.y - 10, solidArea.width + 20, solidArea.height + 20);
                g2.setComposite(original);
            }
        }
        super.draw(g2);
    }

    @Override
    public void takeDamage(int damage) {
        if(dying) return;

        // 75% Damage resistance during Domain Amplification
        if(daActive) super.takeDamage((damage/2) + (damage/4));
        else super.takeDamage(damage);

        if(life <= 0) {
            life = 0; speed = 0; dying = true; alive = true;
            gp.bgM.setDomainState(false); collisionOn = false;
        }
    }

    // JUGGERNAUT STATE (Knockback/Stun Immunity)
    @Override
    public void applyKnockback(String sourceDir, int power, boolean isStun) {
        if(daActive || enraging) return; // Immune to crowd control during DA or Phase 2

        if(this.attacking) {
            this.attacking = false;
            this.spriteNum = 0;
        }

        if(isStun) {
            this.stunTimer = 60;
        }

        if(power > 0) {
            this.knockback = true;
            if(sourceDir.equals("left")) worldX -= power;
            else worldX += power;
        }
    }
}