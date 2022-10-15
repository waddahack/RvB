package ennemies;

import Utils.MyMath;
import java.util.ArrayList;
import javax.sound.sampled.Clip;
import org.lwjgl.input.Mouse;
import org.newdawn.slick.opengl.Texture;
import towers.Bullet;
import towers.Tower;
import rvb.Shootable;
import managers.SoundManager;
import rvb.Tile;
import rvb.RvB;
import static rvb.RvB.game;
import static rvb.RvB.unite;
import ui.Overlay;

public abstract class Enemy implements Shootable, Comparable<Enemy>{
    
    public static double bonusLife = 0, bonusMS = 0;
    protected int eBalance;
    protected int reward, power, shootRate, range, life, maxLife, indiceTuile = -1, width;
    protected Texture sprite = null, brightSprite = null;
    protected long stopFor = -1;
    protected String name;
    protected SoundManager.Volume volume;
    protected float x, y, xBase, yBase, minSpawnSpeed = 0.5f, moveSpeed;
    protected double angle, newAngle, startTimeStopFor, startTimeMove;
    protected String dir;
    protected boolean isAimed = false, isMultipleShot, started = false;
    protected double waitFor = 175, startTimeWaitFor = 0;
    protected Clip clip;
    protected double stepEveryMilli, startTimeSteps;
    protected double movingBy;
    private boolean mouseEntered = false;
    
    public Enemy(){
        if(game.spawn != null){
            x = game.spawn.getX()+unite/2;
            y = game.spawn.getY()+unite/2;
        }
        if(game.base != null){
            xBase = game.base.getX()+unite/2;
            yBase = game.base.getY()+unite/2;
        }
        startTimeMove = System.currentTimeMillis();
        startTimeSteps = System.currentTimeMillis();
    }
    
    protected void initBack(){
        moveSpeed = (float) (moveSpeed + (moveSpeed*bonusMS/100));
        life = (int)Math.round(life + (life*bonusLife/100));
        maxLife = life;
        SoundManager.Instance.setClipVolume(clip, volume);
    }
    
    public void update(){
        if(game.enemySelected == null && isClicked() && started)
            game.setEnemySelected(this);
        if(isMouseIn() && !mouseEntered && RvB.cursor != RvB.Cursor.POINTER){
            mouseEntered = true;
            RvB.setCursor(RvB.Cursor.POINTER);
        }
        else if(!isMouseIn() && RvB.cursor != RvB.Cursor.DEFAULT && mouseEntered){
            mouseEntered = false;
            RvB.setCursor(RvB.Cursor.DEFAULT);
        }
        if(stopFor != -1 && (System.currentTimeMillis() - startTimeStopFor >= stopFor)){
            started = true;
            stopFor = -1;
        }
        if(started){
            move();
            if(clip != null){
                if(stepEveryMilli == 0){
                    stepEveryMilli = -1;
                    SoundManager.Instance.playLoop(clip);
                }   
                else if(stepEveryMilli > 0 && System.currentTimeMillis() - startTimeSteps >= stepEveryMilli/(double)game.gameSpeed){
                    startTimeSteps = System.currentTimeMillis();
                    SoundManager.Instance.playOnce(clip);
                }
            }
        }
    }
    
    public void render(){
        if(!started && stopFor == -1)
            return;
        if(sprite != null){
            double t = 0.03*moveSpeed*game.gameSpeed;
            if(t < 0.1) t = 0.1;

            if(Math.abs(angle - newAngle) <= 5)
                t = 1;
            
            angle = (1-t)*angle + t*newAngle;

            angle = Math.round(angle);

            Texture sprite = this.sprite;
            if(startTimeWaitFor != 0 && System.currentTimeMillis() - startTimeWaitFor < waitFor)
                sprite = this.brightSprite;
            else if(startTimeWaitFor != 0)
                startTimeWaitFor = 0;

            RvB.drawFilledRectangle(x, y, width, width, sprite, angle);
        }  
    }
    
