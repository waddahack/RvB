package towers;

import towser.Tile;
import ennemies.Enemy;
import java.awt.Color;
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
import org.newdawn.slick.font.effects.ColorEffect;
import static towser.Towser.game;
import static towser.Towser.ref;
import static towser.Towser.unite;
import ui.*;

public abstract class Tower extends Tile implements Shootable{
    
    protected int price, range, power, bulletSpeed, life, width, totalPrice, nbUpgrades = 4, totalMoneySpent;
    protected double lastShoot = 0, growth = 0;
    protected float shootRate;
    protected String name, textureName;
    protected SoundManager.Volume volume = SoundManager.Volume.SEMI_LOW;
    protected boolean isPlaced = false, follow, selected = true, isMultipleShot, canRotate, toBeRemoved;
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
        Button b;
        float up = 0f, upPrice, upPriceIncrease, upMultiplier;
        int i;
        for(i = 0 ; i < overlays.get(1).getButtons().size()-1 ; i++){
            b = overlays.get(1).getButtons().get(i);
            if(b.isClicked(0)){
                upPrice = upgradesParam.get("prices").get(i);
                if(game.money < upPrice)
                    continue;
                upPriceIncrease = upgradesParam.get("priceMultipliers").get(i);
                upMultiplier = upgradesParam.get("multipliers").get(i);
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
                size += growth;
                game.money -= upPrice;
                totalMoneySpent += upPrice;
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
        if(overlays.get(1).getButtons().get(i).isClicked(0)){ // Sell button
            game.money += (int)(totalMoneySpent/2);
            Tile grass = new Tile(Towser.textures.get("grass"), "grass");
            grass.setRotateIndex(0);
            grass.setX(x);
            grass.setY(y);
            game.map.get(getIndexY()).set(getIndexX(), grass);
            if(game.towerSelected == this)
                game.towerSelected = null;
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
        
        o1 = new Overlay(0, Towser.windHeight-(int)((86+30)*ref), (int)(150*ref), (int)(30*ref));
        o1.setBG(Towser.textures.get("board"), 0.6f);
        overlays.add(o1);
        
        o2 = new Overlay(0, Towser.windHeight-(int)(86*ref), Towser.windWidth, (int)(86*ref));
        o2.setBG(Towser.textures.get("board"), 0.6f);
        
        int sep = (int) (200*ref);
        int imageSize = o2.getH()-(int)(20*ref);
        int butWidth = (int) (200*ref), butHeight = (int)(38*ref);
        int marginToCenter = Towser.windWidth-o1.getW()-((upgradesParam.size()-1)*sep + (upgradesParam.size()-1)*butWidth + butWidth/2 + butWidth);
        marginToCenter = marginToCenter/2;
        if(marginToCenter < 0)
            marginToCenter = 0;
        o2.addImage(o1.getW()/2, imageSize/2+(int)(10*ref), imageSize, imageSize, Towser.textures.get(textureName));
        for(int i = 0 ; i < upgradesParam.size() ; i++){
            Button b = new Button(o1.getW() + marginToCenter + i*sep + i*butWidth + butWidth/2, 2*o2.getH()/3, butWidth, butHeight, Towser.colors.get("green_semidark"), Towser.colors.get("green_dark"), (int)Math.floor(upgradesParam.get("maxUpgradeClicks").get((i))));
            if(upgradesParam.get("maxUpgradeClicks").get(i) <= 0)
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
        String space, up, price;
        float upNumber = -1;
        UnicodeFont font, priceFont;
        for(i = 0 ; i < nbUpgrades ; i++){
            b = overlay.getButtons().get(i);
            switch(i){
                case 0:
                    if(!b.isHidden()){
                        overlay.drawImage(b.getX()-(int)(36*ref), (int)(4*ref), (int)(32*ref), (int)(32*ref), Towser.textures.get("rangeIcon"));   
                        overlay.drawText(b.getX()+(int)(20*ref), (int)(20*ref), range+"", Towser.fonts.get("normal"));   
                    }
                    else{
                        overlay.drawImage(b.getX()-(int)(36*ref), overlay.getH()/2-(int)(16*ref), (int)(32*ref), (int)(32*ref), Towser.textures.get("rangeIcon"));   
                        overlay.drawText(b.getX()+(int)(20*ref), overlay.getH()/2, range+"", Towser.fonts.get("normal"));   
                    }
                    upNumber = range;
                    break;
                case 1:
                    if(!b.isHidden()){
                        overlay.drawImage(b.getX()-(int)(36*ref), (int)(4*ref), (int)(32*ref), (int)(32*ref), Towser.textures.get("powerIcon"));   
                        overlay.drawText(b.getX()+(int)(20*ref), (int)(20*ref), power+"", Towser.fonts.get("normal"));   
                    }
                    else{
                        overlay.drawImage(b.getX()-(int)(36*ref), overlay.getH()/2-(int)(16*ref), (int)(32*ref), (int)(32*ref), Towser.textures.get("powerIcon"));   
                        overlay.drawText(b.getX()+(int)(20*ref), overlay.getH()/2, power+"", Towser.fonts.get("normal"));   
                    } 
                    upNumber = power;
                    break;
                case 2:
                    if(!b.isHidden()){
                        overlay.drawImage(b.getX()-(int)(36*ref), (int)(4*ref), (int)(32*ref), (int)(32*ref), Towser.textures.get("attackSpeedIcon"));   
                        overlay.drawText(b.getX()+(int)(20*ref), (int)(20*ref), shootRate+"/s", Towser.fonts.get("normal"));   
                    }
                    else{
                        overlay.drawImage(b.getX()-(int)(36*ref), overlay.getH()/2-(int)(16*ref), (int)(32*ref), (int)(32*ref), Towser.textures.get("attackSpeedIcon"));   
                        overlay.drawText(b.getX()+(int)(20*ref), overlay.getH()/2, shootRate+"/s", Towser.fonts.get("normal"));   
                    }
                    upNumber = shootRate;
                    break;
                case 3:
                    t = "Bullet speed : "+bulletSpeed;
                    upNumber = bulletSpeed;
                    break;
            }
            overlay.drawText(b.getX(), (int)(15*ref)+ Towser.fonts.get("normal").getHeight(t)/2, t, Towser.fonts.get("normal"));   
            
            upPrice = upgradesParam.get("prices").get(i);
            if(upPrice != 0 && !b.isHidden()){
                if(b.isHovered() && upNumber > 0){
                    if(upgradesParam.get("multipliers").get(i) > 2)
                        up = "+ " + (int)((upNumber+upgradesParam.get("multipliers").get(i))-upNumber);
                    else{
                        if(i == 2) // shoot rate
                            up = "+ " + (float)Math.ceil(10*((upNumber*upgradesParam.get("multipliers").get(i))-upNumber))/10;
                        else
                            up = "+ " + (int)((upNumber*upgradesParam.get("multipliers").get(i))-upNumber);
                    } 
                    b.drawText(up, Towser.fonts.get("bonus"));
                }
                else{
                    price = (int)Math.floor(upPrice)+"";
                    priceFont = Towser.fonts.get("canBuy");
                    font = Towser.fonts.get("normal");
                    if(game.money < (int)Math.floor(upPrice))
                        priceFont = Towser.fonts.get("cantBuy");

                    space = "";
                    for(int j = 0 ; j < price.length() ; j++)
                        space += " ";
                    up = "Up (  "+ space +"  )";
                    b.drawText(0, 0, up, font);
                    b.drawText((font.getWidth(up) - priceFont.getWidth(price) - font.getWidth("  )"))/2 - 2, 0, price, priceFont);
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
