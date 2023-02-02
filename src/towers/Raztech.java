package towers;

import Buffs.Buff;
import ennemies.Enemy;
import java.util.ArrayList;
import java.util.HashMap;
import managers.PopupManager;
import rvb.RvB;
import managers.SoundManager;
import managers.TextManager;
import managers.TextManager.Text;
import org.lwjgl.input.Mouse;
import org.newdawn.slick.UnicodeFont;
import static rvb.RvB.game;
import static rvb.RvB.ref;
import static rvb.RvB.unite;
import rvb.Tile;
import ui.Button;
import ui.Overlay;

public class Raztech extends Tower{
    
    public static int startPrice = 0;
    public static int priceP = startPrice;
    
    public int lvl = 1;
    public int xp = 0, maxXP = 80;
    private boolean right = true;
    public HashMap<Buff, Integer> buffs;
    
    public Raztech() {
        super("raztech");
        textures.add(RvB.textures.get("raztech"));
        rotateIndex = 0;
        textureStatic = RvB.textures.get("raztech");
        if(game.raztech != null)
            textureStatic = RvB.textures.get("place");
        canRotate = true;
        price = priceP;
        life = 100f;
        width = 4*RvB.unite/5;
        hitboxWidth = width;
        size = width;
        totalMoneySpent = priceP;
        name = Text.RAZTECH;
        explode = false;
        follow = false;
        isMultipleShot = false;
        clip = SoundManager.Instance.getClip("gun");
        volume = SoundManager.Volume.LOW;
        SoundManager.Instance.setClipVolume(clip, volume);
        bulletSprite = RvB.textures.get("gun_bullet");

        range = 4*RvB.unite;
        power = 4f;
        shootRate = 2f;
        bulletSpeed = 20;
        growth = 4*ref;
        
        upgrades.add(new Upgrade(this, "Range", range, RvB.unite/4, "+", 0, 0, 0));
        upgrades.add(new Upgrade(this, "Power", power, 1, "+", 0, 0, 0));
        upgrades.add(new Upgrade(this, "Attack speed", shootRate, 0.1f, "+", 0, 0, 0));
        
        buffs = new HashMap<>();
    }
    
    @Override
    public void initOverlay(){
        Overlay o1, o2;
        
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
        o2.addImage(o2.getW()-(int) (760*ref), o2.getH()/2, (int)(32*ref), (int)(32*ref), RvB.textures.get("buff_upgrade"));
        o2.addImage(o2.getW()-(int) (650*ref), o2.getH()/2, (int)(32*ref), (int)(32*ref), RvB.textures.get("buff_slow"));

        // button focus
        if(canRotate){
            focusButton = new Button(o2.getW()-(int)(140*ref), o2.getH()-(int)(20*ref), (int)(120*ref), (int)(32*ref), TextManager.Text.FOCUS_SWITCH, RvB.fonts.get("normal"), RvB.colors.get("green_semidark"), RvB.colors.get("green_dark"), 0);
            focusButton.setSwitch();
            o2.addButton(focusButton);
        }
        overlays.add(o2);
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
        int width = (int) (500*ref), height = (int) (30*ref), x = overlays.get(0).getW()+(int)(60*ref), y = overlay.getY()+overlay.getH()/2-height/2;
        int xpWidth = (int) ((float)(xp)/(float)(maxXP) * width);
        if(xpWidth < 0) xpWidth = 0;

        RvB.drawString(overlays.get(0).getW()+(int)(20*ref), y+height/2, Text.XP.getText()+" :", RvB.fonts.get("normal"));
        
        RvB.drawFilledRectangle(x, y, width, height, RvB.colors.get("lightGreen"), 1f, null);
        RvB.drawFilledRectangle(x, y, xpWidth, height, RvB.colors.get("lightBlue"), 1f, null);
        RvB.drawRectangle(x, y, width, height, RvB.colors.get("green_dark"), 1f, 4);
        
        RvB.drawString(x+width/2, y+height/2, xp+"/"+maxXP, RvB.fonts.get("normalBlack"));

        for(Upgrade up : upgrades)
            up.render();
        
        // buff upgrade
        overlay.drawText(overlay.getW()-(int) (720*ref), (int)(overlay.getH()/2-18*ref), (int)(bonusRange*100)+"%", RvB.fonts.get("normalS"));
        overlay.drawText(overlay.getW()-(int) (720*ref), overlay.getH()/2, (int)(bonusPower*100)+"%", RvB.fonts.get("normalS"));
        overlay.drawText(overlay.getW()-(int) (720*ref), (int)(overlay.getH()/2+18*ref), (int)(bonusShootRate*100)+"%", RvB.fonts.get("normalS"));
        // buff slow
        overlay.drawText(overlay.getW()-(int) (610*ref), overlay.getH()/2, (int)(slow*100)+"%", RvB.fonts.get("normal"));
        
        if(canRotate){
            b = overlay.getButtons().get(0);
            overlay.drawText(b.getX(), b.getY()-overlay.getY()-(int)(30*ref), Text.FOCUS.getText(), RvB.fonts.get("normal"));
        }
    }
    
