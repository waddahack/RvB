package towers;

import managers.SoundManager;
import managers.TextManager.Text;
import rvb.RvB;
import static rvb.RvB.game;
import static rvb.RvB.ref;
import static towers.Tower.Type.CIRCLE;

public class CircleTower extends Tower{

    public static int startPrice = 350;
    
    public CircleTower(){
        super(CIRCLE);
        textures.add(RvB.textures.get("circleTowerBase"));
        textures.add(RvB.textures.get("circleTowerTurret"));
        texturesBright.add(RvB.textures.get("circleTowerBaseBright"));
        texturesBright.add(RvB.textures.get("circleTowerTurretBright"));
        rotateIndexShoot = 1;
        canFocus = false;
        textureStatic = RvB.textures.get("circleTower");
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
        volume = SoundManager.Volume.LOW;
        SoundManager.Instance.setClipVolume(clip, volume);
        bulletSprite = RvB.textures.get("bullet");
        
        range = (int) (1.2*RvB.unite);
        power = 5f;
        shootRate = 0.7f;
        bulletSpeed = 25;
        upgrades.add(new Upgrade(this, "Range", range, 1.2f, "*", 140f, 1.4f, 2));
        upgrades.add(new Upgrade(this, "Power", power, 2f, "+", 200f, 1.6f, 2));
        upgrades.add(new Upgrade(this, "Attack speed", shootRate, 1.3f, "*", 300f, 1.9f, 2));
        int n = 0;
        for(int i = 0 ; i < upgrades.size() ; i++)
            n += upgrades.get(i).maxClick;
        growth = 20*ref/n;

        initBack();
        initOverlay();
    }
    
    @Override
    public void update(){
        super.update();
        angle += game.gameSpeed*RvB.deltaTime/20;
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
    
    @Override
    public boolean canShoot(){
        return canShoot && (game.timeInGamePassed-lastShoot >= 1000/getShootRate());
    }
}
