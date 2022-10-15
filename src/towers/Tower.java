package towers;

import Utils.MyMath;
import rvb.Tile;
import ennemies.Enemy;
import java.util.ArrayList;
import java.util.Random;
import javax.sound.sampled.Clip;
import org.lwjgl.input.Mouse;
import org.newdawn.slick.opengl.Texture;
import rvb.RvB;
import rvb.Shootable;
import managers.SoundManager;
import static rvb.RvB.game;
import static rvb.RvB.ref;
import static rvb.RvB.unite;
import ui.*;

public abstract class Tower implements Shootable{
    
    protected int price, range, power, bulletSpeed, life, width, totalMoneySpent, explodeRadius, bulletSizeBonus;
    protected double lastShoot = 0;
    protected float shootRate, growth = 0;
    protected String name;
    protected SoundManager.Volume volume = SoundManager.Volume.SEMI_LOW;
    protected boolean isPlaced = false, follow = false, selected = true, isMultipleShot, canRotate, toBeRemoved, continuousSound = false, soundPlayed = false, explode = false;
    protected Enemy enemyAimed;
    protected ArrayList<Bullet> bullets = new ArrayList<>(), bulletsToRemove = new ArrayList<>();
    protected ArrayList<Shootable> enemiesTouched = new ArrayList<>();
    protected ArrayList<Overlay> overlays;
    protected ArrayList<Upgrade> upgrades;
    protected Texture textureStatic, bulletSprite;
    protected Clip clip;
    protected Random random = new Random();
    protected float x, y;
    protected int size = unite;
    protected int angle = 0, newAngle = 0;
    protected ArrayList<Texture> textures;
    protected int rotateIndex = -1;
    public String type;
    protected boolean mouseEntered = false;
    
    public Tower(String type){
        textures = new ArrayList<>();
        this.type = type;
        this.x = Mouse.getX();
        this.y = RvB.windHeight-Mouse.getY();
        upgrades = new ArrayList<>();
        overlays = new ArrayList<>();
    }
    
    public void update(){
        if(selected && (isPlaced || canBePlaced()))
            renderDetails();
        
        if(!isPlaced)
            renderPrevisu();
        else{
            render();
            if(selected)
                checkOverlayInput();
            searchAndShoot();
            updateBullets();
            if(soundPlayed && enemyAimed == null){
                SoundManager.Instance.stopClip(clip);
                soundPlayed = false;
            }   
            // Mouse hover
            if(isMouseIn() && !mouseEntered && RvB.cursor != RvB.Cursor.GRAB){
                RvB.setCursor(RvB.Cursor.POINTER);
                mouseEntered = true;
            }
            else if(!isMouseIn() && mouseEntered && RvB.cursor != RvB.Cursor.GRAB){
                RvB.setCursor(RvB.Cursor.DEFAULT);
                mouseEntered = false;
            } 
        }
    }
    
    private void checkOverlayInput(){
        Button b;
        float upPrice;
        int i;
        for(i = 0 ; i < upgrades.size() ; i++){
            b = overlays.get(1).getButtons().get(i);
            if(b.isClicked(0)){
                upPrice = upgrades.get(i).price;
                if(game.money < upPrice)
                    continue;
                switch(upgrades.get(i).name){
                    case "Range":
                        range = (int) upgrades.get(i).setNewValue();
                        break;
                    case "Power":
                        power = (int) upgrades.get(i).setNewValue();
                        break;
                    case "Attack speed":
                        shootRate = upgrades.get(i).setNewValue();
                        break;
                    case "Bullet speed":
                        bulletSpeed = (int) upgrades.get(i).setNewValue();
                        break;
                    case "Explode radius":
                        explodeRadius = (int) upgrades.get(i).setNewValue();
                        break;
                }
                size += growth;
                game.money -= upPrice;
                totalMoneySpent += upPrice;
                upgrades.get(i).increasePrice();
                b.click();
            }
        }
        if(overlays.get(1).getButtons().get(i).isClicked(0)){ // Sell button
            game.money += (int)(totalMoneySpent/2);
            Tile grass = new Tile(RvB.textures.get("grass"), "grass");
            grass.setRotateIndex(0);
            grass.setX(x);
            grass.setY(y);
            game.map.get(getIndexY()).set(getIndexX(), grass);
            if(game.towerSelected == this)
                game.towerSelected = null;
            if(clip != null){
                SoundManager.Instance.stopClip(clip);
                SoundManager.Instance.clipToClose(clip);
            }     
            toBeRemoved = true;
        }
    }
    
