package towers;

import Utils.MyMath;
import towser.Tile;
import ennemies.Enemy;
import java.util.ArrayList;
import java.util.Random;
import javax.sound.sampled.Clip;
import org.lwjgl.input.Mouse;
import org.newdawn.slick.opengl.Texture;
import towser.Towser;
import towser.Shootable;
import managers.SoundManager;
import static towser.Towser.game;
import static towser.Towser.ref;
import static towser.Towser.unite;
import ui.*;

public abstract class Tower extends Tile implements Shootable{
    
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
    
    public Tower(String type){
        super(type);
        upgrades = new ArrayList<>();
        overlays = new ArrayList<>();
    }
    
    public void update(){
        if(isPlaced){
            if(selected)
                checkOverlayInput();
            searchAndShoot();
            updateBullets();
            if(soundPlayed && enemyAimed == null){
                SoundManager.Instance.stopClip(clip);
                soundPlayed = false;
            }   
            render();
        }
        renderOther();
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
                SoundManager.Instance.playOnce(Towser.clips.get("upgrade"));
            }
        }
        if(overlays.get(1).getButtons().get(i).isClicked(0)){ // Sell button
            game.money += (int)(totalMoneySpent/2);
            Tile grass = new Tile(Towser.textures.get("grass"), "grass");
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
            SoundManager.Instance.playOnce(Towser.clips.get("sell"));
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
        for(int i = 0 ; i < textures.size() ; i++)
            Towser.drawFilledRectangle(x, y, size, size, textures.get(i), angle);
    }
    
    public void renderOther(){
        if(!isPlaced)
            renderPrevisu();
        
        if(selected && (isPlaced || canBePlaced()))
            renderDetails();
    }
    
    private void renderPrevisu(){
        if(canBePlaced()){
            float xPos = Math.floorDiv(Mouse.getX(), unite)*unite, yPos = Math.floorDiv(Towser.windHeight-Mouse.getY(), unite)*unite;
            Towser.drawFilledRectangle(xPos+unite/2-size/2, yPos+unite/2-size/2, size, size, null, 0.5f, textureStatic);
        }
        x = Mouse.getX();
        y = Towser.windHeight-Mouse.getY();
    }
    
    public void renderDetails(){
        int xPos = (int)x, yPos = (int)y;
        if(!isPlaced){
            xPos = Math.floorDiv(Mouse.getX(), unite)*unite+unite/2;
            yPos = Math.floorDiv(Towser.windHeight-Mouse.getY(), unite)*unite+unite/2;
        }
        Towser.drawCircle(xPos, yPos, range, Towser.colors.get("blue"));
        Towser.drawCircle(xPos, yPos, range-1, Towser.colors.get("grey"));
        Towser.drawCircle(xPos, yPos, range-2, Towser.colors.get("grey_light"));
        Towser.drawFilledCircle(xPos, yPos, range-2, Towser.colors.get("grey_light"), 0.1f);
        if(isPlaced)
            renderOverlay();
    }
    
    public void initOverlay(){
        Overlay o1, o2;
        
        o1 = new Overlay(0, Towser.windHeight-(int)((86+30)*ref), (int)(150*ref), (int)(30*ref));
        o1.setBG(Towser.textures.get("board"), 0.6f);
        overlays.add(o1);
        
        o2 = new Overlay(0, Towser.windHeight-(int)(86*ref), Towser.windWidth, (int)(86*ref));
        o2.setBG(Towser.textures.get("board"), 0.6f);
        
        int sep = (int) (600 * ref);
        sep -= 90*upgrades.size();
        if(sep < 25)
            sep = 25;
        int imageSize = o2.getH()-(int)(20*ref);
        int butWidth = (int) (200*ref), butHeight = (int)(38*ref);
        int marginToCenter = Towser.windWidth-o1.getW()-((upgrades.size()-1)*sep + (upgrades.size()-1)*butWidth + butWidth/2);
        marginToCenter = marginToCenter/2;
        if(marginToCenter < 0)
            marginToCenter = 0;
        o2.addImage(o1.getW()/2, imageSize/2+(int)(10*ref), imageSize, imageSize, textureStatic);
        for(int i = 0 ; i < upgrades.size() ; i++){
            Button b = new Button(o1.getW() + marginToCenter + i*sep + i*butWidth + butWidth/2, 2*o2.getH()/3, butWidth, butHeight, Towser.colors.get("green_semidark"), Towser.colors.get("green_dark"), upgrades.get(i).maxClick);
            if(upgrades.get(i).maxClick <= 0)
                b.setHidden(true);
            o2.addButton(b);
        }
        Button b = new Button(o1.getW()+(int)(40*ref), o2.getH()/2, (int)(80*ref), (int)(34*ref), Towser.colors.get("green_semidark"), Towser.colors.get("green_dark"));
        o2.addButton(b);
        overlays.add(o2);
    }
    
    public void renderOverlay(){
        String t = "";
        float upPrice;
        Button b;
        Overlay overlay;
        
        for(Overlay o : overlays)
            o.render();
        
        overlay = overlays.get(0);
        overlay.drawText(overlay.getW()/2, overlay.getH()/2, name, Towser.fonts.get("normalL"));
        
        overlay = overlays.get(1);
        int i;
        String space, price, up;
        for(i = 0 ; i < upgrades.size() ; i++){
            b = overlay.getButtons().get(i);
            if(upgrades.get(i).nbNumberToRound == 0)
                up = (int)upgrades.get(i).getValue()+"";
            else
                up = upgrades.get(i).getValue()+"";
            if(!b.isHidden()){
                overlay.drawImage(b.getX()-(int)(36*ref), (int)(4*ref), (int)(32*ref), (int)(32*ref), upgrades.get(i).icon);   
                overlay.drawText(b.getX()+(int)(20*ref), (int)(20*ref), up, Towser.fonts.get("normal"));   
            }
            else{
                overlay.drawImage(b.getX()-(int)(36*ref), overlay.getH()/2-(int)(16*ref), (int)(32*ref), (int)(32*ref), upgrades.get(i).icon);   
                overlay.drawText(b.getX()+(int)(20*ref), overlay.getH()/2, up, Towser.fonts.get("normal"));   
            }
            
            overlay.drawText(b.getX(), (int)(15*ref)+ Towser.fonts.get("normal").getHeight(t)/2, t, Towser.fonts.get("normal"));   
            
            upPrice = upgrades.get(i).price;
            if(upPrice != 0 && !b.isHidden()){
                if(b.isHovered() && upgrades.get(i).getValue() > 0){
                    b.drawText("+ " + upgrades.get(i).getIncreaseValue(), Towser.fonts.get("bonus"));
                }
                else{
                    price = (int)Math.floor(upPrice)+"";
                    space = "";
                    for(int j = 0 ; j < price.length() ; j++)
                        space += " ";
                    up = "Up (  "+ space +"  )";
                    b.drawText(0, 0, up, Towser.fonts.get("normal"));
                    if(game.money < (int)Math.floor(upPrice))
                        b.drawText((Towser.fonts.get("normal").getWidth(up) - Towser.fonts.get("cantBuy").getWidth(price) - Towser.fonts.get("normal").getWidth("  )"))/2 - 2, 0, price, Towser.fonts.get("cantBuy"));
                    else
                        b.drawText((Towser.fonts.get("normal").getWidth(up) - Towser.fonts.get("canBuy").getWidth(price) - Towser.fonts.get("normal").getWidth("  )"))/2 - 2, 0, price, Towser.fonts.get("canBuy"));
                }
            }
        }
        b = overlay.getButtons().get(overlay.getButtons().size()-1);
        if(b.isHovered()){
            price = "+ "+(int)(totalMoneySpent/2);
            b.drawText(price, Towser.fonts.get("canBuy"));
        }
        else
            b.drawText("Sell", Towser.fonts.get("normal"));
        
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
        y = Math.floorDiv(Towser.windHeight-Mouse.getY(), unite);
        map.get((int) y).set((int) x, this);
        setX(x*unite+unite/2);
        setY(y*unite+unite/2);
        game.money -= price;
        raisePrice();
        isPlaced = true;
        SoundManager.Instance.playOnce(Towser.clips.get("build"));
    }
    
    public boolean canBePlaced(){
        if(!isInWindow())
            return false;
        String tileType = game.map.get(Math.floorDiv((int)y, unite)).get(Math.floorDiv((int) x, unite)).getType(); // middle point
        if(tileType == "grass")
            return true;
        return false;
    }
    
    public boolean isPlaced(){
        return isPlaced;
    }

    private boolean isInWindow() {
        return (x < Towser.windWidth && x > 0 && y < Towser.windHeight && y > 0);
    }
    
    public void destroy(){
        game.towersDestroyed.add(this);
    }
   
    private boolean isMouseIn(){
        int MX = Mouse.getX(), MY = Towser.windHeight-Mouse.getY();
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
        return (System.currentTimeMillis()-lastShoot >= 1000/(shootRate*game.gameSpeed) && angle >= newAngle-4 && angle <= newAngle+4);
    }
    
    public void shoot(){
        enemiesTouched.clear();
        lastShoot = System.currentTimeMillis();
        Bullet bullet = new Bullet(this, (float)(x+size*Math.cos(Math.toRadians(angle))/2), (float)(y+size*Math.sin(Math.toRadians(angle))/2), enemyAimed, size/4 + bulletSizeBonus, bulletSprite, false);
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
