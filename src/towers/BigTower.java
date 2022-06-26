package towers;

import managers.SoundManager;
import towser.*;
import static towser.Towser.ref;

public class BigTower extends Tower{
    
    public static int startPrice = 500;
    public static int priceP = startPrice;
    
    public BigTower() {
        super("bigTower");
        textures.add(Towser.textures.get("grass"));
        textures.add(Towser.textures.get("bigTowerBase"));
        textures.add(Towser.textures.get("bigTowerTurret"));
        rotateIndex = 2;
        textureStatic = Towser.textures.get("bigTower");
        canRotate = true;
        price = priceP;
        life = 100;
        width = 4*Towser.unite/5;
        size = width;
        totalMoneySpent = priceP;
        name = "Razkull";
        explode = true;
        explodeRadius = Towser.unite;
        follow = false;
        isMultipleShot = false;
        clip = SoundManager.Instance.getClip("cannon");
        SoundManager.Instance.setClipVolume(clip, volume);
        bulletSprite = Towser.textures.get("bulletBlue");
        growth = 2*ref;
        
        range = 3*Towser.unite;
        power = 20;
        shootRate = 0.2f;
        bulletSpeed = 12;
        upgrades.add(new Upgrade("Range", range, 1.3f, "Multiplicate", 120f, 1.5f, 3));
        upgrades.add(new Upgrade("Power", power, 1.4f, "Multiplicate", 400f, 1.4f, 3));
        upgrades.add(new Upgrade("Attack speed", shootRate, 1.3f, "Multiplicate", 200f, 1.5f, 3));
        upgrades.add(new Upgrade("Bullet speed", bulletSpeed, 18f, "Add", 200f, 3f, 1));
    }
    
    @Override
    protected void raisePrice(){
        priceP *= 1.1;
        price = priceP;
    }
}
