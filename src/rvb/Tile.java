package rvb;

import org.lwjgl.input.Mouse;
import org.newdawn.slick.opengl.Texture;
import static rvb.RvB.unite;

public class Tile {
    
    protected float x, y;
    protected int size = unite;
    protected float r, g, b;
    protected float[] rgb;
    protected int angle = 0, newAngle = 0, arrowAngle = -1;
    protected Texture texture, stepTexture = null;
    protected int rotateIndex = -1;
    protected float renderX, renderY;
    protected String direction;
    protected int nbStepped = 0;
    public String type;
    public Tile previousRoad = null, nextRoad = null;
    
    public Tile(String t){
        texture = null;
        type = t;
        setX(Mouse.getX());
        setY(RvB.windHeight-Mouse.getY());
    }
    
    public Tile(Texture text, String t){
        texture = text;
        type = t;
        setX(Mouse.getX());
        setY(RvB.windHeight-Mouse.getY());
    }
    
    public Tile(float[] rgb, String t){
        texture = null;
        this.r = r;
        this.g = g;
        this.b = b;
        this.rgb = rgb;
        type = t;
        setX(Mouse.getX());
        setY(RvB.windHeight-Mouse.getY());
    }
    
    public Tile(float x, float y){
        texture = null;
        type = "nothing";
        setX(x);
        setY(y);
    }
    
    public Tile(Texture text, String t, float x, float y){
        texture = text;
        type = t;
        setX(x);
        setY(y);
    }
    
    public void render(){
        RvB.drawFilledRectangle(renderX, renderY, size, size, texture, angle, 1);
    }
    
    public void renderSteps(){
        if(stepTexture != null)
            RvB.drawFilledRectangle(renderX, renderY, size, size, stepTexture, angle, 1);
    }
    
    public void setPreviousRoad(Tile road){
        previousRoad = road;
        setDirection();
    }
    
    public void setNextRoad(Tile road){
        nextRoad = road;
        setDirection();
    }
    
    public String getDirection(){
        return direction;
    }
    
    public boolean isRoadTurn(){
        return nextRoad.getIndexX() != previousRoad.getIndexX() && nextRoad.getIndexY() != previousRoad.getIndexY();
    }
    
    public void stepped(){
        nbStepped++;
        if(nbStepped == 1)
            stepTexture = RvB.textures.get("steps0" + (isRoadTurn() ? "Turn" : ""));
        else if(nbStepped == 15)
            stepTexture = RvB.textures.get("steps1" + (isRoadTurn() ? "Turn" : ""));
        else if(nbStepped == 50)
            stepTexture = RvB.textures.get("steps2" + (isRoadTurn() ? "Turn" : ""));
        else if(nbStepped == 150)
            stepTexture = RvB.textures.get("steps3" + (isRoadTurn() ? "Turn" : ""));
        else if(nbStepped == 300)
            stepTexture = RvB.textures.get("steps4" + (isRoadTurn() ? "Turn" : ""));
    }
    
    public void setDirectionWithPos(){
        if(nextRoad == null){
            arrowAngle = -1;
            direction = null;
        }   
        else if(nextRoad.getY() > y){
            arrowAngle = 180;
            direction = "down";
        }
        else if(nextRoad.getY() < y){
            arrowAngle = 0;
            direction = "up";
        }  
        else if(nextRoad.getX() > x){
            arrowAngle = 90;
            direction = "right";
        }  
        else if(nextRoad.getX() < x){
            arrowAngle = 270;  
            direction = "left";
        }
    }
    
    public void setDirection(){
        if(nextRoad == null){
            arrowAngle = -1;
            direction = null;
        }   
        else if(nextRoad.getIndexY() > getIndexY()){
            arrowAngle = 180;
            direction = "down";
        }
        else if(nextRoad.getIndexY() < getIndexY()){
            arrowAngle = 0;
            direction = "up";
        }  
        else if(nextRoad.getIndexX() > getIndexX()){
            arrowAngle = 90;
            direction = "right";
        }  
        else if(nextRoad.getIndexX() < getIndexX()){
            arrowAngle = 270;  
            direction = "left";
        }
    }
    
    public void renderDirection(){
        if(arrowAngle != -1)
            RvB.drawFilledRectangle(x+unite/2, y+unite/2, unite/2, unite/2, RvB.textures.get("arrow"), arrowAngle, 1);
    }
    
    public int getIndexX(){
        return (int)(x/unite);
    }
    
    public int getIndexY(){   
        return (int)(y/unite);
    }
    
    public float getX(){
        return x;
    }
    
    public float getY(){
        return y;
    }
    
    public float getRealX(){
        return renderX;
    }
    
    public float getRealY(){
        return renderY;
    }
    
    public void setX(float x){
        this.x = x;
        renderX = Math.floorDiv((int)x, unite)*unite+unite/2;
    }
    
    public void setY(float y){
        this.y = y;
        renderY = Math.floorDiv((int)y, unite)*unite+unite/2;
    }
    
    public Texture getTexture(){
        return texture;
    }
    
    public void setTexture(Texture t){
        texture = t;
    }
    
    public void setRotateIndex(int i){
        rotateIndex = i;
    }
    
    public String getType(){
        return type;
    }
    
    public double getAngle(){
        return angle;
    }
    
    public void setAngle(int a){
        angle = a;
    }
    
    public float getR(){
        return r;
    }
    
    public float getG(){
        return g;
    }
    
    public float getB(){
        return b;
    }
}
