package towers;

import ennemies.Enemy;
import java.util.ArrayList;
import rvb.RvB;
import managers.SoundManager;
import managers.TextManager;
import managers.TextManager.Text;
import org.lwjgl.input.Mouse;
import static rvb.RvB.game;
import static rvb.RvB.ref;
import static rvb.RvB.unite;
import rvb.Tile;
import ui.Button;
import ui.Overlay;

public class Raztech extends Tower{
    
    public static int startPrice = 100;
    public static int priceP = startPrice;
    
    public int lvl = 1;
    public int xp = 0, maxXP = 100;
    private boolean right = true;
    
    public Raztech() {
        super("raztech");
        textures.add(RvB.textures.get("raztech"));
        rotateIndex = 0;
        textureStatic = RvB.textures.get("raztech");
        if(game.raztech != null)
            textureStatic = RvB.textures.get("place");
        canRotate = true;
        price = priceP;
        life = 100;
        width = 4*RvB.unite/5;
        hitboxWidth = width;
        size = width;
        totalMoneySpent = priceP;
        name = Text.RAZTECH.getText();
        explode = false;
        follow = false;
        isMultipleShot = false;
        clip = SoundManager.Instance.getClip("gun");
        volume = SoundManager.Volume.LOW;
        SoundManager.Instance.setClipVolume(clip, volume);
        bulletSprite = RvB.textures.get("gun_bullet");
        
        range = 4*RvB.unite;
        power = 6;
        shootRate = 2f;
        bulletSpeed = 20;
        growth = 4;
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
        overlay.drawText(overlay.getW()/2, overlay.getH()/2, name+" ("+Text.RAZTECH_LVL.getText()+" "+lvl+")", RvB.fonts.get("normalL"));
        
        overlay = overlays.get(1);
        int width = (int) (600*ref), height = (int) (30*ref), x = overlays.get(0).getW()+(int)(60*ref), y = overlay.getY()+overlay.getH()/2-height/2;
        int xpWidth = (int) ((float)(xp)/(float)(maxXP) * width);

        RvB.drawString(overlays.get(0).getW()+(int)(20*ref), y+height/2, Text.XP.getText()+" :", RvB.fonts.get("normal"));
        
        RvB.drawFilledRectangle(x, y, width, height, RvB.colors.get("lightGreen"), 1f, null);
        RvB.drawFilledRectangle(x, y, xpWidth, height, RvB.colors.get("lightBlue"), 1f, null);
        RvB.drawRectangle(x, y, width, height, RvB.colors.get("green_dark"), 1f, 4);
        
        RvB.drawString(x+width/2, y+height/2, xp+"/"+maxXP, RvB.fonts.get("normalBlack"));
        
        RvB.drawString(x+width+(int)(300*ref), y+height/2, "ICI ON NOTE LES STATS", RvB.fonts.get("normal"));
        
        if(canRotate){
            b = overlay.getButtons().get(0);
            overlay.drawText(b.getX(), b.getY()-overlay.getY()-(int)(30*ref), Text.FOCUS.getText(), RvB.fonts.get("normal"));
        }
    }
    
    @Override
    public void shoot(){
        enemiesTouched.clear();
        lastShoot = System.currentTimeMillis();
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
        }
        else{
            game.raztech.x = (game.raztech.x-unite/2)/unite;
            game.raztech.y = (game.raztech.y-unite/2)/unite;
            map.get((int) game.raztech.y).set((int) game.raztech.x, new Tile("grass"));
            game.towersDestroyed.add(this);
            game.towerSelected = null;
        }   
        game.raztech.x = Math.floorDiv(Mouse.getX(), unite);
        game.raztech.y = Math.floorDiv(RvB.windHeight-Mouse.getY(), unite);
        map.get((int) game.raztech.y).set((int) game.raztech.x, null);
        game.raztech.x = game.raztech.x*unite+unite/2;
        game.raztech.y = game.raztech.y*unite+unite/2;
        game.raztech.isPlaced = true;
        
        game.money -= price;
        raisePrice();
        
        if(Math.random() < 0.5)
            SoundManager.Instance.playOnce(SoundManager.SOUND_RAZTECH1);
        else
            SoundManager.Instance.playOnce(SoundManager.SOUND_RAZTECH2);
    }
    
    @Override
    protected void raisePrice(){
        priceP *= 1.2;
        price = priceP;
    }
    
    @Override
    public void updateStats(Enemy e){
        super.updateStats(e);
        if(e.getLife()-power <= 0)
            gainXP(e.getMaxLife());
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
        maxXP *= 5;
        
        range += RvB.unite/8 + lvl*RvB.unite/8;
        shootRate += 0.1f + lvl*0.1f;
        power += 1 + lvl*1;
        size += growth;
        
        lvl++;
    }
}
