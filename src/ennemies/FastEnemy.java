package ennemies;

import static ennemies.Enemy.Type.FAST;
import managers.SoundManager;
import managers.TextManager.Text;
import rvb.RvB;

public class FastEnemy extends Enemy{
    
    public static int balance = 20;
    
    public FastEnemy(){
        super(FAST);
        textures.add(RvB.textures.get("fastEnemy"));
        texturesBright.add(RvB.textures.get("fastEnemyBright"));
        rotateIndex = 0;
        textureStatic = RvB.textures.get("fastEnemy");
        name = Text.ENEMY_FAST;
        reward = 4;
        power = 4f;
        shootRate = 1f;
        moveSpeed = 4.4f;
        range = 3*RvB.unite;
        life = 46f;
        size = RvB.unite;
        hitboxWidth = size;
        eBalance = balance;
        volumeWalk = SoundManager.Volume.VERY_LOW;
        clipWalk = SoundManager.Instance.getClip("quad");
        stepEveryMilli = 0;
        
        initBack();
    }
}
