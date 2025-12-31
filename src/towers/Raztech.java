package towers;

import ennemies.Enemy;
import java.util.ArrayList;
import managers.PopupManager;
import rvb.RvB;
import managers.SoundManager;
import managers.StatsManager;
import managers.TextManager;
import managers.TextManager.Text;
import managers.TutoManager;
import org.lwjgl.input.Mouse;
import org.newdawn.slick.UnicodeFont;
import static rvb.RvB.game;
import static rvb.RvB.ref;
import static rvb.RvB.unite;
import rvb.Shootable;
import rvb.Tile;
import static towers.Tower.Type.RAZTECH;
import ui.Button;
import ui.Overlay;

public class Raztech extends Tower{
    
    public static int startPrice = 0;
    public static int priceP = startPrice;
    
    public int lvl = 1;
    public int xp = 0, maxXP = 10;
    private float bonusXP = 0, chanceToKill = 0;
    private boolean right = true;
    
    public Raztech() {
        super(RAZTECH);
        textures.add(RvB.textures.get("raztech"));
        rotateIndexShoot = 0;
        textureStatic = RvB.textures.get("raztech");
        price = priceP;
        life = 0f;
        size = 4*RvB.unite/5;
        hitboxWidth = size;
        totalMoneySpent = priceP;
        name = Text.RAZTECH;
        follow = false;
        clip = SoundManager.Instance.getClip("gun");
        volume = SoundManager.Volume.LOW;
        SoundManager.Instance.setClipVolume(clip, volume);
        bulletSprite = RvB.textures.get("gun_bullet");

        range = 3*RvB.unite;
        power = 6f;
        shootRate = 1.8f;
        bulletSpeed = 20;
        growth = 3*ref;
        
        upgrades.add(new Upgrade(this, "Range", range, RvB.unite/4, "+", 0, 0, 0));
        upgrades.add(new Upgrade(this, "Power", power, 0.5f, "+", 0, 0, 0));
        upgrades.add(new Upgrade(this, "Attack speed", shootRate, 0.1f, "+", 0, 0, 0));
        
        initBack();
        initOverlay();
    }
    
    @Override
    public void initOverlay(){
        Overlay o1, o2, o3;
        
        o1 = new Overlay(0, RvB.windHeight-(int)((60+45)*ref), (int)(200*ref), (int)(45*ref));
        o1.setBG(RvB.textures.get("board"), 0.6f);
        overlays.add(o1);
        
        o2 = new Overlay(0, RvB.windHeight-(int)(60*ref), RvB.windWidth, (int)(60*ref));
        o2.setBG(RvB.textures.get("board"), 0.6f);
        int imageSize = o2.getH()-(int)(5*ref);
        o2.addImage(o1.getW()/2, o2.getH()/2, imageSize, imageSize, RvB.textures.get("raztech"));
        
        // init upgrades position
        int sep = (int) (100 * ref);
        if(sep < 25) sep = 25;
        for(int i = 0 ; i < upgrades.size() ; i++){
            upgrades.get(i).initPosAndButton(o1.getW() + (int)(620*ref)+8 + i*sep, o2.getY()+o2.getH()/2, this); // +8 = 2*epaisseur border du rectangle
        }
        
        // Buffs
        o2.addImage(o2.getW()-(int) (760*ref), o2.getH()/2, (int)(32*ref), (int)(32*ref), RvB.textures.get("buff_Upgrade"));
        o2.addImage(o2.getW()-(int) (680*ref), o2.getH()/2, (int)(32*ref), (int)(32*ref), RvB.textures.get("buff_Slow"));
        o2.addImage(o2.getW()-(int) (600*ref), o2.getH()/2, (int)(32*ref), (int)(32*ref), RvB.textures.get("buff_OS"));
        o2.addImage(o2.getW()-(int) (520*ref), o2.getH()/2, (int)(32*ref), (int)(32*ref), RvB.textures.get("buff_XP"));

        // button focus
        focusButton = new Button(o2.getW()-(int)(140*ref), o2.getH()-(int)(20*ref), (int)(120*ref), (int)(32*ref), TextManager.Text.FOCUS_SWITCH, RvB.fonts.get("normal"), RvB.colors.get("green_semidark"), RvB.colors.get("green_dark"), 0);
        focusButton.setSwitch();
        focusButton.setFunction(__ -> {
            focusIndex = focusButton.indexSwitch;
        });
        o2.addButton(focusButton);
        overlays.add(o2);
        
        // Stats
        o3 = new Overlay(o2.getX(), o2.getY(), o2.getW(), o2.getH());
        o3.setBG(RvB.textures.get("board"), 0.6f);
        o3.addImage(o1.getW()/2, o3.getH()/2, imageSize, imageSize, textureStatic);
        o3.display(false);
        overlays.add(o3);
    }
    