    protected void move(){
        if(isOnCenterOfTile() && !isOnSameTile()){
            indiceTuile += 1;
            setPositionInCenterOfTile();
            if(isInBase())
                attack();
            else
                setDirection();
        }

        movingBy = ((moveSpeed*game.gameSpeed) * RvB.deltaTime / 50) * RvB.ref;
        switch(dir){
            case "down" : 
                y += movingBy;
                break;
            case "left" : 
                x -= movingBy;
                break;
            case "up" : 
                y -= movingBy;
                break;
            case "right" : 
                x += movingBy;
                break;
        }
        startTimeMove = System.currentTimeMillis();
    }
    
    public void setDirection(){
        Tile tile = game.path.get(indiceTuile);
        if(tile.nextRoad.type.equals("nothing")){
            die();
            return;
        }
        if(dir == null){
            dir = tile.getDirection();
            switch(dir){
                case "down":
                    newAngle = 180;
                    break;
                case "left":
                    newAngle = -90;
                    break;
                case "right":
                    newAngle = 90;
                    break;
                default:
                    newAngle = 0;
                    break;
            }
            return;
        }
        String oldDir = dir;
        dir = tile.getDirection();   
        if(!dir.equals(oldDir)){
            switch(dir){
                case "down":
                    if(oldDir.equals("left"))
                        newAngle = angle-90;
                    else
                        newAngle = angle+90;
                    break;
                case "left":
                    if(oldDir.equals("up"))
                        newAngle = angle-90;
                    else
                        newAngle = angle+90;
                    break;
                case "right":
                    if(oldDir.equals("up"))
                        newAngle = angle+90;
                    else
                        newAngle = angle-90;
                    break;
                default:
                    if(oldDir.equals("left"))
                        newAngle = angle+90;
                    else
                        newAngle = angle-90;
                    break;
            }
        }
    }
    
    protected boolean isOnSameTile(){
        if(indiceTuile == -1)
            return false;
        if(isDead())
            return true;
        int x = (int) Math.floor(this.x/unite), y = (int) Math.floor(this.y/unite);
        Tile t = game.path.get(indiceTuile);
        return (x == t.getIndexX() && y == t.getIndexY());
    }
    
    protected boolean isOnCenterOfTile(){
        if(indiceTuile < 0)
            return true;
        return (Math.floor(x)%unite <= unite/2+movingBy && Math.floor(x)%unite >= unite/2-movingBy && Math.floor(y)%unite <= unite/2+movingBy && Math.floor(y)%unite >= unite/2-movingBy);
    }
    
    protected void setPositionInCenterOfTile(){
        Tile t = game.path.get(indiceTuile);
        x = t.getX()+unite/2;
        y = t.getY()+unite/2;
    }
    
    public boolean isInBase(){
        return (indiceTuile == game.path.size()-1);
    }
    
    public ArrayList<Shootable> getEnemiesTouched(){
        return null;
    }
    
    public boolean isInRangeOf(Tower t){
        double angle, cosinus, sinus;
        angle = MyMath.angleBetween(this, (Shootable) t);
        cosinus = Math.floor(Math.cos(angle)*1000)/1000;
        sinus = Math.floor(Math.sin(angle)*1000)/1000;
        return (x <= t.getX()+((t.getRange())*Math.abs(cosinus))+moveSpeed && x >= t.getX()-((t.getRange())*Math.abs(cosinus))-moveSpeed && y <= t.getY()+((t.getRange())*Math.abs(sinus))+moveSpeed && y >= t.getY()-((t.getRange())*Math.abs(sinus))-moveSpeed);
    }
    
    protected boolean isMouseIn(){
        int MX = Mouse.getX(), MY = RvB.windHeight-Mouse.getY();
        return (MX >= x-width/2 && MX <= x+width/2 && MY >= y-width/2 && MY <= y+width/2);
    }
    
    public boolean isClicked(){
        return (isMouseIn() && Mouse.isButtonDown(0));
    }
    
