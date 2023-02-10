package towers;

import managers.SoundManager;
import managers.TextManager.Text;
import rvb.RvB;
import static rvb.RvB.ref;

public class FlameTower extends Tower{

    public static int startPrice = 800;
    
    public FlameTower(){
        super("flameTower");
        textures.add(RvB.textures.get("flameTowerBase"));
        textures.add(RvB.textures.get("flameTowerTurret"));
        rotateIndex = 1;
        textureStatic = RvB.textures.get(("flameTower"));
        price = RvB.game.flameTowerPrice;
        life = 100f;
        size = 4*RvB.unite/5;
        hitboxWidth = size;
        totalMoneySpent = price;
        isMultipleShot = true;
        name = Text.TOWER_FLAME;
        follow = false;
        clip = SoundManager.Instance.getClip("flamethrower");
        volume = SoundManager.Volume.SEMI_HIGH;
        SoundManager.Instance.setClipVolume(clip, volume);
        continuousSound = true;
        bulletSprite = RvB.textures.get("flame");
        
        range = (int) (1.4*RvB.unite);
        power = 1f;
        shootRate = 30f;
        bulletSpeed = 25;
        upgrades.add(new Upgrade(this, "Range", range, 1.3f, "*", 180f, 1.5f, 2));
        upgrades.add(new Upgrade(this, "Power", power, 0.5f, "+", 400f, 1.6f, 2));
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