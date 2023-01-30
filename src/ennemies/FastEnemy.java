package ennemies;

import managers.SoundManager;
import managers.TextManager.Text;
import rvb.RvB;

public class FastEnemy extends Enemy{
    
    public static int balance = 20;
    
    public FastEnemy(){
        super();
        name = Text.ENEMY_FAST;
        reward = 15;
        power = 4;
        shootRate = 1;
        moveSpeed = 4.4f;
        range = 3*RvB.unite;
        life = 42;
        width = RvB.unite;
        hitboxWidth = width;
        eBalance = balance;
        sprite = RvB.textures.get("fastEnemy");
        brightSprite = RvB.textures.get("fastEnemyBright");
        volume = SoundManager.Volume.VERY_LOW;
        clip = SoundManager.Instance.getClip("quad");
        stepEveryMilli = 0;
        
        initBack();
    }
}
