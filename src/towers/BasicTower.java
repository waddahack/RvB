package towers;

import rvb.RvB;
import managers.SoundManager;
import managers.TextManager.Text;
import static rvb.RvB.ref;

public class BasicTower extends Tower{
    
    public static int startPrice = 200;
    
    public BasicTower() {
        super("BasicTower");
        textures.add(RvB.textures.get("basicTowerBase"));
        textures.add(RvB.textures.get("basicTowerTurret"));
        rotateIndex = 1;
        textureStatic = RvB.textures.get("basicTower");
        price = RvB.game.basicTowerPrice;
        life = 100f;
        size = 4*RvB.unite/5;
        hitboxWidth = size;
        totalMoneySpent = price;
        name = Text.TOWER_BASIC;
        follow = false;
        clip = SoundManager.Instance.getClip("cannon");
        SoundManager.Instance.setClipVolume(clip, volume);
        bulletSprite = RvB.textures.get("bullet");
        
        range = 3*RvB.unite;
        power = 22f;
        shootRate = 0.5f;
        bulletSpeed = 20;
        upgrades.add(new Upgrade(this, "Range", range, 1.3f, "*", 120f, 1.5f, 2));
        upgrades.add(new Upgrade(this, "Power", power, 10f, "+", 120f, 1.4f, 3));
        upgrades.add(new Upgrade(this, "Attack speed", shootRate, 1.25f, "*", 100f, 1.5f, 3));
        upgrades.add(new Upgrade(this, "Bullet speed", bulletSpeed, 10f, "+", 100f, 1.5f, 2));
        int n = 0;
        for(int i = 0 ; i < upgrades.size() ; i++)
            n += upgrades.get(i).maxClick;
        growth = 20*ref/n;
        
        initBack();
        initOverlay();
    }
}
