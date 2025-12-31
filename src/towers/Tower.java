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
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonValue;
import ennemies.Enemy;
import java.io.Serializable;
import javax.sound.sampled.Clip;
import managers.PopupManager;
import managers.StatsManager;
import towers.Tower.Type;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = Raztech.class, name = "RAZTECH"),
    @JsonSubTypes.Type(value = PowerTower.class, name = "POWER"),
    @JsonSubTypes.Type(value = RangeTower.class, name = "RANGE"),
    @JsonSubTypes.Type(value = ShootRateTower.class, name = "SHOOTRATE"),
    @JsonSubTypes.Type(value = BasicTower.class, name = "BASIC"),
    @JsonSubTypes.Type(value = CircleTower.class, name = "CIRCLE"),
    @JsonSubTypes.Type(value = BigTower.class, name = "BIG"),
    @JsonSubTypes.Type(value = FlameTower.class, name = "FLAME")
})
public abstract class Tower extends Shootable implements Serializable{
    
    public static enum Type{
        BASIC("BASIC"), CIRCLE("CIRCLE"), BIG("BIG"), FLAME("FLAME"), POWER("POWER"), RANGE("RANGE"), SHOOTRATE("SHOOTRATE"), RAZTECH("RAZTECH");

        private final String name;

        Type(String name) {
            this.name = name;
        }

        @JsonValue
        public String getName() {
            return name;
        }

