package rvb;

import java.util.ArrayList;
import org.lwjgl.input.Mouse;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glRotated;
import static org.lwjgl.opengl.GL11.glTranslated;
import org.newdawn.slick.opengl.Texture;
import static rvb.RvB.unite;

public class Tile {
    
    protected float x, y;
    protected int size = unite;
    protected float r, g, b;
    protected float[] rgb;
    protected int angle = 0, newAngle = 0, arrowAngle = -1;
    protected ArrayList<Texture> textures;
    protected int rotateIndex = -1;
    protected float renderX, renderY;
    protected String direction;
    public String type;
    public Tile previousRoad = null, nextRoad = null;
    
    public Tile(String t){
        textures = new ArrayList<>();
        type = t;
        this.x = Mouse.getX();
        this.y = RvB.windHeight-Mouse.getY();
    }
    
    public Tile(Texture text, String t){
        textures = new ArrayList<>();
        textures.add(text);
        type = t;
        this.x = Mouse.getX();
        this.y = RvB.windHeight-Mouse.getY();
    }
    
    public Tile(float[] rgb, String t){
        textures = new ArrayList<>();
        this.r = r;
        this.g = g;
        this.b = b;
        this.rgb = rgb;
        type = t;
        this.x = Mouse.getX();
        this.y = RvB.windHeight-Mouse.getY();
    }
    
    public Tile(float x, float y){
        textures = new ArrayList<>();
        type = "nothing";
        this.x = x;
        this.y = y;
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
            RvB.drawFilledRectangle(x+unite/2, y+unite/2, unite/2, unite/2, RvB.textures.get("arrow"), arrowAngle);
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
    
    public ArrayList<Texture> getTextures(){
        return textures;
    }
    
    public void setTexture(Texture t){
        if(textures.size() == 1){
            textures.clear();
            textures.add(t);
        }
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
