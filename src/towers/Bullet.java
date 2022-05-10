package towers;

import ennemies.Enemy;
import java.util.ArrayList;
import org.newdawn.slick.opengl.Texture;
import towser.Game;
import towser.Towser;
import towser.Shootable;
import static towser.Towser.game;

public class Bullet{
    
    private double radius;
    private int speed;
    private Shootable aim, shooter;
    private boolean follow, firstUpdate = true, haveWaited = false, goThrough = false;
    private double x, y, xDest, yDest, waitFor, startTime;
    private Texture sprite;
    
    public Bullet(Shootable shooter, Shootable aim, double radius, Texture sprite, boolean goThrought){
        this.x = shooter.getX();
        this.y = shooter.getY();
        this.radius = radius;
        speed = shooter.getBulletSpeed();
        follow = shooter.getFollow();
        this.aim = aim;
        xDest = aim.getX();
        yDest = aim.getY();
        this.sprite = sprite;
        this.shooter = shooter;
        this.goThrough = goThrough;
    }
    
    public Bullet(Shootable shooter, double xDest, double yDest, double radius, Texture sprite, boolean goThrought, int waitFor){
        this.x = shooter.getX();
        this.y = shooter.getY();
        this.radius = radius;
        speed = shooter.getBulletSpeed();
        follow = false;
        this.aim = null;
        this.xDest = xDest;
        this.yDest = yDest;
        this.sprite = sprite;
        this.shooter = shooter;
        this.waitFor = waitFor;
        this.goThrough = goThrough;
    }
    
    public void move(){
        int speed = this.speed*game.gameSpeed;
        double xDiffConst = xDest-shooter.getX(), yDiffConst = yDest-shooter.getY(), xDiff = xDiffConst, yDiff = yDiffConst;
        double hyp = Math.sqrt(xDiffConst*xDiffConst + yDiffConst*yDiffConst), prop = speed/hyp, angle = Math.atan2(yDiff, xDiff);
        boolean touched = hasTouched(angle), inRange = isInRange();
        boolean aimAlreadyTouched = false;
        if(shooter.isMultipleShot() && aim != null && shooter.getEnemiesTouched().contains(aim))
            aimAlreadyTouched = true;
        if((!touched || aimAlreadyTouched) && inRange){
            if(follow){
                xDiff = aim.getX()-x;
                yDiff = aim.getY()-y;
                angle = Math.atan2(yDiff, xDiff);
                hyp = Math.sqrt(xDiff*xDiff + yDiff*yDiff);
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
        Towser.drawFilledRectangle(x-radius, y-radius, (int)(2*radius), (int)(2*radius), null, 1, sprite);
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
        double xDiff = xDest-shooter.getX(), yDiff = yDest-shooter.getY();
        double angle = Math.atan2(yDiff, xDiff), cosinus = Math.abs(Math.cos(angle)), sinus = Math.abs(Math.sin(angle)), coef = 1.5;
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
            double xDiff, yDiff;
            for(i = 0 ; i < ennemies.size() ; i++){
                e = ennemies.get(i);
                if(shooter.isMultipleShot() && shooter.getEnemiesTouched().contains(e))
                    continue;
                xDiff = e.getX()-x;
                yDiff = e.getY()-y;
                angle = Math.atan2(yDiff, xDiff);
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
        int xHitBoxPoint = (int) ((aim.getWidth()-20)*cosinus), yHitBoxPoint = (int) ((aim.getWidth()-20)*sinus);
        if(x-radius <= aim.getX()+xHitBoxPoint && x-radius >= aim.getX()-xHitBoxPoint && y <= aim.getY()+yHitBoxPoint && y >= aim.getY()-yHitBoxPoint)
            return true;
        if(x+radius <= aim.getX()+xHitBoxPoint && x+radius >= aim.getX()-xHitBoxPoint && y <= aim.getY()+yHitBoxPoint && y >= aim.getY()-yHitBoxPoint)
            return true;
        if(x <= aim.getX()+xHitBoxPoint && x >= aim.getX()-xHitBoxPoint && y-radius <= aim.getY()+yHitBoxPoint && y-radius >= aim.getY()-yHitBoxPoint)
            return true;
        if(x <= aim.getX()+xHitBoxPoint && x >= aim.getX()-xHitBoxPoint && y+radius <= aim.getY()+yHitBoxPoint && y+radius >= aim.getY()-yHitBoxPoint)
            return true;
        return false;
    }
}
