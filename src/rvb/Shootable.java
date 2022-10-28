package rvb;

import java.util.ArrayList;
import towers.Bullet;

public interface Shootable {
    public float getX();
    public float getY();
    public ArrayList<Bullet> getBulletsToRemove();
    public ArrayList<Bullet> getBullets();
    public void attacked(int power);
    public int getPower();
    public int getHitboxWidth();
    public boolean isDead();
    public int getExplodeRadius();
    public boolean getExplode();
    public boolean getFollow();
    public int getBulletSpeed();
    public int getRange();
    public boolean isMultipleShot();
    public ArrayList<Shootable> getEnemiesTouched();
}