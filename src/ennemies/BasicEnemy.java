package ennemies;

import managers.SoundManager;
import managers.TextManager.Text;
import rvb.RvB;


public class BasicEnemy extends Enemy{
    
    public static int balance = 10;
    
    public BasicEnemy(){
        super();
        textures.add(RvB.textures.get("basicEnemy"));
        texturesBright.add(RvB.textures.get("basicEnemyBright"));
        rotateIndex = 0;
        textureStatic = RvB.textures.get("basicEnemy");
        name = Text.ENEMY_BASIC;
        reward = 2;
        power = 2f;
        shootRate = 1f;
        moveSpeed = 3.5f;
        range = 3*RvB.unite;
        life = 34f;
        size = 4*RvB.unite/5;
        hitboxWidth = size;
        volumeWalk = SoundManager.Volume.VERY_LOW;
        clipWalk = SoundManager.Instance.getClip("walking");
        stepEveryMilli = 700;
        eBalance = balance;
        
        initBack();
    }
}
