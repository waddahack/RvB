package rvb;

import ennemies.Enemy;
import java.util.ArrayList;
import towers.Bullet;
import towers.Tower;

public interface Shootable {
    public float getX();
    public float getY();
    public ArrayList<Bullet> getBulletsToRemove();
    public ArrayList<Bullet> getBullets();
    public void attacked(Shootable attacker);
    public void updateStats(Enemy e);
    public void updateStats(Tower t);
    public int getRange();
    public float getPower();
    public float getShootRate();
    public int getExplodeRadius();
    public int getBulletSpeed();
    public float getSlow();
    public int getHitboxWidth();
    public boolean isDead();
    public boolean getExplode();
    public boolean getFollow();
    public boolean isMultipleShot();
    public ArrayList<Shootable> getEnemiesTouched();
}