        @JsonCreator
        public static Type fromName(String name) {
            for (Type type : Type.values()) {
                if (type.name.equalsIgnoreCase(name)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Invalid name: " + name);
        }
    }
    
    public Type type;
    public int price, totalMoneySpent;
    protected int hitboxWidth;
    public float growth = 0;
    protected boolean isPlaced = false, forBuff = false, canFocus = true;
    public boolean underPowerTower = false, underRangeTower = false, underShootRateTower = false;
    protected ArrayList<Overlay> overlays;
    protected ArrayList<Upgrade> upgrades;
    protected Button focusButton;
    public int[] nbUpgradesUsed;
    public int indexXSaved, indexYSaved;
    private Clip destroyedClip;
    private int repairPrice = 1; // for each life point, changer dans appcore -> priceToRepairAll
    
    public Tower(Type type){
        super();
        this.type = type;
        this.x = Mouse.getX();
        this.y = RvB.windHeight-Mouse.getY();
        upgrades = new ArrayList<>();
        overlays = new ArrayList<>();
        focusIndex = 0;
        isTower = true;
        destroyedClip = SoundManager.Instance.getClip("destroyed");
        SoundManager.Instance.setClipVolume(destroyedClip, SoundManager.Volume.MEDIUM);
    }
    
    @Override
    public void update(){
        if(game.towerSelected == null && isClicked(0) && !PopupManager.Instance.onPopup())
            game.selectTower(this);
        
        if(isSelected()){
            if(isPlaced || canBePlaced())
                renderDetails();
            renderOverlay();
        }  
        
        if(!isPlaced){
            updateBoosts(false, false, false);
            renderPrevisu();
        }
        else{
            super.update();
            
            render();
        }
    }
    
    protected void renderPrevisu(){
        float xPos = Math.floorDiv(Mouse.getX(), unite)*unite+unite/2, yPos = Math.floorDiv(RvB.windHeight-Mouse.getY(), unite)*unite+unite/2;
        for(int i = 0 ; i < textures.size() ; i++)
            RvB.drawFilledRectangle(xPos, yPos, size, size, textures.get(i), i == rotateIndexShoot ? (int)angle : 0, 0.5f);
        for(int i = 0 ; i < texturesAdditive.size() ; i++)
            RvB.drawFilledRectangle(xPos-3*unite/10, yPos-unite/2, 3*unite/5, 3*unite/5, null, 1, texturesAdditive.get(i));
        if(!canBePlaced()){
            RvB.drawFilledRectangle(xPos-size/2, yPos-size/2, size, size, null, 1f, RvB.textures.get("cross"));
        }
        x = Mouse.getX();
        y = RvB.windHeight-Mouse.getY();
    }
    
    @Override
    public void renderLifeBar(){
        if(life >= maxLife)
            return;
        int width = (int) (40*ref), height = (int) (6*ref);
        int currentLife = (int) (((double)life/(double)maxLife)*width);
        float[] bgColor = RvB.colors.get("lightRed");
        RvB.drawFilledRectangle(x-width/2, y-size/2-height/2, width, height, bgColor, 1, null);
        RvB.drawFilledRectangle(x-width/2, y-size/2-height/2, currentLife, height, RvB.colors.get("life"), 1, null);
        RvB.drawRectangle((int)(x-width/2), (int) (y-size/2-height/2), width, height, RvB.colors.get("green_dark"), 1, 2);
    }
    
    public void renderDetails(){
        int xPos = (int)x, yPos = (int)y;
        if(!isPlaced){
            xPos = Math.floorDiv(xPos, unite)*unite+unite/2;
            yPos = Math.floorDiv(yPos, unite)*unite+unite/2;
        }
        RvB.drawFilledCircle(xPos, yPos, getPreRange(), RvB.colors.get("grey_light"), 0.1f);
        RvB.drawCircle(xPos, yPos, getPreRange(), RvB.colors.get("blue"), (int)(2*ref));
        RvB.drawCircle(xPos, yPos, getPreRange()-1, RvB.colors.get("grey"));
        RvB.drawCircle(xPos, yPos, getPreRange()-1.5f, RvB.colors.get("grey_light"));
    }
    
    public void initOverlay(){
        Overlay o1, o2, o3;
        // Name
        o1 = new Overlay(0, RvB.windHeight-(int)((60+30)*ref), (int)(140*ref), (int)(30*ref));
        o1.setBG(RvB.textures.get("board"), 0.6f);
        overlays.add(o1);
        // Overlay
        o2 = new Overlay(0, RvB.windHeight-(int)(60*ref), RvB.windWidth, (int)(60*ref));
        o2.setBG(RvB.textures.get("board"), 0.6f);
        int imageSize = o2.getH()-(int)(5*ref);
        o2.addImage(o1.getW()/2, o2.getH()/2, imageSize, imageSize, textureStatic);
        
        int sep = (int) (700 * ref);
        sep -= (int)(ref*90*upgrades.size());
        if(sep < 25)
            sep = 25;
        int marginToCenter = RvB.windWidth-o1.getW()-((upgrades.size()-1)*sep + (upgrades.size()-1));
        marginToCenter = marginToCenter/2;
        if(marginToCenter < 0)
            marginToCenter = 0;
        Button b;
        // init upgrades position
        for(int i = 0 ; i < upgrades.size() ; i++){
            upgrades.get(i).initPosAndButton(o1.getW() + marginToCenter + i*sep, o2.getY() + o2.getH()/2, this);
        }
        // button sell id 0
        b = new Button(o1.getW()+(int)(60*ref), o2.getH()/2, (int)(80*ref), (int)(28*ref), RvB.colors.get("green_semidark"), RvB.colors.get("green_dark"));
        b.setClickSound(SoundManager.Instance.getClip("sell"), SoundManager.Volume.MEDIUM);
        b.setFunction(__ -> {
            if(!isPlaced)
                return;
            game.money += (int)((totalMoneySpent/2));
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
        if(life > 0){
            // button repair id 1
            b = new Button(o1.getW()+(int)(200*ref), o2.getH()/2, (int)(80*ref), (int)(28*ref), RvB.colors.get("green_semidark"), RvB.colors.get("green_dark"));
            b.setFunction(__ -> {
                if(!isPlaced)
                    return;
                if(life == maxLife)
                    return;
                int price = (int)(maxLife-life)*repairPrice;
                if(price > game.money)
                    return;
                game.money -= price;
                life = maxLife;
                SoundManager.Instance.playOnce(SoundManager.Instance.getClip("repair"));
            });
            o2.addButton(b);
        }
        // button focus id 2
        if(canFocus){
            b = new Button(o2.getW()-(int)(140*ref), o2.getH()-(int)(20*ref), (int)(120*ref), (int)(32*ref), TextManager.Text.FOCUS_SWITCH, RvB.fonts.get("normal"), RvB.colors.get("green_semidark"), RvB.colors.get("green_dark"), 0);
            b.setSwitch();
            focusButton = b;
            focusButton.setFunction(__ -> {
                focusIndex = focusButton.indexSwitch;
            });
            o2.addButton(focusButton);
        }
        overlays.add(o2);
        
        // Stats
        o3 = new Overlay(o2.getX(), o2.getY(), o2.getW(), o2.getH());
        o3.setBG(RvB.textures.get("board"), 0.6f);
        o3.addImage(o1.getW()/2, o3.getH()/2, imageSize, imageSize, textureStatic);
        o3.display(false);
        overlays.add(o3);
    }
    
    public void renderOverlay(){
        Button b;
        Overlay overlay;
        
        for(Overlay o : overlays)
            o.render();
        
        overlay = overlays.get(0);
        overlay.drawText(overlay.getW()/2, overlay.getH()/2, name.getText(), RvB.fonts.get("normalL"));
        
        overlay = overlays.get(1);
        if(overlay.isDisplayed()){
            for(Upgrade up : upgrades)
                up.render();
            String price;
            
            // button sell
            b = overlay.getButtons().get(0);
            if(b.isHovered()){
                price = "+ "+(int)((totalMoneySpent/2));
                b.drawText(price, RvB.fonts.get("canBuy"));
            }
            else
                b.drawText(Text.SELL.getText(), RvB.fonts.get("normal"));
            if(life > 0){
                // button repair
                b = overlay.getButtons().get(1);
                if(b.isHovered()){
                    int totalRP = (int)(maxLife-life)*repairPrice;
                    if(totalRP <= game.money){
                        b.drawText(totalRP+"   ", RvB.fonts.get("canBuy"));
                        RvB.drawFilledRectangle(b.getX()+(int)(22*ref), b.getY(), (int)(28*ref), (int)(28*ref), RvB.textures.get("coins"), 0, 1);
                    }
                    else{
                        b.drawText(totalRP+"   ", RvB.fonts.get("cantBuy"));
                        RvB.drawFilledRectangle(b.getX()+(int)(22*ref), b.getY(), (int)(28*ref), (int)(28*ref), RvB.textures.get("coinsCantBuy"), 0, 1);
                    }    
                }
                else
                    b.drawText(Text.REPAIR.getText(), RvB.fonts.get("normal"));
            }
            if(canFocus){
                // button focus
                b = overlay.getButtons().get(2);
                overlay.drawText(b.getX(), b.getY()-overlay.getY()-(int)(30*ref), Text.FOCUS.getText(), RvB.fonts.get("normal"));
            }
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
    
    public void switchOverlay(){
        if(overlays.size() < 3)
            return;
        Overlay upgrades = overlays.get(1), stats = overlays.get(2);
        upgrades.display(!upgrades.isDisplayed());
        stats.display(!stats.isDisplayed());
    }
    
    @Override
    public void attack(Shootable shootable){
        super.attack(shootable);
        Enemy e = (Enemy) shootable;
        if(e.isDead())
            StatsManager.Instance.updateEnemyKilled(e.type);
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
        StatsManager.Instance.updateTowerPlaced(type);
    }
    
    public void autoPlace(ArrayList<ArrayList<Tile>> map){
        x = Math.floorDiv((int)x, unite);
        y = Math.floorDiv((int)y, unite);
        map.get((int) y).set((int) x, null);
        x = x*unite+unite/2;
        y = y*unite+unite/2;
        game.raisePrice(this);
        isPlaced = true;
        started = true;
    }
    
    @Override
    public void die(){
        super.die();
        Tile hole = new Tile(RvB.textures.get("grassHole"), "hole", getIndexX()*RvB.unite, getIndexY()*RvB.unite);
        game.map.get((int) getIndexY()).set((int) getIndexX(), hole);
        game.towersDestroyed.add(this);
        if(game.towerSelected == this)
            game.towerSelected = null;
        game.decreasePrice(this);
        SoundManager.Instance.playOnce(destroyedClip);
    }
    
    public boolean canBePlaced(){
        if(!isInWindow())
            return false;
        Tile tile = game.map.get(Math.floorDiv((int)y, unite)).get(Math.floorDiv((int) x, unite));
        if(tile == null)
            return false;
        if(tile.getType() == "grass" || tile.getType() == "hole")
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
    
    public void updateBoosts(boolean powerTowerSelected, boolean rangeTowerSelected, boolean ASTowerSelected){
        if((selected || powerTowerSelected) && underPowerTower && !texturesAdditive.contains(RvB.textures.get("powerUp")))
            texturesAdditive.add(RvB.textures.get("powerUp"));
        else if(!((selected || powerTowerSelected) && underPowerTower) && texturesAdditive.contains(RvB.textures.get("powerUp")))
            texturesAdditive.remove(RvB.textures.get("powerUp"));
        if((selected || rangeTowerSelected) && underRangeTower && !texturesAdditive.contains(RvB.textures.get("rangeUp")))
            texturesAdditive.add(RvB.textures.get("rangeUp"));
        else if(!((selected || rangeTowerSelected) && underRangeTower) && texturesAdditive.contains(RvB.textures.get("rangeUp")))
            texturesAdditive.remove(RvB.textures.get("rangeUp"));
        if((selected || ASTowerSelected) && underShootRateTower && !texturesAdditive.contains(RvB.textures.get("shootRateUp")))
            texturesAdditive.add(RvB.textures.get("shootRateUp"));
        else if(!((selected || ASTowerSelected) && underShootRateTower) && texturesAdditive.contains(RvB.textures.get("shootRateUp")))
            texturesAdditive.remove(RvB.textures.get("shootRateUp"));
    }
    
    @Override
    public void setSelected(boolean selected){
        super.setSelected(selected);
        updateBoosts(false, false, false);
        if(!selected && (overlays.size() < 3 || overlays.get(2).isDisplayed()))
            switchOverlay();
    }
    
    public int getPreRange(){
        if(upgrades.size() == 0)
            return getRange();
        int i = 0;
        Upgrade up = upgrades.get(i);
        while(up.name != "Range" && i < upgrades.size()-1)
            up = upgrades.get(++i);
        if(up.name != "Range" || !up.button.isHovered())
            return getRange();
        return (int) up.getIncreasedValueWithBonus();
    }
    
    public void disableAllButtons(){
        for(Upgrade up : upgrades)
            up.button.disable();
        for(Button b : overlays.get(1).getButtons())
            b.disable();
    }
    
    public void enableAllButtons(){
        for(Upgrade up : upgrades)
            up.button.enable();
        for(Button b : overlays.get(1).getButtons())
            b.enable();
    }
    
    public void updatePos(){
        x = indexXSaved*unite;
        y = indexYSaved*unite;
    }
    
    public Button getFocusButton(){
        return focusButton;
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
    
    public String getJSON(){
        String nbUpgradesUsed = "[";
        for(Upgrade up : upgrades)
            nbUpgradesUsed += up.button.getNbClicks() + (up == upgrades.get(upgrades.size()-1) ? "" : ", ");
        nbUpgradesUsed += "]";
        return "{"+
                "\"type\":\""+type.name+"\", "+
                "\"indexXSaved\":"+getIndexX()+", "+
                "\"indexYSaved\":"+getIndexY()+", "+
                "\"angle\":"+angle+", "+
                "\"focusIndex\":"+getFocusIndex()+", "+
                "\"enemiesKilled\":"+enemiesKilled+", "+
                "\"damagesDone\":"+damagesDone+", "+
                "\"enemiesKilledThisWave\":"+enemiesKilledThisWave+", "+
                "\"damagesDoneThisWave\":"+damagesDoneThisWave+", "+
                "\"nbUpgradesUsed\":"+nbUpgradesUsed+", "+
                "\"life\":"+life+
               "}";
    }
}