    public void searchAndShoot(){
        ArrayList<Enemy> enemies = game.enemies;
        Enemy first = null;
        if(enemies != null && !enemies.isEmpty()){
            for(int i = 0 ; i < enemies.size() ; i++)
                if(enemies.get(i).isSpawned() && enemies.get(i).isInRangeOf(this)){
                    if(first == null || enemies.get(i).getIndiceTuile() > first.getIndiceTuile() || (enemies.get(i).getIndiceTuile() == first.getIndiceTuile() && enemies.get(i).getMoveSpeed() > first.getMoveSpeed()))
                        first = enemies.get(i);
                }
            aim(first);
        }
        else
            aim(null);
        if(enemyAimed != null && enemyAimed.isDead())
            enemyAimed = null;
        if(enemyAimed != null && enemyAimed.isInRangeOf(this) && canShoot())
            shoot();
    }
    
    public void aim(Enemy e){
        if(e == null && enemyAimed != null)
            enemyAimed.setIsAimed(false);
        enemyAimed = e;
        if(e != null){
            e.setIsAimed(true);
            if(canRotate){
                float t = 0.3f;
                newAngle = (int) MyMath.angleDegreesBetween((Shootable)this, enemyAimed);
                
                if(newAngle-angle > 180)
                    newAngle -= 360;
                else if(angle-newAngle > 180)
                    newAngle += 360;
                if(Math.abs(angle-newAngle) <= 5)
                    t = 1;
                
                angle = (int) ((1-t)*angle + t*newAngle);
                
                if(angle >= 360)
                    angle -= 360;
                else if(angle <= -360)
                    angle += 360;
                
                angle = (int)Math.round(angle);
                newAngle = (int)Math.round(newAngle);
            }
        }   
    }
    
    public void render(){
        for(int i = 0 ; i < textures.size() ; i++){
            RvB.drawFilledRectangle(x, y, size, size, textures.get(i), i == rotateIndex ? angle : 0);
        }
    }
    
    private void renderPrevisu(){
        if(canBePlaced()){
            float xPos = Math.floorDiv(Mouse.getX(), unite)*unite, yPos = Math.floorDiv(RvB.windHeight-Mouse.getY(), unite)*unite;
            RvB.drawFilledRectangle(xPos+unite/2-size/2, yPos+unite/2-size/2, size, size, null, 0.5f, textureStatic);
        }
        x = Mouse.getX();
        y = RvB.windHeight-Mouse.getY();
    }
    
    public void renderDetails(){
        int xPos = (int)x, yPos = (int)y;
        if(!isPlaced){
            xPos = Math.floorDiv(Mouse.getX(), unite)*unite+unite/2;
            yPos = Math.floorDiv(RvB.windHeight-Mouse.getY(), unite)*unite+unite/2;
        }
        RvB.drawCircle(xPos, yPos, range, RvB.colors.get("blue"));
        RvB.drawCircle(xPos, yPos, range-1, RvB.colors.get("grey"));
        RvB.drawCircle(xPos, yPos, range-2, RvB.colors.get("grey_light"));
        RvB.drawFilledCircle(xPos, yPos, range-2, RvB.colors.get("grey_light"), 0.1f);
        if(isPlaced)
            renderOverlay();
    }
    
