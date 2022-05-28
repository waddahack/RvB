package towser;

import java.util.ArrayList;
import towers.Bullet;

public interface Shootable {
    public float getX();
    public float getY();
    public ArrayList<Bullet> getBulletsToRemove();
    public ArrayList<Bullet> getBullets();
    public void attacked(int power);
    public int getPower();
    public int getWidth();
    public boolean isDead();
    public boolean getFollow();
    public int getBulletSpeed();
    public int getRange();
    public boolean isMultipleShot();
    public ArrayList<Shootable> getEnemiesTouched();
}
