package Buffs;

import java.util.Stack;
import managers.TextManager.Text;
import org.newdawn.slick.opengl.Texture;
import rvb.RvB;
import static rvb.RvB.ref;
import ui.Button;
import ui.Overlay;

public abstract class Buff {
    
    public String id;
    public Text name;
    public Texture logo;
    public int nbPick, nbMaxPick;
    public String description;
    protected Overlay card;
            
    public Buff(String id, Text name, String description, int nbMaxPick){
        this.id = id;
        this.name = name;
        this.description = description;
        this.logo = RvB.textures.get("buff_"+id);
        this.nbMaxPick = nbMaxPick;
        nbPick = 0;
    }
    
    public static Stack<Buff> initBuffStack(){
        Stack<Buff> buffs = new Stack<>();
        buffs.add(new Upgrade());
        buffs.add(new Slow());
        return buffs;
    }
    
    public void pick(){
        nbPick++;
    }
    
    public boolean isAnyLeft(){
        return (nbMaxPick < 0 || nbPick < nbMaxPick);
    }
    
    public Overlay buildCard(){
        card = new Overlay(1000, 1000, (int)(216*ref), (int)(216*ref));
        card.setBG(RvB.textures.get("board"), 0.8f);
        card.setBorder(RvB.colors.get("green_dark"), 4, 1);
        card.addImage(card.getW()/2, (int)(10*ref+logo.getHeight()/2), (int)(32*ref), (int)(32*ref), logo);
        Button b = new Button(0, 0, card.getW(), card.getH(), RvB.colors.get("green_semidark"), RvB.colors.get("green_dark"));
        b.setFunction(__ -> {
            pick();
        });
        card.addButton(b);
        return card;
    }
    
    public void renderCard(){
        card.drawText(card.getW()/2, card.getH()/2, description, RvB.fonts.get("normal"));
    }
}
