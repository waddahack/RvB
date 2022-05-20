package ennemies;

import managers.SoundManager;
import towser.Towser;


public class BasicEnemy extends Enemy{
    
    public static int idCount = 0, balance = 10;
    
    public BasicEnemy(){
        super(++idCount);
        name = "Bazooldier";
        spawnSpeed = 0.8;
        reward = 2;
        power = 2;
        shootRate = 1;
        moveSpeed = 3.3;
        range = 3*Towser.unite;
        life = 30;
        weight = 1;
        width = 4*Towser.unite/5;
        rgb = new float[]{1f, 0.7f, 0f};
        sprite = Towser.textures.get("basicEnemy");
        brightSprite = Towser.textures.get("basicEnemyBright");
        volume = SoundManager.Volume.LOW;
        clip = SoundManager.Instance.getClip("walking");
        stepEveryMilli = 700;
        eBalance = balance;
        
        initBack();
    }
}
