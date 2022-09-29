package towers;

import rvb.RvB;
import managers.SoundManager;
import static rvb.RvB.ref;

public class BasicTower extends Tower{
    
    public static int startPrice = 200;
    public static int priceP = startPrice;
    
    public BasicTower() {
        super("basicTower");
        textures.add(RvB.textures.get("basicTowerBase"));
        textures.add(RvB.textures.get("basicTowerTurret"));
        rotateIndex = 2;
        textureStatic = RvB.textures.get("basicTower");
        canRotate = true;
        price = priceP;
        life = 100;
        width = 4*RvB.unite/5;
        size = width;
        totalMoneySpent = priceP;
        name = "Razannon";
        explode = false;
        follow = false;
        isMultipleShot = false;
        clip = SoundManager.Instance.getClip("cannon");
        SoundManager.Instance.setClipVolume(clip, volume);
        bulletSprite = RvB.textures.get("bullet");
        growth = 2*ref;
        
        range = 3*RvB.unite;
        power = 18;
        shootRate = 0.5f;
        bulletSpeed = 18;
        upgrades.add(new Upgrade("Range", range, 1.3f, "*", 120f, 1.5f, 2));
        upgrades.add(new Upgrade("Power", power, 1.3f, "*", 120f, 1.4f, 3));
        upgrades.add(new Upgrade("Attack speed", shootRate, 1.25f, "*", 150f, 1.5f, 2));
        upgrades.add(new Upgrade("Bullet speed", bulletSpeed, 20f, "+", 160f, 3f, 1));
    }
    
    @Override
    protected void raisePrice(){
        priceP *= 1.2;
        price = priceP;
    }
}
