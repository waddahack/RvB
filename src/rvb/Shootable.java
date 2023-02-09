package rvb;

import ennemies.Enemy;
import java.util.ArrayList;
import java.util.Random;
import javax.sound.sampled.Clip;
import managers.SoundManager;
import managers.TextManager.Text;
import org.newdawn.slick.opengl.Texture;
import static rvb.RvB.game;
import static rvb.RvB.unite;
import towers.Bullet;
import Utils.MyMath;
import org.lwjgl.input.Mouse;

public abstract class Shootable {
    
    protected boolean canShoot = true, started = false, follow = false, isMultipleShot = false, continuousSound = false, soundPlayed = false, mouseEntered = false, isAimed = false;
    protected ArrayList<Bullet> bullets, bulletsToRemove;
    protected Clip clip;
    protected SoundManager.Volume volume = SoundManager.Volume.SEMI_LOW;
    protected Texture textureStatic, bulletSprite;
    protected Random random = new Random();
    protected int reward = 0, rotateIndex = -1, lastShoot = 0, bulletSizeBonus = 0, hitboxWidth, focusIndex;
    protected float newAngle = 0;
    protected ArrayList<Texture> textures, texturesBright;
    protected Shootable enemyAimed;
    // CARACHTERISTIQUES PUBLIQUES
    public float x, y, angle = 180;
    public int waitFor = 175, startTimeWaitFor = 0;
    public Text name;
    public int range = 0, bulletSpeed = 0, size = unite, explodeRadius;
    public float shootRate = 0, power = 0, slow = 0, life, maxLife;
    public float bonusPower = 0, bonusShootRate = 0, bonusBulletSpeed = 0, bonusRange = 0, bonusExplodeRadius = 0, bonusLife = 0;
    // STATS
    public int damagesDone = 0, enemiesKilled = 0, moneyGained = 0;
    
    public Shootable(){
        textures = new ArrayList<>();
        texturesBright = new ArrayList<>();
        bullets = new ArrayList<>();
        bulletsToRemove = new ArrayList<>();
    }

    protected void initBack(){
        life = (int)Math.round(life + (life*bonusLife/100));
        maxLife = life;
        SoundManager.Instance.setClipVolume(clip, volume);
    }
    
    public void update(){
        // Mouse hover
        if(isMouseIn() && !mouseEntered && RvB.cursor != RvB.Cursor.GRAB){
            RvB.setCursor(RvB.Cursor.POINTER);
            mouseEntered = true;
        }
        else if(!isMouseIn() && mouseEntered && RvB.cursor != RvB.Cursor.GRAB){
            RvB.setCursor(RvB.Cursor.DEFAULT);
            mouseEntered = false;
        } 
        
        if(!canShoot)
            return;
        
        searchAndShoot(getEnemies());
        updateBullets();

        if(soundPlayed && (enemyAimed == null || enemyAimed.isDead())){
            SoundManager.Instance.stopClip(clip);
            soundPlayed = false;
        }   
    }
    
    public void render(){
        if(startTimeWaitFor != 0 && game.timeInGamePassed - startTimeWaitFor < waitFor){
            for(int i = 0 ; i < texturesBright.size() ; i++)
                RvB.drawFilledRectangle(x, y, size, size, texturesBright.get(i), i == rotateIndex ? (int)angle : 0, 1);
        }
        else{
            startTimeWaitFor = 0;
            for(int i = 0 ; i < textures.size() ; i++)
                RvB.drawFilledRectangle(x, y, size, size, textures.get(i), i == rotateIndex ? (int)angle : 0, 1);
        }
    }
    
    public ArrayList<Bullet> getBullets(){
        return bullets;
    }
    
    public ArrayList<Bullet> getBulletsToRemove(){
        return bulletsToRemove;
    }
    
    public void attack(Shootable shootable){
        if(!shootable.hasStarted())
            return;
        damagesDone += shootable.takeDamage(getPower());
        if(shootable.isDead()){
            enemiesKilled += 1;
            moneyGained += shootable.getReward();
        }
    }
    
