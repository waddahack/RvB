package ennemies;

import static ennemies.BasicEnemy.balance;
import static ennemies.Enemy.Type.BOSS;
import managers.SoundManager;
import managers.TextManager.Text;
import rvb.RvB;
import static rvb.RvB.game;
import static rvb.RvB.ref;
import ui.Overlay;

public class Bazoo extends Enemy{
    
    public int level;
    
    public Bazoo(int lvl){
        super(BOSS);
        textures.add(RvB.textures.get("bazoo"));
        texturesBright.add(RvB.textures.get("bazooBright"));
        rotateIndex = 0;
        textureStatic = RvB.textures.get("bazoo");
        // Ce qu'il faut rajouter si on veut qu'un ennemie attaque
        /*canShoot = true;
        bulletSprite = RvB.textures.get("bullet");
        clip = SoundManager.Instance.getClip("cannon");
        SoundManager.Instance.setClipVolume(clip, volume);
        bulletSpeed = 20;*/
        level = lvl;
        name = Text.ENEMY_BOSS;
        reward = lvl*25;
        commitPower = 1000f;
        shootRate = 1f;
        moveSpeed = 2f;
        range = 3*RvB.unite;
        life = 500f;
        size = 3*RvB.unite;
        hitboxWidth = (int) (size*0.51);
        volumeWalk = SoundManager.Volume.SEMI_HIGH;
        clipWalk = SoundManager.Instance.getClip("boss_walking");
        stepEveryMilli = 1100;
        eBalance = balance;
        
        if(level > 0){
            Evolution evo1 = new Evolution(this, 35*game.waveNumber, RvB.textures.get("bazooEvo1"), RvB.textures.get("bazooEvo1Bright"), RvB.colors.get("life1"), null);
            Evolution evo2 = new Evolution(this, 35*game.waveNumber, RvB.textures.get("bazooEvo2"), RvB.textures.get("bazooEvo2Bright"), RvB.colors.get("life2"), evo1);
            Evolution evo3 = new Evolution(this, 20*game.waveNumber, RvB.textures.get("bazooEvo3"), RvB.textures.get("bazooEvo3Bright"), RvB.colors.get("life3"), evo2);
            Evolution evo4 = new Evolution(this, 20*game.waveNumber, RvB.textures.get("bazooEvo4"), RvB.textures.get("bazooEvo4Bright"), RvB.colors.get("life4"), evo3);
            Evolution evo5 = new Evolution(this, 20*game.waveNumber, RvB.textures.get("bazooEvo5"), RvB.textures.get("bazooEvo5Bright"), RvB.colors.get("life5"), evo4);
            Evolution evo6 = new Evolution(this, 20*game.waveNumber, RvB.textures.get("bazooEvo6"), RvB.textures.get("bazooEvo6Bright"), RvB.colors.get("life6"), evo5);
            Evolution evo7 = new Evolution(this, 35*game.waveNumber, RvB.textures.get("bazooEvo7"), RvB.textures.get("bazooEvo7Bright"), RvB.colors.get("life7"), evo6);
            
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
            if(level >= 5){
                evolutions.add(evo7);
            } 
        }
        
        initBack();
    }
    
    @Override
    public void die(){
        if(life > 0){
            if(!game.ended)
                SoundManager.Instance.playOnce(SoundManager.SOUND_BAZOO_LAUGH);
            game.bossDefeated = false;
        } 
        else{
            SoundManager.Instance.playOnce(SoundManager.SOUND_BAZOO_DEFEATED);
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
            SoundManager.Instance.playOnce(SoundManager.SOUND_BAZOO_ENTRANCE);
    }
    
    /// enemy.renderOverlay() is called in game, right after main overlay is rendered
    @Override
    public void renderInfo(){
        Overlay o = game.OvEnemyInfo;
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
    
    @Override
    public void renderLifeBar(){
        float life = this.life;
        float maxLife = this.maxLife;
        float[] lifeColor = RvB.colors.get("life");
        float[] bgLifeColor = RvB.colors.get("lightRed");
        if(!evolutions.isEmpty()){
            if(evolutions.size() > 1)
                bgLifeColor = evolutions.get(evolutions.size()-2).lifeColor;
            else
                bgLifeColor = lifeColor;
            life = evolutions.peek().life;
            maxLife = evolutions.peek().maxLife;
            lifeColor = evolutions.peek().lifeColor;
        }
        
        int width = (int) (100*ref), height = (int) (10*ref);
        int currentLife = (int) (((double)life/(double)maxLife)*width);
        
        RvB.drawFilledRectangle(x-width/2, y-size/2, width, height, bgLifeColor, 1, null);
        RvB.drawFilledRectangle(x-width/2, y-size/2, currentLife, height, lifeColor, 1, null);
        RvB.drawRectangle((int)(x-width/2), (int)(y-size/2), width, height, RvB.colors.get("green_dark"), 1, 3);
    }
}
