package Buffs;

import java.util.Collections;
import java.util.Stack;
import managers.TextManager.Text;
import org.newdawn.slick.opengl.Texture;
import rvb.RvB;

public abstract class Buff {
    
    public String id;
    public Text name;
    public Texture logo;
    public int nbPick, nbMaxPick;
    public Text description;
            
    public Buff(String id, Text name, Text description, int nbMaxPick){
        this.id = id;
        this.name = name;
        this.description = description;
        this.logo = RvB.textures.get("buff_"+id);
        this.nbMaxPick = nbMaxPick;
        nbPick = 0;
    }
    
    public static Stack<Buff> initBuffStack(){
        Stack<Buff> buffs = new Stack<>();
        buffs.add(new Slow());
        Collections.shuffle(buffs);
        buffs.add(new Upgrade());
        return buffs;
    }
    
    public void pick(){
        nbPick++;
    }
    
    public boolean isAnyLeft(){
        return (nbMaxPick < 0 || nbPick < nbMaxPick);
    }
}