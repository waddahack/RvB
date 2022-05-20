package ennemies;

import managers.SoundManager;
import towser.Towser;

public class FastEnemy extends Enemy{
    
    public static int idCount = 0, balance = 20;
    
    public FastEnemy(){
        super(++idCount);
        name = "Quazoo";
        spawnSpeed = 1.2;
        reward = 6;
        power = 4;
        shootRate = 1;
        moveSpeed = 4.2;
        range = 3*Towser.unite;
        life = 42;
        weight = 1.5;
        width = Towser.unite;
        eBalance = balance;
        rgb = new float[]{1f, 1f, 0f};
        sprite = Towser.textures.get("fastEnemy");
        brightSprite = Towser.textures.get("fastEnemyBright");
        volume = SoundManager.Volume.LOW;
        clip = SoundManager.Instance.getClip("quad");
        stepEveryMilli = 0;
        
        initBack();
    }
}
