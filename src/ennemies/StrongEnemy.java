package ennemies;

import static ennemies.Enemy.Type.STRONG;
import managers.SoundManager;
import managers.TextManager.Text;
import rvb.RvB;

public class StrongEnemy extends Enemy{
    
    public static int balance = 60;
    
    public StrongEnemy(){
        super(STRONG);
        textures.add(RvB.textures.get("strongEnemy"));
        texturesBright.add(RvB.textures.get("strongEnemyBright"));
        rotateIndex = 0;
        textureStatic = RvB.textures.get("strongEnemy");
        name = Text.ENEMY_STRONG;
        reward = 8;
        power = 10f;
        shootRate = 1f;
        moveSpeed = 2.6f;
        range = 3*RvB.unite;
        life = 310f;
        size = RvB.unite;
        hitboxWidth = size;
        eBalance = balance;
        clipWalk = SoundManager.Instance.getClip("tank");
        volumeWalk = SoundManager.Volume.SEMI_LOW;
        stepEveryMilli = 0;
        
        initBack();
    }
}
