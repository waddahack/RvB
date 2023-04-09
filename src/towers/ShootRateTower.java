package towers;

import Utils.MyMath;
import java.util.ArrayList;
import rvb.RvB;
import managers.TextManager.Text;
import static rvb.RvB.game;
import static rvb.RvB.ref;
import static rvb.RvB.unite;
import rvb.Shootable;
import ui.Overlay;

public class ShootRateTower extends Tower{
    
    public ArrayList<Tower> towers;
    
    public ShootRateTower() {
        super("ShootRateTower");
        textures.add(RvB.textures.get("shootrateTowerBase"));
        textures.add(RvB.textures.get("shootrateTowerBullet"));
        rotateIndex = 1;
        textureStatic = RvB.textures.get("shootrateTower");
        canShoot = false;
        life = 100f;
        size = 4*RvB.unite/5;
        hitboxWidth = size;
        totalMoneySpent = price;
        name = Text.TOWER_SHOOTRATE;
        forBuff = true;
        
        towers = new ArrayList<>();
        power = 0.1f;
        range = 3*RvB.unite;
        growth = 6f*ref;
        
        initBack();
        initOverlay();
    }
    
    @Override
    public void update(){
        super.update();
        if(game.gameLoaded || (game.towerSelected != null && !game.towerSelected.isPlaced())){
            for(Shootable t : game.towers){
                if(MyMath.distanceBetween(getIndexX()*unite+unite/2, getIndexY()*unite+unite/2, t.getIndexX()*unite+unite/2, t.getIndexY()*unite+unite/2) <= range && !towers.contains(t) && t != this){
                    Tower to = (Tower) t;
                    to.bonusShootRate += power;
                    to.underShootRateTower = true;
                    to.updateBoosts(false, false, selected);
                    towers.add(to);
                }
                else if(!(MyMath.distanceBetween(getIndexX()*unite+unite/2, getIndexY()*unite+unite/2, t.getIndexX()*unite+unite/2, t.getIndexY()*unite+unite/2) <= range) && towers.contains(t)){
                    Tower to = (Tower) t;
                    to.bonusShootRate -= power;
                    to.underShootRateTower = false;
                    to.updateBoosts(false, false, selected);
                    towers.remove((Tower)t);
                }
            }
        }
        
        angle += game.gameSpeed*RvB.deltaTime/8;
    }
    
    @Override
    public void setSelected(boolean selected){
        super.setSelected(selected);
        for(Tower t : towers)
            t.updateBoosts(false, false, selected);
    }
    
    @Override
    public void initOverlay(){
        Overlay o1, o2;
        
        o1 = new Overlay(0, RvB.windHeight-(int)((60+30)*ref), (int)(200*ref), (int)(30*ref));
        o1.setBG(RvB.textures.get("board"), 0.6f);
        overlays.add(o1);
        
        o2 = new Overlay(0, RvB.windHeight-(int)(60*ref), RvB.windWidth, (int)(60*ref));
        int imageSize = o2.getH()-(int)(5*ref);
        o2.setBG(RvB.textures.get("board"), 0.6f);
        o2.addImage(o1.getW()/2, o2.getH()/2, imageSize, imageSize, textureStatic);
        o2.addImage(o2.getW()/2, o2.getH()/2, (int)(32*ref), (int)(32*ref), RvB.textures.get("attackSpeedIcon"));
        overlays.add(o2);
    }
    
    @Override
    public void renderOverlay(){
        Overlay overlay;
        
        for(Overlay o : overlays)
            o.render();
        
        overlay = overlays.get(0);
        overlay.drawText(overlay.getW()/2, overlay.getH()/2, name.getText(), RvB.fonts.get("normalL"));
        
        overlay = overlays.get(1);
        overlay.drawText(overlay.getW()/2+(int)(20*ref), overlay.getH()/2, "+"+(int)(power*100)+"%", RvB.fonts.get("normal"), "midLeft");
    }
}