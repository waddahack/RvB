package towers;

import managers.TextManager.Text;
import org.newdawn.slick.opengl.Texture;
import rvb.RvB;

public class Buff {
    
    public static String[] BuffsID = {"upgrade", "slow"};
    
    public String id;
    public Text name;
    public Texture cardImage, logo;
    public int nbMaxPick;
            
    public Buff(String id){
        this.id = id;
        build();
    }
    
    private void build(){
        switch(id){
            case "upgrade":
                name = Text.BUFF_UPGRADE;
                cardImage = RvB.textures.get("buffUpgrade");
                logo = RvB.textures.get("buffUpgradeLogo");
                nbMaxPick = -1;
                break;
            case "slow":
                name = Text.BUFF_SLOW;
                cardImage = RvB.textures.get("buffSlow");
                logo = RvB.textures.get("buffSlowLogo");
                nbMaxPick = 1;
                break;
        }
    }
    
    public void pick(){
        switch(id){
            case "upgrade":
                upgradeRaztech();
                break;
            case "slow":
                addSlowBuffRaztech();
                break;
        }
    }

    private void upgradeRaztech() {
        System.out.println("buff upgrade");
    }

    private void addSlowBuffRaztech() {
        System.out.println("buff slow");
    }
}
