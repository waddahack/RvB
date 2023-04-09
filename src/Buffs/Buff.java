package Buffs;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.Serializable;
import java.util.Collections;
import java.util.Stack;
import managers.TextManager.Text;
import org.newdawn.slick.opengl.Texture;
import rvb.RvB;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "id")
@JsonSubTypes({
    @JsonSubTypes.Type(value = GetPowerTower.class, name = "GetPowerTower"),
    @JsonSubTypes.Type(value = GetRangeTower.class, name = "GetRangeTower"),
    @JsonSubTypes.Type(value = GetShootRateTower.class, name = "GetShootRateTower"),
    @JsonSubTypes.Type(value = OS.class, name = "OS"),
    @JsonSubTypes.Type(value = Slow.class, name = "Slow"),
    @JsonSubTypes.Type(value = UpPowerTower.class, name = "UpPowerTower"),
    @JsonSubTypes.Type(value = UpRangeTower.class, name = "UpRangeTower"),
    @JsonSubTypes.Type(value = UpShootRateTower.class, name = "UpShootRateTower"),
    @JsonSubTypes.Type(value = Upgrade.class, name = "Upgrade"),
    @JsonSubTypes.Type(value = XP.class, name = "XP")
})
public abstract class Buff implements Serializable{
    
    public String id;
    public Text name;
    public Texture logo;
    public int nbPick, nbMaxPick;
    private Text description;
         
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
        buffs.add(new Upgrade());
        buffs.add(new Slow());
        buffs.add(new XP());
        buffs.add(new OS());
        buffs.add(new GetPowerTower());
        buffs.add(new GetRangeTower());
        buffs.add(new GetShootRateTower());
        Collections.shuffle(buffs);
        return buffs;
    }
    
    public void pick(){
        nbPick++;
        RvB.game.buffsUsed += id+";";
        RvB.debug("buff "+id);
    }
    
    public String[] getDescription(){
        return description.getLines();
    }
    
    public boolean isAnyLeft(){
        return (nbMaxPick < 0 || nbPick < nbMaxPick);
    }
    
    @Override
    public String toString(){
        return "{"+
                "\"id\":\""+id+"\", "+
                "\"nbPick\":"+nbPick+
               "}";
    }
}
