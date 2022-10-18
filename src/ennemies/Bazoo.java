package ennemies;

import static ennemies.BasicEnemy.balance;
import java.util.ArrayList;
import javax.sound.sampled.Clip;
import managers.SoundManager;
import org.newdawn.slick.opengl.Texture;
import rvb.RvB;
import static rvb.RvB.game;
import ui.Overlay;

public class Bazoo extends Enemy{
    
    private int level;
    private static Clip
            entrance = SoundManager.Instance.getClip("boss_wave"),
            defeated = SoundManager.Instance.getClip("boss_defeated"),
            laugh = SoundManager.Instance.getClip("boss_laugh"),
            armorBreak = SoundManager.Instance.getClip("armor_break");
    private static Texture[][] evolutions = new Texture[][]{
        {RvB.textures.get("bazooEvo1"), RvB.textures.get("bazooEvo1Bright")},
        {RvB.textures.get("bazooEvo2"), RvB.textures.get("bazooEvo2Bright")},
        {RvB.textures.get("bazooEvo3"), RvB.textures.get("bazooEvo3Bright")},
        {RvB.textures.get("bazooEvo4"), RvB.textures.get("bazooEvo4Bright")}
    };
    private static float[] evolutionsLives = new float[]{0.20f, 0.15f, 0.15f, 0.5f}; // Total ne doit pas dépasser les 100%
    private static ArrayList<Float> evolutionsThresholds = new ArrayList<Float>();
    private int evoIndex;
    private int displayWidth;
    private static int baseLife = 500;
    
    public Bazoo(int lvl){
        super();
        level = lvl;
        evoIndex = lvl > evolutions.length-1 ? evolutions.length-1 : lvl-1;
        name = "Bazoo";
        reward = lvl*100;
        power = 50;
        shootRate = 1;
        moveSpeed = 2.2f;
        range = 3*RvB.unite;
        baseLife = (int)Math.round(baseLife + (baseLife*bonusLife*5/100));
        life = game.waveNumber/5 == 1 ? baseLife : baseLife+game.waveNumber*game.waveNumber*4;
        displayWidth = 3*RvB.unite;
        width = (int) (displayWidth*0.51);
        sprite = RvB.textures.get("bazoo");
        brightSprite = RvB.textures.get("bazooBright");
        volume = SoundManager.Volume.SEMI_HIGH;
        clip = SoundManager.Instance.getClip("boss_walking");
        SoundManager.Instance.setClipVolume(entrance, SoundManager.Volume.HIGH);
        SoundManager.Instance.setClipVolume(defeated, SoundManager.Volume.SEMI_HIGH);
        SoundManager.Instance.setClipVolume(laugh, SoundManager.Volume.SEMI_HIGH);
        SoundManager.Instance.setClipVolume(armorBreak, SoundManager.Volume.SEMI_HIGH);
        stepEveryMilli = 1100;
        eBalance = balance;
        
        initBack();
        
        float n = 1;
        for(int i = evolutionsLives.length-1 ; i >= 0 ; i--){
            n -= evolutionsLives[i];
            evolutionsThresholds.add(0, n);
        }
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

            if(evoIndex <= 0)
                drawBazoo();
            
            Texture sprite;
            for(int i = 0 ; i <= evoIndex ; i++){
                sprite = evolutions[i][0];
                if(startTimeWaitFor != 0 && System.currentTimeMillis() - startTimeWaitFor < waitFor && i == evoIndex)
                    sprite = evolutions[i][1];
                else if(startTimeWaitFor != 0 && i == evoIndex)
                    startTimeWaitFor = 0;
                RvB.drawFilledRectangle(x, y, displayWidth, displayWidth, sprite, angle);
                
                if(i == 0)
                    drawBazoo();
            }
        }
    }
    
    @Override
    public void attacked(int power){
        super.attacked(power);
        if(evoIndex >= 0 && life-baseLife < (maxLife-baseLife)*evolutionsThresholds.get(evoIndex)){
            SoundManager.Instance.playOnce(armorBreak);
            evoIndex--;
        }
    }
    
    @Override
    public void die(){
        if(life > 0){
            SoundManager.Instance.playOnce(laugh);
            game.bossDefeated = false;
        } 
        else{
            SoundManager.Instance.playOnce(defeated);
            game.bossDefeated = true;
        }
        game.bossDead = true;
        super.die();
    }
    
    private void drawBazoo(){
        Texture sprite = this.sprite;
        if(startTimeWaitFor != 0 && System.currentTimeMillis() - startTimeWaitFor < waitFor && evoIndex == -1)
            sprite = this.brightSprite;
        else if(startTimeWaitFor != 0 && evoIndex == -1)
            startTimeWaitFor = 0;
        RvB.drawFilledRectangle(x, y, displayWidth, displayWidth, sprite, angle);
    }
    
    @Override
    public void setStarted(boolean b){
        super.setStarted(b);
        if(started)
            SoundManager.Instance.playOnce(entrance);
    }
    
    @Override
    /// enemy.renderOverlay() is called in game, right after main overlay is rendered
    public void renderInfo(){
        Overlay o = game.getOverlays().get(2);
        o.render();
        // Sprites
        RvB.drawFilledRectangle(o.getX()+20, o.getY(), o.getH(), o.getH(), null, 1, RvB.textures.get("bazooZoomed"));
        RvB.drawFilledRectangle(o.getX()+o.getW()-o.getH()-20, o.getY(), o.getH(), o.getH(), null, 1, RvB.textures.get("bazooZoomed"));
        // Lifebar
        int width = (int) (290*RvB.ref), height = 16;
        RvB.drawFilledRectangle(o.getX()+o.getW()/2-width/2, o.getY()+o.getH()-height-3, width, height, RvB.colors.get("lightGreen"), 1, null);
        RvB.drawFilledRectangle(o.getX()+o.getW()/2-width/2, o.getY()+o.getH()-height-3, (int)(((double)life/(double)maxLife)*width), height, RvB.colors.get("life"), 1, null);
        RvB.drawRectangle(o.getX()+o.getW()/2-width/2, (int) (o.getY()+o.getH()-height-3), width, height, RvB.colors.get("green_dark"), 0.8f, 2);        
        // Name & life max
        o.drawText(o.getW()/2, 12, name, RvB.fonts.get("normalL"));
        o.drawText(o.getW()/2+RvB.fonts.get("normalL").getWidth(name)/2+RvB.fonts.get("life").getWidth(""+maxLife)/2+5, 12, ""+maxLife, RvB.fonts.get("life"));
    }
}