    @Override
    public void renderOverlay(){
        Button b;
        Overlay overlay;
        
        for(Overlay o : overlays)
            o.render();
        
        overlay = overlays.get(0);
        overlay.drawText(overlay.getW()/2, overlay.getH()/2, name.getText()+" ("+Text.LVL.getText()+lvl+")", RvB.fonts.get("normalL"));
        
        overlay = overlays.get(1);
        if(overlay.isDisplayed()){
            int width = (int) (500*ref), height = (int) (30*ref), x = overlays.get(0).getW()+(int)(60*ref), y = overlay.getY()+overlay.getH()/2-height/2;
            int xpWidth = (int) ((float)(xp)/(float)(maxXP) * width);
            if(xpWidth < 0) xpWidth = 0;

            RvB.drawString(overlays.get(0).getW()+(int)(20*ref), y+height/2, Text.XP.getText()+" :", RvB.fonts.get("normal"));

            RvB.drawFilledRectangle(x, y, width, height, RvB.colors.get("lightGreen"), 1f, null);
            RvB.drawFilledRectangle(x, y, xpWidth, height, RvB.colors.get("lightBlue"), 1f, null);
            RvB.drawRectangle(x, y, width, height, RvB.colors.get("green_dark"), 1f, (int)(4*ref));

            RvB.drawString(x+width/2, y+height/2, xp+"/"+maxXP, RvB.fonts.get("normalBlack"));

            for(Upgrade up : upgrades)
                up.render();

            // buff upgrade
            overlay.drawText(overlay.getW()-(int) (740*ref), (int)(overlay.getH()/2-RvB.fonts.get("normalS").getFont().getSize()/2-18*ref), (int)(bonusRange*100)+"%", RvB.fonts.get("normalS"), "topLeft");
            overlay.drawText(overlay.getW()-(int) (740*ref), overlay.getH()/2-RvB.fonts.get("normalS").getFont().getSize()/2, (int)(bonusPower*100)+"%", RvB.fonts.get("normalS"), "topLeft");
            overlay.drawText(overlay.getW()-(int) (740*ref), (int)(overlay.getH()/2-RvB.fonts.get("normalS").getFont().getSize()/2+18*ref), (int)(bonusShootRate*100)+"%", RvB.fonts.get("normalS"), "topLeft");
            // buff slow
            overlay.drawText(overlay.getW()-(int) (660*ref), overlay.getH()/2-RvB.fonts.get("normal").getFont().getSize()/2, (int)(slow*100)+"%", RvB.fonts.get("normal"), "topLeft");
            // buff xp
            overlay.drawText(overlay.getW()-(int) (580*ref), overlay.getH()/2-RvB.fonts.get("normal").getFont().getSize()/2, (int)(chanceToKill*100)+"%", RvB.fonts.get("normal"), "topLeft");        
            // buff xp
            overlay.drawText(overlay.getW()-(int) (500*ref), overlay.getH()/2-RvB.fonts.get("normal").getFont().getSize()/2, (int)(bonusXP*100)+"%", RvB.fonts.get("normal"), "topLeft");

            b = overlay.getButtons().get(0);
            overlay.drawText(b.getX(), b.getY()-overlay.getY()-(int)(30*ref), Text.FOCUS.getText(), RvB.fonts.get("normal"));
        }
        
        overlay = overlays.get(2);
        if(overlay.isDisplayed()){
            overlay.drawText(2*overlay.getW()/8, overlay.getH()/2, Text.THIS_WAVE.getText()+" :", RvB.fonts.get("normalLB"), "center");
            
            overlay.drawText(3*overlay.getW()/8, (int)(8*ref), Text.ENEMIES_KILLED.getText(), RvB.fonts.get("titleS"), "topMid");
            overlay.drawText(3*overlay.getW()/8, 2*overlay.getH()/3, ""+enemiesKilledThisWave, RvB.fonts.get("normal"), "center");
            overlay.drawText(4*overlay.getW()/8, (int)(8*ref), Text.DAMAGES_DONE.getText(), RvB.fonts.get("titleS"), "topMid");
            overlay.drawText(4*overlay.getW()/8, 2*overlay.getH()/3, ""+(int)damagesDoneThisWave, RvB.fonts.get("normal"), "center");
            
            overlay.drawText(5*overlay.getW()/8, overlay.getH()/2, Text.TOTAL.getText()+" :", RvB.fonts.get("normalLB"), "center");
            
            overlay.drawText(6*overlay.getW()/8, (int)(8*ref), Text.ENEMIES_KILLED.getText(), RvB.fonts.get("titleS"), "topMid");
            overlay.drawText(6*overlay.getW()/8, 2*overlay.getH()/3, ""+enemiesKilled, RvB.fonts.get("normal"), "center");
            overlay.drawText(7*overlay.getW()/8, (int)(8*ref), Text.DAMAGES_DONE.getText(), RvB.fonts.get("titleS"), "topMid");
            overlay.drawText(7*overlay.getW()/8, 2*overlay.getH()/3, ""+(int)damagesDone, RvB.fonts.get("normal"), "center");
        }
    }
    
