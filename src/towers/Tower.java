package towers;

import rvb.Tile;
import java.util.ArrayList;
import managers.TextManager.Text;
import org.lwjgl.input.Mouse;
import rvb.RvB;
import rvb.Shootable;
import managers.SoundManager;
import managers.TextManager;
import static rvb.RvB.game;
import static rvb.RvB.ref;
import static rvb.RvB.unite;
import ui.*;
import Utils.MyMath;

public abstract class Tower extends Shootable{

    public int price;
    
    protected int hitboxWidth, totalMoneySpent;
    protected float growth = 0;
    protected boolean isPlaced = false, forBuff = false;
    protected ArrayList<Overlay> overlays;
    protected ArrayList<Upgrade> upgrades;
    public String type;
    protected Button focusButton;
    
    public Tower(String type){
        super();
        this.type = type;
        this.x = Mouse.getX();
        this.y = RvB.windHeight-Mouse.getY();
        upgrades = new ArrayList<>();
        overlays = new ArrayList<>();
        focusIndex = 0;
    }
    
    @Override
    public void update(){
        if(game.towerSelected == null && isClicked(0))
            game.selectTower(this);
        
        if(isSelected()){
            if(isPlaced || canBePlaced())
                renderDetails();
            renderOverlay();
        }  
        
        if(!isPlaced)
            renderPrevisu();
        else{
            super.update();
            super.render();
        }
    }
    
    private void renderPrevisu(){
        if(canBePlaced()){
            float xPos = Math.floorDiv(Mouse.getX(), unite)*unite, yPos = Math.floorDiv(RvB.windHeight-Mouse.getY(), unite)*unite;
            for(int i = 0 ; i < textures.size() ; i++)
                RvB.drawFilledRectangle(xPos+unite/2, yPos+unite/2, size, size, textures.get(i), i == rotateIndex ? angle : 0, 0.5f);
        }
        x = Mouse.getX();
        y = RvB.windHeight-Mouse.getY();
    }
    
    public void renderDetails(){
        int xPos = (int)x, yPos = (int)y;
        if(!isPlaced){
            xPos = Math.floorDiv(xPos, unite)*unite+unite/2;
            yPos = Math.floorDiv(yPos, unite)*unite+unite/2;
        }
        RvB.drawCircle(xPos, yPos, getRange(), RvB.colors.get("blue"));
        RvB.drawCircle(xPos, yPos, getRange()-1, RvB.colors.get("grey"));
        RvB.drawCircle(xPos, yPos, getRange()-2, RvB.colors.get("grey_light"));
        RvB.drawFilledCircle(xPos, yPos, getRange()-2, RvB.colors.get("grey_light"), 0.1f);
    }
    
    public void initOverlay(){
        Overlay o1, o2;
        
        o1 = new Overlay(0, RvB.windHeight-(int)((60+30)*ref), (int)(140*ref), (int)(30*ref));
        o1.setBG(RvB.textures.get("board"), 0.6f);
        overlays.add(o1);
        
        o2 = new Overlay(0, RvB.windHeight-(int)(60*ref), RvB.windWidth, (int)(60*ref));
        o2.setBG(RvB.textures.get("board"), 0.6f);
        
        int sep = (int) (700 * ref);
        sep -= (int)(ref*90*upgrades.size());
        if(sep < 25)
            sep = 25;
        int imageSize = o2.getH()-(int)(5*ref);
        int marginToCenter = RvB.windWidth-o1.getW()-((upgrades.size()-1)*sep + (upgrades.size()-1));
        marginToCenter = marginToCenter/2;
        if(marginToCenter < 0)
            marginToCenter = 0;
        o2.addImage(o1.getW()/2, o2.getH()/2, imageSize, imageSize, textureStatic);
        Button b;
        // init upgrades position
        for(int i = 0 ; i < upgrades.size() ; i++){
            upgrades.get(i).initPosAndButton(o1.getW() + marginToCenter + i*sep, o2.getY() + o2.getH()/2, this);
        }
        // button sell
        b = new Button(o1.getW()+(int)(60*ref), o2.getH()/2, (int)(80*ref), (int)(28*ref), RvB.colors.get("green_semidark"), RvB.colors.get("green_dark"));
        b.setClickSound(SoundManager.Instance.getClip("sell"), SoundManager.Volume.MEDIUM);
        b.setFunction(__ -> {
            if(!isPlaced)
                return;
            game.money += (int)(totalMoneySpent/2);
            Tile grass = new Tile(RvB.textures.get("grass"), "grass");
            grass.setRotateIndex(0);
            grass.setX(x);
            grass.setY(y);
            game.map.get(getIndexY()).set(getIndexX(), grass);
            if(game.towerSelected == this)
                game.selectTower(null);
            if(clip != null){
                SoundManager.Instance.stopClip(clip);
                SoundManager.Instance.clipToClose(clip);
            }     
            game.towersDestroyed.add(this);
        });
        o2.addButton(b);
        // button focus
        if(rotateIndex >= 0){
            b = new Button(o2.getW()-(int)(140*ref), o2.getH()-(int)(20*ref), (int)(120*ref), (int)(32*ref), TextManager.Text.FOCUS_SWITCH, RvB.fonts.get("normal"), RvB.colors.get("green_semidark"), RvB.colors.get("green_dark"), 0);
            b.setSwitch();
            focusButton = b;
            focusButton.setFunction(__ -> {
                focusIndex = focusButton.indexSwitch;
            });
            o2.addButton(focusButton);
        }
        overlays.add(o2);
    }
    
