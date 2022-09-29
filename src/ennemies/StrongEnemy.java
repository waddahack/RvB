package ennemies;

import managers.SoundManager;
import rvb.RvB;

public class StrongEnemy extends Enemy{
    
    public static int idCount = 0, balance = 40;
    
    public StrongEnemy(){
        super(++idCount);
        name = "Bazank";
        spawnSpeed = 2.2f;
        reward = 28;
        power = 10;
        shootRate = 1;
        moveSpeed = 2.4f;
        range = 3*RvB.unite;
        life = 280;
        width = RvB.unite;
        eBalance = balance;
        rgb = new float[]{0.4f, 0.9f, 0.1f};
        sprite = RvB.textures.get("strongEnemy");
        brightSprite = RvB.textures.get("strongEnemyBright");
        clip = SoundManager.Instance.getClip("tank");
        volume = SoundManager.Volume.SEMI_LOW;
        stepEveryMilli = 0;
        
        initBack();
    }
}
