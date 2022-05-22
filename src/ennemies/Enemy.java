package ennemies;

import java.util.ArrayList;
import javax.sound.sampled.Clip;
import org.lwjgl.input.Mouse;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glRotated;
import static org.lwjgl.opengl.GL11.glTranslated;
import org.newdawn.slick.opengl.Texture;
import towers.Bullet;
import towers.Tower;
import towser.Shootable;
import managers.SoundManager;
import towser.Tile;
import towser.Towser;
import static towser.Towser.game;
import static towser.Towser.unite;
import ui.Overlay;

public abstract class Enemy implements Shootable, Comparable<Enemy>{
    
    public static double bonusLife = 0, bonusMS = 0;
    protected int eBalance;
    protected int reward, power, shootRate, range, life, maxLife, indiceTuile = -1, width;
    protected Texture sprite = null, brightSprite = null;
    protected float[] rgb;
    protected long stopFor = -1;
    protected String name;
    protected SoundManager.Volume volume;
    protected double x, y, xBase, yBase, spawnSpeed, minSpawnSpeed = 0.5, moveSpeed, startTimeStopFor, startTimeMove, weight;
    protected double angle, newAngle;
    protected Tile spawn, base;
    protected String dir;
    protected boolean isAimed = false, isMultipleShot, started = false;
    protected double waitFor = 125, startTimeWaitFor = 0;
    protected Clip clip;
    protected double stepEveryMilli, startTimeSteps;
    private double movingBy;
    private boolean mouseEntered = false;
    private int uniqueId;
    
    public Enemy(int id){
        uniqueId = id;
        spawn = game.getSpawn();
        base = game.getBase();
        if(spawn != null){
            x = spawn.getX()+unite/2;
            y = spawn.getY()+unite/2;
        }
        if(base != null){
            xBase = base.getX()+unite/2;
            yBase = base.getY()+unite/2;
        }
        startTimeMove = System.currentTimeMillis();
        startTimeSteps = System.currentTimeMillis();
    }
    
    protected void initBack(){
        moveSpeed = moveSpeed * (1+bonusMS/100);
        life = (int)Math.round(life * (1+bonusLife/100));
        maxLife = life;
        SoundManager.Instance.setClipVolume(clip, volume);
    }
    
    public void update(){
        if(game.enemySelected == null && isClicked())
            game.enemySelected = this;
        if(isMouseIn() && !mouseEntered && Towser.cursor != Towser.Cursor.POINTER){
            mouseEntered = true;
            Towser.setCursor(Towser.Cursor.POINTER);
        }
        else if(!isMouseIn() && Towser.cursor != Towser.Cursor.DEFAULT && mouseEntered){
            mouseEntered = false;
            Towser.setCursor(Towser.Cursor.DEFAULT);
        }
        if(stopFor != -1 && (System.currentTimeMillis() - startTimeStopFor >= stopFor)){
            started = true;
            stopFor = -1;
        }
        if(started){
            move();
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

            glPushMatrix(); //Save the current matrix.
            glTranslated(x, y, 0);
            if(angle != 0)
                glRotated(angle, 0, 0, 1);
            
            Texture sprite = this.sprite;
            if(startTimeWaitFor != 0 && System.currentTimeMillis() - startTimeWaitFor < waitFor)
                sprite = this.brightSprite;
            else if(startTimeWaitFor != 0)
                startTimeWaitFor = 0;

            Towser.drawFilledRectangle(-width/2, -width/2, width, width, null, 1, sprite);

            glPopMatrix(); // Reset the current matrix to the one that was saved.
        }  
        else
            Towser.drawFilledCircle(x, y, width/2, rgb, 1);
    }
    
