package ennemies;

import managers.SoundManager;
import towser.Towser;
import static towser.Towser.game;
import static towser.Towser.ref;
import static towser.Towser.uniteRef;

public class TrickyEnemy extends Enemy{
    
    public static int idCount = 0, balance = 30;
    
    public TrickyEnemy(){
        super(++idCount);
        name = "Group of Bazooldier";
        spawnSpeed = 1.8;
        reward = 12;
        power = 6;
        shootRate = 1;
        moveSpeed = 2.9;
        range = (int) (30*ref);
        life = 80;
        weight = 3;
        width = 4*Towser.unite/5;
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
                e.stopFor((int) (i*350*uniteRef/game.gameSpeed));
                game.addEnemie(e);
            }
        }
        super.die();
    }
}