    public void initOverlay(){
        Overlay o1, o2;
        
        o1 = new Overlay(0, RvB.windHeight-(int)((60+30)*ref), (int)(140*ref), (int)(30*ref));
        o1.setBG(RvB.textures.get("board"), 0.6f);
        overlays.add(o1);
        
        o2 = new Overlay(0, RvB.windHeight-(int)(60*ref), RvB.windWidth, (int)(60*ref));
        o2.setBG(RvB.textures.get("board"), 0.6f);
        
        int sep = (int) (700 * ref);
        sep -= 90*upgrades.size();
        if(sep < 25)
            sep = 25;
        int imageSize = o2.getH()-(int)(5*ref);
        int butWidth = (int) (32*ref), butHeight = (int)(32*ref);
        int marginToCenter = RvB.windWidth-o1.getW()-((upgrades.size()-1)*sep + (upgrades.size()-1)*butWidth + butWidth/2);
        marginToCenter = marginToCenter/2;
        if(marginToCenter < 0)
            marginToCenter = 0;
        o2.addImage(o1.getW()/2, o2.getH()/2, imageSize, imageSize, textureStatic);
        for(int i = 0 ; i < upgrades.size() ; i++){
            Button b = new Button(o1.getW() + marginToCenter + i*sep + i*butWidth + butWidth/2, o2.getH()/3, butWidth, butHeight, RvB.colors.get("green_semidark"), RvB.colors.get("green_dark"), upgrades.get(i).maxClick);
            b.setBG(RvB.textures.get("plus"));
            if(upgrades.get(i).maxClick <= 0)
                b.setHidden(true);
            b.setClickSound(SoundManager.Instance.getClip("upgrade"), SoundManager.Volume.SEMI_HIGH);
            o2.addButton(b);
        }
        Button b = new Button(o1.getW()+(int)(60*ref), o2.getH()/2, (int)(80*ref), (int)(28*ref), RvB.colors.get("green_semidark"), RvB.colors.get("green_dark"));
        b.setClickSound(SoundManager.Instance.getClip("sell"), SoundManager.Volume.MEDIUM);
        o2.addButton(b);
        overlays.add(o2);
    }
    
    public void renderOverlay(){
        float upPrice;
        Button b;
        Overlay overlay;
        
        for(Overlay o : overlays)
            o.render();
        
        overlay = overlays.get(0);
        overlay.drawText(overlay.getW()/2, overlay.getH()/2, name, RvB.fonts.get("normalL"));
        
        overlay = overlays.get(1);
        int i;
        String price, up, nextUp;
        for(i = 0 ; i < upgrades.size() ; i++){
            b = overlay.getButtons().get(i);
            upPrice = upgrades.get(i).price;
            if(upgrades.get(i).nbNumberToRound == 0){
                up = (int)upgrades.get(i).getValue()+"";
                nextUp = (int)(upgrades.get(i).getValue()+upgrades.get(i).getIncreaseValue())+"";
            }
                
            else{
                up = upgrades.get(i).getValue()+"";
                nextUp = (upgrades.get(i).getValue()+upgrades.get(i).getIncreaseValue())+"";
            }
                
            if(!b.isHidden()){
                overlay.drawImage(b.getX()-(int)(90*ref)-(int)(16*ref), overlay.getH()/3-(int)(16*ref), (int)(32*ref), (int)(32*ref), upgrades.get(i).icon);
                if(b.isHovered())
                    overlay.drawText(b.getX()-(int)(45*ref), overlay.getH()/3, nextUp, RvB.fonts.get("bonus"));
                else   
                    overlay.drawText(b.getX()-(int)(45*ref), overlay.getH()/3, up, RvB.fonts.get("normal"));
                if(game.money >= (int)Math.floor(upPrice))
                    overlay.drawText(b.getX()-(int)(45*ref), 2*overlay.getH()/3+(int)(5*ref), (int)Math.floor(upPrice)+"", RvB.fonts.get("canBuy"));
                else
                    overlay.drawText(b.getX()-(int)(45*ref), 2*overlay.getH()/3+(int)(5*ref), (int)Math.floor(upPrice)+"", RvB.fonts.get("cantBuy"));
            }
            else{
                overlay.drawImage(b.getX()-(int)(90*ref)-(int)(16*ref), overlay.getH()/2-(int)(16*ref), (int)(32*ref), (int)(32*ref), upgrades.get(i).icon);   
                overlay.drawText(b.getX()-(int)(45*ref), overlay.getH()/2, up, RvB.fonts.get("normal"));   
            }
        }
        b = overlay.getButtons().get(overlay.getButtons().size()-1);
        if(b.isHovered()){
            price = "+ "+(int)(totalMoneySpent/2);
            b.drawText(price, RvB.fonts.get("canBuy"));
        }
        else
            b.drawText("Sell", RvB.fonts.get("normal"));
        
    }
    