    @Override
    public void shoot(){
        enemiesTouched.clear();
        lastShoot = RvB.game.timeInGamePassed;
        float x = (float)(this.x+size/2*Math.cos(Math.toRadians(angle)));
        float y = (float)(this.y+size/2*Math.sin(Math.toRadians(angle)));
        if(right){ 
            x -= size/4*Math.sin(Math.toRadians(angle)); 
            y += +size/4*Math.cos(Math.toRadians(angle));
        }
        else{
            x += size/4*Math.sin(Math.toRadians(angle)); 
            y -= +size/4*Math.cos(Math.toRadians(angle));
        }
        right = !right;
        x = Math.abs(x) < 2 ? 0 : x;
        y = Math.abs(y) < 2 ? 0 : y;
        Bullet bullet = new Bullet(this, x, y, enemyAimed, size/4 + bulletSizeBonus, bulletSprite, false);
        bullets.add(bullet);
        if(clip != null){
            if(continuousSound){
                if(!soundPlayed){
                    SoundManager.Instance.playLoop(clip);
                    soundPlayed = true;
                }
            }
            else
                SoundManager.Instance.playOnce(clip);
        }
    }
    
    @Override
    public void place(ArrayList<ArrayList<Tile>> map){
        if(!game.raztech.isPlaced){
            initOverlay();
            game.getOverlays().get(0).getButtons().get(game.getOverlays().get(0).getButtons().size()-1).setBG(RvB.textures.get("placeRaztech"));
            game.getOverlays().get(0).getButtons().get(0).unlock();
        }
        else{
            game.raztech.x = (game.raztech.x-unite/2)/unite;
            game.raztech.y = (game.raztech.y-unite/2)/unite;
            map.get((int) game.raztech.y).set((int) game.raztech.x, new Tile("grass"));
            game.towersToBeDestroyed.add(this);
            game.selectTower(game.raztech);
            game.raztech.xp -= 0.2*game.raztech.maxXP;
        }   
        game.raztech.x = Math.floorDiv(Mouse.getX(), unite);
        game.raztech.y = Math.floorDiv(RvB.windHeight-Mouse.getY(), unite);
        map.get((int) game.raztech.y).set((int) game.raztech.x, null);
        game.raztech.x = game.raztech.x*unite+unite/2;
        game.raztech.y = game.raztech.y*unite+unite/2;
        game.raztech.isPlaced = true;
        
        if(Math.random() < 0.5)
            SoundManager.Instance.playOnce(SoundManager.SOUND_RAZTECH1);
        else
            SoundManager.Instance.playOnce(SoundManager.SOUND_RAZTECH2);
    }
    
    @Override
    public void updateStats(Enemy e){
        super.updateStats(e);
        if(e.getLife()-power <= 0 && e.name.getText() != Text.ENEMY_BOSS.getText())
            gainXP((int)(e.getMaxLife()/2));
    }
    
    public void gainXP(int amount){
        xp += amount;
        if(xp >= maxXP){
            levelUp();
        }
    }
    
    public void levelUp(){
        xp -= maxXP;
        if(xp < 0) xp = 0;
        maxXP = (int) (maxXP*1.5);
        
        range = (int) upgrades.get(0).setNewValue();
        power = (int) upgrades.get(1).setNewValue();
        shootRate = upgrades.get(2).setNewValue();
        
        size += growth;
        
        lvl++;
        if(lvl == 2)
            game.getOverlays().get(0).getButtons().get(1).unlock();
        else if(lvl == 4)
            game.getOverlays().get(0).getButtons().get(2).unlock();
        else if(lvl == 6)
            game.getOverlays().get(0).getButtons().get(3).unlock();
        
        SoundManager.Instance.playOnce(SoundManager.SOUND_LEVELUP);
        if(!game.buffs.empty())
            PopupManager.Instance.rewardSelection();
        else
            PopupManager.Instance.popup(new String[]{Text.LEVEL.getText()+lvl+" !", "\n", Text.RANGE.getText()+" +"+Math.round(upgrades.get(0).addOrMultiplicateValue), Text.POWER.getText()+" +"+Math.round(upgrades.get(1).addOrMultiplicateValue), Text.SHOOTRATE.getText()+" +"+upgrades.get(2).addOrMultiplicateValue, Text.NOTHING_LEFT.getText()}, new UnicodeFont[]{RvB.fonts.get("normalXLB"), RvB.fonts.get("normal"), RvB.fonts.get("normal"), RvB.fonts.get("normal"), RvB.fonts.get("normal"), RvB.fonts.get("normalL")}, "...");
    }
    
    public void upgradeStats(){
        range = (int) upgrades.get(0).setNewValue();
        power = (int) upgrades.get(1).setNewValue();
        shootRate = upgrades.get(2).setNewValue();
    }
}
