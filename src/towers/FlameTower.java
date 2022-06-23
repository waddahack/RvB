package towers;

import managers.SoundManager;
import towser.Towser;
import static towser.Towser.ref;
import static towser.Towser.unite;

public class FlameTower extends Tower{

    public static int startPrice = 900;
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
        clip = SoundManager.Instance.getClip("flamethrower");
        volume = SoundManager.Volume.SEMI_HIGH;
        SoundManager.Instance.setClipVolume(clip, volume);
        continuousSound = true;
        bulletSprite = Towser.textures.get("flame");
        growth = 4*ref;
        
        range = (int) (1.6*Towser.unite);
        power = 1;
        shootRate = 30f;
        bulletSpeed = 25;
        upgrades.add(new Upgrade("Range", range, 1.4f, "Multiplicate", 150f, 1.5f, 2));
        upgrades.add(new Upgrade("Power", power, 1f, "Add", 300f, 1.2f, 3));

    }
    
    @Override
    protected void raisePrice(){
        priceP *= 1.10;
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