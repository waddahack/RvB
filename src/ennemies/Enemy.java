package ennemies;

import java.util.ArrayList;
import java.util.Stack;
import javax.sound.sampled.Clip;
import managers.PopupManager;
import rvb.Shootable;
import managers.SoundManager;
import rvb.Tile;
import rvb.RvB;
import static rvb.RvB.game;
import static rvb.RvB.ref;
import static rvb.RvB.unite;
import ui.Overlay;

public abstract class Enemy extends Shootable implements Comparable<Enemy>{
    
    public static enum Type{
        BASIC, FAST, TRICKY, STRONG, FLYING, BOSS
    }
    
    public Type type;
    public float bonusMS = 0;
    protected int eBalance;
    protected int indiceTuile = -1, startTimeStopFor, startTimeMove, startTimeSlow;
    protected int stepEveryMilli, oldstepEveryMilli, startTimeSteps;
    protected float xBase, yBase, moveSpeed, oldMoveSpeed, slowedBy = 0;
    protected String dir;
    protected long stopFor = -1;
    protected double movingBy;
    protected Stack<Evolution> evolutions = new Stack<>();
    protected static Clip armorBreak = SoundManager.Instance.getClip("armor_break");
    protected Clip clipWalk;
    protected SoundManager.Volume volumeWalk = SoundManager.Volume.SEMI_LOW;
    
    public Enemy(Type type){
        super();
        this.type = type;
        if(game.spawn != null){
            x = game.spawn.getX()+unite/2;
            y = game.spawn.getY()+unite/2;
        }
        if(game.base != null){
            xBase = game.base.getX()+unite/2;
            yBase = game.base.getY()+unite/2;
        }
        startTimeMove = game.timeInGamePassed;
        startTimeSteps = game.timeInGamePassed;
        SoundManager.Instance.setClipVolume(armorBreak, SoundManager.Volume.SEMI_HIGH);
        canShoot = false;
        focusIndex = 4;
    }
    
    @Override
    protected void initBack(){
        super.initBack();
        life *= game.difficulty.enemiesLife;
        moveSpeed += moveSpeed*bonusMS/100f;
        oldMoveSpeed = moveSpeed;
        oldstepEveryMilli = stepEveryMilli;
        SoundManager.Instance.setClipVolume(clipWalk, volumeWalk);
    }
    
    @Override
    public void update(){
        if(game.enemySelected == null && isClicked(0) && started && !PopupManager.Instance.onPopup())
            game.setEnemySelected(this);
        if(isMouseIn() && !mouseEntered && RvB.cursor != RvB.Cursor.POINTER){
            mouseEntered = true;
            RvB.setCursor(RvB.Cursor.POINTER);
        }
        else if(!isMouseIn() && RvB.cursor != RvB.Cursor.DEFAULT && mouseEntered){
            mouseEntered = false;
            RvB.setCursor(RvB.Cursor.DEFAULT);
        }
        if(stopFor != -1 && (game.timeInGamePassed - startTimeStopFor >= stopFor)){
            started = true;
            stopFor = -1;
        }
        if(started){
            move();
            if(clipWalk != null){
                //if(game.enemySelected == this) RvB.debug(stepEveryMilli);
                if(stepEveryMilli == 0){
                    stepEveryMilli = -1;
                    //if(game.enemySelected == this) RvB.debug(stepEveryMilli);
                    SoundManager.Instance.playLoop(clipWalk);
                }   
                else if(stepEveryMilli > 0 && game.timeInGamePassed - startTimeSteps >= stepEveryMilli){
                    startTimeSteps = game.timeInGamePassed;
                    SoundManager.Instance.playOnce(clipWalk);
                }
            }
            super.update();
        }
    }
    
    @Override
    public void render(){
        if(!started && stopFor == -1)
            return;
        if(rotateIndex < 0 || enemyAimed == null || enemyAimed.isDead()){
            double t = 0.03*moveSpeed*game.gameSpeed;
            if(t < 0.1) t = 0.1;

            if(Math.abs(angle - newAngle) <= 5)
                t = 1;

            angle = (int) Math.round((1-t)*angle + t*newAngle);
        }   

        if(!evolutions.isEmpty()){
            RvB.drawFilledRectangle(x, y, size, size, textures.get(0), angle, 1);
            evolutions.peek().render(); 
        } 
        else
            super.render();
    }
    
