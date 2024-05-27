package ennemies;

import static ennemies.Enemy.Type.BASIC;
import managers.SoundManager;
import managers.TextManager.Text;
import rvb.RvB;


public class BasicEnemy extends Enemy{
    
    public static int balance = 10;
    
    public BasicEnemy(){
        super(BASIC);
        textures.add(RvB.textures.get("basicEnemy"));
        texturesBright.add(RvB.textures.get("basicEnemyBright"));
        rotateIndex = 0;
        textureStatic = RvB.textures.get("basicEnemy");
        name = Text.ENEMY_BASIC;
        reward = 1;
        commitPower = 2f;
        
        canShoot = true;
        follow = true;
        power = 2f;
        bulletSprite = RvB.textures.get("bullet");
        clip = SoundManager.Instance.getClip("cannon");
        SoundManager.Instance.setClipVolume(clip, volume);
        bulletSpeed = 20;
        shootRate = 1f;
        range = 10*RvB.unite;
        
        moveSpeed = 3.5f;
        life = 30f;
        size = 4*RvB.unite/5;
        hitboxWidth = size;
        volumeWalk = SoundManager.Volume.VERY_LOW;
        clipWalk = SoundManager.Instance.getClip("walking");
        stepEveryMilli = 700;
        eBalance = balance;
        
        initBack();
    }
}
