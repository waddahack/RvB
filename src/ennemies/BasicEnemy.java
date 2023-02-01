package ennemies;

import managers.SoundManager;
import managers.TextManager.Text;
import rvb.RvB;


public class BasicEnemy extends Enemy{
    
    public static int balance = 10;
    
    public BasicEnemy(){
        super();
        name = Text.ENEMY_BASIC;
        reward = 5;
        power = 2f;
        shootRate = 1f;
        moveSpeed = 3.5f;
        range = 3*RvB.unite;
        life = 30f;
        width = 4*RvB.unite/5;
        hitboxWidth = width;
        sprite = RvB.textures.get("basicEnemy");
        brightSprite = RvB.textures.get("basicEnemyBright");
        volume = SoundManager.Volume.VERY_LOW;
        clip = SoundManager.Instance.getClip("walking");
        stepEveryMilli = 700;
        eBalance = balance;
        
        initBack();
    }
}
