package towers;

import managers.SoundManager;
import managers.TextManager.Text;
import rvb.RvB;
import static rvb.RvB.ref;

public class CircleTower extends Tower{

    public static int startPrice = 400;
    
    public CircleTower(){
        super("circleTower");
        textureStatic = RvB.textures.get(("circleTower"));
        textures.add(textureStatic);
        price = RvB.game.circleTowerPrice;
        life = 100f;
        size = 4*RvB.unite/5;
        hitboxWidth = size;
        totalMoneySpent = price;
        name = Text.TOWER_CIRCLE;
        follow = false;
        isMultipleShot = true;
        focusIndex = -1;
        clip = SoundManager.Instance.getClip("multicannon");
        SoundManager.Instance.setClipVolume(clip, volume);
        bulletSprite = RvB.textures.get("bullet");
        
        range = (int) (1.2*RvB.unite);
        power = 20f;
        shootRate = 0.7f;
        bulletSpeed = 25;
        upgrades.add(new Upgrade(this, "Range", range, 1.2f, "*", 100f, 1.5f, 2));
        upgrades.add(new Upgrade(this, "Power", power, 5f, "+", 200f, 1.5f, 2));
        upgrades.add(new Upgrade(this, "Attack speed", shootRate, 1.5f, "*", 275f, 2f, 2));
        int n = 0;
        for(int i = 0 ; i < upgrades.size() ; i++)
            n += upgrades.get(i).maxClick;
        growth = 20*ref/n;

        initBack();
        initOverlay();
    }
    
    @Override
    public void shoot(){
        lastShoot = RvB.game.timeInGamePassed;
        if(clip != null){
            if(continuousSound){
                if(!soundPlayed){
                    SoundManager.Instance.playLoop(clip);
                    soundPlayed = true;
                }
            }
            else
                SoundManager.Instance.playOnce(clip);
        }
        
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
