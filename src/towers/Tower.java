package towers;

import towser.Tile;
import ennemies.Enemy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.sound.sampled.Clip;
import org.lwjgl.input.Mouse;
import org.newdawn.slick.UnicodeFont;
import org.newdawn.slick.opengl.Texture;
import towser.Towser;
import towser.Shootable;
import managers.SoundManager;
import static towser.Towser.game;
import static towser.Towser.ref;
import static towser.Towser.unite;
import ui.*;

public abstract class Tower extends Tile implements Shootable{
    
    protected int price, range, power, bulletSpeed, life, width, totalPrice, nbUpgrades = 4;
    protected double lastShoot = 0, growth = 0;
    protected float shootRate;
    protected String name, textureName;
    protected SoundManager.Volume volume = SoundManager.Volume.SEMI_LOW;
    protected boolean isPlaced = false, follow, selected = true, isMultipleShot, canRotate;
    protected Enemy enemyAimed;
    protected ArrayList<Bullet> bullets = new ArrayList<>(), bulletsToRemove = new ArrayList<>();
    protected ArrayList<Shootable> enemiesTouched = new ArrayList<>();
    protected ArrayList<Overlay> overlays;
    protected Texture textureStatic, bulletSprite;
    protected Clip clip;
    // Upgrades order : range, power, shootRate, bulletSpeed
    protected Map<String, ArrayList<Float>> upgradesParam;
    
    public Tower(String type){
        super(type);
        upgradesParam = new HashMap<>();
        overlays = new ArrayList<>();
    }
    
    public void update(){
        if(isPlaced){
            if(selected)
                checkOverlayInput();
            searchAndShoot();
            updateBullets();
        }
        renderOther();
    }
    