    protected void move(){
        if(isOnCenterOfTile() && !isOnSameTile()){
            indiceTuile += 1;
            if(indiceTuile > 0)
                game.path.get(indiceTuile-1).stepped();
            setPositionInCenterOfTile();
            if(isInBase())
                commit();
            else
                setDirection();
        }
        
        if(slowedBy > 0 && moveSpeed == oldMoveSpeed){
            moveSpeed *= (1-slowedBy);
            stepEveryMilli *= (1+slowedBy);
        }
            
        else if(slowedBy != 0 && game.timeInGamePassed - startTimeSlow >= 1000){
            moveSpeed = oldMoveSpeed;
            stepEveryMilli = oldstepEveryMilli;
            slowedBy = 0;
        }
            
        movingBy = (moveSpeed*game.gameSpeed * RvB.deltaTime/50) * RvB.ref;
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
        startTimeMove = game.timeInGamePassed;
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
    
    /// enemy.renderOverlay() is called in game, right after main overlay is rendered
    public void renderInfo(){
        Overlay o = game.OvEnemyInfo;
        o.render();
        // Sprites
        RvB.drawFilledRectangle(o.getX()+20, o.getY(), o.getH(), o.getH(), null, 1, textureStatic);
        RvB.drawFilledRectangle(o.getX()+o.getW()-o.getH()-20, o.getY(), o.getH(), o.getH(), null, 1, textureStatic);
        // Lifebar
        int width = (int) (290*ref), height = (int) (16*ref);
        int currentLife = (int) (evolutions.isEmpty() ? ((double)life/(double)maxLife)*width : ((double)evolutions.peek().life/(double)evolutions.peek().maxLife)*width);
        float[] bgColor = evolutions.isEmpty() ? RvB.colors.get("lightGreen") : evolutions.size() > 1 ? evolutions.get(evolutions.size()-2).lifeColor : RvB.colors.get("life");
        RvB.drawFilledRectangle(o.getX()+o.getW()/2-width/2, o.getY()+o.getH()-height-3, width, height, bgColor, 1, null);
        RvB.drawFilledRectangle(o.getX()+o.getW()/2-width/2, o.getY()+o.getH()-height-3, currentLife, height, evolutions.isEmpty() ? RvB.colors.get("life") : evolutions.peek().lifeColor, 1, null);
        RvB.drawRectangle(o.getX()+o.getW()/2-width/2, (int) (o.getY()+o.getH()-height-3), width, height, RvB.colors.get("green_dark"), 0.8f, (int) (2*ref));        
        // Name & life max
        o.drawText(o.getW()/2, (int) (12*ref), name.getText(), RvB.fonts.get("normalL"));
        o.drawText(o.getW()/2+RvB.fonts.get("normalL").getWidth(name.getText())/2+RvB.fonts.get("life").getWidth(""+Math.round(maxLife))/2+5, (int)(12*ref), ""+Math.round(maxLife), RvB.fonts.get("life"));
    }
    
    @Override
    public void die(){
        super.die();
        game.enemiesDead.add(this);
        if(selected)
            game.setEnemySelected(null);
        game.money += reward;
        if(clipWalk != null){
            clipWalk.stop();
            SoundManager.Instance.clipToClose(clipWalk);
        }
    }
    
    public void commit(){
        if(game.life > 0){
            game.getAttackedBy(getPower());
            game.money -= reward;
        } 
        die();
    }
    
    public double getMoveSpeed(){
        return moveSpeed;
    }
    
    public void addBonusMS(int amount){
        bonusMS += amount;
        moveSpeed += moveSpeed*bonusMS/100f;
        oldMoveSpeed = moveSpeed;
    }
    
    public void putInBase(){
        x = xBase;
        y = yBase;
    }
    
    public void beSlowedBy(float slow){
        startTimeSlow = game.timeInGamePassed;
        slowedBy = slow;
    }
    
    @Override
    public int compareTo(Enemy e){
        if(eBalance == e.eBalance)
            return 0;
        else if(eBalance < e.eBalance)
            return -1;
        else
            return 1;
    }

    public Clip getClipWalk(){
        return clipWalk;
    }
    
    @Override
    public ArrayList<Shootable> getEnemies(){
        return game.towers;
    }
    
    public int getIndiceTuile(){
        return indiceTuile;
    }
    
    public double getStepEveryMilli(){
        return stepEveryMilli;
    }
    
    public void setIndiceTuile(int i){
        indiceTuile = i;
    }
    
    public void setDir(String d){
        dir = d;
    }
    
    public void stopFor(int t){
        stopFor = t;
        startTimeStopFor = game.timeInGamePassed;
    }
    
    public boolean isAimed(){
        return isAimed;
    }
}
