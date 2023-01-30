package ennemies;

import managers.SoundManager;
import managers.TextManager.Text;
import rvb.RvB;
import static rvb.RvB.game;
import static rvb.RvB.ref;

public class TrickyEnemy extends Enemy{
    
    public static int balance = 30;
    
    public TrickyEnemy(){
        super();
        name = Text.ENEMY_TRICKY;
        reward = 25;
        power = 6;
        shootRate = 1;
        moveSpeed = 3.1f;
        range = 0;
        life = 80;
        width = 4*RvB.unite/5;
        hitboxWidth = width;
        eBalance = balance;
        sprite = RvB.textures.get("trickyEnemy");
        brightSprite = RvB.textures.get("trickyEnemyBright");
        volume = SoundManager.Volume.VERY_LOW;
        clip = SoundManager.Instance.getClip("group_walking");
        stepEveryMilli = 800;
        
        initBack();
    }
    
    @Override
    public void die(){
        if(!isInBase() && !game.ended){
            newAngle = (int)Math.round(angle/90) * 90;
            angle = newAngle;
            for(int i = 0 ; i < 3 ; i++){
                Enemy e = new BasicEnemy();
                e.setX(x);
                e.setY(y);
                e.setIndiceTuile(indiceTuile);
                e.setDirection();
                e.stopFor((int) (i*350*ref/game.gameSpeed));
                game.addEnemy(e);
            }
        }
        super.die();
    }
}