    public float takeDamage(float power){
        if(!started)
            return 0;

        float damageDone = power;
        life -= power;
        
        if(life <= 0){
            damageDone += life;
            die();
        }   
        
        startTimeWaitFor = game.timeInGamePassed;
        
        return damageDone;
    }
    
    public void searchAndShoot(ArrayList<Shootable> enemies){
        if(!game.inWave || game.gameSpeed <= 0)
            return;
        
        if(enemies != null && !enemies.isEmpty()){
            switch(focusIndex){
                case 0: // Dans l'ordre du text (cf TextManager)
                    // /!\ Enemy CAN'T FOCUS FIRST /!\
                    aim(searchForFirst(enemies));
                    break;
                case 1:
                    // /!\ Enemy CAN'T FOCUS LAST /!\
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
                    aim(searchForAnyone(enemies));
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
    
    // FOR TOWERS ONLY
    protected Shootable searchForFirst(ArrayList<Shootable> enemies){
        Enemy first = null;
        for(int i = 0 ; i < enemies.size() ; i++){
            Enemy e = (Enemy) enemies.get(i);
            if(e.hasStarted() && e.isInRangeOf(this)){
                if(first == null || e.getIndiceTuile() > first.getIndiceTuile() || (e.getIndiceTuile() == first.getIndiceTuile() && e.getMoveSpeed() > first.getMoveSpeed()))
                    first = e;
            }
        }
        return first;
    }
    
    // FOR TOWERS ONLY
    protected Shootable searchForLast(ArrayList<Shootable> enemies){
        Enemy last = null;
        for(int i = 0 ; i < enemies.size() ; i++){
            Enemy e = (Enemy) enemies.get(i);
            if(e.hasStarted() && e.isInRangeOf(this)){
                if(last == null || e.getIndiceTuile() < last.getIndiceTuile() || (e.getIndiceTuile() == last.getIndiceTuile() && e.getMoveSpeed() < last.getMoveSpeed()))
                    last = e;
            }
        }   
        return last;
    }
    
    protected Shootable searchForStrongest(ArrayList<Shootable> enemies){
        Shootable strongest = null;
        for(int i = 0 ; i < enemies.size() ; i++)
            if(enemies.get(i).hasStarted() && enemies.get(i).isInRangeOf(this)){
                if(strongest == null || enemies.get(i).getMaxLife() > strongest.getMaxLife())
                    strongest = enemies.get(i);
            }
        return strongest;
    }
    
    protected Shootable searchForWeakest(ArrayList<Shootable> enemies){
        Shootable weakest = null;
        for(int i = 0 ; i < enemies.size() ; i++)
            if(enemies.get(i).hasStarted() && enemies.get(i).isInRangeOf(this)){
                if(weakest == null || enemies.get(i).getMaxLife() < weakest.getMaxLife())
                    weakest = enemies.get(i);
            }
        return weakest;
    }
    
    protected Shootable searchForClosest(ArrayList<Shootable> enemies){
        Shootable closest = null;
        float minDist = 10000000;
        for(int i = 0 ; i < enemies.size() ; i++)
            if(enemies.get(i).hasStarted() && enemies.get(i).isInRangeOf(this)){
                if(closest == null || MyMath.distanceBetween(this, enemies.get(i)) <= minDist){
                    closest = enemies.get(i);
                    minDist = (float)MyMath.distanceBetween(this, enemies.get(i));
                }
                    
            }
        return closest;
    }
    
    protected Shootable searchForAnyone(ArrayList<Shootable> enemies){
        for(int i = 0 ; i < enemies.size() ; i++)
            if(enemies.get(i).hasStarted() && enemies.get(i).isInRangeOf(this)){
                return enemies.get(i); 
            }
        return null;
    }
    
    public void aim(Shootable s){
        if(s == null && enemyAimed != null)
            enemyAimed.setIsAimed(false);
        enemyAimed = s;
        if(s == null)
            return;
        s.setIsAimed(true);
        if(rotateIndex >= 0){
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
    
    public void updateBullets(){
        for(Bullet b : bulletsToRemove)
            bullets.remove(b);

        bulletsToRemove.clear();
        
        for(Bullet b : bullets)
            b.update();
    }
    
    public float getMaxLife(){
        return maxLife;
    }
    
    public boolean isInRangeOf(Shootable s){
        double angle, cosinus, sinus;
        angle = MyMath.angleBetween(this, (Shootable) s);
        cosinus = Math.floor(Math.cos(angle)*1000)/1000;
        sinus = Math.floor(Math.sin(angle)*1000)/1000;
        return (x <= s.getX()+((s.getRange())*Math.abs(cosinus)) && x >= s.getX()-((s.getRange())*Math.abs(cosinus)) && y <= s.getY()+((s.getRange())*Math.abs(sinus)) && y >= s.getY()-((s.getRange())*Math.abs(sinus)));
    }
    
    public String getName(){
        return name.getText();
    }
    
    public float getX(){
        return x;
    }
    
    public float getY(){
        return y;
    }
    
    public int getIndexX(){
        return (int)(x/unite);
    }
    
    public int getIndexY(){   
        return (int)(y/unite);
    }
    
    public void setX(float x){
        this.x = x;
    }
    
    public void setY(float y){
        this.y = y;
    }

    public float getPower(){
        return (float)(Math.round(power*(1+bonusPower)*10f)/10f);
    }
    
    public float getShootRate(){
        return (float)(Math.round(shootRate*(1+bonusShootRate)*10f)/10f);
    }
    
    public boolean isMultipleShot(){
        return isMultipleShot;
    }
    
    public float getRoundedLife(){
        return Math.round(life*10)/10;
    }
    
    protected boolean isInWindow() {
        return (x < RvB.windWidth && x > 0 && y < RvB.windHeight && y > 0);
    }
    
    public boolean hasStarted(){
        return started;
    }
    
    public void setStarted(boolean v){
        started = v;
    }
    
    public void setIsAimed(boolean b){
        isAimed = b;
    }
    
    public int getReward(){
        return reward;
    }
    
    public float getLife(){
        return life;
    }
    
    public void setRange(int range){
        this.range = range;
    }
    
    public int getRange(){
        return Math.round(range*(1+bonusRange));
    }
    
    public boolean isMouseIn(){
        int MX = Mouse.getX(), MY = RvB.windHeight-Mouse.getY();
        return (MX >= x-unite/2 && MX <= x+unite/2 && MY >= y-unite/2 && MY <= y+unite/2);
    }
    
    public boolean isClicked(int but){
        return (isMouseIn() && Mouse.isButtonDown(but));
    }
    
    public void die(){
        life = 0;
        if(clip != null){
            clip.stop();
            SoundManager.Instance.clipToClose(clip);
        } 
    }
    
    public void addBonusLife(int amount){
        bonusLife += amount;
        life = (int)Math.round(life + (life*bonusLife/100));
        maxLife = life;
    }
    
    public boolean isSoundContinuous(){
        return continuousSound;
    }
    
    public boolean isSelected(){
        return game.towerSelected == this;
    }
    
    public int getSize(){
        return size;
    }
    
    public int getHitboxWidth(){
        return hitboxWidth;
    }
    
    public void setFollow(boolean b){
        follow = b;
    }
    
    public float getSlow(){
        return slow;
    }
    
    public void addSlowAmount(float percentage){
        slow += percentage;
    }
    
    public Clip getClip(){
        return clip;
    }
    
    public int getBulletSpeed(){
        return bulletSpeed;
    }
    
    public Shootable getEnemyAimed(){
        return enemyAimed;
    }
    
    public ArrayList<Shootable> getEnemies(){
        return null;
    }
    
    public boolean getFollow(){
        return follow;
    }
    
    public boolean canShoot(){
        return canShoot && (game.timeInGamePassed-lastShoot >= 1000/getShootRate() && (angle >= newAngle-6 && angle <= newAngle+6));
    }
    
    public void shoot(){
        lastShoot = game.timeInGamePassed;
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
    
    public boolean isDead(){
        return (life <= 0);
    }
}
