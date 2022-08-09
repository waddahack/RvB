package towers;

import Utils.MyMath;
import ennemies.Enemy;
import java.util.ArrayList;
import org.newdawn.slick.opengl.Texture;
import towser.Towser;
import towser.Shootable;
import static towser.Towser.game;

public class Bullet{
    
    private int speed;
    private Shootable aim, shooter;
    private boolean follow, firstUpdate = true, haveWaited = false, goThrough = false, aimAlreadyTouched, cone, explode;
    private float x, y, xDest, yDest, angle, radius;
    private double waitFor, startTime;
    private Texture sprite;
    
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
        speed = shooter.getBulletSpeed();
        explode = shooter.getExplode();
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
        angle = (float) MyMath.angleDegreesBetween(x, y, this.xDest, this.yDest);
    }
    
    public void move(){
        double speed = (this.speed*game.gameSpeed * Towser.deltaTime / 50) * Towser.ref;
        double xDiffConst = xDest-shooter.getX(), yDiffConst = yDest-shooter.getY(), xDiff = xDiffConst, yDiff = yDiffConst;
        double hyp = MyMath.distanceBetween(shooter.getX(), shooter.getY(), xDest, yDest), prop = speed/hyp, angle = MyMath.angleBetween(shooter.getX(), shooter.getY(), xDest, yDest);
        boolean touched = hasTouched(angle), inRange = isInRange();
        aimAlreadyTouched = false;
        if(shooter.isMultipleShot() && aim != null && shooter.getEnemiesTouched().contains(aim))
            aimAlreadyTouched = true;
        if((!touched || aimAlreadyTouched) && inRange){
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
            
        if(!aimAlreadyTouched && touched){
            shooter.getEnemiesTouched().add(aim);
            aim.attacked(shooter.getPower());
            if(explode){
                BigTower bt = (BigTower)shooter;
                bt.bombExplode(aim.getX(), aim.getY());
                for(int i = 0 ; i < game.enemies.size() ; i++){
                    Enemy e = game.enemies.get(i);
                    if(MyMath.distanceBetween(aim, e) <= shooter.getExplodeRadius())
                        e.attacked(shooter.getPower());
                }
            }
            if(!goThrough)
                shooter.getBulletsToRemove().add(this);
        }
    }
    
    public void update(){
        if(firstUpdate){
            startTime = System.currentTimeMillis();
            firstUpdate = false;
        }
        if(System.currentTimeMillis() - startTime >= waitFor){
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
            Towser.drawFilledRectangle(x, y, (int)((2*radius)*percentToDest), (int)((2*radius)*percentToDest), sprite, angle);
        }
        else
            Towser.drawFilledRectangle(x, y, (int)(2*radius), (int)(2*radius), sprite, angle);
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
            return (x <= Towser.windWidth && x >= 0 && y <= Towser.windHeight && y >= 0 && !aim.isDead());
        return (Math.abs(x-shooter.getX()) <= shooter.getRange()*cosinus*coef && Math.abs(y-shooter.getY()) <= shooter.getRange()*sinus*coef);
    }
    
    private boolean hasTouched(double angle){
        double cosinus = Math.abs(Math.cos(angle)), sinus = Math.abs(Math.sin(angle));
        if(!follow){
            ArrayList<Enemy> ennemies = game.enemies;
            Enemy e;
            int i;
            for(i = 0 ; i < ennemies.size() ; i++){
                e = ennemies.get(i);
                if(!e.hasStarted() || (shooter.isMultipleShot() && aimAlreadyTouched))
                    continue;
                angle = MyMath.angleDegreesBetween(x, y, e.getX(), e.getY());
                cosinus = Math.abs(Math.cos(angle));
                sinus = Math.abs(Math.sin(angle));
                if(aimTouched(e, cosinus, sinus)){
                    aim = e;
                    return true;
                }
            }       
            return false;
        }
        else
            return aimTouched(aim, cosinus, sinus);
    }
    
    private boolean aimTouched(Shootable aim, double cosinus, double sinus){
        int xHitBoxPoint = (int) ((aim.getWidth()/2)*cosinus + (radius/2)*cosinus), yHitBoxPoint = (int) ((aim.getWidth()/2)*sinus + (radius/2)*sinus);
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
