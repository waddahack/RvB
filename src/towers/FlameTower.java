package towers;

import managers.SoundManager;
import rvb.RvB;
import static rvb.RvB.ref;

public class FlameTower extends Tower{

    public static int startPrice = 600;
    public static int priceP = startPrice;
    
    public FlameTower(){
        super("flameTower");
        textures.add(RvB.textures.get("flameTowerBase"));
        textures.add(RvB.textures.get("flameTowerTurret"));
        rotateIndex = 1;
        textureStatic = RvB.textures.get(("flameTower"));
        canRotate = true;
        price = priceP;
        life = 100;
        width = 4*RvB.unite/5;
        size = width;
        totalMoneySpent = priceP;
        name = "Flametech";
        explode = false;
        follow = false;
        isMultipleShot = true;
        clip = SoundManager.Instance.getClip("flamethrower");
        volume = SoundManager.Volume.SEMI_HIGH;
        SoundManager.Instance.setClipVolume(clip, volume);
        continuousSound = true;
        bulletSprite = RvB.textures.get("flame");
        growth = 4*ref;
        
        range = (int) (1.4*RvB.unite);
        power = 1;
        shootRate = 25f;
        bulletSpeed = 25;
        upgrades.add(new Upgrade("Range", range, 1.4f, "*", 150f, 1.5f, 2));
        upgrades.add(new Upgrade("Power", power, 1f, "+", 450f, 1.3f, 2));

    }
    
    @Override
    protected void raisePrice(){
        priceP *= 1.2;
        price = priceP;
    }
    
    @Override
    public void shoot(){
        super.shoot();
        
        lastShoot = System.currentTimeMillis();

        bullets.remove(bullets.size()-1);
        int a = 75;
        int addX = (int) Math.abs(Math.tan(a/2)*(enemyAimed.getY()-y));
        int addY = (int) Math.abs(Math.tan(a/2)*(enemyAimed.getX()-x));
        if(addX < 1)
            addX = 1;
        if(addY < 1)
            addY = 1;
        bullets.add(new Bullet(this, (float)(x+size*Math.cos(Math.toRadians(angle))/2), (float)(y+size*Math.sin(Math.toRadians(angle))/2), enemyAimed.getX()+random.nextInt(addX)-addX/2, enemyAimed.getY()+random.nextInt(addY)-addY/2, size/2, bulletSprite, true, true));
    }
}