    public void renderOverlay(){
        Button b;
        Overlay overlay;
        
        for(Overlay o : overlays)
            o.render();
        
        overlay = overlays.get(0);
        overlay.drawText(overlay.getW()/2, overlay.getH()/2, name.getText(), RvB.fonts.get("normalL"));
        
        overlay = overlays.get(1);
        for(Upgrade up : upgrades)
            up.render();
        String price;
        
        b = overlay.getButtons().get(0);
        if(b.isHovered()){
            price = "+ "+(int)(totalMoneySpent/2);
            b.drawText(price, RvB.fonts.get("canBuy"));
        }
        else
            b.drawText(Text.SELL.getText(), RvB.fonts.get("normal"));
        if(rotateIndex >= 0){
            b = overlay.getButtons().get(1);
            overlay.drawText(b.getX(), b.getY()-overlay.getY()-(int)(30*ref), Text.FOCUS.getText(), RvB.fonts.get("normal"));
        }
    }
    
    public void place(ArrayList<ArrayList<Tile>> map){
        x = Math.floorDiv(Mouse.getX(), unite);
        y = Math.floorDiv(RvB.windHeight-Mouse.getY(), unite);
        map.get((int) y).set((int) x, null);
        x = x*unite+unite/2;
        y = y*unite+unite/2;
        game.money -= price;
        game.raisePrice(this);
        isPlaced = true;
        started = true;
        SoundManager.Instance.playOnce(SoundManager.SOUND_BUILD);
    }
    
    @Override
    public void die(){
        super.die();
        game.map.get((int) y).set((int) x, new Tile("grass"));
        game.towersDestroyed.add(this);
    }
    
    public boolean canBePlaced(){
        if(!isInWindow())
            return false;
        Tile tile = game.map.get(Math.floorDiv((int)y, unite)).get(Math.floorDiv((int) x, unite));
        if(tile == null)
            return false;
        if(tile.getType() == "grass")
            return true;
        return false;
    }
    
    @Override
    public boolean isInRangeOf(Shootable s){
        double angle, cosinus, sinus;
        if(isPlaced){
            angle = MyMath.angleBetween(this, (Shootable) s);
            cosinus = Math.floor(Math.cos(angle)*1000)/1000;
            sinus = Math.floor(Math.sin(angle)*1000)/1000;
            return (x <= s.getX()+((s.getRange())*Math.abs(cosinus)) && x >= s.getX()-((s.getRange())*Math.abs(cosinus)) && y <= s.getY()+((s.getRange())*Math.abs(sinus)) && y >= s.getY()-((s.getRange())*Math.abs(sinus)));
        }
        float x = Math.floorDiv((int)this.x, unite)*unite+unite/2, y = Math.floorDiv((int)this.y, unite)*unite+unite/2;
        angle = MyMath.angleBetween(x, y, s.getX(), s.getY());
        cosinus = Math.floor(Math.cos(angle)*1000)/1000;
        sinus = Math.floor(Math.sin(angle)*1000)/1000;
        return (x <= s.getX()+((s.getRange())*Math.abs(cosinus)) && x >= s.getX()-((s.getRange())*Math.abs(cosinus)) && y <= s.getY()+((s.getRange())*Math.abs(sinus)) && y >= s.getY()-((s.getRange())*Math.abs(sinus)));
    }
    
    @Override
    public ArrayList<Shootable> getEnemies(){
        return game.enemies;
    }
    
    public boolean isForBuff(){
        return forBuff;
    }
    
    public boolean isPlaced(){
        return isPlaced;
    }
    
    public void setIsPlaced(boolean b){
        isPlaced = b;
    }
    
    public ArrayList<Upgrade> getUpgrades(){
        return upgrades;
    }
    
    public int getPrice(){
        return price;
    }
}
