package ennemies;

import Utils.MyMath;
import managers.SoundManager;
import org.newdawn.slick.opengl.Texture;
import rvb.RvB;
import static rvb.RvB.game;
import static rvb.RvB.unite;

public class FlyingEnemy extends Enemy{
    
    public static int balance = 100;
    
    private double xDiffConst, yDiffConst, hyp, tileEveryPixel;
    private static Texture turningSprite, turningBrightSprite, baseSprite, baseBrightSprite;
    private int turningSpriteAngle = 0;
    
    public FlyingEnemy(){
        super();
        name = "Bazoopter";
        spawnSpeed = 12f;
        reward = 36;
        power = 12;
        shootRate = 1;
        moveSpeed = 1.6f;
        range = 3*unite;
        life = 180;
        width = (int) (1.25*unite);
        eBalance = balance;
        sprite = RvB.textures.get("flyingEnemy");
        baseSprite = RvB.textures.get("flyingEnemyBase");
        baseBrightSprite = RvB.textures.get("flyingEnemyBaseBright");
        turningSprite = RvB.textures.get("flyingEnemyProp");
        turningBrightSprite = RvB.textures.get("flyingEnemyPropBright");
        clip = SoundManager.Instance.getClip("helicopter");
        volume = SoundManager.Volume.SEMI_LOW;
        stepEveryMilli = 0;
        xDiffConst = (game.base.getRealX()-game.spawn.getRealX());
        yDiffConst = (game.base.getRealY()-game.spawn.getRealY());
        hyp = MyMath.distanceBetween(game.spawn, game.base);
        tileEveryPixel = (hyp/(game.path.size()-1));
        newAngle = 90+(float) MyMath.angleDegreesBetween(game.spawn, game.base);
        angle = newAngle;
        
        initBack();
    }
    
    @Override
    protected void move(){
        indiceTuile = (int) (game.path.size()-1 - Math.floor(MyMath.distanceBetween(this, game.base)/tileEveryPixel));

        if(isInBase())
            attack();

        double speed = ((moveSpeed*game.gameSpeed) * RvB.deltaTime / 50) * RvB.ref;
        speed *= (hyp/700);
        
        x += xDiffConst * (speed/hyp);
        y += yDiffConst * (speed/hyp);
        
        startTimeMove = System.currentTimeMillis();
    }
    
    @Override
    public void render(){
        if(!started && stopFor == -1)
            return;
        
        Texture sprite = this.baseSprite;
        if(startTimeWaitFor != 0 && System.currentTimeMillis() - startTimeWaitFor < waitFor)
            sprite = this.baseBrightSprite;
        else if(startTimeWaitFor != 0)
            startTimeWaitFor = 0;
        
        RvB.drawFilledRectangle(x, y, width, width, sprite, angle);
        
        sprite = this.turningSprite;
        if(startTimeWaitFor != 0 && System.currentTimeMillis() - startTimeWaitFor < waitFor)
            sprite = this.turningBrightSprite;
        else if(startTimeWaitFor != 0)
            startTimeWaitFor = 0;
        
        turningSpriteAngle += 2*game.gameSpeed;
        RvB.drawFilledRectangle(x, y, width, width, sprite, turningSpriteAngle);
    }
}