    /// enemy.renderOverlay() is called in game, right after main overlay is rendered
    public void renderInfo(){
        Overlay o = game.getOverlays().get(2);
        o.render();
        // Sprites
        RvB.drawFilledRectangle(o.getX()+20, o.getY(), o.getH(), o.getH(), null, 1, sprite);
        RvB.drawFilledRectangle(o.getX()+o.getW()-o.getH()-20, o.getY(), o.getH(), o.getH(), null, 1, sprite);
        // Lifebar
        int width = (int) (290*RvB.ref), height = 16;
        RvB.drawFilledRectangle(o.getX()+o.getW()/2-width/2, o.getY()+o.getH()-height-3, width, height, RvB.colors.get("lightGreen"), 1, null);
        RvB.drawFilledRectangle(o.getX()+o.getW()/2-width/2, o.getY()+o.getH()-height-3, (int)(((double)life/(double)maxLife)*width), height, RvB.colors.get("life"), 1, null);
        RvB.drawRectangle(o.getX()+o.getW()/2-width/2, (int) (o.getY()+o.getH()-height-3), width, height, RvB.colors.get("green_dark"), 0.8f, 2);        
        // Name & life max
        o.drawText(o.getW()/2, 12, name, RvB.fonts.get("normalL"));
        o.drawText(o.getW()/2+RvB.fonts.get("normalL").getWidth(name)/2+RvB.fonts.get("life").getWidth(""+maxLife)/2+5, 12, ""+maxLife, RvB.fonts.get("life"));
    }
    
    public void attack(){
        if(game.life > 0)
            game.getAttackedBy(power);
        die();
    }
    
    public void putInBase(){
        x = xBase;
        y = yBase;
    }

    public void attacked(int power){
        if(!started)
            return;
        life -= power;
        startTimeWaitFor = System.currentTimeMillis();
        if(life <= 0)
            die();
    }
    
    public void die(){
        life = 0;
        game.getEnnemiesDead().add(this);
        if(game.enemySelected == this)
            game.enemySelected = null;
        if(clip != null){
            clip.stop();
            SoundManager.Instance.clipToClose(clip);
        } 
        game.money += reward;
    }
    
    public int compareTo(Enemy e){
        if(eBalance == e.eBalance)
            return 0;
        else if(eBalance < e.eBalance)
            return -1;
        else
            return 1;
    }
    
    public boolean isDead(){
        return (life <= 0);
    }
    
    public void setStarted(boolean b){
        started = b;
    }
    
    public int getIndiceTuile(){
        return indiceTuile;
    }
    
    public double getMoveSpeed(){
        return moveSpeed;
    }
    
    public float getX(){
        return x;
    }
    
    public float getY(){
        return y;
    }
    
    public void setX(float x){
        this.x = x;
    }
    
    public void setY(float y){
        this.y = y;
    }
    
    public int getWidth(){
        return width;
    }
    public int getExplodeRadius(){
        return 0;
    }
    
    public boolean getExplode(){
        return false;
    }
    
    public boolean getFollow(){
        return false;
    }
    
    public int getBulletSpeed(){
        return 1;
    }
    
    public int getReward(){
        return reward;
    }
    
    public Clip getClip(){
        return clip;
    }
    
    public double getStepEveryMilli(){
        return stepEveryMilli;
    }
    
    public ArrayList<Bullet> getBullets(){
        return null;
    }
    
    public ArrayList<Bullet> getBulletsToRemove(){
        return null;
    }
    
    public int getPower(){
        return power;
    }
    
    public boolean hasStarted(){
        return started;
    }
    
    public boolean isSpawned(){
        return started;
    }
    
    public int getRange(){
        return range;
    }
    
    public boolean isMultipleShot(){
        return isMultipleShot;
    }
    
    public void setIndiceTuile(int i){
        indiceTuile = i;
    }
    
    public void setDir(String d){
        dir = d;
    }
    
    public void stopFor(int t){
        stopFor = t;
        startTimeStopFor = System.currentTimeMillis();
    }
    
    public boolean isAimed(){
        return isAimed;
    }
    
    public void setIsAimed(boolean b){
        isAimed = b;
    }
}
