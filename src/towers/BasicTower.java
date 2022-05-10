package towers;

import java.util.ArrayList;
import managers.SoundManager;
import towser.*;

public class BasicTower extends Tower{
    
    public static int startPrice = 200;
    public static int priceP = startPrice;
    
    public BasicTower() {
        super("basicTower");
        textures.add(Towser.textures.get("grass"));
        textures.add(Towser.textures.get("basicTowerBase"));
        textures.add(Towser.textures.get("basicTowerTurret"));
        rotateIndex = 2;
        textureStatic = Towser.textures.get(("basicTower"));
        canRotate = true;
        price = priceP;
        totalPrice = price;
        power = 20;
        shootRate = 0.6f;
        range = 3*Game.unite;
        life = 100;
        width = 40;
        size = width;
        name = "Razannon";
        textureName = "basicTower";
        bulletSpeed = 2*120/Towser.fps;
        follow = false;
        isMultipleShot = false;
        clip = SoundManager.Instance.getClip("cannon");
        SoundManager.Instance.setClipVolume(clip, volume);
        bulletSprite = Towser.textures.get("bulletBlue");
        ArrayList<Float> prices = new ArrayList<>();
        ArrayList<Float> priceMultipliers = new ArrayList<>();
        ArrayList<Float> multipliers = new ArrayList<>();
        ArrayList<Float> maxUpgradeClicks = new ArrayList<>();
        prices.add(100f); // range
        prices.add(120f); // power
        prices.add(150f); // shoot rate
        prices.add(200f); // bullet speed
        priceMultipliers.add(1.5f);
        priceMultipliers.add(1.4f);
        priceMultipliers.add(1.5f);
        priceMultipliers.add(3f);
        multipliers.add(1.2f);
        multipliers.add(1.3f);
        multipliers.add(1.25f);
        multipliers.add(3f*120/Towser.fps);
        maxUpgradeClicks.add(3f);
        maxUpgradeClicks.add(3f);
        maxUpgradeClicks.add(3f);
        maxUpgradeClicks.add(1f);
        upgradesParam.put("prices", prices);
        upgradesParam.put("priceMultipliers", priceMultipliers);
        upgradesParam.put("multipliers", multipliers);
        upgradesParam.put("maxUpgradeClicks", maxUpgradeClicks);
        growth = 2;
    }
    
    @Override
    protected void raisePrice(){
        priceP *= 1.08;
        price = priceP;
    }
}
