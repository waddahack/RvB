package ennemies;

import managers.SoundManager;
import towser.Towser;

public class FastEnemy extends Enemy{
    
    public static int idCount = 0, balance = 20;
    
    public FastEnemy(){
        super(++idCount);
        name = "Quazoo";
        spawnSpeed = 1.2;
        reward = 5;
        power = 4;
        shootRate = 1;
        moveSpeed = 4.2;
        range = 30;
        life = 42;
        weight = 1.5;
        width = 50;
        eBalance = balance;
        rgb = new float[]{1f, 1f, 0f};
        sprite = Towser.textures.get("fastEnemy");
        brightSprite = Towser.textures.get("fastEnemyBright");
        volume = SoundManager.Volume.SEMI_LOW;
        clip = SoundManager.Instance.getClip("quad");
        stepEveryMilli = 0;
        
        initBack();
    }
}
