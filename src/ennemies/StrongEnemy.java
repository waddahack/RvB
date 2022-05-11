package ennemies;

import managers.SoundManager;
import towser.Towser;
import static towser.Towser.ref;

public class StrongEnemy extends Enemy{
    
    public static int idCount = 0, balance = 40;
    
    public StrongEnemy(){
        super(++idCount);
        name = "Bazank";
        spawnSpeed = 2.2;
        reward = 10;
        power = 10;
        shootRate = 1;
        moveSpeed = 2.4;
        range = (int) (30*ref);
        life = 280;
        weight = 2.5;
        width = (int) (50*ref);
        eBalance = balance;
        rgb = new float[]{0.4f, 0.9f, 0.1f};
        sprite = Towser.textures.get("strongEnemy");
        brightSprite = Towser.textures.get("strongEnemyBright");
        clip = SoundManager.Instance.getClip("tank");
        stepEveryMilli = 0;
        
        initBack();
    }
}
