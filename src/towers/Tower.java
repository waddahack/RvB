package towers;

import Utils.MyMath;
import rvb.Tile;
import ennemies.Enemy;
import java.util.ArrayList;
import java.util.Random;
import javax.sound.sampled.Clip;
import managers.TextManager.Text;
import org.lwjgl.input.Mouse;
import org.newdawn.slick.opengl.Texture;
import rvb.RvB;
import rvb.Shootable;
import managers.SoundManager;
import managers.TextManager;
import static rvb.RvB.game;
import static rvb.RvB.ref;
import static rvb.RvB.unite;
import ui.*;

public abstract class Tower implements Shootable{
    
    protected int price, range, power, bulletSpeed, life, width, hitboxWidth, totalMoneySpent, explodeRadius, bulletSizeBonus;
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
    private Button focusButton;
    
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
            searchAndShoot();
            updateBullets();
            render();
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
    
    public void searchAndShoot(){
        ArrayList<Enemy> enemies = game.enemies;
        if(enemies != null && !enemies.isEmpty()){
            switch(focusButton.indexSwitch){
                case 0: // Dans l'ordre du text (cf TextManager)
                    aim(searchForFirst(enemies));
                    break;
                case 1:
                    aim(searchForLast(enemies));
                    break;
                case 2:
                    aim(searchForStrongest(enemies));
                    break;
                case 3:
                    aim(searchForWeakest(enemies));
                    break;
                case 4:
                    aim(searchForClosest(enemies));
                    break;
                default:
                    aim(null);
                    break;
            }
        }
        else
            aim(null);
        if(enemyAimed != null && enemyAimed.isDead())
            enemyAimed = null;
        if(enemyAimed != null && enemyAimed.isInRangeOf(this) && canShoot())
            shoot();
    }
    
    private Enemy searchForFirst(ArrayList<Enemy> enemies){
        Enemy first = null;
        for(int i = 0 ; i < enemies.size() ; i++)
            if(enemies.get(i).isSpawned() && enemies.get(i).isInRangeOf(this)){
                if(first == null || enemies.get(i).getIndiceTuile() > first.getIndiceTuile() || (enemies.get(i).getIndiceTuile() == first.getIndiceTuile() && enemies.get(i).getMoveSpeed() > first.getMoveSpeed()))
                    first = enemies.get(i);
            }
        return first;
    }
    
    private Enemy searchForLast(ArrayList<Enemy> enemies){
        Enemy last = null;
        for(int i = 0 ; i < enemies.size() ; i++)
            if(enemies.get(i).isSpawned() && enemies.get(i).isInRangeOf(this)){
                if(last == null || enemies.get(i).getIndiceTuile() < last.getIndiceTuile() || (enemies.get(i).getIndiceTuile() == last.getIndiceTuile() && enemies.get(i).getMoveSpeed() < last.getMoveSpeed()))
                    last = enemies.get(i);
            }
        return last;
    }
    
    private Enemy searchForStrongest(ArrayList<Enemy> enemies){
        Enemy strongest = null;
        for(int i = 0 ; i < enemies.size() ; i++)
            if(enemies.get(i).isSpawned() && enemies.get(i).isInRangeOf(this)){
                if(strongest == null || enemies.get(i).getMaxLife() > strongest.getMaxLife())
                    strongest = enemies.get(i);
            }
        return strongest;
    }
    
    private Enemy searchForWeakest(ArrayList<Enemy> enemies){
        Enemy weakest = null;
        for(int i = 0 ; i < enemies.size() ; i++)
            if(enemies.get(i).isSpawned() && enemies.get(i).isInRangeOf(this)){
                if(weakest == null || enemies.get(i).getMaxLife() < weakest.getMaxLife())
                    weakest = enemies.get(i);
            }
        return weakest;
    }
    
    private Enemy searchForClosest(ArrayList<Enemy> enemies){
        Enemy closest = null;
        float minDist = 10000000;
        for(int i = 0 ; i < enemies.size() ; i++)
            if(enemies.get(i).isSpawned() && enemies.get(i).isInRangeOf(this)){
                if(closest == null || MyMath.distanceBetween(this, enemies.get(i)) < minDist){
                    closest = enemies.get(i);
                    minDist = (float)MyMath.distanceBetween(this, enemies.get(i));
                }
                    
            }
        return closest;
    }
    
    public void aim(Enemy e){
        if(e == null && enemyAimed != null)
            enemyAimed.setIsAimed(false);
        enemyAimed = e;
        if(e == null)
            return;
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
    
    public void render(){
        for(int i = 0 ; i < textures.size() ; i++){
            RvB.drawFilledRectangle(x, y, size, size, textures.get(i), i == rotateIndex ? angle : 0);
        }
        if(selected)
            renderOverlay();
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
        });
        o2.addButton(b);
        // button focus
        b = new Button(o2.getW()-(int)(140*ref), o2.getH()-(int)(20*ref), (int)(120*ref), (int)(32*ref), TextManager.Text.FOCUS_SWITCH, RvB.fonts.get("normal"), RvB.colors.get("green_semidark"), RvB.colors.get("green_dark"), 0);
        b.setSwitch();
        focusButton = b;
        o2.addButton(b);
        overlays.add(o2);
    }
    
    public void renderOverlay(){
        Button b;
        Overlay overlay;
        
        for(Overlay o : overlays)
            o.render();
        
        overlay = overlays.get(0);
        overlay.drawText(overlay.getW()/2, overlay.getH()/2, name, RvB.fonts.get("normalL"));
        
        overlay = overlays.get(1);
        for(Upgrade up : upgrades)
            up.render();
        String price;
        b = overlay.getButtons().get(overlay.getButtons().size()-2);
        if(b.isHovered()){
            price = "+ "+(int)(totalMoneySpent/2);
            b.drawText(price, RvB.fonts.get("canBuy"));
        }
        else
            b.drawText(Text.SELL.getText(), RvB.fonts.get("normal"));
        b = overlay.getButtons().get(overlay.getButtons().size()-1);
        overlay.drawText(b.getX(), b.getY()-overlay.getY()-(int)(30*ref), Text.FOCUS.getText(), RvB.fonts.get("normal"));
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
    
    public int getWidth(){
        return width;
    }
    
    @Override
    public int getHitboxWidth(){
        return hitboxWidth;
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
        return (System.currentTimeMillis()-lastShoot >= 1000/(shootRate*game.gameSpeed) && (angle >= newAngle-6 && angle <= newAngle+6 || enemyAimed == null));
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