    @Override
    public void shoot(){
        lastShoot = RvB.game.timeInGamePassed;
        float x = (float)(this.x+size/2*Math.cos(Math.toRadians(angleShoot)));
        float y = (float)(this.y+size/2*Math.sin(Math.toRadians(angleShoot)));
        if(right){ 
            x -= size/4*Math.sin(Math.toRadians(angleShoot)); 
            y += +size/4*Math.cos(Math.toRadians(angleShoot));
        }
        else{
            x += size/4*Math.sin(Math.toRadians(angleShoot)); 
            y -= +size/4*Math.cos(Math.toRadians(angleShoot));
        }
        right = !right;
        x = Math.abs(x) < 2 ? 0 : x;
        y = Math.abs(y) < 2 ? 0 : y;
        Bullet bullet = new Bullet(this, x, y, enemyAimed, size/4 + bulletSizeBonus, bulletSprite, false);
        bullets.add(bullet);
        SoundManager.Instance.playOnce(clip);
    }
    
    @Override
    public void place(ArrayList<ArrayList<Tile>> map){
        if(game.raztech == null){
            game.OvShop.getButtons().get(game.OvShop.getButtons().size()-1).setBG(RvB.textures.get("placeRaztech"));
            game.OvShop.getButtons().get(0).unlock();
            game.raztech = this;
        }
        else{
            xp -= 0.2*maxXP;
        }   
        x = Math.floorDiv(Mouse.getX(), unite);
        y = Math.floorDiv(RvB.windHeight-Mouse.getY(), unite);
        map.get((int) y).set((int) x, null);
        x = x*unite+unite/2;
        y = y*unite+unite/2;
        game.oldRaztechXpos = (int) x;
        game.oldRaztechYpos = (int) y;
        isPlaced = true;
        started = true;
        game.OvShop.getButtons().get(0).unlock();
        
        if(Math.random() < 0.5)
            SoundManager.Instance.playOnce(SoundManager.SOUND_RAZTECH1);
        else
            SoundManager.Instance.playOnce(SoundManager.SOUND_RAZTECH2);
        StatsManager.Instance.updateTowerPlaced(type);
        TutoManager.Instance.showTutoIfNotDone(TutoManager.TutoStep.RZTCH_PLCD);
    }
    
    @Override
    public void autoPlace(ArrayList<ArrayList<Tile>> map){
        if(game.raztech == null){
            game.OvShop.getButtons().get(game.OvShop.getButtons().size()-1).setBG(RvB.textures.get("placeRaztech"));
            game.OvShop.getButtons().get(0).unlock();
            game.raztech = this;
        }
        else{
            xp -= 0.2*maxXP;
        }   
        x = Math.floorDiv((int)x, unite);
        y = Math.floorDiv((int)y, unite);
        map.get((int) y).set((int) x, null);
        x = x*unite+unite/2;
        y = y*unite+unite/2;
        game.oldRaztechXpos = (int) x;
        game.oldRaztechYpos = (int) y;
        isPlaced = true;
        started = true;
        game.OvShop.getButtons().get(0).unlock();
    }
    
