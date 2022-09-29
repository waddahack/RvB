package ui;

import java.util.ArrayList;
import org.lwjgl.input.Mouse;
import org.newdawn.slick.UnicodeFont;
import org.newdawn.slick.opengl.Texture;
import rvb.RvB;

public class Overlay {
    
    private int x, y, width, height;
    private float[] rgb = null, borderRgb = null;
    private int borderWidth;
    private Texture bg = null;
    private float a = 1, borderA = 1;
    private boolean display;
    private ArrayList<Button> buttons = new ArrayList<>();
    private ArrayList<Integer> texturesX = new ArrayList<>(), texturesY = new ArrayList<>(), texturesW = new ArrayList<>(), texturesH = new ArrayList<>();
    private ArrayList<Texture> textures;
    /***
     * Creates an overlay
     * @param x Position x of the top left corner
     * @param y Position y of the top left corner
     * @param width Width of the overlay
     * @param height Height of the overlay
     */
    public Overlay(int x, int y, int width, int height){
        this.textures = new ArrayList<>();
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        display = true;
    }
    
    public void setBG(Texture bg, float a){
        this.bg = bg;
        this.a = a;
    }
    
    public void setRGB(float[] rgb){
        this.rgb = rgb;
    }
    
    public void setRGBA(float[] rgb, float a){
        this.rgb = rgb;
        this.a = a;
    }
    
    public void setBorder(float[] rgb, int width, float a){
        this.borderRgb = rgb;
        this.borderWidth = width;
        this.borderA = a;
    }
    
    public void setX(int x){
        this.x = x;
    }
    
    public void setY(int y){
        this.y = y;
    }
    
    public void setA(float a){
        this.a = a;
    }
    
    public void setBorderA(float borderA){
        this.borderA = borderA;
    }
    
    public void render(){
        if(!display)
            return;
        if(bg != null)
            RvB.drawFilledRectangle(x, y, width, height, null, a, bg);
        else if(rgb != null)
            RvB.drawFilledRectangle(x, y, width, height, rgb, a, null);
        if(borderRgb != null)
            RvB.drawRectangle(x, y, width, height, borderRgb, borderA, borderWidth);
        
        for(Button b : buttons)
            b.update();
        
        int x, y, w, h;
        for(int i = 0 ; i < textures.size() ; i++){
            x = texturesX.get(i);
            y = texturesY.get(i);
            w = texturesW.get(i);
            h = texturesH.get(i);
            RvB.drawFilledRectangle(x-w/2, y-h/2, w, h, null, 1, textures.get(i));
        }
    }
   
    public void addButton(Button b){
        b.setX(this.x + b.getX());
        b.setY(this.y + b.getY());
        buttons.add(b);
    }
    
    public void addImage(int x, int y, int w, int h, Texture texture){
        textures.add(texture);
        texturesX.add(this.x+x);
        texturesY.add(this.y+y);
        texturesW.add(w);
        texturesH.add(h);
    }
    
    private boolean isMouseIn(){
        int MX = Mouse.getX(), MY = RvB.windHeight-Mouse.getY();
        return (MX >= x && MX <= x+width && MY >= y && MY <= y+height);
    }
    
    public boolean isClicked(int but){
        return (isMouseIn() && Mouse.isButtonDown(but));
    }
    
    public void updateCoords(int xChange, int yChange){
        x += xChange;
        y += yChange;
        for(Button b : buttons){
            b.setX(b.getX()+xChange);
            b.setY(b.getY()+yChange);
        }
    }
    
    public int getX(){
        return x;
    }
    
    public int getY(){
        return y;
    }
    
    public int getW(){
        return width;
    }
    
    public int getH(){
        return height;
    }
    
    public boolean isDisplayed(){
        return display;
    }
    
    public void display(boolean d){
        display = d;
    }
    
    public ArrayList<Button> getButtons(){
        return buttons;
    } 

    public boolean buttonClicked(int c){
        for(Button b : buttons)
            if(b.isClicked(c))
                return true;
        
        return false;
    }
    
    public void drawImage(int x, int y, int width, int height, Texture t){
        RvB.drawFilledRectangle(this.x+x, this.y+y, width, height, rgb, 1, t);
    }
    
    public void drawText(int x, int y, String text, UnicodeFont font) {
        RvB.drawString(this.x+x, this.y+y, text, font);
    }
}
