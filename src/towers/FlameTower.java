package towers;

import managers.SoundManager;
import towser.Towser;
import static towser.Towser.ref;

public class FlameTower extends Tower{

    public static int startPrice = 500;
    public static int priceP = startPrice;
    
    public FlameTower(){
        super("flameTower");
        textures.add(Towser.textures.get("grass"));
        textures.add(Towser.textures.get("flameTowerBase"));
        textures.add(Towser.textures.get("flameTowerTurret"));
        rotateIndex = 2;
        textureStatic = Towser.textures.get(("flameTower"));
        canRotate = true;
        price = priceP;
        life = 100;
        width = 4*Towser.unite/5;
        size = width;
        totalMoneySpent = priceP;
        name = "Flametech";
        follow = false;
        isMultipleShot = true;
        clip = null;
        SoundManager.Instance.setClipVolume(clip, volume);
        bulletSprite = Towser.textures.get("flame");
        growth = 4*ref;
        
        range = (int) (1.6*Towser.unite);
        power = 1;
        shootRate = 60f;
        bulletSpeed = 25;
        upgrades.add(new Upgrade("Range", range, 1.4f, "Multiplicate", 100f, 1.5f, 2));
        upgrades.add(new Upgrade("Power", power, 1f, "Add", 200f, 1.2f, 3));

    }
    
    @Override
    protected void raisePrice(){
        priceP *= 1.20;
        price = priceP;
    }
    
    @Override
    public void shoot(){
        super.shoot();
        
        lastShoot = System.currentTimeMillis();

        bullets.remove(bullets.size()-1);
        bullets.add(new Bullet(this, (float)(x+size*Math.cos(Math.toRadians(angle))/2), (float)(y+size*Math.sin(Math.toRadians(angle))/2), enemyAimed.getX()+random.nextInt(120)-60, enemyAimed.getY()+random.nextInt(120)-60, size/3, bulletSprite, true));
    }
}