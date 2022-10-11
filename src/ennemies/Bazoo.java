package ennemies;

import static ennemies.BasicEnemy.balance;
import managers.SoundManager;
import org.newdawn.slick.opengl.Texture;
import rvb.RvB;

public class Bazoo extends Enemy{
    
    private int level;
    private static Texture evo1, evo1Bright, evo2, evo2Bright, evo3, evo3Bright, evo4, evo4Bright;
    
    public Bazoo(int lvl){
        super();
        level = lvl;
        name = "Bazoo";
        spawnSpeed = 1f;
        reward = lvl*100;
        power = 50;
        shootRate = 1;
        moveSpeed = 2f;
        range = 3*RvB.unite;
        life = 30;
        width = 3*RvB.unite/2;
        sprite = RvB.textures.get("bazoo");
        brightSprite = RvB.textures.get("bazooBright");
        evo1 = RvB.textures.get("bazooEvo1");
        evo1Bright = RvB.textures.get("bazooEvo1Bright");
        evo2 = RvB.textures.get("bazooEvo2");
        evo2Bright = RvB.textures.get("bazooEvo2Bright");
        evo3 = RvB.textures.get("bazooEvo3");
        evo3Bright = RvB.textures.get("bazooEvo3Bright");
        evo4 = RvB.textures.get("bazooEvo4");
        evo4Bright = RvB.textures.get("bazooEvo4Bright");
        volume = SoundManager.Volume.VERY_LOW;
        clip = SoundManager.Instance.getClip("walking");
        stepEveryMilli = 700;
        eBalance = balance;
        
        initBack();
    }
    
    @Override
    public void render(){
        if(!started && stopFor == -1)
            return;
        if(sprite != null){
            double t = 0.03*moveSpeed*RvB.game.gameSpeed;
            if(t < 0.1) t = 0.1;

            if(Math.abs(angle - newAngle) <= 5)
                t = 1;
            
            angle = (1-t)*angle + t*newAngle;

            angle = Math.round(angle);

            Texture sprite = this.sprite;
            if(startTimeWaitFor != 0 && System.currentTimeMillis() - startTimeWaitFor < waitFor)
                sprite = this.brightSprite;
            else if(startTimeWaitFor != 0)
                startTimeWaitFor = 0;
            RvB.drawFilledRectangle(x, y, width, width, sprite, angle);
            
            sprite = this.evo1;
            if(startTimeWaitFor != 0 && System.currentTimeMillis() - startTimeWaitFor < waitFor)
                sprite = this.evo1Bright;
            else if(startTimeWaitFor != 0)
                startTimeWaitFor = 0;
            RvB.drawFilledRectangle(x, y, width, width, sprite, angle);
            
            sprite = this.evo2;
            if(startTimeWaitFor != 0 && System.currentTimeMillis() - startTimeWaitFor < waitFor)
                sprite = this.evo2Bright;
            else if(startTimeWaitFor != 0)
                startTimeWaitFor = 0;
            RvB.drawFilledRectangle(x, y, width, width, sprite, angle);
            
            sprite = this.evo3;
            if(startTimeWaitFor != 0 && System.currentTimeMillis() - startTimeWaitFor < waitFor)
                sprite = this.evo3Bright;
            else if(startTimeWaitFor != 0)
                startTimeWaitFor = 0;
            RvB.drawFilledRectangle(x, y, width, width, sprite, angle);
            
            sprite = this.evo4;
            if(startTimeWaitFor != 0 && System.currentTimeMillis() - startTimeWaitFor < waitFor)
                sprite = this.evo4Bright;
            else if(startTimeWaitFor != 0)
                startTimeWaitFor = 0;
            RvB.drawFilledRectangle(x, y, width, width, sprite, angle);
        }
    }
}
