package Utils;

import java.lang.Math;
import towser.Shootable;
import towser.Tile;

public class MyMath {
    
    // Distance between
    public static int distanceBetween(int x1, int y1, int x2, int y2){
        return (int)Math.sqrt((x2-x1)*(x2-x1) + (y2-y1)*(y2-y1));
    }
    
    public static double distanceBetween(double x1, double y1, double x2, double y2){
        return Math.sqrt((x2-x1)*(x2-x1) + (y2-y1)*(y2-y1));
    }
    
    public static double distanceBetween(Shootable a, Shootable b){
        return Math.sqrt((b.getX()-a.getX())*(b.getX()-a.getX()) + (b.getY()-a.getY())*(b.getY()-a.getY()));
    }
    
    public static double distanceBetween(Tile a, Tile b){
        return Math.sqrt((b.getRealX()-a.getRealX())*(b.getRealX()-a.getRealX()) + (b.getRealY()-a.getRealY())*(b.getRealY()-a.getRealY()));
    }
    
    public static double distanceBetween(Shootable a, Tile b){
        return Math.sqrt((b.getRealX()-a.getX())*(b.getRealX()-a.getX()) + (b.getRealY()-a.getY())*(b.getRealY()-a.getY()));
    }
    
    public static double distanceBetween(Tile a, Shootable b){
        return Math.sqrt((b.getX()-a.getRealX())*(b.getX()-a.getRealX()) + (b.getY()-a.getRealY())*(b.getY()-a.getRealY()));
    }
    
    // Angle between
    public static double angleDegreesBetween(double x1, double y1, double x2, double y2){
        return Math.toDegrees(Math.atan2(y2-y1, x2-x1));
    }
    
    public static double angleDegreesBetween(Shootable a, Shootable b){
        return Math.toDegrees(Math.atan2(b.getY()-a.getY(), b.getX()-a.getX()));
    }
    
    public static double angleDegreesBetween(Tile a, Tile b){
        return Math.toDegrees(Math.atan2(b.getRealY()-a.getRealY(), b.getRealX()-a.getRealX()));
    }
    
    public static double angleDegreesBetween(Shootable a, Tile b){
        return Math.toDegrees(Math.atan2(b.getRealY()-a.getY(), b.getRealX()-a.getX()));
    }
    
    public static double angleDegreesBetween(Tile a, Shootable b){
        return Math.toDegrees(Math.atan2(b.getY()-a.getRealY(), b.getX()-a.getRealX()));
    }
    
    public static double angleBetween(double x1, double y1, double x2, double y2){
        return Math.atan2(y2-y1, x2-x1);
    }
    
    public static double angleBetween(Shootable a, Shootable b){
        return Math.atan2(b.getY()-a.getY(), b.getX()-a.getX());
    }
    
    public static double angleBetween(Tile a, Tile b){
        return Math.atan2(b.getRealY()-a.getRealY(), b.getRealX()-a.getRealX());
    }
    
    public static double angleBetween(Shootable a, Tile b){
        return Math.atan2(b.getRealY()-a.getY(), b.getRealX()-a.getX());
    }
    
    public static double angleBetween(Tile a, Shootable b){
        return Math.atan2(b.getY()-a.getRealY(), b.getX()-a.getRealX());
    }
}
