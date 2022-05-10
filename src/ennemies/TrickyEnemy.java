package ennemies;

import towser.Game;
import managers.SoundManager;
import towser.Towser;
import static towser.Towser.game;

public class TrickyEnemy extends Enemy{
    
    public static int idCount = 0, balance = 30;
    
    public TrickyEnemy(){
        super(++idCount);
        name = "Group of Bazooldier";
        spawnSpeed = 1.8;
        reward = 5;
        power = 6;
        shootRate = 1;
        moveSpeed = 2.9;
        range = 30;
        life = 80;
        weight = 3;
        width = 40;
        eBalance = balance;
        rgb = new float[]{0.2f, 0.2f, 0.8f};
        sprite = Towser.textures.get("trickyEnemy");
        brightSprite = Towser.textures.get("trickyEnemyBright");
        volume = SoundManager.Volume.SEMI_LOW;
        clip = SoundManager.Instance.getClip("group_walking");
        stepEveryMilli = 800;
        
        initBack();
    }
    
    @Override
    public void die(){
        if(!isInBase()){
            for(int i = 0 ; i < 3 ; i++){
                Enemy e = new BasicEnemy();
                e.setX(x);
                e.setY(y);
                e.setIndiceTuile(indiceTuile);
                e.setDir(dir);
                e.stopFor(i*350/game.gameSpeed);
                game.addEnemie(e);
            }
        }
        super.die();
    }
}
