package ennemies;

import Utils.MyMath;
import static ennemies.Enemy.Type.FLYING;
import managers.SoundManager;
import managers.TextManager.Text;
import rvb.RvB;
import static rvb.RvB.game;
import static rvb.RvB.unite;

public class FlyingEnemy extends Enemy{
    
    public static int balance = 100;
    
    private double xDiffConst, yDiffConst, hyp, tileEveryPixel;
    private int turningSpriteAngle = 0;
    
    public FlyingEnemy(){
        super(FLYING);
        textures.add(RvB.textures.get("flyingEnemyBase"));
        textures.add(RvB.textures.get("flyingEnemyProp"));
        texturesBright.add(RvB.textures.get("flyingEnemyBaseBright"));
        texturesBright.add(RvB.textures.get("flyingEnemyPropBright"));
        rotateIndex = 0;
        textureStatic = RvB.textures.get("flyingEnemy");
        
        name = Text.ENEMY_FLYING;
        reward = 5;
        power = 12f;
        shootRate = 1f;
        moveSpeed = 1.6f;
        range = 3*unite;
        life = 132f;
        size = (int) (1.25*unite);
        hitboxWidth = size;
        eBalance = balance;

        clipWalk = SoundManager.Instance.getClip("helicopter");
        volumeWalk = SoundManager.Volume.SEMI_LOW;
        stepEveryMilli = 0;
        xDiffConst = (game.base.getRealX()-game.spawn.getRealX());
        yDiffConst = (game.base.getRealY()-game.spawn.getRealY());
        hyp = MyMath.distanceBetween(game.spawn, game.base);
        tileEveryPixel = (hyp/(game.path.size()-1));
        newAngle = (int) Math.round((90+(float) MyMath.angleDegreesBetween(game.spawn, game.base)));
        angle = newAngle;
        
        initBack();
    }
    
    @Override
    protected void move(){
        indiceTuile = (int) (game.path.size()-1 - Math.floor(MyMath.distanceBetween(this, game.base)/tileEveryPixel));

        if(isInBase())
            commit();
        if(slowedBy > 0 && moveSpeed == oldMoveSpeed){
            moveSpeed *= (1-slowedBy);
            stepEveryMilli *= (1+slowedBy);
        }
            
        else if(slowedBy != 0 && game.timeInGamePassed - startTimeSlow >= 1000){
            moveSpeed = oldMoveSpeed;
            stepEveryMilli = oldstepEveryMilli;
            slowedBy = 0;
        }
        double speed = ((moveSpeed*game.gameSpeed) * RvB.deltaTime / 50) * RvB.ref;
        speed *= (hyp/700);
        
        x += xDiffConst * (speed/hyp);
        y += yDiffConst * (speed/hyp);
        
        startTimeMove = game.timeInGamePassed;
    }
    
    @Override
    public void render(){
        if(!started && stopFor == -1)
            return;
        
        if(startTimeWaitFor != 0 && game.timeInGamePassed - startTimeWaitFor < waitFor){
            for(int i = 0 ; i < textures.size() ; i++)
                RvB.drawFilledRectangle(x, y, size, size, texturesBright.get(i), i == rotateIndex ? angle : turningSpriteAngle, 1);
        }
        else{
            startTimeWaitFor = 0;
            for(int i = 0 ; i < textures.size() ; i++)
                RvB.drawFilledRectangle(x, y, size, size, textures.get(i), i == rotateIndex ? angle : turningSpriteAngle, 1);
        }
        turningSpriteAngle += 2*game.gameSpeed;
    }
}