    @Override
    public boolean canBePlaced(){
        if(!TutoManager.Instance.hasDone(TutoManager.TutoStep.RZTCH_PLCD)){
            if(!super.canBePlaced())
                return false;
            int ix = Math.floorDiv((int) x, unite), iy = Math.floorDiv((int)y, unite);
            if(ix == 4 && iy == 4)
                return true;
            return false;
        }
        return super.canBePlaced();
    }
    
    @Override
    public void attack(Shootable enemy){ 
        if(!enemy.hasStarted())
            return;
        Enemy e = (Enemy) enemy;
        boolean isEnemyBazoo = (e == game.bazoo);
        float d;
        if(Math.random() <= chanceToKill)
            d = e.takeDamage(getPower()*18);
        else
            d = e.takeDamage(getPower());
        damagesDone += d;
        damagesDoneThisWave += d;
        e.beSlowedBy(slow);
        if(e.isDead()){
            enemiesKilled += 1;
            enemiesKilledThisWave += 1;
            if(!isEnemyBazoo)
                gainXP(e.getReward()*2);
            
            StatsManager.Instance.updateEnemyKilled(e.type);
        }
    }
    
    public void gainXP(int amount){
        xp += Math.round(amount+amount*bonusXP);
        if(xp >= maxXP){
            levelUp();
        }
    }
    
    public void levelUp(){
        levelUp(false);
        TutoManager.Instance.showTutoIfNotDone(TutoManager.TutoStep.LVL_P);
    }
    
    public void levelUp(boolean setup){
        if(!setup){
            xp -= maxXP;
            if(xp < 0) xp = 0;
        }
        maxXP = (int) (maxXP*1.6);
        
        range = (int) upgrades.get(0).setNewValue();
        power = (int) upgrades.get(1).setNewValue();
        shootRate = upgrades.get(2).setNewValue();
        
        size += growth;
        
        if(!setup)
            lvl++;
        if(lvl >= 2)
            game.OvShop.getButtons().get(1).unlock();
        if(lvl >= 4)
            game.OvShop.getButtons().get(2).unlock();
        if(lvl >= 6)
            game.OvShop.getButtons().get(3).unlock();
        
        if(!setup){
            SoundManager.Instance.playOnce(SoundManager.SOUND_LEVELUP);
            if(!game.buffs.empty())
                PopupManager.Instance.rewardSelection();
            else
                PopupManager.Instance.popup(new String[]{Text.LEVEL.getText()+lvl+" !", "\n", Text.RANGE.getText()+" +"+Math.round(upgrades.get(0).addOrMultiplicateValue), Text.POWER.getText()+" +"+Math.round(upgrades.get(1).addOrMultiplicateValue), Text.SHOOTRATE.getText()+" +"+upgrades.get(2).addOrMultiplicateValue, Text.NOTHING_LEFT.getText()}, new UnicodeFont[]{RvB.fonts.get("normalXLB"), RvB.fonts.get("normal"), RvB.fonts.get("normal"), RvB.fonts.get("normal"), RvB.fonts.get("normal"), RvB.fonts.get("normalL")}, Text.DOTS);
            // Stats
            if(lvl > StatsManager.Instance.raztechLvlMax)
                StatsManager.Instance.raztechLvlMax = lvl;
        }
    }
    
    public void upgradeStats(){
        range = (int) upgrades.get(0).setNewValue();
        power = (int) upgrades.get(1).setNewValue();
        shootRate = upgrades.get(2).setNewValue();
    }
    
    public void addBonusXP(float amount){
        bonusXP += amount;
    }
    
    public void addChanceToKill(float amount){
        chanceToKill += amount;
    }
    
    @Override
    public String getJSON(){
        return "{"+
                "\"type\":\""+type+"\", "+
                "\"indexXSaved\":"+getIndexX()+", "+
                "\"indexYSaved\":"+getIndexY()+", "+
                "\"angle\":"+angle+", "+
                "\"focusIndex\":"+getFocusIndex()+", "+
                "\"enemiesKilled\":"+enemiesKilled+", "+
                "\"damagesDone\":"+damagesDone+", "+
                "\"enemiesKilledThisWave\":"+enemiesKilledThisWave+", "+
                "\"damagesDoneThisWave\":"+damagesDoneThisWave+", "+
                "\"nbUpgradesUsed\":[], "+
                "\"lvl\":"+lvl+", "+
                "\"xp\":"+xp+
               "}";
    }
}
