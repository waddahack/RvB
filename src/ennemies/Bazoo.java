package ennemies;

import static ennemies.BasicEnemy.balance;
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
            laugh = SoundManager.Instance.getClip("boss_laugh");
    
    public Bazoo(int lvl){
        super();
        level = lvl;
        name = "Bazoo";
        reward = lvl*100;
        power = 50;
        shootRate = 1;
        moveSpeed = 2.2f;
        range = 3*RvB.unite;
        life = 200;
        width = 3*RvB.unite;
        hitboxWidth = (int) (width*0.51);
        sprite = RvB.textures.get("bazoo");
        brightSprite = RvB.textures.get("bazooBright");
        volume = SoundManager.Volume.SEMI_HIGH;
        clip = SoundManager.Instance.getClip("boss_walking");
        SoundManager.Instance.setClipVolume(entrance, SoundManager.Volume.HIGH);
        SoundManager.Instance.setClipVolume(defeated, SoundManager.Volume.SEMI_HIGH);
        SoundManager.Instance.setClipVolume(laugh, SoundManager.Volume.SEMI_HIGH);
        stepEveryMilli = 1100;
        eBalance = balance;
        
        if(level > 0){
            Evolution evo1 = new Evolution(this, 500, RvB.textures.get("bazooEvo1"), RvB.textures.get("bazooEvo1Bright"), RvB.colors.get("life1"), null);
            Evolution evo2 = new Evolution(this, 500, RvB.textures.get("bazooEvo2"), RvB.textures.get("bazooEvo2Bright"), RvB.colors.get("life2"), evo1);
            Evolution evo3 = new Evolution(this, 300, RvB.textures.get("bazooEvo3"), RvB.textures.get("bazooEvo3Bright"), RvB.colors.get("life3"), evo2);
            Evolution evo4 = new Evolution(this, 300, RvB.textures.get("bazooEvo4"), RvB.textures.get("bazooEvo4Bright"), RvB.colors.get("life4"), evo3);
            Evolution evo5 = new Evolution(this, 500, RvB.textures.get("bazooEvo5"), RvB.textures.get("bazooEvo5Bright"), RvB.colors.get("life5"), evo4);
            Evolution evo6 = new Evolution(this, 500, RvB.textures.get("bazooEvo6"), RvB.textures.get("bazooEvo6Bright"), RvB.colors.get("life6"), evo5);
            
            evolutions.add(evo1);
            if(level >= 2)
                evolutions.add(evo2);
            if(level >= 3){
                evolutions.add(evo3);
                evolutions.add(evo4);
            }
            if(level >= 4){
                evolutions.add(evo5);
                evolutions.add(evo6);
            }
                
        }
        
        initBack();
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

    @Override
    public void setStarted(boolean b){
        super.setStarted(b);
        if(started)
            SoundManager.Instance.playOnce(entrance);
    }
    
    /// enemy.renderOverlay() is called in game, right after main overlay is rendered
    public void renderInfo(){
        Overlay o = game.getOverlays().get(2);
        o.render();
        // Sprites
        RvB.drawFilledRectangle(o.getX()+20, o.getY(), o.getH(), o.getH(), null, 1, sprite);
        RvB.drawFilledRectangle(o.getX()+o.getW()-o.getH()-20, o.getY(), o.getH(), o.getH(), null, 1, sprite);
        // Lifebar
        int width = (int) (290*RvB.ref), height = 16;
        int currentLife = (int) (evolutions.isEmpty() ? ((double)life/(double)maxLife)*width : ((double)evolutions.peek().life/(double)evolutions.peek().maxLife)*width);
        float[] bgColor = evolutions.isEmpty() ? RvB.colors.get("lightGreen") : evolutions.size() > 1 ? evolutions.get(evolutions.size()-2).lifeColor : RvB.colors.get("life");
        RvB.drawFilledRectangle(o.getX()+o.getW()/2-width/2, o.getY()+o.getH()-height-3, width, height, bgColor, 1, null);
        RvB.drawFilledRectangle(o.getX()+o.getW()/2-width/2, o.getY()+o.getH()-height-3, currentLife, height, evolutions.isEmpty() ? RvB.colors.get("life") : evolutions.peek().lifeColor, 1, null);
        RvB.drawRectangle(o.getX()+o.getW()/2-width/2, (int) (o.getY()+o.getH()-height-3), width, height, RvB.colors.get("green_dark"), 0.8f, 2);        
        // Name & life max
        o.drawText(o.getW()/2, 12, name, RvB.fonts.get("normalL"));
        o.drawText(o.getW()/2+RvB.fonts.get("normalL").getWidth(name)/2+RvB.fonts.get("life").getWidth(""+maxLife)/2+5, 12, ""+maxLife, RvB.fonts.get("life"));
    }
}
