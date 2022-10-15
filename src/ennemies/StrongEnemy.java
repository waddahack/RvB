package ennemies;

import managers.SoundManager;
import rvb.RvB;

public class StrongEnemy extends Enemy{
    
    public static int balance = 40;
    
    public StrongEnemy(){
        super();
        name = "Bazank";
        reward = 28;
        power = 10;
        shootRate = 1;
        moveSpeed = 2.4f;
        range = 3*RvB.unite;
        life = 280;
        width = RvB.unite;
        eBalance = balance;
        sprite = RvB.textures.get("strongEnemy");
        brightSprite = RvB.textures.get("strongEnemyBright");
        clip = SoundManager.Instance.getClip("tank");
        volume = SoundManager.Volume.SEMI_LOW;
        stepEveryMilli = 0;
        
        initBack();
    }
}
