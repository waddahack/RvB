package ui;

import javax.sound.sampled.Clip;
import managers.SoundManager;
import managers.TextManager.Text;
import org.lwjgl.input.Mouse;
import org.newdawn.slick.UnicodeFont;
import org.newdawn.slick.opengl.Texture;
import rvb.RvB;
import rvb.RvB.Cursor;
import java.util.function.Consumer;
import static rvb.RvB.mouseDown;
import static rvb.RvB.ref;


public class Button {
    
    private int x, y, width, height, nbClicks = 0, nbClicksMax;
    private Text text;
    private float[] rgb;
    private float[] borderRgb;
    private UnicodeFont font;
    private Texture bg = null;
    private boolean hidden = false, disabled = false, locked = false;
    private boolean mouseEntered = false;
    private boolean itemFramed = false;
    private Clip click;
    private boolean clickSound = true;
    private boolean selected = false;
    public int indexSwitch = -1;
    private Consumer<Object> clickFunction;
    
    public Button(int x, int y, int width, int height, float[] rgb, float[] borderRgb){
        build(x ,y, width, height, null, null, null, rgb, borderRgb, 0);
    }
    
    public Button(int x, int y, int width, int height, float[] rgb, float[] borderRgb, int nbClicksMax){
        build(x ,y, width, height, null, null, null, rgb, borderRgb, nbClicksMax);
    }
    
    public Button(int x, int y, int width, int height, Texture bg, float[] rgb, float[] borderRgb){
        build(x ,y, width, height, null, null, bg, rgb, borderRgb, 0);
    }
    
    public Button(int x, int y, int width, int height, Text text, UnicodeFont font, Texture bg, float[] borderRgb){
        build(x ,y, width, height, text, font, bg, null, borderRgb, 0);
    }
    
    public Button(int x, int y, int width, int height, Text text, UnicodeFont font, float[] rgb, float[] borderRgb, int nbClicksMax){
        build(x ,y, width, height, text, font, null, rgb, borderRgb, nbClicksMax);
    }
    
    /**
     * Creates a button
     * @param x Position x of the center of the button
     * @param y Position y of the center of the button
     * @param width Width
     * @param height Height
     * @param text Text
     * @param bgName Background name, null if no background
     * @param borderRgb RGB of the borders on mouse hover, null if no borders
     */
    private void build(int x, int y, int width, int height, Text text, UnicodeFont font, Texture bg, float[] rgb, float[] borderRgb, int nbClicksMax){
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.text = text;
        this.font = font;
        this.nbClicksMax = nbClicksMax;
        this.rgb = rgb;
        this.borderRgb = borderRgb;
        this.bg = bg;
        click = SoundManager.Instance.getClip("click");
        SoundManager.Instance.setClipVolume(click, SoundManager.Volume.SEMI_HIGH);
    }
    
    public void setFunction(Consumer<Object> function){
        clickFunction = function;
    }
    
    public void setSwitch(){
        indexSwitch = 0;
    }
    
    public boolean click(){
        if(locked || disabled)
            return false;
        nbClicks++;
        if(clickSound)
            SoundManager.Instance.playOnce(click);
        if(nbClicks == nbClicksMax)
            hidden = true;
        if(indexSwitch >= 0)
            indexSwitch = indexSwitch == text.getLines().length-1 ? 0 : indexSwitch+1;
        if(clickFunction != null)
            clickFunction.accept(null);
        return true;
    }
    
    public void setHidden(boolean b){
        hidden = b;
        disabled = b;
    }
    
    public boolean isHidden(){
        return hidden;
    }
    
    public void update(){
        if(!disabled){
            if(isMouseIn() && !mouseEntered && RvB.cursor != Cursor.GRAB){
                RvB.setCursor(Cursor.POINTER);
                mouseEntered = true;
            }
            else if(!isMouseIn() && mouseEntered && RvB.cursor != Cursor.GRAB){
                RvB.setCursor(Cursor.DEFAULT);
                mouseEntered = false;
            }
            if(isClicked(0))
                click();
        }
            
        render();
    }
    
    private void render(){
        if(hidden)
            return;
            
        // background
        if(rgb != null)
            RvB.drawFilledRectangle((x-width/2), (y-height/2), width, height, rgb, 1f, null);
        if(bg != null){
            if(itemFramed)
                RvB.drawFilledRectangle((x-width/2+(int)(5*ref)), (y-height/2+(int)(5*ref)), width-(int)(10*ref), height-(int)(10*ref), null, 1f, bg);
            else
                RvB.drawFilledRectangle((x-width/2), (y-height/2), width, height, null, 1f, bg);
        }
        // hover
        if((isMouseIn() || selected) && borderRgb != null && !disabled)
            RvB.drawRectangle(x-width/2, y-height/2, width, height, borderRgb, 1f, 4);
        // text
        if(text != null && font != null)
            RvB.drawString(x, y, indexSwitch >= 0 ? text.getLines()[indexSwitch] : text.getText(), font);
        // lock
        if(locked){
            RvB.drawFilledRectangle((x-width/2), (y-height/2), width, height, (rgb != null ? rgb : RvB.colors.get("green_semidark")), 0.5f, null);
            int size = (int) Math.min(width-width%ref, height-(height%ref));
            RvB.drawFilledRectangle((x-size/2), (y-size/2), size, size, null, 1f, RvB.textures.get("lock"));
        }
    }
    
    public void drawText(String text, UnicodeFont font){
        drawText(0, 0, text, font);
    }
    
    public void drawText(int x, int y, String text, UnicodeFont font){
        RvB.drawString(this.x+x, this.y+y, text, font);
    }
    
    private boolean isMouseIn(){
        int MX = Mouse.getX(), MY = RvB.windHeight-Mouse.getY();
        return (!hidden && MX >= x-width/2 && MX <= x+width/2 && MY >= y-height/2 && MY <= y+height/2);
    }
    
    public boolean isHovered(){
        return isMouseIn() && !disabled;
    }
    
    public boolean isClicked(int but){ 
        return (!disabled && isMouseIn() && Mouse.isButtonDown(but) && !mouseDown);
    }
    
    public void setBG(Texture t){
        bg = t;
    }
    
    public void setSelected(boolean selected){
        this.selected = selected;
    }
    
    public void setItemFramed(boolean b){
        itemFramed = b;
    }
    
    public boolean isEnabled(){
        return !disabled;
    }
    
    public void disable(){
        disabled = true;
        clickSound = false;
    }
    
    public void enable(){
        if(locked)
            return;
        disabled = false;
        clickSound = true;
    }
    
    public void lock(){
        locked = true;
        disable();
    }
    
    public void unlock(){
        locked = false;
        disabled = false;
        clickSound = true;
    }
    
    public boolean isLocked(){
        return locked;
    }
    
    public void disableClickSound(){
        clickSound = false;
    }
    
    public void enableClickSound(){
        clickSound = true;
    }
    
    public void setClickSound(Clip sound, SoundManager.Volume v){
        click = sound;
        SoundManager.Instance.setClipVolume(click, v);
    }
    
    public int getNbClicks(){
        return nbClicks;
    }
    
    public int getX(){
        return x;
    }
    
    public int getY(){
        return y;
    }
    
    public void setX(int x){
        this.x = x;
    }
    
    public void setY(int y){
        this.y = y;
    }
    
    public void setText(Text text, UnicodeFont font){
        this.text = text;
        this.font = font;
    }
    
    public Text getText(){
        return text;
    }
    
    public int getW(){
        return width;
    }
    
    public int getH(){
        return height;
    }
}
