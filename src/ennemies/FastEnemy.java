package ennemies;

import managers.SoundManager;
import rvb.RvB;

public class FastEnemy extends Enemy{
    
    public static int idCount = 0, balance = 20;
    
    public FastEnemy(){
        super(++idCount);
        name = "Quazoo";
        spawnSpeed = 1.2f;
        reward = 6;
        power = 4;
        shootRate = 1;
        moveSpeed = 4.2f;
        range = 3*RvB.unite;
        life = 42;
        width = RvB.unite;
        eBalance = balance;
        rgb = new float[]{1f, 1f, 0f};
        sprite = RvB.textures.get("fastEnemy");
        brightSprite = RvB.textures.get("fastEnemyBright");
        volume = SoundManager.Volume.VERY_LOW;
        clip = SoundManager.Instance.getClip("quad");
        stepEveryMilli = 0;
        
        initBack();
    }
}
