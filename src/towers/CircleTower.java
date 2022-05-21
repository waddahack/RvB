package towers;

import java.util.ArrayList;
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
        totalPrice = price;
        power = 12;
        shootRate = 0.7f;
        range = (int) (1.2*Towser.unite);
        life = 100;
        width = 4*Towser.unite/5;
        size = width;
        totalMoneySpent = priceP;
        name = "Razingun";
        textureName = "circleTower";
        bulletSpeed = 25;
        follow = false;
        isMultipleShot = true;
        clip = SoundManager.Instance.getClip("multicannon");
        SoundManager.Instance.setClipVolume(clip, volume);
        bulletSprite = Towser.textures.get("bulletGrey");
        ArrayList<Float> prices = new ArrayList<>();
        ArrayList<Float> priceMultipliers = new ArrayList<>();
        ArrayList<Float> multipliers = new ArrayList<>();
        ArrayList<Float> maxUpgradeClicks = new ArrayList<>();
        prices.add(100f); // range
        prices.add(200f); // power
        prices.add(250f); // shoot rate
        prices.add(0f); // bullet speed
        priceMultipliers.add(1.5f);
        priceMultipliers.add(2f);
        priceMultipliers.add(2f);
        priceMultipliers.add(0f);
        multipliers.add(1.2f);
        multipliers.add(5f);
        multipliers.add(1.5f);
        multipliers.add(0f);
        maxUpgradeClicks.add(2f);
        maxUpgradeClicks.add(1f);
        maxUpgradeClicks.add(2f);
        maxUpgradeClicks.add(0f);
        upgradesParam.put("prices", prices);
        upgradesParam.put("priceMultipliers", priceMultipliers);
        upgradesParam.put("multipliers", multipliers);
        upgradesParam.put("maxUpgradeClicks", maxUpgradeClicks);
        growth = 4*ref;
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
        bullets.add(new Bullet(this, x-100, y, size/14, bulletSprite, true, 75));
        bullets.add(new Bullet(this, x-100, y-100, size/14, bulletSprite, true, 0));
        bullets.add(new Bullet(this, x, y-100, size/14, bulletSprite, true, 50));
        bullets.add(new Bullet(this, x+100, y, size/14, bulletSprite, true, 25));
        bullets.add(new Bullet(this, x+100, y-100, size/14, bulletSprite, true, 50));
        bullets.add(new Bullet(this, x+100, y+100, size/14, bulletSprite, true, 75));
        bullets.add(new Bullet(this, x, y+100, size/14, bulletSprite, true, 25));
        bullets.add(new Bullet(this, x-100, y+100, size/14, bulletSprite, true, 0));
    }
}
