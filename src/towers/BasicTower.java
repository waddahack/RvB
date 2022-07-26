package towers;

import managers.SoundManager;
import towser.*;
import static towser.Towser.ref;

public class BasicTower extends Tower{
    
    public static int startPrice = 200;
    public static int priceP = startPrice;
    
    public BasicTower() {
        super("basicTower");
        textures.add(Towser.textures.get("grass"));
        textures.add(Towser.textures.get("basicTowerBase"));
        textures.add(Towser.textures.get("basicTowerTurret"));
        rotateIndex = 2;
        textureStatic = Towser.textures.get("basicTower");
        canRotate = true;
        price = priceP;
        life = 100;
        width = 4*Towser.unite/5;
        size = width;
        totalMoneySpent = priceP;
        name = "Razannon";
        explode = false;
        follow = false;
        isMultipleShot = false;
        clip = SoundManager.Instance.getClip("cannon");
        SoundManager.Instance.setClipVolume(clip, volume);
        bulletSprite = Towser.textures.get("bullet");
        growth = 2*ref;
        
        range = 3*Towser.unite;
        power = 18;
        shootRate = 0.5f;
        bulletSpeed = 18;
        upgrades.add(new Upgrade("Range", range, 1.3f, "Multiplicate", 120f, 1.5f, 3));
        upgrades.add(new Upgrade("Power", power, 1.3f, "Multiplicate", 120f, 1.4f, 3));
        upgrades.add(new Upgrade("Attack speed", shootRate, 1.25f, "Multiplicate", 150f, 1.5f, 3));
        upgrades.add(new Upgrade("Bullet speed", bulletSpeed, 20f, "Add", 160f, 3f, 1));
    }
    
    @Override
    protected void raisePrice(){
        priceP *= 1.1;
        price = priceP;
    }
}
