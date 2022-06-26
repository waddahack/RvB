package towers;

import managers.SoundManager;
import towser.Towser;
import static towser.Towser.ref;

public class CircleTower extends Tower{

    public static int startPrice = 375;
    public static int priceP = startPrice;
    
    public CircleTower(){
        super("circleTower");
        textureStatic = Towser.textures.get(("circleTower"));
        textures.add(Towser.textures.get("grass")); 
        textures.add(textureStatic);
        canRotate = false;
        price = priceP;
        life = 100;
        width = 4*Towser.unite/5;
        size = width;
        totalMoneySpent = priceP;
        name = "Razingun";
        explode = false;
        follow = false;
        isMultipleShot = true;
        clip = SoundManager.Instance.getClip("multicannon");
        SoundManager.Instance.setClipVolume(clip, volume);
        bulletSprite = Towser.textures.get("bullet");
        growth = 4*ref;
        
        range = (int) (1.2*Towser.unite);
        power = 12;
        shootRate = 0.7f;
        bulletSpeed = 25;
        upgrades.add(new Upgrade("Range", range, 1.2f, "Multiplicate", 100f, 1.5f, 2));
        upgrades.add(new Upgrade("Power", power, 5f, "Add", 200f, 2f, 1));
        upgrades.add(new Upgrade("Attack speed", shootRate, 1.5f, "Multiplicate", 250f, 2f, 2));

    }
    
    @Override
    protected void raisePrice(){
        priceP *= 1.08;
        price = priceP;
    }
    
    @Override
    public void shoot(){
        super.shoot();
        
        lastShoot = System.currentTimeMillis();

        bullets.clear();
        bullets.add(new Bullet(this, x-size/2, y, x-100, y, size/6, bulletSprite, true, 75));
        bullets.add(new Bullet(this, x-size/2, y-size/2, x-100, y-100, size/6, bulletSprite, true, 0));
        bullets.add(new Bullet(this, x, y-size/2, x, y-100, size/6, bulletSprite, true, 50));
        bullets.add(new Bullet(this, x+size/2, y, x+100, y, size/6, bulletSprite, true, 25));
        bullets.add(new Bullet(this, x+size/2, y-size/2, x+100, y-100, size/6, bulletSprite, true, 50));
        bullets.add(new Bullet(this, x+size/2, y+size/2, x+100, y+100, size/6, bulletSprite, true, 75));
        bullets.add(new Bullet(this, x, y+size/2, x, y+100, size/6, bulletSprite, true, 25));
        bullets.add(new Bullet(this, x-size/2, y+size/2, x-100, y+100, size/6, bulletSprite, true, 0));
    }
}
