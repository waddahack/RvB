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
        
        moveSpeed = 3f;
        life = 40f;
        size = 4*RvB.unite/5;
        hitboxWidth = size;
        volumeWalk = SoundManager.Volume.VERY_LOW;
        clipWalk = SoundManager.Instance.getClip("walking");
        stepEveryMilli = 700;
        eBalance = balance;
        
        initBack();
    }
}
