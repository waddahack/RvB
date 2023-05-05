package ui;

import java.util.ArrayList;
import managers.TextManager.Text;
import org.lwjgl.input.Mouse;
import org.newdawn.slick.UnicodeFont;
import org.newdawn.slick.opengl.Texture;
import rvb.RvB;
import static rvb.RvB.ref;

public class Overlay {
    
    private int x, y, width, height;
    private float[] rgb = null, borderRgb = null;
    private int borderWidth;
    private Texture bg = null;
    private float a = 1, borderA = 1;
    private boolean display;
    private ArrayList<Text> texts = new ArrayList<>();
    private ArrayList<UnicodeFont> fonts = new ArrayList<>();
    private ArrayList<int[]> textPos = new ArrayList<>();
    private ArrayList<String> anchors = new ArrayList<>();
    private ArrayList<Button> buttons = new ArrayList<>();
    private ArrayList<Integer> texturesX = new ArrayList<>(), texturesY = new ArrayList<>(), texturesW = new ArrayList<>(), texturesH = new ArrayList<>(), texturesA = new ArrayList<>();
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
    
    public void setW(int w){
        width = w;
    }
    
    public void setH(int h){
        height = h;
    }
    
    public void setX(int x){
        int bX;
        for(Button b : buttons){
            bX = b.getX()-this.x;
            b.setX(bX+x);
        }
        this.x = x;
    }
    
    public void setY(int y){
        int bY;
        for(Button b : buttons){
            bY = b.getY()-this.y;
            b.setY(bY+y);
        }
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
            RvB.drawRectangle(x, y, width, height, borderRgb, borderA, (int)(borderWidth*ref));
        
        for(Button b : buttons)
            b.update();
        
        int x, y, w, h, angle;
        for(int i = 0 ; i < textures.size() ; i++){
            x = texturesX.get(i);
            y = texturesY.get(i);
            w = texturesW.get(i);
            h = texturesH.get(i);
            angle = texturesA.get(i);
            RvB.drawFilledRectangle(x, y, w, h, textures.get(i), angle, 1);
        }
        
        int top = 0;
        for(int i = 0 ; i < texts.size() ; i++){
            for(String s : texts.get(i).getLines()){
                drawText(textPos.get(i)[0], textPos.get(i)[1]+top, s, fonts.get(i), anchors.get(i));
                top += fonts.get(i).getHeight(s)+8*RvB.ref;
            }
        }
    }
    
    public void addText(Text text, UnicodeFont font, int[] pos){
        addText(text, font, pos, "center");
    }
    
    public void addText(Text text, UnicodeFont font, int[] pos, String anchor){
        texts.add(text);
        fonts.add(font);
        textPos.add(pos);
        anchors.add(anchor);
    }
    
    public void addButton(Button b){
        b.setX(this.x + b.getX());
        b.setY(this.y + b.getY());
        buttons.add(b);
    }
    
    public void addImage(int x, int y, int w, int h, Texture texture){
        addImage(x, y, w, h, texture, 0);
    }
    
    public void addImage(int x, int y, int w, int h, Texture texture, int angle){
        textures.add(texture);
        texturesX.add(this.x+x);
        texturesY.add(this.y+y);
        texturesW.add(w);
        texturesH.add(h);
        texturesA.add(angle);
    }
    
    private boolean isMouseIn(){
        int MX = Mouse.getX(), MY = RvB.windHeight-Mouse.getY();
        return (MX >= x && MX <= x+width && MY >= y && MY <= y+height);
    }
    
    public boolean isClicked(int but){
        return (isMouseIn() && Mouse.isButtonDown(but));
    }
    
    public void updateCoords(int newX, int newY){
        for(Button b : buttons){
            b.setX(b.getX()-x+newX);
            b.setY(b.getY()-y+newY);
        }
        x = newX;
        y = newY;
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
    
    public void clearButtons(){
        buttons.clear();
    } 
    
    public void clearTexts(){
        texts.clear();
        fonts.clear();
        textPos.clear();
        anchors.clear();
    } 
    
    public void clearImages(){
        textures.clear();
        texturesX.clear();
        texturesY.clear();
        texturesW.clear();
        texturesH.clear();
        texturesA.clear();
    } 

    public boolean buttonClicked(int c){
        for(Button b : buttons)
            if(b.isClicked(c))
                return true;
        
        return false;
    }
    
    public void drawImage(int x, int y, int width, int height, Texture t){
        RvB.drawFilledRectangle(this.x+x-width/2, this.y+y-height/2, width, height, rgb, 1, t);
    } 
    
    public void drawText(int x, int y, String text, UnicodeFont font) {
        drawText(x, y, text, font, "center");
    }
    
    public void drawText(int x, int y, String text, UnicodeFont font, String anchor) {
        switch(anchor){
            case "topLeft":
                RvB.drawString(this.x+x + font.getWidth(text)/2, this.y+y + font.getHeight(text)/2, text, font);
                break;
            case "topRight":
                RvB.drawString(this.x+x - font.getWidth(text)/2, this.y+y + font.getHeight(text)/2, text, font);
                break;
            case "bottomLeft":
                RvB.drawString(this.x+x + font.getWidth(text)/2, this.y+y - font.getHeight(text)/2, text, font);
                break;
            case "bottomRight":
                RvB.drawString(this.x+x - font.getWidth(text)/2, this.y+y - font.getHeight(text)/2, text, font);
                break;
            case "midLeft":
                RvB.drawString(this.x+x + font.getWidth(text)/2, this.y+y, text, font);
                break;
            case "midRight":
                RvB.drawString(this.x+x - font.getWidth(text)/2, this.y+y, text, font);
                break;
            case "topMid":
                RvB.drawString(this.x+x, this.y+y + font.getHeight(text)/2, text, font);
                break;
            case "bottomMid":
                RvB.drawString(this.x+x, this.y+y - font.getHeight(text)/2, text, font);
                break;
            case "center":
                RvB.drawString(this.x+x, this.y+y, text, font);
                break;
        }
    }
}
