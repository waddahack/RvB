package towers;

import Utils.MyMath;
import java.util.ArrayList;
import org.newdawn.slick.opengl.Texture;
import rvb.RvB;
import rvb.Shootable;
import static rvb.RvB.game;

public class Bullet{
    
    private int speed;
    private Shootable aim, shooter;
    private boolean follow, firstUpdate = true, haveWaited = false, goThrough = false, cone;
    private float x, y, xDest, yDest, angle, radius;
    private double waitFor, startTime;
    private Texture sprite;
    private ArrayList<Shootable> enemies; 
    private ArrayList<Shootable> enemiesTouched;
    
    public Bullet(Shootable shooter, float xStart, float yStart, Shootable aim, float radius, Texture sprite, boolean goThrough){ // basic tower - big tower
        build(shooter, xStart, yStart, aim, 0, 0, radius, sprite, goThrough, false, 0);
    }
    
    public Bullet(Shootable shooter, float xStart, float yStart, float xDest, float yDest, float radius, Texture sprite, boolean goThrough, int waitFor){ // circle tower
        build(shooter, xStart, yStart, null, xDest, yDest, radius, sprite, goThrough, false, waitFor);
    }
    
    public Bullet(Shootable shooter, float xStart, float yStart, float xDest, float yDest, float radius, Texture sprite, boolean goThrough, boolean cone){ // flame tower
        build(shooter, xStart, yStart, null, xDest, yDest, radius, sprite, goThrough, cone, 0);
    }
    
    public void build(Shootable shooter, float xStart, float yStart, Shootable aim, float xDest, float yDest, float radius, Texture sprite, boolean goThrough, boolean cone, int waitFor){
        this.shooter = shooter;
        this.x = xStart;
        this.y = yStart;
        this.radius = radius;
        enemies = shooter.getEnemies();
        enemiesTouched = new ArrayList<>();
        speed = shooter.getBulletSpeed();
        follow = shooter.getFollow();
        this.aim = aim;
        if(aim == null){
            this.xDest = xDest;
            this.yDest = yDest;
        }
        else{
            this.xDest = aim.getX();
            this.yDest = aim.getY();
        } 
        this.sprite = sprite;
        this.goThrough = goThrough;
        this.cone = cone;
        this.waitFor = waitFor;
        angle = (float) MyMath.angleDegreesBetween(shooter.getX(), shooter.getY(), this.xDest, this.yDest);
    }
    
    public void move(){
        double speed = (this.speed*game.gameSpeed * RvB.deltaTime / 50) * RvB.ref;
        double xDiffConst = xDest-shooter.getX(), yDiffConst = yDest-shooter.getY(), xDiff = xDiffConst, yDiff = yDiffConst;
        double hyp = MyMath.distanceBetween(shooter.getX(), shooter.getY(), xDest, yDest), prop = speed/hyp, angle = MyMath.angleBetween(shooter.getX(), shooter.getY(), xDest, yDest);
        Shootable enemyTouched = hasTouched(angle);
        boolean inRange = isInRange();
        if((enemyTouched == null || goThrough) && inRange){
            if(follow){
                xDiff = aim.getX()-x;
                yDiff = aim.getY()-y;
                this.angle = (float) MyMath.angleDegreesBetween(x, y, aim.getX(), aim.getY());
                hyp = MyMath.distanceBetween(x, y, aim.getX(), aim.getY());
                prop = speed/hyp;
                x += xDiff*prop;
                y += yDiff*prop;
            }
            else{
                x += xDiffConst*prop;
                y += yDiffConst*prop; 
            }
        }
        if(!inRange)
            shooter.getBulletsToRemove().add(this);
            
        if(enemyTouched != null){
            enemiesTouched.add(enemyTouched);
            shooter.attack(enemyTouched);
                
            if(!goThrough)
                shooter.getBulletsToRemove().add(this);
        }
    }
    
    public void update(){
        if(firstUpdate){
            startTime = game.timeInGamePassed;
            firstUpdate = false;
        }
        if(game.timeInGamePassed - startTime >= waitFor){
            haveWaited = true;
        }
        if(haveWaited){
            move();
            render();
        }
    }
    
    private void render(){
        if(cone){
            // distanceDone / totalDistance
            float distanceDone = (float) (MyMath.distanceBetween(x, y, shooter.getX(), shooter.getY()));
            float totalDistance = (float) shooter.getRange();
            float percentToDest = (float) (distanceDone / totalDistance);
            if(percentToDest < 0.2)
                percentToDest = 0.2f;
            RvB.drawFilledRectangle(x, y, (int)((2*radius)*percentToDest), (int)((2*radius)*percentToDest), sprite, angle, 1);
        }
        else
            RvB.drawFilledRectangle(x, y, (int)(2*radius), (int)(2*radius), sprite, angle, 1);
    }
    
    public double getX(){
        return x;
    }
    
    public double getY(){
        return y;
    }
    
    public double getRadius(){
        return radius;
    }
    
    private boolean isInRange(){
        double angle = MyMath.angleBetween(shooter.getX(), shooter.getY(), xDest, yDest), cosinus = Math.abs(Math.cos(angle)), sinus = Math.abs(Math.sin(angle)), coef = 1.5;
        if(shooter.isMultipleShot())
            coef = 1;
        if(follow)
            return (x <= RvB.windWidth && x >= 0 && y <= RvB.windHeight && y >= 0 && !aim.isDead());
        return (Math.abs(x-shooter.getX()) <= shooter.getRange()*cosinus*coef+RvB.unite/2 && Math.abs(y-shooter.getY()) <= shooter.getRange()*sinus*coef+RvB.unite/2);
    }
    
    private Shootable hasTouched(double angle){
        double cosinus = Math.abs(Math.cos(angle)), sinus = Math.abs(Math.sin(angle));
        if(!follow){
            for(Shootable e : enemies){
                if(!e.hasStarted() || enemiesTouched.contains(e))
                    continue;
                angle = MyMath.angleDegreesBetween(x, y, e.getX(), e.getY());
                cosinus = Math.abs(Math.cos(angle));
                sinus = Math.abs(Math.sin(angle));
                if(aimTouched(e, cosinus, sinus))
                    return e;
            }       
            return null;
        }
        else if(aimTouched(aim, cosinus, sinus))
            return aim;
        return null;
    }
    
    private boolean aimTouched(Shootable aim, double cosinus, double sinus){
        int xHitBoxPoint = (int) ((aim.getHitboxWidth()/2)*cosinus + (radius/2)*cosinus), yHitBoxPoint = (int) ((aim.getHitboxWidth()/2)*sinus + (radius/2)*sinus);
        if(x <= aim.getX()+xHitBoxPoint && x >= aim.getX()-xHitBoxPoint && y <= aim.getY()+yHitBoxPoint && y >= aim.getY()-yHitBoxPoint)
            return true;
        if(x <= aim.getX()+xHitBoxPoint && x >= aim.getX()-xHitBoxPoint && y <= aim.getY()+yHitBoxPoint && y >= aim.getY()-yHitBoxPoint)
            return true;
        if(x <= aim.getX()+xHitBoxPoint && x >= aim.getX()-xHitBoxPoint && y <= aim.getY()+yHitBoxPoint && y >= aim.getY()-yHitBoxPoint)
            return true;
        if(x <= aim.getX()+xHitBoxPoint && x >= aim.getX()-xHitBoxPoint && y <= aim.getY()+yHitBoxPoint && y >= aim.getY()-yHitBoxPoint)
            return true;
        return false;
    }
}