    private void checkOverlayInput(){
        ArrayList<Button> buts = overlays.get(1).getButtons();
        Button b;
        float up = 0f, upPrice, upPriceIncrease, upMultiplier;
        for(int i = 0 ; i < buts.size() ; i++){
            switch(i){
                case 0:
                    up = range;
                    break;
                case 1:
                    up = power;
                    break;
                case 2:
                    up = shootRate;
                    break;
                case 3:
                    up = bulletSpeed;
                    break;
            }
            b = buts.get(i);
            upPrice = upgradesParam.get("prices").get(i);
            upPriceIncrease = upgradesParam.get("priceMultipliers").get(i);
            upMultiplier = upgradesParam.get("multipliers").get(i);
            if(b.isClicked(0) && game.money >= upPrice){
                size += growth;
                game.money -= upPrice;
                if(upMultiplier > 2)
                    up += upMultiplier;
                else
                    up *= upMultiplier;
                totalPrice += upPrice;
                upgradesParam.get("prices").set(i, upPrice*upPriceIncrease);
                b.click();
                switch(i){
                    case 0:
                        range = (int)up;
                        break;
                    case 1:
                        power = (int)up;
                        break;
                    case 2:
                        shootRate = (float) Math.ceil(up*10)/10;
                        break;
                    case 3:
                        bulletSpeed = (int)up;
                        break;
                }
            }
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
        if(enemyAimed != null && !enemyAimed.isDead() && enemyAimed.isInRangeOf(this) && canShoot())
            shoot();
    }
    
    public void aim(Enemy e){
        if(e == null && enemyAimed != null)
            enemyAimed.setIsAimed(false);
        enemyAimed = e;
        if(e != null){
            e.setIsAimed(true);
            if(canRotate){
                double t = 0.3;
                newAngle = Math.toDegrees(Math.atan2(enemyAimed.getY()-y, enemyAimed.getX()-x));
                
                if(newAngle-angle > 180)
                    newAngle -= 360;
                else if(angle-newAngle > 180)
                    newAngle += 360;
                
                angle = (1-t)*angle + t*newAngle;
                
                if(angle >= 360)
                    angle -= 360;
                else if(angle <= -360)
                    angle += 360;
                
                angle = Math.round(angle);
                newAngle = Math.round(newAngle);
            }
        }   
    }
    
    public void renderOther(){
        if(!isPlaced)
            renderPrevisu();
        
        if(selected && (isPlaced || canBePlaced()))
            renderDetails();
    }
    
    
    
    private void renderPrevisu(){
        if(canBePlaced()){
            double xPos = Math.floorDiv(Mouse.getX(), unite)*unite, yPos = Math.floorDiv(Towser.windHeight-Mouse.getY(), unite)*unite;
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
        
        o1 = new Overlay(0, (int) (Towser.windHeight-(90+20)*ref), (int)(ref*200), (int)(ref*20));
        o1.setBG(Towser.textures.get("board"));
        o1.setA(0.8f);
        overlays.add(o1);
        
        o2 = new Overlay(0, (int) (Towser.windHeight-90*ref), Towser.windWidth, (int) (90*ref));
        o2.setBG(Towser.textures.get("board"));
        o2.setA(0.8f);
        
        int margin = (int) (10*ref);
        int sep = (int) (75*ref);
        int imageSize = Math.min(o2.getW(), o2.getH());
        int butWidth = (int) (150*ref), butHeight = (int) (38*ref);
        
        o2.addImage(margin, margin, imageSize-2*margin, imageSize-2*margin, Towser.textures.get(textureName));
        for(int i = 0 ; i < upgradesParam.size() ; i++){
            Button b = new Button(margin*2 + imageSize + sep/2 + i*sep + i*butWidth + butWidth/2, 2*o2.getH()/3, butWidth, butHeight, Towser.colors.get("green_semidark"), Towser.colors.get("green_dark"), (int)Math.floor(upgradesParam.get("maxUpgradeClicks").get((i))));
            if(upgradesParam.get("maxUpgradeClicks").get(i) <= 0)
                b.setHidden(true);
            o2.addButton(b);
        }
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
        for(int i = 0 ; i < nbUpgrades ; i++){
            switch(i){
                case 0:
                    t = "Range : "+range;
                    break;
                case 1:
                    t = "Power : "+power;
                    break;
                case 2:
                    t = "Shoot rate : "+shootRate;
                    break;
                case 3:
                    t = "Bullet speed : "+bulletSpeed;
                    break;
            }
            b = overlay.getButtons().get(i);
            upPrice = upgradesParam.get("prices").get(i);
            
            overlay.drawText(b.getX(), (int) (15*ref + Towser.fonts.get("normal").getHeight(t)/2), t, Towser.fonts.get("normal"));
            
            if(upPrice != 0 && !b.isHidden()){
                String price = (int)Math.floor(upPrice)+"";
                
                UnicodeFont priceFont = Towser.fonts.get("canBuy");
                UnicodeFont font = Towser.fonts.get("normal");
                if(game.money < (int)Math.floor(upPrice))
                    priceFont = Towser.fonts.get("cantBuy");

                String space = "";
                for(int j = 0 ; j < price.length() ; j++)
                    space += " ";
                String up = "Up (  "+ space +"  )";
                b.drawText(0, 0, up, font);
                b.drawText((font.getWidth(up) - priceFont.getWidth(price) - font.getWidth("  )"))/2 - 2, 0, price, priceFont);
            }
        }
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
    }
    
    public boolean canBePlaced(){
        if(!isInWindow())
            return false;
        String tileType = game.getMap().get(Math.floorDiv((int)y, unite)).get(Math.floorDiv((int) x, unite)).getType(); // middle point
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
        game.getTowersDestroyed().add(this);
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
    
    public void setSelected(boolean b){
        selected = b;
    }
    
    public boolean isSelected(){
        return selected;
    }
    
    @Override
    public int getWidth(){
        return width;
    }
    
    public void setFollow(boolean b){
        follow = b;
    }
    
    @Override
    public boolean getFollow(){
        return follow;
    }
    
    public boolean canShoot(){
        return (System.currentTimeMillis()-lastShoot >= 1000/(shootRate*game.gameSpeed) && angle >= newAngle-4 && angle <= newAngle+4);
    }
    
    public void shoot(){
        if(isMultipleShot)
            enemiesTouched.clear();
        lastShoot = System.currentTimeMillis();
        Bullet bullet = new Bullet(this, enemyAimed, size/10, bulletSprite, false);
        bullets.add(bullet);
        if(clip != null)
            SoundManager.Instance.playOnce(clip);
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