    public void updateBullets(){
        for(Bullet b : bulletsToRemove)
            bullets.remove(b);

        bulletsToRemove.clear();
        
        for(Bullet b : bullets)
            b.update();
    }
    
    public void place(ArrayList<ArrayList<Tile>> map){
        initOverlay();
        x = Math.floorDiv(Mouse.getX(), unite);
        y = Math.floorDiv(RvB.windHeight-Mouse.getY(), unite);
        map.get((int) y).set((int) x, null);
        x = x*unite+unite/2;
        y = y*unite+unite/2;
        game.money -= price;
        raisePrice();
        isPlaced = true;
        SoundManager.Instance.playOnce(SoundManager.SOUND_BUILD);
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
    
    public boolean isPlaced(){
        return isPlaced;
    }

    private boolean isInWindow() {
        return (x < RvB.windWidth && x > 0 && y < RvB.windHeight && y > 0);
    }
    
    public void destroy(){
        game.towersDestroyed.add(this);
    }
   
    public boolean isMouseIn(){
        int MX = Mouse.getX(), MY = RvB.windHeight-Mouse.getY();
        return (MX >= x-unite/2 && MX <= x+unite/2 && MY >= y-unite/2 && MY <= y+unite/2);
    }
    
    public boolean isClicked(int but){
        return (isMouseIn() && Mouse.isButtonDown(but));
    }
    
    public void die(){
        destroy();
    }
    
    public boolean isSoundContinuous(){
        return continuousSound;
    }
    
    public Clip getClip(){
        return clip;
    }
    
    public float getX(){
        return x;
    }
    
    public float getY(){
        return y;
    }
    
    public void setSelected(boolean b){
        selected = b;
    }
    
    public boolean isSelected(){
        return selected;
    }
    
    public boolean toRemove(){
        return toBeRemoved;
    }
    
    @Override
    public int getWidth(){
        return width;
    }
    
    public void setFollow(boolean b){
        follow = b;
    }
    
    @Override
    public int getExplodeRadius(){
        return explodeRadius;
    }
    
    @Override
    public boolean getExplode(){
        return explode;
    }
    
    @Override
    public boolean getFollow(){
        return follow;
    }
    
    public boolean canShoot(){
        return (System.currentTimeMillis()-lastShoot >= 1000/(shootRate*game.gameSpeed) && angle >= newAngle-6 && angle <= newAngle+6);
    }
    
    public void shoot(){
        enemiesTouched.clear();
        lastShoot = System.currentTimeMillis();
        float x = (float)(this.x+size*Math.cos(Math.toRadians(angle))/2);
        float y = (float)(this.y+size*Math.sin(Math.toRadians(angle))/2);
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
    
    public Enemy getEnemyAimed(){
        return enemyAimed;
    }
    
    public ArrayList<Shootable> getEnemiesTouched(){
        return enemiesTouched;
    }
    
    @Override
    public boolean isDead(){
        return (life <= 0);
    }
    
    @Override
    public int getBulletSpeed(){
        return bulletSpeed;
    }
    
    public String getName(){
        return name;
    }
    
    public int getPrice(){
        return price;
    }
    
    public int getIndexX(){
        return (int)(x/unite);
    }
    
    public int getIndexY(){   
        return (int)(y/unite);
    }
    
    @Override
    public int getPower(){
        return power;
    }
    
    @Override
    public boolean isMultipleShot(){
        return isMultipleShot;
    }
    
    public float getShootRate(){
        return shootRate;
    }
    
    public int getLife(){
        return life;
    }
    
    @Override
    public int getRange(){
        return range;
    }
    
    @Override
    public void attacked(int power){
        this.life -= power;
        if(life <= 0)
            die();
    }
    
    @Override
    public ArrayList<Bullet> getBullets(){
        return bullets;
    }
    
    @Override
    public ArrayList<Bullet> getBulletsToRemove(){
        return bulletsToRemove;
    }
    
    protected void raisePrice(){
        
    }
}
