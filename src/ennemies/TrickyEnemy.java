package ennemies;

import static ennemies.Enemy.Type.TRICKY;
import managers.SoundManager;
import managers.TextManager.Text;
import rvb.RvB;
import static rvb.RvB.game;
import static rvb.RvB.ref;

public class TrickyEnemy extends Enemy{
    
    public static int balance = 30;
    
    public TrickyEnemy(){
        super(TRICKY);
        textures.add(RvB.textures.get("trickyEnemy"));
        texturesBright.add(RvB.textures.get("trickyEnemyBright"));
        rotateIndex = 0;
        textureStatic = RvB.textures.get("trickyEnemy");
        name = Text.ENEMY_TRICKY;
        reward = 3;
        commitPower = 6f;
        shootRate = 1f;
        moveSpeed = 3.1f;
        range = 0;
        life = 70f;
        size = 4*RvB.unite/5;
        hitboxWidth = size;
        eBalance = balance;
        volumeWalk = SoundManager.Volume.VERY_LOW;
        clipWalk = SoundManager.Instance.getClip("group_walking");
        stepEveryMilli = 800;
        
        initBack();
    }
    
    @Override
    public void die(){
        if(dead)
            return;
        if(!isInBase() && !game.ended && hasStarted()){
            newAngle = (int)Math.round(angle/90) * 90;
            angle = newAngle;
            for(int i = 0 ; i < 3 ; i++){
                Enemy e = new BasicEnemy();
                e.setX(x);
                e.setY(y);
                e.angle = angle;
                e.setIndiceTuile(indiceTuile);
                e.setDirection(true);
                e.stopFor((int) (i*350*ref));
                game.addEnemy(e);
            }
        }
        super.die();
    }
}
