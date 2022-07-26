package ennemies;

import Utils.MyMath;
import managers.SoundManager;
import org.newdawn.slick.opengl.Texture;
import towser.Towser;
import static towser.Towser.game;
import static towser.Towser.unite;

public class FlyingEnemy extends Enemy{
    
    public static int idCount = 0, balance = 100;
    
    private double xDiffConst, yDiffConst, hyp, tileEveryPixel;
    private Texture turningSprite, turningBrightSprite, baseSprite, baseBrightSprite;
    private int turningSpriteAngle = 0;
    
    public FlyingEnemy(){
        super(++idCount);
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
        rgb = new float[]{0.4f, 0.9f, 0.1f};
        sprite = Towser.textures.get("flyingEnemy");
        baseSprite = Towser.textures.get("flyingEnemyBase");
        baseBrightSprite = Towser.textures.get("flyingEnemyBaseBright");
        turningSprite = Towser.textures.get("flyingEnemyProp");
        turningBrightSprite = Towser.textures.get("flyingEnemyPropBright");
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

        double speed = ((moveSpeed*game.gameSpeed) * Towser.deltaTime / 50) * Towser.ref;
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
        
        Towser.drawFilledRectangle(x, y, width, width, sprite, angle);
        
        sprite = this.turningSprite;
        if(startTimeWaitFor != 0 && System.currentTimeMillis() - startTimeWaitFor < waitFor)
            sprite = this.turningBrightSprite;
        else if(startTimeWaitFor != 0)
            startTimeWaitFor = 0;
        
        turningSpriteAngle += 2*game.gameSpeed;
        Towser.drawFilledRectangle(x, y, width, width, sprite, turningSpriteAngle);
    }
}
