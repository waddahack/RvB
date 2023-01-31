package Buffs;

import managers.TextManager.Text;
import static rvb.RvB.game;

public class Upgrade extends Buff{
    
    public Upgrade(){
        super("upgrade", Text.BUFF_UPGRADE, Text.BUFF_UPGRADE_DESC, 5);
    }
    
    @Override
    public void pick(){
        super.pick();
        
        game.raztech.range *= 1.1;
        game.raztech.getUpgrades().get(0).setValue(game.raztech.range);
        
        game.raztech.power *= 1.2;
        game.raztech.getUpgrades().get(1).setValue(game.raztech.power);
        
        game.raztech.shootRate *= 1.08f;
        game.raztech.getUpgrades().get(2).setValue(game.raztech.shootRate);
    }
}