    private void move(){
        if(isOnCenterOfTile() && !isOnSameTile()){
            indiceTuile += 1;
            setPositionInCenterOfTile();
             if(isInBase())
                attack();
            else
                setDirection();
        }

        movingBy = ((moveSpeed*game.gameSpeed) * Towser.deltaTime / 50) * Towser.ref;
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
    
    private boolean isOnSameTile(){
        if(indiceTuile == -1)
            return false;
        if(isDead())
            return true;
        int x = (int) Math.floor(this.x/unite), y = (int) Math.floor(this.y/unite);
        Tile t = game.path.get(indiceTuile);
        return (x == t.getIndexX() && y == t.getIndexY());
    }
    
    private boolean isOnCenterOfTile(){
        return (Math.floor(x)%unite <= unite/2+movingBy && Math.floor(x)%unite >= unite/2-movingBy && Math.floor(y)%unite <= unite/2+movingBy && Math.floor(y)%unite >= unite/2-movingBy);
    }
    
    private void setPositionInCenterOfTile(){
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
        double xDiff = t.getX()-x, yDiff = t.getY()-y;
        double angle, cosinus, sinus;
        angle = Math.atan2(yDiff, xDiff);
        cosinus = Math.floor(Math.cos(angle)*1000)/1000;
        sinus = Math.floor(Math.sin(angle)*1000)/1000;
        return (x <= t.getX()+((t.getRange())*Math.abs(cosinus))+moveSpeed && x >= t.getX()-((t.getRange())*Math.abs(cosinus))-moveSpeed && y <= t.getY()+((t.getRange())*Math.abs(sinus))+moveSpeed && y >= t.getY()-((t.getRange())*Math.abs(sinus))-moveSpeed);
    }
    
    private boolean isMouseIn(){
        int MX = Mouse.getX(), MY = Towser.windHeight-Mouse.getY();
        return (MX >= x-width/2 && MX <= x+width/2 && MY >= y-width/2 && MY <= y+width/2);
    }
    
    public boolean isClicked(){
        return (isMouseIn() && Mouse.isButtonDown(0));
    }
    
    /// enemy.renderOverlay() is called in game, right after main overlay is rendered
    public void renderInfo(Overlay o){
        
        // Sprites
        Towser.drawFilledRectangle(o.getX()+20, o.getY(), o.getH(), o.getH(), null, 1, sprite);
        Towser.drawFilledRectangle(o.getX()+o.getW()-o.getH()-20, o.getY(), o.getH(), o.getH(), null, 1, sprite);
        // Lifebar
        int width = (int) (290*Towser.ref), height = 16;
        Towser.drawFilledRectangle(o.getX()+o.getW()/2-width/2, o.getY()+o.getH()-height-3, width, height, Towser.colors.get("white"), 1, null);
        Towser.drawFilledRectangle(o.getX()+o.getW()/2-width/2, o.getY()+o.getH()-height-3, (int)(((double)life/(double)maxLife)*width), height, Towser.colors.get("life"), 1, null);
        Towser.drawRectangle(o.getX()+o.getW()/2-width/2, (int) (o.getY()+o.getH()-height-3), width, height, Towser.colors.get("green_dark"), 0.8f, 2);        
        // Name & life max
        o.drawText(o.getW()/2, 12, name, Towser.fonts.get("normalL"));
        o.drawText(o.getW()/2+Towser.fonts.get("normalL").getWidth(name)/2+Towser.fonts.get("life").getWidth(""+maxLife)/2+5, 12, ""+maxLife, Towser.fonts.get("life"));
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
        if(life <= 0){
            die();
            game.money += reward;
        }
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
    
    public double getSpawnSpeed(){
        return spawnSpeed;
    }
    
    public float[] getRGB(){
        return rgb;
    }
    
    public int getIndiceTuile(){
        return indiceTuile;
    }
    
    public double getMoveSpeed(){
        return moveSpeed;
    }
    
    public double getX(){
        return x;
    }
    
    public double getY(){
        return y;
    }
    
    public void decreaseSpawnSpeedBy(double decrease){
        if(spawnSpeed - decrease >= minSpawnSpeed)
            spawnSpeed -= decrease;
        else
            spawnSpeed = minSpawnSpeed;
    }
    
    public void setX(double x){
        this.x = x;
    }
    
    public void setY(double y){
        this.y = y;
    }
    
    public int getWidth(){
        return width;
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
    
    public double getWeight(){
        return weight;
    }
}
