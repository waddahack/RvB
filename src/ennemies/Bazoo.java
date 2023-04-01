package ennemies;

import static ennemies.BasicEnemy.balance;
import javax.sound.sampled.Clip;
import managers.SoundManager;
import managers.TextManager.Text;
import rvb.RvB;
import static rvb.RvB.game;
import static rvb.RvB.ref;
import ui.Overlay;

public class Bazoo extends Enemy{
    
    public int level;
    private static Clip
            entrance = SoundManager.Instance.getClip("boss_wave"),
            defeated = SoundManager.Instance.getClip("boss_defeated"),
            laugh = SoundManager.Instance.getClip("boss_laugh");
    
    public Bazoo(int lvl){
        super();
        textures.add(RvB.textures.get("bazoo"));
        texturesBright.add(RvB.textures.get("bazooBright"));
        rotateIndex = 0;
        textureStatic = RvB.textures.get("bazoo");
        // Ce qu'il faut rajouter si on veut qu'un ennemie attaque
        // + problème d'angle de enemy + faire une autre variable commitPower
        /*canShoot = true;
        bulletSprite = RvB.textures.get("bullet");
        clip = SoundManager.Instance.getClip("cannon");
        SoundManager.Instance.setClipVolume(clip, volume);
        bulletSpeed = 20;*/
        level = lvl;
        name = Text.ENEMY_BOSS;
        reward = lvl*50;
        power = 50f;
        shootRate = 1f;
        moveSpeed = 2f;
        range = 3*RvB.unite;
        life = 800f;
        size = 3*RvB.unite;
        hitboxWidth = (int) (size*0.51);
        volumeWalk = SoundManager.Volume.SEMI_HIGH;
        clipWalk = SoundManager.Instance.getClip("boss_walking");
        SoundManager.Instance.setClipVolume(entrance, SoundManager.Volume.HIGH);
        SoundManager.Instance.setClipVolume(defeated, SoundManager.Volume.SEMI_HIGH);
        SoundManager.Instance.setClipVolume(laugh, SoundManager.Volume.SEMI_HIGH);
        stepEveryMilli = 1100;
        eBalance = balance;
        
        if(level > 0){
            Evolution evo1 = new Evolution(this, 50*game.waveNumber, RvB.textures.get("bazooEvo1"), RvB.textures.get("bazooEvo1Bright"), RvB.colors.get("life1"), null);
            Evolution evo2 = new Evolution(this, 50*game.waveNumber, RvB.textures.get("bazooEvo2"), RvB.textures.get("bazooEvo2Bright"), RvB.colors.get("life2"), evo1);
            Evolution evo3 = new Evolution(this, 20*game.waveNumber, RvB.textures.get("bazooEvo3"), RvB.textures.get("bazooEvo3Bright"), RvB.colors.get("life3"), evo2);
            Evolution evo4 = new Evolution(this, 20*game.waveNumber, RvB.textures.get("bazooEvo4"), RvB.textures.get("bazooEvo4Bright"), RvB.colors.get("life4"), evo3);
            Evolution evo5 = new Evolution(this, 20*game.waveNumber, RvB.textures.get("bazooEvo5"), RvB.textures.get("bazooEvo5Bright"), RvB.colors.get("life5"), evo4);
            Evolution evo6 = new Evolution(this, 20*game.waveNumber, RvB.textures.get("bazooEvo6"), RvB.textures.get("bazooEvo6Bright"), RvB.colors.get("life6"), evo5);
            
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
    public float takeDamage(float power){
        if(!started)
            return 0;

        float damageDone = power;
        if(!evolutions.isEmpty()){
            evolutions.peek().attacked(power);
            if(evolutions.peek().life <= 0){
                damageDone += evolutions.peek().life;
                evolutions.pop();
                SoundManager.Instance.playOnce(armorBreak);
            }
        }
        else
            life -= power;
        
        startTimeWaitFor = game.timeInGamePassed;
        if(life <= 0){
            damageDone += life;
            die();
        }    
        
        return damageDone;
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
        game.bazoo = null;
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
    @Override
    public void renderInfo(){
        Overlay o = game.getOverlays().get(2);
        o.render();
        // Sprites
        RvB.drawFilledRectangle(o.getX()+20, o.getY(), o.getH(), o.getH(), null, 1, RvB.textures.get("bazooZoomed"));
        RvB.drawFilledRectangle(o.getX()+o.getW()-o.getH()-20, o.getY(), o.getH(), o.getH(), null, 1, RvB.textures.get("bazooZoomed"));
        // Lifebar
        int width = (int) (290*ref), height = (int) (16*ref);
        int currentLife = (int) (evolutions.isEmpty() ? ((double)life/(double)maxLife)*width : ((double)evolutions.peek().life/(double)evolutions.peek().maxLife)*width);
        float[] bgColor = evolutions.isEmpty() ? RvB.colors.get("lightGreen") : evolutions.size() > 1 ? evolutions.get(evolutions.size()-2).lifeColor : RvB.colors.get("life");
        RvB.drawFilledRectangle(o.getX()+o.getW()/2-width/2, o.getY()+o.getH()-height-3, width, height, bgColor, 1, null);
        RvB.drawFilledRectangle(o.getX()+o.getW()/2-width/2, o.getY()+o.getH()-height-3, currentLife, height, evolutions.isEmpty() ? RvB.colors.get("life") : evolutions.peek().lifeColor, 1, null);
        RvB.drawRectangle(o.getX()+o.getW()/2-width/2, (int) (o.getY()+o.getH()-height-3), width, height, RvB.colors.get("green_dark"), 0.8f, 2);        
        // Name & life max
        o.drawText(o.getW()/2, (int) (12*ref), name.getText(), RvB.fonts.get("normalL"));
        o.drawText(o.getW()/2+RvB.fonts.get("normalL").getWidth(name.getText())/2+RvB.fonts.get("life").getWidth(""+maxLife)/2+5, (int) (12*ref), ""+Math.round(maxLife), RvB.fonts.get("life"));
    }
}
